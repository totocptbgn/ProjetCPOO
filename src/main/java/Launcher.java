public interface Launcher<T extends Thread> {
	public String getNom();
	public state getEtat();

	public void pause(); 	// WORK -> WAIT
	public void restart(); 	// WAIT -> WORK
	public void delete(); 	// * -> FAIL
	public void start(); 	// NEW -> WORK

	public static enum state {
		STOP, WORK, WAIT, NEW, FAIL, SUCCESS;
	}
}
