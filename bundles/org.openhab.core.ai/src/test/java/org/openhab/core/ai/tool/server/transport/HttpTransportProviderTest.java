package org.openhab.core.ai.tool.server.transport;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Test class for HttpTransportProvider.
 * 
 * Tests HTTP transport functionality including HTTP/1.1, HTTP/2, HTTP/3 support,
 * TLS security, and load balancing capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class HttpTransportProviderTest {

    private HttpTransportProvider transportProvider;
    private static final int TEST_PORT = 8081;

    @BeforeEach
    void setUp() {
        transportProvider = new HttpTransportProvider();
    }

    @AfterEach
    void tearDown() {
        if (transportProvider != null && transportProvider.isRunning()) {
            transportProvider.stop();
        }
    }

    @Test
    void testProviderIdentification() {
        assertEquals("http-transport-provider", transportProvider.getProviderId());
        assertEquals("HTTP Transport Provider", transportProvider.getProviderName());

        String[] protocols = transportProvider.getSupportedProtocols();
        assertNotNull(protocols);
        assertEquals(3, protocols.length);
        assertArrayEquals(new String[] { "HTTP/1.1", "HTTP/2", "HTTP/3" }, protocols);
    }

    @Test
    void testInitializationWithDefaultConfiguration() {
        Map<String, Object> config = new HashMap<>();

        assertDoesNotThrow(() -> transportProvider.initialize(config));

        Map<String, Object> stats = transportProvider.getStatistics();
        assertEquals("localhost", stats.get("host"));
        assertEquals(8080, stats.get("port"));
        assertEquals(false, stats.get("sslEnabled"));
        assertEquals("HTTP/1.1", stats.get("httpVersion"));
        assertEquals(false, stats.get("loadBalancingEnabled"));
    }

    @Test
    void testInitializationWithCustomConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "127.0.0.1");
        config.put("port", TEST_PORT);
        config.put("ssl.enabled", false);
        config.put("http.version", "HTTP/2");
        config.put("load.balancing.enabled", true);
        config.put("backend.servers", new String[] { "http://localhost:8082", "http://localhost:8083" });
        config.put("max.connections", 50);
        config.put("connection.timeout", 15000);
        config.put("read.timeout", 30000);

        assertDoesNotThrow(() -> transportProvider.initialize(config));

        Map<String, Object> stats = transportProvider.getStatistics();
        assertEquals("127.0.0.1", stats.get("host"));
        assertEquals(TEST_PORT, stats.get("port"));
        assertEquals(false, stats.get("sslEnabled"));
        assertEquals("HTTP/2", stats.get("httpVersion"));
        assertEquals(true, stats.get("loadBalancingEnabled"));
        assertEquals(50, stats.get("maxConnections"));
    }

    @Test
    void testInitializationWithInvalidConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 99999); // Invalid port

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }

    @Test
    void testInitializationWithSSLConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("ssl.enabled", true);
        config.put("ssl.keystore", "/path/to/keystore.jks");
        config.put("ssl.keystore.password", "password");

        // TODO: This test should be updated when SSL validation is implemented
        // For now, the stub implementation doesn't validate SSL configuration
        assertDoesNotThrow(() -> transportProvider.initialize(config));

        Map<String, Object> stats = transportProvider.getStatistics();
        assertEquals(true, stats.get("sslEnabled"));
    }

    @Test
    void testInitializationWithLoadBalancingButNoBackends() {
        Map<String, Object> config = new HashMap<>();
        config.put("load.balancing.enabled", true);
        // No backend servers configured

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStartAndStop() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);

        // Should not be running initially
        assertFalse(transportProvider.isRunning());

        // Start the provider
        assertDoesNotThrow(() -> transportProvider.start());
        assertTrue(transportProvider.isRunning());

        // Stop the provider
        assertDoesNotThrow(() -> transportProvider.stop());
        assertFalse(transportProvider.isRunning());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHealthEndpoint() throws IOException, InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);
        transportProvider.start();

        // Wait a moment for server to start
        Thread.sleep(1000);

        // Test health endpoint
        URL healthUrl = new URL("http://localhost:" + TEST_PORT + "/health");
        HttpURLConnection connection = (HttpURLConnection) healthUrl.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
        assertEquals("application/json", connection.getContentType());

        // Verify response contains expected fields
        String response = new String(connection.getInputStream().readAllBytes());
        assertTrue(response.contains("providerId"));
        assertTrue(response.contains("running"));
        assertTrue(response.contains("healthy"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStatsEndpoint() throws IOException, InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);
        transportProvider.start();

        // Wait a moment for server to start
        Thread.sleep(1000);

        // Test stats endpoint
        URL statsUrl = new URL("http://localhost:" + TEST_PORT + "/stats");
        HttpURLConnection connection = (HttpURLConnection) statsUrl.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
        assertEquals("application/json", connection.getContentType());

        // Verify response contains expected fields
        String response = new String(connection.getInputStream().readAllBytes());
        assertTrue(response.contains("providerId"));
        assertTrue(response.contains("totalRequests"));
        assertTrue(response.contains("totalErrors"));
        assertTrue(response.contains("uptime"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMcpMessageEndpoint() throws IOException, InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);
        transportProvider.start();

        // Wait a moment for server to start
        Thread.sleep(1000);

        // Test MCP message endpoint
        URL messageUrl = new URL("http://localhost:" + TEST_PORT + "/mcp/message");
        HttpURLConnection connection = (HttpURLConnection) messageUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send a test MCP message
        String testMessage = "{\"jsonrpc\": \"2.0\", \"method\": \"tools/list\", \"params\": {}, \"id\": 1}";
        connection.getOutputStream().write(testMessage.getBytes());

        assertEquals(200, connection.getResponseCode());
        assertEquals("application/json", connection.getContentType());

        // Verify response is valid JSON-RPC
        String response = new String(connection.getInputStream().readAllBytes());
        assertTrue(response.contains("jsonrpc"));
        assertTrue(response.contains("result"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSseEndpoint() throws IOException, InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);
        transportProvider.start();

        // Wait a moment for server to start
        Thread.sleep(1000);

        // Test SSE endpoint
        URL sseUrl = new URL("http://localhost:" + TEST_PORT + "/mcp/events");
        HttpURLConnection connection = (HttpURLConnection) sseUrl.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
        assertEquals("text/event-stream", connection.getContentType());
        assertEquals("no-cache", connection.getHeaderField("Cache-Control"));
        assertEquals("keep-alive", connection.getHeaderField("Connection"));
    }

    @Test
    void testStatistics() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);

        Map<String, Object> stats = transportProvider.getStatistics();

        // Verify basic statistics
        assertEquals("http-transport-provider", stats.get("providerId"));
        assertEquals("HTTP Transport Provider", stats.get("providerName"));
        assertEquals(false, stats.get("running"));
        assertNotNull(stats.get("uptime"));
        assertEquals(0L, stats.get("totalRequests"));
        assertEquals(0L, stats.get("totalErrors"));
        assertEquals(0L, stats.get("totalBytesTransferred"));

        // Verify HTTP-specific statistics
        assertEquals("localhost", stats.get("host"));
        assertEquals(TEST_PORT, stats.get("port"));
        assertEquals(false, stats.get("sslEnabled"));
        assertEquals("HTTP/1.1", stats.get("httpVersion"));
        assertEquals(false, stats.get("loadBalancingEnabled"));
        assertEquals(100, stats.get("maxConnections"));
    }

    @Test
    void testHealthStatus() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);

        Map<String, Object> health = transportProvider.getHealthStatus();

        // Verify health status
        assertEquals("http-transport-provider", health.get("providerId"));
        assertEquals(false, health.get("running"));
        assertEquals(false, health.get("healthy"));
        assertNotNull(health.get("uptime"));
        assertEquals(false, health.get("httpServerHealthy"));
    }

    @Test
    void testLoadBalancingStatistics() {
        Map<String, Object> config = new HashMap<>();
        config.put("load.balancing.enabled", true);
        config.put("backend.servers", new String[] { "http://localhost:8082", "http://localhost:8083" });
        transportProvider.initialize(config);

        Map<String, Object> stats = transportProvider.getStatistics();

        // Verify load balancing statistics
        assertEquals(true, stats.get("loadBalancingEnabled"));

        @SuppressWarnings("unchecked")
        Map<String, Object> backendStats = (Map<String, Object>) stats.get("backendServers");
        assertNotNull(backendStats);
        assertEquals(2, backendStats.size());

        // Verify each backend server has required statistics
        for (Object backendStat : backendStats.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> serverStat = (Map<String, Object>) backendStat;
            assertNotNull(serverStat.get("url"));
            assertEquals(0L, serverStat.get("requestCount"));
            assertEquals(0L, serverStat.get("errorCount"));
            assertEquals(0.0, serverStat.get("averageResponseTime"));
            assertEquals(0.0, serverStat.get("errorRate"));
            assertEquals(true, serverStat.get("healthy"));
        }
    }

    @Test
    void testLoadBalancingHealthStatus() {
        Map<String, Object> config = new HashMap<>();
        config.put("load.balancing.enabled", true);
        config.put("backend.servers", new String[] { "http://localhost:8082", "http://localhost:8083" });
        transportProvider.initialize(config);

        Map<String, Object> health = transportProvider.getHealthStatus();

        // Verify load balancing health
        assertEquals(true, health.get("loadBalancingHealthy"));
    }

    @Test
    void testMultipleStartStopCycles() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);

        // Multiple start/stop cycles should work
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> transportProvider.start());
            assertTrue(transportProvider.isRunning());

            assertDoesNotThrow(() -> transportProvider.stop());
            assertFalse(transportProvider.isRunning());
        }
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("port", TEST_PORT);
        transportProvider.initialize(config);

        // Test concurrent access to statistics and health
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    transportProvider.getStatistics();
                    transportProvider.getHealthStatus();
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Should not throw any exceptions
        assertDoesNotThrow(() -> transportProvider.getStatistics());
        assertDoesNotThrow(() -> transportProvider.getHealthStatus());
    }

    @Test
    void testInvalidPortConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", -1); // Invalid port

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }

    @Test
    void testInvalidHostConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", ""); // Empty host

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }

    @Test
    void testInvalidMaxConnectionsConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("max.connections", 0); // Invalid max connections

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }

    @Test
    void testInvalidTimeoutConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("connection.timeout", -1); // Invalid timeout

        assertThrows(IllegalArgumentException.class, () -> transportProvider.initialize(config));
    }
}
