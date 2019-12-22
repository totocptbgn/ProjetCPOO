
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Gère un ensemble de téléchargement <br/>
 * Chaque Launcher sera représenté par un nom et un id
 */
public interface Launcher {
	/**
	 * @return nom de la tache
	 */
	public String getNom();
	/**
	 * NEW -> créé mais non lancé <br/>
	 * WORK -> en cours de téléchargement <br/>
	 * WAIT -> en pause <br/>
	 * FAIL -> annulé <br/>
	 * SUCCESS -> réussi
	 * @return état de la tache
	 */
	public state getEtat();
	/**
	 * WORK -> WAIT
	 * met le téléchargement en pause
	 * @return réussite du téléchargement
	 */
	public boolean pause();
	/**
	 * NEW -> WORK <br/>
	 * lance le téléchargement
	 * @return renvoie un Optional tel que 
	 * - Vide si le téléchargement est annulé/échoue
	 * - Sinon liste des téléchargements avec pour chaque élément : la où il est téléchargé et le nom de la page
	 */
	public CompletableFuture<Optional<Map<Path,String>>> restart(); // WAIT -> WORK
	/**
	 * * -> FAIL <br/>
	 * Suppressions d'une tache et des fichiers en lien avec celui-ci
	 * @return renvoie si le téléchargement à bien été annulé
	 */
	public boolean delete(); 
	/**
	 * PAUSE -> STOP -> WORK
	 * @return renvoie un Optional tel que <br/>
	 * - Vide si le téléchargement est annulé/échoue <br/>
	 * - Sinon liste des téléchargements avec pour chaque élément : la où il est téléchargé et le nom de la page
	 */
	public CompletableFuture<Optional<Map<Path,String>>> start(); // NEW -> WORK
	/**
	 * @return taille restante à télécharger
	 */
	public long getSizeLeft();
	/**
	 * @return taille total à télécharger
	 */
	public long getTotalSize();
	/**
	 * Fonction accessible à n'importe quel état
	 * @return renvoie les chemins ou les pages seront téléchargé avec le nom de la page
	 */
	public Map<Path,String> getPages();
	/**
	 * @return id de la page
	 */
	public int getId();
	/**
	 * NEW -> créé mais non lancé <br/>
	 * WORK -> en cours de téléchargement <br/>
	 * WAIT -> en pause <br/>
	 * FAIL -> annulé <br/>
	 * SUCCESS -> réussi
	 */
	public static enum state {
		STOP,		// Etape entre WAIT et WORK : WAIT -> STOP -> WORK
		WORK,		// En cours de téléchargement
		WAIT,		// En pause
		NEW,		// Creé sans avoir été lancé
		FAIL,		// Echec du téléchargement
		SUCCESS   	// Téléchargement fini
	}
}
