import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Téléchargement d'une page
 */

final class TacheTelechargement extends Thread implements Tache {
	// Mettre un httpClient par thread?
	private static final HttpClient client = HttpClient.newHttpClient();
	private final long size;
	private final String URL;

	public String getURL() {
		return this.URL;
	}

	public long getSize() {
		return this.size;
	}

	public TacheTelechargement(String URL) throws MalformedURLException, IOException {
		this.URL = URL;
		HttpURLConnection conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
		this.size = conn.getContentLengthLong();
		conn.disconnect();
	}

	// met la page dans un fichier
	private synchronized void get() {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();

		try {
			// pour les tests
			// Thread.sleep(3000);
			// non Asynch pour pouvoir l'areter
			HttpResponse<Path> hr = client.send(request, BodyHandlers.ofFile(Paths.get(this.getPage())));
			// System.out.print("done\n");
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			//System.out.print("stopped\n");
		}
	}

	/*
	 * Renvoie le nom qui sera donné à la page dans notre fichier
	 */
	public String getPage() {
		String[] tab = URL.split("/");
		return tab[tab.length-1];
	}

	public void run() {
		this.get();
	}
}
