#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Check Javadoc generation for UAA and gateway
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn -q "$MAVEN_OPTS" javadoc:javadoc
cd "$PODIUM_BASE"/podium-gateway
mvn -q "$MAVEN_OPTS" javadoc:javadoc

mkdir -p "$PODIUM_BASE"/podium-gateway/src/test/features

#-------------------------------------------------------------------------------
# Launch UAA tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn -q "$MAVEN_OPTS" -Dspring.profiles.active=h2,test test
#-------------------------------------------------------------------------------
# Launch gateway tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-gateway
mvn  -q "$MAVEN_OPTS" -Dspring.profiles.active=test test \
    -Dlogging.level.nl.thehyve.podium.sample=ERROR \
    -Dlogging.level.nl.thehyve.podium.travis=ERROR

if [ -f "tsconfig.json" ]; then
    yarn run test
fi

echo "Stopping and removing docker containers"
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
