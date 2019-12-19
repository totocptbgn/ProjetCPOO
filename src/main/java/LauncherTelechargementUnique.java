import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

public final class LauncherTelechargementUnique implements Launcher {
	// Limite de nombre de fichier
	private static long MAX = 1;
	
	private boolean limit = true;
	private state etat = state.NEW;
	private Stream<Tache> commandes;
	private Set<Tache> elements;
	private Set<Tache> elementsdone = new HashSet<Tache>();
	private List<ForkJoinTask<Tache>> inExecution = new ArrayList<ForkJoinTask<Tache>>();

	private ForkJoinPool es;

	// Permettra à l'utilisateur de choisir ce launcher
	private final String nom;

	// TO DO changer commandes
	protected void setLimit(boolean limit) {
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
				//arret de l'application avant la fin -> rien à faire
			}
		}
		return etat;
	}

	/*
	 * Créer le launcher
	 * @param String URL : URL de base
	 */
	
	LauncherTelechargementUnique(String URL) throws IOException {
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
	public synchronized CompletableFuture<Boolean> start() {
		return CompletableFuture.supplyAsync(this::run);
	}
	
	/*
	 *  lance l'ensemble du telechargement 
	 */
	private synchronized Boolean run() {
		//etat non prevu
		if(this.etat!=Launcher.state.NEW && this.etat!=Launcher.state.STOP) {
			return false;
		}
		if(this.etat == Launcher.state.NEW)
			elements = Collections.synchronizedSet(commandes.collect(Collectors.toSet()));			
		this.etat = state.WORK;
		try {
			//creer la pool
			es = new ForkJoinPool();
			//elements a télécharger
			inExecution.clear(); //vide les taches en téléchargement
			//lance les téléchargements
			for(Tache t:elements) {
				inExecution.add(es.submit(t, t));
			}
		
			//télécharge jusqu'a arret
			while(!es.isShutdown()) {
				
				//on laisse la main aux autres actions
				this.wait();
				//futur tous fini et non arété de force -> fini normalement
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
			//notifie que la verification est terminé
			this.notify();
		}
		return false;
		
	}

	public synchronized boolean delete() {
		try {
			//si fini -> ne fait rien
			if(es.awaitTermination(1, TimeUnit.NANOSECONDS)) {
				return false;
			}
		} catch (InterruptedException e) {
			//fin non voulu de l'application
			return false;
		}

		//on n'utilise plus le gestionnaire de téléchargement
		es.shutdownNow();
		//on change l'état
		this.etat = Launcher.state.FAIL;
		this.notify();
		return true;
		
	}
	
	//met en pause le telechargement
	public synchronized boolean pause() {
	
		try {
			//si fini -> ne fait rien
			if(es.awaitTermination(1, TimeUnit.NANOSECONDS)) {
				return false;
			}
		} catch (InterruptedException e) {
			//arret inattendu
			return false;
		}
		//interrons les taches
		for (ForkJoinTask<Tache> f:inExecution) {
			f.cancel(true);
		}
		
		//on n'utilise plus le gestionnaire de téléchargement pour l'instant
		es.shutdownNow();
		this.etat = Launcher.state.WAIT;
		this.notify();
		return true;
	}
	
	public synchronized CompletableFuture<Boolean> restart() {

		for (Future<Tache> f:inExecution) {
			
			if(f.isDone() && !f.isCancelled()) {
				try {
					Tache t = f.get();
					//on enlève les taches qui ont eu le temps de finir
					elements.remove(t);
					//on garde les éléments dans une liste
					elementsdone.add(t);
				} catch (InterruptedException | ExecutionException e) {
					//erreur ne devrait pas arrivé (et au pire on fait les autres taches
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

	public synchronized long getTotalSize() {
		//ouille, limite les changements de taille /;
		if(this.etat == Launcher.state.NEW) {
			elements = Collections.synchronizedSet(commandes.collect(Collectors.toSet()));
			commandes = elements.stream();
		}
		long res = 0;
		for(Tache t:elements) {
			res+=t.getSize();
		}
		for(Tache t:elementsdone) {
			res+=t.getSize();
		}
		return res;
	}
	
	public synchronized long getSizeLeft() {
		if(this.etat == Launcher.state.FAIL || this.etat == Launcher.state.SUCCESS)
			return 0;
		if(this.etat == Launcher.state.NEW) {
			return this.getTotalSize();
		}
		long res = 0;
		Set<Tache> finished = inExecution.stream().filter((e) -> !e.isCancelled() && e.isDone()).map(e -> {
			try {
				return e.get();
			} catch (InterruptedException | ExecutionException e1) {
				//unexcepted error
				return null;
			}
			
		}).collect(Collectors.toSet());
		Set<Tache> notfinished = elements.stream().filter(e -> !finished.contains(e)).collect(Collectors.toSet());
		for(Tache t:notfinished) {
			res+=t.getSize();
		}
		for(Tache t:elementsdone) {
			res+=t.getSize();
		}
		return res;
		
	}

}
