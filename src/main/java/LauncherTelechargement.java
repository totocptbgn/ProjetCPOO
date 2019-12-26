import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gère un ensemble de téléchargement créé à partir d'une URL<br/>
 * Chaque Launcher sera représenté par un nom et un id
 */

public final class LauncherTelechargement implements LauncherIntern {
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
	 * Etat du launcher -> renvoie null si interrompu
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
	 * 
	 * @param URL - URL dont on veut le chemin
	 * @return URL sans le nom de la page
	 */
	private String chemin (String URL) {
		String[] split = URL.split("/");
		String res ="";
		for(int i = 0;i<split.length - 1;i++) {
			res = res + split[i] + "/";
		}
		return res;
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
	
	public synchronized CompletableFuture<Optional<Map<Path,String>>> startAt(int time) {
		this.etat = Launcher.state.WAIT;
		return CompletableFuture.supplyAsync(
				() -> {
					try {
						
						Thread.currentThread().sleep(time);
						if(this.etat == Launcher.state.WAIT) {
							return this.restart().join();
						}
					} catch (InterruptedException e) {
						
					} 
					return Optional.empty();
				});
	}

	public synchronized CompletableFuture<Optional<Map<Path,String>>> start() {
		return CompletableFuture.supplyAsync(this::run).thenApplyAsync(e ->
		 {
			 //System.out.println("ok");
			 if(e.isEmpty()) return e;
			 
			 Map<Path,String> map = e.get();
			 //System.err.println(e.size());
			 
			for(Path pere:map.keySet()) {
				
				if(!pere.toString().endsWith("pdf") && !pere.toString().endsWith("png") && !pere.toString().endsWith("jpg") && !pere.toString().endsWith("jpeg") && !pere.toString().endsWith("gif")) {	
					//System.out.println(pere.toString());

					File f = pere.toFile();
					File ftemp = null;
					try {
						ftemp = File.createTempFile(this.getNom(),null);
						FileOutputStream fileStream = new FileOutputStream(ftemp);
						OutputStreamWriter fw = new OutputStreamWriter(fileStream, "UTF-8");
						BufferedReader scan = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
				         String line;
				         while((line = scan.readLine()) != null) {
							
							//System.out.println("line : "+line);
							 for(Path p:map.keySet()) {
								 String link = map.get(p);
								 if(!link.isBlank())
									 line = line.replace(link,p.toString());
								 if(!p.equals(pere) && map.get(p).startsWith(chemin(map.get(pere)))) {
									String link2 = map.get(p).substring(chemin(map.get(pere)).length());
								 	//System.err.println(link2);
								 	if(!link2.isBlank())
								 		line = line.replace(link2,p.toString());
								 }
							 }
							 //System.out.println("become : "+line);
							 fw.write(line+"\n");
							
						}
						scan.close();
						fw.close();
						
						f.delete();
						f.createNewFile();
						fileStream = new FileOutputStream(f);
						fw = new OutputStreamWriter(fileStream, "UTF-8");
						scan = new BufferedReader(new InputStreamReader(new FileInputStream(ftemp), "UTF8"));
						 while((line = scan.readLine()) != null) {
							
							//System.out.println(line);
							fw.write(line+"\n");
						 }
						scan.close();
						fw.close();
						ftemp.delete();
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
	 * Lance le téléchargement
	 * @return - renvoie le contenu necessaire pour la fin du téléchargement
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
				notify();
				this.wait(1000);
				
				//futur tous fini et non arété de force -> fini normalement
				if (inExecution.stream().allMatch(f -> f.isDone() && !f.isCancelled() && f.isCompletedNormally())) {
					/*
					System.out.println("Total :"+inExecution.size());
					System.out.println("EndedNormally : "+inExecution.stream().filter(f -> f.isDone() && !f.isCancelled() && f.isCompletedNormally()).count());
					System.out.println("Cancelled : "+inExecution.stream().filter(f -> f.isDone() && f.isCancelled()).count());
					System.out.println("Done : "+inExecution.stream().filter(f -> f.isDone()).count());
					*/
					es.shutdown();
					this.etat= state.SUCCESS;
					Map<Path,String> finalfiles = this.files.get();
					for(ForkJoinTask<Tache> t:inExecution) {
						try {
							
							//System.out.println("page "+t.get().getPage()+" de "+t.get().getURL());
							finalfiles.put(Path.of(repository.getAbsolutePath()+"/"+t.get().getPage()),t.get().getURL());
						} catch (InterruptedException | ExecutionException e) {
							//le fichier n'a pas pu être ajouté au résultat final
							//isCompletedNormally -> n'arrive pas
							throw new IllegalStateException();
						}
					}
					
					return files;
				}
				//thread interrompu -> interrompu
				if (inExecution.stream().anyMatch(f -> f.isCancelled())) {
					/*
					System.out.println("Total :"+inExecution.size());
					System.out.println("EndedNormally : "+inExecution.stream().filter(f -> f.isDone() && !f.isCancelled() && f.isCompletedNormally()).count());
					System.out.println("Cancelled : "+inExecution.stream().filter(f -> f.isDone() && f.isCancelled()).count());
					System.out.println("Done : "+inExecution.stream().filter(f -> f.isDone()).count());
					*/
					throw new InterruptedException();
				}
				
				//tous fini mais pas dans les 2 cas précédents -> bug
				if (inExecution.stream().allMatch(f -> f.isDone())) {
					//System.out.println("bad one");
					this.etat = Launcher.state.FAIL;
					throw new UnsupportedOperationException();
				}
			
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
		if(this.etat == Launcher.state.WORK) {
			try {
				this.notify();
				this.wait();
				if(this.etat==Launcher.state.WORK) {
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
		
		int count = 0;
		//supprime les taches finies
		for(ForkJoinTask<Tache> fjt:inExecution) {
			if(fjt.isDone() && !fjt.isCancelled() &&fjt.isCompletedNormally()) {
				try {
					File f = new File(repository.getAbsolutePath()+"/"+fjt.get().getPage());
					f.delete();
					count++;
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
		return count != 0;
		
	}
	
	
	public synchronized CompletableFuture<Boolean> deleteAt(int time) {
		
		CompletableFuture<Boolean> cf = CompletableFuture.supplyAsync(() -> { 
			try {
				Thread.sleep(time);
				//System.out.println("test");
				if(this.getEtat()!=Launcher.state.SUCCESS && this.getEtat()!= Launcher.state.FAIL)
					return this.delete();
				else return false;
			} catch (InterruptedException e1) {
				return false;
			} 
			});
		return cf;
	}
	
	//met en pause le telechargement
	public synchronized boolean pause() {
		System.out.println("pause");
		try {
			this.notify();
			this.wait();
			if(this.etat==Launcher.state.WORK) {
				//interrons les taches
				for (ForkJoinTask<Tache> f:inExecution) {
					f.cancel(true);
				}
				//on n'utilise plus le gestionnaire de téléchargement pour l'instant
				es.shutdownNow();
				System.out.println("ok");
				this.etat = Launcher.state.WAIT;
				this.notify();
				return true;
			}
		} catch (InterruptedException e) {
			//tache interrompu
			
		}
		return false;
		
		
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

	public int getId() {
		return myid;
	}

}
