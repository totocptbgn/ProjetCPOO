import java.io.File;
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
	private long size;
	private final String URL;
	private final File repository;
	public String getURL() {
		return this.URL;
	}

	public long getSize() {
		return this.size;
	}

	public TacheTelechargement(String URL,File f) throws MalformedURLException, IOException {
		this.URL = URL;
		repository = f;
		try {
			HttpURLConnection conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		}
		catch (MalformedURLException e) {
			this.size = -1;
		}
	}

	/*
	 * Renvoie le nom qui sera donné à la page dans notre fichier
	 */
	public String getPage() {
		String[] tab = URL.split("/");
		return tab[tab.length-1];
	}

	public synchronized void run() {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();

		try {
			// pour les tests
			// Thread.sleep(3000);
			// non Asynch pour pouvoir l'areter
			HttpResponse<Path> hr = client.send(request, BodyHandlers.ofFile(Paths.get(repository.getPath()+"/"+this.getPage())));
			// System.out.print("done\n");
		} catch (java.net.ConnectException e) {
			throw new UnsupportedOperationException();
		}
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
			//interruption -> on ne fait rien de spécial (on observe l'arret grace à cancel car on veut pouvoir connaitre les taches même en cas d'arret)
			//System.out.print("stopped\n");
		}
	}
}
