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
			current.start();
		}
	}
	
	/*
	 * lance le telechargement, 
	 * @return : si celui ci n'existe pas renvoie faux
	 */
	public boolean launch(String launcher) {
		Launcher toUse=null;
		for(Launcher l:wait) {
			if(l.getName().equals(launcher)) {
				toUse=l;
			}
		}
		if(toUse==null) {
			return false;
		}
		wait.remove(toUse);
		toUse.start();
		return true;
		
	}
	
	public void addLauncher(String URL) {
		Launcher l = new LauncherTelechargement(URL);
		wait.add(l);
		current = l;
	}
	
	public void changeCurrentLauncher(String nom) {
		Launcher l=wait.stream().reduce(null, (a,e) -> e.getName().equals(nom)?e:null);
		if(l!=null) current = l;
	}
	
	//liste des noms des launchers
	public String[] listOfName() {
		return (String[]) wait.stream().map((l)->l.getName()).toArray();
	}
	
}
