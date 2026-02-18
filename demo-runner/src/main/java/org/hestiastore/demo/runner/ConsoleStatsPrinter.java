package org.hestiastore.demo.runner;

import org.hestiastore.demo.core.RuntimeMetricsSnapshot;

public final class ConsoleStatsPrinter {
    public void print(RuntimeMetricsSnapshot snapshot) {
        System.out.println("[HestiaStore Demo]");
        System.out.printf("Keys:          %,d%n", snapshot.keyCount());
        System.out.printf("Ops/sec:       %,d%n", snapshot.opsPerSecond());
        System.out.printf("P99 latency:   %.3f ms%n", snapshot.p99LatencyMs());
        System.out.printf("Bloom Index:   %s%n", humanReadableBytes(snapshot.bloomIndexSizeBytes()));
        System.out.printf("Bloom Hit %%:   %.2f%%%n", snapshot.bloomHitRatio());
        System.out.printf("Registry Hit %%: %.2f%%%n", snapshot.registryCacheHitRatio());
        System.out.printf("Registry Fill: %.2f%%%n", snapshot.registryCacheFill());
        System.out.println();
    }

    public static String humanReadableBytes(long bytes) {
        if (bytes < 1024L) {
            return bytes + "B";
        }
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        while (value >= 1024.0d && unitIndex < units.length - 1) {
            value /= 1024.0d;
            unitIndex++;
        }
        return String.format("%.1f%s", value, units[unitIndex]);
    }
}
