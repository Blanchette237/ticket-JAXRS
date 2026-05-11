# TicketApp — Gestion de tickets de concerts

Application full-stack de réservation de tickets de concerts, composée d'une **API REST Java JAX-RS** (backend) et d'une **application Angular** (frontend).

---

## Architecture du projet

```
ticket-JAXRS/
├── src/                        ← Backend Java (JAX-RS)
│   └── main/java/fr/istic/taa/
│       ├── jaxr/
│       │   ├── dto/            ← Objets de transfert (entrée API)
│       │   └── services/       ← Logique métier
│       └── jaxrs/
│           ├── dao/            ← Accès base de données (JPA/Hibernate)
│           ├── domain/         ← Entités JPA (Concert, Ticket, Client…)
│           └── rest/           ← Ressources REST exposées
├── ticket-front/               ← Frontend Angular 17
│   └── src/app/
│       ├── models/             ← Interfaces TypeScript
│       ├── services/           ← Appels HTTP vers le backend
│       └── components/         ← Pages et composants UI
└── pom.xml                     ← Configuration Maven
```

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 11, JAX-RS (RESTEasy 6.2), Hibernate 6.2, Undertow |
| Frontend | Angular 17 (standalone components), TypeScript, SCSS |
| Base de données | MySQL 8 |
| Documentation API | OpenAPI 3 + Swagger UI |
| Tests | JUnit 5 + Mockito 5 |

---

## Prérequis

- **Java 11+** et **Maven 3.6+**
- **Node.js 18+** et **npm 9+**
- **MySQL 8** en cours d'exécution
- **Angular CLI 17** : `npm install -g @angular/cli@17`

---

## 1. Démarrer la base de données

### Créer la base MySQL

```sql
CREATE DATABASE ticketdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ticketuser'@'localhost' IDENTIFIED BY 'ticketpass';
GRANT ALL PRIVILEGES ON ticketdb.* TO 'ticketuser'@'localhost';
FLUSH PRIVILEGES;
```

### Configuration dans `persistence.xml`

Le fichier `src/main/resources/META-INF/persistence.xml` contient la connexion :

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/ticketdb"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value=""/>
```

> Modifiez les identifiants selon votre configuration MySQL.

Hibernate crée automatiquement les tables au premier démarrage (`hbm2ddl.auto=update`).

---

## 2. Démarrer le backend

### Depuis le terminal

```bash
# Compiler
mvn compile

# Lancer le serveur
mvn exec:java -Dexec.mainClass="fr.istic.taa.jaxrs.RestServer"
```

### Depuis IntelliJ / Eclipse

Clic droit sur `RestServer.java` → **Run 'RestServer.main()'**

Le serveur démarre sur **http://localhost:8080**.

---

## 3. Accéder à Swagger UI

Une fois le backend démarré :

| URL | Description |
|---|---|
| `http://localhost:8080/api/` | **Swagger UI** — interface graphique |
| `http://localhost:8080/openapi.json` | Schéma OpenAPI au format JSON |

Swagger UI liste tous les endpoints disponibles et permet de les tester directement depuis le navigateur sans aucun outil externe.

---

## 4. Démarrer le frontend Angular

```bash
# Aller dans le dossier frontend
cd ticket-front

# Installer les dépendances (première fois uniquement)
npm install

# Lancer le serveur de développement
ng serve
```

L'application est accessible sur **http://localhost:4200**.

> Le backend doit être démarré avant le frontend pour que les données s'affichent.

---

## Comment le frontend se connecte au backend

### Le lien clé : `environment.ts`

```typescript
// ticket-front/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'   ← URL du backend
};
```

Toutes les URLs d'appel API sont construites à partir de cette variable. Pour changer l'adresse du backend (déploiement, Docker, etc.), il suffit de modifier ce seul fichier.

### Services Angular → API REST

Chaque service Angular injecte `HttpClient` et appelle les endpoints :

```
Angular ConcertService          →    Backend /concerts
  getAll()                      →    GET  /concerts
  getById(id)                   →    GET  /concerts/{id}
  create(dto)                   →    POST /concerts
  delete(id)                    →    DELETE /concerts/{id}

Angular TicketService           →    Backend /tickets
  getAll()                      →    GET  /tickets
  create(dto)                   →    POST /tickets
  annuler(id)                   →    DELETE /tickets/{id}/annuler
```

### CORS (pourquoi ça fonctionne)

Sans configuration CORS, le navigateur bloquerait les requêtes Angular (port 4200) vers le backend (port 8080) car ils sont sur des ports différents. Le `CorsFilter` ajouté côté backend autorise ces échanges :

```java
// src/main/java/fr/istic/taa/jaxrs/rest/CorsFilter.java
response.getHeaders().add("Access-Control-Allow-Origin", "*");
response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
```

---

## API REST — Endpoints disponibles

### Concerts

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/concerts` | Liste de tous les concerts |
| `GET` | `/concerts/{id}` | Détail d'un concert |
| `POST` | `/concerts` | Créer un concert |
| `DELETE` | `/concerts/{id}` | Supprimer un concert |

**Corps POST /concerts :**
```json
{
  "organiserId": 1,
  "lieu": "Zénith Paris",
  "description": "Grand concert de rock",
  "dateTime": "2026-06-15T20:00:00",
  "capacite": 500,
  "popularite": 4
}
```

### Tickets

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/tickets` | Liste de tous les tickets |
| `GET` | `/tickets/{id}` | Détail d'un ticket |
| `POST` | `/tickets` | Acheter un ticket |
| `DELETE` | `/tickets/{id}/annuler` | Annuler un ticket |

**Corps POST /tickets :**
```json
{
  "utilisateurId": 1,
  "concertId": 10,
  "numeroPlace": "VIP3"
}
```

---

## Logique métier implémentée

### Pricing dynamique des tickets

Le prix est calculé automatiquement selon plusieurs critères :

| Critère | Règle | Impact |
|---|---|---|
| Zone de la place | VIP | +50€ |
| | Zone A | +20€ |
| | Zone B | +10€ |
| | Autres | +5€ |
| Numéro de place | Places 1-10 | +20€ |
| | Places 11-30 | +10€ |
| Popularité | Par étoile (1 à 5) | +5€ par étoile |
| Surge pricing | Moins de 20% de places restantes | +15€ |
| Prix de base | — | 30€ |

**Exemple :** Place VIP1, concert popularité 4 → 30 + 50 + 20 + 20 = **120€**

### Validation des réservations

L'API vérifie automatiquement :
- Le client existe en base
- Le concert existe et n'est pas passé
- Des places sont encore disponibles (`capacite > 0`)
- La place spécifique n'est pas déjà réservée

### Gestion des places

- À chaque achat : `concert.capacite - 1`
- À chaque annulation : `concert.capacite + 1`
- `capaciteMax` reste immuable (capacité totale initiale)

### Statuts de ticket

| Statut | Description |
|---|---|
| `ACTIVE` | Ticket valide, peut être annulé |
| `ANNULE` | Ticket annulé (place restituée au concert) |
| `UTILISE` | Ticket utilisé à l'entrée (non annulable) |

---

## Fonctionnalités du frontend Angular

### Page Concerts (`/concerts`)
- Affiche la liste de tous les concerts à venir
- Barre de progression du taux de remplissage (rouge si < 20% restant)
- Badge "COMPLET" si plus de places disponibles
- Étoiles de popularité
- Bouton "Réserver" désactivé si complet

### Page Détail Concert (`/concerts/:id`)
- Informations complètes du concert
- Formulaire d'achat intégré :
  - Saisie de l'ID client et du numéro de place
  - Confirmation de réservation en temps réel
  - Message de succès ou d'erreur

### Page Mes Tickets (`/mes-tickets`)
- Chargement de tous les tickets
- Statistiques (actifs / annulés)
- Bouton "Annuler" sur chaque ticket actif avec confirmation
- Mise à jour du statut en temps réel sans rechargement

---

## Lancer les tests unitaires (backend)

```bash
mvn test
```

24 tests unitaires couvrent la logique métier des services :

| Classe testée | Nb tests | Ce qui est testé |
|---|---|---|
| `TicketService` | 13 | validations, pricing VIP/A/B, surge, annulation |
| `ConcertService` | 11 | validations, initialisation capacité, suppression |

Les DAOs sont mockés avec Mockito — aucune base de données nécessaire pour les tests.

---

## Structure des données (modèle JPA)

```
User (abstract, TABLE_PER_CLASS)
├── Client  ─── ticketsAchetes ──→ Ticket (1-N)
└── Organiser ── concerts ───────→ Concert (1-N)

Concert ──── ticketsVendus ──────→ Ticket (1-N)
Ticket  ──── client ─────────────→ Client (N-1)
Ticket  ──── concert ────────────→ Concert (N-1)
```

---

## Déploiement en production

Pour pointer le frontend vers un backend distant, modifiez :

```typescript
// ticket-front/src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://votre-api.example.com'
};
```

Puis buildez :
```bash
ng build --configuration=production
```

Les fichiers du dossier `dist/ticket-front/` peuvent être déployés sur n'importe quel serveur web statique (Nginx, Apache, Netlify, etc.).
