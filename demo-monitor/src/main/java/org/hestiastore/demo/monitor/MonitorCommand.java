package org.hestiastore.demo.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hestiastore.console.MonitoringConsoleServer;
import org.hestiastore.console.web.MonitoringConsoleWebApplication;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Command(name = "monitor", description = "Run monitor dashboard")
public final class MonitorCommand implements Runnable {
    @Option(names = "--port", defaultValue = "8081")
    private int webPort;

    @Option(names = "--backend-port", defaultValue = "8085")
    private int backendPort;

    @Option(names = "--target", defaultValue = "http://localhost:9090")
    private String target;

    @Option(names = "--write-token", defaultValue = "")
    private String writeToken;

    @Option(names = "--node-id", defaultValue = "")
    private String nodeId;

    @Option(names = "--node-name", defaultValue = "HestiaStore Demo Node")
    private String nodeName;

    @Override
    public void run() {
        try {
            MonitoringConsoleServer backend = new MonitoringConsoleServer("127.0.0.1", backendPort, writeToken);
            Runtime.getRuntime().addShutdownHook(new Thread(backend::close));
            backend.start();

            String resolvedNodeId = nodeId == null || nodeId.isBlank()
                    ? "demo-" + UUID.randomUUID().toString().substring(0, 8)
                    : nodeId;

            registerNode(resolvedNodeId, nodeName, target);

            System.setProperty("server.port", Integer.toString(webPort));
            System.setProperty("hestia.console.web.backend-base-url", "http://127.0.0.1:" + backendPort);
            System.setProperty("hestia.console.web.write-token", writeToken);
            System.setProperty("hestia.console.web.refresh-millis", "1000");

            System.out.printf("Monitoring Console backend: http://127.0.0.1:%d%n", backendPort);
            System.out.printf("Monitoring Console Web: http://127.0.0.1:%d%n", webPort);
            System.out.printf("Registered node: id=%s name=%s target=%s%n", resolvedNodeId, nodeName, target);

            MonitoringConsoleWebApplication.main(new String[0]);
        } catch (Exception e) {
            throw new RuntimeException("Monitor startup failed", e);
        }
    }

    private void registerNode(String resolvedNodeId, String resolvedNodeName, String targetBaseUrl) throws Exception {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("nodeId", resolvedNodeId);
        body.put("nodeName", resolvedNodeName);
        body.put("baseUrl", trimTrailingSlash(targetBaseUrl));
        body.put("agentToken", "");

        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + backendPort + "/console/v1/nodes"))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .header("X-Hestia-Console-Token", writeToken == null ? "" : writeToken)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Node registration failed: HTTP " + response.statusCode() + " body=" + response.body());
        }
    }

    private String trimTrailingSlash(String input) {
        if (input == null) {
            return "";
        }
        if (input.endsWith("/")) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }
}
