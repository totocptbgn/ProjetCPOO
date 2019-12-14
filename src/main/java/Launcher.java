
public interface Launcher<T extends Thread> {
	String getNom();
	void pause();
	void restart();
	void delete();
	void start();
	void run();
	state getEtat();
	static enum state {
		STOP,LAUNCH,WAIT,NEW,FAIL,SUCCESS;
	}
}
