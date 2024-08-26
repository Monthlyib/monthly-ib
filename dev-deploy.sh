#!/bin/bash

BUILD=/home/ubuntu/monthly-ib
DEV_JAR=server-0.0.1.jar
LOG_PATH=/home/ubuntu/monthly-ib
DEPLOY_PATH=/home/ubuntu/monthly-ib

cd $BUILD || { echo "Failed to change directory to $BUILD"; exit 1; }

# 스크립트 로그 파일이 없다면 생성
mkdir -p $LOG_PATH

git fetch --all
git reset --hard origin/main
git checkout main
git pull origin main

chmod 700 gradlew
sudo chmod -R 755 .

# MySQL 컨테이너 이름
MYSQL_CONTAINER_NAME=mysql

# MySQL 컨테이너가 실행 중인지 확인하고 실행되지 않은 경우 시작
if [ ! "$(sudo docker ps -q -f name=$MYSQL_CONTAINER_NAME)" ]; then
    echo "Starting MySQL container..."
    sudo docker-compose up -d db
    sleep 20  # 컨테이너가 완전히 시작될 때까지 대기
fi

./gradlew clean build

BUILD_JAR=$(ls $BUILD/build/libs/server-0.0.1.jar)

echo "> 현재 시간: $(date)" >> $LOG_PATH/deploy.log
echo "> dev build 파일명: $DEV_JAR" >> $LOG_PATH/deploy.log
echo "> dev build 파일 복사" >> $LOG_PATH/deploy.log

EXIST_FILE=$DEPLOY_PATH/$DEV_JAR
sudo rm -f $EXIST_FILE
sudo cp $BUILD_JAR $EXIST_FILE

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