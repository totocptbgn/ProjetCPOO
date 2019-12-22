public interface Tache extends Runnable {
	/**
	 * @return taille du téléchargement
	 */
	public long getSize();
	/**
	 * @return URL de la page télécharger
	 */
	public String getURL();
	/**
	 * @return nom de la page téléchargée
	 */
	public String getPage();
}
