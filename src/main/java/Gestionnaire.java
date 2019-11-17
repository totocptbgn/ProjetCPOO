import java.util.HashSet;
import java.util.Set;

/*
 * Gere l'ensemble des téléchargements
 * Il contient une liste de téléchargements en attente
 */
public class Gestionnaire {
	//Liste des telechargements en attente 
    //Attention : peut-etre pas synchrone
	private /*final*/ Set<Launcher> wait = new HashSet<>();
	
	//Le launcher en cours de changement
	private Launcher current;
	
	public Launcher getCurrentLaunch() {
		return current;
	}
	
	public Gestionnaire() {
		
	}
	
	//lance le launcher current
	public void launch() {
		if(current!=null) {
			wait.remove(current);
			current.launch();
		}
	}
	
	public void addLauncher(String URL) {
		Launcher l = new Launcher(URL);
		wait.add(l);
		current = l;
	}
	
	public void changeCurrentLauncher(String nom) {
		Launcher l=wait.stream().reduce(null, (a,e) -> e.getNom().equals(nom)?e:null);
		if(l!=null) current = l;
	}
	
	//liste des noms des launchers
	public String[] listOfName() {
		return (String[]) wait.stream().map((l)->l.getNom()).toArray();
	}
	
}
