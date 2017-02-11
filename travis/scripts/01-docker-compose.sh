#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Start docker container
#-------------------------------------------------------------------------------
cd "$HOME"/podium-gateway
if [ -a src/main/docker/podium-registry.yml ]; then
    docker-compose -f src/main/docker/podium-registry.yml up -d
fi
if [ -a src/main/docker/postgresql.yml ]; then
    docker-compose -f src/main/docker/postgresql.yml up -d
fi
if [ -a src/main/docker/elasticsearch.yml ]; then
    docker-compose -f src/main/docker/elasticsearch.yml up -d
fi
cd "$HOME"/podium-uaa
if [ -a src/main/docker/postgresql.yml ]; then
    docker-compose -f src/main/docker/postgresql.yml up -d
fi

docker ps -a
