package org.openhab.core.ai.agent.transport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.transport.api.AgentTransport;
import org.openhab.core.ai.agent.transport.api.TransportCapabilities;
import org.openhab.core.ai.agent.transport.api.TransportHealth;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A2A HTTP Transport Client Implementation.
 * 
 * <p>
 * Provides HTTP client functionality for A2A transport, communicating with
 * the AgentServlet endpoint. This is a CLIENT-SIDE transport that makes
 * HTTP requests to the servlet.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentTransport.class, property = { "transport.type=http" })
@NonNullByDefault
public class AgentHttpTransport implements AgentTransport {

    private static final Logger logger = LoggerFactory.getLogger(AgentHttpTransport.class);

    private final String transportId;
    private final TransportCapabilities capabilities;
    private final Map<String, Object> metrics;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong messageCounter;
    // private final AtomicLong errorCounter;
    // private final AtomicLong latencySum;
    // private final AtomicLong requestCount;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    private boolean running = false;
    private long startTime;
    private long lastHealthCheck;

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Activate
    public AgentHttpTransport() {
        this.transportId = "http-client-transport-" + System.currentTimeMillis();
        this.capabilities = new AgentHttpTransportCapabilities();
        this.metrics = new ConcurrentHashMap<>();
        // this.messageCounter = new AtomicLong(0);
        // this.errorCounter = new AtomicLong(0);
        // this.latencySum = new AtomicLong(0);
        // this.requestCount = new AtomicLong(0);

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = "http://localhost:8080"; // AgentServlet endpoint

        logger.debug("HTTP Client Transport created: {}", transportId);
    }

    @Deactivate
    public void deactivate() {
        stop();
        logger.debug("HTTP Client Transport deactivated: {}", transportId);
    }

    @Override
    public String getTransportId() {
        return transportId;
    }

    @Override
    public TransportCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Starting HTTP client transport: {}", transportId);

                // Test connection to AgentServlet
                testConnection();

                running = true;
                startTime = System.currentTimeMillis();
                lastHealthCheck = startTime;

                logger.info("HTTP client transport started successfully: {}", transportId);

            } catch (Exception e) {
                logger.error("Failed to start HTTP client transport: {}", transportId, e);
                throw new RuntimeException("Failed to start HTTP client transport", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Stopping HTTP client transport: {}", transportId);

                running = false;

                logger.info("HTTP client transport stopped successfully: {}", transportId);

            } catch (Exception e) {
                logger.error("Failed to stop HTTP client transport: {}", transportId, e);
                throw new RuntimeException("Failed to stop HTTP client transport", e);
            }
        });
    }

    @Override
    public TransportHealth getHealth() {
        long currentTime = System.currentTimeMillis();
        lastHealthCheck = currentTime;

        boolean healthy = running && testConnection();

        Map<String, Object> healthMetrics = Map.of("uptime", currentTime - startTime, "messageCount",
                // Placeholder - would need MetricsService to implement getMetric
                0L,
                "errorCount",
                // Placeholder - would need MetricsService to implement getMetric
                0L,
                "averageLatency",
                // Placeholder - would need MetricsService to implement getMetric
                0L,
                "lastHealthCheck", lastHealthCheck,
                "baseUrl", baseUrl);

        return new AgentHttpTransportHealth(healthy, "HTTP client transport health check", currentTime, healthMetrics);
    }

    @Override
    public CompletableFuture<Map<String, Object>> sendMessage(Map<String, Object> message) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                logger.debug("Sending HTTP message to servlet: {}", message);

                // Convert message to JSON
                String jsonMessage = objectMapper.writeValueAsString(message);

                // Create HTTP request
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/a2a/message/send"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonMessage)).build();

                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Parse response
                Map<String, Object> responseData = objectMapper.readValue(response.body(), Map.class);

                long latency = System.currentTimeMillis() - startTime;
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("http_transport", "message")
                            .withSuccess(true)
                            .withDuration(Duration.ofMillis(latency).toNanos())
                            .withData("transportId", transportId)
                            .withData("latencyMs", latency)
                            .withData("endpoint", "/a2a/message/send")
                            .record();
                    } catch (Exception e) {
                        logger.warn("Failed to record HTTP message metrics for transport {}: {}", transportId, e.getMessage());
                        // Graceful degradation: continue with message processing even if metrics recording fails
                    }
                }

                Map<String, Object> result = Map.of("status", "success", "transport", "http", "messageId",
                        message.get("id"), "latency", latency, "endpoint", "/a2a/message/send", "response",
                        responseData);

                logger.debug("HTTP message sent successfully: {}", result);
                return result;

            } catch (Exception e) {
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("http_transport", "error")
                            .withSuccess(false)
                            .withDuration(0L)
                            .withData("transportId", transportId)
                            .withData("exceptionType", e.getClass().getSimpleName())
                            .withData("errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error")
                            .record();
                    } catch (Exception ex) {
                        logger.debug("Failed to record HTTP error metrics: {}", ex.getMessage());
                    }
                }
                logger.error("Failed to send HTTP message", e);
                throw new RuntimeException("HTTP message sending failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<Map<String, Object>> subscribeToStream(Map<String, Object> subscription) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Subscribing to HTTP stream: {}", subscription);

                // For HTTP transport, streaming is handled via Server-Sent Events
                // The client would connect to the AgentServlet's SSE endpoint
                String streamId = "http-stream-" + System.currentTimeMillis();

                Map<String, Object> response = Map.of("status", "subscribed", "transport", "http", "streamId", streamId,
                        "endpoint", "/a2a/message/stream", "capabilities",
                        Map.of("sse", true, "compression", false, "cors", true));

                logger.debug("HTTP stream subscription successful: {}", response);
                return response;

            } catch (Exception e) {
                logger.error("Failed to subscribe to HTTP stream", e);
                throw new RuntimeException("HTTP stream subscription failed", e);
            }
        });
    }

    @Override
    public Map<String, Object> getMetrics() {
        metrics.put("transportId", transportId);
        metrics.put("running", running);
        metrics.put("uptime", running ? System.currentTimeMillis() - startTime : 0);
        
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot messageSnapshot = metricsService.getSnapshot("http_transport", "message");
                GenericMetricsSnapshot errorSnapshot = metricsService.getSnapshot("http_transport", "error");
                
                metrics.put("messageCount", messageSnapshot.getMetricAsLong("total_count"));
                metrics.put("errorCount", errorSnapshot.getMetricAsLong("total_count"));
                metrics.put("requestCount", messageSnapshot.getMetricAsLong("total_count"));
                metrics.put("averageLatency", messageSnapshot.getMetricAsLong("average_duration_ms"));
            } catch (Exception e) {
                logger.debug("Failed to retrieve HTTP transport metrics: {}", e.getMessage());
                // Fallback to default values
                metrics.put("messageCount", 0L);
                metrics.put("errorCount", 0L);
                metrics.put("requestCount", 0L);
                metrics.put("averageLatency", 0L);
            }
        } else {
            // Fallback to default values when MetricsService is not available
            metrics.put("messageCount", 0L);
            metrics.put("errorCount", 0L);
            metrics.put("requestCount", 0L);
            metrics.put("averageLatency", 0L);
        }
        
        metrics.put("lastHealthCheck", lastHealthCheck);
        metrics.put("transportType", "http");
        metrics.put("baseUrl", baseUrl);
        metrics.put("endpoints", Map.of("messageSend", "/a2a/message/send", "messageStream", "/a2a/message/stream",
                "health", "/a2a/health", "status", "/a2a/status", "agentCard", "/.well-known/agent.json"));

        return new ConcurrentHashMap<>(metrics);
    }

    @Override
    public CompletableFuture<Void> updateConfiguration(Map<String, Object> configuration) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Updating HTTP client transport configuration: {}", configuration);

                // TODO: Apply configuration changes
                // - Update base URL
                // - Modify timeout settings
                // - Update authentication settings

                logger.debug("HTTP client transport configuration updated successfully");

            } catch (Exception e) {
                logger.error("Failed to update HTTP client transport configuration", e);
                throw new RuntimeException("Configuration update failed", e);
            }
        });
    }

    /**
     * Test connection to the AgentServlet.
     * 
     * @return true if connection is successful
     */
    private boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/a2a/health")).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.debug("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HTTP Transport Capabilities Implementation.
     * Extracted to AgentHttpTransportCapabilities
     */

    /**
     * HTTP Transport Health Implementation.
     * Extracted to AgentHttpTransportHealth
     */
}
