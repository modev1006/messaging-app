# 💬 Messagerie Instantanée - Guide de démarrage

## Prérequis
- Java 17+
- Maven
- MySQL (en cours d'exécution)
- IntelliJ IDEA ou Eclipse

---

## 1. Configuration de la base de données

```sql
CREATE DATABASE messagerie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Ensuite dans `resources/hibernate.cfg.xml`, mettez à jour :
```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">VOTRE_MOT_DE_PASSE</property>
```

Les tables (users, messages) seront créées automatiquement par Hibernate au premier lancement.

---

## 2. Lancement du Serveur

```bash
mvn compile
mvn exec:java -Dexec.mainClass="server.Server"
```

Ou dans IntelliJ : Run → `server.Server`

Vous devriez voir :
```
[2026-02-22 10:00:00] Démarrage du serveur sur le port 8080...
[2026-02-22 10:00:00] Serveur prêt. En attente de connexions...
```

---

## 3. Lancement du Client (JavaFX)

```bash
mvn javafx:run
```

Ou dans IntelliJ : Run → `client.ClientApp`

Vous pouvez lancer **plusieurs instances** du client pour simuler plusieurs utilisateurs.

---

## Architecture

```
messaging-app/
├── server/
│   ├── Server.java          → Point d'entrée serveur, écoute port 8080
│   ├── ClientHandler.java   → Gère 1 client dans 1 thread (RG11)
│   └── ServerLogger.java    → Journalisation (RG12)
├── client/
│   ├── ClientApp.java       → Point d'entrée JavaFX
│   ├── ServerConnection.java → Gestion socket client
│   └── controllers/
│       ├── LoginController.java    → Écran connexion
│       ├── RegisterController.java → Écran inscription
│       └── ChatController.java     → Écran principal chat
├── model/
│   ├── User.java            → Entité JPA utilisateur
│   ├── Message.java         → Entité JPA message
│   ├── Status.java          → Enum ONLINE/OFFLINE
│   └── MessageStatus.java   → Enum ENVOYE/RECU/LU
├── dao/
│   ├── UserDAO.java         → CRUD utilisateurs
│   └── MessageDAO.java      → CRUD messages
├── util/
│   ├── HibernateUtil.java   → SessionFactory singleton
│   └── PasswordUtil.java    → Hachage SHA-256 (RG9)
└── resources/
    ├── login.fxml           → UI connexion
    ├── register.fxml        → UI inscription
    ├── chat.fxml            → UI chat principal
    └── hibernate.cfg.xml    → Config BDD

```

---

## Protocole Client ↔ Serveur

| Client → Serveur         | Description                        |
|--------------------------|------------------------------------|
| `REGISTER\|user\|pass`   | Inscription                        |
| `LOGIN\|user\|pass`      | Connexion                          |
| `SEND\|receiver\|msg`    | Envoi d'un message                 |
| `GET_USERS`              | Liste des utilisateurs             |
| `GET_HISTORY\|user`      | Historique avec un utilisateur     |
| `LOGOUT`                 | Déconnexion                        |

| Serveur → Client         | Description                        |
|--------------------------|------------------------------------|
| `OK_LOGIN\|username`     | Connexion réussie                  |
| `FAIL_LOGIN\|raison`     | Échec connexion                    |
| `MESSAGE\|from\|msg\|date` | Message reçu en temps réel       |
| `USERS\|u1:status,...`   | Liste des utilisateurs             |
| `HISTORY\|...`           | Historique messages                |
| `USER_CONNECTED\|user`   | Notification connexion             |
| `USER_DISCONNECTED\|user`| Notification déconnexion           |

---

## Règles de gestion implémentées

| RG  | Description                                      | Où                          |
|-----|--------------------------------------------------|-----------------------------|
| RG1 | Username unique                                  | UserDAO + contrainte BDD    |
| RG2 | Authentification requise pour envoyer            | ClientHandler.handleSend()  |
| RG3 | Un seul login à la fois                          | ClientHandler.handleLogin() |
| RG4 | Statut ONLINE/OFFLINE automatique                | ClientHandler               |
| RG5 | Destinataire doit exister                        | ClientHandler.handleSend()  |
| RG6 | Messages différés si offline                     | MessageDAO + deliverPending |
| RG7 | Message non vide, max 1000 caractères            | ClientHandler + ChatCtrl    |
| RG8 | Historique par ordre chronologique               | MessageDAO.getHistory()     |
| RG9 | Mot de passe haché (SHA-256)                     | PasswordUtil                |
| RG10| Erreur affichée si perte connexion               | LoginCtrl + ChatCtrl        |
| RG11| Thread séparé par client                         | Server.java                 |
| RG12| Journalisation connexions/messages               | ServerLogger.java           |
