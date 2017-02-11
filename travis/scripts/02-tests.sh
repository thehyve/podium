#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Check Javadoc generation for UAA and gateway
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn javadoc:javadoc
cd "$PODIUM_BASE"/podium-gateway
mvn javadoc:javadoc

#-------------------------------------------------------------------------------
# Launch UAA tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn test
#-------------------------------------------------------------------------------
# Launch gateway tests
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-gateway
mvn test \
    -Dlogging.level.org.bbmri.podium.sample=ERROR \
    -Dlogging.level.org.bbmri.podium.travis=ERROR

if [ -f "tsconfig.json" ]; then
    yarn run test
fi