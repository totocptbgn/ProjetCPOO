package aspirateur;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import downloadmanager.*;

public class Aspirateur {
	// Limite de nombre de fichier
	private static long MAX = 1000;
	
	// activation de la limite
	private boolean limit = true;
	
	// ce que l'on ajoute
	private Stream<String> commandes;
	
	//base du téléchargement
	private AspirateurURL base;
	
	public String getBaseURL() {
		return base.getURL();
	}
	
	public Aspirateur(String URL) {
		
	}

	protected void setLimit(boolean limit) {
		this.limit = limit;
		commandes.limit(MAX);
	}

	/**
	 * @param Predicate<Tache>
	 *            p : La condition sur les taches permet de prendre les elements
	 *            acceptant la condition
	 */

	private void addPredicate(Predicate<String> p) {
		commandes = commandes.filter(p);
	}

	/**
	 * @param T
	 *            start : valeur de base de l'accumulateur
	 * @param BiFunction<T,
	 *            Tache, T> faccu : fonction transformant l'accumulateur en fonction
	 *            d'un élément tache
	 * @param Predicate<T>
	 *            p : vérifie si la condition de l'accumulateur est encore vrai
	 *            permet de s'arreter quand la condition du predicat devient fausse
	 *            sur l'accumulateur -> necessite l'ordre /:
	 */

	private <V> void addPredicateWithAccumulator(V start, BiFunction<V, String, V> faccu, Predicate<V> p) {

		Predicate<String> pwithaccu = new Predicate<String>() {
			V accumulator = start;

			@Override
			public boolean test(String t) {
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
		this.addPredicateWithAccumulator(deb, (x, y) -> {AspirateurURL au = new AspirateurURL(y); return x + au.getSize(); }, x -> x < size);
	}

	/* Limite la profondeur des pages du téléchargement du site */
	
	 public void limitProfondeur(long profondeur) { 
		 	double deb = 0;
	 		this.addPredicateWithAccumulator(deb, (x, y) -> x + y.getProfondeur(), (x) -> x < profondeur); 
	 }
	 

	// TO DO : applique une opération sur les résultats obtenu après telechargement
	private void apply(Consumer<T> consumer) {
		
	}
}
