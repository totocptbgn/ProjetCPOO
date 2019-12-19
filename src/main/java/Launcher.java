package downloadmanager;
import java.util.concurrent.CompletableFuture;

public interface Launcher {
	public String getNom();
	public state getEtat();

	public boolean pause(); 	// WORK -> WAIT
	public CompletableFuture<Boolean> restart(); 	// WAIT -> WORK
	public boolean delete(); 	// * -> FAIL
	public CompletableFuture<Boolean> start(); 	// NEW -> WORK
	public long getSizeLeft();
	public long getTotalSize();
	public static enum state {
		STOP/*etape entre WAIT et WORK : WAIT -> STOP -> WORK*/, WORK/*En cours de téléchargement*/, WAIT/*en pause*/, NEW/*Creé sans avoir été lancé*/, FAIL/*Echec du téléchargement*/, SUCCESS/*Telechargement fini*/;
	}
}
