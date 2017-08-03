FROM docker.elastic.co/beats/metricbeat:5.5.1

COPY dashboards/ /tmp
WORKDIR /usr/share/metricbeat/scripts/
COPY wait-for-elasticsearch.sh create-index-patterns.sh ./
ENV ELASTICSEARCH_URL=http://podium-elasticsearch-1:9200

CMD ./wait-for-elasticsearch.sh && ./create-index-patterns.sh && ./import_dashboards -es $ELASTICSEARCH_URL -dir /tmp/
