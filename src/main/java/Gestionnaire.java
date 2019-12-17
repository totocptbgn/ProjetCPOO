import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import com.sun.tools.javac.util.Pair;

/*
 * Gère l'ensemble des téléchargements
 * Il contient une liste de téléchargements en attente
 */

public class Gestionnaire {

	private final Deque<Launcher> newQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements instanciés
	private final Deque<Launcher> waitQueue = new ConcurrentLinkedDeque<>();   // La file d'attente des téléchagements en pause
	private final Deque<Launcher> launchQueue = new ConcurrentLinkedDeque<>(); // La file d'attente des téléchagements en cours de téléchargements
	private final Deque<Launcher> endQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements finis (ou interrompus)

	// Dernier launcher non lancé
	public Launcher getCurrentNew() {
		return newQueue.peek();
	}

	// Dernier launcher en attente
	public Launcher getCurrentWait() {
		return waitQueue.peek();
	}
	
	// Dernier launcher lancé
	public Launcher getCurrentLaunch() {
		return launchQueue.peek();
	}
	
	public Gestionnaire() {

	}
	
	public boolean changeCurrentLauncher(String nom, Deque<Launcher> queue) {
		Launcher l = queue.parallelStream().reduce(null, (a, e) -> e.getNom().equals(nom)?e:null);
		if (l != null) {
			queue.remove(l);
			queue.push(l);
			return true;
		}
		return false;
	}
	
	// Lance le launcher au dessus de la pile
	public boolean launch() {
		if (getCurrentNew() != null) {
			if(this.getCurrentNew().getEtat() != Launcher.state.NEW) {
				return false;
			}
			Launcher currentNew = newQueue.pop();
			launchQueue.push(currentNew);
			currentNew.start().thenAcceptAsync(e -> { if(launchQueue.remove(currentNew)) { endQueue.add(currentNew); }});			
			return true;
		}
		return false;
	}
	
	/**
	 * Lance le telechargement,
	 * @param String launcher nom du telechargement
	 * @return : si celui ci n'existe pas renvoie faux
	 */

	public boolean launch(String launcher) {
		if (!changeCurrentLauncher(launcher, newQueue)) {
			return false;
		}
		return this.launch();
	}
	
	public boolean delete() {
		if (getCurrentLaunch() != null) {
			Launcher l = launchQueue.pop();
			l.delete();
			endQueue.add(l);
			return true;
		}
		return false;
	}
	
	public boolean delete(String launcher) {
		if (!changeCurrentLauncher(launcher, launchQueue)) {
			return false;
		}
		return this.delete();
	}
	
	public boolean pause() {
		if (getCurrentLaunch() != null) {
			if(this.getCurrentLaunch().getEtat() != Launcher.state.WORK) {
				return false;
			}
			Launcher l = launchQueue.pop();
			l.pause();
			waitQueue.add(l);
			return true;
		}
		return false;
	}
	
	public boolean pause(String launcher) {
		if (!changeCurrentLauncher(launcher, launchQueue)) {
			return false;
		}
		return this.pause();
	}
	
	public boolean restart() {
		if (this.getCurrentWait().getEtat() == Launcher.state.WAIT) {
			Launcher l = waitQueue.pop();
			launchQueue.push(l);
			l.restart().thenAcceptAsync((e) -> { if(launchQueue.remove(l)) endQueue.add(l); });
			return true;
		}
		return false;
	}
	
	public boolean restart(String launcher) {
		if (!changeCurrentLauncher(launcher, waitQueue)) {
			return false;
		}
		return this.restart();
	}

	public void addLauncher(String URL) {
		Launcher l = new LauncherTelechargement(URL);
		newQueue.push(l);
	}
	
	// Liste des noms et états des launchers non lancé
	public List<Pair<String, Launcher.state>> listNew() {
		return (List<Pair<String, Launcher.state>>) newQueue.parallelStream().map(
				(l) -> new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList()
		);
	}
	
	// Liste des noms et états des launchers en pause
	public List<Pair<String, Launcher.state>> listWait() {
		return (List<Pair<String, Launcher.state>>) waitQueue.parallelStream().map(
				(l) -> new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList()
		);
	}
	
	// Liste des noms et états des launchers en pause
	public List<Pair<String, Launcher.state>> listLaunch() {
		return (List<Pair<String, Launcher.state>>) launchQueue.parallelStream().map(
				(l) -> new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList()
		);
	}
	
	// Liste des noms et états des launchers terminés/arétés
	public List<Pair<String, Launcher.state>> listEnd() {
		return (List<Pair<String, Launcher.state>>) endQueue.parallelStream().map(
				(l) -> new Pair<>(l.getNom(), l.getEtat())).collect(Collectors.toList()
		);
	}
	
	// Liste des noms et états des launchers
	public List<Pair<String, Launcher.state>> list() {
		List<Pair<String, Launcher.state>> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		return l;
	}
	
	// Liste des noms et états des launchers
	public List<Pair<String, Launcher.state>> listOfAll() {
		List<Pair<String, Launcher.state>> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		l.addAll(listEnd());
		return l;
	}
}
