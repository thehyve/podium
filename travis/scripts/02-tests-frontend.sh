#!/usr/bin/env bash

cd "$PODIUM_BASE"/podium-gateway

if [ -f "tsconfig.json" ]; then
    yarn run test
    # upload karma code coverage to codebeat.co
    yarn global add codeclimate-test-reporter
    codeclimate-test-reporter < target/test-results/coverage/lcov.info
fi
