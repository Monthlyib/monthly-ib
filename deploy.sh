#!/bin/bash
set -euo pipefail

BUILD=/home/ubuntu/monthly-ib
JAR_PATH=$BUILD/build/libs/server-0.0.1.jar
RELEASE_DIR=$BUILD/releases
STATE_DIR=/etc/monthlyib
ACTIVE_PORT_FILE=$STATE_DIR/active_port
ACTIVE_SERVICE_FILE=$STATE_DIR/active_service
BLUE_SERVICE=monthlyib-blue
GREEN_SERVICE=monthlyib-green
LEGACY_SERVICE=monthlyib
BLUE_PORT=8987
GREEN_PORT=8988
HEALTH_PATH=/open-api/health
KEEP_RELEASES=5

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

service_name_for_port() {
  case "$1" in
    "$BLUE_PORT") echo "$BLUE_SERVICE" ;;
    "$GREEN_PORT") echo "$GREEN_SERVICE" ;;
    *) echo "Unknown port: $1" >&2; exit 1 ;;
  esac
}

other_port() {
  case "$1" in
    "$BLUE_PORT") echo "$GREEN_PORT" ;;
    "$GREEN_PORT") echo "$BLUE_PORT" ;;
    *) echo "$GREEN_PORT" ;;
  esac
}

detect_active_port() {
  if [ -f "$ACTIVE_PORT_FILE" ]; then
    local saved_port
    saved_port=$(tr -d '[:space:]' < "$ACTIVE_PORT_FILE")
    if [ "$saved_port" = "$BLUE_PORT" ] || [ "$saved_port" = "$GREEN_PORT" ]; then
      echo "$saved_port"
      return
    fi
  fi

  if sudo grep -RqsE "127\\.0\\.0\\.1:$GREEN_PORT|localhost:$GREEN_PORT" /etc/nginx 2>/dev/null; then
    echo "$GREEN_PORT"
    return
  fi

  echo "$BLUE_PORT"
}

write_service_file() {
  local service_name=$1
  local port=$2
  local release_jar=$3
  local service_file=/etc/systemd/system/$service_name.service

  sudo tee "$service_file" > /dev/null <<EOF
[Unit]
Description=MonthlyIB Spring Boot Application ($service_name)
After=network.target

[Service]
User=ubuntu
WorkingDirectory=$BUILD
ExecStart=/usr/bin/java -jar $release_jar --server.port=$port
SuccessExitStatus=143
Restart=always
RestartSec=10
TimeoutStopSec=35
KillSignal=SIGTERM
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=$service_name

[Install]
WantedBy=multi-user.target
EOF
}

wait_for_health() {
  local port=$1
  local url="http://127.0.0.1:$port$HEALTH_PATH"

  log "Waiting for health check: $url"
  for i in $(seq 1 60); do
    local status
    status=$(curl -fsS -o /dev/null -w "%{http_code}" --max-time 3 "$url" 2>/dev/null || true)
    if [ "$status" = "200" ]; then
      log "Health check passed on port $port"
      return
    fi
    sleep 2
  done

  log "Health check failed on port $port"
  return 1
}

switch_nginx_port() {
  local target_port=$1
  local found_files
  local stamp
  stamp=$(date +%Y%m%d%H%M%S)

  found_files=$(sudo grep -RIlE --exclude="*.bak.*" "127\\.0\\.0\\.1:$BLUE_PORT|127\\.0\\.0\\.1:$GREEN_PORT|localhost:$BLUE_PORT|localhost:$GREEN_PORT" /etc/nginx 2>/dev/null || true)
  if [ -z "$found_files" ]; then
    log "No Nginx upstream config referencing $BLUE_PORT/$GREEN_PORT was found."
    log "Refusing to switch traffic because public traffic would not reach the new port."
    return 1
  fi

  log "Switching Nginx upstream to port $target_port"
  while IFS= read -r file; do
    [ -n "$file" ] || continue
    sudo cp "$file" "$file.bak.$stamp"
    sudo sed -i -E \
      -e "s#127\\.0\\.0\\.1:($BLUE_PORT|$GREEN_PORT)#127.0.0.1:$target_port#g" \
      -e "s#localhost:($BLUE_PORT|$GREEN_PORT)#localhost:$target_port#g" \
      "$file"
  done <<< "$found_files"

  if ! sudo nginx -t; then
    log "Nginx config test failed. Restoring previous Nginx files."
    while IFS= read -r file; do
      [ -n "$file" ] || continue
      sudo cp "$file.bak.$stamp" "$file"
    done <<< "$found_files"
    sudo nginx -t || true
    return 1
  fi

  sudo systemctl reload nginx
}

stop_if_exists() {
  local service_name=$1
  if sudo systemctl list-unit-files "$service_name.service" --no-legend 2>/dev/null | grep -q "$service_name.service"; then
    sudo systemctl stop "$service_name" || true
  fi
}

cleanup_releases() {
  if compgen -G "$RELEASE_DIR/server-*.jar" > /dev/null; then
    ls -1t "$RELEASE_DIR"/server-*.jar | tail -n +"$((KEEP_RELEASES + 1))" | xargs -r rm -f
  fi
}

cd "$BUILD" || { echo "Failed to change directory to $BUILD"; exit 1; }

log "Updating source from origin/main"
git fetch --all
git reset --hard origin/main

log "Building application"
chmod 700 gradlew
./gradlew clean build -x test

mkdir -p "$RELEASE_DIR"
sudo mkdir -p "$STATE_DIR"
sudo chown ubuntu:ubuntu "$STATE_DIR"

RELEASE_JAR="$RELEASE_DIR/server-$(date +%Y%m%d%H%M%S).jar"
cp "$JAR_PATH" "$RELEASE_JAR"

ACTIVE_PORT=$(detect_active_port)
TARGET_PORT=$(other_port "$ACTIVE_PORT")
TARGET_SERVICE=$(service_name_for_port "$TARGET_PORT")
ACTIVE_SERVICE=$(service_name_for_port "$ACTIVE_PORT")

log "Active port: $ACTIVE_PORT ($ACTIVE_SERVICE)"
log "Target port: $TARGET_PORT ($TARGET_SERVICE)"

log "Starting new release on $TARGET_SERVICE"
stop_if_exists "$TARGET_SERVICE"
write_service_file "$TARGET_SERVICE" "$TARGET_PORT" "$RELEASE_JAR"
sudo systemctl daemon-reload
sudo systemctl enable "$TARGET_SERVICE"
sudo systemctl start "$TARGET_SERVICE"

if ! wait_for_health "$TARGET_PORT"; then
  sudo journalctl -u "$TARGET_SERVICE" -n 120 --no-pager || true
  stop_if_exists "$TARGET_SERVICE"
  exit 1
fi

if ! switch_nginx_port "$TARGET_PORT"; then
  stop_if_exists "$TARGET_SERVICE"
  exit 1
fi

echo "$TARGET_PORT" > "$ACTIVE_PORT_FILE"
echo "$TARGET_SERVICE" > "$ACTIVE_SERVICE_FILE"

log "Stopping previous services after traffic switch"
stop_if_exists "$ACTIVE_SERVICE"
stop_if_exists "$LEGACY_SERVICE"

cleanup_releases

log "Deploy complete. Active service: $TARGET_SERVICE on port $TARGET_PORT"
sudo systemctl status "$TARGET_SERVICE" --no-pager
