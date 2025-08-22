package org.openhab.core.ai.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration validator for manual validation of configuration maps.
 * 
 * This class provides validation methods for different configuration domains
 * following the ai.{domain}.* naming convention. It performs validation without
 * relying on OSGi MetaType, using manual validation patterns.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ConfigurationValidator {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    /**
     * Validates model configuration with ai.model.* keys.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    public void validateModelConfiguration(Map<String, Object> config) throws ConfigurationException {
        logger.debug("Validating model configuration");

        // Validate required fields
        validateRequiredField(config, "ai.model.primary.provider");
        validateRequiredField(config, "ai.model.fallback.provider");

        // Validate numeric ranges
        validateTemperatureRange(getDoubleConfig(config, "ai.model.default.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.default.maxTokens", 1000));
        validateTimeoutRange(getIntConfig(config, "ai.model.default.timeoutMs", 30000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.default.retryAttempts", 3));

        // Validate provider configurations
        validateProviderConfigurations(config);
    }

    /**
     * Validates agent configuration with ai.agent.* keys.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    public void validateAgentConfiguration(Map<String, Object> config) throws ConfigurationException {
        logger.debug("Validating agent configuration");

        // Validate required fields
        validateRequiredField(config, "ai.agent.server.id");
        validateRequiredField(config, "ai.agent.server.name");

        // Validate numeric ranges
        validatePortRange(getIntConfig(config, "ai.agent.server.port", 8080));
        validateTimeoutRange(getIntConfig(config, "ai.agent.execution.timeout", 300));
        validateMaxAgentsRange(getIntConfig(config, "ai.agent.max.agents", 50));

        // Validate boolean fields
        validateBooleanField(config, "ai.agent.server.enabled");
        validateBooleanField(config, "ai.agent.persistence.enabled");
        validateBooleanField(config, "ai.agent.push.notifications.enabled");
    }

    /**
     * Validates tool configuration with ai.tool.* keys.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    public void validateToolConfiguration(Map<String, Object> config) throws ConfigurationException {
        logger.debug("Validating tool configuration");

        // Validate required fields
        validateRequiredField(config, "ai.tool.server.id");
        validateRequiredField(config, "ai.tool.server.name");

        // Validate numeric ranges
        validatePortRange(getIntConfig(config, "ai.tool.server.port", 8081));
        validateMaxToolsRange(getIntConfig(config, "ai.tool.max.tools", 100));
        validateThreadPoolSizeRange(getIntConfig(config, "ai.tool.async.thread.pool.size", 10));
        validateQueueCapacityRange(getIntConfig(config, "ai.tool.async.queue.capacity", 1000));

        // Validate boolean fields
        validateBooleanField(config, "ai.tool.server.enabled");
        validateBooleanField(config, "ai.tool.authentication.enabled");
        validateBooleanField(config, "ai.tool.async.enable.server");
    }

    /**
     * Validates common AI configuration with ai.common.* keys.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    public void validateCommonConfiguration(Map<String, Object> config) throws ConfigurationException {
        logger.debug("Validating common AI configuration");

        // Validate boolean fields
        validateBooleanField(config, "ai.common.enabled");
        validateBooleanField(config, "ai.common.debug.mode");
        validateBooleanField(config, "ai.common.security.enabled");

        // Validate numeric ranges
        validateTimeoutRange(getIntConfig(config, "ai.common.default.timeout", 30000));
        validateMaxConcurrentRequestsRange(getIntConfig(config, "ai.common.max.concurrent.requests", 10));
    }

    /**
     * Validates provider-specific configurations.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateProviderConfigurations(Map<String, Object> config) throws ConfigurationException {
        // Validate OpenAI configuration if enabled
        if (getBooleanConfig(config, "ai.model.openai.enabled", false)) {
            validateOpenAIConfig(config);
        }

        // Validate Anthropic configuration if enabled
        if (getBooleanConfig(config, "ai.model.anthropic.enabled", false)) {
            validateAnthropicConfig(config);
        }

        // Validate Ollama configuration if enabled
        if (getBooleanConfig(config, "ai.model.ollama.enabled", true)) {
            validateOllamaConfig(config);
        }

        // Validate Google configuration if enabled
        if (getBooleanConfig(config, "ai.model.google.enabled", false)) {
            validateGoogleConfig(config);
        }

        // Validate Azure configuration if enabled
        if (getBooleanConfig(config, "ai.model.azure.enabled", false)) {
            validateAzureConfig(config);
        }
    }

    /**
     * Validates OpenAI configuration.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateOpenAIConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "ai.model.openai.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("OpenAI API key is required when OpenAI is enabled");
        }

        String baseUrl = getStringConfig(config, "ai.model.openai.base.url", "https://api.openai.com/v1");
        if (!isValidUrl(baseUrl)) {
            throw new ConfigurationException("Invalid OpenAI base URL: " + baseUrl);
        }

        validateTemperatureRange(getDoubleConfig(config, "ai.model.openai.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.openai.maxTokens", 4000));
        validateTimeoutRange(getIntConfig(config, "ai.model.openai.timeoutMs", 30000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.openai.retryAttempts", 3));
    }

    /**
     * Validates Anthropic configuration.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateAnthropicConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "ai.model.anthropic.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("Anthropic API key is required when Anthropic is enabled");
        }

        String baseUrl = getStringConfig(config, "ai.model.anthropic.base.url", "https://api.anthropic.com");
        if (!isValidUrl(baseUrl)) {
            throw new ConfigurationException("Invalid Anthropic base URL: " + baseUrl);
        }

        validateTemperatureRange(getDoubleConfig(config, "ai.model.anthropic.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.anthropic.maxTokens", 4000));
        validateTimeoutRange(getIntConfig(config, "ai.model.anthropic.timeoutMs", 30000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.anthropic.retryAttempts", 3));
    }

    /**
     * Validates Ollama configuration.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateOllamaConfig(Map<String, Object> config) throws ConfigurationException {
        String baseUrl = getStringConfig(config, "ai.model.ollama.base.url", "http://localhost:11434");
        if (!isValidUrl(baseUrl)) {
            throw new ConfigurationException("Invalid Ollama base URL: " + baseUrl);
        }

        validateTemperatureRange(getDoubleConfig(config, "ai.model.ollama.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.ollama.maxTokens", 4000));
        validateTimeoutRange(getIntConfig(config, "ai.model.ollama.timeoutMs", 60000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.ollama.retryAttempts", 2));
    }

    /**
     * Validates Google configuration.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateGoogleConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "ai.model.google.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("Google API key is required when Google is enabled");
        }

        String baseUrl = getStringConfig(config, "ai.model.google.base.url",
                "https://generativelanguage.googleapis.com");
        if (!isValidUrl(baseUrl)) {
            throw new ConfigurationException("Invalid Google base URL: " + baseUrl);
        }

        validateTemperatureRange(getDoubleConfig(config, "ai.model.google.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.google.maxTokens", 4000));
        validateTimeoutRange(getIntConfig(config, "ai.model.google.timeoutMs", 30000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.google.retryAttempts", 3));
    }

    /**
     * Validates Azure configuration.
     * 
     * @param config the configuration map to validate
     * @throws ConfigurationException if validation fails
     */
    private void validateAzureConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "ai.model.azure.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("Azure API key is required when Azure is enabled");
        }

        String endpoint = getStringConfig(config, "ai.model.azure.endpoint", "");
        if (endpoint.isEmpty()) {
            throw new ConfigurationException("Azure endpoint is required when Azure is enabled");
        }

        if (!isValidUrl(endpoint)) {
            throw new ConfigurationException("Invalid Azure endpoint URL: " + endpoint);
        }

        String deploymentName = getStringConfig(config, "ai.model.azure.deployment.name", "");
        if (deploymentName.isEmpty()) {
            throw new ConfigurationException("Azure deployment name is required when Azure is enabled");
        }

        validateTemperatureRange(getDoubleConfig(config, "ai.model.azure.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.azure.maxTokens", 4000));
        validateTimeoutRange(getIntConfig(config, "ai.model.azure.timeoutMs", 30000));
        validateRetryAttemptsRange(getIntConfig(config, "ai.model.azure.retryAttempts", 3));
    }

    /**
     * Validates that a required field is present and not empty.
     * 
     * @param config the configuration map
     * @param key the configuration key
     * @throws ConfigurationException if the field is missing or empty
     */
    private void validateRequiredField(Map<String, Object> config, String key) throws ConfigurationException {
        Object value = config.get(key);
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            throw new ConfigurationException("Required configuration field is missing or empty: " + key);
        }
    }

    /**
     * Validates that a boolean field is present and valid.
     * 
     * @param config the configuration map
     * @param key the configuration key
     * @throws ConfigurationException if the field is invalid
     */
    private void validateBooleanField(Map<String, Object> config, String key) throws ConfigurationException {
        Object value = config.get(key);
        if (value != null && !(value instanceof Boolean) && !(value instanceof String)) {
            throw new ConfigurationException("Invalid boolean value for configuration field: " + key);
        }
    }

    /**
     * Validates temperature range (0.0 to 2.0).
     * 
     * @param temperature the temperature value to validate
     * @throws ConfigurationException if temperature is out of range
     */
    private void validateTemperatureRange(double temperature) throws ConfigurationException {
        if (temperature < 0.0 || temperature > 2.0) {
            throw new ConfigurationException("Temperature must be between 0.0 and 2.0, got: " + temperature);
        }
    }

    /**
     * Validates max tokens range (1 to 8192).
     * 
     * @param maxTokens the max tokens value to validate
     * @throws ConfigurationException if max tokens is out of range
     */
    private void validateMaxTokensRange(int maxTokens) throws ConfigurationException {
        if (maxTokens < 1 || maxTokens > 8192) {
            throw new ConfigurationException("Max tokens must be between 1 and 8192, got: " + maxTokens);
        }
    }

    /**
     * Validates timeout range (1000 to 300000 ms).
     * 
     * @param timeoutMs the timeout value to validate
     * @throws ConfigurationException if timeout is out of range
     */
    private void validateTimeoutRange(int timeoutMs) throws ConfigurationException {
        if (timeoutMs < 1000 || timeoutMs > 300000) {
            throw new ConfigurationException("Timeout must be between 1000 and 300000 ms, got: " + timeoutMs);
        }
    }

    /**
     * Validates retry attempts range (0 to 10).
     * 
     * @param retryAttempts the retry attempts value to validate
     * @throws ConfigurationException if retry attempts is out of range
     */
    private void validateRetryAttemptsRange(int retryAttempts) throws ConfigurationException {
        if (retryAttempts < 0 || retryAttempts > 10) {
            throw new ConfigurationException("Retry attempts must be between 0 and 10, got: " + retryAttempts);
        }
    }

    /**
     * Validates port range (1 to 65535).
     * 
     * @param port the port value to validate
     * @throws ConfigurationException if port is out of range
     */
    private void validatePortRange(int port) throws ConfigurationException {
        if (port < 1 || port > 65535) {
            throw new ConfigurationException("Port must be between 1 and 65535, got: " + port);
        }
    }

    /**
     * Validates max agents range (1 to 1000).
     * 
     * @param maxAgents the max agents value to validate
     * @throws ConfigurationException if max agents is out of range
     */
    private void validateMaxAgentsRange(int maxAgents) throws ConfigurationException {
        if (maxAgents < 1 || maxAgents > 1000) {
            throw new ConfigurationException("Max agents must be between 1 and 1000, got: " + maxAgents);
        }
    }

    /**
     * Validates max tools range (1 to 10000).
     * 
     * @param maxTools the max tools value to validate
     * @throws ConfigurationException if max tools is out of range
     */
    private void validateMaxToolsRange(int maxTools) throws ConfigurationException {
        if (maxTools < 1 || maxTools > 10000) {
            throw new ConfigurationException("Max tools must be between 1 and 10000, got: " + maxTools);
        }
    }

    /**
     * Validates thread pool size range (1 to 100).
     * 
     * @param threadPoolSize the thread pool size value to validate
     * @throws ConfigurationException if thread pool size is out of range
     */
    private void validateThreadPoolSizeRange(int threadPoolSize) throws ConfigurationException {
        if (threadPoolSize < 1 || threadPoolSize > 100) {
            throw new ConfigurationException("Thread pool size must be between 1 and 100, got: " + threadPoolSize);
        }
    }

    /**
     * Validates queue capacity range (1 to 100000).
     * 
     * @param queueCapacity the queue capacity value to validate
     * @throws ConfigurationException if queue capacity is out of range
     */
    private void validateQueueCapacityRange(int queueCapacity) throws ConfigurationException {
        if (queueCapacity < 1 || queueCapacity > 100000) {
            throw new ConfigurationException("Queue capacity must be between 1 and 100000, got: " + queueCapacity);
        }
    }

    /**
     * Validates max concurrent requests range (1 to 1000).
     * 
     * @param maxConcurrentRequests the max concurrent requests value to validate
     * @throws ConfigurationException if max concurrent requests is out of range
     */
    private void validateMaxConcurrentRequestsRange(int maxConcurrentRequests) throws ConfigurationException {
        if (maxConcurrentRequests < 1 || maxConcurrentRequests > 1000) {
            throw new ConfigurationException(
                    "Max concurrent requests must be between 1 and 1000, got: " + maxConcurrentRequests);
        }
    }

    /**
     * Validates if a string is a valid URL.
     * 
     * @param url the URL string to validate
     * @return true if the URL is valid, false otherwise
     */
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    // Helper methods for extracting configuration values

    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    private boolean getBooleanConfig(Map<String, Object> config, String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private int getIntConfig(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for config key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    private double getDoubleConfig(Map<String, Object> config, String key, double defaultValue) {
        Object value = config.get(key);
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for config key {}: {}", key, value);
            }
        }
        return defaultValue;
    }
}
