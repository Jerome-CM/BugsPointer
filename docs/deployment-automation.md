# Deploiement staging et production

Ce depot est prepare pour le schema suivant :

- `develop` deploie vers `staging.bugspointer.com`
- `main` ou `master` deploie vers `bugspointer.com`

## Fichiers versionnes

- `deploy/env/staging.env.example`
- `deploy/env/prod.env.example`
- `deploy/systemd/bugspointer-staging.service`
- `deploy/systemd/bugspointer.service`
- `deploy/nginx/staging.bugspointer.conf`
- `deploy/nginx/bugspointer.conf`
- `.github/workflows/ci.yml`
- `.github/workflows/deploy-staging.yml`
- `.github/workflows/deploy-prod.yml`

## Secrets GitHub Actions a creer

Ces secrets doivent exister dans le repository avant d'activer les deploiements :

- `VPS_HOST`
- `VPS_USER`
- `VPS_SSH_PRIVATE_KEY`

## Fichiers a creer sur le VPS

1. Creer les repertoires de deploiement :

```bash
mkdir -p /home/jerome/apps/bugspointer/prod
mkdir -p /home/jerome/apps/bugspointer/staging
```

2. Creer les fichiers d'environnement :

- `/etc/bugspointer/prod.env`
- `/etc/bugspointer/staging.env`

Tu peux partir des exemples du dossier `deploy/env/`.

3. Installer les services systemd :

```bash
sudo cp deploy/systemd/bugspointer.service /etc/systemd/system/bugspointer.service
sudo cp deploy/systemd/bugspointer-staging.service /etc/systemd/system/bugspointer-staging.service
sudo systemctl daemon-reload
sudo systemctl enable bugspointer
sudo systemctl enable bugspointer-staging
```

4. Installer la conf nginx :

```bash
sudo cp deploy/nginx/bugspointer.conf /etc/nginx/sites-available/bugspointer.conf
sudo cp deploy/nginx/staging.bugspointer.conf /etc/nginx/sites-available/staging.bugspointer.conf
```

Puis activer les sites selon ta distribution.

## Notes pratiques

- Les services sont prepares pour tourner en HTTP local sur `127.0.0.1`, avec TLS termine par nginx.
- Le deploiement GitHub Actions envoie simplement le jar sur le VPS puis redemarre le service cible.
- Si ton quota GitHub Actions est momentanement epuise, les workflows peuvent rester versionnes des maintenant. Il suffira d'ajouter les secrets et de les laisser tourner quand le quota reviendra.
