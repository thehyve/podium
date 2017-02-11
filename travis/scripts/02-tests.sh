#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Check Javadoc generation for UAA and gateway
#-------------------------------------------------------------------------------
if [ -f "mvn" ]; then
    cd "$HOME"/podium-uaa
    mvn javadoc:javadoc
    cd "$HOME"/podium-gateway
    mvn javadoc:javadoc
fi

#-------------------------------------------------------------------------------
# Launch UAA tests
#-------------------------------------------------------------------------------
cd "$HOME"/podium-uaa
if [ -f "mvn" ]; then
    mvn test
fi
#-------------------------------------------------------------------------------
# Launch gateway tests
#-------------------------------------------------------------------------------
cd "$HOME"/podium-gateway
if [ -f "mvn" ]; then
    mvn test \
        -Dlogging.level.org.bbmri.podium.sample=ERROR \
        -Dlogging.level.org.bbmri.podium.travis=ERROR
fi
if [ -f "tsconfig.json" ]; then
    yarn run test
fi