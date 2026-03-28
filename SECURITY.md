# 🔒 Politique de Sécurité

## Versions supportées

| Version | Supportée |
|:---|:---|
| 1.0.x | ✅ |
| < 1.0 | ❌ |

---

## 🛡️ Mesures de sécurité implémentées

| Mesure | Détails |
|:---|:---|
| **Hachage des mots de passe** | SHA-256 (voir `util/PasswordUtil.java`) |
| **Protocole réseau** | TCP avec protocole texte personnalisé |
| **Validation des entrées** | Vérification côté serveur avant traitement |
| **Gestion des sessions** | Suivi des connexions actives côté serveur |

---

## 🚨 Signaler une vulnérabilité

Si vous découvrez une faille de sécurité :

1. **NE PAS** ouvrir une issue publique
2. Envoyer un email à l'adresse du mainteneur avec :
   - Description de la vulnérabilité
   - Étapes pour reproduire le problème
   - Impact potentiel estimé
3. Attendre une confirmation sous **48h**

> [!CAUTION]
> Ne partagez jamais publiquement les détails d'une vulnérabilité avant qu'elle ne soit corrigée.

---

## 📋 Bonnes pratiques pour les contributeurs

- Ne **jamais** commiter de credentials (mots de passe, clés API)
- Utiliser le fichier `.env` pour la configuration locale
- Vérifier le `.gitignore` avant chaque commit
- Utiliser des requêtes préparées (Hibernate/JPA le fait automatiquement)
