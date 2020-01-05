package aspirateur;

import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aspirateur de pages internets
 */
public class Aspirateur {

	/**
	 * WAIT -> en attente de transformation en launcher <br/>
	 * TAKE -> en train de se transformer
	 * DIE -> l'aspiration est fini
	 */
	 
	public enum state {
		WAIT,TAKE,DIE;
	}
	
	//le future permettant de récupéré la tache
	private CompletableFuture<Optional<Set<String>>> future;
	
	//etat du thread
	private state etat = state.WAIT;

	// Limite de nombre de fichier
	private final static long MAX = 100;
	
	//id de l'aspirateur
	private static int id = 0;
	private final int myId;

	// activation de la limite
	private boolean limit = true;

	// ce que l'on ajoute à la page de base
	private Stream<AspirateurURL> commandes;

	// base du téléchargement
	private AspirateurURL base;

	/**
	 * Donne l'état de l'aspirateur (WAIT -> en attente de transformation, TAKE -> En attente de transformation
	 */
	public synchronized state getState() {
		return this.etat;
	}
	
	
	/**
	 * @return URL de base du téléchargement
	 */
	public String getBaseURL() {
		return base.getURL();
	}

	/**
	 * 
	 * @param whitelist - active ou désactive la whiteList
	 */
	public void whiteList(boolean whitelist) {
		if(this.etat == state.WAIT)
			base.setWhiteListed(whitelist);
		else 
			throw new IllegalStateException();
	}

	/**
	 * 
	 * @param site - ajoute site à la whiteList
	 */
	public void addWhiteList(String site) {
		if(this.etat == state.WAIT)
			base.addSitetoWhiteList(site);
		else 
			throw new IllegalStateException();
	}
	
	/**
	 * 
	 * @param site - ajoute site à la whiteList
	 */
	public void removeWhiteList(String site) {
		if(this.etat == state.WAIT)
			base.removeSitetoWhiteList(site);
		else 
			throw new IllegalStateException();
	}

	/**
	 * 
	 * @return id de l'aspirateur
	 */
	public int getId() {
		return myId;
	}
	
	/**
	 * 
	 * @param URL - URL de base de l'aspirateur
	 */
	private Aspirateur(String URL) {
		id++;
		this.myId=id;
		base = new AspirateurURL(URL);
	}

	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui n'aspire que la page donné
	 */
	static Aspirateur aspirateurNormal(String URL) {
		
		Aspirateur a = new Aspirateur(URL);
		a.commandes = Stream.of(a.base);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		return a;
	}


	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui aspire les pages accessibles
	 */
	static Aspirateur aspirateurPages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {

			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if (myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				if (myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.link());
				myQueue.addAll(current.css());
				return current;
			}

		};
		a.commandes = Stream.generate(supply);
		if (a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		a.commandes = a.commandes.takeWhile(e -> e!=null);
		return a;
	}

	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui aspire les images accessibles
	 */
	static Aspirateur aspirateurImages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {

			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if (myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.images());
				myQueue.addAll(current.css());
				return current;
			}

		};
		a.commandes = Stream.generate(supply);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		a.commandes = a.commandes.takeWhile(e -> e!=null);
		return a;
	}

	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui aspire les pages et images accessibles
	 */
	static Aspirateur aspirateurImagesPages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {

			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if(myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				//System.out.println("size :"+myQueue.size());
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.images());
				myQueue.addAll(current.css());
				myQueue.addAll(current.link());
				return current;
			}

		};
		a.commandes = Stream.generate(supply);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		a.commandes = a.commandes.takeWhile(e -> e!=null);
		return a;
	}

	/**
	 * active ou desactive (! dangereux) la limite 
	 * @param limit - true -> active la limite | false -> desactive la limite
	 */
	public synchronized void setLimit(boolean limit) {
		if(this.etat == state.WAIT) {
			this.limit = limit;
			if(limit) commandes.limit(MAX);
			else commandes.limit(Integer.MAX_VALUE);
		}
		else 
			throw new IllegalStateException();
	}

	/**
	 * permet de prendre les elements acceptant la condition
	 * @param p : La condition sur les URL
	 */

	private synchronized void addPredicate(Predicate<AspirateurURL> p) {
		if(this.etat == state.WAIT)
			commandes = commandes.filter(e -> p.test(e));
		else 
			throw new IllegalStateException();
	}

	/**
	 * @param start - valeur de base de l'accumulateur
	 * @param faccu - fonction transformant l'accumulateur en fonction d'un élément AspirateurURL
	 * @param p - vérifie si la condition de l'accumulateur est encore vrai
	 *            permet de s'arreter quand la condition du predicat devient fausse
	 *            sur l'accumulateur
	 */

	private <V> void addPredicateWithAccumulator(V start, BiFunction<V, AspirateurURL, V> faccu, Predicate<V> p) {
		if(this.etat == state.WAIT) {
			Predicate<AspirateurURL> pwithaccu = new Predicate<AspirateurURL>() {
				V accumulator = start;

				@Override
				public boolean test(AspirateurURL t) {
				accumulator = faccu.apply(accumulator, t);
				return p.test(accumulator);
				}

			};

			commandes = commandes.takeWhile(pwithaccu);
		}
		else throw new IllegalStateException();
	}


	/**
	 * 
	 * @param limit - limite en nombre de fichier
	 */
	public synchronized void limit(long limit) {
		if(this.etat == state.WAIT)
			commandes = commandes.limit(limit);
		else 
			throw new IllegalStateException();
	}

	/**
	 * @param size - limite de taille
	 */
	public synchronized void limitSize(long size) {
		
		double deb = 0;

		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getSize(), x -> x < size);
	}
	/**
	 * limite la taille de chaque fichier
	 * @param size - taille limite (inclus la taille limite comme acceptable)
	 */
	public synchronized void limitMax(long size) {
		this.addPredicate(p -> p.getSize()<=size);

	}

	/**
	 * @param profondeur - limite de profondeur (ne prend pas en compte image et css)
	 */
	 public synchronized void limitProfondeur(long profondeur) {
	
		this.addPredicate(p -> p.getProfondeur() < profondeur);

	 }

	 /**
	  * @return URLs récupérés par l'aspirateur
	  */
	 synchronized CompletableFuture<Optional<Set<String>>> getContent() {
		 if(this.etat == state.WAIT) {
			this.etat = state.TAKE;
		 	future = CompletableFuture.supplyAsync(() -> commandes.map(e->e.getURL()).collect(Collectors.toSet())).thenApplyAsync(this::ending);
		 	return future;
		 }
		 else 
			throw new IllegalStateException();
	 }
	 
	 /**
	  * dernière étape avant le renvoie du résultat de l'aspirateur
	  * @param e - resultat de l'aspiration
	  * @return renvoie le résultat de l'aspiration si celui ci n'a pas été annulé
	  */
	 private synchronized Optional<Set<String>> ending(Set<String> e) {
		 if(this.getState() == state.DIE) {
			 return Optional.empty();
				
		 }
		 else {
			 this.etat = state.DIE;
			 return Optional.of(e);
			 
		 }
	 }
	 
	 /**
	  * Supprime la tache (meme si celle-ci est TAKE)
	  */
	 synchronized void cancel() {
		 if(this.etat == state.TAKE) {
			 future.cancel(true);
		 }
		 this.etat = state.DIE;
	 }
	 
	 /**
	  * 
	  * @return la whiteliste de l'aspirateur
	  */
	 public Set<String> whiteList() {
		 return base.whiteList();
	 }
	 /*
	// TO DO : applique une opération sur les résultats obtenu après telechargement
	private void apply(Consumer<String> consumer) {
		commandes.peek(p -> consumer.accept(p.getURL()));
	}
	*/
	 
	 
}
