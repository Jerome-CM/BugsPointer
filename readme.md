# BugsPointer

![Java](https://img.shields.io/badge/Java-8-00E676?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?style=for-the-badge)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-templates-005F0F?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-database-4479A1?style=for-the-badge)

BugsPointer ajoute un bouton ou un lien de signalement sur un site web pour transformer les retours flous des utilisateurs en rapports de bug exploitables.

Le visiteur décrit le problème, pointe la zone concernée, et l'équipe reçoit le contexte utile pour comprendre et corriger plus vite.

## Introduction

Un message comme "ça ne marche pas" ne suffit presque jamais.

BugsPointer capture le retour au bon moment, directement sur la page où le problème arrive. Le rapport peut contenir l'URL, la description, le contexte navigateur, la taille d'écran et la zone pointée par l'utilisateur.

## Fonctionnalités

- Signalement de bug depuis le site client.
- Widget installable avec un script.
- Bouton flottant ou lien personnalisé.
- Rapport structuré pour comprendre et reproduire.
- Dashboard pour centraliser et prioriser les retours.
- Vérification d'installation pendant l'onboarding.

## Installation Du Widget

### Option 1 : bouton flottant

```html
<script src="https://bugspointer.com/widget/v1/modalPointer.js" data-public-key="pk_xxxxx" defer></script>
```

Cette option affiche automatiquement le bouton BugsPointer sur le site.

### Option 2 : lien personnalisé

```html
<script src="https://bugspointer.com/widget/v1/modalPointer.js" data-public-key="pk_xxxxx" data-button-style="custom" defer></script>
<a href="#" data-bugspointer-open data-bugspointer-key="pk_xxxxx">Signaler un bug</a>
```

Cette option laisse le site décider où placer le lien : menu, footer, centre d'aide ou page support.
