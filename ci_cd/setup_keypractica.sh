#!/bin/bash

# Ensure script is run as root
if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit
fi

# Load deployment-related variables from deployment-config.properties
DEPLOYMENT_PROPERTIES_FILE="/home/keypractica/deployment-config.properties"
if [ -f "$DEPLOYMENT_PROPERTIES_FILE" ]; then
  # Load specific lines from the deployment properties file
  JDK_URL=$(grep '^JDK_URL=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  JDK_DEST=$(grep '^JDK_DEST=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  JDK_INSTALL_DIR=$(grep '^JDK_INSTALL_DIR=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  REPO_DIR=$(grep '^REPO_DIR=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  SERVICE_FILE=$(grep '^SERVICE_FILE=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  USER=$(grep '^USER=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
  DAEMON_MODE=$(grep '^DAEMON_MODE=' $DEPLOYMENT_PROPERTIES_FILE | cut -d'=' -f2-)
else
  echo "Deployment properties file not found: $DEPLOYMENT_PROPERTIES_FILE"
  exit 1
fi

# Update and install core utilities
echo "Updating package list and installing core utilities..."
apt update && apt install -y coreutils maven git

# Download Adoptium JDK 21
echo "Downloading Adoptium JDK 21 from $JDK_URL..."
curl -L $JDK_URL -o $JDK_DEST

echo "Extracting JDK to $JDK_INSTALL_DIR..."
tar -xzf $JDK_DEST -C /opt/

echo "Creating symlink for Java..."
ln -s $JDK_INSTALL_DIR/bin/java /usr/bin/java

# Stop Keypractica service if running
echo "Stopping Keypractica service if it exists..."
systemctl stop keypractica || true

# Clone/update the Keypractica project
if [ -d "$REPO_DIR" ]; then
  echo "Updating Keypractica project from GitHub..."
  git -C $REPO_DIR pull origin master
else
  echo "Cloning Keypractica project from GitHub..."
  git clone https://github.com/Semantyca/Keypractica.git $REPO_DIR
fi

# Build the project using Maven
echo "Building the Keypractica project..."
cd $REPO_DIR && MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED" mvn clean package -Dquarkus.package.type=uber-jar -DskipTests

# Check if Maven build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed. Exiting."
  exit 1
fi

# Identify the correct JAR file
JAR_FILE=$(find $REPO_DIR -name "*-runner.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found. Exiting."
  exit 1
fi

echo "Using JAR file: $JAR_FILE"

# Option to run as service or in the current terminal
if [ "$DAEMON_MODE" = "true" ]; then
  echo "Setting up to run as a systemd service..."

  # Create systemd service file
  echo "Creating systemd service for Keypractica..."
  cat << EOF > $SERVICE_FILE
[Unit]
Description=Keypractica Service
After=network.target

[Service]
User=$USER
ExecStart=/usr/bin/java -Dquarkus.profile=prod -Dquarkus.config.locations=file:/home/keypractica/application-prod.properties -jar $JAR_FILE
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

  # Reload systemd and enable/start the service
  echo "Reloading systemd daemon and starting Keypractica service..."
  systemctl daemon-reload
  systemctl enable keypractica
  systemctl start keypractica

  echo "Service is running as a daemon!"
else
  echo "Running Keypractica in the current terminal..."
  /usr/bin/java -Dquarkus.profile=prod -Dquarkus.config.locations=file:/home/keypractica/application-prod.properties -jar $JAR_FILE
fi
