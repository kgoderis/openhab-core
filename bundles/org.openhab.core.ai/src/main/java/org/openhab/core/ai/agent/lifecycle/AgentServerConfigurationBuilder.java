package org.openhab.core.ai.agent.lifecycle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for A2A server configuration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentServerConfigurationBuilder {
    private String serverId = "default-a2a-server-id";
    private String serverName = "openHAB A2A Server";
    private String serverVersion = "1.0.0";
    private String baseUrl = "http://localhost:8080";
    private String servletPath = "/a2a";
    private String servletPattern = "/a2a/*";
    private String messageSendEndpoint = "/a2a/message/send";
    private String taskGetEndpoint = "/a2a/task/get";
    private String taskCancelEndpoint = "/a2a/task/cancel";
    private String healthEndpoint = "/a2a/health";
    private String statusEndpoint = "/a2a/status";
    private String agentCardEndpoint = "/.well-known/agent.json";

    // Security configuration
    private boolean enableAuthentication = false;
    private String primaryAuthMethod = "oauth2.1";
    private String fallbackAuthMethod = "openhab_users";
    private boolean enableFallbackAuth = true;
    private int maxConnections = 100;
    private int rateLimitPerMinute = 1000;
    private boolean enableRequestValidation = true;

    // OAuth 2.1 configuration
    private String oauthIssuerUrl = "";
    private String oauthClientId = "";
    private String oauthClientSecret = "";
    private String oauthRedirectUri = "";
    private boolean oauthPkceEnabled = true;

    // openHAB users authentication
    private String openhabUsersFile = "";
    private boolean openhabUsersEnabled = true;

    // API key authentication
    private String apiKeyHeader = "X-API-Key";
    private String apiKeyValue = "";
    private boolean apiKeyEnabled = false;

    // JWT authentication
    private String jwtSecret = "";
    private String jwtIssuer = "openhab-a2a";
    private int jwtExpirationMinutes = 60;
    private boolean jwtEnabled = false;

    // Monitoring and metrics
    private boolean enableMetrics = true;
    private boolean enableHealthChecks = true;
    private int healthCheckInterval = 30000; // 30 seconds
    private boolean enablePerformanceMonitoring = true;

    // Production settings
    private boolean productionMode = false;
    private int requestTimeout = 30000; // 30 seconds
    private int connectionTimeout = 10000; // 10 seconds
    private boolean enableGracefulShutdown = true;
    private int shutdownTimeout = 30000; // 30 seconds

    // A2A-specific settings
    private boolean enableTaskManagement = true;
    private boolean enableMessageRouting = true;
    private int maxTaskQueueSize = 1000;
    private int taskTimeoutSeconds = 300; // 5 minutes
    private boolean enableTaskPersistence = false;

    private Map<String, Object> serverOptions = new HashMap<>();

    // Getters for AgentServerConfiguration constructor
    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getServletPath() {
        return servletPath;
    }

    public String getServletPattern() {
        return servletPattern;
    }

    public String getMessageSendEndpoint() {
        return messageSendEndpoint;
    }

    public String getTaskGetEndpoint() {
        return taskGetEndpoint;
    }

    public String getTaskCancelEndpoint() {
        return taskCancelEndpoint;
    }

    public String getHealthEndpoint() {
        return healthEndpoint;
    }

    public String getStatusEndpoint() {
        return statusEndpoint;
    }

    public String getAgentCardEndpoint() {
        return agentCardEndpoint;
    }

    public boolean isEnableAuthentication() {
        return enableAuthentication;
    }

    public String getPrimaryAuthMethod() {
        return primaryAuthMethod;
    }

    public String getFallbackAuthMethod() {
        return fallbackAuthMethod;
    }

    public boolean isEnableFallbackAuth() {
        return enableFallbackAuth;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public boolean isEnableRequestValidation() {
        return enableRequestValidation;
    }

    public String getOauthIssuerUrl() {
        return oauthIssuerUrl;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public String getOauthRedirectUri() {
        return oauthRedirectUri;
    }

    public boolean isOauthPkceEnabled() {
        return oauthPkceEnabled;
    }

    public String getOpenhabUsersFile() {
        return openhabUsersFile;
    }

    public boolean isOpenhabUsersEnabled() {
        return openhabUsersEnabled;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public boolean isApiKeyEnabled() {
        return apiKeyEnabled;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public int getJwtExpirationMinutes() {
        return jwtExpirationMinutes;
    }

    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    public boolean isEnableHealthChecks() {
        return enableHealthChecks;
    }

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public boolean isEnablePerformanceMonitoring() {
        return enablePerformanceMonitoring;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public boolean isEnableGracefulShutdown() {
        return enableGracefulShutdown;
    }

    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    public boolean isEnableTaskManagement() {
        return enableTaskManagement;
    }

    public boolean isEnableMessageRouting() {
        return enableMessageRouting;
    }

    public int getMaxTaskQueueSize() {
        return maxTaskQueueSize;
    }

    public int getTaskTimeoutSeconds() {
        return taskTimeoutSeconds;
    }

    public boolean isEnableTaskPersistence() {
        return enableTaskPersistence;
    }

    public Map<String, Object> getServerOptions() {
        return serverOptions;
    }

    public AgentServerConfigurationBuilder serverId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public AgentServerConfigurationBuilder serverName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public AgentServerConfigurationBuilder serverVersion(String serverVersion) {
        this.serverVersion = serverVersion;
        return this;
    }

    public AgentServerConfigurationBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public AgentServerConfigurationBuilder servletPath(String servletPath) {
        this.servletPath = servletPath;
        return this;
    }

    public AgentServerConfigurationBuilder servletPattern(String servletPattern) {
        this.servletPattern = servletPattern;
        return this;
    }

    public AgentServerConfigurationBuilder messageSendEndpoint(String messageSendEndpoint) {
        this.messageSendEndpoint = messageSendEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder taskGetEndpoint(String taskGetEndpoint) {
        this.taskGetEndpoint = taskGetEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder taskCancelEndpoint(String taskCancelEndpoint) {
        this.taskCancelEndpoint = taskCancelEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder healthEndpoint(String healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder statusEndpoint(String statusEndpoint) {
        this.statusEndpoint = statusEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder agentCardEndpoint(String agentCardEndpoint) {
        this.agentCardEndpoint = agentCardEndpoint;
        return this;
    }

    public AgentServerConfigurationBuilder enableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
        return this;
    }

    public AgentServerConfigurationBuilder primaryAuthMethod(String primaryAuthMethod) {
        this.primaryAuthMethod = primaryAuthMethod;
        return this;
    }

    public AgentServerConfigurationBuilder fallbackAuthMethod(String fallbackAuthMethod) {
        this.fallbackAuthMethod = fallbackAuthMethod;
        return this;
    }

    public AgentServerConfigurationBuilder enableFallbackAuth(boolean enableFallbackAuth) {
        this.enableFallbackAuth = enableFallbackAuth;
        return this;
    }

    public AgentServerConfigurationBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public AgentServerConfigurationBuilder rateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
        return this;
    }

    public AgentServerConfigurationBuilder enableRequestValidation(boolean enableRequestValidation) {
        this.enableRequestValidation = enableRequestValidation;
        return this;
    }

    public AgentServerConfigurationBuilder oauthIssuerUrl(String oauthIssuerUrl) {
        this.oauthIssuerUrl = oauthIssuerUrl;
        return this;
    }

    public AgentServerConfigurationBuilder oauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
        return this;
    }

    public AgentServerConfigurationBuilder oauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
        return this;
    }

    public AgentServerConfigurationBuilder oauthRedirectUri(String oauthRedirectUri) {
        this.oauthRedirectUri = oauthRedirectUri;
        return this;
    }

    public AgentServerConfigurationBuilder oauthPkceEnabled(boolean oauthPkceEnabled) {
        this.oauthPkceEnabled = oauthPkceEnabled;
        return this;
    }

    public AgentServerConfigurationBuilder openhabUsersFile(String openhabUsersFile) {
        this.openhabUsersFile = openhabUsersFile;
        return this;
    }

    public AgentServerConfigurationBuilder openhabUsersEnabled(boolean openhabUsersEnabled) {
        this.openhabUsersEnabled = openhabUsersEnabled;
        return this;
    }

    public AgentServerConfigurationBuilder apiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
        return this;
    }

    public AgentServerConfigurationBuilder apiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
        return this;
    }

    public AgentServerConfigurationBuilder apiKeyEnabled(boolean apiKeyEnabled) {
        this.apiKeyEnabled = apiKeyEnabled;
        return this;
    }

    public AgentServerConfigurationBuilder jwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
        return this;
    }

    public AgentServerConfigurationBuilder jwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
        return this;
    }

    public AgentServerConfigurationBuilder jwtExpirationMinutes(int jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        return this;
    }

    public AgentServerConfigurationBuilder jwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
        return this;
    }

    public AgentServerConfigurationBuilder enableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }

    public AgentServerConfigurationBuilder enableHealthChecks(boolean enableHealthChecks) {
        this.enableHealthChecks = enableHealthChecks;
        return this;
    }

    public AgentServerConfigurationBuilder healthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    public AgentServerConfigurationBuilder enablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        return this;
    }

    public AgentServerConfigurationBuilder productionMode(boolean productionMode) {
        this.productionMode = productionMode;
        return this;
    }

    public AgentServerConfigurationBuilder requestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public AgentServerConfigurationBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public AgentServerConfigurationBuilder enableGracefulShutdown(boolean enableGracefulShutdown) {
        this.enableGracefulShutdown = enableGracefulShutdown;
        return this;
    }

    public AgentServerConfigurationBuilder shutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    public AgentServerConfigurationBuilder enableTaskManagement(boolean enableTaskManagement) {
        this.enableTaskManagement = enableTaskManagement;
        return this;
    }

    public AgentServerConfigurationBuilder enableMessageRouting(boolean enableMessageRouting) {
        this.enableMessageRouting = enableMessageRouting;
        return this;
    }

    public AgentServerConfigurationBuilder maxTaskQueueSize(int maxTaskQueueSize) {
        this.maxTaskQueueSize = maxTaskQueueSize;
        return this;
    }

    public AgentServerConfigurationBuilder taskTimeoutSeconds(int taskTimeoutSeconds) {
        this.taskTimeoutSeconds = taskTimeoutSeconds;
        return this;
    }

    public AgentServerConfigurationBuilder enableTaskPersistence(boolean enableTaskPersistence) {
        this.enableTaskPersistence = enableTaskPersistence;
        return this;
    }

    public AgentServerConfigurationBuilder serverOptions(Map<String, Object> serverOptions) {
        this.serverOptions = serverOptions;
        return this;
    }

    public AgentServerConfiguration build() {
        return new AgentServerConfiguration(this);
    }
}
