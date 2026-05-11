# TicketApp — Plateforme de billetterie de concerts

> **Application full-stack de gestion et de réservation de tickets de concerts.**
> Développée en Java JAX-RS (backend) et Angular 17 (frontend), connectée à une base MySQL.

---

## Démonstration rapide

```
Terminal 1 — Démarrer le backend  →  mvn exec:java -Dexec.mainClass="fr.istic.taa.jaxrs.RestServer"
Terminal 2 — Démarrer le frontend →  cd ticket-front && ng serve
Navigateur                         →  http://localhost:4200
```

---

## Ce que fait l'application

TicketApp met en relation deux types d'utilisateurs :

| Rôle | Ce qu'il peut faire |
|---|---|
| **Organisateur** | Créer un compte · Créer des concerts avec date, lieu, capacité et popularité · Suivre le taux de remplissage en temps réel · Supprimer un concert |
| **Client** | Créer un compte · Parcourir les concerts disponibles · Réserver un ticket (prix calculé automatiquement) · Consulter et annuler ses tickets |

### Parcours utilisateur complet

```
1. L'organisateur s'inscrit → obtient un ID
2. Il crée un concert (lieu, date, capacité, popularité)
3. Le client s'inscrit → obtient un ID
4. Il parcourt les concerts disponibles
5. Il choisit une place (ex: VIP1, A12, B34) → prix calculé dynamiquement
6. Il reçoit son ticket (statut ACTIVE)
7. Il peut annuler → la place est automatiquement restituée
```

---

## Fonctionnalités métier

### Tarification dynamique

Le prix de chaque ticket est calculé en temps réel selon 4 critères cumulables :

| Critère | Détail | Impact |
|---|---|---|
| **Zone** | VIP | +50 € |
| | Zone A | +20 € |
| | Zone B | +10 € |
| | Autres zones | +5 € |
| **Position** | Places 1 à 10 (devant la scène) | +20 € |
| | Places 11 à 30 | +10 € |
| **Popularité** | Note de 1 à 5 étoiles | +5 € par étoile |
| **Surge pricing** | Moins de 20 % de places restantes | +15 € |
| **Base** | Prix plancher | 30 € |

> **Exemple :** Place `VIP3` pour un concert 4 étoiles = 30 + 50 + 20 + 20 = **120 €**

### Gestion de la capacité

- `capaciteMax` : nombre total de places défini à la création — **ne change jamais**
- `capacite` : places encore disponibles — décrémentée à chaque achat, restaurée à chaque annulation
- Dès que `capacite = 0`, le concert affiche "COMPLET" et les réservations sont bloquées

### Cycle de vie d'un ticket

```
[Achat] → ACTIVE → [Annulation] → ANNULE  (place restituée au concert)
                 → [Utilisation]→ UTILISE (non annulable)
```

### Validations automatiques côté serveur

Chaque demande de réservation est vérifiée avant traitement :
- Le client existe bien en base de données
- Le concert existe et sa date est dans le futur
- Il reste au moins une place disponible
- La place demandée n'est pas déjà prise

---

## Architecture technique

```
┌─────────────────────────────────────────────────────────┐
│                    NAVIGATEUR                           │
│              Angular 17 — localhost:4200                │
│                                                         │
│  /inscription   /concerts   /concerts/:id   /mes-tickets│
│  /organisateur                                          │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP + JSON (CORS autorisé)
┌──────────────────────▼──────────────────────────────────┐
│                 API REST — localhost:8080                │
│              Java 11 · JAX-RS (RESTEasy) · Undertow     │
│                                                         │
│  /clients  /organisateurs  /concerts  /tickets          │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │          Couche Service (logique métier)         │   │
│  │  Pricing · Validations · Gestion des places     │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │           Couche DAO (accès données)             │   │
│  │        Hibernate 6 · JPA · AbstractJpaDao        │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────────────┐
│                   MySQL 8 — port 3306                   │
│         Tables : client · oganiser · concert · ticket   │
└─────────────────────────────────────────────────────────┘
```

### Stack technique

| Couche | Technologie | Rôle |
|---|---|---|
| Frontend | Angular 17, TypeScript, SCSS | Interface utilisateur |
| Backend | Java 11, JAX-RS (RESTEasy 6.2) | API REST |
| Serveur embarqué | Undertow | Pas de Tomcat/WildFly à installer |
| ORM | Hibernate 6.2 + JPA | Mapping objet-base de données |
| Base de données | MySQL 8 | Persistance des données |
| Documentation API | OpenAPI 3 + Swagger UI | `/api/` |
| Tests | JUnit 5 + Mockito 5 | 24 tests unitaires |

### Modèle de données

```
User (classe abstraite — TABLE_PER_CLASS)
├── Client   ────────────────→ Ticket (1 client → N tickets)
└── Organiser ───────────────→ Concert (1 organisateur → N concerts)

Concert ─────────────────────→ Ticket (1 concert → N tickets)
Ticket ──────────────────────→ Client  (N-1)
Ticket ──────────────────────→ Concert (N-1)
```

> `TABLE_PER_CLASS` signifie que `Client` et `Organiser` ont chacun leur propre table SQL,
> avec tous les champs de `User` dupliqués — pas de jointure nécessaire pour les récupérer.

---

## Comment frontend et backend communiquent

### 1. Point de connexion unique : `environment.ts`

```typescript
// ticket-front/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'  // ← changer cette ligne pour un déploiement
};
```

Toutes les URLs d'appel API sont construites à partir de cette variable.
Pour pointer vers un serveur distant, **un seul fichier à modifier**.

### 2. Services Angular → Endpoints REST

```
Angular ConcertService.getAll()        →  GET  /concerts
Angular ConcertService.create(dto)     →  POST /concerts
Angular TicketService.create(dto)      →  POST /tickets
Angular TicketService.annuler(id)      →  DELETE /tickets/{id}/annuler
Angular ClientService.getTickets(id)   →  GET  /clients/{id}/tickets
```

### 3. Gestion CORS

Le navigateur interdit par défaut les requêtes cross-port (4200 → 8080).
Le `CorsFilter` côté backend lève cette restriction en ajoutant les en-têtes HTTP appropriés.
Sans lui, l'application Angular ne pourrait pas appeler l'API.

### 4. Session utilisateur (localStorage)

La session est stockée dans le `localStorage` du navigateur via un `BehaviorSubject` RxJS.
Elle survit aux rechargements de page. La navbar se met à jour instantanément à la connexion
et à la déconnexion sans rechargement.

---

## API REST — Référence complète

### Clients — `/clients`

| Méthode | Endpoint | Description | Corps |
|---|---|---|---|
| `POST` | `/clients` | Créer un client | `{"name","firstname","email","password"}` |
| `GET` | `/clients` | Lister tous les clients | — |
| `GET` | `/clients/{id}` | Détail d'un client | — |
| `GET` | `/clients/{id}/tickets` | Tickets d'un client | — |

### Organisateurs — `/organisateurs`

| Méthode | Endpoint | Description | Corps |
|---|---|---|---|
| `POST` | `/organisateurs` | Créer un organisateur | `{"name","firstname","email","password"}` |
| `GET` | `/organisateurs` | Lister tous les organisateurs | — |
| `GET` | `/organisateurs/{id}` | Détail d'un organisateur | — |

### Concerts — `/concerts`

| Méthode | Endpoint | Description | Corps |
|---|---|---|---|
| `GET` | `/concerts` | Lister tous les concerts | — |
| `GET` | `/concerts/{id}` | Détail d'un concert | — |
| `POST` | `/concerts` | Créer un concert | voir ci-dessous |
| `DELETE` | `/concerts/{id}` | Supprimer un concert | — |

```json
POST /concerts
{
  "organiserId": 1,
  "lieu": "Zénith Paris",
  "description": "Grand concert de rock",
  "dateTime": "2026-06-15T20:00:00",
  "capacite": 500,
  "popularite": 4
}
```

### Tickets — `/tickets`

| Méthode | Endpoint | Description | Corps |
|---|---|---|---|
| `GET` | `/tickets` | Lister tous les tickets | — |
| `GET` | `/tickets/{id}` | Détail d'un ticket | — |
| `POST` | `/tickets` | Acheter un ticket | voir ci-dessous |
| `DELETE` | `/tickets/{id}/annuler` | Annuler un ticket | — |

```json
POST /tickets
{
  "utilisateurId": 1,
  "concertId": 10,
  "numeroPlace": "VIP3"
}
```

---

## Installation et démarrage

### Prérequis

- **Java 11+** et **Maven 3.6+**
- **Node.js 18+** et **npm 9+**
- **MySQL 8** démarré en local
- **Angular CLI 17** : `npm install -g @angular/cli@17`

### Étape 1 — Préparer la base de données MySQL

```sql
-- Créer la base et l'utilisateur
CREATE DATABASE ticketdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ticketuser'@'localhost' IDENTIFIED BY 'ticketpass';
GRANT ALL PRIVILEGES ON ticketdb.* TO 'ticketuser'@'localhost';
FLUSH PRIVILEGES;
```

Puis vérifier la connexion dans `src/main/resources/META-INF/persistence.xml` :
```xml
<property name="jakarta.persistence.jdbc.url"      value="jdbc:mysql://localhost:3306/ticketdb"/>
<property name="jakarta.persistence.jdbc.user"     value="root"/>
<property name="jakarta.persistence.jdbc.password" value=""/>
```
> Hibernate crée les tables automatiquement au premier démarrage (`hbm2ddl.auto=update`).
> Aucun script SQL à exécuter manuellement.

### Étape 2 — Démarrer le backend

```bash
# Option A — ligne de commande
mvn exec:java -Dexec.mainClass="fr.istic.taa.jaxrs.RestServer"

# Option B — IDE (IntelliJ / Eclipse)
# Clic droit sur RestServer.java → Run 'RestServer.main()'
```

Le serveur est prêt quand vous voyez : `JAX-RS based micro-service running!`

| URL | Accès |
|---|---|
| `http://localhost:8080/api/` | **Swagger UI** — tester l'API visuellement |
| `http://localhost:8080/openapi.json` | Schéma OpenAPI brut |
| `http://localhost:8080/concerts` | Premier endpoint à tester |

### Étape 3 — Démarrer le frontend

```bash
cd ticket-front
npm install          # première fois uniquement
ng serve
```

Ouvrir **http://localhost:4200** dans le navigateur.

### Étape 4 — Premier scénario de test

```
1. Aller sur http://localhost:4200/inscription
2. Choisir "Organisateur" → remplir le formulaire → valider
   → vous êtes redirigé sur le dashboard organisateur
3. Créer un concert (ex: Zénith Paris, 100 places, popularité 4, date future)
4. Cliquer "Se déconnecter" dans la navbar
5. Retourner sur /inscription → choisir "Client" → créer un compte
6. Aller sur /concerts → cliquer sur le concert créé
7. Entrer une place (ex: VIP1) → Confirmer la réservation
8. Aller sur /mes-tickets → voir le ticket acheté avec son prix calculé
9. Cliquer "Annuler" → le ticket passe à ANNULE, la place est restituée
```

---

## Swagger UI — Tester l'API sans code

Swagger UI est disponible sur **http://localhost:8080/api/** dès que le backend est démarré.

Il permet de :
- Visualiser tous les endpoints disponibles organisés par ressource
- Tester chaque endpoint directement depuis le navigateur (pas besoin de Postman)
- Voir les formats JSON attendus en entrée et retournés en sortie
- Comprendre les codes de réponse possibles (200, 201, 400, 404, 409…)

**Ordre conseillé pour tester dans Swagger :**
1. `POST /organisateurs` → noter l'ID retourné
2. `POST /concerts` → utiliser l'ID organisateur
3. `POST /clients` → noter l'ID retourné
4. `POST /tickets` → utiliser les IDs client et concert
5. `GET /clients/{id}/tickets` → voir le ticket créé
6. `DELETE /tickets/{id}/annuler` → annuler

---

## Tests unitaires

```bash
mvn test
# → 24 tests, 0 échec
```

Les tests couvrent la logique métier des services, sans base de données
(les DAOs sont remplacés par des mocks Mockito) :

| Classe | Tests | Scénarios couverts |
|---|---|---|
| `TicketService` | 13 | client inexistant · concert inexistant · concert passé · concert complet · place déjà prise · pricing VIP/A/B · surge pricing · annulation OK · annulation d'un ticket déjà annulé |
| `ConcertService` | 11 | organisateur inexistant · capacité nulle ou négative · date passée · création OK · popularité nulle · suppression OK |

---

## Structure du projet

```
ticket-JAXRS/
│
├── src/main/java/fr/istic/taa/
│   ├── jaxr/
│   │   ├── dto/                    ← Objets reçus par l'API (validation des entrées)
│   │   │   ├── ClientCreateDTO.java
│   │   │   ├── ConcertCreateDTO.java
│   │   │   ├── OrganiserCreateDTO.java
│   │   │   └── TicketCreateDTO.java
│   │   └── services/               ← Logique métier pure (sans HTTP)
│   │       ├── ClientService.java
│   │       ├── ConcertService.java
│   │       ├── OrganiserService.java
│   │       ├── TicketService.java  ← Pricing + validations
│   │       └── ConflictException.java
│   └── jaxrs/
│       ├── dao/generic/            ← Accès base de données (pattern DAO générique)
│       │   ├── IGenericDao.java
│       │   ├── AbstractJpaDao.java ← CRUD générique réutilisable
│       │   ├── EntityManagerHelper.java
│       │   ├── ClientDao.java
│       │   ├── ConcertDao.java
│       │   ├── OrganisateurDao.java
│       │   └── TicketDao.java
│       ├── domain/                 ← Entités JPA (miroir des tables SQL)
│       │   ├── User.java           ← Classe parente abstraite
│       │   ├── Client.java
│       │   ├── Organiser.java
│       │   ├── Concert.java
│       │   ├── Ticket.java
│       │   └── TicketStatus.java   ← Enum ACTIVE / ANNULE / UTILISE
│       ├── rest/                   ← Points d'entrée HTTP
│       │   ├── ClientResource.java
│       │   ├── OrganisateurResource.java
│       │   ├── ConcertRessource.java
│       │   ├── TicketResource.java
│       │   ├── CorsFilter.java     ← Autorise les requêtes Angular
│       │   └── config.java         ← Métadonnées Swagger
│       ├── RestServer.java         ← Point d'entrée — démarre le serveur
│       └── TestApplication.java    ← Enregistrement de toutes les ressources
│
├── src/test/                       ← Tests unitaires JUnit 5 + Mockito
│
├── ticket-front/                   ← Application Angular 17
│   └── src/app/
│       ├── models/                 ← Interfaces TypeScript (Concert, Ticket, User)
│       ├── services/               ← Appels HTTP + gestion de session
│       │   ├── concert.service.ts
│       │   ├── ticket.service.ts
│       │   ├── client.service.ts
│       │   ├── organiser.service.ts
│       │   └── session.service.ts  ← Stockage session localStorage
│       ├── components/
│       │   ├── navbar/             ← Barre de navigation contextuelle
│       │   ├── inscription/        ← Formulaire client ou organisateur
│       │   ├── concert-list/       ← Liste des concerts (/concerts)
│       │   ├── concert-detail/     ← Détail + réservation (/concerts/:id)
│       │   ├── mes-tickets/        ← Tickets du client (/mes-tickets)
│       │   └── organisateur-dashboard/ ← Gestion concerts (/organisateur)
│       └── environments/
│           └── environment.ts      ← URL du backend (à modifier pour prod)
│
└── pom.xml                         ← Dépendances Maven
```

---

## Déploiement en production

### Modifier l'URL du backend

```typescript
// ticket-front/src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://votre-api.example.com'  // ← URL du serveur de production
};
```

### Construire le frontend

```bash
cd ticket-front
ng build --configuration=production
# → génère dist/ticket-front/ (fichiers statiques)
```

Ces fichiers peuvent être déployés sur **Nginx, Apache, Netlify, Vercel**, etc.

### Modifier la connexion base de données

```xml
<!-- src/main/resources/META-INF/persistence.xml -->
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://votre-serveur:3306/ticketdb"/>
```

---

## Évolutions prévues

Les fonctionnalités suivantes ont été identifiées et sont prêtes à être développées :

| Fonctionnalité | Valeur métier |
|---|---|
| Authentification JWT | Sécurisation des endpoints (actuellement ouverts) |
| Réservation temporaire | Bloquer une place 15 min le temps du paiement |
| Paiement en ligne | Intégration Stripe ou PayPal |
| QR Code sur ticket | Contrôle d'accès à l'entrée du concert |
| Remboursement partiel | Annulation à moins de 24h = 50 % remboursé |
| Notifications email | Confirmation de réservation / annulation |
| Recherche de concerts | Filtrer par lieu, date, popularité |
| Dashboard analytics | Revenus et taux de remplissage pour l'organisateur |
