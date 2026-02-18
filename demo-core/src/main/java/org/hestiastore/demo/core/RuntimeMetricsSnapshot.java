package org.hestiastore.demo.core;

import java.util.Map;

public record RuntimeMetricsSnapshot(
        long uptimeSeconds,
        long totalOps,
        long totalGte,
        long totalPut,
        long totalDelete,
        long opsPerSecond,
        long gtePerSecond,
        long putPerSecond,
        long deletePerSecond,
        double avgLatencyMs,
        double p50LatencyMs,
        double p95LatencyMs,
        double p99LatencyMs,
        long keyCount,
        long bloomIndexSizeBytes,
        long bloomFilterHits,
        long bloomFilterMisses,
        double bloomHitRatio,
        long registryCacheHits,
        long registryCacheMisses,
        long registryCacheLoads,
        long registryCacheEvictions,
        long registryCacheCurrent,
        long registryCacheMax,
        double registryCacheHitRatio,
        double registryCacheFill,
        Map<String, Object> nodeConfig
) {
}
