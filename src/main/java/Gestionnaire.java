import java.util.Set;

/*
 * Gere l'ensemble des téléchargements
 */
public class Gestionnaire {
	//Liste des telechargements en attente 
    //TO DO :initialiser
	private /*final*/ Set<Launcher> wait;
	
	//Le launcher en cours de changement
	private Launcher current;
	
	public Launcher getCurrentLaunch() {
		return current;
	}
	
	public Gestionnaire() {
		
	}
	
	//lance le launcher current
	public void launch() {
		wait.remove(current);
		current.launch();
	}
	
	//TO DO ajoute un Launch et le met en current
	public void addLaunch(String URL) {
		
	}
	
	
	//TO DO met current le launcher associer au nom
	public void changeCurrentLauch(String nom) {
		
	}
	
	//TO DO renvoie la liste des noms des launchers
	public String[] listOfName() {
		return null;
	}
	
}
