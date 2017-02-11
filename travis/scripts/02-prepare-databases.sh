#!/bin/bash
set -e

#-------------------------------------------------------------------------------
# Prepare databases
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
mvn liquibase:update
cd "$PODIUM_BASE"/podium-gateway
mvn liquibase:update
