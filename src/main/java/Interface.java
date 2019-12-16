import java.io.IOException;
import java.util.Scanner;

/**
 * Classe gérant l'interface du gestionnaire
 */

public class Interface {

	public static String getProgressBar(int progress) {
		if (progress < 0 || progress > 100) throw new IllegalArgumentException();

		StringBuilder str = new StringBuilder();
		switch (progress) {
			case 0:
				str.append(" 0%   ");
				break;
			case 100:
				str.append(" 100% ");
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				str.append(" ").append(progress).append("%   ");
				break;
			default:
				str.append(" ").append(progress).append("%  ");
		}
		str.append("| ");

		int size = progress;
		for (int i = 0; i < size; i++) {
			str.append("#");
		}
		for (int i = 0; i < 100 - size; i++) {
			str.append(" ");
		}
		str.append(" |");
		if (progress == 100) str.append(" [" + ColoredOutput.set(Color.GREEN, "Done") + "]\n");
		return str.toString();
	}

	public static void main (String [] args) throws InterruptedException, IOException {

		System.out.println("> Download of \"irif.fr/~bignon\" :");
		for (int i = 0; i <= 100; i++) {
			System.out.print("\r" + getProgressBar(i));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("> Download of \"irif.fr/~thauvin\" :");
		for (int i = 0; i <= 100; i++) {
			System.out.print("\r" + getProgressBar(i));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		


	}

	/**
	 *  Use set() to transform a string to a colored string for the terminal.
	 */

	static class ColoredOutput {
		public static String set(Color color, String s) {
			return (char) 27 + "[" + color.code + "m" + s + (char) 27 + "[97m";
		}
	}

	/**
	 *  Enumeration of colors for the ColoredOutput class.
	 *  You can add value from there (FG Code) :
	 *  https://en.wikipedia.org/wiki/ANSI_escape_code#3/4_bit
	 */

	public enum Color {

		RED(31),
		GREEN(32),
		YELLOW(33),
		BLUE(34);

		public final int code;
		private Color(int n) {
			this.code = n;
		}
	}
}
