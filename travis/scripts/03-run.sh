#!/bin/bash

#-------------------------------------------------------------------------------
# Functions
#-------------------------------------------------------------------------------
launchCurlOrProtractor() {
    retryCount=1
    maxRetry=10
    httpUrl="http://localhost:8080"

    rep=$(curl -v "$httpUrl")
    status=$?
    while [ "$status" -ne 0 ] && [ "$retryCount" -le "$maxRetry" ]; do
        echo "[$(date)] Application not reachable yet. Sleep and retry - retryCount =" $retryCount "/" $maxRetry
        retryCount=$((retryCount+1))
        sleep 10
        rep=$(curl -v "$httpUrl")
        status=$?
    done

    if [ "$status" -ne 0 ]; then
        echo "[$(date)] Not connected after" $retryCount " retries."
        exit 1
    fi

    if [ "$PROTRACTOR" != 1 ]; then
        exit 0
    fi

    retryCount=0
    maxRetry=2
    until [ "$retryCount" -ge "$maxRetry" ]
    do
        result=0
        if [[ -f "tsconfig.json" ]]; then
          cd "$PODIUM_BASE"/podium-gateway
          yarn run e2e
        fi
        result=$?
        [ $result -eq 0 ] && break
        retryCount=$((retryCount+1))
        echo "e2e tests failed... retryCount =" $retryCount "/" $maxRetry
        sleep 15
    done
    exit $result
}

#-------------------------------------------------------------------------------
# Package UAA
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-uaa
if [ -f "mvn" ]; then
    mvn package -DskipTests=true -P"$PROFILE"
        mv target/*.war podium-uaa.war
else
    echo "No maven for UAA"
    exit 0
fi


#-------------------------------------------------------------------------------
# Package gateway
#-------------------------------------------------------------------------------
cd "$PODIUM_BASE"/podium-gateway
if [ -f "mvn" ]; then
    mvn package -DskipTests=true -P"$PROFILE"
    mv target/*.war podium-gateway.war
else
    echo "No maven for gateway"
    exit 0
fi
if [ $? -ne 0 ]; then
    echo "Error when packaging"
    exit 1
fi

#-------------------------------------------------------------------------------
# Run the application
#-------------------------------------------------------------------------------
if [ "$RUN_PODIUM" == 1 ]; then
    cd "$PODIUM_BASE"/podium-uaa
    java -jar podium-uaa.war \
        --spring.profiles.active="$PROFILE" \
        --logging.level.org.bbmri.podium.sample=ERROR \
        --logging.level.org.bbmri.podium.travis=ERROR &
    sleep 80

    cd "$PODIUM_BASE"/podium-gateway
    java -jar podium-gateway.war \
        --spring.profiles.active="$PROFILE" \
        --logging.level.org.bbmri.podium.sample=ERROR \
        --logging.level.org.bbmri.podium.travis=ERROR &
    sleep 40

    #-------------------------------------------------------------------------------
    # Once everything is started, run the tests
    #-------------------------------------------------------------------------------
    launchCurlOrProtractor
fi