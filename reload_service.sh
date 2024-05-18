#!/bin/bash

if [ -f .env ]; then
  export $(cat .env | grep -v '#' | awk '/=/ {print $1}')
fi

if [ -z "$REMOTE_USER" ] || [ -z "$REMOTE_HOST" ] || [ -z "$REMOTE_PORT" ] || [ -z "$SERVICE_NAME" ]; then
  echo "One or more required variables are not set in the .env file."
  exit 1
fi

ssh -p $REMOTE_PORT $REMOTE_USER@$REMOTE_HOST "sudo systemctl restart $SERVICE_NAME"

if [ $? -eq 0 ]; then
    echo "Service $SERVICE_NAME restarted successfully on $REMOTE_HOST"
else
    echo "Failed to restart service $SERVICE_NAME on $REMOTE_HOST"
fi
