#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Prepare databases
#-------------------------------------------------------------------------------
export MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cl‌​i.transfer.Slf4jMave‌​nTransferListener=wa‌​rn"

cd "$PODIUM_BASE"/podium-uaa
mvn -q ${MAVEN_OPTS} liquibase:update
cd "$PODIUM_BASE"/podium-gateway
mvn -q ${MAVEN_OPTS} liquibase:update
