package org.openhab.core.ai.common.stub;

/**
 * Configuration class for the AI stub framework.
 * 
 * This class holds configuration settings for stub servers and services.
 * 
 * 
 */
public class AIStubConfiguration {

    private int httpStubPort = 8080;
    private int webSocketStubPort = 8081;
    private int mqttStubPort = 1883;
    private boolean enabled = true;

    /**
     * Get the HTTP stub server port.
     * 
     * @return HTTP port
     */
    public int getHttpStubPort() {
        return httpStubPort;
    }

    /**
     * Set the HTTP stub server port.
     * 
     * @param httpStubPort HTTP port
     */
    public void setHttpStubPort(int httpStubPort) {
        this.httpStubPort = httpStubPort;
    }

    /**
     * Get the WebSocket stub server port.
     * 
     * @return WebSocket port
     */
    public int getWebSocketStubPort() {
        return webSocketStubPort;
    }

    /**
     * Set the WebSocket stub server port.
     * 
     * @param webSocketStubPort WebSocket port
     */
    public void setWebSocketStubPort(int webSocketStubPort) {
        this.webSocketStubPort = webSocketStubPort;
    }

    /**
     * Get the MQTT stub broker port.
     * 
     * @return MQTT port
     */
    public int getMqttStubPort() {
        return mqttStubPort;
    }

    /**
     * Set the MQTT stub broker port.
     * 
     * @param mqttStubPort MQTT port
     */
    public void setMqttStubPort(int mqttStubPort) {
        this.mqttStubPort = mqttStubPort;
    }

    /**
     * Check if stub framework is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set stub framework enabled state.
     * 
     * @param enabled enabled state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "AIStubConfiguration{" + "httpStubPort=" + httpStubPort + ", webSocketStubPort=" + webSocketStubPort
                + ", mqttStubPort=" + mqttStubPort + ", enabled=" + enabled + '}';
    }
}
