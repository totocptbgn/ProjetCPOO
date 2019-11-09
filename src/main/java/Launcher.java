import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

/*
 * Gere un ensemble de téléchargement créé à partir d'un URL (URL et ses enfants)
 * Chaque Launcher sera représenté par un nom
 */
public final class Launcher {
	// limite de nombre/taille de fichier (peut etre changeable)
	private static long MAX = 100;
	
	private Stream<Tache> commandes;

	private boolean limit = true;

	//TO DO trouver un nom et l'initialiser
	private /*final*/ String nom;
		
	// TO DO message de prévention si enleve limit
	public void setLimit(boolean limit) {
		this.limit = limit;
	}
	
	public String getNom() {
		//copie?
		return nom;
	}

	public Launcher(String URL) {
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
				Tache t = file.poll();
				file.addAll(t.NextProfondeur());
				return t;
			}

		};

		commandes = Stream.generate(sup);

		if (limit) {
			commandes = commandes.limit(MAX);
		}
	}
	
	// lance l'ensemble du telechargement (et l'ordre?)
	public void launch() {
		commandes.forEach(x -> x.run());
	}

	// TO DO : arrete le telechargement
	public void stop() {
		
	}

	/*
	 * permet de prendre les elements acceptant la condition
	 */
	public void addPredicate(Predicate<Tache> p) {
		commandes = commandes.filter(p);
	}

	/*
	 * permet de s'arreter quand la condition du predicat devient fausse sur
	 * l'accumulateur
	 */
	public <T> void addPredicateWithAccumulator(T start, BiFunction<T, Tache, T> faccu, Predicate<T> p) {

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

	// TO DO : ajoute une limite de taille pour l'ensemble du telechargement
	//(avec addPredicateWithAccumulator)
	public void limitSize(long size) {

	}

	// TO DO : ajoute une limite de profondeur pour l'ensemble du telechargement
	// (avec addPredicateWithAccumulator)
	public void limitProfondeur(long profondeur) {

	}

	// TO DO : applique une opération sur les Taches au moment du téléchargement
	public void apply(Consumer<Tache> consumer) {

	}

}
