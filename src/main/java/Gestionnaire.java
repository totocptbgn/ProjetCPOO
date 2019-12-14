import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sun.tools.javac.util.Pair;

/*
 * Gere l'ensemble des téléchargements
 * Il contient une liste de téléchargements en attente
 */
public class Gestionnaire {
	// Liste des telechargements
	private final Deque<Launcher> newQueue = new ArrayDeque<>();    // La file d'attente des téléchagements instanciés
	private final Deque<Launcher> waitQueue = new ArrayDeque<>();   // La file d'attente des téléchagements en pause
	private final Deque<Launcher> launchQueue = new ArrayDeque<>(); // La file d'attente des téléchagements en cours de téléchargements
	private final Deque<Launcher> endQueue = new ArrayDeque<>();    // La file d'attente des téléchagements finis (ou interrompus)
	
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
	
	// Lance le launcher au dessus de la pile
	public boolean launch() {
		if (getCurrentNew() != null) {
			Launcher currentNew = newQueue.pop();
			currentNew.start();
			launchQueue.push(currentNew);
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
		if (!changeNewCurrentLauncher(launcher,newQueue)) {
			return false;
		}
		this.launch();
		return true;
		
	}
	
	public void addLauncher(String URL) {
		Launcher l = new LauncherTelechargement(URL);
		newQueue.push(l);
	}
	
	public void delete() {
		Launcher l = launchQueue.pop();
		l.delete();
		endQueue.add(l);
	}
	
	public boolean changeNewCurrentLauncher(String nom,Deque<Launcher> queue) {
		Launcher l=queue.parallelStream().reduce(null, (a,e) -> e.getNom().equals(nom)?e:null);
		if (l != null) {
			queue.remove(l);
			queue.push(l);
			return true;
		}
		return false;
	}
	
	// Liste des noms et etats des launchers non lancé
	public List<Pair<String, Launcher.state>> listNew() {
		return (List<Pair<String, Launcher.state>>) newQueue.parallelStream().map((l)->new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList());
	}
	
	//liste des noms et etats des launchers en pause
	public List<Pair<String, Launcher.state>> listWait() {
		return (List<Pair<String, Launcher.state>>) waitQueue.parallelStream().map((l)->new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList());
	}
	
	//liste des noms et etats des launchers en pause
	public List<Pair<String, Launcher.state>> listLaunch() {
		return (List<Pair<String, Launcher.state>>) launchQueue.parallelStream().map((l)->new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList());
	}
	
	//liste des noms et etats des launchers terminés/arétés
		public List<Pair<String, Launcher.state>> listEnd() {
			return (List<Pair<String, Launcher.state>>) endQueue.parallelStream().map((l)->new Pair<>(l.getNom(),l.getEtat())).collect(Collectors.toList());
		}
	
	//liste des noms et etats des launchers
	public List<Pair<String, Launcher.state>> list() {
		List<Pair<String, Launcher.state>> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		return l;
	}
	
	//liste des noms et etats des launchers
		public List<Pair<String, Launcher.state>> listOfAll() {
			List<Pair<String, Launcher.state>> l = listLaunch();
			l.addAll(listWait());
			l.addAll(listNew());
			l.addAll(listEnd());
			return l;
		}
	
	
	
	
}
