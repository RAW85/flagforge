#!/usr/bin/env bash
# FlagForge control center for macOS / Linux
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

# colors (if terminal supports them)
if [[ -t 1 ]]; then
  C_CYAN='\033[0;36m'
  C_YELLOW='\033[0;33m'
  C_GREEN='\033[0;32m'
  C_RED='\033[0;31m'
  C_GRAY='\033[0;90m'
  C_WHITE='\033[1;37m'
  C_DGREEN='\033[0;32m'
  C_DYELLOW='\033[0;33m'
  C_RESET='\033[0m'
else
  C_CYAN= C_YELLOW= C_GREEN= C_RED= C_GRAY= C_WHITE= C_DGREEN= C_DYELLOW= C_RESET=
fi

banner() {
  clear 2>/dev/null || true
  echo ""
  echo -e "${C_CYAN}  ============================================================${C_RESET}"
  echo -e "${C_CYAN}   FlagForge Control Center${C_RESET}"
  echo -e "${C_GRAY}   Feature Flag Platform (macOS / Linux)${C_RESET}"
  echo -e "${C_CYAN}  ============================================================${C_RESET}"
  echo ""
}

# Sets globals: JAVA_OK JAVA_DETAIL NODE_OK NODE_DETAIL NPM_OK NPM_DETAIL DOCKER_OK DOCKER_DETAIL
# Each *_OK is 0|1|2  (1=ok, 2=warn, 0=missing)
check_java() {
  JAVA_OK=0
  JAVA_DETAIL="MISSING - install JDK 21"
  have java || return 0
  local v
  v="$(java -version 2>&1 || true)"
  if [[ -z "$v" ]]; then
    JAVA_OK=2
    JAVA_DETAIL="FOUND but version unreadable"
    return 0
  fi
  local ver
  ver="$(echo "$v" | sed -n 's/.*version "\([^"]*\)".*/\1/p' | head -n1)"
  if [[ -z "$ver" ]]; then
    JAVA_OK=2
    JAVA_DETAIL="WARN - installed (could not parse version)"
  elif [[ "$ver" == 21* ]]; then
    JAVA_OK=1
    JAVA_DETAIL="OK - $ver"
  else
    JAVA_OK=2
    JAVA_DETAIL="WARN - $ver (JDK 21 recommended)"
  fi
}

check_node() {
  NODE_OK=0
  NODE_DETAIL="MISSING - install Node 20.19+ / 22.12+ / 24.x"
  have node || return 0
  local ver
  ver="$(node -v 2>/dev/null || true)"
  if [[ -z "$ver" ]]; then
    NODE_OK=2
    NODE_DETAIL="FOUND but version unreadable"
    return 0
  fi
  local major
  major="$(echo "$ver" | sed -n 's/^v\?\([0-9]*\).*/\1/p')"
  if [[ -n "$major" && "$major" -ge 20 ]]; then
    NODE_OK=1
    NODE_DETAIL="OK - $ver"
  else
    NODE_OK=2
    NODE_DETAIL="WARN - $ver (need 20.19+ / 22.12+ / 24.x)"
  fi
}

check_npm() {
  NPM_OK=0
  NPM_DETAIL="MISSING - install npm (comes with Node)"
  have npm || return 0
  local ver
  ver="$(npm -v 2>/dev/null || true)"
  if [[ -z "$ver" ]]; then
    NPM_OK=2
    NPM_DETAIL="WARN - npm found, version unreadable"
  else
    NPM_OK=1
    NPM_DETAIL="OK - $ver"
  fi
}

check_docker() {
  DOCKER_OK=0
  DOCKER_DETAIL="MISSING - install Docker Desktop / Docker Engine"
  have docker || return 0
  if ! docker info >/dev/null 2>&1; then
    DOCKER_OK=2
    DOCKER_DETAIL="INSTALLED but NOT RUNNING - start Docker"
    return 0
  fi
  if docker compose version >/dev/null 2>&1; then
    DOCKER_OK=1
    DOCKER_DETAIL="OK - Docker running + Compose v2"
  else
    DOCKER_OK=2
    DOCKER_DETAIL="WARN - Docker running, but 'docker compose' missing"
  fi
}

print_status_line() {
  local label="$1"
  local ok="$2"
  local detail="$3"
  local pad
  pad="$(printf '%-16s' "$label")"
  if [[ "$ok" -eq 0 ]]; then
    echo -e "${C_RED}  [MISSING] ${pad} ${detail}${C_RESET}"
  elif [[ "$ok" -eq 2 ]]; then
    echo -e "${C_YELLOW}  [WARN]    ${pad} ${detail}${C_RESET}"
  else
    echo -e "${C_GREEN}  [OK]      ${pad} ${C_GRAY}${detail}${C_RESET}"
  fi
}

prerequisites() {
  check_java
  check_node
  check_npm
  check_docker

  echo -e "${C_YELLOW}  SYSTEM CHECK (live)${C_RESET}"
  echo -e "${C_GRAY}  ------------------------------------------------------------${C_RESET}"
  print_status_line "Java JDK 21" "$JAVA_OK" "$JAVA_DETAIL"
  print_status_line "Node.js" "$NODE_OK" "$NODE_DETAIL"
  print_status_line "npm" "$NPM_OK" "$NPM_DETAIL"
  print_status_line "Docker" "$DOCKER_OK" "$DOCKER_DETAIL"
  echo ""

  echo -e "${C_YELLOW}  MODE READINESS${C_RESET}"
  echo -e "${C_GRAY}  ------------------------------------------------------------${C_RESET}"

  local host_ok=0
  if [[ "$JAVA_OK" -ge 1 && "$NODE_OK" -ge 1 && "$NPM_OK" -ge 1 ]]; then
    host_ok=1
  fi
  local docker_ready=0
  if [[ "$DOCKER_OK" -eq 1 ]]; then
    docker_ready=1
  fi

  if [[ "$host_ok" -eq 1 ]]; then
    echo -e "${C_GREEN}  [OK]      Mode 1 (Local H2)              ready${C_RESET}"
  else
    echo -e "${C_RED}  [BLOCKED] Mode 1 (Local H2)              needs Java 21 + Node + npm${C_RESET}"
  fi

  if [[ "$host_ok" -eq 1 && "$docker_ready" -eq 1 ]]; then
    echo -e "${C_GREEN}  [OK]      Mode 2 (Local + Redis/Kafka)   ready${C_RESET}"
  elif [[ "$host_ok" -eq 0 && "$docker_ready" -eq 0 ]]; then
    echo -e "${C_RED}  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Java/Node/npm + Docker running${C_RESET}"
  elif [[ "$host_ok" -eq 0 ]]; then
    echo -e "${C_RED}  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Java 21 + Node + npm${C_RESET}"
  else
    echo -e "${C_RED}  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Docker running${C_RESET}"
  fi

  if [[ "$docker_ready" -eq 1 ]]; then
    echo -e "${C_GREEN}  [OK]      Mode 3 (Docker + H2)           ready${C_RESET}"
    echo -e "${C_GREEN}  [OK]      Mode 4 (Full stack Docker)     ready${C_RESET}"
    echo -e "${C_GREEN}  [OK]      Modes 6-7 (Docker stop/wipe)   ready${C_RESET}"
  else
    echo -e "${C_RED}  [BLOCKED] Mode 3 (Docker + H2)           needs Docker running${C_RESET}"
    echo -e "${C_RED}  [BLOCKED] Mode 4 (Full stack Docker)     needs Docker running${C_RESET}"
    echo -e "${C_RED}  [BLOCKED] Modes 6-7 (Docker stop/wipe)   needs Docker${C_RESET}"
  fi

  echo -e "${C_GREEN}  [OK]      Mode 5 (Stop app)              always available${C_RESET}"
  echo ""
  echo -e "${C_GRAY}  Required: Java 21, Node 20.19+/22.12+/24.x, Docker 4.x (for Docker modes).${C_RESET}"
  echo -e "${C_GRAY}  Images on first Docker use: postgres:16-alpine, redis:7-alpine, bitnami/kafka:3.9${C_RESET}"
  echo -e "${C_DYELLOW}  NOTE: App does NOT auto-detect Redis/Kafka/Postgres; the mode you pick wires them.${C_RESET}"
  echo ""
}

menu() {
  echo -e "${C_YELLOW}  MODES${C_RESET}"
  echo -e "${C_GRAY}  ------------------------------------------------------------${C_RESET}"
  echo -e "${C_WHITE}  [1] Local H2${C_RESET}"
  echo -e "${C_GRAY}      Quick dev. Host BE+FE. H2+memory. No Docker. Data resets.${C_RESET}"
  echo -e "${C_WHITE}  [2] Local + Redis/Kafka${C_RESET}"
  echo -e "${C_GRAY}      Host BE+FE. H2 DB. Redis+Kafka in Docker.${C_RESET}"
  echo -e "${C_WHITE}  [3] Docker + H2${C_RESET}"
  echo -e "${C_GRAY}      BE+FE+Redis+Kafka in Docker. H2 inside BE. No Postgres.${C_RESET}"
  echo -e "${C_WHITE}  [4] Full stack Docker${C_RESET}"
  echo -e "${C_GRAY}      BE+FE+Postgres+Redis+Kafka in Docker. Data persists.${C_RESET}"
  echo ""
  echo -e "${C_YELLOW}  CONTROL${C_RESET}"
  echo -e "${C_GRAY}  ------------------------------------------------------------${C_RESET}"
  echo -e "${C_WHITE}  [5] Stop app${C_RESET}"
  echo -e "${C_GRAY}      Stop host BE/FE and app containers. Leave infra up.${C_RESET}"
  echo -e "${C_WHITE}  [6] Stop everything${C_RESET}"
  echo -e "${C_GRAY}      Stop app + all FlagForge Docker services. Volumes kept.${C_RESET}"
  echo -e "${C_WHITE}  [7] Wipe Docker data${C_RESET}"
  echo -e "${C_GRAY}      compose down -v (DESTRUCTIVE).${C_RESET}"
  echo -e "${C_WHITE}  [0] Exit${C_RESET}"
  echo ""
}

die() {
  echo -e "${C_RED}  ERROR: $*${C_RESET}" >&2
  return 1
}

have() { command -v "$1" >/dev/null 2>&1; }

assert_java() {
  have java || die "Java not found. Install JDK 21."
  local v
  v="$(java -version 2>&1 || true)"
  if ! echo "$v" | grep -q 'version "21'; then
    echo -e "${C_YELLOW}  WARNING: Java 21 recommended. Detected:${C_RESET}"
    echo -e "${C_YELLOW}  $v${C_RESET}"
  fi
}

assert_node() {
  have node || die "Node.js not found. Install Node 20.19+ / 22.12+ / 24.x."
  have npm || die "npm not found."
}

assert_docker() {
  have docker || die "Docker not found. Install Docker Desktop / Docker Engine."
  docker info >/dev/null 2>&1 || die "Docker is not running."
}

ensure_frontend_deps() {
  if [[ ! -d "$ROOT/frontend/node_modules" ]]; then
    echo -e "${C_CYAN}  Installing frontend npm dependencies...${C_RESET}"
    (cd "$ROOT/frontend" && npm install)
  fi
}

# Kill process listening on port (macOS + Linux)
stop_port() {
  local port="$1"
  local pids=""
  if have lsof; then
    pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  elif have fuser; then
    fuser -k "${port}/tcp" 2>/dev/null || true
    return 0
  fi
  if [[ -n "$pids" ]]; then
    echo -e "${C_DYELLOW}  Stopping PID(s) on port $port: $pids${C_RESET}"
    # shellcheck disable=SC2086
    kill -9 $pids 2>/dev/null || true
  fi
}

stop_port_listeners() {
  stop_port 8080
  stop_port 5173
}

stop_app_containers() {
  have docker || return 0
  for n in flagforge-backend flagforge-frontend; do
    if docker ps -aq -f "name=$n" 2>/dev/null | grep -q .; then
      echo -e "${C_DYELLOW}  Stopping container $n...${C_RESET}"
      docker stop "$n" >/dev/null 2>&1 || true
      docker rm "$n" >/dev/null 2>&1 || true
    fi
  done
}

# Run backend in background log files under .flagforge/
LOG_DIR="$ROOT/.flagforge/logs"
mkdir -p "$LOG_DIR"

port_open() {
  local port="$1"
  if have nc; then
    nc -z 127.0.0.1 "$port" >/dev/null 2>&1 && return 0
  fi
  if have lsof; then
    lsof -ti tcp:"$port" -sTCP:LISTEN >/dev/null 2>&1 && return 0
  fi
  (echo >/dev/tcp/127.0.0.1/"$port") >/dev/null 2>&1 && return 0
  return 1
}

wait_for_port() {
  local port="$1"
  local timeout_sec="${2:-180}"
  local label="${3:-service}"
  echo -e "${C_GRAY}  Waiting for $label on port $port (up to ${timeout_sec}s)...${C_RESET}"
  local i=0
  while [[ $i -lt $timeout_sec ]]; do
    if port_open "$port"; then
      echo -e "${C_GREEN}  $label is up on port $port.${C_RESET}"
      return 0
    fi
    sleep 2
    i=$((i + 2))
  done
  return 1
}

# Failsafe: clear host + Docker app before any start mode
stop_previous_app() {
  local reason="${1:-Preparing clean start}"
  echo -e "${C_YELLOW}  [$reason] Stopping any previous FlagForge app (host + containers)...${C_RESET}"
  stop_port_listeners
  if [[ -f "$LOG_DIR/backend.pid" ]]; then
    kill -9 "$(cat "$LOG_DIR/backend.pid")" 2>/dev/null || true
    rm -f "$LOG_DIR/backend.pid"
  fi
  if [[ -f "$LOG_DIR/frontend.pid" ]]; then
    kill -9 "$(cat "$LOG_DIR/frontend.pid")" 2>/dev/null || true
    rm -f "$LOG_DIR/frontend.pid"
  fi
  stop_port_listeners
  stop_app_containers
  sleep 2
  if port_open 8080 || port_open 5173; then
    stop_port_listeners
    sleep 2
  fi
  if port_open 8080 || port_open 5173; then
    die "Could not free ports 8080/5173. Close other FlagForge/Java/Node processes and try again."
  fi
  echo -e "${C_GREEN}  Previous app cleared.${C_RESET}"
}

start_host_backend() {
  local profile_args="${1:-}"
  assert_java
  [[ -f "$ROOT/backend/gradlew" ]] || die "backend/gradlew not found"
  chmod +x "$ROOT/backend/gradlew" 2>/dev/null || true
  echo -e "${C_CYAN}  Starting backend (logs: .flagforge/logs/backend.log)...${C_RESET}"
  echo -e "${C_GRAY}  First Gradle run can take 1-2 min. If it fails: tail -f .flagforge/logs/backend.log${C_RESET}"
  (
    cd "$ROOT/backend"
    if [[ -n "$profile_args" ]]; then
      nohup ./gradlew bootRun --args="$profile_args" >"$LOG_DIR/backend.log" 2>&1 &
    else
      nohup ./gradlew bootRun >"$LOG_DIR/backend.log" 2>&1 &
    fi
    echo $! >"$LOG_DIR/backend.pid"
  )
}

start_host_frontend() {
  assert_node
  ensure_frontend_deps
  echo -e "${C_CYAN}  Starting frontend (logs: .flagforge/logs/frontend.log)...${C_RESET}"
  (
    cd "$ROOT/frontend"
    nohup npm run dev -- --host 127.0.0.1 --port 5173 >"$LOG_DIR/frontend.log" 2>&1 &
    echo $! >"$LOG_DIR/frontend.pid"
  )
}

open_browser() {
  local url="$1"
  sleep 1
  if [[ "$(uname -s)" == "Darwin" ]]; then
    open "$url" 2>/dev/null || true
  elif have xdg-open; then
    xdg-open "$url" 2>/dev/null || true
  fi
}

mode_local_h2() {
  echo ""
  echo -e "${C_GREEN}  >> Mode 1: Local H2${C_RESET}"
  stop_previous_app "Mode 1"
  start_host_backend
  if ! wait_for_port 8080 180 "backend"; then
    die "Backend did not open port 8080. Check: tail -f .flagforge/logs/backend.log"
  fi
  start_host_frontend
  if ! wait_for_port 5173 90 "frontend"; then
    echo -e "${C_YELLOW}  WARNING: Frontend not on 5173 yet. Check .flagforge/logs/frontend.log${C_RESET}"
  fi
  open_browser "http://localhost:5173"
  echo -e "${C_CYAN}  UI: http://localhost:5173  |  API: http://localhost:8080${C_RESET}"
}

mode_local_redis_kafka() {
  echo ""
  echo -e "${C_GREEN}  >> Mode 2: Local app + Redis/Kafka Docker${C_RESET}"
  assert_docker
  stop_previous_app "Mode 2"
  docker compose --profile redis-kafka up -d
  echo -e "${C_GRAY}  Waiting for Redis/Kafka...${C_RESET}"
  sleep 20
  start_host_backend "--spring.profiles.active=redis-kafka"
  if ! wait_for_port 8080 180 "backend"; then
    die "Backend did not open port 8080. Check: tail -f .flagforge/logs/backend.log"
  fi
  start_host_frontend
  if ! wait_for_port 5173 90 "frontend"; then
    echo -e "${C_YELLOW}  WARNING: Frontend not on 5173 yet. Check .flagforge/logs/frontend.log${C_RESET}"
  fi
  open_browser "http://localhost:5173"
  echo -e "${C_CYAN}  UI: http://localhost:5173  |  API: http://localhost:8080${C_RESET}"
}

mode_docker_h2() {
  echo ""
  echo -e "${C_GREEN}  >> Mode 3: Docker BE+FE + H2 + Redis + Kafka${C_RESET}"
  assert_docker
  stop_previous_app "Mode 3"
  export BACKEND_ENV_FILE="./deploy/backend-h2.env"
  docker compose --profile docker-h2 up -d --build
  echo -e "${C_GRAY}  Waiting for stack (first build can take several minutes)...${C_RESET}"
  if ! wait_for_port 8080 300 "backend container"; then
    die "Docker backend did not open 8080. Try: docker logs flagforge-backend"
  fi
  if ! wait_for_port 5173 120 "frontend container"; then
    echo -e "${C_YELLOW}  WARNING: Frontend container not on 5173. Try: docker logs flagforge-frontend${C_RESET}"
  fi
  open_browser "http://localhost:5173"
  echo -e "${C_CYAN}  UI: http://localhost:5173  |  API: http://localhost:8080${C_RESET}"
  echo -e "${C_DYELLOW}  H2 is inside the backend container - data lost when BE container stops.${C_RESET}"
}

mode_docker_full() {
  echo ""
  echo -e "${C_GREEN}  >> Mode 4: Full stack Docker (Postgres + Redis + Kafka + BE + FE)${C_RESET}"
  assert_docker
  stop_previous_app "Mode 4"
  export BACKEND_ENV_FILE="./deploy/backend-full.env"
  docker compose --profile docker-full up -d --build
  echo -e "${C_GRAY}  Waiting for Postgres and stack (first build can take several minutes)...${C_RESET}"
  sleep 15
  docker restart flagforge-backend >/dev/null 2>&1 || true
  if ! wait_for_port 8080 300 "backend container"; then
    die "Docker backend did not open 8080. Try: docker logs flagforge-backend"
  fi
  if ! wait_for_port 5173 120 "frontend container"; then
    echo -e "${C_YELLOW}  WARNING: Frontend container not on 5173. Try: docker logs flagforge-frontend${C_RESET}"
  fi
  open_browser "http://localhost:5173"
  echo -e "${C_CYAN}  UI: http://localhost:5173  |  API: http://localhost:8080${C_RESET}"
  echo -e "${C_DGREEN}  Postgres data persists in Docker volume flagforge_pg_data.${C_RESET}"
}

mode_stop_app() {
  echo ""
  echo -e "${C_GREEN}  >> Stop app (host processes + app containers)${C_RESET}"
  stop_previous_app "Stop app"
  echo -e "${C_CYAN}  App stopped. Docker infra (if any) still running.${C_RESET}"
}

mode_stop_everything() {
  echo ""
  echo -e "${C_GREEN}  >> Stop everything${C_RESET}"
  mode_stop_app
  if have docker; then
    docker compose --profile redis-kafka --profile docker-h2 --profile docker-full stop 2>/dev/null || true
    docker compose --profile redis-kafka --profile docker-h2 --profile docker-full down 2>/dev/null || true
  fi
  echo -e "${C_CYAN}  Stopped. Volumes kept (use 7 to wipe).${C_RESET}"
}

mode_wipe() {
  echo ""
  echo -e "${C_RED}  >> Wipe Docker data (DESTRUCTIVE)${C_RESET}"
  read -r -p "  Type YES to remove FlagForge Docker volumes: " confirm
  if [[ "$confirm" != "YES" ]]; then
    echo -e "${C_YELLOW}  Cancelled.${C_RESET}"
    return 0
  fi
  stop_port_listeners
  assert_docker
  docker compose --profile redis-kafka --profile docker-h2 --profile docker-full down -v
  echo -e "${C_CYAN}  Volumes removed.${C_RESET}"
}

pause_return() {
  echo ""
  read -r -p "  Press Enter to return to menu..."
}

# make gradlew executable if needed
[[ -f "$ROOT/backend/gradlew" ]] && chmod +x "$ROOT/backend/gradlew" 2>/dev/null || true

while true; do
  banner
  prerequisites
  menu
  read -r -p "  Select option: " choice
  choice="$(echo "$choice" | tr -d '[:space:]')"
  set +e
  case "$choice" in
    1) mode_local_h2 ;;
    2) mode_local_redis_kafka ;;
    3) mode_docker_h2 ;;
    4) mode_docker_full ;;
    5) mode_stop_app ;;
    6) mode_stop_everything ;;
    7) mode_wipe ;;
    0)
      echo ""
      echo -e "${C_CYAN}  Bye.${C_RESET}"
      echo ""
      exit 0
      ;;
    *) echo -e "${C_RED}  Invalid option.${C_RESET}" ;;
  esac
  set -e
  pause_return
done
