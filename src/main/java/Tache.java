/**
 * Tache de téléchargement
 */
public interface Tache extends Runnable {
	/**
	 * @return taille de l'élément à télécharger
	 */
	public long getSize();
	/**
	 * @return URL de la page télécharger
	 */
	public String getURL();
	/**
	 * @return nom de la page télécharger
	 */
	public String getPage();
}
