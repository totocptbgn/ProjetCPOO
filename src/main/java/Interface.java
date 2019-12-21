import javax.print.attribute.standard.NumberUp;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
 *   (option -v pour verbal)
 */

public class Interface {

	private static boolean running = true;
	private static Gestionnaire gstn;


	public static void main (String [] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		clearTerminal();
		ColoredOutput.init();
		gstn = new Gestionnaire();
		printHeader();
		
		while (running) {
			System.out.print("> ");
			String cmd = sc.nextLine();
			try {
				newCommand(cmd);
			} catch (Exception e) {
				System.out.print(" ");
				System.out.println(ColoredOutput.set(Color.RED, "Task failed... : "));
				e.printStackTrace();
			}
		}
	}

	/**
	 * Recoit une commande sous forme de String et la traite.
	 */

	private static void newCommand(String cmd) throws IOException {

		// Ne fais rien quand rien n'est tapé
		if (cmd.matches("^\\p{Blank}*$")) {
			return;
		}

		// Ferme le programme
		if (cmd.matches("\\p{Blank}*exit\\p{Blank}*")) {
			exit();
			clearTerminal();
			return;
		}

		// Clear le terminal
		if (cmd.matches("\\p{Blank}*clear\\p{Blank}*")) {
			clearTerminal();
			return;
		}

		// Créé un Launcher
		if (cmd.matches("^add .+")) {
			String link = cmd.substring(4);

			if (!link.matches("(http(s)?:\\/\\/.)(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\/([*0-9a-zA-Z._\\-/=])+")) {
				System.out.println("Error: This is not a correct link.");
				return;
			}
			gstn.addLauncher(link);
			System.out.println(gstn.getCurrentNew().getNom() + " was added with id : [" + gstn.getCurrentNew().getId() + "]");
			return;
		}

		// Lance un launcher par son nom
		if (cmd.matches("^launch [^ ]+$")) {
			String name = cmd.substring(7);
			System.out.println("Not implemented...");
			return;
		}

		// Launch le dernier Laucher ajouté
		if (cmd.matches("\\p{Blank}*launch\\p{Blank}*")) {
			Launcher t;
			try {
				t = gstn.getCurrentNew();
				System.out.println("Launching [" + t.getId() + "]");
				gstn.launch();
			} catch (NullPointerException n) {
				System.out.println("Error: there is no launcher to start.");
			} finally {
				return;
			}
		}

		// Commandes mal utilisées
		if (cmd.matches("\\p{Blank}*add\\p{Blank}*")) {
			System.out.println(ColoredOutput.set(Color.RED, "Usage: add [link]"));
			return;
		}

		if (cmd.matches("\\p{Blank}*list\\p{Blank}*")) {
			Iterator<Launcher> it = gstn.listOfAll().iterator();
			printListOfLauncher(it);
			return;
		}

		// Commandes non reconnues
		System.out.println(ColoredOutput.set(Color.YELLOW, "Unknown command : '" + cmd + "'."));
	}

	// Ferme le programme
	private static void exit() {
		running = false;
	}

	// Affiche l'en-tête du programme
	private static void printHeader() throws IOException {
		System.out.print(
				" + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +\n" +
						" |                                                                                                                         |\n" +
						" |                                                                                                                         |\n" +
						" |                        ██████╗  ██████╗ ██╗    ██╗███╗   ██╗██╗      ██████╗  █████╗ ██████╗                            |\n" +
						" |                        ██╔══██╗██╔═══██╗██║    ██║████╗  ██║██║     ██╔═══██╗██╔══██╗██╔══██╗                           |\n" +
						" |                        ██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║██║     ██║   ██║███████║██║  ██║                           |\n" +
						" |                        ██║  ██║██║   ██║██║███╗██║██║╚██╗██║██║     ██║   ██║██╔══██║██║  ██║                           |\n" +
						" |                        ██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║███████╗╚██████╔╝██║  ██║██████╔╝                           |\n" +
						" |                        ╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═════╝                            |\n" +
						" |                                                                                                                         |\n" +
						" |                            ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗██████╗                                |\n" +
						" |                            ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗                               |\n" +
						" |                            ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗  ██████╔╝                               |\n" +
						" |                            ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝  ██╔══██╗                               |\n" +
						" |                            ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗██║  ██║                               |\n" +
						" |                            ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝                               |\n" +
						" |                                                                                                                         |\n" +
						" |                                                                        *    Dao Thauvin & Thomas Copt-Bignon     *      |\n" +
						" |                                                                        *             version 1.0.0               *      |\n" +
						" |                                                                        *  CPOO | Final project | year 2019-2020  *      |\n" +
						" |                                                                                                                         |\n" +
						" + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +\n\n" +
						"  Type 'help' to get details.\n" +
						"  The download directory is at " + gstn.pathDownload() + "\n\n"
		);
	}

	// Nettoie le terminal
	private static void clearTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	// Affiche les launcher avec leur id, état, nom et taille
	private static void printListOfLauncher(Iterator<Launcher> it) {
		while (it.hasNext()){
			Launcher l = it.next();
			System.out.println(l.getId() + " | " + l.getNom() + " | " + l.getEtat() + " | " + getSizeDownloaded(l));
		}
	}

	// Renvoie un String avec le pourcentage d'avancement de download et la taille totale en octet
	private static String getSizeDownloaded(Launcher l) {
		long downloaded  = l.getTotalSize() - l.getSizeLeft();
		long total = l.getTotalSize();
		String sizeof = "% of ";
		if (l.getTotalSize() == -1f) {
			sizeof += "unknown size.";
		} else {
			sizeof+= humanReadableSize(l.getTotalSize());
		}
		return (downloaded * 100 / total) + sizeof;
	}

	// Renvoie un String représentant une taille en octet facilemnt lisible
	private static String humanReadableSize(long bytes) {
		String s = bytes < 0 ? "-" : "";
		long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		return b < 1000L ? bytes + " B"
				: b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
				: (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
				: (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
				: (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
				: (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
				: String.format("%s%.1f EB", s, b / 1e6);
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
