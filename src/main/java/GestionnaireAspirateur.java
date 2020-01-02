import java.io.IOException;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Decorateur de Gestionnaire en ajoutant les aspirateurs
 * Gère l'ensemble des téléchargements des launchers 
 * Exceptions : 
 * - IllegalStateException : Erreur inattendu <br/>
 * - RuntimeException "name has failed" : Erreur de modification de fichier <br/>
 * - UnsupportedOperationException : Erreur de connection

 */
public class GestionnaireAspirateur {
	private final Gestionnaire g;
	private final Deque<Aspirateur> aspirateurs = new ConcurrentLinkedDeque<>();

	public GestionnaireAspirateur() {
		g = new Gestionnaire();
	}
	
	/**
	 * Suppression de l'aspirateur d'id id
	 * @param id - id de l'aspirateur
	 */
	public void cancel(int id) {
		Aspirateur a = this.getAspirateur(id);
		aspirateurs.remove(a);
		a.cancel();
	}

	/**
	 * Suppression de l'aspirateur de nom nom
	 * @param nom - nom de l'aspirateur
	 */
	public void cancel(String nom) {
		Aspirateur a = this.getAspirateur(nom);
		aspirateurs.remove(a);
		a.cancel();
	}

	/**
	 * @param URL de base de l'aspirateur
	 * @return aspirateur n'aspirant rien
	 */
	public int addPage(String URL) {
		Aspirateur a = Aspirateur.aspirateurNormal(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	/**
	 * @param URL de base de l'aspirateur
	 * @return aspirateur aspirant les pages
	 */
	public int addAspirateurPages(String URL) {
		Aspirateur a = Aspirateur.aspirateurPages(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	/**
	 * @param URL de base de l'aspirateur
	 * @return aspirateur aspirant les images
	 */
	public int addAspirateurImages(String URL) {
		Aspirateur a = Aspirateur.aspirateurImages(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	
	/**
	 * @param URL de base de l'aspirateur
	 * @return aspirateur aspirant les images et les pages
	 */
	public int addAspirateurPagesWithImages(String URL) {
		Aspirateur a = Aspirateur.aspirateurImagesPages(URL);
		aspirateurs.add(a);
		return a.getId();
	}
	
	/**
	 * @return Gestionnaire associé
	 */
	public Gestionnaire getGestionnaire() {
		return g;
	}
	
	/**
	 * Recupère un Aspirateur d'URL de base URL
	 * @param URL - URL de base de l'aspirateur
	 * @return renvoie l'aspirateur trouvé, null sinon
	 */
	public Aspirateur getAspirateur(String URL) {
		for(Aspirateur a : aspirateurs) {
			if(a.getBaseURL().equals(URL)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Recupère un Aspirateur d'id id
	 * @param id - id de l'aspirateur
	 * @return renvoie l'aspirateur trouvé, null sinon
	 */
	public Aspirateur getAspirateur(int id) {
		for(Aspirateur a : aspirateurs) {
			if(a.getId()==id) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * @return Liste des aspirateurs
	 */
	public Set<Aspirateur> listAspirateurs() {
		return aspirateurs.stream().collect(Collectors.toSet());
	}
	/**
	 * transforme un aspirateur d'id id en launcher 
	 * @param id - id de l'aspirateur
	 * @return renvoie un CompletableFuture de la tache
	 */
	public CompletableFuture<Void> aspirateurToLauncher(int id) {
		Aspirateur a = this.getAspirateur(id);
		return a.getContent().thenAcceptAsync(e -> {
			try {
				if(e.isPresent()) {
					Set<String> set = e.get();
					aspirateurs.remove(a);
					g.addLauncher(a.getBaseURL(),set);
				}
			} catch (IOException e1) {
				throw new IllegalStateException();
			}
		});
	}
	
	/**
	 * transforme un aspirateur de nom nom en launcher 
	 * @param nom - nom de l'aspirateur
	 * @return renvoie un CompletableFuture de la tache
	 */
	public  CompletableFuture<Void> aspirateurToLauncher(String nom) {
		Aspirateur a = this.getAspirateur(nom);
		return a.getContent().thenAcceptAsync(e -> {
			try {
				if(e.isPresent()) {
					Set<String> set = e.get();
					aspirateurs.remove(a);
					g.addLauncher(a.getBaseURL(),set);
				}
			} catch (IOException e1) {
				throw new IllegalStateException();
			}
		});
	}
	
	/**
	 * transforme un aspirateur de nom nom en plusieurs launchers 
	 * @param nom - nom de l'aspirateur
	 * @return CompletableFuture correspondant à la fin de la tache
	 */
	public CompletableFuture<Void> aspirateurToLaunchers(String nom) {
		Aspirateur a = this.getAspirateur(nom);
		return a.getContent().thenAcceptAsync(e -> {
			try {
				if(e.isPresent()) {
					Set<String> set = e.get();
					for(String name:set)
						g.addLauncher(name);
					aspirateurs.remove(a);
				}
			} catch (IOException e1) {
				throw new IllegalStateException();
			}
		});
	}
	
	/**
	 * transforme un aspirateur d'id id en plusieurs launchers 
	 * @param id - id de l'aspirateur
	 * @return CompletableFuture correspondant à la fin de la tache
	 */
	public CompletableFuture<Void> aspirateurToLaunchers(int id) {
		Aspirateur a = this.getAspirateur(id);
		return a.getContent().thenAcceptAsync(e -> {
			try {
				if(e.isPresent()) {
					Set<String> set = e.get();
					for(String name:set)
						g.addLauncher(name);
					aspirateurs.remove(a);
				}
			} catch (IOException e1) {
				throw new IllegalStateException();
			}
		});
	}
	
}
