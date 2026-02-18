package org.hestiastore.demo.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WorkloadEngine {
    private final DemoConfiguration configuration;
    private final InMemoryStore store;
    private final PayloadGenerator payloadGenerator;
    private final ZipfLikeKeySelector keySelector;
    private final DemoMetrics metrics;

    private final ExecutorService executor;
    private final AtomicBoolean running;

    public WorkloadEngine(DemoConfiguration configuration, InMemoryStore store, DemoMetrics metrics) {
        this.configuration = configuration;
        this.store = store;
        this.metrics = metrics;
        this.payloadGenerator = new PayloadGenerator();
        this.keySelector = new ZipfLikeKeySelector(2.5d);
        this.executor = Executors.newFixedThreadPool(configuration.threads());
        this.running = new AtomicBoolean(false);
    }

    public void start() {
        running.set(true);
        for (int i = 0; i < configuration.threads(); i++) {
            executor.submit(this::workerLoop);
        }
    }

    public void stop() {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public RuntimeMetricsSnapshot snapshotAndResetWindow() {
        metrics.setKeyCount(store.size());
        return metrics.snapshotAndResetWindow();
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                OperationType op = chooseOperation();
                long startNanos = System.nanoTime();
                boolean bloomHit = false;
                boolean registryHit = false;

                switch (op) {
                    case GTE -> {
                        if (!store.isEmpty()) {
                            long key = keySelector.nextKey(Math.max(1L, store.nextKey()));
                            ValueRecord record = store.get(key);
                            bloomHit = record != null;
                            registryHit = ThreadLocalRandom.current().nextInt(100) < 97;
                        }
                    }
                    case PUT -> {
                        long key = store.allocateKey();
                        store.put(key, new ValueRecord(1L, System.currentTimeMillis(), payloadGenerator.generate()));
                        bloomHit = true;
                        registryHit = true;
                    }
                    case DELETE -> {
                        if (!store.isEmpty()) {
                            long key = keySelector.nextKey(Math.max(1L, store.nextKey()));
                            ValueRecord deleted = store.delete(key);
                            bloomHit = deleted != null;
                            registryHit = ThreadLocalRandom.current().nextInt(100) < 95;
                        }
                    }
                }

                long latencyMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos);
                metrics.record(op, latencyMicros, bloomHit, registryHit);
            } catch (Exception ignored) {
                // Keep workers alive while recording long-running workload characteristics.
            }
        }
    }

    private OperationType chooseOperation() {
        double value = ThreadLocalRandom.current().nextDouble();
        if (value < configuration.gteRatio()) {
            return OperationType.GTE;
        }
        if (value < configuration.gteRatio() + configuration.putRatio()) {
            return OperationType.PUT;
        }
        return OperationType.DELETE;
    }
}
