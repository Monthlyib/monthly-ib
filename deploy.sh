#!/bin/bash
set -euo pipefail

BUILD=/home/ubuntu/monthly-ib

cd $BUILD || { echo "Failed to change directory to $BUILD"; exit 1; }

# Git 업데이트 (main 브랜치)
git fetch --all
git reset --hard origin/main
git pull origin main

# Gradle 실행 권한 설정
chmod 700 gradlew
sudo chmod -R 755 .

# 컨테이너 및 이미지 이름
APP_CONTAINER_NAME=web-server-app
APP_OLD_IMAGES_NAME=monthly-ib_app
NGINX_CONTAINER_NAME=nginx_server
NGINX_OLD_IMAGES_NAME=nginx
CERTBOT_CONTAINER_NAME=certbot
CERTBOT_OLD_IMAGES_NAME=certbot/certbot

# 기존 컨테이너 및 이미지 정리
cleanup_old_container_and_images() {
  local container_name=$1
  local image_reference=$2

  if [ "$(sudo docker ps -q -f name=$container_name)" ]; then
    echo "Stopping existing ${container_name} container..."
    sudo docker stop $container_name
    sudo docker rm $container_name
  fi

  OLD_IMAGES=$(sudo docker images -aq --filter "reference=$image_reference")
  if [ ! -z "$OLD_IMAGES" ]; then
    echo "Removing old images for reference: ${image_reference}..."
    sudo docker rmi -f $OLD_IMAGES
  fi
}

cleanup_old_container_and_images "$APP_CONTAINER_NAME" "$APP_OLD_IMAGES_NAME"
cleanup_old_container_and_images "$NGINX_CONTAINER_NAME" "$NGINX_OLD_IMAGES_NAME"
cleanup_old_container_and_images "$CERTBOT_CONTAINER_NAME" "$CERTBOT_OLD_IMAGES_NAME"

# Gradle 빌드
./gradlew clean build -x test

# Docker Compose 빌드 및 실행
sudo docker-compose build
sudo docker-compose up -d

echo "Deploy complete."
