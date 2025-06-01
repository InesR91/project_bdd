# RaPizz - Application de Gestion de Pizzeria

Application de bureau Java pour la gestion d'une pizzeria, développée avec Java Swing et PostgreSQL.

## Prérequis

- Java JDK 17 ou supérieur
- Maven
- PostgreSQL 12 ou supérieur
- Base de données "pizzeria" créée dans PostgreSQL

## Configuration

1. Assurez-vous que PostgreSQL est installé et en cours d'exécution
2. La base de données doit être configurée avec :
   - Nom de la base : pizzeria
   - Utilisateur : postgres
   - Mot de passe : postgres
   - Port : 5432

Le fichier de configuration `src/main/resources/application.properties` contient ces paramètres et peut être modifié si nécessaire.

## Compilation

Pour compiler l'application, exécutez la commande suivante à la racine du projet :

```bash
mvn clean package
```

Cette commande va créer un fichier JAR exécutable dans le dossier `target`.

## Exécution

Pour lancer l'application, utilisez la commande :

```bash
java -jar target/rapizz-1.0-SNAPSHOT.jar
```

## Guide d'utilisation de l'interface

L'interface est organisée en onglets pour une navigation facile :

### Onglet Commandes
- **Nouvelle commande** : Cliquez sur le bouton "Nouvelle commande" pour créer une commande
  - Sélectionnez un client dans la liste
  - Choisissez une pizza et sa taille
  - Le prix est calculé automatiquement
- **Modifier une commande** : Sélectionnez une commande dans la liste et cliquez sur "Modifier"
- **Filtrer les commandes** : Utilisez le menu déroulant "Statut" pour filtrer par état (En attente, En cours, Livrée, etc.)

### Onglet Clients
- **Nouveau client** : Créez un nouveau compte client
- **Rechercher** : Utilisez la barre de recherche pour trouver un client
- **Gérer le compte** : 
  - Sélectionnez un client pour voir son historique
  - Utilisez "Recharger compte" pour ajouter du crédit

### Onglet Livraisons
- **Gestion des livreurs** :
  - Ajoutez/Modifiez les informations des livreurs
  - Suivez leur disponibilité
- **Suivi des livraisons** :
  - Visualisez les livraisons en cours
  - Marquez une livraison comme terminée
  - Signalez un retard

### Fonctionnalités générales
- La barre de menu en haut permet d'accéder aux paramètres et à l'aide
- Double-cliquez sur un élément pour voir ses détails
- Les tableaux peuvent être triés en cliquant sur les en-têtes de colonnes
- Un code couleur indique les statuts (vert = terminé, orange = en cours, rouge = retard)

## Structure du projet

- `src/main/java/com/pizzeria/model` : Classes modèles (Client, Commande, etc.)
- `src/main/java/com/pizzeria/dao` : Couche d'accès aux données
- `src/main/java/com/pizzeria/ui` : Interface utilisateur Swing
- `src/main/resources` : Fichiers de configuration

## Support

Pour toute question ou problème, veuillez créer une issue dans le dépôt du projet. 