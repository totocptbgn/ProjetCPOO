import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
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
	private final File f = new File("download");
	
	public String pathDownload() throws IOException {
		return f.getAbsolutePath();
	}
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
		if(!f.isDirectory()) f.mkdir();
	}

	/*
	 * Donne le nom d'un launcher grace à son id
	 */
	private String nameOf(int id ,Deque<Launcher> d) {
		for(Launcher l:d) {
			if(l.getId() == id) {
				return l.getNom();
			}
		}
		return null;
	}

	private boolean changeCurrentLauncher(String nom,Deque<Launcher> queue) {
		Launcher l = queue.stream().reduce(null, (a,e) -> nom.equals(e.getNom())?e:a);
		if (l != null) {
			queue.remove(l);
			queue.push(l);
			System.out.println("ok");
			return true;
		}
		return false;
	}

	// Lance le launcher au dessus de la pile
	public CompletableFuture<Optional<Map<Path,String>>> launch() {
		if (getCurrentNew() != null && this.getCurrentNew().getEtat() == Launcher.state.NEW) {
			Launcher currentNew = newQueue.pop();
			launchQueue.push(currentNew);
			return currentNew.start().thenApplyAsync(e -> { if(!e.isEmpty() && launchQueue.remove(currentNew)) { endQueue.add(currentNew); } return e;});
		}
		throw new NullPointerException();
	}

	/**
	 * Lance le telechargement,
	 * @param String launcher nom du telechargement
	 * @return : si celui ci n'existe pas renvoie faux
	 */
	public CompletableFuture<Optional<Map<Path,String>>> launch(String launcher) {
		if (!changeCurrentLauncher(launcher,newQueue)) {
			throw new NullPointerException();
		}
		return this.launch();

	}

	public CompletableFuture<Optional<Map<Path,String>>> launch(int id) {
		String launcher = nameOf(id ,newQueue);
		return this.launch(launcher);

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
		for(Launcher l:this.list()) {
			if(l.getNom().equals(launcher)) {
				boolean b = l.delete();
				
				if(b) { 
					endQueue.remove(l);
					launchQueue.remove(l);
					waitQueue.remove(l);
					newQueue.remove(l);
					endQueue.add(l);
				}
				return b;
			}
		}
		return false;

	}

	public boolean delete(int id) {
		String launcher = nameOf(id ,launchQueue);
		if(launcher==null) {
			launcher = nameOf(id ,endQueue);
		}
		if(launcher==null) {
			launcher = nameOf(id ,newQueue);
		}
		if(launcher==null) {
			launcher = nameOf(id ,waitQueue);
		}
		return this.delete(launcher);

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

	public boolean pause(int id) {
		String launcher = nameOf(id ,launchQueue);
		return this.pause(launcher);
	}


	public CompletableFuture<Optional<Map<Path,String>>> restart() {
		if (this.getCurrentWait()!= null && this.getCurrentWait().getEtat() == Launcher.state.WAIT) {
			Launcher l = waitQueue.pop();
			launchQueue.push(l);

			return l.restart().thenApplyAsync((e) -> { if(!e.isEmpty() && launchQueue.remove(l)) { endQueue.add(l); } return e;});					
		}
		throw new NullPointerException();
	}

	public CompletableFuture<Optional<Map<Path,String>>> restart(String launcher) {
		if (!changeCurrentLauncher(launcher,waitQueue)) {
			throw new NullPointerException();
		}
		return this.restart();
	}

	public CompletableFuture<Optional<Map<Path,String>>> restart(int id) {
		String launcher = nameOf(id ,waitQueue);
		return this.restart(launcher);
	}


	public void addLauncher(String URL) throws IOException {
		Launcher l = new LauncherTelechargement(URL);
		newQueue.push(l);
	}

	public void addLauncher(String URL,Set<String> s) throws IOException {
		Launcher l = new LauncherTelechargement(URL,s);
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
