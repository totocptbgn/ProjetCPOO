import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Téléchargement d'une page
 */

final class Tache extends Thread {
	// Mettre un httpClient par thread?
	private static final HttpClient client = HttpClient.newHttpClient();
	private final long size;
	private final String URL;

	/*
	 * private final Tache father;
	 */
	public String getURL() {
		return this.URL;
	}

	public double getSize() {
		return this.size;
	}

	/*
	 * public long getProfondeur() { if(father==null) return 0; return
	 * father.getProfondeur()+1; }
	 */

	public Tache(String URL) throws MalformedURLException, IOException {
		this.URL = URL;
		HttpURLConnection conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
		this.size = conn.getContentLengthLong();
		conn.disconnect();
		// this.father=null;
	}

	//met la page dans un fichier
	private synchronized void get() {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
		try {
			//pour les tests
			Thread.sleep(1000);
			//non Asynch pour pouvoir l'areter
			
			
			client.send(request, BodyHandlers.ofFile(Paths.get(this.getPage()))).body();
			System.out.print("done\n");
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.print("stopped\n");
		}
	}

	// tache avec parent
	private Tache(String URL, Tache father) throws MalformedURLException, IOException {
		this.URL = URL;
		HttpURLConnection conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
		this.size = conn.getContentLengthLong();
		conn.disconnect();
		// this.father=father;
	}

	/*
	 * Renvoie le nom qui sera donné à la page dans notre fichier
	 */
	private String getPage() {
		return "body" + ".html";
	}

	public void run()  {
		
		this.get();
		
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
