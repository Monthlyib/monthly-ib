#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="$ROOT_DIR/build/libs/server-0.0.1.jar"

cd "$ROOT_DIR"
chmod 700 gradlew

./gradlew clean build

exec java -jar "$JAR_PATH" --spring.profiles.active=dev
