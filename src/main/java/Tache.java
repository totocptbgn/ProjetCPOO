import java.util.Set;

/*
 * Telechargement d'une page
 */
public final class Tache extends Thread {
	
	private final long size;
	private final String URL;
	
	public String getURL() {
		//doit faire une copie du string?
		return this.URL;
	}
	
	public double getSize() {
		return this.size;
	}
	
	public Tache(String URL) {
		this.URL=URL;
		//TO DO calcul taille
		this.size=0;
	}
	
	//TO DO lance le télécharchement
	public void run() {
		
	}
	
	/*
	 * Renvoie la liste des taches suivantes (les sous liens de la page)
	 */
	public Set<Tache> NextProfondeur() {
		return null;
	}
	
	/*
	 * Même que NextProfondeur mais sans prendre les éléments de without
	 */
	public Set<Tache> NextProfondeur(Set<Tache> without) {
		return null;
	}
	
	
}
