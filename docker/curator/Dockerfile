FROM python:3.6-alpine

RUN pip install elasticsearch-curator

COPY ./config/ /config

RUN /usr/bin/crontab /config/crontab.txt

CMD ["/usr/sbin/crond","-f"]
