#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

PORT="${PORT:-8080}"
TARGET="${TARGET:-http://localhost:9090}"
BACKEND_PORT="${BACKEND_PORT:-8085}"
WRITE_TOKEN="${WRITE_TOKEN:-}"
NODE_ID="${NODE_ID:-}"
NODE_NAME="${NODE_NAME:-HestiaStore Demo Node}"

cd "$REPO_DIR"

if [[ ! -f "demo-monitor/target/demo-monitor-0.1.0-SNAPSHOT.jar" ]]; then
  echo "Monitor jar not found. Building project first..."
  "$SCRIPT_DIR/build.sh"
fi

exec java -jar demo-monitor/target/demo-monitor-0.1.0-SNAPSHOT.jar monitor \
  --port="$PORT" \
  --backend-port="$BACKEND_PORT" \
  --target="$TARGET" \
  --write-token="$WRITE_TOKEN" \
  --node-id="$NODE_ID" \
  --node-name="$NODE_NAME"
