
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

public final class LauncherTelechargement implements Launcher {
	private static int id = 0;
	private int myid;
	private state etat = state.NEW;
	private Set<Tache> elements;
	private Set<Tache> elementsdone = new HashSet<Tache>();
	private List<ForkJoinTask<Tache>> inExecution = new ArrayList<ForkJoinTask<Tache>>();
	private Map<Path,String> files = Collections.synchronizedMap(new HashMap<>());
	private File repository;
	private ForkJoinPool es;

	// Permettra à l'utilisateur de choisir ce launcher
	private final String nom;

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
	
	LauncherTelechargement(String URL,Supplier<String> s) {
		id++;
		nom = id+"_"+URL.split("/")[2];
		this.myid=id;
		repository = new File("sites/"+nom);
		if(!repository.isDirectory())
			repository.mkdir();
		elements = Stream.generate(s).map(t -> {
			try {
				return new TacheTelechargement(t,repository);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}).collect(Collectors.toSet());
	}
	
	LauncherTelechargement(String URL) throws IOException {
		id++;
		nom = id+"_"+URL.split("/")[2];
		this.myid=id;
		repository = new File("sites/"+nom);
		if(!repository.isDirectory())
			repository.mkdir();
		elements = Stream.of(new TacheTelechargement(URL,repository)).collect(Collectors.toSet());
	}

	/**
	 * Lance le téléchargement
	 * 
	 */
	public synchronized CompletableFuture<Map<Path,String>> start() {
		return CompletableFuture.supplyAsync(this::run);
	}
	
	/*
	 *  lance l'ensemble du telechargement 
	 */
	private synchronized Map<Path,String> run() {
		//etat non prevu
		if(this.etat!=Launcher.state.NEW && this.etat!=Launcher.state.STOP) {
			return null;
		}
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
					
					for(ForkJoinTask<Tache> t:inExecution) {
						try {
							files.put(Paths.get(t.get().getPage()),t.get().getURL());
						} catch (ExecutionException e) {
							//should not happen
						}
						return files;
					}
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
		return null;
		
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
	
	public synchronized CompletableFuture<Map<Path,String>> restart() {

		for (Future<Tache> f:inExecution) {
			
			if(f.isDone() && !f.isCancelled()) {
				try {
					Tache t = f.get();
					//on enlève les taches qui ont eu le temps de finir
					elements.remove(t);
					//on garde les éléments dans une liste
					elementsdone.add(t);
					files.put(Paths.get(t.getPage()),t.getURL());
				} catch (InterruptedException | ExecutionException e) {
					//erreur ne devrait pas arrivé (et au pire on fait les autres taches
				} 
			}
		}
		this.etat = Launcher.state.STOP;

		return CompletableFuture.supplyAsync(this::run);
	}


	public synchronized long getTotalSize() {
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
	
	public Map<Path,String> getPages() {
		Map<Path,String> m = new HashMap<Path,String>();
		for(Tache element:elements) {
			m.put(Path.of(repository.getAbsolutePath()+"/"+element.getPage()), element.getURL());
		}	
		return m;
	}

	@Override
	public int getId() {
		return myid;
	}

}
