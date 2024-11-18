#!/bin/bash
# Gradle 실행 권한 설정
chmod 700 gradlew
sudo chmod -R 755 .

./gradlew clean build

cd build/libs

java -jar server-0.0.1.jar