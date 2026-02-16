# Sprint 2 - Guide de Test

## Objectifs du Sprint 2
- Implémentation du CRUD Véhicule
- Implémentation du système Token avec génération automatique
- Tests des nouvelles fonctionnalités

---

## Prérequis

### 1. Base de données
Exécuter les scripts SQL suivants :

```sql
-- Créer les nouvelles tables
CREATE TABLE Vehicule(
   id SERIAL PRIMARY KEY,
   reference VARCHAR(50) NOT NULL UNIQUE,
   place INTEGER NOT NULL,
   type_carburant CHAR(1) NOT NULL CHECK (type_carburant IN ('D', 'E'))
);

CREATE TABLE Token(
   id SERIAL PRIMARY KEY,
   token VARCHAR(255) NOT NULL UNIQUE,
   date_heure_expiration TIMESTAMP NOT NULL
);

-- Insérer des données de test
INSERT INTO Vehicule (reference, place, type_carburant) VALUES 
    ('VH-001', 4, 'E'),
    ('VH-002', 5, 'D'),
    ('VH-003', 7, 'D'),
    ('VH-004', 2, 'E'),
    ('VH-005', 9, 'D');
```

### 2. Démarrer l'application

```bash
# Si l'application n'est pas démarrée
mvn jetty:run

# L'application sera accessible sur http://localhost:8081/backoffice
```

---

## Tests des API Véhicule

### 1. Lister tous les véhicules
**Méthode :** GET  
**URL :** http://localhost:8081/backoffice/vehicules

**Test navigateur :**
- Ouvrir l'URL dans le navigateur
- Vérifier la réponse JSON avec la liste des véhicules

**Réponse attendue :**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "reference": "VH-001",
      "place": 4,
      "typeCarburant": "E",
      "typeCarburantLibelle": "Essence"
    },
    ...
  ]
}
```

---

### 2. Obtenir un véhicule par ID
**Méthode :** GET  
**URL :** http://localhost:8081/backoffice/vehicules/detail?id=1

**Test navigateur :**
- Ouvrir l'URL dans le navigateur avec différents IDs
- Tester avec un ID inexistant (ex: id=999)

**Réponse attendue (succès) :**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "reference": "VH-001",
    "place": 4,
    "typeCarburant": "E",
    "typeCarburantLibelle": "Essence"
  }
}
```

**Réponse attendue (ID inexistant) :**
```json
{
  "status": "error",
  "code": 404,
  "message": "Véhicule non trouvé"
}
```

---

### 3. Créer un nouveau véhicule
**Méthode :** POST  
**URL :** http://localhost:8081/backoffice/vehicules

**Paramètres :**
- `reference` (string) : Référence unique du véhicule (ex: "VH-010")
- `place` (int) : Nombre de places (ex: 5)
- `type_carburant` (string) : Type de carburant "D" (Diesel) ou "E" (Essence)

**Test avec HTML/JavaScript :**
Créer un fichier `test-vehicule.html` :

```html
<!DOCTYPE html>
<html>
<head>
    <title>Test Véhicule API</title>
</head>
<body>
    <h2>Créer un véhicule</h2>
    <form id="createForm">
        Référence: <input type="text" id="reference" value="VH-010"><br>
        Places: <input type="number" id="place" value="5"><br>
        Carburant: 
        <select id="type_carburant">
            <option value="E">Essence</option>
            <option value="D">Diesel</option>
        </select><br>
        <button type="submit">Créer</button>
    </form>
    <pre id="result"></pre>

    <script>
        document.getElementById('createForm').onsubmit = async (e) => {
            e.preventDefault();
            const formData = new URLSearchParams({
                reference: document.getElementById('reference').value,
                place: document.getElementById('place').value,
                type_carburant: document.getElementById('type_carburant').value
            });

            const response = await fetch('http://localhost:8081/backoffice/vehicules', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            });
            const result = await response.text();
            document.getElementById('result').textContent = result;
        };
    </script>
</body>
</html>
```

**Test avec curl :**
```bash
curl -X POST "http://localhost:8081/backoffice/vehicules" \
  -d "reference=VH-010&place=5&type_carburant=E"
```

**Réponse attendue :**
```json
{
  "status": "success",
  "data": {
    "id": 6,
    "reference": "VH-010",
    "place": 5,
    "typeCarburant": "E"
  }
}
```

**Test avec type_carburant invalide :**
```bash
curl -X POST "http://localhost:8081/backoffice/vehicules" \
  -d "reference=VH-011&place=5&type_carburant=X"
```

**Réponse attendue (erreur) :**
```json
{
  "status": "error",
  "code": 400,
  "message": "Type carburant doit être 'D' (Diesel) ou 'E' (Essence)"
}
```

---

### 4. Modifier un véhicule
**Méthode :** POST  
**URL :** http://localhost:8081/backoffice/vehicules/update

**Paramètres :**
- `id` (int) : ID du véhicule à modifier
- `reference` (string) : Nouvelle référence
- `place` (int) : Nouveau nombre de places
- `type_carburant` (string) : Nouveau type de carburant

**Test avec curl :**
```bash
curl -X POST "http://localhost:8081/backoffice/vehicules/update" \
  -d "id=1&reference=VH-001-MOD&place=6&type_carburant=D"
```

**Test avec fichier HTML :**
```html
<h2>Modifier un véhicule</h2>
<form id="updateForm">
    ID: <input type="number" id="update_id" value="1"><br>
    Référence: <input type="text" id="update_reference" value="VH-001-MOD"><br>
    Places: <input type="number" id="update_place" value="6"><br>
    Carburant: 
    <select id="update_type_carburant">
        <option value="E">Essence</option>
        <option value="D">Diesel</option>
    </select><br>
    <button type="submit">Modifier</button>
</form>
<pre id="updateResult"></pre>

<script>
    document.getElementById('updateForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new URLSearchParams({
            id: document.getElementById('update_id').value,
            reference: document.getElementById('update_reference').value,
            place: document.getElementById('update_place').value,
            type_carburant: document.getElementById('update_type_carburant').value
        });

        const response = await fetch('http://localhost:8081/backoffice/vehicules/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        });
        const result = await response.text();
        document.getElementById('updateResult').textContent = result;
    };
</script>
```

---

### 5. Supprimer un véhicule
**Méthode :** POST  
**URL :** http://localhost:8081/backoffice/vehicules/delete

**Paramètres :**
- `id` (int) : ID du véhicule à supprimer

**Test avec curl :**
```bash
curl -X POST "http://localhost:8081/backoffice/vehicules/delete" \
  -d "id=5"
```

**Test avec fichier HTML :**
```html
<h2>Supprimer un véhicule</h2>
<form id="deleteForm">
    ID: <input type="number" id="delete_id" value="5"><br>
    <button type="submit">Supprimer</button>
</form>
<pre id="deleteResult"></pre>

<script>
    document.getElementById('deleteForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new URLSearchParams({
            id: document.getElementById('delete_id').value
        });

        const response = await fetch('http://localhost:8081/backoffice/vehicules/delete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        });
        const result = await response.text();
        document.getElementById('deleteResult').textContent = result;
    };
</script>
```

**Réponse attendue (succès) :**
```json
{
  "status": "success",
  "data": "Véhicule supprimé avec succès"
}
```

---

## Tests du système Token

### 1. Générer des tokens avec le Main

**Commande :**
```bash
mvn exec:java -Dexec.mainClass="com.hotel.Main"
```

**Résultat attendu dans la console :**
```
=== Gestion des Tokens ===

Tokens expirés supprimés: 0

--- Génération de nouveaux tokens ---
Token 1 créé: a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6
  Expiration: 2026-02-13 12:11:00
Token 2 créé: b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7
  Expiration: 2026-02-13 13:11:00
Token 3 créé: c3d4e5f6-g7h8-i9j0-k1l2-m3n4o5p6q7r8
  Expiration: 2026-02-14 11:11:00

--- Liste de tous les tokens ---
ID: 1 | Token: a1b2c3d4... | Expiration: 2026-02-13 12:11:00 | Status: VALIDE
ID: 2 | Token: b2c3d4e5... | Expiration: 2026-02-13 13:11:00 | Status: VALIDE
ID: 3 | Token: c3d4e5f6... | Expiration: 2026-02-14 11:11:00 | Status: VALIDE

--- Test de validation ---
Token 1 valide: true

=== Fin du programme ===
```

### 2. Vérifier les tokens dans la base de données

```sql
-- Voir tous les tokens
SELECT id, 
       SUBSTRING(token, 1, 12) as token_preview, 
       date_heure_expiration,
       CASE 
         WHEN date_heure_expiration < NOW() THEN 'EXPIRÉ'
         ELSE 'VALIDE'
       END as status
FROM Token 
ORDER BY date_heure_expiration DESC;

-- Compter les tokens valides
SELECT COUNT(*) as tokens_valides 
FROM Token 
WHERE date_heure_expiration >= NOW();

-- Supprimer les tokens expirés
DELETE FROM Token WHERE date_heure_expiration < NOW();
```

---

## Scénarios de test complets

### Scénario 1 : CRUD complet d'un véhicule

1. **Lister** les véhicules initiaux
2. **Créer** un nouveau véhicule "VH-020" avec 8 places, Diesel
3. **Vérifier** qu'il apparaît dans la liste
4. **Récupérer** les détails du véhicule créé
5. **Modifier** le véhicule : changer en 10 places
6. **Vérifier** la modification
7. **Supprimer** le véhicule
8. **Vérifier** qu'il n'apparaît plus dans la liste

### Scénario 2 : Tests d'erreur

1. **Créer** un véhicule avec type_carburant invalide ("X") → Erreur 400
2. **Créer** un véhicule avec une référence déjà existante → Erreur
3. **Modifier** un véhicule avec ID inexistant (999) → Erreur 404
4. **Supprimer** un véhicule avec ID inexistant (999) → Erreur 404
5. **Récupérer** un véhicule avec ID inexistant → Erreur 404

### Scénario 3 : Gestion des tokens

1. **Exécuter** le Main pour générer 3 tokens
2. **Vérifier** dans la base que les tokens sont créés
3. **Attendre** l'expiration d'un token (ou modifier la date manuellement)
4. **Réexécuter** le Main
5. **Vérifier** que les tokens expirés sont supprimés

---

## Fichier de test complet HTML

Créer un fichier `test-api-vehicule.html` à la racine du projet :

```html
<!DOCTYPE html>
<html>
<head>
    <title>Test API Véhicule - Sprint 2</title>
    <style>
        body { font-family: Arial; margin: 20px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }
        input, select { margin: 5px; padding: 5px; }
        button { padding: 8px 15px; background: #007bff; color: white; border: none; cursor: pointer; }
        button:hover { background: #0056b3; }
        pre { background: #f4f4f4; padding: 10px; overflow-x: auto; }
        .success { color: green; }
        .error { color: red; }
    </style>
</head>
<body>
    <h1>Test API Véhicule - Sprint 2</h1>

    <div class="section">
        <h2>1. Lister tous les véhicules (GET)</h2>
        <button onclick="getAllVehicules()">Récupérer la liste</button>
        <pre id="listResult"></pre>
    </div>

    <div class="section">
        <h2>2. Obtenir un véhicule par ID (GET)</h2>
        ID: <input type="number" id="getById" value="1">
        <button onclick="getVehiculeById()">Récupérer</button>
        <pre id="getResult"></pre>
    </div>

    <div class="section">
        <h2>3. Créer un véhicule (POST)</h2>
        Référence: <input type="text" id="createRef" value="VH-100"><br>
        Places: <input type="number" id="createPlace" value="5"><br>
        Carburant: 
        <select id="createType">
            <option value="E">Essence</option>
            <option value="D">Diesel</option>
        </select><br>
        <button onclick="createVehicule()">Créer</button>
        <pre id="createResult"></pre>
    </div>

    <div class="section">
        <h2>4. Modifier un véhicule (POST)</h2>
        ID: <input type="number" id="updateId" value="1"><br>
        Référence: <input type="text" id="updateRef" value="VH-001-MOD"><br>
        Places: <input type="number" id="updatePlace" value="6"><br>
        Carburant: 
        <select id="updateType">
            <option value="E">Essence</option>
            <option value="D">Diesel</option>
        </select><br>
        <button onclick="updateVehicule()">Modifier</button>
        <pre id="updateResult"></pre>
    </div>

    <div class="section">
        <h2>5. Supprimer un véhicule (POST)</h2>
        ID: <input type="number" id="deleteId" value="5">
        <button onclick="deleteVehicule()">Supprimer</button>
        <pre id="deleteResult"></pre>
    </div>

    <script>
        const BASE_URL = 'http://localhost:8081/backoffice';

        async function getAllVehicules() {
            try {
                const response = await fetch(`${BASE_URL}/vehicules`);
                const text = await response.text();
                document.getElementById('listResult').textContent = JSON.stringify(JSON.parse(text), null, 2);
            } catch (error) {
                document.getElementById('listResult').textContent = 'Erreur: ' + error.message;
            }
        }

        async function getVehiculeById() {
            const id = document.getElementById('getById').value;
            try {
                const response = await fetch(`${BASE_URL}/vehicules/detail?id=${id}`);
                const text = await response.text();
                document.getElementById('getResult').textContent = JSON.stringify(JSON.parse(text), null, 2);
            } catch (error) {
                document.getElementById('getResult').textContent = 'Erreur: ' + error.message;
            }
        }

        async function createVehicule() {
            const formData = new URLSearchParams({
                reference: document.getElementById('createRef').value,
                place: document.getElementById('createPlace').value,
                type_carburant: document.getElementById('createType').value
            });

            try {
                const response = await fetch(`${BASE_URL}/vehicules`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const text = await response.text();
                document.getElementById('createResult').textContent = JSON.stringify(JSON.parse(text), null, 2);
            } catch (error) {
                document.getElementById('createResult').textContent = 'Erreur: ' + error.message;
            }
        }

        async function updateVehicule() {
            const formData = new URLSearchParams({
                id: document.getElementById('updateId').value,
                reference: document.getElementById('updateRef').value,
                place: document.getElementById('updatePlace').value,
                type_carburant: document.getElementById('updateType').value
            });

            try {
                const response = await fetch(`${BASE_URL}/vehicules/update`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const text = await response.text();
                document.getElementById('updateResult').textContent = JSON.stringify(JSON.parse(text), null, 2);
            } catch (error) {
                document.getElementById('updateResult').textContent = 'Erreur: ' + error.message;
            }
        }

        async function deleteVehicule() {
            const formData = new URLSearchParams({
                id: document.getElementById('deleteId').value
            });

            try {
                const response = await fetch(`${BASE_URL}/vehicules/delete`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const text = await response.text();
                document.getElementById('deleteResult').textContent = JSON.stringify(JSON.parse(text), null, 2);
            } catch (error) {
                document.getElementById('deleteResult').textContent = 'Erreur: ' + error.message;
            }
        }
    </script>
</body>
</html>
```

---

## Checklist de validation

- [ ] Les tables Vehicule et Token sont créées
- [ ] Les données de test sont insérées
- [ ] L'application démarre sans erreur sur le port 8081
- [ ] GET /vehicules retourne la liste des véhicules
- [ ] GET /vehicules/detail?id=1 retourne un véhicule spécifique
- [ ] POST /vehicules crée un nouveau véhicule
- [ ] POST /vehicules avec type_carburant invalide retourne une erreur 400
- [ ] POST /vehicules/update modifie un véhicule existant
- [ ] POST /vehicules/delete supprime un véhicule
- [ ] Le Main génère des tokens avec UUID
- [ ] Le Main supprime les tokens expirés
- [ ] Les tokens sont correctement stockés en base
- [ ] Les validations fonctionnent (type_carburant = D ou E uniquement)

---

## Notes importantes

1. **Types de carburant** : Seules les valeurs 'D' (Diesel) et 'E' (Essence) sont acceptées
2. **Référence unique** : La référence d'un véhicule doit être unique
3. **Tokens** : Les tokens sont générés avec UUID et ont une date d'expiration
4. **Format des dates** : Les timestamps sont au format PostgreSQL TIMESTAMP

## Liens utiles

- Application : http://localhost:8081/backoffice
- API Véhicules : http://localhost:8081/backoffice/vehicules
- API Hotels : http://localhost:8081/backoffice/hotels
- API Réservations : http://localhost:8081/backoffice/reservations
