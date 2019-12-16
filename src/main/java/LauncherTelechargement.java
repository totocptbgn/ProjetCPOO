import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Gère un ensemble de téléchargement créé à partir d'un URL (URL et ses enfants)
 * Chaque Launcher sera représenté par un nom
 */

public final class LauncherTelechargement implements Launcher<Tache> {
	// Limite de nombre de fichier
	private static long MAX = 1;
	
	private Stream<Tache> commandes;
	private Set<Tache> elements;
	private List<ForkJoinTask<Tache>> inExecution = new ArrayList<ForkJoinTask<Tache>>();
	private boolean limit = true;
	private state etat = state.NEW;


	private ForkJoinPool es;

	// Permettra à l'utilisateur de choisir ce launcher
	private final String nom;

	// TO DO changer commandes
	public void setLimit(boolean limit) {
		this.limit = limit;
	}

	public String getNom() {
		return nom;
	}
	
	public synchronized state getEtat() {
		if(this.etat==Launcher.state.WORK) {
			//verifie si modification de l'info
			this.notify();
			try {
				this.wait();
			} catch (InterruptedException e) {
				//impossible
			}
		}
		return etat;
	}

	/*
	 * Créer le launcher
	 * @param String URL : URL de base
	 */
	
	public LauncherTelechargement(String URL) {
		// Donne les prochains éléments à traiter
		// Faire à l'exterieur de la classe
		Supplier<Tache> sup = new Supplier<Tache>() {
			Queue<Tache> file = new LinkedList<>(); // Non synchrone...

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

	/**
	 * Lance le téléchargement
	 * 
	 */
	@Override
	public synchronized CompletableFuture<Boolean> start() {
		return CompletableFuture.supplyAsync(this::run);
	}
	
	/*
	 *  lance l'ensemble du telechargement 
	 */
	public synchronized Boolean run() {
		//System.out.println(Thread.currentThread().getName());
		if(this.etat!=Launcher.state.NEW && this.etat!=Launcher.state.STOP) {
			return false;
		}
		
		this.etat = state.WORK;
		try {
			//creer la pool
			es = new ForkJoinPool();
			//elements a télécharger
			if(elements==null)
				elements = Collections.synchronizedSet(commandes.collect(Collectors.toSet()));			

			inExecution.clear();
			//lance les téléchargements
			for(Tache t:elements) {
				inExecution.add(es.submit(t, t));
			}
		
			//télécharge jusqu'a arret
			while(!es.isShutdown()) {
				
				//on laisse la moins aux autres actions
				//System.out.println("waitbegin");
				this.wait();
				//System.out.println("waitend");
				//futur tous fini et non arété de force -> fini normalement
				//System.out.println(inExecution.get(0).isCancelled());
				if (inExecution.stream().allMatch(f -> f.isDone() && !f.isCancelled())) {
					es.shutdown();
					this.etat= state.SUCCESS;
					
					return true;
				}
				//thread tous interrompu -> fini sur erreur
				if (inExecution.stream().allMatch(f -> f.isCancelled())) {
					throw new InterruptedException();
				}
				
				this.notify();
			}
			
			
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		finally {
			this.notify();
		}
		return false;
		
	}

	public synchronized void delete() {
		try {
			//si fini -> ne fait rien
			if(es.awaitTermination(1, TimeUnit.NANOSECONDS)) {
				return;
			}
		} catch (InterruptedException e) {
			//ne devrait jamais arriver
			e.printStackTrace();
		}

		//on n'utilise plus le gestionnaire de téléchargement
		es.shutdownNow();
		//on change l'état
		this.etat = Launcher.state.FAIL;
		this.notify();
		
	}
	
	// TO DO : met en pause le telechargement
	public synchronized void pause() {
		//System.out.println(Thread.currentThread().getName());
		try {
			//si fini -> ne fait rien
			if(es.awaitTermination(1, TimeUnit.NANOSECONDS)) {
				System.out.print("endedbefore\n");
				return;
			}
		} catch (InterruptedException e) {
			//ne devrait jamais arriver
			e.printStackTrace();
		}
		//interrons les taches
		for (ForkJoinTask<Tache> f:inExecution) {
			System.out.println(f.cancel(true));
		}
		
		//on n'utilise plus le gestionnaire de téléchargement pour l'instant
		es.shutdownNow();
		this.etat = Launcher.state.WAIT;
		this.notify();
	}
	
	public synchronized CompletableFuture<Boolean> restart() {

		for (Future<Tache> f:inExecution) {
			
			if(f.isDone() && !f.isCancelled()) {
				try {
					//on enlève les taches qui ont eu le temps de finir
					elements.remove(f.get());
				} catch (InterruptedException | ExecutionException e) {
					
				} 
			}
		}
		this.etat = Launcher.state.STOP;

		return CompletableFuture.supplyAsync(this::run);
	}

	/**
	 * @param Predicate<Tache> p : La condition sur les taches
	 * permet de prendre les elements acceptant la condition
	 */

	private void addPredicate(Predicate<Tache> p) {
		commandes = commandes.filter(p);
	}

	/**
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

	// Ajoute une limite au nombre de fichier
	public void limit(long limit) {
		commandes = commandes.limit(limit);
	}
	
	/* Limite la taille de téléchargement du site */
	public void limitSize(long size) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getSize(), x -> x < size);
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
