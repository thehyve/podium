#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Prepare databases
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn -q "$MAVEN_OPTS" liquibase:update
cd "$PODIUM_BASE"/podium-gateway
mvn -q "$MAVEN_OPTS" liquibase:update
