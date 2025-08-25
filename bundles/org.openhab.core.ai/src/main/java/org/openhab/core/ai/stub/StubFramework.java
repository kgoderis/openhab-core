package org.openhab.core.ai.stub;

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
public class StubFramework {

    private static final Logger logger = LoggerFactory.getLogger(StubFramework.class);

    private final Map<String, StubService> stubServices = new ConcurrentHashMap<>();
    private final StubHttpServer httpStubServer;
    private final StubWebSocketServer webSocketStubServer;
    private final StubMQTTBroker mqttStubBroker;
    private final StubConfiguration configuration;
    private boolean enabled = false;

    /**
     * Create a new stub framework.
     * 
     * @param configuration Stub framework configuration
     */
    public StubFramework(StubConfiguration configuration) {
        this.configuration = configuration;
        this.httpStubServer = new StubHttpServer(configuration.getHttpStubPort());
        this.webSocketStubServer = new StubWebSocketServer(configuration.getWebSocketStubPort());
        this.mqttStubBroker = new StubMQTTBroker(configuration.getMqttStubPort());

        logger.info("Stub Framework initialized with configuration: {}", configuration);
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
            stubServices.values().forEach(StubService::stop);
            stubServices.clear();

            // Stop stub servers
            httpStubServer.stop();
            webSocketStubServer.stop();
            mqttStubBroker.stop();

            enabled = false;
            logger.info("Stub Framework stopped successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop Stub Framework", e);
            return false;
        }
    }

    /**
     * Register a stub service.
     * 
     * @param name Service name
     * @param service Stub service implementation
     */
    public void registerStubService(String name, StubService service) {
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
        StubService service = stubServices.remove(name);
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
    public <T extends StubService> @Nullable T getStubService(String name, Class<T> type) {
        StubService service = stubServices.get(name);
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
    public StubHttpServer getHttpStubServer() {
        return httpStubServer;
    }

    /**
     * Get the WebSocket stub server.
     * 
     * @return WebSocket stub server
     */
    public StubWebSocketServer getWebSocketStubServer() {
        return webSocketStubServer;
    }

    /**
     * Get the MQTT stub broker.
     * 
     * @return MQTT stub broker
     */
    public StubMQTTBroker getMqttStubBroker() {
        return mqttStubBroker;
    }

    /**
     * Reset all stub services to initial state.
     */
    public void resetAll() {
        logger.info("Resetting all stub services");
        stubServices.values().forEach(StubService::reset);
        httpStubServer.reset();
        webSocketStubServer.reset();
        mqttStubBroker.reset();
    }

    /**
     * Get stub framework statistics.
     * 
     * @return Statistics about stub usage
     */
    public StubStatistics getStatistics() {
        return StubStatistics.fromCurrentState(enabled, stubServices.size(), httpStubServer.getRequestCount(),
                webSocketStubServer.getConnectionCount(), mqttStubBroker.getMessageCount());
    }

    /**
     * Create a stub response for testing.
     * 
     * @param serviceName Service name
     * @param request Request data
     * @return Stub response
     */
    public StubResponse createStubResponse(String serviceName, Object request) {
        StubService service = stubServices.get(serviceName);
        if (service != null) {
            return service.handleRequest(request);
        }

        // Return default stub response
        return StubResponse.builder().withSuccess(false).withMessage("Stub service not found: " + serviceName).build();
    }

    /**
     * Configure stub behavior for testing scenarios.
     * 
     * @param scenario Test scenario configuration
     */
    public void configureTestScenario(StubTestScenario scenario) {
        logger.info("Configuring test scenario: {}", scenario.getName());

        // Apply scenario configuration to stub services
        for (Map.Entry<String, Object> config : scenario.getServiceConfigurations().entrySet()) {
            StubService service = stubServices.get(config.getKey());
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
        registerStubService("auth", new AuthenticationStubService());

        // MCP protocol stub service
        registerStubService("mcp", new MCPStubService());

        // A2A protocol stub service
        registerStubService("a2a", new A2AStubService());

        // openHAB service stubs
        registerStubService("things", new ThingStubService());
        registerStubService("items", new ItemStubService());
        registerStubService("rules", new RuleStubService());
        registerStubService("events", new EventStubService());

        // External service stubs
        registerStubService("database", new DatabaseStubService());
        registerStubService("filesystem", new FilesystemStubService());
        registerStubService("network", new NetworkStubService());

        logger.info("Registered {} default stub services", stubServices.size());
    }
}
