package downloadmanager;

public interface Tache extends Runnable {
	public long getSize();
	public String getURL();
}
