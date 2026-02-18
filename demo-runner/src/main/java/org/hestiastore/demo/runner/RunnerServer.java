package org.hestiastore.demo.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.hestiastore.management.api.ActionResponse;
import org.hestiastore.management.api.ActionRequest;
import org.hestiastore.management.api.ActionStatus;
import org.hestiastore.management.api.ActionType;
import org.hestiastore.management.api.MetricsResponse;
import org.hestiastore.management.api.NodeStateResponse;
import org.hestiastore.demo.core.RuntimeMetricsSnapshot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class RunnerServer {
    private final int port;
    private final ObjectMapper objectMapper;

    private final PrometheusMeterRegistry meterRegistry;
    private final AtomicLong opsPerSecond = new AtomicLong();
    private final AtomicLong gtePerSecond = new AtomicLong();
    private final AtomicLong putPerSecond = new AtomicLong();
    private final AtomicLong deletePerSecond = new AtomicLong();
    private final AtomicLong keyCount = new AtomicLong();

    private RuntimeMetricsSnapshot latestSnapshot;
    private HttpServer httpServer;

    public RunnerServer(int port, ObjectMapper objectMapper) {
        this.port = port;
        this.objectMapper = objectMapper;
        this.meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);

        Gauge.builder("hestia_demo_ops_per_sec", opsPerSecond, AtomicLong::doubleValue).register(meterRegistry);
        Gauge.builder("hestia_demo_gte_per_sec", gtePerSecond, AtomicLong::doubleValue).register(meterRegistry);
        Gauge.builder("hestia_demo_put_per_sec", putPerSecond, AtomicLong::doubleValue).register(meterRegistry);
        Gauge.builder("hestia_demo_delete_per_sec", deletePerSecond, AtomicLong::doubleValue).register(meterRegistry);
        Gauge.builder("hestia_demo_key_count", keyCount, AtomicLong::doubleValue).register(meterRegistry);
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/metrics", this::handleMetrics);
        httpServer.createContext("/node/detail", this::handleNodeDetail);
        httpServer.createContext("/node/config", this::handleNodeConfig);
        httpServer.createContext("/api/v1/state", this::handleApiState);
        httpServer.createContext("/api/v1/metrics", this::handleApiMetrics);
        httpServer.createContext("/api/v1/actions/flush", exchange -> handleAction(exchange, ActionType.FLUSH));
        httpServer.createContext("/api/v1/actions/compact", exchange -> handleAction(exchange, ActionType.COMPACT));
        httpServer.start();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    public void updateSnapshot(RuntimeMetricsSnapshot snapshot) {
        latestSnapshot = snapshot;
        opsPerSecond.set(snapshot.opsPerSecond());
        gtePerSecond.set(snapshot.gtePerSecond());
        putPerSecond.set(snapshot.putPerSecond());
        deletePerSecond.set(snapshot.deletePerSecond());
        keyCount.set(snapshot.keyCount());
    }

    private void handleMetrics(HttpExchange exchange) throws IOException {
        byte[] body = meterRegistry.scrape().getBytes(StandardCharsets.UTF_8);
        writeResponse(exchange, 200, "text/plain; charset=utf-8", body);
    }

    private void handleNodeDetail(HttpExchange exchange) throws IOException {
        if (latestSnapshot == null) {
            writeResponse(exchange, 503, "application/json", "{\"status\":\"warming_up\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] body = objectMapper.writeValueAsBytes(latestSnapshot);
        writeResponse(exchange, 200, "application/json", body);
    }

    private void handleNodeConfig(HttpExchange exchange) throws IOException {
        if (latestSnapshot == null) {
            writeResponse(exchange, 503, "application/json", "{\"status\":\"warming_up\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", "1.0.0");
        payload.put("values", latestSnapshot.nodeConfig());
        payload.put("descriptions", NodeConfigMetadata.descriptions());
        byte[] body = objectMapper.writeValueAsBytes(payload);
        writeResponse(exchange, 200, "application/json", body);
    }

    private void handleApiState(HttpExchange exchange) throws IOException {
        NodeStateResponse stateResponse = new NodeStateResponse(
                indexName(),
                latestSnapshot == null ? "STARTING" : "RUNNING",
                latestSnapshot != null,
                Instant.now()
        );
        byte[] body = objectMapper.writeValueAsBytes(stateResponse);
        writeResponse(exchange, 200, "application/json", body);
    }

    private void handleApiMetrics(HttpExchange exchange) throws IOException {
        RuntimeMetricsSnapshot snapshot = latestSnapshot;
        MetricsResponse response = new MetricsResponse(
                indexName(),
                snapshot == null ? "STARTING" : "RUNNING",
                snapshot == null ? 0L : snapshot.totalGte(),
                snapshot == null ? 0L : snapshot.totalPut(),
                snapshot == null ? 0L : snapshot.totalDelete(),
                Instant.now()
        );
        byte[] body = objectMapper.writeValueAsBytes(response);
        writeResponse(exchange, 200, "application/json", body);
    }

    private void handleAction(HttpExchange exchange, ActionType actionType) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "application/json", "{\"code\":\"METHOD_NOT_ALLOWED\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }

        ActionRequest actionRequest;
        try {
            actionRequest = objectMapper.readValue(exchange.getRequestBody(), ActionRequest.class);
        } catch (Exception e) {
            actionRequest = new ActionRequest(UUID.randomUUID().toString());
        }

        ActionResponse response = new ActionResponse(
                actionRequest.requestId(),
                actionType,
                ActionStatus.COMPLETED,
                "Completed in demo mode.",
                Instant.now()
        );
        byte[] body = objectMapper.writeValueAsBytes(response);
        writeResponse(exchange, 200, "application/json", body);
    }

    private String indexName() {
        if (latestSnapshot != null) {
            Object value = latestSnapshot.nodeConfig().get("indexName");
            if (value != null) {
                return value.toString();
            }
        }
        return "hestia-demo-index";
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
