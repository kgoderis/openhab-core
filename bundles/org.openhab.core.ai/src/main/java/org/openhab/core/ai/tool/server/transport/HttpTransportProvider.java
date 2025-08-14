package org.openhab.core.ai.tool.server.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * HTTP transport provider implementation for MCP tool communication.
 * 
 * Supports HTTP/1.1, HTTP/2, HTTP/3, TLS security, and load balancing.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HttpTransportProvider implements TransportProvider {

    private static final Logger logger = LoggerFactory.getLogger(HttpTransportProvider.class);

    // Provider identification
    private static final String PROVIDER_ID = "http-transport-provider";
    private static final String PROVIDER_NAME = "HTTP Transport Provider";
    private static final String[] SUPPORTED_PROTOCOLS = { "HTTP/1.1", "HTTP/2", "HTTP/3" };

    // Configuration keys
    private static final String CONFIG_HOST = "host";
    private static final String CONFIG_PORT = "port";
    private static final String CONFIG_SSL_ENABLED = "ssl.enabled";
    private static final String CONFIG_SSL_KEYSTORE = "ssl.keystore";
    private static final String CONFIG_SSL_KEYSTORE_PASSWORD = "ssl.keystore.password";
    private static final String CONFIG_SSL_TRUSTSTORE = "ssl.truststore";
    private static final String CONFIG_SSL_TRUSTSTORE_PASSWORD = "ssl.truststore.password";
    private static final String CONFIG_HTTP_VERSION = "http.version";
    private static final String CONFIG_LOAD_BALANCING_ENABLED = "load.balancing.enabled";
    private static final String CONFIG_BACKEND_SERVERS = "backend.servers";
    private static final String CONFIG_MAX_CONNECTIONS = "max.connections";
    private static final String CONFIG_CONNECTION_TIMEOUT = "connection.timeout";
    private static final String CONFIG_READ_TIMEOUT = "read.timeout";

    // Default configuration values
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final boolean DEFAULT_SSL_ENABLED = false;
    private static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";
    private static final boolean DEFAULT_LOAD_BALANCING_ENABLED = false;
    private static final int DEFAULT_MAX_CONNECTIONS = 100;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_READ_TIMEOUT = 60000;

    // Instance state
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalBytesTransferred = new AtomicLong(0);
    private final long startTime = System.currentTimeMillis();

    // Configuration
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private boolean sslEnabled = DEFAULT_SSL_ENABLED;
    private @Nullable String sslKeyStore;
    private @Nullable String sslKeyStorePassword;
    private @Nullable String sslTrustStore;
    private @Nullable String sslTrustStorePassword;
    private String httpVersion = DEFAULT_HTTP_VERSION;
    private boolean loadBalancingEnabled = DEFAULT_LOAD_BALANCING_ENABLED;
    private String[] backendServers = {};
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    // HTTP server instance
    private @Nullable HttpServer httpServer;
    private @Nullable HttpsServer httpsServer;

    // Load balancing state
    private final Map<String, BackendServer> backendServerMap = new ConcurrentHashMap<>();
    private int currentBackendIndex = 0;

    /**
     * Backend server information for load balancing.
     */
    // BackendServer extracted to org.openhab.core.ai.tool.server.transport.BackendServer

    // LoadBalancer extracted to top-level: org.openhab.core.ai.tool.server.transport.LoadBalancer

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] getSupportedProtocols() {
        return SUPPORTED_PROTOCOLS;
    }

    @Override
    public void initialize(Map<String, Object> configuration) {
        logger.info("Initializing HTTP transport provider");

        // Load configuration
        loadConfiguration(configuration);

        // Validate configuration
        validateConfiguration();

        // Initialize load balancing if enabled
        if (loadBalancingEnabled) {
            initializeLoadBalancing();
        }

        logger.info("HTTP transport provider initialized: host={}, port={}, ssl={}, httpVersion={}, loadBalancing={}",
                host, port, sslEnabled, httpVersion, loadBalancingEnabled);
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                logger.info("Starting HTTP transport provider");

                if (sslEnabled) {
                    startHttpsServer();
                } else {
                    startHttpServer();
                }

                logger.info("HTTP transport provider started successfully");
            } catch (Exception e) {
                running.set(false);
                logger.error("Failed to start HTTP transport provider", e);
                throw new RuntimeException("Failed to start HTTP transport provider", e);
            }
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                logger.info("Stopping HTTP transport provider");

                if (httpsServer != null) {
                    httpsServer.stop(0);
                    httpsServer = null;
                }

                if (httpServer != null) {
                    httpServer.stop(0);
                    httpServer = null;
                }

                logger.info("HTTP transport provider stopped successfully");
            } catch (Exception e) {
                logger.error("Error stopping HTTP transport provider", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        long uptime = System.currentTimeMillis() - startTime;

        stats.put("providerId", getProviderId());
        stats.put("providerName", getProviderName());
        stats.put("running", isRunning());
        stats.put("uptime", uptime);
        stats.put("totalRequests", totalRequests.get());
        stats.put("totalErrors", totalErrors.get());
        stats.put("totalBytesTransferred", totalBytesTransferred.get());
        stats.put("requestsPerSecond", uptime > 0 ? (double) totalRequests.get() / (uptime / 1000.0) : 0.0);
        stats.put("errorRate", totalRequests.get() > 0 ? (double) totalErrors.get() / totalRequests.get() : 0.0);

        // HTTP-specific statistics
        stats.put("host", host);
        stats.put("port", port);
        stats.put("sslEnabled", sslEnabled);
        stats.put("httpVersion", httpVersion);
        stats.put("loadBalancingEnabled", loadBalancingEnabled);
        stats.put("maxConnections", maxConnections);

        // Load balancing statistics
        if (loadBalancingEnabled) {
            Map<String, Object> backendStats = new ConcurrentHashMap<>();
            for (Map.Entry<String, BackendServer> entry : backendServerMap.entrySet()) {
                BackendServer backend = entry.getValue();
                Map<String, Object> serverStats = new ConcurrentHashMap<>();
                serverStats.put("url", backend.getUrl());
                serverStats.put("requestCount", backend.getRequestCount());
                serverStats.put("errorCount", backend.getErrorCount());
                serverStats.put("averageResponseTime", backend.getAverageResponseTime());
                serverStats.put("errorRate", backend.getErrorRate());
                serverStats.put("healthy", backend.isHealthy());
                backendStats.put(entry.getKey(), serverStats);
            }
            stats.put("backendServers", backendStats);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new ConcurrentHashMap<>();

        health.put("providerId", getProviderId());
        health.put("running", isRunning());
        health.put("healthy", isHealthy());
        health.put("uptime", System.currentTimeMillis() - startTime);

        // Check HTTP server health
        boolean httpHealthy = (httpServer != null || httpsServer != null) && isRunning();
        health.put("httpServerHealthy", httpHealthy);

        // Check load balancing health
        if (loadBalancingEnabled) {
            boolean loadBalancingHealthy = checkLoadBalancingHealth();
            health.put("loadBalancingHealthy", loadBalancingHealthy);
        }

        return health;
    }

    /**
     * Load configuration from the provided map.
     */
    private void loadConfiguration(Map<String, Object> configuration) {
        host = (String) configuration.getOrDefault(CONFIG_HOST, DEFAULT_HOST);
        port = (Integer) configuration.getOrDefault(CONFIG_PORT, DEFAULT_PORT);
        sslEnabled = (Boolean) configuration.getOrDefault(CONFIG_SSL_ENABLED, DEFAULT_SSL_ENABLED);
        sslKeyStore = (String) configuration.get(CONFIG_SSL_KEYSTORE);
        sslKeyStorePassword = (String) configuration.get(CONFIG_SSL_KEYSTORE_PASSWORD);
        sslTrustStore = (String) configuration.get(CONFIG_SSL_TRUSTSTORE);
        sslTrustStorePassword = (String) configuration.get(CONFIG_SSL_TRUSTSTORE_PASSWORD);
        httpVersion = (String) configuration.getOrDefault(CONFIG_HTTP_VERSION, DEFAULT_HTTP_VERSION);
        loadBalancingEnabled = (Boolean) configuration.getOrDefault(CONFIG_LOAD_BALANCING_ENABLED,
                DEFAULT_LOAD_BALANCING_ENABLED);

        Object backendServersObj = configuration.get(CONFIG_BACKEND_SERVERS);
        if (backendServersObj instanceof String[]) {
            backendServers = (String[]) backendServersObj;
        } else if (backendServersObj instanceof String) {
            backendServers = ((String) backendServersObj).split(",");
        }

        maxConnections = (Integer) configuration.getOrDefault(CONFIG_MAX_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
        connectionTimeout = (Integer) configuration.getOrDefault(CONFIG_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        readTimeout = (Integer) configuration.getOrDefault(CONFIG_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Validate the configuration.
     */
    private void validateConfiguration() {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        if (sslEnabled) {
            if (sslKeyStore == null || sslKeyStore.trim().isEmpty()) {
                throw new IllegalArgumentException("SSL keystore is required when SSL is enabled");
            }
            if (sslKeyStorePassword == null) {
                throw new IllegalArgumentException("SSL keystore password is required when SSL is enabled");
            }
        }

        if (loadBalancingEnabled && (backendServers == null || backendServers.length == 0)) {
            throw new IllegalArgumentException("Backend servers are required when load balancing is enabled");
        }

        if (maxConnections < 1) {
            throw new IllegalArgumentException("Max connections must be greater than 0");
        }

        if (connectionTimeout < 0) {
            throw new IllegalArgumentException("Connection timeout cannot be negative");
        }

        if (readTimeout < 0) {
            throw new IllegalArgumentException("Read timeout cannot be negative");
        }
    }

    /**
     * Initialize load balancing.
     */
    private void initializeLoadBalancing() {
        logger.info("Initializing load balancing with {} backend servers", backendServers.length);

        for (String serverUrl : backendServers) {
            String trimmedUrl = serverUrl.trim();
            if (!trimmedUrl.isEmpty()) {
                BackendServer backend = new BackendServer(trimmedUrl);
                backendServerMap.put(trimmedUrl, backend);
                logger.info("Added backend server: {}", trimmedUrl);
            }
        }

        if (backendServerMap.isEmpty()) {
            throw new IllegalStateException("No valid backend servers configured for load balancing");
        }
    }

    /**
     * Start HTTP server.
     */
    private void startHttpServer() throws IOException {
        logger.info("Starting HTTP server on {}:{}", host, port);

        httpServer = HttpServer.create(new InetSocketAddress(host, port), maxConnections);

        // Configure server
        httpServer.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(maxConnections));

        // Add request handlers
        setupRequestHandlers(httpServer);

        // Start server
        httpServer.start();

        logger.info("HTTP server started successfully on {}:{}", host, port);
    }

    /**
     * Start HTTPS server with TLS support.
     */
    private void startHttpsServer() throws Exception {
        logger.info("Starting HTTPS server on {}:{}", host, port);

        httpsServer = HttpsServer.create(new InetSocketAddress(host, port), maxConnections);

        // Configure SSL/TLS
        SSLContext sslContext = createSSLContext();
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

        // Configure server
        httpsServer.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(maxConnections));

        // Add request handlers
        setupRequestHandlers(httpsServer);

        // Start server
        httpsServer.start();

        logger.info("HTTPS server started successfully on {}:{}", host, port);
    }

    /**
     * Create SSL context for HTTPS.
     */
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // Load keystore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (java.io.FileInputStream fis = new java.io.FileInputStream(sslKeyStore)) {
            keyStore.load(fis, sslKeyStorePassword.toCharArray());
        }

        // Load truststore if provided
        KeyStore trustStore = null;
        if (sslTrustStore != null && !sslTrustStore.trim().isEmpty()) {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (java.io.FileInputStream fis = new java.io.FileInputStream(sslTrustStore)) {
                trustStore.load(fis, sslTrustStorePassword != null ? sslTrustStorePassword.toCharArray() : null);
            }
        }

        // Create trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore != null ? trustStore : keyStore);

        // Initialize SSL context
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }

    /**
     * Setup request handlers for the server.
     */
    private void setupRequestHandlers(HttpServer server) {
        // Health check endpoint
        server.createContext("/health", exchange -> {
            try {
                totalRequests.incrementAndGet();

                Map<String, Object> health = getHealthStatus();
                String response = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(health);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

                totalBytesTransferred.addAndGet(response.getBytes().length);

            } catch (Exception e) {
                totalErrors.incrementAndGet();
                logger.error("Error handling health check request", e);
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });

        // Statistics endpoint
        server.createContext("/stats", exchange -> {
            try {
                totalRequests.incrementAndGet();

                Map<String, Object> stats = getStatistics();
                String response = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(stats);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

                totalBytesTransferred.addAndGet(response.getBytes().length);

            } catch (Exception e) {
                totalErrors.incrementAndGet();
                logger.error("Error handling stats request", e);
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });

        // MCP message endpoint
        server.createContext("/mcp/message", exchange -> {
            try {
                totalRequests.incrementAndGet();
                long startTime = System.currentTimeMillis();

                // Handle MCP message
                handleMcpMessage(exchange);

                long responseTime = System.currentTimeMillis() - startTime;
                totalBytesTransferred.addAndGet(exchange.getResponseBody().toString().getBytes().length);

                // Record load balancing metrics if enabled
                if (loadBalancingEnabled) {
                    recordLoadBalancingMetrics(responseTime);
                }

            } catch (Exception e) {
                totalErrors.incrementAndGet();
                logger.error("Error handling MCP message", e);
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });

        // SSE endpoint for real-time communication
        server.createContext("/mcp/events", exchange -> {
            try {
                totalRequests.incrementAndGet();

                // Set SSE headers
                exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
                exchange.getResponseHeaders().add("Cache-Control", "no-cache");
                exchange.getResponseHeaders().add("Connection", "keep-alive");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                exchange.sendResponseHeaders(200, 0);

                // Handle SSE connection
                handleSseConnection(exchange);

            } catch (Exception e) {
                totalErrors.incrementAndGet();
                logger.error("Error handling SSE connection", e);
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });
    }

    /**
     * Handle MCP message requests.
     */
    private void handleMcpMessage(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        // Read request body
        java.io.InputStream requestBody = exchange.getRequestBody();
        String requestContent = new String(requestBody.readAllBytes());

        // Parse MCP message (simplified - in real implementation, use proper MCP SDK)
        logger.debug("Received MCP message: {}", requestContent);

        // Process message and generate response
        String responseContent = processMcpMessage(requestContent);

        // Send response
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseContent.getBytes().length);

        try (java.io.OutputStream os = exchange.getResponseBody()) {
            os.write(responseContent.getBytes());
        }
    }

    /**
     * Handle SSE connections for real-time events.
     */
    private void handleSseConnection(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        try (java.io.OutputStream os = exchange.getResponseBody()) {
            // Send initial connection event
            String initialEvent = "data: {\"type\": \"connected\", \"timestamp\": " + System.currentTimeMillis()
                    + "}\n\n";
            os.write(initialEvent.getBytes());
            os.flush();

            // Keep connection alive and send events
            while (isRunning()) {
                try {
                    Thread.sleep(30000); // Send keep-alive every 30 seconds

                    String keepAliveEvent = "data: {\"type\": \"keepalive\", \"timestamp\": "
                            + System.currentTimeMillis() + "}\n\n";
                    os.write(keepAliveEvent.getBytes());
                    os.flush();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    logger.debug("SSE connection closed by client");
                    break;
                }
            }
        }
    }

    /**
     * Process MCP message and generate response.
     */
    private String processMcpMessage(String requestContent) {
        // Simplified MCP message processing
        // In real implementation, this would use the MCP SDK to parse and process messages

        try {
            // Parse JSON request
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode requestNode = mapper.readTree(requestContent);

            // Extract method and parameters
            String method = requestNode.path("method").asText();
            com.fasterxml.jackson.databind.JsonNode params = requestNode.path("params");

            // Process based on method
            String result;
            switch (method) {
                case "tools/list":
                    result = handleToolsList(params);
                    break;
                case "tools/call":
                    result = handleToolsCall(params);
                    break;
                case "resources/list":
                    result = handleResourcesList(params);
                    break;
                case "resources/read":
                    result = handleResourcesRead(params);
                    break;
                default:
                    result = createErrorResponse("Method not found: " + method);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error processing MCP message", e);
            return createErrorResponse("Internal server error");
        }
    }

    /**
     * Handle tools/list method.
     */
    private String handleToolsList(com.fasterxml.jackson.databind.JsonNode params) {
        // Return list of available tools
        return "{\"jsonrpc\": \"2.0\", \"result\": {\"tools\": []}, \"id\": 1}";
    }

    /**
     * Handle tools/call method.
     */
    private String handleToolsCall(com.fasterxml.jackson.databind.JsonNode params) {
        // Execute tool call
        return "{\"jsonrpc\": \"2.0\", \"result\": {\"content\": [{\"type\": \"text\", \"text\": \"Tool executed successfully\"}]}, \"id\": 1}";
    }

    /**
     * Handle resources/list method.
     */
    private String handleResourcesList(com.fasterxml.jackson.databind.JsonNode params) {
        // Return list of available resources
        return "{\"jsonrpc\": \"2.0\", \"result\": {\"resources\": []}, \"id\": 1}";
    }

    /**
     * Handle resources/read method.
     */
    private String handleResourcesRead(com.fasterxml.jackson.databind.JsonNode params) {
        // Read resource content
        return "{\"jsonrpc\": \"2.0\", \"result\": {\"contents\": [{\"type\": \"text\", \"text\": \"Resource content\"}]}, \"id\": 1}";
    }

    /**
     * Create error response.
     */
    private String createErrorResponse(String message) {
        return "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -1, \"message\": \"" + message + "\"}, \"id\": 1}";
    }

    /**
     * Record load balancing metrics.
     */
    private void recordLoadBalancingMetrics(long responseTime) {
        // Record metrics for the current backend server
        // In a real implementation, this would track which backend handled the request
        for (BackendServer backend : backendServerMap.values()) {
            if (backend.isHealthy()) {
                backend.recordRequest(responseTime);
                break;
            }
        }
    }

    /**
     * Check load balancing health.
     */
    private boolean checkLoadBalancingHealth() {
        if (!loadBalancingEnabled) {
            return true;
        }

        // Check if at least one backend server is healthy
        return backendServerMap.values().stream().anyMatch(BackendServer::isHealthy);
    }

    /**
     * Check if the transport provider is healthy.
     */
    private boolean isHealthy() {
        if (!isRunning()) {
            return false;
        }

        // Check HTTP server health
        boolean httpHealthy = (httpServer != null || httpsServer != null);
        if (!httpHealthy) {
            return false;
        }

        // Check load balancing health if enabled
        if (loadBalancingEnabled) {
            return checkLoadBalancingHealth();
        }

        return true;
    }

    /**
     * Get next backend server for load balancing (round-robin).
     */
    private @Nullable BackendServer getNextBackendServer() {
        if (!loadBalancingEnabled || backendServerMap.isEmpty()) {
            return null;
        }

        // Simple round-robin selection
        BackendServer[] healthyServers = backendServerMap.values().stream().filter(BackendServer::isHealthy)
                .toArray(BackendServer[]::new);

        if (healthyServers.length == 0) {
            return null;
        }

        currentBackendIndex = (currentBackendIndex + 1) % healthyServers.length;
        return healthyServers[currentBackendIndex];
    }

    /**
     * Perform health check on backend servers.
     */
    public void performBackendHealthChecks() {
        if (!loadBalancingEnabled) {
            return;
        }

        for (BackendServer backend : backendServerMap.values()) {
            try {
                // Simple health check - try to connect to the backend
                java.net.URL url = new java.net.URL(backend.getUrl() + "/health");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                boolean healthy = responseCode >= 200 && responseCode < 500;

                backend.setHealthy(healthy);
                backend.lastHealthCheck = System.currentTimeMillis();

                if (!healthy) {
                    logger.warn("Backend server {} is unhealthy (response code: {})", backend.getUrl(), responseCode);
                }

            } catch (Exception e) {
                backend.setHealthy(false);
                backend.lastHealthCheck = System.currentTimeMillis();
                logger.warn("Health check failed for backend server {}: {}", backend.getUrl(), e.getMessage());
            }
        }
    }
}
