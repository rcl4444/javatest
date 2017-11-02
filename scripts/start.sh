#!/bin/sh

DB_CONTEXT=${DB_CONTEXT:=prod}

# db setup
java -jar ppbe-1.0-SNAPSHOT.jar db migrate -i $DB_CONTEXT config.yml

# run
java -jar ppbe-1.0-SNAPSHOT.jar server config.yml

