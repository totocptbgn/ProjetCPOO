
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface interne de Launcher
 */
interface LauncherIntern extends Launcher {
	
	/**
	 * NEW | WAIT | STOP | WORK -> FAIL <br/>
	 * Supprime le launcher après time millisecondes s'il le téléchargement n'a pas fini
	 * @param time - temps avant la suppression
	 * @return réussite de la suppression
	 */
	public CompletableFuture<Boolean> deleteAt(int time);
	
	/**
	 * NEW -> WAIT -> WORK <br/>
	 * réalise un téléchargement après time millisecondes
	 * @param time - temps en nanoseconde à attentre
	 * @return renvoie un Optional tel que 
	 * - Vide si le téléchargement est annulé/échoue
	 * - Sinon liste des téléchargements avec pour chaque élément : la où il est téléchargé et le nom de la page
	 */
	public CompletableFuture<Optional<Map<Path,String>>> startAt(int time);
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
	public CompletableFuture<Optional<Map<Path,String>>> restart();
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
	public CompletableFuture<Optional<Map<Path,String>>> start();
	
}
