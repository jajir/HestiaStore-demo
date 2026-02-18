# HestiaStore-demo
Realistic workload simulations and observability toolkit for HestiaStore embedded engine.

## Goal
Build a credibility-oriented demo that proves HestiaStore is a serious embedded, persistent key-value engine by demonstrating:
- stable long-running behavior (12-24h),
- clean restart and data recovery,
- predictable performance under sustained load,
- bounded latency under concurrency,
- stability under memory pressure,
- production-grade observability.

## Documentation
- `docs/demo_requirements.md` - complete demo requirements and success criteria.
- `docs/steps.md` - step-by-step execution plan to deliver the demo.
- `docs/components.plantuml` - component diagram source (PlantUML).
- `docs/components.png` - rendered component architecture image.

## Current Implementation (v0)
- Multi-module Maven project:
  - `demo-core`
  - `demo-runner`
  - `demo-monitor`
- Runner CLI:
  - workload engine with configurable `GTE/PUT/DELETE` ratios
  - restart state persistence (`runner-state.json`)
  - endpoints: `/metrics`, `/node/detail`, `/node/config`
- Monitor CLI/Web:
  - runs official `monitoring-console` backend
  - runs official `monitoring-console-web`
  - auto-registers runner node in console backend

## Build
```bash
mvn clean package
```

## Scripts
- `/Users/jan/projects/HestiaStore-demo/scripts/build.sh`
  - builds all modules (`mvn clean package`)
- `/Users/jan/projects/HestiaStore-demo/scripts/start-runner.sh`
  - starts runner (`demo run`) with env-overridable settings
- `/Users/jan/projects/HestiaStore-demo/scripts/start-monitor.sh`
  - starts monitor (`demo monitor`) against runner target

Usage:
```bash
./scripts/build.sh
./scripts/start-runner.sh
./scripts/start-monitor.sh
```

Runner data directory options:
```bash
./scripts/start-runner.sh /tmp/hestia-data
./scripts/start-runner.sh --dir=/tmp/hestia-data
DATA_DIR=/tmp/hestia-data ./scripts/start-runner.sh
```

Optional env overrides:
```bash
THREADS=32 METRICS_PORT=9191 ./scripts/start-runner.sh
PORT=8090 BACKEND_PORT=8086 TARGET=http://localhost:9191 ./scripts/start-monitor.sh
```

## Run Runner
```bash
java -jar demo-runner/target/demo-runner-0.1.0-SNAPSHOT.jar run \
  --dir=./data \
  --threads=16 \
  --target-keys=50000000 \
  --gte-ratio=0.6 \
  --put-ratio=0.3 \
  --delete-ratio=0.1 \
  --metrics-port=9090
```

## Run Monitor
```bash
java -jar demo-monitor/target/demo-monitor-0.1.0-SNAPSHOT.jar monitor \
  --port=8080 \
  --backend-port=8085 \
  --target=http://localhost:9090
```

Open `http://localhost:8080`.
