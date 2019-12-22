import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Decorateur de Gestionnaire
 *
 */
public class GestionnaireAspirateur {
	private final Gestionnaire g;
	private final Deque<Aspirateur> aspirateurs = new ConcurrentLinkedDeque<>();
	/**
	 * Dernier launcher non lancé
	 */
	public Launcher getCurrentNew() {
		return g.getCurrentNew();
	}

	/**
	 * Dernier launcher en attente
	 */
	public Launcher getCurrentWait() {
		return g.getCurrentWait();
	}

	/**
	 * Dernier launcher lancé
	 */
	public Launcher getCurrentLaunch() {
		return g.getCurrentLaunch();
	}

	public GestionnaireAspirateur() {
		g = new Gestionnaire();
	}
	
	/*
	 * Aspirateur n'aspirant pas
	 */
	public int addPage(String URL) {
		Aspirateur a = Aspirateur.aspirateurNormal(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	/*
	 * Aspirateur aspirant les pages
	 */
	public int addAspirateurPages(String URL) {
		Aspirateur a = Aspirateur.aspirateurPages(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	/*
	 * Aspirateur aspirant les images
	 */
	public int addAspirateurImages(String URL) {
		Aspirateur a = Aspirateur.aspirateurImages(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	
	/*
	 * Aspirateur aspirant les pages et les images
	 */
	public int addAspirateurPagesWithImages(String URL) {
		Aspirateur a = Aspirateur.aspirateurImagesPages(URL);
		aspirateurs.add(a);
		return a.getId();
	}


	// Lance le launcher au dessus de la pile
	public CompletableFuture<Optional<Map<Path,String>>> launch() {
		return g.launch();
	}

	/**
	 * Lance le telechargement,
	 * @param String launcher nom du telechargement
	 * @return : si celui ci n'existe pas renvoie faux
	 */
	public CompletableFuture<Optional<Map<Path,String>>> launch(String launcher) {
		return g.launch(launcher);

	}

	public CompletableFuture<Optional<Map<Path,String>>> launch(int id) {
		return g.launch(id);
	}

	public boolean delete() {
		return g.delete();
	}

	public boolean delete(String launcher) {
		return g.delete(launcher);

	}

	public boolean delete(int id) {
		return g.delete(id);

	}


	public boolean pause() {
		return g.pause();
	}

	public boolean pause(String launcher) {
		return g.pause(launcher);
	}

	public boolean pause(int id) {
		return g.pause(id);
	}


	public CompletableFuture<Optional<Map<Path,String>>> restart() {
		return g.restart();
	}

	public CompletableFuture<Optional<Map<Path,String>>> restart(String launcher) {
		return g.restart(launcher);
	}

	public CompletableFuture<Optional<Map<Path,String>>> restart(int id) {
		return g.restart(id);
	}


	public void addLauncher(String URL) throws IOException {
		g.addLauncher(URL);
	}
	
	public void addLauncher(Aspirateur a) throws IOException {
		g.addLauncher(a.getBaseURL(), a.getContent());
	}
	
	

	// Liste des noms et etats des launchers non lancé
	public Set<Launcher> listNew() {
		return g.listNew();
	}

	//liste des noms et etats des launchers en pause
	public Set<Launcher> listWait() {
		return g.listWait();
	}

	//liste des noms et etats des launchers en pause
	public Set<Launcher> listLaunch() {
		return g.listLaunch();
	}

	//liste des noms et etats des launchers terminés/arétés
	public Set<Launcher> listEnd() {
		return g.listEnd();
	}

	//liste des noms et etats des launchers
	public Set<Launcher> list() {
		return g.list();
	}

	//liste des noms et etats des launchers
	public Set<Launcher> listOfAll() {
		return g.listOfAll();
	}
	
	/*
	 * Recupere un aspirateur avec son URL
	 */
	public Aspirateur getAspirateur(String URL) {
		for(Aspirateur a : aspirateurs) {
			if(a.getBaseURL().equals(URL)) {
				return a;
			}
		}
		return null;
	}
	
	/*
	 * Recupere un aspirateur avec son id
	 */
	public Aspirateur getAspirateur(int id) {
		for(Aspirateur a : aspirateurs) {
			if(a.getId()==id) {
				return a;
			}
		}
		return null;
	}
	
}
