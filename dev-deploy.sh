#!/bin/bash

# 빌드 경로 설정
BUILD=/home/ubuntu/monthly-ib

# 빌드 디렉토리로 이동
cd $BUILD || { echo "Failed to change directory to $BUILD"; exit 1; }

# Git 업데이트
git fetch --all
git reset --hard origin/dev
git checkout dev
git pull origin dev

# Gradle 실행 권한 설정
chmod 700 gradlew
sudo chmod -R 755 .

# 컨테이너 및 이미지 이름 설정
APP_CONTAINER_NAME=web-server-app
APP_OLD_IMAGES_NAME=monthly-ib_app
REDIS_CONTAINER_NAME=redis_server
NGINX_OLD_IMAGES_NAME=nginx
NGINX_CONTAINER_NAME=nginx_server
REDIS_OLD_IMAGES_NAME=redis
CERBOT_CONTAINER_NAME=cerbot
CERTBOT_OLD_IMAGES_NAME=certbot/certbot


# Gradle 빌드
./gradlew clean build

# 기존 컨테이너 종료 및 이미지 삭제 함수
cleanup_old_container_and_images() {
  local container_name=$1
  local image_reference=$2

  if [ "$(sudo docker ps -q -f name=$container_name)" ]; then
    echo "Stopping existing ${container_name} container..."
    sudo docker stop $container_name
    echo "Removing existing ${container_name} container..."
    sudo docker rm $container_name
  fi

  OLD_IMAGES=$(sudo docker images -aq --filter "reference=$image_reference")

  if [ ! -z "$OLD_IMAGES" ]; then
    echo "Removing old images for reference: ${image_reference}..."
    sudo docker rmi -f $OLD_IMAGES
  else
    echo "No images found for reference: ${image_reference}"
  fi
}

# 각 컨테이너 및 이미지 정리
cleanup_old_container_and_images "$APP_CONTAINER_NAME" "$APP_OLD_IMAGES_NAME"
cleanup_old_container_and_images "$REDIS_CONTAINER_NAME" "$REDIS_OLD_IMAGES_NAME"
cleanup_old_container_and_images "$NGINX_CONTAINER_NAME" "$NGINX_OLD_IMAGES_NAME"
cleanup_old_container_and_images "$CERBOT_CONTAINER_NAME" "$CERTBOT_OLD_IMAGES_NAME"

# Docker Compose 빌드 및 실행
sudo docker-compose build
sudo docker-compose up -d