import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Aspirateur {
	
	//gestionnaire de telechargement
	private static Gestionnaire g = new Gestionnaire();
	
	// Limite de nombre de fichier
	private static long MAX = 3;
	
	// activation de la limite
	private boolean limit = true;
	
	// ce que l'on ajoute à la page de base
	private Stream<AspirateurURL> commandes;
	
	//base du téléchargement
	private AspirateurURL base;
	
	public String getBaseURL() {
		return base.getURL();
	}
	
	public void whiteList(boolean whitelist) {
		base.setWhiteListed(whitelist);
	}
	
	public void addWhiteList(String site) {
		base.addSitetoWhiteList(site);
	}
	
	private Aspirateur(String URL) {
		base = new AspirateurURL(URL);
	}
	
	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui n'aspire que la page donné
	 */
	public static Aspirateur aspirateurNormal(String URL) {
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
	public static Aspirateur aspirateurPages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {
			
			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if(myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.link());
				return current;
			}
			
		};
		a.commandes = Stream.generate(supply);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		return a;
	}
	
	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui aspire les images accessibles
	 */
	public static Aspirateur aspirateurImages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {
			
			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if(myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.images());
				return current;
			}
			
		};
		a.commandes = Stream.generate(supply);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		return a;
	}
	
	/**
	 * @param URL de base
	 * @return fabrique static pour un aspirateur qui aspire les pages et images accessibles
	 */
	public static Aspirateur aspirateurImagesPages(String URL) {
		Aspirateur a = new Aspirateur(URL);
		Supplier<AspirateurURL> supply = new Supplier<AspirateurURL> () {
			
			private Deque<AspirateurURL> myQueue;
			@Override
			public AspirateurURL get() {
				if(myQueue==null) {
					myQueue = new ConcurrentLinkedDeque<AspirateurURL>();
					myQueue.add(a.base);
				}
				if(myQueue.isEmpty()) return null;
				AspirateurURL current = myQueue.poll();
				myQueue.addAll(current.images());
				myQueue.addAll(current.link());
				return current;
			}
			
		};
		a.commandes = Stream.generate(supply);
		if(a.limit) {
			a.commandes = a.commandes.limit(MAX);
		}
		return a;
	}

	protected void setLimit(boolean limit) {
		this.limit = limit;
		commandes.limit(MAX);
	}

	/*
	 * @param Predicate<Tache>
	 *            p : La condition sur les URL 
	 *            permet de prendre les elements acceptant la condition
	 */

	private void addPredicate(Predicate<String> p) {
		commandes = commandes.filter(e -> p.test(e.getURL()));
	}

	/*
	 * @param V
	 *            start : valeur de base de l'accumulateur
	 * @param BiFunction<V,
	 *            String, V> faccu : fonction transformant l'accumulateur en fonction
	 *            d'un élément String
	 * @param Predicate<V>
	 *            p : vérifie si la condition de l'accumulateur est encore vrai
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
	 public void limitProfondeur(long profondeur) { 
		 	double deb = 0;
	 		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getProfondeur(), (x) -> x < profondeur); 
	 }
	 
	 public void downloadAll() {
	 	/*
		 List<CompletableFuture<Set<Path>>> cfs= commandes.takeWhile(e -> e!=null).map(e -> { try {
			g.addLauncher(e.getURL());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} return g.launch(); }).collect(Collectors.toList());

		 for(CompletableFuture<Set<Path>> cf : cfs) {
			 cf.thenApply(
					 set ->
					 	for(Path p:set) {
					 	}
			);
		 }
		 */
	 }
	 /*
	// TO DO : applique une opération sur les résultats obtenu après telechargement
	private void apply(Consumer<String> consumer) {
		commandes.peek(p -> consumer.accept(p.getURL()));
	}
	*/
}
