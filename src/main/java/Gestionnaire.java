import java.io.IOException;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;


/*
 * Gere l'ensemble des téléchargements
 * Il contient une liste de téléchargements en attente
 */
public class Gestionnaire {
	// Liste des telechargements
	private final Deque<Launcher> newQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements instanciés
	private final Deque<Launcher> waitQueue = new ConcurrentLinkedDeque<>();   // La file d'attente des téléchagements en pause
	private final Deque<Launcher> launchQueue = new ConcurrentLinkedDeque<>(); // La file d'attente des téléchagements en cours de téléchargements
	private final Deque<Launcher> endQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements finis (ou interrompus)
	private final CompletableFuture<Boolean> not_possible = CompletableFuture.completedFuture(false);
	/**
	 * Dernier launcher non lancé
	 */
	public Launcher getCurrentNew() {
		return newQueue.peek();
	}
	
	/**
	 * Dernier launcher en attente
	 */
	public Launcher getCurrentWait() {
		return waitQueue.peek();
	}
	
	/**
	 * Dernier launcher lancé
	 */
	public Launcher getCurrentLaunch() {
		return launchQueue.peek();
	}
	
	public Gestionnaire() {

	}
	
	public boolean changeCurrentLauncher(String nom,Deque<Launcher> queue) {
		Launcher l=queue.parallelStream().reduce(null, (a,e) -> e.getNom().equals(nom)?e:null);
		if (l != null) {
			queue.remove(l);
			queue.push(l);
			return true;
		}
		return false;
	}
	
	// Lance le launcher au dessus de la pile
	public CompletableFuture<Boolean> launch() {
		if (getCurrentNew() != null && this.getCurrentNew().getEtat() == Launcher.state.NEW) {
			Launcher currentNew = newQueue.pop();
			launchQueue.push(currentNew);


			return currentNew.start().thenApplyAsync(e -> { if(e && launchQueue.remove(currentNew)) { endQueue.add(currentNew); } return e;});
		}
		return not_possible;
	}
	
	/**
	 * Lance le telechargement,
	 * @param String launcher nom du telechargement
	 * @return : si celui ci n'existe pas renvoie faux
	 */
	public CompletableFuture<Boolean> launch(String launcher) {
		if (!changeCurrentLauncher(launcher,newQueue)) {
			return not_possible;
		}
		return this.launch();
		
	}
	
	public boolean delete() {
		if (getCurrentLaunch() != null) {
			Launcher l = launchQueue.pop();
			boolean b = l.delete();
			if(b) endQueue.add(l);
			return b;
		}
		return false;
	}
	
	public boolean delete(String launcher) {
		if (!changeCurrentLauncher(launcher,launchQueue)) {
			return false;
		}
		return this.delete();
		
	}
	
	public boolean pause() {
		if (getCurrentLaunch() != null && this.getCurrentLaunch().getEtat() == Launcher.state.WORK) {
			Launcher l = launchQueue.pop();
			boolean b = l.pause();
			if(b) waitQueue.add(l);
			return b;
		}
		return false;
	}
	
	public boolean pause(String launcher) {
		if (!changeCurrentLauncher(launcher,launchQueue)) {
			return false;
		}
		return this.pause();
	}
	
	public CompletableFuture<Boolean> restart() {
		if (this.getCurrentWait()!= null && this.getCurrentWait().getEtat() == Launcher.state.WAIT) {
			Launcher l = waitQueue.pop();
			launchQueue.push(l);
			
			return l.restart().thenApplyAsync((e) -> { if(launchQueue.remove(l)) { endQueue.add(l); } return e;});
		}
		return not_possible;
	}
	
	public CompletableFuture<Boolean> restart(String launcher) {
		if (!changeCurrentLauncher(launcher,waitQueue)) {
			return not_possible;
		}
		return this.restart();
	}
	
	
	public void addLauncher(String URL) throws IOException {
		Launcher l = new LauncherTelechargement(URL);
		newQueue.push(l);
	}
	

	
	// Liste des noms et etats des launchers non lancé
	public Set<Launcher> listNew() {
		return newQueue.stream().collect(Collectors.toSet());
	}
	
	//liste des noms et etats des launchers en pause
	public Set<Launcher> listWait() {
		return waitQueue.stream().collect(Collectors.toSet());
	}
	
	//liste des noms et etats des launchers en pause
	public Set<Launcher> listLaunch() {
		return launchQueue.stream().collect(Collectors.toSet());
	}
	
	//liste des noms et etats des launchers terminés/arétés
	public Set<Launcher> listEnd() {
		return endQueue.stream().collect(Collectors.toSet());
	}
	
	//liste des noms et etats des launchers
	public Set<Launcher> list() {
		Set<Launcher> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		return l;
	}
	
	//liste des noms et etats des launchers
		public Set<Launcher> listOfAll() {
			Set<Launcher> l = listLaunch();
			l.addAll(listWait());
			l.addAll(listNew());
			l.addAll(listEnd());
			return l;
		}
	
	
	
	
}
