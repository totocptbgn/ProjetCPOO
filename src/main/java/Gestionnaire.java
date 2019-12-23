import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Gère l'ensemble des téléchargements des launchers <br/>
 * Exceptions : <br/>
 * - IllegalStateException : Erreur inattendu <br/>
 * - RuntimeException "name has failed" : Erreur de modification de fichier <br/>
 * - UnsupportedOperationException : Erreur de connection
 * - NullPointerException : Launcher inexistant
 */
public final class Gestionnaire {
	// Liste des telechargements
	private final Deque<LauncherIntern> newQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements instanciés
	private final Deque<LauncherIntern> waitQueue = new ConcurrentLinkedDeque<>();   // La file d'attente des téléchagements en pause
	private final Deque<LauncherIntern> launchQueue = new ConcurrentLinkedDeque<>(); // La file d'attente des téléchagements en cours de téléchargements
	private final Deque<LauncherIntern> endQueue = new ConcurrentLinkedDeque<>();    // La file d'attente des téléchagements finis (ou interrompus)
	private final File f = new File("download");
	/**
	 * @return renvoie l'emplacement du fichier download
	 * @throws IOException - si une I/O exception se produit
	 */
	public String pathDownload() throws IOException {
		return f.getAbsolutePath();
	}

	/**
	 * @result Dernier launcher non lancé
	 */
	public LauncherIntern getCurrentNew() {
		return newQueue.peek();
	}

	/**
	 * @result Dernier launcher en attente
	 */
	public LauncherIntern getCurrentWait() {
		return waitQueue.peek();
	}

	/**
	 * @result Dernier launcher lancé
	 */
	public LauncherIntern getCurrentLaunch() {
		return launchQueue.peek();
	}

	public Gestionnaire() {
		if(!f.isDirectory()) f.mkdir();
	}

	/**
	 * @param id - id d'un launcher
	 * @param d - Queue où chercher
	 * @return renvoie le nom associé à l'id dans d
	 */
	private String nameOf(int id ,Deque<LauncherIntern> d) {
		for(LauncherIntern l:d) {
			if(l.getId() == id) {
				return l.getNom();
			}
		}
		return null;
	}

	/**
	 * @param nom - nom du téléchargement à mettre en avant
	 * @param queue - queue de téléchargement
	 * @return Renvoie false si le téléchargement n'éxiste pas
	 */
	private boolean changeCurrentLauncher(String nom,Deque<LauncherIntern> queue) {
		LauncherIntern l = queue.stream().reduce(null, (a,e) -> nom.equals(e.getNom())?e:a);
		if (l != null) {
			queue.remove(l);
			queue.push(l);
			return true;
		}
		return false;
	}

	/**
	 * Lance le dernier launcher créé
	 * @return renvoie le ComplétableFuture du launcher (voir documentation start de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> launch() {
		if (getCurrentNew() != null && this.getCurrentNew().getEtat() == Launcher.state.NEW) {
			LauncherIntern currentNew = newQueue.pop();
			launchQueue.push(currentNew);
			return currentNew.start().thenApplyAsync(e -> { if(e.isPresent() && launchQueue.remove(currentNew)) { endQueue.add(currentNew); } return e;});
		}
		throw new NullPointerException();
	}

	/**
	 * Lance le launcher
	 * @param launcher - nom du launcher à lancer
	 * @return renvoie le ComplétableFuture du launcher (voir documentation start de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> launch(String launcher) {
		if (!changeCurrentLauncher(launcher,newQueue)) {
			throw new NullPointerException();
		}
		return this.launch();

	}
	/**
	 * Lance le launcher d'id id
	 * @param id - id du launcher
	 * @return renvoie le ComplétableFuture du launcher (voir documentation start de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> launch(int id) {
		String launcher = nameOf(id ,newQueue);
		return this.launch(launcher);

	}
	/**
	 *  Enlève le dernier launcher éxécuté
	 *  @return réussite de la suppression
	 */
	public boolean delete() {
		if (getCurrentLaunch() != null) {
			LauncherIntern l = launchQueue.pop();
			boolean b = l.delete();
			if(b) endQueue.add(l);
			return b;
		}
		return false;
	}

	/**
	 *  Enlève le launcher avec le nom launcher
	 *  @param launcher - nom du launcher à supprimer
	 *  @return réussite de la suppression
	 */
	public boolean delete(String launcher) {
		for(LauncherIntern l:listOfAllInside()) {
			if(l.getNom().equals(launcher)) {
				boolean b = l.delete();
				if (b) {
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

	/**
	 *  Enlève le launcher avec l'id id
	 *  @param id - id du launcher à supprimer
	 *  @return réussite de la suppression
	 */
	public boolean delete(int id) {
		String launcher = nameOf(id ,launchQueue);
		if(launcher == null) {
			launcher = nameOf(id ,endQueue);
		}
		if(launcher == null) {
			launcher = nameOf(id ,newQueue);
		}
		if(launcher == null) {
			launcher = nameOf(id ,waitQueue);
		}
		return this.delete(launcher);
	}

	/**
	 *  Met en pause le launcher
	 *  @return réussite de la pause
	 */
	public boolean pause() {
		if (getCurrentLaunch() != null && this.getCurrentLaunch().getEtat() == Launcher.state.WORK) {
			LauncherIntern l = launchQueue.pop();
			boolean b = l.pause();
			if(b) waitQueue.add(l);
			return b;
		}
		return false;
	}

	/**
	 *  Met en pause le launcher avec le nom launcher
	 *  @param launcher - nom du launcher à mettre en pause
	 *  @return réussite de la pause
	 */

	public boolean pause(String launcher) {
		if (!changeCurrentLauncher(launcher,launchQueue)) {
			return false;
		}
		return this.pause();
	}

	/**
	 *  Met en pause le launcher avec l'id id
	 *  @param id - id du launcher à mettre en pause
	 *  @return réussite de la pause
	 */
	public boolean pause(int id) {
		String launcher = nameOf(id ,launchQueue);
		return this.pause(launcher);
	}

	/**
	 * Relance le dernier launcher mis en pause
	 * @return  renvoie le ComplétableFuture du launcher (voir documentation restart de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> restart() {
		if (this.getCurrentWait()!= null && this.getCurrentWait().getEtat() == Launcher.state.WAIT) {
			LauncherIntern l = waitQueue.pop();
			launchQueue.push(l);

			return l.restart().thenApplyAsync((e) -> { if(!e.isEmpty() && launchQueue.remove(l)) { endQueue.add(l); } return e;});
		}
		throw new NullPointerException();
	}
	/**
	 * Relance un launcher de nom launcher
	 * @param launcher - nom du launcher à mettre en pause
	 * @return renvoie le ComplétableFuture du launcher (voir documentation restart de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> restart(String launcher) {
		if (!changeCurrentLauncher(launcher,waitQueue)) {
			throw new NullPointerException();
		}
		return this.restart();
	}

	/**
	 * Relance un launcher d'id id
	 * @param id - id du launcher à mettre en pause
	 * @return renvoie le ComplétableFuture du launcher (voir documentation restart de launcher)
	 */
	public CompletableFuture<Optional<Map<Path,String>>> restart(int id) {
		String launcher = nameOf(id ,waitQueue);
		return this.restart(launcher);
	}

	/**
	 * Ajoute un launcher grace à l'URL
	 * @param URL - URL du launcher
	 * @throws IOException - si une I/O exception se produit
	 */
	public void addLauncher(String URL) throws IOException {
		LauncherIntern l = new LauncherTelechargement(URL);
		newQueue.push(l);
	}

	/**
	 * Ajoute un launcher grace à la liste d'URL
	 * @param URL - URL qui servira de base au launcher
	 * @param s - ensemble des URL à télécharger
	 * @throws IOException - si une I/O exception se produit
	 */
	public void addLauncher(String URL,Set<String> s) throws IOException {
		LauncherIntern l = new LauncherTelechargement(URL,s);
		newQueue.push(l);
	}

	/**
	 * @return Liste des launchers non lancés
	 */
	public Set<Launcher> listNew() {
		return new HashSet<>(newQueue);
	}

	/**
	 * @return liste des launchers en pause
	 */
	public Set<Launcher> listWait() {
		return new HashSet<>(waitQueue);
	}

	/**
	 * @return liste des launchers en cours de téléchargement
	 */
	public Set<Launcher> listLaunch() {
		return new HashSet<>(launchQueue);
	}

	/**
	 * @return liste des launchers terminés/supprimés
	 */
	public Set<Launcher> listEnd() {
		return new HashSet<>(endQueue);
	}

	/**
	 * @return liste des launchers non finis
	 */
	public Set<Launcher> list() {
		Set<Launcher> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		return l;
	}

	/**
	 * @return liste de tous les launchers
	 */
	public Set<Launcher> listOfAll() {
		Set<Launcher> l = listLaunch();
		l.addAll(listWait());
		l.addAll(listNew());
		l.addAll(listEnd());
		return l;
	}

	/**
	 * @return liste de tous les launchers
	 */
	private Set<LauncherIntern> listOfAllInside() {
		Set<LauncherIntern> l = launchQueue.stream().collect(Collectors.toSet());
		l.addAll(waitQueue);
		l.addAll(newQueue);
		l.addAll(endQueue);
		return l;
	}


}
