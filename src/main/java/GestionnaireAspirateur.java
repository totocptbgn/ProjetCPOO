import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Decorateur de Gestionnaire en ajoutant les aspirateurs
 *
 */
public class GestionnaireAspirateur {
	private final Gestionnaire g;
	private final Deque<Aspirateur> aspirateurs = new ConcurrentLinkedDeque<>();

	public GestionnaireAspirateur() {
		g = new Gestionnaire();
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
	
}
