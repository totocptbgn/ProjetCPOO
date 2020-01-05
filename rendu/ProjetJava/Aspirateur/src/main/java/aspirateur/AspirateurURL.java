package aspirateur;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
/**
 * Aspirateur sur une seule page
 */
final class AspirateurURL {
	// URL de la page
	private final String URL;
	// le parent de l'URL si elle existe
	private AspirateurURL parent = null;
	
	// les pages déjà téléchargées (évite les boucles)
	private Set<String> inside;
	
	// activation de la whiteList
	private boolean whiteListed = false;
	// si le lien est une image
	private boolean notPage;
	
	// whiteList des sites
	private Set<String> whiteList = new HashSet<>();
	
	//taille de la page
	private long size;
	
	/**
	 * 
	 * @return la whiteliste de l'aspirateur d'URL
	 */
	public Set<String> whiteList() {
		return new HashSet<String>(whiteList);
	}
	
	/**
	 * @return taille de la page ( -1 si non défini )
	 */
	public long getSize() {
		return this.size;
	}
	
	/**
	 * Active la whiteList : permet de limiter les sites accessibles
	 * @param b - true -> active la whiteList | false -> désactive la whiteList
	 */
	public void setWhiteListed(boolean b) {
		whiteListed = b;
	}
	
	/**
	 * @return URL de l'aspirateurURL
	 */
	public String getURL() {
		return URL;
	}
	
	/**
	 * @return permet de savoir si whiteListed
	 */
	public boolean getWhiteListed() {
		if(parent != null) 
			return parent.getWhiteListed();
		return whiteListed;
	}
	
	/**
	 * ajoute le site s à la whiteList
	 * @param s - s doit être sous la forme monsite ou https://...monsite
	 * Remarque : marche aussi pour les pages
	 */
	void addSitetoWhiteList(String s) {
		whiteListed = true;
		whiteList.add(s);
	}
	
	/**
	 * enlève le site s à la whiteList
	 * @param s - s doit être sous la forme monsite ou https://...monsite
	 * Remarque : marche aussi pour les pages
	 */
	void removeSitetoWhiteList(String s) {
		whiteListed = true;
		whiteList.remove(s);
	}
	
	/**
	 * Permet de savoir si une page est accessible (si accepte la whiteList et lien correct)
	 * @param URL - URL
	 * @return si la page est accepter
	 */
	private boolean isWell(String URL) {
		//commence par #
		if( URL.matches("^#")) return false;
		//vide
		if(URL.isBlank()) return false;
		if(!this.getWhiteListed()) return true;
		String little;
		String[] tab= URL.split("/");
		if(tab.length < 2) {
			little = tab[0];
		}
		else {
	
			little = tab[2] ;
		}
		for(String site:whiteList) {
			
			if (URL.startsWith(site) || little.startsWith(site)) 
				return true;
		}
		return false;
	}
	
	/**
	 * @return parent de la page
	 */
	public AspirateurURL getParent() {
		return parent;
	}
	
	/**
	 * @return renvoie la profondeur d'une page (s'il s'agit d'une image ou d'une page css alors 0)
	 */
	public long getProfondeur() {
		if(notPage) return 0;
		if(this.getParent() == null) return 0;
		return 1 + this.getParent().getProfondeur();
	}
	
	/**
	 * @param URL - URL de la page à aspirer
	 */
	public AspirateurURL(String URL) {
		inside=new HashSet<String>();
		inside.add(URL);
		notPage = false;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		} catch (IOException e) {
			this.size = -1;
		}
	
		this.URL = URL;
		String[] tab= URL.split("/");
		whiteList.add(tab[2]);
		
	}
	
	/**
	 * 
	 * @param URL - URL pour la page
	 * @param parent - parent de la page
	 * @param notPage - s'il ne s'agit pas d'une page
	 * @throws IOException - si une I/O exception se produit
	 */
	private AspirateurURL(String URL,AspirateurURL parent,boolean notPage) throws IOException {
		this.inside = parent.inside;
		this.inside.add(URL);
		this.notPage = notPage;	
		HttpURLConnection conn = null;
		try {
			//System.out.println(URL);
			conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		}
		catch (MalformedURLException e) {
			this.size = -1;
		}
		this.URL = URL;
		this.parent = parent;
		this.whiteList = parent.whiteList;
	}
	
	/**
	 * recupère les liens vers les autres pages
	 * @return la liste d'AspirateurURL de ces liens
	 */
	public Set<AspirateurURL> link() { 
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com")              
		               .timeout(1000*5)
		               .get();
		} catch (Exception e) {
			//le site n'autorise pas la connection -> vide
			return Set.of();
		}  
		Elements links = doc.select("a[href]");  
		
		Set<AspirateurURL> liste = new HashSet<>();
		for (Element link : links) {  
			String l = transform(link.attr("href"));
			if(this.isWell(l))
					
				if(!inside.contains(l)) {
					try {
						AspirateurURL aURL = new AspirateurURL(l,this,false);
						liste.add(aURL);
					}
					catch (java.net.UnknownHostException e) {
						throw new LinkageError();
					}
					catch(Exception e) {
							//pas ajouté
					}
				}
				
		
		} 
		return liste;
	}
	
	/**
	 * recupère les liens vers les pages css
	 * @return la liste d'AspirateurURL de ces liens
	 */
	public Set<AspirateurURL> css() { 
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com")              
		               .timeout(1000*5)
		               .get();
		} catch (Exception e) {
			//le site n'autorise pas la connection -> vide
			return Set.of();
		}  
		Set<AspirateurURL> liste = new HashSet<>();
		Elements links = doc.select("link[href]");
		for (Element link : links) {  
		String l = transform(link.attr("href"));
		if (this.isWell(l))
			if (!inside.contains(l)) {
				try {
					AspirateurURL aURL = new AspirateurURL(l,this,true);
					liste.add(aURL);
				}
				catch (java.net.UnknownHostException e) {
					throw new LinkageError();
				}
				catch(Exception e) {
					//pas ajouté
				}
			}
		} 
		return liste;
	}
	
	/**
	 * Transforme les URLs
	 * @param s - URL à transformer
	 * @return l'URL transformé
	 */
	private String transform(String s) {
		if (s.isBlank()) return s;
		String[] tab =s.split("#");
		if (tab.length==0) return s;
		s = tab[0];
		tab =s.split("\\?");
		if(tab.length==0) return s;
		s = tab[0];
		if(!s.contains("http")) {
			String[] ens = this.getURL().split("/");
			
			for(int i=1;i<ens.length - 1;i++)
				ens[0] = ens[0] + "/" + ens[i]; 
			s = ens[0] + "/" + s;
		}
		return s;
	}
	/**
	 * 
	 * @return renvoie les liens vers les images
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public Set<AspirateurURL> images() {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com")              
		               .timeout(1000*5)
		               .get();
		} catch (java.net.UnknownHostException e) {
			throw new LinkageError();
		}
		catch (Exception e) {
			//e.printStackTrace();
			return Set.of();
		}
		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
		images.addAll(doc.select("link[href]"));
		Set<AspirateurURL> liste = new HashSet<>();
		for (Element image : images) {
			String l = transform(image.attr("src"));
			if(this.isWell(l))
				try {
					if(!inside.contains(l)) {
						liste.add(new AspirateurURL(l,this,true));
					}
				} 
				catch (java.net.UnknownHostException e) {
					throw new LinkageError();
				}
				catch (Exception e) {
					//une erreur est survenu, la page ne sera juste pas téléchargé
				}
        }	
		return liste;  
	}

}
