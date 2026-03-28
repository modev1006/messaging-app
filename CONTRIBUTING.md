# 🤝 Guide de Contribution

Merci de votre intérêt pour ce projet ! Voici comment contribuer efficacement.

---

## 📋 Prérequis

- **Java 17** (LTS)
- **Maven 3.8+**
- **PostgreSQL 16**
- **JavaFX 17 SDK** (inclus via Maven)

---

## 🚀 Démarrage rapide

```bash
# 1. Forker le repo
# 2. Cloner votre fork
git clone https://github.com/VOTRE_USERNAME/messaging-app.git
cd messaging-app

# 3. Copier le fichier d'environnement
cp .env.example .env
# → Remplir avec vos identifiants PostgreSQL

# 4. Compiler
mvn clean compile

# 5. Lancer le serveur
mvn exec:java -Dexec.mainClass="server.Server"

# 6. Lancer le client (dans un autre terminal)
mvn javafx:run
```

---

## 🔀 Workflow Git

1. **Créer une branche** depuis `main` :
   ```bash
   git checkout -b feature/ma-fonctionnalite
   ```

2. **Conventions de nommage des branches** :
   | Préfixe | Usage |
   |:---|:---|
   | `feature/` | Nouvelle fonctionnalité |
   | `fix/` | Correction de bug |
   | `docs/` | Modification de documentation |
   | `refactor/` | Refactoring de code |

3. **Commiter** en utilisant les [Conventional Commits](https://www.conventionalcommits.org/) :
   ```
   feat: ajout du transfert d'images
   fix: correction de la déconnexion inattendue
   docs: mise à jour du README
   ```

4. **Pousser** et créer une Pull Request vers `main`.

---

## ✅ Checklist avant Pull Request

- [ ] Le code compile sans erreur (`mvn clean compile`)
- [ ] Les modifications respectent l'architecture existante (MVC, DAO, etc.)
- [ ] Le code est commenté (Javadoc pour les méthodes publiques)
- [ ] Le README est mis à jour si nécessaire
- [ ] Pas de credentials en dur dans le code

---

## 🏗️ Architecture du projet

```
src/main/java/
├── server/          # Logique serveur (Socket, ClientHandler)
├── client/          # Application JavaFX (Controllers, App)
├── model/           # Entités JPA (User, Message)
├── dao/             # Couche d'accès aux données
└── util/            # Utilitaires (Hibernate, Sécurité)
resources/           # Vues FXML, CSS, Configuration
```

> [!IMPORTANT]
> Respectez la séparation des couches. Ne mettez jamais de logique métier dans les contrôleurs JavaFX.

---

## 📄 Code de conduite

Soyez respectueux et constructif dans vos échanges. Toute contribution est la bienvenue !
