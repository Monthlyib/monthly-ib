#!/bin/bash

BUILD=/home/sussa/Desktop/Workspace/monthly-ib/dev/
DEV_JAR=dev-web-server-0.0.1.jar
LOG_PATH=/home/sussa/log/monthlyib
DEPLOY_PATH=/home/greneta/Desktop/workspace/monthly-ib/dev/

cd $BUILD

git checkout dev

git pull origin dev

chmod 700 gradlew

./gradlew clean build

BUILD_JAR=$(ls /home/greneta/Desktop/workspace/dev/generator-web-server/build/libs/web-server-0.0.1.jar)
JAR_NAME=$(basename $BUILD_JAR)

echo "> 현재 시간: $(date)" >> $LOG_PATH/deploy.log

echo "> dev build 파일명: $DEV_JAR" >> $LOG_PATH/deploy.log

echo "> dev build 파일 복사" >> $LOG_PATH/deploy.log

EXIST_FILE=$DEPLOY_PATH$DEV_JAR
sudo rm -f $EXIST_FILE
sudo cp $BUILD_JAR $DEPLOY_PATH$DEV_JAR

echo "> dev 현재 실행중인 애플리케이션 pid 확인" >> $LOG_PATH/deploy.log
CURRENT_PID=$(pgrep -fn $DEV_JAR)

if [ -z $CURRENT_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없어 종료하지 않는다." >> $LOG_PATH/deploy.log
else
  echo "> kill -9 $CURRENT_PID" >> $LOG_PATH/deploy.log
  sudo kill -9 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$DEV_JAR

echo "> Dev DEPLOY_JAR 배포"    >> $LOG_PATH/deploy.log

nohup java -jar -Dspring.profiles.active=dev $DEPLOY_JAR >> $LOG_PATH/dev-runtime.log 2> $LOG_PATH/dev-runtime_err.log &