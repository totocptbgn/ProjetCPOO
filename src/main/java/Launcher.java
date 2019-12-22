import java.nio.file.Path;
import java.util.Map;
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
	 * @return id du launcher
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
