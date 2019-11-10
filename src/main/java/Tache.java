import java.util.Set;

/*
 * Telechargement d'une page
 */
public final class Tache extends Thread {
	
	private final long size;
	private final String URL;
	private final Tache father;
	public String getURL() {
		//doit faire une copie du string?
		return this.URL;
	}
	
	public double getSize() {
		return this.size;
	}
	
	public long getProfondeur() {
		if(father==null) return 0;
		return father.getProfondeur()+1;
	}
	
	public Tache(String URL) {
		this.URL=URL;
		//TO DO calcul taille
		this.size=0;
		this.father=null;
	}
	
	private Tache(String URL, Tache father) {
		this.URL=URL;
		//TO DO calcul taille
		this.size=0;
		this.father=father;
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
