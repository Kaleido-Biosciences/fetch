FROM openjdk:13-alpine3.9

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

# Add a jhipster user to run our application so that it doesn't need to run as root
RUN adduser -D -s /bin/sh dea
WORKDIR /home/dea

ADD entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown dea:dea entrypoint.sh
USER dea

ADD target/*.jar app.jar

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8080

