FROM openjdk:8-jre-slim

ENV PODIUM_SLEEP 0

# add directly the war
ADD *.war /app.war

RUN sh -c 'touch /app.war'
VOLUME /tmp
EXPOSE 9999 5701/udp
CMD echo "The application will start in ${PODIUM_SLEEP}s..." && \
    sleep ${PODIUM_SLEEP} && \
    java -Djava.security.egd=file:/dev/./urandom -jar /app.war
