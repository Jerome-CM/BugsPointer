# BugsPointer

BugsPointer aide les équipes web à recevoir des rapports de bug exploitables directement depuis leur site.

Un visiteur clique sur un bouton ou un lien, décrit le problème, pointe la zone concernée et BugsPointer centralise les informations utiles pour reproduire plus vite : URL, navigateur, résolution, contexte de page et description.

## Fonctionnalités

- Widget de signalement intégrable avec un simple script.
- Mode bouton flottant ou lien personnalisé.
- Dashboard privé pour suivre et prioriser les bugs.
- Vérification de domaine pendant l'onboarding.
- Authentification par e-mail/mot de passe, Google et GitHub.
- Gestion des plans Free et Target.
- Scanner de site pour les contrôles avant mise en production.
- Pages SEO dédiées aux cas d'usage BugsPointer.

## Stack

- Java 8
- Spring Boot 2.7
- Spring Security
- OAuth2 Client Google/GitHub
- Thymeleaf
- Spring Data JPA
- MySQL
- Maven Wrapper
- Mollie pour les paiements
- Jsoup pour la vérification/scanner

## Démarrage Local

Préparer un fichier de configuration local à partir de l'exemple :

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Configurer au minimum la base de données, le secret JWT, l'e-mail transactionnel et la clé Mollie.

Lancer les tests :

```bash
./mvnw test
```

Lancer l'application :

```bash
./mvnw spring-boot:run
```

Par défaut, l'application écoute sur `127.0.0.1:8080`.

## Variables D'environnement

```env
SERVER_PORT=8080
SERVER_ADDRESS=127.0.0.1

SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bugspointer
SPRING_DATASOURCE_USERNAME=bugspointer
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

JWT_SECRET=change-me
APP_BASE_URL=https://bugspointer.com

GOOGLE_OAUTH_CLIENT_ID=
GOOGLE_OAUTH_CLIENT_SECRET=
GITHUB_OAUTH_CLIENT_ID=
GITHUB_OAUTH_CLIENT_SECRET=

MAIL_SMTP=ssl0.ovh.net
MAIL_PORT=587
MAIL_USER=noreply@bugspointer.com
MAIL_PASSWORD=
MAIL_FROM_NOREPLY=noreply@bugspointer.com
MAIL_FROM_CONTACT=contact@bugspointer.com
MAIL_FROM_NAME=BugsPointer

MOLLIE_KEY=
METRICS_EXCLUDED_IPS=
```

Pour staging :

```env
APP_BASE_URL=https://staging.bugspointer.com
```

## OAuth

Callbacks à déclarer côté fournisseurs :

Google :

```text
https://bugspointer.com/login/oauth2/code/google
https://staging.bugspointer.com/login/oauth2/code/google
```

GitHub :

```text
https://bugspointer.com/login/oauth2/code/github
https://staging.bugspointer.com/login/oauth2/code/github
```

Scopes utilisés :

- Google : `openid,email`
- GitHub : `user:email`

Le projet demande le minimum nécessaire pour identifier l'utilisateur par e-mail.

## E-mails OVH MX Plan

L'application envoie les e-mails transactionnels avec `noreply@bugspointer.com` et ajoute `contact@bugspointer.com` en réponse.

À vérifier côté DNS :

- MX : conserver les entrées MX Plan OVH.
- SPF : autoriser les serveurs OVH à envoyer pour le domaine.
- DKIM : activer DKIM dans l'espace client OVH si disponible.
- DMARC : démarrer avec une politique progressive en surveillance.

Après modification DNS, attendre la propagation puis tester un envoi vers Gmail, Outlook et une adresse externe.

## Routes Utiles

- `/` : accueil
- `/authentication` : connexion/inscription
- `/features` : fonctionnalités et plan Target
- `/documentations` : installation et aide
- `/testPage` : page de test du widget
- `/app/private/onboarding/widget` : onboarding d'installation
- `/app/private/dashboard` : dashboard utilisateur
- `/app/admin/dashboard` : dashboard admin
- `/app/admin/companiesList` : liste des comptes

## Widget

Installation bouton flottant :

```html
<script src="https://bugspointer.com/widget/v1/modalPointer.js" data-public-key="pk_xxxxx" defer></script>
```

Installation avec lien personnalisé :

```html
<script src="https://bugspointer.com/widget/v1/modalPointer.js" data-public-key="pk_xxxxx" data-button-style="custom" defer></script>
<a href="#" data-bugspointer-open data-bugspointer-key="pk_xxxxx">Signaler un bug</a>
```

## Qualité Et Workflow

Avant de pousser une modification :

```bash
./mvnw test
```

Les changements fonctionnels ou visuels sont suivis via des issues GitHub avec :

- le titre de la modification ;
- une description courte ;
- les fichiers à vérifier ;
- une checklist de validation.

## Notes

Le projet utilise actuellement `spring.jpa.hibernate.ddl-auto=update` par défaut. Pour une production plus stricte, prévoir des migrations explicites dès que le schéma se stabilise.
