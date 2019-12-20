
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

final class AspirateurURL {
	private final String URL;
	private AspirateurURL parent = null;
	
	private final boolean image;
	// activation de la whiteList
	private boolean whiteListed = false;
	// whiteList des sites
	private Set<String> whiteList = new HashSet<>();
		
	private long size;
	public long getSize() {
		return this.size;
	}
	
	public void setWhiteListed(boolean b) {
		whiteListed = b;
	}
	
	public String getURL() {
		return URL;
	}
	
	public boolean getWhiteListed() {
		if(parent != null) 
			return parent.getWhiteListed();
		return whiteListed;
	}
	
	public void addSitetoWhiteList(String s) {
		whiteListed = true;
	}
	
	// permet de savoir si une URL rentre dans la whiteList
	public boolean isWell(String URL) {
		if( URL.matches("#*")) return false;
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
	
	public AspirateurURL getParent() {
		return parent;
	}
	public long getProfondeur() {
		if(image) return 0;
		if(this.getParent() == null) return 0;
		return 1 + this.getParent().getProfondeur();
	}
	
	public AspirateurURL(String URL) {
		System.out.println(URL);
		image = false;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		this.size = conn.getContentLengthLong();
		conn.disconnect();
		this.URL = URL;
		String[] tab= URL.split("/");
		whiteList.add(tab[2]);
	}
	private AspirateurURL(String URL,AspirateurURL parent,boolean image) throws MalformedURLException, IOException {
		System.out.println(URL);
		this.image = image;		
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
		this.size = conn.getContentLengthLong();
		conn.disconnect();
		this.URL = URL;
		this.parent = parent;
		this.whiteList = parent.whiteList;
	}
	
	/**
	 * Renvoie les liens internes
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public Set<AspirateurURL> link() { 
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		Elements links = doc.select("a[href]");  
		Set<AspirateurURL> liste = new HashSet<>();
		for (Element link : links) {  
			if(this.isWell(link.attr("href")))
				try {
					liste.add(new AspirateurURL(transform(link.attr("href")),this,false));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
		} 
		return liste;
	}
	
	/*
	 * to have a good url
	 */
	private String transform(String s) {
		System.out.println(s);
		return s.split("#")[0];
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
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");  
		Set<AspirateurURL> liste = new HashSet<>();
		for (Element image : images) {  
			if(this.isWell(image.attr("src")))
				try {
					liste.add(new AspirateurURL(image.attr("src"),this,true));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
		return liste;  
	}
}
