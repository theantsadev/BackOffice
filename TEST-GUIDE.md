# Guide de Test - BackOffice Réservations

## ✅ Corrections effectuées pour le JSON

### Problème initial
L'erreur "Unexpected token '<', "<!doctype "... is not valid JSON" indiquait que le serveur renvoyait du HTML au lieu de JSON.

### Solution appliquée
1. **Création de JsonUtil** : Classe utilitaire pour sérialiser/désérialiser avec Gson
2. **Modification des contrôleurs REST** : Retour de String JSON au lieu de ModelView
3. **Annotation @Json** : Conservée pour indiquer au framework le type de contenu

## 🧪 Tests à effectuer

### 1. Test de l'API Hotels
```powershell
# Dans PowerShell
curl http://localhost:8080/backoffice/hotels
```

**Résultat attendu** : JSON avec la liste des hôtels
```json
{
  "status": "success",
  "data": [
    {"id_hotel": 1, "nom": "Hôtel Colbert"},
    ...
  ]
}
```

### 2. Test de création de réservation (via l'interface web)

1. Accéder à `http://localhost:8080/backoffice/pages/`
2. Cliquer sur "Nouvelle Réservation"
3. Remplir le formulaire :
   - **ID Client** : 1234 (4 chiffres)
   - **Nombre de passagers** : 2
   - **Date et heure d'arrivée** : 2026-02-15 14:30
   - **Hôtel** : Sélectionner un hôtel dans la liste
4. Cliquer sur "Réserver"

**Résultat attendu** : Message de succès + redirection vers la liste

### 3. Vérifier dans la liste des réservations

1. Accéder à `http://localhost:8080/backoffice/pages/liste-reservations`
2. Vérifier que la réservation apparaît dans le tableau

### 4. Vérifier dans la base de données

```powershell
# Se connecter à PostgreSQL
psql -U postgres -d projet_hotel

# Exécuter le script de test
\i test-data.sql

# OU manuellement
SELECT * FROM Reservation ORDER BY date_heure_arrivee DESC;
```

### 5. Test de l'API Réservations

```powershell
# Toutes les réservations
curl http://localhost:8080/backoffice/reservations

# Filtrer par date
curl "http://localhost:8080/backoffice/reservations?date=2026-02-15"
```

### 6. Test POST via curl (optionnel)

```powershell
curl -X POST http://localhost:8080/backoffice/reservations `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "id_client=5678&nb_passager=3&date_heure_arrivee=2026-02-20T10:00&id_hotel=2"
```

## 📊 Vérification des données

### Requêtes SQL utiles

```sql
-- Voir toutes les réservations avec les noms d'hôtels
SELECT 
    r.id_reservation,
    r.id_client,
    r.nb_passager,
    r.date_heure_arrivee,
    h.nom as hotel
FROM Reservation r
LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel
ORDER BY r.date_heure_arrivee DESC;

-- Compter les réservations par hôtel
SELECT 
    h.nom,
    COUNT(r.id_reservation) as nombre_reservations
FROM Hotel h
LEFT JOIN Reservation r ON h.id_hotel = r.id_hotel
GROUP BY h.id_hotel, h.nom
ORDER BY nombre_reservations DESC;

-- Supprimer toutes les réservations (si besoin de reset)
DELETE FROM Reservation;

-- Reset de la séquence
ALTER SEQUENCE reservation_id_reservation_seq RESTART WITH 1;
```

## 🐛 Dépannage

### Erreur persiste : "is not valid JSON"

1. Vérifier que Tomcat a bien redéployé :
   ```powershell
   # Voir la date du fichier WAR
   Get-Item "E:\Etude\Etude\Outils\TOMCAT\apache-tomcat-10.1.28\webapps\backoffice.war"
   
   # Voir les logs Tomcat
   Get-Content "E:\Etude\Etude\Outils\TOMCAT\apache-tomcat-10.1.28\logs\catalina.out" -Tail 50
   ```

2. Vider le cache du navigateur (Ctrl+Shift+R)

3. Redémarrer Tomcat :
   ```powershell
   # Arrêter
   cd "E:\Etude\Etude\Outils\TOMCAT\apache-tomcat-10.1.28\bin"
   .\shutdown.bat
   
   # Attendre 5 secondes
   Start-Sleep -Seconds 5
   
   # Démarrer
   .\startup.bat
   ```

### Erreur de connexion à la base de données

Vérifier les paramètres dans `src/main/resources/database.properties` :
```properties
db.url=jdbc:postgresql://localhost:5432/projet_hotel
db.user=postgres
db.password=bolo1925
```

### Les données n'apparaissent pas dans la liste

1. Ouvrir la console du navigateur (F12)
2. Vérifier les erreurs JavaScript
3. Tester l'API directement :
   ```
   http://localhost:8080/backoffice/reservations
   ```

## ✅ Checklist de validation

- [ ] L'API `/hotels` retourne du JSON valide
- [ ] L'API `/reservations` retourne du JSON valide
- [ ] Le formulaire de réservation fonctionne sans erreur
- [ ] La réservation apparaît dans la liste
- [ ] La réservation est visible dans la base de données
- [ ] Les filtres par date fonctionnent
- [ ] Les liens de navigation fonctionnent tous
- [ ] Aucune erreur dans la console du navigateur
- [ ] Aucune erreur dans les logs Tomcat

## 📝 Structure des réponses JSON

### Succès
```json
{
  "status": "success",
  "code": null,
  "data": { ... },
  "error": null
}
```

### Erreur
```json
{
  "status": "error",
  "code": 500,
  "data": null,
  "error": {
    "code": 500,
    "message": "Description de l'erreur"
  }
}
```

## 🎯 Objectif final

- ✅ Créer des réservations via le formulaire web
- ✅ Voir les réservations dans la liste
- ✅ Filtrer les réservations par date
- ✅ Vérifier les données dans PostgreSQL
- ✅ APIs REST fonctionnelles (GET, POST)
