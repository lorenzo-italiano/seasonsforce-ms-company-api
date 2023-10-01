# Utilisation de l'image de base officielle PostgreSQL
FROM postgres:latest

# Variables d'environnement pour configurer PostgreSQL
ENV POSTGRES_USER seasonsforce
ENV POSTGRES_PASSWORD seasonsforce
ENV POSTGRES_DB company

# Port sur lequel PostgreSQL écoutera (par défaut : 5432)
EXPOSE 5432

# Commande pour démarrer PostgreSQL
CMD ["postgres"]
