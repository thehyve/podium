#!/usr/bin/env bash

cd "$PODIUM_BASE"/podium-gateway

if [ -f "tsconfig.json" ]; then
    yarn run test
fi
