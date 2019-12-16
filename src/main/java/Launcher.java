import java.util.concurrent.CompletableFuture;

public interface Launcher<T> {
	public String getNom();
	public state getEtat();

	public void pause(); 							// WORK -> WAIT
	public CompletableFuture<Boolean> restart(); 	// WAIT -> WORK
	public void delete(); 							// * -> FAIL
	public CompletableFuture<Boolean> start(); 		// NEW -> WORK

	public static enum state {
		STOP,		// Etape entre WAIT et WORK : WAIT -> STOP -> WORK
		WORK,		// En cours de téléchargement
		WAIT,		// En pause
		NEW,		// Creé sans avoir été lancé
		FAIL,		// Echec du téléchargement
		SUCCESS		// Téléchargement fini
	}
}
