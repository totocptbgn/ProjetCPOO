import java.io.IOException;
import java.util.Scanner;

/**
 * Interface textuelle pour l'aspirateur de sites
 *
 *  list aspi                           -> id et nom de l'aspirateur
 *  create {-i} {-p} URL                -> creer un aspirateur d'images / pages
 *  limit {-p} {-m} {-f} id/nom int     -> change la limite (max/profondeur/max pour un fichier) de l'aspirateur
 *  limit -r id/nom                     -> remet la limite de base
 *  limitless id/nom                    -> enlève la limite de fichier de base (dangereux)
 *  whiteList -l id/nom                 -> donne la whitelist d'un aspirateur
 *  whiteList -a id/nom [fichier]       -> ajoute un fichier à la whiteList de l'aspirateur (sans argument activera juste la whiteList sur l'aspirateur)
 *  whiteList -r id/nom [fichier]       -> enlève un fichier à la whiteList de l'aspirateur (sans argument desactivera juste la whiteList sur l'aspirateur)
 *  list pages id/nom                   -> affiche la liste des pages d'un launcher (et pas aspirateur)
 *  tolauncher -s [id/nom]              -> transforme l'aspirateur en launcher
 *  tolauncher -m [id/nom]               -> transforme l'aspirateur en launchers
 *  cancel [id/nom]                     -> suppression de l'aspirateur
 */

public class InterfaceAspirateur {

	private static Gestionnaire gstn;
	private static GestionnaireAspirateur aspi;

	public static void main (String [] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		clearTerminal();
		ColoredOutput.init();
		aspi = new GestionnaireAspirateur();
		gstn = aspi.getGestionnaire();
		printHeader();

		while (true) {
			System.out.print("> ");
			String cmd = sc.nextLine();
			new Thread(() -> {
				try {
					newCommand(cmd);
				} catch (UnsupportedOperationException e) {
					System.out.println(ColoredOutput.set(Color.RED, "[Error]") + " UnsupportedOperationException, a connection ");
				} catch (IllegalStateException e) {
					System.out.println(ColoredOutput.set(Color.RED, "[Error]") + " IllegalStateException, an internal error happened...");
				} catch (RuntimeException e) {
					System.out.println(ColoredOutput.set(Color.RED, "[Error]") + " RuntimeException, a file modification error happened...");
				}
				/*
				catch (IOException e) {
					System.out.println(ColoredOutput.set(Color.RED, "[Error]") + " IOException, an unexepected error happened...");
				} catch (InterruptedException e) {
					System.out.println(ColoredOutput.set(Color.RED, "[Error]") + " InterruptedException, an unexepected error happened...");
				}
				*/
			}).start();
		}
	}

	/**
	 * Recoit une commande sous forme de String et la traite.
	 */

	private static void newCommand(String cmd) {

		// Ne fais rien quand rien n'est tapé
		if (cmd.matches("^\\p{Blank}*$")) {
			return;
		}

		// Ferme le programme
		if (cmd.matches("\\p{Blank}*exit\\p{Blank}*")) {
			exit();
			return;
		}

		// Clear le terminal
		if (cmd.matches("\\p{Blank}*clear\\p{Blank}*")) {
			clearTerminal();
			System.out.print("> ");
			return;
		}

		// Affiche l'aide
		if (cmd.matches("\\p{Blank}*help\\p{Blank}*")) {
			printManPage();
			return;
		}

		// Liste les aspis ou les pages d'un aspi
		if (cmd.matches("list.*")) {
			String [] s = cmd.split(" +");
 			return;
		}

		// Créer un aspi
		if (cmd.matches("create.*")) {
			String [] s = cmd.split(" +");
			if (s.length != 3) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "create [-i | -p] [link]");
				return;
			}
			// Récupérer le lien (et le tester)
			if (s[1].equals("-i")) {
				// Créer un aspi d'image
			}
			if (s[1].equals("-p")) {
				// Créer un aspi de pages
			}
			if (s[1].equals("-ip") || s[1].equals("-pi")) {
				// Créer un aspi de pages et d'images
			} else {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "create [-i | -p] [link]");
				return;
			}
			return;
		}

		// Change les limites d'un aspi
		if (cmd.matches("limit.*")) {
			String [] s = cmd.split(" +");
			// Récupérer l'id ou le nom
			if (s.length == 4) {
				// Récupérer l'
				if (s[1].equals("-p")) {
					// limite prondeur
				}
				if (s[1].equals("-m")) {
					// limite max
				}
				if (s[1].equals("-f")) {
					// limit le max de fichier
				}
				if (s[1].equals("-r")) {
					// reset le max de fichier
				} else {
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limit [-p | -m | -f | -r] [id | name] [int]");
					return;
				}
			} else if (s.length == 3 && s[1].equals("-r")) {

			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limit [-p | -m | -f | -r] [id | name] [int | (none)]");
			return;
		}

		if (cmd.matches("limitless.*")) {
			String [] s = cmd.split(" +");
			if (s.length != 2) {
				// Récupérer l'aspi
				// Enlever la limite de fichier de base (?)
				return;
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limitless [id | nom]");
			return;
		}

		if (cmd.matches("whitelist.*")) {
			String [] s = cmd.split(" +");
			// Récupérer l'aspi
			if (s.length == 3) {
				if (s[1].equals("-l")) {
					// Afficher la whitelist
				}
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-l | -a | -r] [id | nom] [file | (none)]");
				return;
			}
			if (s.length == 4) {
				if (s[1].equals("-a")) {
					// Ajouter une whitelist
				}
				if (s[1].equals("-r")) {
					// Retirer la whitelist
				}
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-l | -a | -r] [id | nom] [file | (none)]");
				return;
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-l | -a | -r] [id | nom] [file | (none)]");
			return;
		}

		if (cmd.matches("tolauncher.*")) {
			String [] s = cmd.split(" +");
			// Récupérer l'aspi
			if (s.length == 3) {
				if (s[1].equals("-s")) {
					// Convertir en un launcher
				}
				if (s[1].equals(("-m"))) {
					// Convertir en plusieurs launchers
				} else {
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "tolauncher [-s | -m] [id | nom]");
				}
				return;
			} else {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "tolauncher [-s | -m] [id | nom]");
			}
			return;
		}

		if (cmd.matches("cancel.*")) {
			String [] s = cmd.split(" +");
			if (s.length == 2) {
				
			}
			return;
		}

		// Commandes non reconnues
		print(ColoredOutput.set(Color.YELLOW, "Unknown command : ") + cmd);
	}

	// Ferme le programme
	private static void exit() {
		clearTerminal();
		System.exit(0);
	}

	private static void print(String s) {
		System.out.print("\r" + s + "\n> ");
	}

	// Affiche l'en-tête du programme
	private static void printHeader() throws IOException {
		System.out.print(
				" + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +\n" +
						" |                                                                                                                         |\n" +
						" |                                                                                                                         |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "           ██╗    ██╗███████╗██████╗ ███████╗██╗████████╗███████╗ ") + "                              |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "           ██║    ██║██╔════╝██╔══██╗██╔════╝██║╚══██╔══╝██╔════╝ ") + "                              |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "           ██║ █╗ ██║█████╗  ██████╔╝███████╗██║   ██║   █████╗   ") + "                              |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "           ██║███╗██║██╔══╝  ██╔══██╗╚════██║██║   ██║   ██╔══╝   ") + "                              |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "           ╚███╔███╔╝███████╗██████╔╝███████║██║   ██║   ███████╗ ") + "                              |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "            ╚══╝╚══╝ ╚══════╝╚═════╝ ╚══════╝╚═╝   ╚═╝   ╚══════╝ ") + "                              |\n" +
						" |                                                                                                                         |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ███╗   ███╗██╗██████╗ ██████╗  ██████╗ ██████╗ ██╗███╗   ██╗ ██████╗   ") + "                     |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ████╗ ████║██║██╔══██╗██╔══██╗██╔═══██╗██╔══██╗██║████╗  ██║██╔════╝   ") + "                     |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ██╔████╔██║██║██████╔╝██████╔╝██║   ██║██████╔╝██║██╔██╗ ██║██║  ███╗  ") + "                     |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ██║╚██╔╝██║██║██╔══██╗██╔══██╗██║   ██║██╔══██╗██║██║╚██╗██║██║   ██║  ") + "                     |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ██║ ╚═╝ ██║██║██║  ██║██║  ██║╚██████╔╝██║  ██║██║██║ ╚████║╚██████╔╝  ") + "                     |\n" +
						" |                         " + ColoredOutput.set(Color.RED, "    ╚═╝     ╚═╝╚═╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝   ") + "                     |\n" +
						" |                                                                                                                         |\n" +
						" |                                                                        " + ColoredOutput.set(Color.YELLOW, " *    Dao Thauvin & Thomas Copt-Bignon     * ") + "    |\n" +
						" |                                                                        " + ColoredOutput.set(Color.YELLOW, " *                Part II                  * ") + "    |\n" +
						" |                                                                        " + ColoredOutput.set(Color.YELLOW, " *  CPOO | Final project | year 2019-2020  * ") + "    |\n" +
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

	// Print n fois le charactère c (utile pour la fonction printListOfLauncher())
	private static void printChara(int n, char c) {
		for (int i = 0; i < n; i++) {
			System.out.print(c);
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

	// Affiche la page d'aide
	private static void printManPage() {
		System.out.print(
				"\r" +
				"-- " + ColoredOutput.set(Color.YELLOW, "About") + " --\n" +
						"Programm created by Dao Thauvin & Thomas Copt-Bignon for CPOO Final project.\n> "
		);
	}
}
