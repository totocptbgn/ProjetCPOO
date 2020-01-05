# Projet CPOO

> Projet de développement d'un gestionnaire de téléchargement & d'un aspirateur de sites web.
> Année 2019 - 2020
> Dao Thauvin, Thomas Bignon

## Fonctionnalités implémentées

### Gestionnaire de téléchargement
   - création d'une tâche de téléchargement   
   - lancement d'une tâche de téléchargement  
   - affichage des différentes tâches  
   - suppression d'une tâche de téléchargement  
   - pause d'une tâche de téléchargement  
   - reprise d'une tâche de téléchargement  
   - lancement d'une page de téléchargement avec délai  
   - lancement d'une page de téléchargement avec annulation de la tâche si non fini après un temps donné  
  
### Aspirateur de sites
   - affichage des différents aspirateurs  
   - création d'un aspirateur avec images et/ou pages  
   - gestion d'une limite de taille total/taille par fichier/nombre de fichier/profondeur  
   - gestion d'une whitelist de sites  
   - liste des pages d'un aspirateur (après transformation en tache)  
   - transformation en une tache ou plusieurs  
   - annulation d'une aspiration  
  
## Contenu  
Il y a 4 répertoires : 
> Contenu du code : `src/java/nom_repertoire_sans_majuscule`
> Contenu du jar : `build/libs` 

  **IU**  - Interface pour la gestion de téléchargement
   - `Color.java` - énumération des couleurs utilisées
   - `ColoredOutput.java` - changement de la couleur de l'affichage 
   - `Interface.java` - Classe principale de l'interface textuelle
   
   **IUAspirateur** - Interface pour la gestion des aspirateurs
   - `InterfaceAspirateur.java`  - Interface textuelle
  
**Aspirateur**  - Bibliotheque de gestion des aspirateurs  
- `Aspirateur.java` - Arbre des pages à aspirer
- `AspirateurURL.java` - Un noeud de l'arbre, aspiration d'une page
- `GestionnaireAspirateur.java` - Gestion des aspirateurs

**DownloadManager** - Bibliotheque de gestion de téléchargement
- `Gestionnaire.java` - Gestion des téléchargements
- `LauncherTelechargement.java` - `Launcher` permettant de télécharger des pages web implémentant `Launcher` et `LauncherIntern`   
- `Launcher.java` - Interface pour une vision externe au package du launcher 
- `LauncherIntern.java` - Interface pour une vision interne au package du launcher],   
- `Tache.java` - Interface pour une tache de téléchargement
- `TacheTelechargement.java` - Implémentation de `Tache` permettant de téléchargement une page web

**Remarque** : la totalité des fonctions et classes ont une javadoc.
  
## Compilation
  
   Le jar est déjà compilé mais si vous voulez le faire par vous-même, utilisez `gradle fatJar` dans les 4 répertoires dans l'ordre :  
      - `DownloadManager/`
      - `Aspirateur/`
      - `IU/`
      - `IUAspirateur/`
        
## Exécution

Le programme nécessite **Java 11**.

Interface pour le gestionnaire de téléchargement :  

    java -jar  IU\build\libs\DownLoad-1.0.jar  

Interface pour le gestionnaire des aspirateurs :  

    java -jar  IUAspirateur\build\libs\Aspirateur-1.0.jar  

## Syntaxe

### Description
La syntaxe est assez simple, elle est inspirée d'interfaces textuelles classiques avec `[commande] [option] [paramêtre]`. La liste des commandes peut être affichée dans le programme avec la commande  `help`. Leurs utilisations y sont détaillées.

### Exemples
Voici des exemples d'utilisations de raccourcis basiques pour télécharger ou aspirer des fichiers sans paramettrage.
Gestionnaire de téléchargement :  

    > startnew https://a.wattpad.com/cover/191731194-352-k261912.jpg

Gestionnaire d'aspirateur :  

    > mirror https://www.irif.fr/~francoisl//l3algo.html  
      
