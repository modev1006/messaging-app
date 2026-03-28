# 📋 Changelog

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

---

## [1.0.0] — 2026-03-28

### ✨ Ajouté
- **Authentification** : Inscription et connexion avec hachage SHA-256
- **Messagerie temps réel** : Communication bidirectionnelle via TCP Sockets
- **Statuts de message** : Envoyé (✓), Reçu (✓✓), Lu (✓✓ bleu)
- **Transfert de fichiers** : Envoi d'images et documents via encodage Base64
- **Indicateurs de présence** : Statut en ligne / hors ligne en temps réel
- **Indicateurs de saisie** : Notification visuelle quand un contact écrit
- **Compteur de non-lus** : Badges visuels pour les messages manqués
- **Persistance** : Historique complet des messages et données utilisateur en PostgreSQL
- **Splash Screen** : Écran d'accueil animé au lancement de l'application
- **Interface moderne** : Design CSS avec thème sombre et glassmorphism

### 🏗️ Infrastructure
- Configuration Maven avec JavaFX 17 et Hibernate 6.4
- Architecture Client-Serveur multi-threadée
- Protocole de communication personnalisé (pipe-delimited)
- Pattern DAO pour l'accès aux données
- Pattern MVC pour l'interface utilisateur

### 📄 Documentation
- README professionnel avec badges et diagrammes
- Guide de contribution (CONTRIBUTING.md)
- Journal des modifications (CHANGELOG.md)
- Politique de sécurité (SECURITY.md)
- Licence MIT
