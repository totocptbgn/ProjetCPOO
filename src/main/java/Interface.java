import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Classe gérant l'interface du gestionnaire
 *
 * Liste des commandes à faire :
 *
 *   start 	y		 		ok
 *   add 	y				ok
 *   delete y				ok
 *   pause 	y				ok
 *   list 	y				ok
 *   help					à faire
 *   restart                à faire
 *
 *
 *   Colors :
 *   RED : error, FAIL
 *   YELLOW : usage, unknown cmd, WAIT
 *   BLUE : list tab, some print (?)
 *   GREEN : SUCCESS, some successfull print (?)
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
			} catch (UnsupportedOperationException e) {
				System.out.println("Error : UnsupportedOperationException, a connection ");
			} catch (IllegalStateException e) {
				System.out.println("Error: IllegalStateException, an internal error happened...");
			}  catch (RuntimeException e) {
				System.out.println("Error: RuntimeException, a file modification error happened...");
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

			if (!link.matches("(http(s)?://.)(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}/([~*0-9a-zA-Z._\\-/=])+")) {
				System.out.println("Error: This is not a correct link.");
				return;
			}
			gstn.addLauncher(link);
			System.out.println(gstn.getCurrentNew().getNom() + " was added with id : [" + gstn.getCurrentNew().getId() + "]");
			return;
		}

		// Start un launcher par son nom ou son id
		if (cmd.matches("^start [^\\p{Blank}]+")) {
			String name = cmd.substring(6);
			Iterator<Launcher> it = gstn.listNew().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						gstn.launch(l.getId());
						System.out.print("Started launcher " + l.getNom() + " [" + l.getId() + "]");
						if (l.getTotalSize() != -1L) {
							System.out.print("of the size of " + humanReadableSize(l.getTotalSize()) + ".\n");
						} else {
							System.out.print("\n");
						}
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						gstn.launch(l.getNom());
						System.out.print("Started launcher " + l.getNom() + " [" + l.getId() + "]");
						if (l.getTotalSize() != -1L) {
							System.out.print(" of the size of " + humanReadableSize(l.getTotalSize()) + ".\n");
						} else {
							System.out.print("\n");
						}
						return;
					}
				}
			}
			System.out.println("Error: the launcher was not found...");
			return;
		}

		// Start le dernier Laucher ajouté
		if (cmd.matches("\\p{Blank}*start\\p{Blank}*")) {
			Launcher t;
			try {
				t = gstn.getCurrentNew();
				System.out.println("Launching [" + t.getId() + "]");
				gstn.launch();
			} catch (NullPointerException n) {
				System.out.println("Error: there is no launcher to start.");
			}
			return;
		}

		// Liste tous les launchers
		if (cmd.matches("\\p{Blank}*list\\p{Blank}*")) {
			Set<Launcher> set = gstn.listOfAll();
			printListOfLauncher(set);
			return;
		}

		if (cmd.matches("^list .+")) {
			String type = cmd.substring(5);
			if (type.matches("new\\p{Blank}*")) {
				Set<Launcher> set = gstn.listNew();
				printListOfLauncher(set);
				return;
			}
			if (type.matches("wait\\p{Blank}*")) {
				Set<Launcher> set = gstn.listWait();
				printListOfLauncher(set);
				return;
			}
			if (type.matches("started\\p{Blank}*")) {
				Set<Launcher> set = gstn.listLaunch();
				printListOfLauncher(set);
				return;
			}
			if (type.matches("done\\p{Blank}*")) {
				Set<Launcher> set = gstn.listEnd();
				printListOfLauncher(set);
				return;
			}
			if (type.matches("all\\p{Blank}*")) {
				Set<Launcher> set = gstn.listOfAll();
				printListOfLauncher(set);
				return;
			}
			System.out.println("Usage: list [new | wait | started | done | all]");
		}

		// Delete un launcher par son nom ou son id
		if (cmd.matches("^delete [^\\p{Blank}]+")) {
			String name = cmd.substring(7);
			Iterator<Launcher> it = gstn.listOfAll().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						System.out.println("Deleted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.delete(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						System.out.println("Deleted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.delete(l.getNom());
						return;
					}
				}
			}
			System.out.println("Error: the launcher was not found...");
			return;
		}

		// Pause un launcher par son nom ou son id
		if (cmd.matches("^pause [^\\p{Blank}]+")) {
			String name = cmd.substring(6);
			Iterator<Launcher> it = gstn.listOfAll().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						if (l.getEtat() != Launcher.state.WORK) {
							System.out.println("Error: the launcher is not downloading, state = " + l.getEtat() + ".");
							return;
						}
						System.out.println("Stoped launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.pause(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						if (l.getEtat() != Launcher.state.WORK) {
							System.out.println("Error: the launcher is not downloading, state = " + l.getEtat() + ".");
							return;
						}
						System.out.println("Stoped launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.pause(l.getNom());
						return;
					}
				}
			}
			System.out.println("Error: the launcher was not found...");
			return;
		}

		// Pause un launcher par son nom ou son id
		if (cmd.matches("^restart [^\\p{Blank}]+")) {
			String name = cmd.substring(8);
			Iterator<Launcher> it = gstn.listOfAll().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						if (l.getEtat() != Launcher.state.WAIT) {
							System.out.println("Error: the launcher is not paused, state = " + l.getEtat() + ".");
							return;
						}
						System.out.println("Restarted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.restart(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						if (l.getEtat() != Launcher.state.WAIT) {
							System.out.println("Error: the launcher is not paused, state = " + l.getEtat() + ".");
							return;
						}
						System.out.println("Restarted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.restart(l.getNom());
						return;
					}
				}
			}
			System.out.println("Error: the launcher was not found...");
			return;
		}

		// Print manual page
		if (cmd.matches("\\p{Blank}*help\\p{Blank}*")) {
			System.out.print(
					""
			);
		}

		// Commandes mal utilisées
		if (cmd.matches("\\p{Blank}*add\\p{Blank}*")) {
			System.out.println("Usage: add [link]");
			return;
		}
		if (cmd.matches("\\p{Blank}*delete\\p{Blank}*")) {
			System.out.println("Usage: delete [id | name]");
			return;
		}
		if (cmd.matches("\\p{Blank}*pause\\p{Blank}*")) {
			System.out.println("Usage: pause [id | name]");
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
	private static void printListOfLauncher(Set<Launcher> set) {
		Iterator<Launcher> it = set.iterator();
		if (!it.hasNext()) {
			System.out.println("Error: there is no launcher to print.");
			return;
		}

		int id = 2;
		int name = 4;
		int state = 5;
		int dl = 4;

		while (it.hasNext()) {
			Launcher l = it.next();
			id = Math.max(((Integer)l.getId()).toString().length(), id);
			name = Math.max(l.getNom().length(), name);
			state = Math.max(l.getEtat().toString().length(), state);
			dl = Math.max(getSizeDownloaded(l).length(), dl);
		}

		it = set.iterator();

		System.out.print("+-");
		printChara(id, '-');
		System.out.print("-+-");
		printChara(name, '-');
		System.out.print("-+-");
		printChara(state, '-');
		System.out.print("-+-");
		printChara(dl, '-');
		System.out.print("-+\n");

		System.out.print("| ID");
		printChara(id - 2, ' ');
		System.out.print(" | NAME");
		printChara(name - 4, ' ');
		System.out.print(" | STATE");
		printChara(state - 5, ' ');
		System.out.print(" | SIZE");
		printChara(dl - 4, ' ');
		System.out.print(" |\n");

		System.out.print("+-");
		printChara(id, '-');
		System.out.print("-+-");
		printChara(name, '-');
		System.out.print("-+-");
		printChara(state, '-');
		System.out.print("-+-");
		printChara(dl, '-');
		System.out.print("-+\n");


		while (it.hasNext()) {
			Launcher l = it.next();
			System.out.print("| " + l.getId());
			printChara(id - ((Integer)l.getId()).toString().length(), ' ');
			System.out.print(" | " + l.getNom());
			printChara(name - l.getNom().length(), ' ');
			System.out.print(" | " + l.getEtat());
			printChara(state - l.getEtat().toString().length(), ' ');
			System.out.print(" | " + getSizeDownloaded(l));
			printChara(dl - getSizeDownloaded(l).length(), ' ');
			System.out.print(" |\n");
		}

		System.out.print("+-");
		printChara(id, '-');
		System.out.print("-+-");
		printChara(name, '-');
		System.out.print("-+-");
		printChara(state, '-');
		System.out.print("-+-");
		printChara(dl, '-');
		System.out.print("-+\n");
	}

	// Print n fois le charactère c (utile pour la fonction printListOfLauncher())
	private static void printChara(int n, char c) {
		for (int i = 0; i < n; i++) {
			System.out.print(c);
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
		try {
			return  (downloaded * 100 / total) + sizeof;
		} catch (ArithmeticException e) {
			return 0 + sizeof;
		}
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
		public static Color defaultColor = Color.WHITE;

		public static String set(Color color, String s) {
			return (char) 27 + "[" + color.code + "m" + s + (char) 27 + "[" + defaultColor.code + "m";
		}

		public static void init(PrintStream output, Color defaultColor) {
			ColoredOutput.defaultColor = defaultColor;
			output.print((char) 27 + "[" + ColoredOutput.defaultColor.code + "m");
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
