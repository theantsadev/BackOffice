# Hotel BackOffice - Système de Gestion des Réservations

Application de gestion des réservations hôtelières développée avec un framework personnalisé et PostgreSQL.

## Prérequis

- Java JDK 8 ou supérieur
- Apache Tomcat 9 ou supérieur
- PostgreSQL 12 ou supérieur
- Maven 3.6 ou supérieur

## Installation

### 1. Configuration de la base de données

```bash
# Créer la base de données
createdb hotel_db

# Exécuter le script de création des tables
psql -U postgres -d hotel_db -f base.sql

# Insérer les données initiales des hôtels
psql -U postgres -d hotel_db -f insert-hotels.sql
```

### 2. Configuration de l'application

Modifier le fichier `src/main/resources/database.properties` avec vos paramètres de connexion :

```properties
db.url=jdbc:postgresql://localhost:5432/hotel_db
db.user=postgres
db.password=votre_mot_de_passe
```

### 3. Compilation et déploiement

```bash
# Compiler le projet avec Maven
mvn clean package

# Le fichier WAR sera généré dans target/backoffice.war
# Copier ce fichier dans le dossier webapps de Tomcat
cp target/backoffice.war $TOMCAT_HOME/webapps/

# Démarrer Tomcat
$TOMCAT_HOME/bin/startup.sh  # Linux/Mac
$TOMCAT_HOME/bin/startup.bat  # Windows
```

### 4. Accès à l'application

Ouvrir dans un navigateur : `http://localhost:8080/backoffice/`

## Fonctionnalités

### Pages Web

- **Page d'accueil** (`/`) : Menu principal avec accès aux fonctionnalités
- **Formulaire de réservation** (`/formulaire-reservation`) : Création de nouvelles réservations
- **Liste des réservations** (`/liste-reservations`) : Consultation et filtrage des réservations

### API REST

#### Hôtels

- `GET /hotels` : Récupérer la liste de tous les hôtels

#### Réservations

- `GET /reservations` : Récupérer toutes les réservations
- `GET /reservations?date=YYYY-MM-DD` : Filtrer les réservations par date
- `POST /reservations` : Créer une nouvelle réservation

**Paramètres POST :**
- `id_client` : Identifiant client (4 chiffres)
- `nb_passager` : Nombre de passagers
- `date_heure_arrivee` : Date et heure d'arrivée (format: YYYY-MM-DDTHH:mm)
- `id_hotel` : ID de l'hôtel

## Structure du projet

```
BackOffice/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hotel/
│   │   │           ├── controller/       # Contrôleurs REST et pages
│   │   │           ├── model/            # Modèles de données
│   │   │           ├── service/          # Logique métier
│   │   │           └── database/         # Connexion à la base de données
│   │   ├── resources/
│   │   │   └── database.properties       # Configuration base de données
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml              # Configuration web
│   │       ├── index.jsp                # Page d'accueil
│   │       ├── formulaire-reservation.jsp
│   │       ├── liste-reservations.jsp
│   │       └── error.jsp
├── lib/
│   └── framework.jar                     # Framework personnalisé
├── base.sql                              # Script création tables
├── insert-hotels.sql                     # Script insertion hôtels
└── pom.xml                              # Configuration Maven
```

## Technologies utilisées

- **Backend** : Java 8, Servlets, JSP
- **Frontend** : HTML5, CSS3, Bootstrap 5, JavaScript
- **Base de données** : PostgreSQL
- **Build** : Maven
- **Serveur** : Apache Tomcat
- **Framework** : Framework personnalisé (framework.jar)

## Caractéristiques de l'interface

- Design moderne et professionnel
- Interface responsive (compatible mobile et desktop)
- Formulaires avec validation côté client et serveur
- Messages d'erreur et de succès clairs
- Filtrage dynamique des réservations
- Chargement asynchrone des données (AJAX)

## Notes importantes

- L'application ne nécessite pas d'authentification (sans protection)
- L'ID client est une saisie libre de 4 chiffres
- Les dates sont au format ISO pour assurer la compatibilité
- Toutes les heures sont gérées avec le fuseau horaire du serveur

## Dépannage

### Erreur de connexion à la base de données

Vérifier que :
- PostgreSQL est démarré
- Les paramètres de connexion dans `database.properties` sont corrects
- L'utilisateur a les droits sur la base de données

### Erreur 404

Vérifier que :
- Le fichier WAR est bien déployé dans Tomcat
- L'URL contient le nom de contexte correct : `/backoffice/`
- Tomcat est démarré

### Erreur de compilation

Vérifier que :
- Le fichier `framework.jar` est présent dans le dossier `lib/`
- Maven peut accéder au fichier JAR
- Java JDK est correctement configuré

## Support

Pour toute question ou problème, consulter la documentation du framework ou contacter le support technique.

## accès à l'application
-> # http://localhost:8080/backoffice/pages/
