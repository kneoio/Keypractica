#!/bin/bash

# Ensure script is run as root
if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit 1
fi

# Check for the -d flag (daemon mode)
DAEMON_MODE=false
while getopts "d" option; do
  case $option in
    d) DAEMON_MODE=true ;;
    *) echo "Usage: $0 [-d] (run as a service)"; exit 1 ;;
  esac
done

# Debugging output to verify if daemon mode is recognized
echo "Daemon mode: $DAEMON_MODE"

# Update and install core utilities
echo "Updating package list and installing core utilities..."
apt update && apt install -y coreutils maven git

# Download Adoptium JDK 21
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21+35/OpenJDK21U-jdk_x64_linux_hotspot_21_35.tar.gz"
JDK_DEST="/tmp/OpenJDK21.tar.gz"
JDK_INSTALL_DIR="/opt/jdk-21+35"

echo "Downloading Adoptium JDK 21..."
curl -L $JDK_URL -o $JDK_DEST

echo "Extracting JDK..."
tar -xzf $JDK_DEST -C /opt/

echo "Creating symlink for Java..."
ln -s $JDK_INSTALL_DIR/bin/java /usr/bin/java

# Stop Keypractica service if running
echo "Stopping Keypractica service if it exists..."
systemctl stop keypractica || true

# Clone/update the Keypractica project
REPO_DIR="/home/keypractica/be_server"
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
if [ "$DAEMON_MODE" = true ]; then
  echo "Setting up to run as a systemd service..."

  # Create systemd service file
  SERVICE_FILE="/etc/systemd/system/keypractica.service"
  echo "Creating systemd service for Keypractica..."
  cat << EOF > $SERVICE_FILE
[Unit]
Description=Keypractica Service
After=network.target

[Service]
User=aida
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
