#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Check Javadoc generation for UAA and gateway
#-------------------------------------------------------------------------------
export MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cl‌​i.transfer.Slf4jMave‌​nTransferListener=wa‌​rn"

cd "$PODIUM_BASE"/podium-uaa
mvn -B -q javadoc:javadoc
cd "$PODIUM_BASE"/podium-gateway
mvn -B -q javadoc:javadoc

mkdir -p "$PODIUM_BASE"/podium-gateway/src/test/features

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
