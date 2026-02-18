package org.hestiastore.demo.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hestiastore.demo.core.DemoConfiguration;
import org.hestiastore.demo.core.DemoMetrics;
import org.hestiastore.demo.core.InMemoryStore;
import org.hestiastore.demo.core.RuntimeMetricsSnapshot;
import org.hestiastore.demo.core.ValueRecord;
import org.hestiastore.demo.core.WorkloadEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(name = "run", description = "Run workload and expose monitoring endpoints")
public final class RunCommand implements Runnable {
    @Option(names = "--dir", required = true)
    private Path directory;

    @Option(names = "--threads", defaultValue = "16")
    private int threads;

    @Option(names = "--target-keys", defaultValue = "50000000")
    private long targetKeys;

    @Option(names = "--gte-ratio", defaultValue = "0.6")
    private double gteRatio;

    @Option(names = "--put-ratio", defaultValue = "0.3")
    private double putRatio;

    @Option(names = "--delete-ratio", defaultValue = "0.1")
    private double deleteRatio;

    @Option(names = "--metrics-port", defaultValue = "9090")
    private int metricsPort;

    @Override
    public void run() {
        ObjectMapper objectMapper = new ObjectMapper();
        RunnerStateStore stateStore = new RunnerStateStore(objectMapper);
        RunnerState state = stateStore.load(directory);

        long startRecoverNanos = System.nanoTime();
        InMemoryStore store = new InMemoryStore(state.nextKey());
        seedRecoveredData(store, state.recoveredKeys());
        long recoverMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startRecoverNanos);
        System.out.printf("Recovered %,d keys in %.3f seconds%n", state.recoveredKeys(), recoverMillis / 1000.0d);

        DemoConfiguration configuration = DemoConfiguration.defaults(threads, targetKeys, gteRatio, putRatio, deleteRatio);
        configuration.validateRatios();

        DemoMetrics metrics = new DemoMetrics(configuration);
        WorkloadEngine engine = new WorkloadEngine(configuration, store, metrics);
        RunnerServer runnerServer = new RunnerServer(metricsPort, objectMapper);
        ConsoleStatsPrinter printer = new ConsoleStatsPrinter();

        AtomicBoolean shutdownRequested = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownRequested.set(true);
            engine.stop();
            runnerServer.stop();
            try {
                stateStore.save(directory, new RunnerState(store.size(), store.nextKey()));
            } catch (Exception ignored) {
                // Best-effort state persistence on shutdown.
            }
        }));

        try {
            runnerServer.start();
            engine.start();

            long nextConsolePrint = System.currentTimeMillis();
            while (!shutdownRequested.get()) {
                RuntimeMetricsSnapshot snapshot = engine.snapshotAndResetWindow();
                runnerServer.updateSnapshot(snapshot);

                if (System.currentTimeMillis() >= nextConsolePrint) {
                    printer.print(snapshot);
                    nextConsolePrint = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
                }
                Thread.sleep(1000L);
            }
        } catch (Exception e) {
            throw new RuntimeException("Runner failed", e);
        } finally {
            engine.stop();
            runnerServer.stop();
            try {
                stateStore.save(directory, new RunnerState(store.size(), store.nextKey()));
            } catch (Exception ignored) {
                // Ignore on final shutdown path.
            }
        }
    }

    private void seedRecoveredData(InMemoryStore store, long recoveredKeys) {
        long toSeed = Math.min(10_000L, recoveredKeys);
        for (long key = 0; key < toSeed; key++) {
            store.seed(key, new ValueRecord(1L, System.currentTimeMillis(), new byte[256]));
        }
    }
}
