FROM docker.elastic.co/kibana/kibana:5.5.1

ENV ELASTICSEARCH_URL=http://podium-elasticsearch-1:9200
COPY kibana.yml /usr/share/kibana/config/kibana.yml
RUN ./bin/kibana-plugin install https://github.com/sivasamyk/logtrail/releases/download/v0.1.18/logtrail-5.5.1-0.1.18.zip
COPY logtrail.json /usr/share/kibana/plugins/logtrail/logtrail.json
