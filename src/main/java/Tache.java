import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/*
 * Telechargement d'une page
 */
final class Tache extends Thread {
	//mettre un httpClient par thread?
	private static final HttpClient client = HttpClient.newHttpClient();
	private final long size;
	private final String URL;
	/*
	private final Tache father;
	*/
	public String getURL() {
		return this.URL;
	}
	
	public double getSize() {
		return this.size;
	}
	
	/*
	public long getProfondeur() {
		if(father==null) return 0;
		return father.getProfondeur()+1;
	}
	*/
	
	public Tache(String URL) {
		this.URL=URL;
		//TO DO calcul taille
		this.size=0;
		//this.father=null;
	}
	
	//pour test pas vraiment opti
	private void get(String uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(uri))
              .build();

        HttpResponse<Path> response =
              client.send(request, BodyHandlers.ofFile(Paths.get("body.html")));

        System.out.println("Response in file:" + response.body());
        HttpResponse<Path> response2 =
                client.send(request, BodyHandlers.ofFile(Paths.get("body.css")));
    }
	
	//tache avec parent 
	private Tache(String URL, Tache father) {
		this.URL=URL;
		//TO DO calcul taille
		this.size=0;
		//this.father=father;
	}
	
	/*
	 * Renvoie le nom qui sera donné à la page dans notre fichier
	 */
	private String getPage() {
		return "body"+".html";
	}
	
	//test pour l'instant
	public void run() {
		try {
			this.get(URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Renvoie la liste des taches suivantes (les sous liens de la page)
	 */
	public Set<Tache> NextProfondeur() {
		return new HashSet<Tache>();
	}
	
	/*
	 * Même que NextProfondeur mais sans prendre les éléments de without
	 */
	public Set<Tache> NextProfondeur(Set<Tache> without) {
		return new HashSet<Tache>();
	}
	
	
	
	
}
