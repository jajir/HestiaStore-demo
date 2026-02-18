# HestiaStore Demo - Credibility-Oriented Requirements

## 1. Purpose

The goal of this demo project is to establish technical credibility for HestiaStore as a serious embedded storage engine.

The demo must demonstrate:
- Predictable performance under sustained load
- Stability under memory pressure
- Correct persistence and restart behavior
- Clean concurrency scaling
- Strong observability
- Production-minded architecture

This is not a synthetic benchmark project.  
This is a long-running, realistic workload simulation.

## 2. Positioning

The demo positions HestiaStore as:

A high-performance, embeddable, persistent key-value engine suitable for production systems.

Comparable in architectural seriousness (not maturity) to:
- RocksDB
- LevelDB
- SQLite

The demo must feel like a real system, not an experiment.

## 3. Repository Structure

Repository name:

`hestiastore-demo`

Recommended structure:

- `demo-core` - workload + metrics abstraction
- `demo-runner` - CLI run command
- `demo-monitor` - monitoring server
- `docs/`

If single-module initially, responsibilities must still be clearly separated.

## 4. Technical Stack

### Core
- Java 21
- Maven
- HestiaStore (embedded)

### CLI
- picocli

### Metrics
- HestiaStore Monitoring API (mandatory)
- HestiaStore Monitoring Micrometer (mandatory)
- HestiaStore Monitoring Prometheus (recommended)

### Management
- HestiaStore Management API (mandatory)
- HestiaStore Management Agent (mandatory)

### Monitoring UI
- HestiaStore Monitoring Console (mandatory)
- HestiaStore Monitoring Console Web (mandatory)

### HTTP Server
- JDK built-in HTTP server (preferred for simplicity)  
or
- Undertow (lightweight alternative)

No Spring Boot in v1 (avoid unnecessary complexity).

## 5. CLI Requirements

### 5.1 Run Command

```bash
demo run \
  --dir=./data \
  --threads=16 \
  --target-keys=50_000_000 \
  --read-ratio=0.6 \
  --update-ratio=0.3 \
  --insert-ratio=0.1
```

Responsibilities:
- Initialize HestiaStore
- Start worker thread pool
- Generate workload
- Expose metrics endpoint
- Print periodic statistics
- Handle graceful shutdown (SIGTERM)
- Support restart from existing directory

### 5.2 Monitor Command

```bash
demo monitor --port=8080
```

Responsibilities:
- Connect to metrics endpoint
- Display live charts
- Display store and JVM statistics
- Display node detail with operation counters and live rates
- Refresh automatically

## 6. Data Model

### Key
- `long` (8 bytes)
- Simulates object ID

### Value

Binary structure:

```c
struct Value {
   long version;
   long timestamp;
   byte[] payload (200-2000 bytes)
}
```

Payload requirements:
- Random size within range
- Mix of compressible and non-compressible content

Purpose:
- Simulate realistic metadata store
- Create non-trivial disk behavior
- Trigger segment growth and compaction

## 7. Workload Design

### 7.1 Access Distribution

Must use Zipf distribution.

Reason:
- Real systems are not uniform
- Hot keys must exist
- Stress caching and segment heat patterns

Target pattern:
- ~20% keys receive ~80% traffic

### 7.2 Operation Mix

Default:
- 60% reads
- 30% updates
- 10% inserts

Must be configurable.

### 7.3 Worker Algorithm

Each worker thread:

```text
loop:
   select operation by weighted probability
   select key via Zipf distribution
   execute operation
   record latency metrics
```

Requirements:
- No global locks in demo layer
- No artificial throttling (unless testing backpressure)
- Clean exception handling

## 8. Concurrency Requirements

Must support configurable thread count.

Test scenarios:
- `--threads=1`
- `--threads=8`
- `--threads=32`
- `--threads=64`

Expected behavior:
- Throughput increases with threads
- Latency remains bounded
- No collapse under contention
- No deadlocks

## 9. Persistence and Restart

Non-negotiable requirement.

The demo must support:
1. Populate large dataset
2. Terminate process
3. Restart using same directory
4. Resume workload
5. Maintain data integrity
6. I want command line cli to controll whole system

On startup, console must display:

`Recovered X keys in Y seconds`

This builds production credibility.

## 10. Observability Requirements

Observability is mandatory.

No claims without metrics.

### 10.1 Required Metrics

Throughput:
- ops/sec
- reads/sec
- writes/sec
- GTE total + GTE current/sec (GET/read operation)
- PUT total + PUT current/sec
- DELETE total + DELETE current/sec

Latency:
- average
- P50
- P95
- P99

Store:
- total keys
- active segments
- compaction count
- disk usage
- Bloom Index Size (bytes) - format using the same number/unit rules as HEAP USED
- Bloom filter hits
- Bloom filter misses
- Bloom filter hit ratio
- Bloom false positive ratio (if available)
- store memory usage (if available)

JVM:
- heap used
- GC pause time
- GC count
- thread count

Configuration (from monitored app):
- all runtime/store configuration values listed in section 14 must be exported by runner JVM and shown in Node Detail with label + description

### 10.2 Console Output (Every 5 Seconds)

Example:

```text
[HestiaStore Demo]

Keys:          18,234,221
Ops/sec:       82,440
P99 latency:   3.4 ms
Heap:          410MB / 512MB
Bloom Index:   128MB
Segments:      128
Compactions:   14
Disk:          4.2GB
GC pause avg:  12 ms
```

Console must remain clean and readable.

## 11. Memory Pressure Testing

Must support running with limited heap:

`-Xmx512m --target-keys=100_000_000`

Expected behavior:
- Dataset exceeds heap
- Heap stabilizes
- No OOM
- No runaway growth
- Predictable disk expansion

This is critical for credibility.

## 12. Stability Requirements

The demo must:
- Run continuously for 12-24 hours
- Avoid OOM
- Avoid thread leaks
- Avoid deadlocks
- Avoid corruption on restart

Graceful shutdown must:
- Stop workers
- Flush store
- Close resources cleanly

## 13. Monitoring UI Requirements

Simple and professional.

Must display:
- Throughput chart
- Latency percentiles
- Heap usage over time
- Segment count
- Disk usage
- Bloom filter hits/misses and hit ratio
- Node detail panel: total GTE/PUT/DELETE counts and current GTE/PUT/DELETE values per second
- Node detail configuration panel with all fields from section 14 and human-readable descriptions
- Node detail section: Segment Registry Cache (metrics and related config values)

Refresh interval: ~1 second.

Minimalistic design is acceptable.  
Clarity and correctness are more important than aesthetics.

## 14. Node Detail Configuration Contract

All values below must be passed from monitored application (runner JVM) to monitor UI and displayed in Node Detail.

### 14.1 Transport Rules

- Source of truth is monitored app runtime configuration (not hardcoded in monitor app).
- Values must be delivered via metrics payload and/or dedicated endpoint (for example `/node/config`).
- Field names in payload should match canonical property names from application config.
- Node Detail must show:
  - field display name
  - current value
  - short description
  - unit (if applicable)
- `null` values must be shown explicitly as `not set`.
- `Class<?>` values must be shown using fully qualified class name.

### 14.2 Required Fields, Labels, and Descriptions

| Property Name | Node Detail Label | Description |
| --- | --- | --- |
| `maxNumberOfKeysInSegmentCache` | Max Keys in Segment Cache | Maximum number of keys kept in in-memory segment read cache. |
| `maxNumberOfKeysInSegmentWriteCache` | Max Keys in Segment Write Cache | Maximum number of keys buffered in segment write cache during normal operation. |
| `maxNumberOfKeysInSegmentWriteCacheDuringMaintenance` | Max Keys in Segment Write Cache (Maintenance) | Maximum write-cache key capacity while maintenance mode is active. |
| `maxNumberOfKeysInSegmentChunk` | Max Keys in Segment Chunk | Maximum number of keys per segment chunk unit. |
| `maxNumberOfDeltaCacheFiles` | Max Delta Cache Files | Maximum number of delta cache files retained. |
| `maxNumberOfKeysInSegment` | Max Keys in Segment | Upper bound of keys in a single segment. |
| `maxNumberOfSegmentsInCache` | Max Segments in Cache | Maximum number of segments held in cache simultaneously. |
| `bloomFilterNumberOfHashFunctions` | Bloom Hash Functions | Number of hash functions used by Bloom filter. |
| `bloomFilterIndexSizeInBytes` | Bloom Index Size (bytes) | Size of Bloom filter index. Format value with same unit/number style as `HEAP USED`. |
| `bloomFilterProbabilityOfFalsePositive` | Bloom False Positive Probability | Expected Bloom filter false positive probability. |
| `diskIoBufferSizeInBytes` | Disk I/O Buffer Size (bytes) | Buffer size used for disk I/O operations. |
| `indexWorkerThreadCount` | Index Worker Threads | Number of worker threads for index operations. |
| `numberOfIoThreads` | I/O Threads | Number of threads dedicated to I/O processing. |
| `numberOfSegmentIndexMaintenanceThreads` | Segment Index Maintenance Threads | Number of threads for segment-index maintenance tasks. |
| `numberOfIndexMaintenanceThreads` | Index Maintenance Threads | Number of threads for general index maintenance tasks. |
| `numberOfRegistryLifecycleThreads` | Registry Lifecycle Threads | Number of threads used for registry lifecycle operations. |
| `indexBusyBackoffMillis` | Index Busy Backoff (ms) | Backoff interval before retry when index is busy. |
| `indexBusyTimeoutMillis` | Index Busy Timeout (ms) | Timeout waiting for index busy state to clear. |
| `segmentMaintenanceAutoEnabled` | Segment Maintenance Auto Enabled | Whether automatic segment maintenance is enabled. |
| `indexName` | Index Name | Logical index identifier. |
| `keyClass` | Key Class | Java type used for key serialization/handling. |
| `valueClass` | Value Class | Java type used for value serialization/handling. |
| `keyTypeDescriptor` | Key Type Descriptor | Descriptor string for key type. |
| `valueTypeDescriptor` | Value Type Descriptor | Descriptor string for value type. |
| `contextLoggingEnabled` | Context Logging Enabled | Whether context-aware logging is enabled. |

### 14.3 Validation Rules

- Monitor app must not invent defaults when value is missing from runner.
- On load, Node Detail must verify all required properties are present and mark missing fields as `missing` with warning state.
- A compatibility version should be reported to avoid UI/backend schema drift.

### 14.4 Segment Registry Cache Section (Node Detail)

Move the following values into a dedicated Node Detail section named `Segment Registry Cache`:
- Registry Cache Hits / Misses
- Registry Cache Load / Evict
- Registry Cache Size
- Registry Cache Hit Ratio
- Registry Cache Fill

Display format requirements:
- `Registry Cache Hits / Misses`: `<hits> / <misses>`
- `Registry Cache Load / Evict`: `<loads> / <evictions>`
- `Registry Cache Size`: `<current> / <max>`
- `Registry Cache Hit Ratio`: `<ratio>%`
- `Registry Cache Fill`: `<fill>%`

Related configuration values that must be displayed in the same section:
- `maxNumberOfSegmentsInCache` (maps to `Registry Cache Size` max value)
- `numberOfRegistryLifecycleThreads` (registry cache lifecycle processing capacity)
- `segmentMaintenanceAutoEnabled` (can influence registry cache churn behavior)

## 15. Phase 2 Enhancements (Optional)

After base stability is proven:
- Backpressure simulation
- Dynamic load adjustment
- Compaction visualization
- Segment heat map
- Failure injection testing
- Vector workload mode

These are secondary to core stability.

## 16. Success Criteria

The demo is considered credible when:
- It runs 24 hours without crash
- It survives restart cleanly
- It scales with threads
- It remains stable under memory pressure
- It exposes meaningful metrics
- It does not rely on artificial benchmark tricks

Final goal:

A user should be able to run the demo and trust HestiaStore as a serious embedded storage engine.

## 17. Component and JVM Architecture

Architecture documentation is mandatory in `docs/components.plantuml` and `docs/components.png`.

### 17.1 JVM Layout

JVM #1 - Runner process:
- Main artifact: `demo-runner.jar`
- Loaded jars/modules:
  - `demo-core.jar`
  - HestiaStore engine jars
  - HestiaStore Monitoring API
  - HestiaStore Monitoring Micrometer
  - HestiaStore Monitoring Prometheus
  - HestiaStore Management API
  - HestiaStore Management Agent
  - picocli
  - JDK HTTP server (or Undertow)
- Responsibilities:
  - Execute workload workers
  - Execute GTE/PUT/DELETE operations
  - Persist/recover data in `--dir`
  - Collect and expose metrics on `/metrics`
  - Print periodic console statistics

JVM #2 - Monitor process:
- Main artifact: `demo-monitor.jar`
- Loaded jars/modules:
  - HestiaStore Monitoring Console
  - HestiaStore Monitoring Console Web
- Responsibilities:
  - Pull runner metrics endpoint
  - Pull runner management endpoint (if enabled)
  - Render live monitoring UI (including Node Detail and Bloom metrics)
  - Serve dashboard HTTP endpoint for browser access

### 17.2 Connections

Must be represented in the diagram:
- Runner JVM -> local data directory (read/write persistence)
- Monitor JVM -> Runner JVM `/metrics` (HTTP pull)
- Monitor JVM -> Runner JVM management endpoint (HTTP)
- Browser -> Monitor JVM dashboard endpoint (HTTP)
- Optional: Prometheus -> Runner JVM `/metrics` (HTTP scrape)

### 17.3 Diagram Artifacts

- Source: `docs/components.plantuml`
- Rendered image: `docs/components.png`

![HestiaStore component architecture](./components.png)
