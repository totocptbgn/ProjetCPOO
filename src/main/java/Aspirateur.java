import java.util.Deque;
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

	// Limite de nombre de fichier
	private static long MAX = 100;
	
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
		base.setWhiteListed(whitelist);
	}

	/**
	 * 
	 * @param site - ajoute site à la whiteList
	 */
	public void addWhiteList(String site) {
		base.addSitetoWhiteList(site);
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
		a.commandes = Stream.generate(supply).takeWhile(e -> e!=null);
		if (a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
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
		a.commandes = Stream.generate(supply).takeWhile(e -> e!=null);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
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
				System.out.println(myQueue.size());
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.images());
				myQueue.addAll(current.css());
				myQueue.addAll(current.link());
				return current;
			}

		};
		a.commandes = Stream.generate(supply).takeWhile(e -> e!=null);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		return a;
	}

	/**
	 * active ou desactive (! dangereux) la limite 
	 * @param limit - true -> active la limite | false -> desactive la limite
	 */
	protected void setLimit(boolean limit) {
		this.limit = limit;
		if(limit) commandes.limit(MAX);
		else commandes.limit(Integer.MAX_VALUE);
	}

	/**
	 * permet de prendre les elements acceptant la condition
	 * @param p : La condition sur les URL
	 */

	private void addPredicate(Predicate<AspirateurURL> p) {
		commandes = commandes.filter(e -> p.test(e));
	}

	/**
	 * @param start - valeur de base de l'accumulateur
	 * @param faccu - fonction transformant l'accumulateur en fonction d'un élément AspirateurURL
	 * @param p - vérifie si la condition de l'accumulateur est encore vrai
	 *            permet de s'arreter quand la condition du predicat devient fausse
	 *            sur l'accumulateur
	 */

	private <V> void addPredicateWithAccumulator(V start, BiFunction<V, AspirateurURL, V> faccu, Predicate<V> p) {

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


	/**
	 * 
	 * @param limit - limite en nombre de fichier
	 */
	public void limit(long limit) {
		commandes = commandes.limit(limit);
	}

	/**
	 * @param size - limite de taille
	 */
	public void limitSize(long size) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getSize(), x -> x < size);
	}
	/**
	 * limite la taille de chaque fichier
	 * @param size - taille limite (inclus la taille limite comme acceptable)
	 */
	public void limitMax(long size) {
	
		this.addPredicate(p -> p.getSize()<=size);
	}

	/**
	 * @param profondeur - limite de profondeur (ne prend pas en compte image et css)
	 */
	 public void limitProfondeur(long profondeur) {
		 	double deb = 0;
	 		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getProfondeur(), (x) -> x < profondeur);
	 }

	 /**
	  * @return URLs récupérés par l'aspirateur
	  */
	 CompletableFuture<Set<String>> getContent() {
		 
		 return CompletableFuture.supplyAsync(() -> commandes.map(e->e.getURL()).collect(Collectors.toSet()));
	 }
	 
	 /*
	// TO DO : applique une opération sur les résultats obtenu après telechargement
	private void apply(Consumer<String> consumer) {
		commandes.peek(p -> consumer.accept(p.getURL()));
	}
	*/
}
