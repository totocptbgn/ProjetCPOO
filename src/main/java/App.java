/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class App {
    
    public static void main(String[] args) throws Exception {
    	/*Thread t = new Thread(() -> {
			try {
				Interface.main(args);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    	t.setDaemon(true);
    	t.start();
    	*/
    	/*
    	Gestionnaire g=new Gestionnaire();
    	g.addLauncher("https://www.irif.fr/~sighirea//cours/reseauxM/java.url.html");
    	for(Launcher l: g.list()) {
    		System.out.println(l.getNom()+" "+l.getSizeLeft()+" "+l.getEtat()+"\n");
    	}
    	g.launch();
    	long time = System.currentTimeMillis();
    	while(System.currentTimeMillis()-time<3000);
    	for(Launcher l: g.list()) {
    		System.out.println(l.getNom()+" "+l.getSizeLeft()+" "+l.getEtat()+"\n");
    	}
    	System.out.print("Do pause : "+g.pause()+"\n");
    	time = System.currentTimeMillis();
    	
    	//while(System.currentTimeMillis()-time<2000);
    	for(Launcher l: g.list()) {
    		System.out.println(l.getNom()+" "+l.getSizeLeft()+" "+l.getEtat()+"\n");
    	}
    	System.out.print("Do restart : "+g.restart()+"\n");
    	time = System.currentTimeMillis();
    	
    	while(System.currentTimeMillis()-time<5000);
    	for(Launcher l: g.list()) {
    		System.out.println(l.getNom()+" "+l.getSizeLeft()+" "+l.getEtat()+"\n");
    	}
    	*/
    	 GestionnaireAspirateur g = new GestionnaireAspirateur();
    	 g.addAspirateurPagesWithImages("http://matdisblog.informatique.univ-paris-diderot.fr/2019/11/22/plus-court-chemin-dans-une-grille-mathman-a-la-rescousse/");
    	 g.getAspirateur(1).limit(30);
    	 g.aspirateurToLauncher(1).join();
    	 
    	 System.out.println("---before launch---");

    	 for(Launcher l:g.getGestionnaire().listOfAll()) {
    		 System.out.println(l.getNom()+" "+l.getEtat());
    	 }
    	 
    	 CompletableFuture<Optional<Map<Path, String>>> cf = g.getGestionnaire().launch();
    	 
    	 System.out.println("---before pause---");
    	 for(Launcher l:g.getGestionnaire().listOfAll()) {
    		 System.out.println(l.getNom()+" "+l.getEtat());
    	 }
    	 Thread.sleep(1000);
    	 System.out.println(g.getGestionnaire().pause());
    	 System.out.println("---after pause---");
    	 for(Launcher l:g.getGestionnaire().listOfAll()) {
    		 System.out.println(l.getNom()+" "+l.getEtat());
    	 }
    	 
    	 cf = g.getGestionnaire().restart();
    	 System.out.println("---before restart---");
    	 for(Launcher l:g.getGestionnaire().listOfAll()) {
    		 System.out.println(l.getNom()+" "+l.getEtat());
    	 }
    	 System.out.println("---after restart---");
    	 
    	 cf.join();
    	 System.out.println("---after end---");
    	 for(Launcher l:g.getGestionnaire().listOfAll()) {
    		 System.out.println(l.getNom()+" "+l.getEtat());
    	 }
    	 
    	 
    	 
    	 
    }
}
