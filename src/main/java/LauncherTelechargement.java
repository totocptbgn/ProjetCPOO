import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gère un ensemble de téléchargement créé à partir d'une URL<br/>
 * Chaque Launcher sera représenté par un nom et un id
 */

public final class LauncherTelechargement implements Launcher {
	private static int id = 0;
	//id du launcher
	private int myid;
	//état du téléchargement
	private state etat = state.NEW;
	//taches à faire
	private Set<Tache> elements;
	//taches finies
	private Set<Tache> elementsdone = new HashSet<Tache>();
	//future des taches en cours d'execution
	private List<ForkJoinTask<Tache>> inExecution = new ArrayList<ForkJoinTask<Tache>>();
	//resultat optenu : Path -> chemin vers le fichier, String -> lien de la page
	private Optional<Map<Path,String>> files = Optional.of(Collections.synchronizedMap(new HashMap<>()));
	//repertoire de téléchargement du launcher
	private File repository;
	//pool ou sont executés toutes les taches
	private ForkJoinPool es;

	// nom sous la forme id_nomdelapage
	private final String nom;

	public String getNom() {
		return nom;
	}
	/**
	 * Etat du téléchargement -> renvoie null si interrompu
	 */
	public synchronized state getEtat () {
		
		if(this.etat==Launcher.state.WORK) {
			//verifie si modification de l'info
			this.notify();
			try {
				this.wait();
			}
			catch(InterruptedException e) {
				return null;
			}
		}
		return etat;
	}
	
	/**
	 * @param URL : URL de base (pour avoir un nom)
	 * @param s : Ensemble des taches
	 * Realise un launcher pour un ensemble de taches
	 */
	LauncherTelechargement(String URL,Set<String> s) {
		id++;
		nom = id+"_"+URL.split("/")[2];
		this.myid=id;
		repository = new File("download/"+nom);
		if(!repository.isDirectory())
			repository.mkdir();
		elements = s.stream().map(e -> {
			try {
				return new TacheTelechargement(e,repository);
			} catch (IOException e1) {
				//unexcepted link -> forget it
				return null;
				
			}
		}).collect(Collectors.toSet());
	}
	/**
	 * @param URL : URL de base
	 * Realise un launcher pour une tache
	 */
	LauncherTelechargement(String URL) throws IOException {
		id++;
		nom = id+"_"+URL.split("/")[2];
		this.myid=id;
		repository = new File("download/"+nom);
		if(!repository.isDirectory())
			repository.mkdir();
		elements = Stream.of(new TacheTelechargement(URL,repository)).collect(Collectors.toSet());
	}

	/**
	 * Lance le téléchargement
	 * 
	 */
	public synchronized CompletableFuture<Optional<Map<Path,String>>> start() {
		return CompletableFuture.supplyAsync(this::run).thenApplyAsync(e ->
		 {
			 if(e.isEmpty()) return e;
			 Map<Path,String> map = e.get();
			 //System.err.println(e.size());
			 for(Path p:map.keySet()) {
				 String link = map.get(p);

				 for(Path pere:map.keySet()) {
					File f = pere.toFile();
					//System.out.println(f.getAbsolutePath());

					File ftemp = null;
					try {
						ftemp = File.createTempFile(map.get(pere),"");
						FileWriter fw = new FileWriter(ftemp);

						Scanner scan=new Scanner(f);
						while(scan.hasNext()) {
							String mot = scan.next().replace(link,p.toString());
							fw.write(mot+" ");
						}
						scan.close();
						fw.close();

						f.delete();

						f.createNewFile();
						fw = new FileWriter(f);
						scan=new Scanner(ftemp);
						while(scan.hasNext()) {
							String mot = scan.next();
							//System.out.println(line);
							fw.write(mot+" ");
						 }
						scan.close();
						fw.close();
					} catch (IOException e1) {
						throw new RuntimeException(f.getName()+" has failed");
					}

				 }

			 }

			 return e;
		 }

		);
	}
	
	/**
	 *  lance l'ensemble du telechargement -> observe annulation avec la fonction cancel des futures
	 */
	private synchronized Optional<Map<Path,String>> run() {
		//etat non prevu
		if(this.etat!=Launcher.state.NEW && this.etat!=Launcher.state.STOP) {
			return Optional.empty();
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
				
				//on laisse la main aux autres actions (pour 1000 secondes pour savoir si fini)
				this.wait(1000);
				//futur tous fini et non arété de force -> fini normalement
				if (inExecution.stream().allMatch(f -> f.isDone() && !f.isCancelled() && f.isCompletedNormally())) {
					es.shutdown();
					this.etat= state.SUCCESS;
					Map<Path,String> finalfiles = this.files.get();
					for(ForkJoinTask<Tache> t:inExecution) {
						try {
							finalfiles.put(Path.of(repository.getAbsolutePath()+"/"+t.get().getPage()),t.get().getURL());
						} catch (InterruptedException | ExecutionException e) {
							//le fichier n'a pas pu être ajouté au résultat final
							//isCompletedNormally -> n'arrive pas
							throw new IllegalStateException();
						}
					}
					
					return files;
				}
				//thread tous interrompus -> interrompu
				if (inExecution.stream().allMatch(f -> f.isCancelled())) {
					throw new InterruptedException();
				}
				
				this.notify();
			}
			
			
		} catch (InterruptedException e) {
			//interruption
		}
		finally {
			//notifie que la verification est terminé
			this.notify();
		}
		return Optional.empty(); //arrive quand la tache echoue
		
	}

	public synchronized boolean delete() {
		if(this.etat == Launcher.state.FAIL) {
			return false;
		}
		if(this.etat == Launcher.state.WORK) {
			try {
				if(!es.awaitTermination(1, TimeUnit.NANOSECONDS)) {
					//interrons les taches
					for (ForkJoinTask<Tache> f:inExecution) {
						f.cancel(true);
					}
					//on n'utilise plus le gestionnaire de téléchargement
					es.shutdownNow();
				
					
					this.notify();
				}
			} catch (InterruptedException e) {
				//tache interrompu
				return false;
			}
			
			
		}
		//on change l'état
		this.etat = Launcher.state.FAIL;
		
		
		//supprime les taches finies
		for(ForkJoinTask<Tache> fjt:inExecution) {
			if(fjt.isDone() && !fjt.isCancelled() &&fjt.isCompletedNormally()) {
				try {
					File f = new File(repository.getAbsolutePath()+"/"+fjt.get().getPage());
					f.delete();
				} catch (InterruptedException | ExecutionException e) {
					//le fichier n'a pas pu être récupéré
					//isCompletedNormally -> n'arrive pas
					throw new IllegalStateException();
				}
			}
		}
		for(Tache t:elementsdone) {
			File f = new File(repository.getAbsolutePath()+"/"+t.getPage());
			f.delete();
		}
		repository.delete();
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
	
	public synchronized CompletableFuture<Optional<Map<Path,String>>> restart() {
	
		for (ForkJoinTask<Tache> f:inExecution) {
			
			if(f.isDone() && !f.isCancelled() && f.isCompletedNormally()) {
				try {
					Tache t = f.get();
					//on enlève les taches qui ont eu le temps de finir
					elements.remove(t);
					//on garde les éléments dans une liste
					elementsdone.add(t);
					files.get().put(Path.of(repository.getAbsolutePath()+"/"+t.getPage()),t.getURL());
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException();
					//ne devrait pas arrivé
					
				} 
			}
		}
		this.etat = Launcher.state.STOP;

		return this.start();
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
		if (this.etat == state.SUCCESS) return 0;
		if (this.etat == state.NEW || this.etat == state.FAIL) return getTotalSize();
		long res = 0;
		Set<Tache> finished = inExecution.stream().filter((e) -> e.isCompletedAbnormally() && !e.isCancelled() && e.isDone()).map(e -> {
			try {
				return e.get();
			} catch (InterruptedException | ExecutionException e1) {
				throw new IllegalStateException();
			}
			
		}).collect(Collectors.toSet());
		Set<Tache> notfinished = elements.stream().filter(e -> !finished.contains(e)).collect(Collectors.toSet());
		for(Tache t:notfinished) {
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
