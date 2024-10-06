#!/bin/bash

# Define variables
REPO_DIR="/home/keypractica/be_server"

# Find the JAR file in the project
JAR_FILE=$(find $REPO_DIR -name "*-runner.jar" | head -n 1)

# Check if the JAR file was found
if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found. Please build the project first."
  exit 1
fi

echo "Starting Keypractica server using JAR file: $JAR_FILE in development-like mode..."

# Run the JAR file in a development-like configuration
/usr/bin/java \
  -Dquarkus.profile=prod \
  -Dquarkus.config.locations=file:/home/keypractica/application-prod.properties \
  -Dquarkus.live-reload.password=devmode \
  -Dquarkus.log.level=DEBUG \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/dev_database \
  -Dquarkus.http.port=8080 \
  -jar $JAR_FILE
