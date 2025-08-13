package org.openhab.core.ai.agent.lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for A2A server.
 * 
 * This configuration class provides settings for the A2A (Agent-to-Agent) protocol
 * server integration with openHAB's HTTP server using the OSGi HTTP Whiteboard pattern.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentServerConfiguration {

    private final String serverId;
    private final String serverName;
    private final String serverVersion;
    private final String baseUrl;
    private final String servletPath;
    private final String servletPattern;

    // Protocol endpoints
    private final String messageSendEndpoint;
    private final String taskGetEndpoint;
    private final String taskCancelEndpoint;
    private final String healthEndpoint;
    private final String statusEndpoint;
    private final String agentCardEndpoint;

    // Security configuration
    private final boolean enableAuthentication;
    private final String primaryAuthMethod; // oauth2.1, openhab_users, api_key, jwt
    private final String fallbackAuthMethod; // openhab_users, api_key, jwt, none
    private final boolean enableFallbackAuth;
    private final int maxConnections;
    private final int rateLimitPerMinute;
    private final boolean enableRequestValidation;

    // OAuth 2.1 configuration
    private final String oauthIssuerUrl;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthRedirectUri;
    private final boolean oauthPkceEnabled;

    // openHAB users authentication
    private final String openhabUsersFile;
    private final boolean openhabUsersEnabled;

    // API key authentication
    private final String apiKeyHeader;
    private final String apiKeyValue;
    private final boolean apiKeyEnabled;

    // JWT authentication
    private final String jwtSecret;
    private final String jwtIssuer;
    private final int jwtExpirationMinutes;
    private final boolean jwtEnabled;

    // Monitoring and metrics
    private final boolean enableMetrics;
    private final boolean enableHealthChecks;
    private final int healthCheckInterval;
    private final boolean enablePerformanceMonitoring;

    // Production settings
    private final boolean productionMode;
    private final int requestTimeout;
    private final int connectionTimeout;
    private final boolean enableGracefulShutdown;
    private final int shutdownTimeout;

    // A2A-specific settings
    private final boolean enableTaskManagement;
    private final boolean enableMessageRouting;
    private final int maxTaskQueueSize;
    private final int taskTimeoutSeconds;
    private final boolean enableTaskPersistence;

    private final Map<String, Object> serverOptions;

    /**
     * Private constructor for builder pattern.
     */
    public AgentServerConfiguration(AgentServerConfigurationBuilder builder) {
        this.serverId = builder.getServerId();
        this.serverName = builder.getServerName();
        this.serverVersion = builder.getServerVersion();
        this.baseUrl = builder.getBaseUrl();
        this.servletPath = builder.getServletPath();
        this.servletPattern = builder.getServletPattern();
        this.messageSendEndpoint = builder.getMessageSendEndpoint();
        this.taskGetEndpoint = builder.getTaskGetEndpoint();
        this.taskCancelEndpoint = builder.getTaskCancelEndpoint();
        this.healthEndpoint = builder.getHealthEndpoint();
        this.statusEndpoint = builder.getStatusEndpoint();
        this.agentCardEndpoint = builder.getAgentCardEndpoint();
        this.enableAuthentication = builder.isEnableAuthentication();
        this.primaryAuthMethod = builder.getPrimaryAuthMethod();
        this.fallbackAuthMethod = builder.getFallbackAuthMethod();
        this.enableFallbackAuth = builder.isEnableFallbackAuth();
        this.maxConnections = builder.getMaxConnections();
        this.rateLimitPerMinute = builder.getRateLimitPerMinute();
        this.enableRequestValidation = builder.isEnableRequestValidation();
        this.oauthIssuerUrl = builder.getOauthIssuerUrl();
        this.oauthClientId = builder.getOauthClientId();
        this.oauthClientSecret = builder.getOauthClientSecret();
        this.oauthRedirectUri = builder.getOauthRedirectUri();
        this.oauthPkceEnabled = builder.isOauthPkceEnabled();
        this.openhabUsersFile = builder.getOpenhabUsersFile();
        this.openhabUsersEnabled = builder.isOpenhabUsersEnabled();
        this.apiKeyHeader = builder.getApiKeyHeader();
        this.apiKeyValue = builder.getApiKeyValue();
        this.apiKeyEnabled = builder.isApiKeyEnabled();
        this.jwtSecret = builder.getJwtSecret();
        this.jwtIssuer = builder.getJwtIssuer();
        this.jwtExpirationMinutes = builder.getJwtExpirationMinutes();
        this.jwtEnabled = builder.isJwtEnabled();
        this.enableMetrics = builder.isEnableMetrics();
        this.enableHealthChecks = builder.isEnableHealthChecks();
        this.healthCheckInterval = builder.getHealthCheckInterval();
        this.enablePerformanceMonitoring = builder.isEnablePerformanceMonitoring();
        this.productionMode = builder.isProductionMode();
        this.requestTimeout = builder.getRequestTimeout();
        this.connectionTimeout = builder.getConnectionTimeout();
        this.enableGracefulShutdown = builder.isEnableGracefulShutdown();
        this.shutdownTimeout = builder.getShutdownTimeout();
        this.enableTaskManagement = builder.isEnableTaskManagement();
        this.enableMessageRouting = builder.isEnableMessageRouting();
        this.maxTaskQueueSize = builder.getMaxTaskQueueSize();
        this.taskTimeoutSeconds = builder.getTaskTimeoutSeconds();
        this.enableTaskPersistence = builder.isEnableTaskPersistence();
        this.serverOptions = new HashMap<>(builder.getServerOptions());
    }

    /**
     * Create a new builder for A2A server configuration.
     * 
     * @return the builder
     */
    public static AgentServerConfigurationBuilder builder() {
        return new AgentServerConfigurationBuilder();
    }

    // Getters
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
        return new HashMap<>(serverOptions);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentServerConfiguration that = (AgentServerConfiguration) o;
        return Objects.equals(serverId, that.serverId) && Objects.equals(serverName, that.serverName)
                && Objects.equals(serverVersion, that.serverVersion) && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(servletPath, that.servletPath) && Objects.equals(servletPattern, that.servletPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, serverName, serverVersion, baseUrl, servletPath, servletPattern);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentServerConfiguration{serverId='%s', serverName='%s', serverVersion='%s', baseUrl='%s', servletPath='%s', servletPattern='%s', agentCardEndpoint='%s'}",
                serverId, serverName, serverVersion, baseUrl, servletPath, servletPattern, agentCardEndpoint);
    }
}
