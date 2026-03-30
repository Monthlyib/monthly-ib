#!/bin/bash
set -euo pipefail

BUILD=/home/ubuntu/monthly-ib
JAR_PATH=$BUILD/build/libs/server-0.0.1.jar
SERVICE_NAME=monthlyib

cd $BUILD || { echo "Failed to change directory to $BUILD"; exit 1; }

# Git 업데이트 (main 브랜치)
git fetch --all
git reset --hard origin/main
git pull origin main

# Gradle 빌드
chmod 700 gradlew
./gradlew clean build -x test

# systemd 서비스 파일 생성 (없을 경우)
SERVICE_FILE=/etc/systemd/system/$SERVICE_NAME.service
if [ ! -f "$SERVICE_FILE" ]; then
  sudo tee $SERVICE_FILE > /dev/null <<EOF
[Unit]
Description=MonthlyIB Spring Boot Application
After=network.target

[Service]
User=ubuntu
WorkingDirectory=$BUILD
ExecStart=/usr/bin/java -jar $JAR_PATH --spring.profiles.active=dev
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=$SERVICE_NAME

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable $SERVICE_NAME
fi

# 서비스 재시작
sudo systemctl restart $SERVICE_NAME

echo "Deploy complete. App starting on port 8987..."
sleep 5
sudo systemctl status $SERVICE_NAME --no-pager
