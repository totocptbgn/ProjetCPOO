import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Interface textuelle pour l'aspirateur de sites
 *
 *  [ ] list -a                             -> liste tout les id et nom des aspirateurs
 *  [x] create {-i} {-p} URL                -> creer un aspirateur d'images / pages
 *  [o] limit {-p} {-m} {-f} id/nom int     -> change la limite (max/profondeur/max pour un fichier) de l'aspirateur
 *  [x] limit -r id/nom                     -> remet la limite de base
 *  [x] limitless id/nom                    -> enlève la limite de fichier de base (dangereux)
 *  [o] whitelist -l id/nom                 -> donne la whitelist d'un aspirateur
 *  [o] whitelist -a id/nom [fichier]       -> ajoute un fichier à la whiteList de l'aspirateur (sans argument activera juste la whiteList sur l'aspirateur)
 *  [o] whitelist -r id/nom [fichier]       -> enlève un fichier à la whiteList de l'aspirateur (sans argument desactivera juste la whiteList sur l'aspirateur)
 *  [ ] list -p id/nom                      -> affiche la liste des pages d'un launcher (et pas aspirateur)
 *  [o] tolauncher -s [id/nom]              -> transforme l'aspirateur en launcher
 *  [o] tolauncher -m [id/nom]              -> transforme l'aspirateur en launchers
 *  [x] cancel [id/nom]                     -> suppression de l'aspirateur
 *
 *  [x] add [link]                          -> Create a launcher, ready to be started.
 * 	[x] start [id]  [name]                  -> Start the download of the given launcher, or the last created if not specified.
 * 	[x] list -l [state]                     -> Print launcher, with id, name, state and size.
 * 	[x] delete [id]  [name]                 -> Delete a launcher, set his state to FAIL. The launcher can't be started.
 * 	[x] pause [id]  [name]                  -> Pause a launcher, set his state to WAIT. The launcher can be unpaused.
 * 	[x] restart [id]  [name]                -> Unpause a launcher and continue the download. The laucher's state must be WAIT.
 * 	[x] exit                                -> Exit the programm.
 * 	[x] clear                               -> Clear the terminal.
 * 	[x] help                                -> Print the manual page.
 * 	[x] startnew [link]                     -> Shortcut to create and start a launcher directly.
 * 	[x] startall                            -> Shortcut to start all the launchers directly.
 * 	[x] startat [time] [id  name]           -> Launch but begin to download after the given time (in seconds).
 * 	[x] startlimit [time] [id  name]        -> Launch and delete the launcher if the download is not done before the given time
 *
 *	[ ] pas fait
 * 	[o] pas fini / à modifier
 * 	[x] fini :)
 *
 * 	"???" = problèmes avec GestionnaireAspirateur
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
					print(ColoredOutput.set(Color.RED, "[Error]") + " UnsupportedOperationException, a connection ");
				} catch (IllegalStateException e) {
					print(ColoredOutput.set(Color.RED, "[Error]") + " IllegalStateException, an internal error happened...");
				} catch (RuntimeException e) {
					print(ColoredOutput.set(Color.RED, "[Error]") + " RuntimeException, a file modification error happened...");
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

		// Liste les aspis ou les pages d'un aspi ou les launchers
		if (cmd.matches("list.*")) {
			String [] s = cmd.split(" +");
			if (s.length == 2) {
				if (s[1].equals("-l")) {
					Set<Launcher> set = gstn.listOfAll();
					printListOfLauncher(set);
					return;
				}
				if (s[1].equals("-a")) {
					// Liste les aspirateur : A FAIRE
					return;
				}
			}
			if (s.length == 3) {
				if (s[1].equals("-p")) {
					// Liste les pages d'un aspirateur : A FAIRE
				}
				if (s[1].equals("-l")) {
					if (s[2].equals("new")) {
						Set<Launcher> set = gstn.listNew();
						printListOfLauncher(set);
						return;
					}
					if (s[2].equals("wait")) {
						Set<Launcher> set = gstn.listWait();
						printListOfLauncher(set);
						return;
					}
					if (s[2].equals("started")) {
						Set<Launcher> set = gstn.listLaunch();
						printListOfLauncher(set);
						return;
					}
					if (s[2].equals("done")) {
						Set<Launcher> set = gstn.listEnd();
						printListOfLauncher(set);
						return;
					}
					if (s[2].equals("all")) {
						Set<Launcher> set = gstn.listOfAll();
						printListOfLauncher(set);
						return;
					}
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "list -l [new | wait | started | done | all]");
					return;
				}
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "list -a | list -p [id/nom] | list -l [new | wait | started | done | all]");
			return;
		}

		// Créer un aspi
		if (cmd.matches("create.*")) {
			String [] s = cmd.split(" +");
			if (s.length != 3) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "create [-i | -p | -pi] [link]");
				return;
			}
			if (!s[2].matches("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&/=]*)")) {
				print(ColoredOutput.set(Color.RED, "[Error] ") + "The link is not correct.");
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "create [-i | -p | -pi] [link]");
				return;
			}
			// Récupérer le lien (et le tester)
			if (s[1].equals("-i")) {
				// Créer un aspi d'image
				int id = aspi.addAspirateurImages(s[2]);
				print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Images aspi created with id [" + id + "].");
				return;
			}
			if (s[1].equals("-p")) {
				// Créer un aspi de pages
				int id = aspi.addAspirateurPages(s[2]);
				print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Pages aspi created with id [" + id + "].");
				return;
			}
			if (s[1].equals("-ip") || s[1].equals("-pi")) {
				// Créer un aspi de pages et d'images
				int id = aspi.addAspirateurPagesWithImages(s[2]);
				print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Images and pages aspi created with id [" + id + "].");
				return;
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "create [-i | -p | -pi] [link]");
			return;
		}

		// Enlever la limite de fichier de base d'un aspi
		if (cmd.matches("limitless.*")) {
			String [] s = cmd.split(" +");
			if (s.length == 2) {
				Aspirateur asp = getAspi(s[2]);
				if (asp == null) {
					print(ColoredOutput.set(Color.RED, "[Error] ") + "The aspi was not found.");
					return;
				}
				asp.setLimit(false);
				print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Limits was removed for aspi with id [" + asp.getId() + "].");
				return;
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limitless [id | nom]");
			return;
		}

		// Change les limites d'un aspi
		if (cmd.matches("limit.*")) {
			String [] s = cmd.split(" +");
			if (s.length == 4) {
				Aspirateur asp = getAspi(s[2]);
				if (asp == null) {
					print(ColoredOutput.set(Color.RED, "[Error] ") + "The aspi was not found.");
					return;
				}

				long limit = 0;
				try {
					limit = Long.valueOf(s[3]);
				} catch (NumberFormatException e) {
					print(ColoredOutput.set(Color.RED, "[Error] ") + "The limit must be an integer.");
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limit [-p | -m | -f | -r] [id | name] [int]");
					return;
				}
				if (s[1].equals("-p")) {
					asp.limitProfondeur(limit);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Depth limit was set to " + limit + " for aspi with id [" + asp.getId() + "].");
					return;
				}
				if (s[1].equals("-m")) {
					asp.limitMax(limit);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Max limit was set to " + limit + " for aspi with id [" + asp.getId() + "].");
					return;
				}
				if (s[1].equals("-f")) {
					asp.limitSize(limit);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "Size limit was set to " + limit + " for aspi with id [" + asp.getId() + "].");

					return;
				}
				else {
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limit [-p | -m | -f | -r] [id | name] [int]");
					return;
				}
			} else if (s.length == 3 && s[1].equals("-r")) {
				Aspirateur asp = getAspi(s[2]);
				if (asp == null) {
					print(ColoredOutput.set(Color.RED, "[Error] ") + "The aspi was not found.");
					return;
				}
				// Remettre la limite de base ???
				print(ColoredOutput.set(Color.GREEN, "[Info] ") + "limit was reset to default for aspi with id [" + asp.getId() + "].");
				return;
			}
			print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "limit [-p | -m | -f | -r] [id | name] [int | (none)]");
			return;
		}

		if (cmd.matches("whitelist.*")) {
			String [] s = cmd.split(" +");
			if (s.length != 3 && s.length != 4) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r] [id | nom] [file] ");
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r | -l] [id | nom]");
				return;
			}

			Aspirateur asp = getAspi(s[2]);
			if (asp == null) {
				print(ColoredOutput.set(Color.RED, "[Error] ") + "The aspi was not found.");
				return;
			}

			if (s.length == 3) {
				if (s[1].equals("-l")) {
					// Afficher la whitelist : A FAIRE ???
				}

				if (s[1].equals("-a")) {
					// Ajoute la whitelist
					asp.whiteList(true);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "whitelist enabled for aspi with id [" + asp.getId() + "].");
				}
				if (s[1].equals("-r")) {
					// Retire la whitelist
					asp.whiteList(false);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "whitelist disabled for aspi with id [" + asp.getId() + "].");
				}
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r] [id | nom] [file] ");
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r | -l] [id | nom]");
				return;
			}
			if (s.length == 4) {
				if (s[1].equals("-a")) {
					// Ajouter une whitelist
					asp.addWhiteList(s[3]);
					print(ColoredOutput.set(Color.GREEN, "[Info] ") + "site added to the whitelist of aspi with id [" + asp.getId() + "].");
				}

				if (s[1].equals("-r")) {
					// Retirer la whitelist ???
				}
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r] [id | nom] [file] ");
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "whitelist [-a | -r | -l] [id | nom]");
				return;
			}
		}

		// Transforme un aspi en launcher
		if (cmd.matches("tolauncher.*")) {
			String [] s = cmd.split(" +");
			Aspirateur asp = getAspi(s[1]);
			if (asp == null) {
				print(ColoredOutput.set(Color.RED, "[Error] ") + "The aspi was not found.");
				return;
			}
			if (s.length == 3) {
				if (s[1].equals("-s")) {
					// Convertir en un launcher ???
					aspi.aspirateurToLauncher(asp.getId()).thenRun(() -> {
						print(ColoredOutput.set(Color.GREEN, "[Info] ") + "the launcher " /* + l.getId() */ + "was created from aspi [" + asp.getId() + "].");
					});
				}
				if (s[1].equals(("-m"))) {
					// Convertir en plusieurs launchers ???
					aspi.aspirateurToLaunchers(asp.getId());
				} else {
					print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "tolauncher [-s | -m] [id | nom]");
				}
				return;
			} else {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "tolauncher [-s | -m] [id | nom]");
			}
			return;
		}

		// Supprime l'aspi
		if (cmd.matches("cancel.*")) {
			String [] s = cmd.split(" +");
			if (s.length != 2) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage] ") + "cancel [id | nom]");
			}
			Aspirateur asp = getAspi(s[1]);
			if (asp == null) {
				print(ColoredOutput.set(Color.RED, "[Error] ") + "the aspi was not found.");
				return;
			}
			if (asp.getState() == Aspirateur.state.DIE) {
				print(ColoredOutput.set(Color.RED, "[Error] ") + "the aspi was already canceled.");
				return;
			}
			asp.cancel();
			print(ColoredOutput.set(Color.GREEN, "[Info] ") + "the aspi with id [" + asp.getId() + "] was cancelled.");
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
						String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
						if (l.getTotalSize() != -1L) {
							s += (" of the size of " + humanReadableSize(l.getTotalSize()) + ".");
						} else {
							s += ".";
						}
						print(s);
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						gstn.launch(l.getNom());
						String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
						if (l.getTotalSize() != -1L) {
							s += " of the size of " + humanReadableSize(l.getTotalSize()) + ".\n";
						} else {
							s += ".";
						}
						print(s);
						return;
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
			return;
		}

		// Start le dernier Laucher ajouté
		if (cmd.matches("\\p{Blank}*start\\p{Blank}*")) {
			LauncherIntern t;
			try {
				t = gstn.getCurrentNew();
				print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading [" + t.getId() + "].");
				gstn.launch();
			} catch (NullPointerException n) {
				print(ColoredOutput.set(Color.RED, "[Error]") + " there is no launcher to start.");
			}
			return;
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
						print("Deleted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.delete(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						print("Deleted launcher " + l.getNom() + " [" + l.getId() + "]");
						gstn.delete(l.getNom());
						return;
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
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
							print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher is not downloading, state = " + l.getEtat() + ".");
							return;
						}
						print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "stopped launcher " + l.getNom() + " [" + l.getId() + "].");
						gstn.pause(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						if (l.getEtat() != Launcher.state.WORK) {
							print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher is not downloading, state = " + l.getEtat() + ".");
							return;
						}
						print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "stopped launcher " + l.getNom() + " [" + l.getId() + "].");
						gstn.pause(l.getNom());
						return;
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
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
							print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher is not paused, state = " + l.getEtat() + ".");
							return;
						}
						print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "restarted launcher " + l.getNom() + " [" + l.getId() + "].");
						gstn.restart(l.getId());
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						if (l.getEtat() != Launcher.state.WAIT) {
							print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher is not paused, state = " + l.getEtat() + ".");
							return;
						}
						print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "restarted launcher " + l.getNom() + " [" + l.getId() + "].");
						gstn.restart(l.getNom());
						return;
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
			return;
		}

		// Affiche l'aide
		if (cmd.matches("\\p{Blank}*help\\p{Blank}*")) {
			printManPage();
			return;
		}

		// Raccourci pour créer et lancer un launcher en une commande
		if (cmd.matches("^startnew .+")) {
			String link = cmd.substring(9);
			if (!link.matches("(http(s)?://.)(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}/([~*0-9a-zA-Z._\\-/=])+")) {
				print(ColoredOutput.set(Color.RED, "[Error]") + " This is not a correct link.");
				return;
			}
			newCommand("add " + link);
			newCommand("start");
			return;
		}

		// Launch tous les launchers
		if (cmd.matches("\\p{Blank}*startall\\p{Blank}*")) {
			Set<Launcher> set = gstn.listNew();
			set.forEach(l ->  {
				gstn.launch(l.getId());
				print(ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]");
			});
			return;
		}

		// Launch après un certain temps
		if (cmd.matches("\\p{Blank}*startat.*")) {
			String[] arr = cmd.split(" +");
			if (arr.length != 3) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage]") + " startat [seconds] [id | name]");
			}

			int time = 0;
			try {
				time = Integer.valueOf(arr[1]) * 1000;
			} catch (NumberFormatException e) {
				print(ColoredOutput.set(Color.RED, "[Error]") + " The time is incorrect.");
			}

			String name = arr[2];
			Iterator<Launcher> it = gstn.listNew().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						gstn.launchAt(l.getNom(), time).thenRun(() -> {
							String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
							if (l.getTotalSize() != -1L) {
								s += " of the size of " + humanReadableSize(l.getTotalSize()) + ".";
							} else {
								s += ".";
							}
							print(s);
						});
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						gstn.launchAt(l.getNom(), time).thenRun(() -> {
							String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
							if (l.getTotalSize() != -1L) {
								s += " of the size of " + humanReadableSize(l.getTotalSize()) + ".";
							} else {
								s += ".";
							}
							print(s);
						});
						return;
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
			return;
		}

		// Launch et limite le temps de téléchargement
		if (cmd.matches("\\p{Blank}*startlimit.*")) {
			String[] arr = cmd.split(" +");
			if (arr.length != 3) {
				print(ColoredOutput.set(Color.YELLOW, "[Usage]") + " launchat [seconds] [id | name]");
			}

			int time = 0;
			try {
				time = Integer.valueOf(arr[1]) * 1000;
			} catch (NumberFormatException e) {
				print(ColoredOutput.set(Color.RED, "[Error]") + " The time is incorrect.");
			}

			String name = arr[2];
			Iterator<Launcher> it = gstn.listNew().iterator();
			try {
				int id = Integer.valueOf(name);
				while (it.hasNext()) {
					Launcher l = it.next();
					if (id == l.getId()) {
						gstn.launch(l.getId());
						String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
						if (l.getTotalSize() != -1L) {
							s += " of the size of " + humanReadableSize(l.getTotalSize()) + ".";
						} else {
							s += ".";
						}
						print(s);
						int finalTime = time;
						gstn.deleteAt(l.getNom(), time).thenApplyAsync(e -> {
							if (e) {
								print(ColoredOutput.set(Color.GREEN, "[Info] ") + "the launcher " + l.getNom() + " was delete because not done after " + finalTime + "seconds.");
							}
							return null;
						});
						return;
					}
				}
			} catch (NumberFormatException e) {
				while (it.hasNext()) {
					Launcher l = it.next();
					if (name.equals(l.getNom())) {
						gstn.launch(l.getNom());
						String s = ColoredOutput.set(Color.GREEN, "[Info] ") +  "started downloading " + l.getNom() + " [" + l.getId() + "]";
						if (l.getTotalSize() != -1L) {
							s += " of the size of " + humanReadableSize(l.getTotalSize()) + ".";
						} else {
							s += ".";
						}
						print(s);int finalTime = time;
						gstn.deleteAt(l.getNom(), time).thenApplyAsync(b -> {
							if (b) {
								print(ColoredOutput.set(Color.GREEN, "[Info] ") + "the launcher " + l.getNom() + " was delete because not done after " + finalTime + "seconds.");
							}
							return null;
						});
					}
				}
			}
			print(ColoredOutput.set(Color.RED, "[Error]") + " the launcher was not found...");
			return;
		}

		// Commandes mal utilisées
		if (cmd.matches("\\p{Blank}*add\\p{Blank}*")) {
			print(ColoredOutput.set(Color.YELLOW, "[Usage]") + " add [link]");
			return;
		}
		if (cmd.matches("\\p{Blank}*delete\\p{Blank}*")) {
			print(ColoredOutput.set(Color.YELLOW, "[Usage]") + " delete [id | name]");
			return;
		}
		if (cmd.matches("\\p{Blank}*pause\\p{Blank}*")) {
			print(ColoredOutput.set(Color.YELLOW, "[Usage]") + " pause [id | name]");
			return;
		}

		// Commandes non reconnues
		print(ColoredOutput.set(Color.YELLOW, "[Unknown command] ") + cmd);
	}

	// Affiche les launcher avec leur id, état, nom et taille
	private static void printListOfLauncher(Set<Launcher> set) {
		Iterator<Launcher> it = set.iterator();
		if (!it.hasNext()) {
			print(ColoredOutput.set(Color.RED, "[Error]") + " there is no launcher to print.");
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
		System.out.print("\r");
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
		System.out.print("> ");
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

	// Affiche la page d'aide
	private static void printManPage() {
		System.out.print(
				"\r" +
				"-- " + ColoredOutput.set(Color.YELLOW, "About") + " --\n" +
						"Programm created by Dao Thauvin & Thomas Copt-Bignon for CPOO Final project.\n> "
		);
	}


	private static Aspirateur getAspi(String name) {
		Aspirateur asp;
		try {
			int id = Integer.valueOf(name);
			asp = aspi.getAspirateur(id);
		} catch (NumberFormatException e) {
			asp = aspi.getAspirateur(name);
		}
		return asp;
	}
}
