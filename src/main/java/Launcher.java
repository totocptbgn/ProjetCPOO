
public interface Launcher<T extends Thread> {
	String getName();
	void stop();
	void delete();
	void start();
	static enum state {
		STOP,LAUNCH,WAIT,NEW;
	}
}
