package org.openhab.core.ai.common.stub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comprehensive stub framework for AI protocol testing.
 * 
 * This framework provides mock implementations of network dependencies,
 * external services, and protocol handlers for development and testing
 * without requiring live connections.
 * 
 * 
 */
public class AIStubFramework {

    private static final Logger logger = LoggerFactory.getLogger(AIStubFramework.class);

    private final Map<String, AIStubService> stubServices = new ConcurrentHashMap<>();
    private final AIStubHttpServer httpStubServer;
    private final AIStubWebSocketServer webSocketStubServer;
    private final AIStubMQTTBroker mqttStubBroker;
    private final AIStubConfiguration configuration;
    private boolean enabled = false;

    /**
     * Create a new stub framework.
     * 
     * @param configuration Stub framework configuration
     */
    public AIStubFramework(AIStubConfiguration configuration) {
        this.configuration = configuration;
        this.httpStubServer = new AIStubHttpServer(configuration.getHttpStubPort());
        this.webSocketStubServer = new AIStubWebSocketServer(configuration.getWebSocketStubPort());
        this.mqttStubBroker = new AIStubMQTTBroker(configuration.getMqttStubPort());

        logger.info("AI Stub Framework initialized with configuration: {}", configuration);
    }

    /**
     * Start the stub framework.
     * 
     * @return true if started successfully
     */
    public boolean start() {
        if (enabled) {
            logger.warn("Stub framework is already running");
            return true;
        }

        try {
            // Start stub servers
            httpStubServer.start();
            webSocketStubServer.start();
            mqttStubBroker.start();

            // Register default stub services
            registerDefaultStubServices();

            enabled = true;
            logger.info("AI Stub Framework started successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to start AI Stub Framework", e);
            return false;
        }
    }

    /**
     * Stop the stub framework.
     * 
     * @return true if stopped successfully
     */
    public boolean stop() {
        if (!enabled) {
            logger.warn("Stub framework is not running");
            return true;
        }

        try {
            // Stop all stub services
            stubServices.values().forEach(AIStubService::stop);
            stubServices.clear();

            // Stop stub servers
            httpStubServer.stop();
            webSocketStubServer.stop();
            mqttStubBroker.stop();

            enabled = false;
            logger.info("AI Stub Framework stopped successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop AI Stub Framework", e);
            return false;
        }
    }

    /**
     * Register a stub service.
     * 
     * @param name Service name
     * @param service Stub service implementation
     */
    public void registerStubService(String name, AIStubService service) {
        stubServices.put(name, service);
        if (enabled) {
            service.start();
        }
        logger.debug("Registered stub service: {}", name);
    }

    /**
     * Unregister a stub service.
     * 
     * @param name Service name
     * @return true if service was removed
     */
    public boolean unregisterStubService(String name) {
        AIStubService service = stubServices.remove(name);
        if (service != null) {
            service.stop();
            logger.debug("Unregistered stub service: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Get a stub service by name.
     * 
     * @param name Service name
     * @param type Expected service type
     * @param <T> Service type
     * @return Stub service instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends AIStubService> @Nullable T getStubService(String name, Class<T> type) {
        AIStubService service = stubServices.get(name);
        if (service != null && type.isInstance(service)) {
            return (T) service;
        }
        return null;
    }

    /**
     * Check if the stub framework is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the HTTP stub server.
     * 
     * @return HTTP stub server
     */
    public AIStubHttpServer getHttpStubServer() {
        return httpStubServer;
    }

    /**
     * Get the WebSocket stub server.
     * 
     * @return WebSocket stub server
     */
    public AIStubWebSocketServer getWebSocketStubServer() {
        return webSocketStubServer;
    }

    /**
     * Get the MQTT stub broker.
     * 
     * @return MQTT stub broker
     */
    public AIStubMQTTBroker getMqttStubBroker() {
        return mqttStubBroker;
    }

    /**
     * Reset all stub services to initial state.
     */
    public void resetAll() {
        logger.info("Resetting all stub services");
        stubServices.values().forEach(AIStubService::reset);
        httpStubServer.reset();
        webSocketStubServer.reset();
        mqttStubBroker.reset();
    }

    /**
     * Get stub framework statistics.
     * 
     * @return Statistics about stub usage
     */
    public AIStubStatistics getStatistics() {
        AIStubStatistics stats = new AIStubStatistics();
        stats.setEnabled(enabled);
        stats.setRegisteredServices(stubServices.size());
        stats.setHttpRequestCount(httpStubServer.getRequestCount());
        stats.setWebSocketConnectionCount(webSocketStubServer.getConnectionCount());
        stats.setMqttMessageCount(mqttStubBroker.getMessageCount());
        return stats;
    }

    /**
     * Create a stub response for testing.
     * 
     * @param serviceName Service name
     * @param request Request data
     * @return Stub response
     */
    public AIStubResponse createStubResponse(String serviceName, Object request) {
        AIStubService service = stubServices.get(serviceName);
        if (service != null) {
            return service.handleRequest(request);
        }

        // Return default stub response
        return AIStubResponse.builder().success(false).message("Stub service not found: " + serviceName).build();
    }

    /**
     * Configure stub behavior for testing scenarios.
     * 
     * @param scenario Test scenario configuration
     */
    public void configureTestScenario(AIStubTestScenario scenario) {
        logger.info("Configuring test scenario: {}", scenario.getName());

        // Apply scenario configuration to stub services
        for (Map.Entry<String, Object> config : scenario.getServiceConfigurations().entrySet()) {
            AIStubService service = stubServices.get(config.getKey());
            if (service != null) {
                service.configure(config.getValue());
            }
        }

        // Configure stub servers
        httpStubServer.configureScenario(scenario.getHttpConfiguration());
        webSocketStubServer.configureScenario(scenario.getWebSocketConfiguration());
        mqttStubBroker.configureScenario(scenario.getMqttConfiguration());
    }

    private void registerDefaultStubServices() {
        // Authentication stub service
        registerStubService("auth", new AIAuthenticationStubService());

        // MCP protocol stub service
        registerStubService("mcp", new AIMCPStubService());

        // A2A protocol stub service
        registerStubService("a2a", new AIA2AStubService());

        // openHAB service stubs
        registerStubService("things", new AIThingStubService());
        registerStubService("items", new AIItemStubService());
        registerStubService("rules", new AIRuleStubService());
        registerStubService("events", new AIEventStubService());

        // External service stubs
        registerStubService("database", new AIDatabaseStubService());
        registerStubService("filesystem", new AIFilesystemStubService());
        registerStubService("network", new AINetworkStubService());

        logger.info("Registered {} default stub services", stubServices.size());
    }
}
