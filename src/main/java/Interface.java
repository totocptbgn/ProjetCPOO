
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Classe gérant l'interface du gestionnaire
 *
 * Liste des commandes à faire :
 *
 *   launch [name]
 *   add [link]
 *   delete [name]
 *   pause [name]
 *   list [type] | all
 *   help
 *
 */

public class Interface {

	private static boolean running = true;

	public static void main (String [] args) throws InterruptedException, IOException {
		Gestionnaire g = new Gestionnaire();
		Scanner sc = new Scanner(System.in);
		ColoredOutput.init();
		printIntro();

		while (running) {
			if(sc.hasNextLine()) {
				System.out.print("> ");
				String cmd = sc.nextLine();
				newCommand(cmd);
			}
		}
	}

	/**
	 * Recoit une commande sous forme de String et la traite.
	 */

	private static void newCommand(String cmd) {
		if (cmd.equals("exit")) {
			exit();
			return;
		}
		System.out.println(ColoredOutput.set(Color.BLUE, "  " + cmd));
	}

	private static void exit() {
		running = false;
		System.out.println("Exit.");
	}

	private static void printIntro() {
		System.out.print(
				" + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +\n" +
				" |                                                                                                       |\n" +
				" |                                                                                                       |\n" +
				" |               ██████╗  ██████╗ ██╗    ██╗███╗   ██╗██╗      ██████╗  █████╗ ██████╗                   |\n" +
				" |               ██╔══██╗██╔═══██╗██║    ██║████╗  ██║██║     ██╔═══██╗██╔══██╗██╔══██╗                  |\n" +
				" |               ██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║██║     ██║   ██║███████║██║  ██║                  |\n" +
				" |               ██║  ██║██║   ██║██║███╗██║██║╚██╗██║██║     ██║   ██║██╔══██║██║  ██║                  |\n" +
				" |               ██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║███████╗╚██████╔╝██║  ██║██████╔╝                  |\n" +
				" |               ╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═════╝                   |\n" +
				" |                                                                                                       |\n" +
				" |                     ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗██████╗                     |\n" +
				" |                     ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗                    |\n" +
				" |                     ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗  ██████╔╝                    |\n" +
				" |                     ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝  ██╔══██╗                    |\n" +
				" |                     ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗██║  ██║                    |\n" +
				" |                     ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝                    |\n" +
				" |                                                                                                       |\n" +
				" |                                                    *    Dao Thauvin & Thomas Copt-Bignon     *        |\n" +
				" |                                                    *             version 1.0.0               *        |\n" +
				" |                                                    *  CPOO | Final project | year 2019-2020  *        |\n" +
				" |                                                                                                       |\n" +
				" + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +\n\n"
			);
	}

	/**
	 *  Use an init() to be sure that the ouput is set to WHITE by default.
	 *  Use set() to transform a string to a colored string for the terminal.
	 */

	static class ColoredOutput {
		public static Color basic = Color.WHITE;

		public static String set(Color color, String s) {
			return (char) 27 + "[" + color.code + "m" + s + (char) 27 + "[" + basic.code + "m";
		}

		public static void init(PrintStream output, Color defaultColor) {
			basic = defaultColor;
			output.print((char) 27 + "[" + basic.code + "m");
		}

		public static void init() {
			init(System.out, Color.WHITE);
		}
	}

	/**
	 *  Enumeration of colors for the ColoredOutput class.
	 *  You can add value from there (FG Code) :
	 *  https://en.wikipedia.org/wiki/ANSI_escape_code#3/4_bit
	 */

	public static enum Color {

		WHITE(97),
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
