FROM openjdk:8-jre-alpine
EXPOSE 8080

COPY target/ppbe-1.0-SNAPSHOT.jar /
COPY config.yml /
COPY config.yml.tmpl /
COPY scripts/*.sh /
RUN chmod a+x /*.sh

WORKDIR /

ENTRYPOINT ["/entrypoint.sh"]
CMD /start.sh

