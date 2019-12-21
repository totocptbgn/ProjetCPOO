
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
	
	private Set<String> inside;
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
		//System.out.println(URL);
		inside=new HashSet<String>();
		inside.add(URL);
		image = false;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			this.size = 0;
		}
	
		this.URL = URL;
		
		String[] tab= URL.split("/");
		whiteList.add(tab[2]);
		System.out.println(URL);
		
	}
	private AspirateurURL(String URL,AspirateurURL parent,boolean image) throws MalformedURLException, IOException {
		this.inside = parent.inside;
		this.inside.add(URL);
		this.image = image;		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new java.net.URL(URL).openConnection();
			this.size = conn.getContentLengthLong();
			conn.disconnect();
		}
		catch (MalformedURLException e) {
			this.size = 0;
		}
		this.URL = URL;
		this.parent = parent;
		this.whiteList = parent.whiteList;
		System.out.println(URL);
	}
	
	/**
	 * Renvoie les liens internes
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public Set<AspirateurURL> link() { 
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com")              
		               .timeout(1000*5)
		               .get();
		} catch (Exception e) {
			//le site n'autorise pas la connection
			return Set.of();
		}  
		Elements links = doc.select("a[href]");  
		
		Set<AspirateurURL> liste = new HashSet<>();
		for (Element link : links) {  
			if(this.isWell(link.attr("href")))
				try {
					String l = transform(link.attr("href"));
					if(!inside.contains(l)) {
						liste.add(new AspirateurURL(l,this,false));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
		} 
		links = doc.select("link[href]");
		for (Element link : links) {  
			if(this.isWell(link.attr("href")))
				try {
					String l = transform(link.attr("href"));
					if(!inside.contains(l)) {
						liste.add(new AspirateurURL(l,this,false));
					}
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
		
		s = s.split("#")[0];
		if(!s.contains("http")) {
			String[] ens = this.getURL().split("/");
			
			for(int i=1;i<ens.length - 1;i++)
				ens[0] = ens[0] + "/" + ens[i]; 
			s = ens[0] + "/" + s;
		}
		System.out.println(s);
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
		} catch (Exception e) {
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
						liste.add(new AspirateurURL(image.attr("src"),this,true));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
		return liste;  
	}

}
