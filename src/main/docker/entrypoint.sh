#!/bin/sh

exec java ${JAVA_OPTS} -Dspring.profiles.active=prod,swagger -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/app.jar" "$@"
