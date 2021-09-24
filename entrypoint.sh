#!/bin/sh

exec java ${JAVA_OPTS} -Pprod -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/app.jar" "$@"
