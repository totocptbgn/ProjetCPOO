import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;

/**
 * Téléchargement d'une page
 */

final class TacheTelechargement extends Thread implements Tache {
	private static final HttpClient client = HttpClient.newHttpClient();
	private long size;
	private final String URL;
	private final File repository;
	public String getURL() {
		return this.URL;
	}

	/**
	 * @return taille du fichier ( -1 si introuvable ) 
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * @param URL - URL de la page
	 * @param f - fichier ou sera téléchargé la page
	 * @throws IOException - si une I/O exception se produit
	 */
	public TacheTelechargement(String URL,File f) throws IOException {
		this.URL = URL;
		repository = f;
		try {
			//System.out.print(URL);
			HttpURLConnection conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		}
		catch (MalformedURLException e) {
			this.size = -1;
		}
	}

	/**
	 * @return - Le nom qui sera donné à la page dans notre fichier
	 */
	public String getPage() {
		String[] tab = URL.split("/");
		return tab[tab.length-1];
	}

	/**
	 * Lance la tache <br/>
	 * arret d'une tache -> la fonction ne fait rien <br/>
	 * Pas de connection -> UnsupportedOperationException <br/>
	 */
	public synchronized void run() {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();

		try {
			// pour les tests
			//Thread.sleep(5000);
			// non Asynch pour pouvoir l'areter
			client.send(request, BodyHandlers.ofFile(Paths.get(repository.getPath()+"/"+this.getPage())));
			// System.out.print("done\n");
		} catch (java.net.ConnectException e) {
			//probleme de connection
			throw new LinkageError();
		}
		catch(IOException e) {
			//e.printStackTrace();
			//System.out.println(e.getMessage());
			
			throw new LinkageError();
		}
		catch (InterruptedException e) {
			//interruption -> on ne fait rien de spécial (on observe l'arret grace à cancel car on veut pouvoir connaitre les taches même en cas d'arret)
			//System.out.print("stopped\n");
		}
	}
}
