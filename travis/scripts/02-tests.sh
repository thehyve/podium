#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Check Javadoc generation for UAA and gateway
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn -q javadoc:javadoc
cd "$PODIUM_BASE"/podium-gateway
mvn -q javadoc:javadoc

mkdir -p "$PODIUM_BASE"/podium-gateway/src/test/features

#-------------------------------------------------------------------------------
# Launch UAA tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn -q -Dspring.profiles.active=h2,test test
#-------------------------------------------------------------------------------
# Launch gateway tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-gateway
mvn  -q -Dspring.profiles.active=test test \
    -Dlogging.level.nl.thehyve.podium.sample=ERROR \
    -Dlogging.level.nl.thehyve.podium.travis=ERROR

