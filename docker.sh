#!/bin/bash


OLD_IMAGES_NAME=monthly-ib_app


# 기존 컨테이너 종료 후 이미지 제거
OLD_IMAGES=$(sudo docker images -aq --filter "reference=$OLD_IMAGES_NAME")

if [ ! -z "$OLD_IMAGES" ]; then
  echo "Stopping and removing containers using images..."
  CONTAINERS=$(sudo docker ps -q --filter "ancestor=$OLD_IMAGES_NAME")

  if [ ! -z "$CONTAINERS" ]; then
    echo "Stopping running containers..."
    sudo docker stop $CONTAINERS
    echo "Removing stopped containers..."
    sudo docker rm $CONTAINERS
  fi

  echo "Removing old images..."
  sudo docker rmi -f $OLD_IMAGES
else
  echo "No images found for reference: $OLD_IMAGES"
fi


NGINX_CONTAINER_NAME=nginx_server
# 기존 Nginx 컨테이너를 중지하고 제거
if [ "$(sudo docker ps -q -f name=NGINX_CONTAINER_NAME)" ]; then
    echo "Stopping existing NGINX container..."
    sudo docker stop NGINX_CONTAINER_NAME
    echo "Removing existing NGINX container..."
    sudo docker rm NGINX_CONTAINER_NAME
fi

CERBOT_CONTAINER_NAME=cerbot
# 기존 Nginx 컨테이너를 중지하고 제거
if [ "$(sudo docker ps -q -f name=CERBOT_CONTAINER_NAME)" ]; then
    echo "Stopping existing CERBOT container..."
    sudo docker stop CERBOT_CONTAINER_NAME
    echo "Removing existing CERBOT container..."
    sudo docker rm CERBOT_CONTAINER_NAME
fi

echo "> Dev DEPLOY_JAR 배포" >> $LOG_PATH/deploy.log

# Docker Compose 빌드
sudo docker-compose build

# 나머지 컨테이너를 Docker Compose을 사용하여 실행
sudo docker-compose up -d