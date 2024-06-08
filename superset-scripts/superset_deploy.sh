#!/bin/bash

current="$(dirname "$0")"
cd $current
cd ..

if [[ -z "${SUPERSET_SECRET_KEY}" ]]; then
  key=$(openssl rand -base64 42)
else
  key=$SUPERSET_SECRET_KEY
fi

volumeDirectory=/home/ssm-user/superset-volumes

cd superset-scripts || {
  echo "Error cd"
  exit 1
}

# Create a user-defined network
docker network create superset-network

# Start a PostgreSQL container
docker run -d --network superset-network --name superset-postgres -e POSTGRES_USER=superset -e POSTGRES_PASSWORD=superset \
-e POSTGRES_DB=superset -v $volumeDirectory/pgdata:/var/lib/postgresql/data --health-cmd='pg_isready' --health-interval=10s \
--health-timeout=5s --health-retries=5 postgres:15

# Start a Redis container
docker run -d --network superset-network --name superset-redis --health-cmd='redis-cli ping' --health-interval=10s --health-timeout=5s \
--health-retries=5 redis:7.2.4-alpine

#https://hub.docker.com/r/apache/superset

# Start a Superset container
docker run -d --network superset-network --name superset -p 8088:8088 -e POSTGRES_USER=superset -e POSTGRES_PASSWORD=superset \
-e POSTGRES_DB=superset -e POSTGRES_HOST=superset-postgres -e POSTGRES_PORT=5432 -e REDIS_HOST=superset-redis -e REDIS_PORT=6379 \
-e SUPERSET_SECRET_KEY="$key" -e SUPERSET_CONFIG_PATH=/app/superset/superset_config.py \
-v $volumeDirectory/superset:/app/superset_home \
--health-cmd='wget --quiet --tries=1 --spider http://localhost:8088/health || exit 1' --health-interval=30s --health-timeout=10s --health-retries=3 "$ecrUrl"

docker exec -it superset superset fab create-admin --username admin --firstname Superset --lastname Admin --email admin@superset.com --password admin

docker exec -it superset superset db upgrade

docker exec -it superset superset init