package org.hestiastore.demo.core;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class DemoMetrics {
    private static final int LATENCY_WINDOW_SIZE = 10_000;

    private final DemoConfiguration configuration;
    private final long startedAtMillis;

    private final AtomicLong totalOps = new AtomicLong();
    private final AtomicLong totalGte = new AtomicLong();
    private final AtomicLong totalPut = new AtomicLong();
    private final AtomicLong totalDelete = new AtomicLong();

    private final AtomicLong windowOps = new AtomicLong();
    private final AtomicLong windowGte = new AtomicLong();
    private final AtomicLong windowPut = new AtomicLong();
    private final AtomicLong windowDelete = new AtomicLong();

    private final AtomicLong bloomHits = new AtomicLong();
    private final AtomicLong bloomMisses = new AtomicLong();

    private final AtomicLong registryCacheHits = new AtomicLong();
    private final AtomicLong registryCacheMisses = new AtomicLong();
    private final AtomicLong registryCacheLoads = new AtomicLong();
    private final AtomicLong registryCacheEvictions = new AtomicLong();

    private final AtomicLong keyCount = new AtomicLong();

    private final ConcurrentLinkedQueue<Long> latencyMicrosWindow = new ConcurrentLinkedQueue<>();

    public DemoMetrics(DemoConfiguration configuration) {
        this.configuration = configuration;
        this.startedAtMillis = System.currentTimeMillis();
    }

    public void record(OperationType type, long latencyMicros, boolean bloomHit, boolean registryHit) {
        totalOps.incrementAndGet();
        windowOps.incrementAndGet();
        switch (type) {
            case GTE -> {
                totalGte.incrementAndGet();
                windowGte.incrementAndGet();
            }
            case PUT -> {
                totalPut.incrementAndGet();
                windowPut.incrementAndGet();
            }
            case DELETE -> {
                totalDelete.incrementAndGet();
                windowDelete.incrementAndGet();
            }
        }

        if (bloomHit) {
            bloomHits.incrementAndGet();
        } else {
            bloomMisses.incrementAndGet();
        }

        if (registryHit) {
            registryCacheHits.incrementAndGet();
        } else {
            registryCacheMisses.incrementAndGet();
            registryCacheLoads.incrementAndGet();
        }

        latencyMicrosWindow.add(latencyMicros);
        while (latencyMicrosWindow.size() > LATENCY_WINDOW_SIZE) {
            latencyMicrosWindow.poll();
        }
    }

    public void setKeyCount(long keys) {
        keyCount.set(keys);
    }

    public RuntimeMetricsSnapshot snapshotAndResetWindow() {
        long opsPerSecond = windowOps.getAndSet(0);
        long gtePerSecond = windowGte.getAndSet(0);
        long putPerSecond = windowPut.getAndSet(0);
        long deletePerSecond = windowDelete.getAndSet(0);

        double[] latencyStats = latencyStats();

        long hits = bloomHits.get();
        long misses = bloomMisses.get();
        long bloomTotal = Math.max(1L, hits + misses);
        double bloomHitRatio = hits * 100.0d / bloomTotal;

        long rcHits = registryCacheHits.get();
        long rcMisses = registryCacheMisses.get();
        long rcTotal = Math.max(1L, rcHits + rcMisses);
        double registryHitRatio = rcHits * 100.0d / rcTotal;

        long rcMax = configuration.toNodeConfigMap().get("maxNumberOfSegmentsInCache") instanceof Integer value ? value.longValue() : 1L;
        long rcCurrent = Math.min(rcMax, Math.max(1L, keyCount.get() / 100_000L));
        double rcFill = Math.min(100.0d, (rcCurrent * 100.0d) / Math.max(1L, rcMax));

        return new RuntimeMetricsSnapshot(
                Math.max(1L, (System.currentTimeMillis() - startedAtMillis) / 1000L),
                totalOps.get(),
                totalGte.get(),
                totalPut.get(),
                totalDelete.get(),
                opsPerSecond,
                gtePerSecond,
                putPerSecond,
                deletePerSecond,
                latencyStats[0],
                latencyStats[1],
                latencyStats[2],
                latencyStats[3],
                keyCount.get(),
                (Integer) configuration.toNodeConfigMap().get("bloomFilterIndexSizeInBytes"),
                hits,
                misses,
                bloomHitRatio,
                rcHits,
                rcMisses,
                registryCacheLoads.get(),
                registryCacheEvictions.get(),
                rcCurrent,
                rcMax,
                registryHitRatio,
                rcFill,
                configuration.toNodeConfigMap()
        );
    }

    private double[] latencyStats() {
        if (latencyMicrosWindow.isEmpty()) {
            return new double[] {0.0d, 0.0d, 0.0d, 0.0d};
        }

        long[] values = latencyMicrosWindow.stream().mapToLong(Long::longValue).sorted().toArray();
        double avgMicros = java.util.Arrays.stream(values).average().orElse(0.0d);
        double p50 = percentile(values, 0.50d);
        double p95 = percentile(values, 0.95d);
        double p99 = percentile(values, 0.99d);
        return new double[] {avgMicros / 1000.0d, p50 / 1000.0d, p95 / 1000.0d, p99 / 1000.0d};
    }

    private double percentile(long[] values, double percentile) {
        if (values.length == 0) {
            return 0.0d;
        }
        int index = (int) Math.ceil(percentile * values.length) - 1;
        index = Math.max(0, Math.min(values.length - 1, index));
        return values[index];
    }

    public Map<String, Object> nodeConfig() {
        return configuration.toNodeConfigMap();
    }
}
