package org.openhab.core.ai.common.stub;

import java.util.Map;
import java.util.Objects;

/**
 * Test scenario configuration for the AI stub framework.
 * 
 * This class defines a test scenario with specific configurations
 * for different stub services and servers.
 * 
 * 
 */
public class AIStubTestScenario {

    private final String name;
    private final Map<String, Object> serviceConfigurations;
    private final Object httpConfiguration;
    private final Object webSocketConfiguration;
    private final Object mqttConfiguration;

    /**
     * Create a new test scenario.
     * 
     * @param name Scenario name
     * @param serviceConfigurations Service-specific configurations
     * @param httpConfiguration HTTP server configuration
     * @param webSocketConfiguration WebSocket server configuration
     * @param mqttConfiguration MQTT broker configuration
     */
    public AIStubTestScenario(String name, Map<String, Object> serviceConfigurations, Object httpConfiguration,
            Object webSocketConfiguration, Object mqttConfiguration) {
        this.name = Objects.requireNonNull(name, "Scenario name cannot be null");
        this.serviceConfigurations = serviceConfigurations != null ? Map.copyOf(serviceConfigurations) : Map.of();
        this.httpConfiguration = httpConfiguration;
        this.webSocketConfiguration = webSocketConfiguration;
        this.mqttConfiguration = mqttConfiguration;
    }

    /**
     * Get the scenario name.
     * 
     * @return Scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * Get service configurations.
     * 
     * @return Map of service configurations
     */
    public Map<String, Object> getServiceConfigurations() {
        return serviceConfigurations;
    }

    /**
     * Get HTTP configuration.
     * 
     * @return HTTP configuration
     */
    public Object getHttpConfiguration() {
        return httpConfiguration;
    }

    /**
     * Get WebSocket configuration.
     * 
     * @return WebSocket configuration
     */
    public Object getWebSocketConfiguration() {
        return webSocketConfiguration;
    }

    /**
     * Get MQTT configuration.
     * 
     * @return MQTT configuration
     */
    public Object getMqttConfiguration() {
        return mqttConfiguration;
    }

    @Override
    public String toString() {
        return "AIStubTestScenario{" + "name='" + name + '\'' + ", serviceConfigurations="
                + serviceConfigurations.size() + ", hasHttpConfig=" + (httpConfiguration != null)
                + ", hasWebSocketConfig=" + (webSocketConfiguration != null) + ", hasMqttConfig="
                + (mqttConfiguration != null) + '}';
    }
}
