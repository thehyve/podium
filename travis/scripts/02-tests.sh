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
mvn -q "$MAVEN_OPTS" test
#-------------------------------------------------------------------------------
# Launch gateway tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-gateway
mvn  -q "$MAVEN_OPTS" test \
    -Dlogging.level.nl.thehyve.podium.sample=ERROR \
    -Dlogging.level.nl.thehyve.podium.travis=ERROR

if [ -f "tsconfig.json" ]; then
    yarn run test
fi
