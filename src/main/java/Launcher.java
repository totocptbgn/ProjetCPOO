
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Launcher {
	public String getNom();
	public state getEtat();

	public boolean pause(); 						// WORK -> WAIT
	public CompletableFuture<Set<Path>> restart(); 	// WAIT -> WORK
	public boolean delete(); 						// * -> FAIL
	public CompletableFuture<Set<Path>> start(); 	// NEW -> WORK
	public long getSizeLeft();
	public long getTotalSize();

	public static enum state {
		STOP,		// Etape entre WAIT et WORK : WAIT -> STOP -> WORK
		WORK,		// En cours de téléchargement
		WAIT,		// En pause
		NEW,		// Creé sans avoir été lancé
		FAIL,		// Echec du téléchargement
		SUCCESS   	// Téléchargement fini
	}
}
