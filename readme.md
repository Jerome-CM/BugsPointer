# Bienvenue

## Obtenez un rapport de Bug sur votre site facilement

BugsPointer est une solution simple de laisser vos utilisateurs signaler des problèmes sur votre site lors de leur navigation : 

    - Problème de synthaxe
    - Chemin de fichier erroné
    - Soucis de design
    - Erreur système

Le projet est encore en cours de développement

## Configuration des e-mails OVH MX Plan

L'application envoie les e-mails transactionnels avec l'adresse `noreply@bugspointer.com` et ajoute `contact@bugspointer.com` en adresse de réponse.

Variables d'environnement à configurer sur le serveur :

```env
MAIL_SMTP=ssl0.ovh.net
MAIL_PORT=587
MAIL_USER=noreply@bugspointer.com
MAIL_PASSWORD=mot_de_passe_de_la_boite_noreply
MAIL_FROM_NOREPLY=noreply@bugspointer.com
MAIL_FROM_CONTACT=contact@bugspointer.com
MAIL_FROM_NAME=BugsPointer
```

Pour limiter l'arrivée en spam, vérifier aussi la zone DNS `bugspointer.com` chez OVH :

- MX : conserver les entrées MX Plan OVH.
- SPF : autoriser les serveurs OVH à envoyer pour le domaine.
- DKIM : activer DKIM dans l'espace client OVH si disponible pour le MX Plan.
- DMARC : ajouter une politique DMARC progressive, par exemple en surveillance au départ.

Après modification DNS, attendre la propagation puis tester un envoi vers Gmail, Outlook et une adresse externe. Le `From` doit être `noreply@bugspointer.com` et le `Reply-To` doit être `contact@bugspointer.com`.
