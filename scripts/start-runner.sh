#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

DATA_DIR="${DATA_DIR:-$REPO_DIR/data}"
THREADS="${THREADS:-16}"
TARGET_KEYS="${TARGET_KEYS:-50000000}"
GTE_RATIO="${GTE_RATIO:-0.6}"
PUT_RATIO="${PUT_RATIO:-0.3}"
DELETE_RATIO="${DELETE_RATIO:-0.1}"
METRICS_PORT="${METRICS_PORT:-9090}"

if [[ $# -gt 0 ]]; then
  case "$1" in
    --dir=*)
      DATA_DIR="${1#--dir=}"
      shift
      ;;
    --dir)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --dir" >&2
        exit 1
      fi
      DATA_DIR="$2"
      shift 2
      ;;
    -*)
      echo "Unknown argument: $1" >&2
      echo "Usage: $0 [DATA_DIR] [--dir=PATH]" >&2
      exit 1
      ;;
    *)
      DATA_DIR="$1"
      shift
      ;;
  esac
fi

cd "$REPO_DIR"

if [[ ! -f "demo-runner/target/demo-runner-0.1.0-SNAPSHOT.jar" ]]; then
  echo "Runner jar not found. Building project first..."
  "$SCRIPT_DIR/build.sh"
fi

exec java -jar demo-runner/target/demo-runner-0.1.0-SNAPSHOT.jar run \
  --dir="$DATA_DIR" \
  --threads="$THREADS" \
  --target-keys="$TARGET_KEYS" \
  --gte-ratio="$GTE_RATIO" \
  --put-ratio="$PUT_RATIO" \
  --delete-ratio="$DELETE_RATIO" \
  --metrics-port="$METRICS_PORT"
