import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

/*
 * Gere un ensemble de téléchargement créé à partir d'un URL (URL et ses enfants)
 * Chaque Launcher sera représenté par un nom
 */
public final class LauncherTelechargement extends Thread implements Launcher<Tache> {
	// limite de nombre de fichier
	private static long MAX = 1;
	
	private Stream<Tache> commandes;
	private List<Tache> elements;
	private boolean limit = true;
	
	private ExecutorService es= Executors.newCachedThreadPool();;
	//permettra à l'utilisateur de choisir ce launcher
	private final String nom;

	// TO DO changer commandes
	public void setLimit(boolean limit) {
		this.limit = limit;
	}

	public String getNom() {
		return nom;
	}
	
	public state getEtat() {
		return etat;
	}

	/*
	 * Creer le launcher
	 * @param String URL : URL de base
	 */
	
	private state etat = state.NEW;
	public LauncherTelechargement(String URL) {
		// Donne les prochains elements à traiter
		// faire à l'exterieur de la classe?
		Supplier<Tache> sup = new Supplier<Tache>() {
			// Non synchrone -> trouver mieux
			Queue<Tache> file = new LinkedList<Tache>();
			// pas tres beau -> surement mieux
			{
				file.add(new Tache(URL));
			}

			@Override
			public Tache get() {
				if (file.isEmpty())
					return null;
				Tache t = file.poll();

				file.addAll(t.NextProfondeur());
				return t;
			}

		};
		
		nom = URL.split("/")[2];

		commandes = Stream.generate(sup);

		if (limit) {
			commandes = commandes.limit(MAX);
		}
	}

	/*
	 *  lance l'ensemble du telechargement 
	 *  (et l'ordre?)(non-Javadoc)
	 */
	public void run() {
		this.etat=etat.LAUNCH;
		
		try {
			if(es.isShutdown()) {
				throw new InterruptedException();
			}
			elements = commandes.collect(Collectors.toList());
			List<Future<Void>> inExecution = es.invokeAll(elements);
			while(!es.isShutdown()) {
				if (elements.stream().allMatch(t -> t.isInterrupted())) {
					throw new InterruptedException();
				}
				if (inExecution.stream().allMatch(f -> f.isDone())) {
					es.shutdown();
				}
			}
			this.etat=etat.SUCCESS;
		} catch (InterruptedException e) {
			this.etat=etat.FAIL;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TO DO : arrete le telechargement pour de bon
	public void delete() {
		es.shutdownNow();
		if(elements!=null) {
			for(Tache t:elements) {
				t.interrupt();
			}
		}
		
	}
	
	// TO DO : met en pause le telechargement
	public void pause() {
		if(elements!=null) {
			try {
			
				for(Tache t:elements) {
					t.wait();
				}
			}
			catch (InterruptedException e) {
				this.etat=etat.FAIL;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void restart() {
		es.notifyAll();
	}

	/*
	 * @param Predicate<Tache> p : La condition sur les taches
	 * permet de prendre les elements acceptant la condition
	 */
	private void addPredicate(Predicate<Tache> p) {
		commandes = commandes.filter(p);
	}

	/*
	 * @param T start : valeur de base de l'accumulateur
	 * @param BiFunction<T, Tache, T> faccu : fonction transformant l'accumulateur en fonction d'un élément tache
	 * @param Predicate<T> p : vérifie si la condition de l'accumulateur est encore vrai
	 * permet de s'arreter quand la condition du predicat devient fausse sur
	 * l'accumulateur -> necessite l'ordre /:
	 */
	private <T> void addPredicateWithAccumulator(T start, BiFunction<T, Tache, T> faccu, Predicate<T> p) {

		Predicate<Tache> pwithaccu = new Predicate<Tache>() {
			T accumulator = start;

			@Override
			public boolean test(Tache t) {
				accumulator = faccu.apply(accumulator, t);
				return p.test(accumulator);
			}

		};

		commandes = commandes.takeWhile(pwithaccu);
	}

	// ajoute une limite au nombre de fichier
	public void limit(long limit) {
		commandes = commandes.limit(limit);
	}
	
	/* limite la taille de téléchargement du site */
	public void limitSize(long size) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getSize(), (x) -> x < size);
	}

	/* Limite la profondeur des pages du téléchargement du site */
	/*
	public void limitProfondeur(long profondeur) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getProfondeur(), (x) -> x < profondeur);
	}
	*/

	// TO DO : applique une opération sur les résultats obtenu après telechargement
	private void apply(Consumer<Tache> consumer) {

	}

}
