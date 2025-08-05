package org.openhab.core.ai.stub;

/**
 * Statistics class for the AI stub framework.
 * 
 * This class holds performance and usage metrics for the entire stub framework.
 * 
 * 
 */
public class StubStatistics {

    private boolean enabled = false;
    private int registeredServices = 0;
    private long httpRequestCount = 0;
    private long webSocketConnectionCount = 0;
    private long mqttMessageCount = 0;

    /**
     * Check if the framework is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the framework enabled state.
     * 
     * @param enabled enabled state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the number of registered services.
     * 
     * @return registered services count
     */
    public int getRegisteredServices() {
        return registeredServices;
    }

    /**
     * Set the number of registered services.
     * 
     * @param registeredServices registered services count
     */
    public void setRegisteredServices(int registeredServices) {
        this.registeredServices = registeredServices;
    }

    /**
     * Get the HTTP request count.
     * 
     * @return HTTP request count
     */
    public long getHttpRequestCount() {
        return httpRequestCount;
    }

    /**
     * Set the HTTP request count.
     * 
     * @param httpRequestCount HTTP request count
     */
    public void setHttpRequestCount(long httpRequestCount) {
        this.httpRequestCount = httpRequestCount;
    }

    /**
     * Get the WebSocket connection count.
     * 
     * @return WebSocket connection count
     */
    public long getWebSocketConnectionCount() {
        return webSocketConnectionCount;
    }

    /**
     * Set the WebSocket connection count.
     * 
     * @param webSocketConnectionCount WebSocket connection count
     */
    public void setWebSocketConnectionCount(long webSocketConnectionCount) {
        this.webSocketConnectionCount = webSocketConnectionCount;
    }

    /**
     * Get the MQTT message count.
     * 
     * @return MQTT message count
     */
    public long getMqttMessageCount() {
        return mqttMessageCount;
    }

    /**
     * Set the MQTT message count.
     * 
     * @param mqttMessageCount MQTT message count
     */
    public void setMqttMessageCount(long mqttMessageCount) {
        this.mqttMessageCount = mqttMessageCount;
    }

    /**
     * Reset all statistics.
     */
    public void reset() {
        httpRequestCount = 0;
        webSocketConnectionCount = 0;
        mqttMessageCount = 0;
    }
}
