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
	private static long MAX = 1;

	private Stream<Tache> commandes;

	private boolean limit = true;

	// TO DO trouver un nom et l'initialiser
	//permettra à l'utilisateur de choisir ce launcher
	private /* final */ String nom;

	// TO DO message de prévention si enleve limit
	public void setLimit(boolean limit) {
		this.limit = limit;
	}

	public String getNom() {
		// copie?
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
				if (file.isEmpty())
					return null;
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
		commandes.forEach(x -> {
			if (x != null)
				x.start();
		});
	}

	// TO DO : arrete le telechargement
	public void stop() {

	}
	
	// TO DO : met en pause le telechargement
	public void pause() {

	}

	/*
	 * permet de prendre les elements acceptant la condition
	 */
	public void addPredicate(Predicate<Tache> p) {
		commandes = commandes.filter(p);
	}

	/*
	 * permet de s'arreter quand la condition du predicat devient fausse sur
	 * l'accumulateur -> necessite l'ordre /:
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

	public void limitSize(long size) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getSize(), (x) -> x < size);
	}

	public void limitProfondeur(long profondeur) {
		double deb = 0;
		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getProfondeur(), (x) -> x < profondeur);
	}

	// TO DO : applique une opération sur les résultats obtenu après telechargement
	public void apply(Consumer<Tache> consumer) {

	}

}
