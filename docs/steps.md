# HestiaStore Demo - Implementation Steps

## 1. Foundation Setup
1. Set Java to 21 and configure Maven compiler/toolchain.
2. Create package structure separating core workload, CLI runner, and monitoring concerns.
3. Add dependencies:
   - picocli
   - HestiaStore Monitoring API
   - HestiaStore Monitoring Micrometer
   - HestiaStore Monitoring Prometheus
   - HestiaStore Management API
   - HestiaStore Management Agent
   - HestiaStore Monitoring Console
   - HestiaStore Monitoring Console Web
4. Add config object for runtime parameters (`dir`, `threads`, `targetKeys`, operation ratios).

## 2. Core Domain and Workload
1. Implement key/value model (`long` key + binary value with version, timestamp, payload).
2. Implement payload generator (200-2000 bytes, mix compressible and random bytes).
3. Implement operation chooser with weighted read/update/insert probabilities.
4. Integrate Zipf key selection to create hot/cold access skew.
5. Implement worker loop with clean exception handling and no global lock contention in demo code.

## 3. CLI Commands
1. Implement `demo run` command with required flags and validation (ratios sum to 1.0).
2. Initialize or reopen store from `--dir` and print recovery line on startup:
   `Recovered X keys in Y seconds`.
3. Start worker pool using configured thread count.
4. Implement `demo monitor --port=8080` command skeleton and endpoint connectivity.

## 4. Metrics and Observability
1. Register required monitoring metrics via HestiaStore Monitoring API and its Micrometer bridge.
2. Add JVM metrics (heap, GC, thread count) using Micrometer binders.
3. Add store metrics (keys, segments, compactions, disk usage, memory usage if available).
4. Add operation metrics for `GTE` (`GET`/read), `PUT`, `DELETE`: total counters and current per-second rates.
5. Include `Bloom Index Size (bytes)` metric and format it with the same human-readable number rules as `HEAP USED`.
6. Add Bloom filter metrics: hits, misses, hit ratio, and false-positive ratio (if available from store APIs).
7. Expose Prometheus metrics endpoint via HestiaStore Monitoring Prometheus integration.
8. Implement readable console report every 5 seconds with key indicators.

## 5. Persistence and Lifecycle
1. Implement graceful shutdown hook (SIGTERM): stop workers, flush/close store, close server.
2. Ensure restart on existing data directory resumes workload without reinitializing data.
3. Add integrity check path for startup/restart validation.

## 6. Monitoring UI
1. Build monitor UI using HestiaStore Monitoring Console + Monitoring Console Web components; refresh ~1 second.
2. Show charts/panels for throughput, latency percentiles, heap, segments, and disk.
3. Add a node detail section showing total and current/sec values for `GTE`, `PUT`, and `DELETE`.
4. Add Bloom filter panel showing hits, misses, and hit ratio.
5. Add Node Detail configuration panel using the field contract in `docs/demo_requirements.md` section 14 (label + value + description + unit).
6. Add `Segment Registry Cache` subsection in Node Detail and place these lines there:
   - `Registry Cache Hits / Misses`
   - `Registry Cache Load / Evict`
   - `Registry Cache Size`
   - `Registry Cache Hit Ratio`
   - `Registry Cache Fill`
7. In the same section, display related config values:
   - `maxNumberOfSegmentsInCache`
   - `numberOfRegistryLifecycleThreads`
   - `segmentMaintenanceAutoEnabled`
8. Keep UI simple and stable; prioritize correctness over visual polish.

## 6A. Configuration Propagation (Runner -> Monitor)
1. Export runtime configuration from monitored app via HestiaStore Management API (for example `/node/config`).
2. Bind export to the same config object used to construct the index (prefer wiring at/near SegmentIndexBuilder usage point).
3. Keep payload field names aligned with canonical Java property names.
4. Add schema/version field for compatibility checks between Management API and Monitoring Console Web.
5. Add startup validation in monitor app:
   - verify all required fields are present
   - display explicit warning for missing fields
6. Ensure `bloomFilterIndexSizeInBytes` uses the same display formatter as `HEAP USED`.

## 7. Verification Scenarios
1. Concurrency scaling runs: `--threads=1`, `8`, `32`, `64`.
2. Memory pressure run: `-Xmx512m --target-keys=100_000_000`.
3. Restart scenario: populate -> stop -> restart same dir -> verify recovery and continuity.
4. Long soak run: 12-24 hours with periodic metric snapshots.

## 8. Acceptance Gates
1. No OOM, deadlock, thread leak, or corruption in soak/restart tests.
2. Throughput scales with threads and latency remains bounded.
3. Metrics endpoint and console output remain available throughout runs.
4. Node Detail shows all required configuration fields with correct descriptions and units.
5. Final demo script is reproducible from clean checkout.

## 9. Suggested Delivery Milestones
1. Milestone 1: CLI + workload skeleton + basic metrics endpoint.
2. Milestone 2: Zipf workload + full metric set + console stats.
3. Milestone 3: restart correctness + graceful shutdown + monitor UI.
4. Milestone 4: memory pressure + concurrency + 24h stability proof.

## 10. Architecture Documentation Deliverables
1. Define JVM/process boundaries:
   - Runner JVM (`demo-runner.jar`)
   - Monitor JVM (`demo-monitor.jar`)
2. List jars/modules per JVM and responsibilities in docs.
3. Add component diagram source file: `docs/components.plantuml`.
4. Render and commit image file: `docs/components.png`.
   - Render command: `plantuml -tpng docs/components.plantuml`
5. Ensure diagram shows connections:
   - Monitor -> Runner `/metrics`
   - Monitor -> Runner management endpoint
   - Browser -> Monitor dashboard
   - Runner -> local data directory
   - optional Prometheus -> Runner `/metrics`
