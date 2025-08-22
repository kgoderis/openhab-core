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

    private AgentServerConfiguration(Builder builder) {
        this.serverId = builder.serverId;
        this.serverName = builder.serverName;
        this.serverVersion = builder.serverVersion;
        this.baseUrl = builder.baseUrl;
        this.servletPath = builder.servletPath;
        this.servletPattern = builder.servletPattern;
        this.messageSendEndpoint = builder.messageSendEndpoint;
        this.taskGetEndpoint = builder.taskGetEndpoint;
        this.taskCancelEndpoint = builder.taskCancelEndpoint;
        this.healthEndpoint = builder.healthEndpoint;
        this.statusEndpoint = builder.statusEndpoint;
        this.agentCardEndpoint = builder.agentCardEndpoint;
        this.enableAuthentication = builder.enableAuthentication;
        this.primaryAuthMethod = builder.primaryAuthMethod;
        this.fallbackAuthMethod = builder.fallbackAuthMethod;
        this.enableFallbackAuth = builder.enableFallbackAuth;
        this.maxConnections = builder.maxConnections;
        this.rateLimitPerMinute = builder.rateLimitPerMinute;
        this.enableRequestValidation = builder.enableRequestValidation;
        this.oauthIssuerUrl = builder.oauthIssuerUrl;
        this.oauthClientId = builder.oauthClientId;
        this.oauthClientSecret = builder.oauthClientSecret;
        this.oauthRedirectUri = builder.oauthRedirectUri;
        this.oauthPkceEnabled = builder.oauthPkceEnabled;
        this.openhabUsersFile = builder.openhabUsersFile;
        this.openhabUsersEnabled = builder.openhabUsersEnabled;
        this.apiKeyHeader = builder.apiKeyHeader;
        this.apiKeyValue = builder.apiKeyValue;
        this.apiKeyEnabled = builder.apiKeyEnabled;
        this.jwtSecret = builder.jwtSecret;
        this.jwtIssuer = builder.jwtIssuer;
        this.jwtExpirationMinutes = builder.jwtExpirationMinutes;
        this.jwtEnabled = builder.jwtEnabled;
        this.enableMetrics = builder.enableMetrics;
        this.enableHealthChecks = builder.enableHealthChecks;
        this.healthCheckInterval = builder.healthCheckInterval;
        this.enablePerformanceMonitoring = builder.enablePerformanceMonitoring;
        this.productionMode = builder.productionMode;
        this.requestTimeout = builder.requestTimeout;
        this.connectionTimeout = builder.connectionTimeout;
        this.enableGracefulShutdown = builder.enableGracefulShutdown;
        this.shutdownTimeout = builder.shutdownTimeout;
        this.enableTaskManagement = builder.enableTaskManagement;
        this.enableMessageRouting = builder.enableMessageRouting;
        this.maxTaskQueueSize = builder.maxTaskQueueSize;
        this.taskTimeoutSeconds = builder.taskTimeoutSeconds;
        this.enableTaskPersistence = builder.enableTaskPersistence;
        this.serverOptions = Map.copyOf(builder.serverOptions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public static final class Builder {
        private String serverId = "default-a2a-server-id";
        private String serverName = "openHAB A2A Server";
        private String serverVersion = "1.0.0";
        private String baseUrl = "http://localhost:8080";
        private String servletPath = "/a2a";
        private String servletPattern = "/a2a/*";

        // Protocol endpoints
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

        public Builder() {
        }

        public Builder(AgentServerConfiguration source) {
            this.serverId = source.serverId;
            this.serverName = source.serverName;
            this.serverVersion = source.serverVersion;
            this.baseUrl = source.baseUrl;
            this.servletPath = source.servletPath;
            this.servletPattern = source.servletPattern;
            this.messageSendEndpoint = source.messageSendEndpoint;
            this.taskGetEndpoint = source.taskGetEndpoint;
            this.taskCancelEndpoint = source.taskCancelEndpoint;
            this.healthEndpoint = source.healthEndpoint;
            this.statusEndpoint = source.statusEndpoint;
            this.agentCardEndpoint = source.agentCardEndpoint;
            this.enableAuthentication = source.enableAuthentication;
            this.primaryAuthMethod = source.primaryAuthMethod;
            this.fallbackAuthMethod = source.fallbackAuthMethod;
            this.enableFallbackAuth = source.enableFallbackAuth;
            this.maxConnections = source.maxConnections;
            this.rateLimitPerMinute = source.rateLimitPerMinute;
            this.enableRequestValidation = source.enableRequestValidation;
            this.oauthIssuerUrl = source.oauthIssuerUrl;
            this.oauthClientId = source.oauthClientId;
            this.oauthClientSecret = source.oauthClientSecret;
            this.oauthRedirectUri = source.oauthRedirectUri;
            this.oauthPkceEnabled = source.oauthPkceEnabled;
            this.openhabUsersFile = source.openhabUsersFile;
            this.openhabUsersEnabled = source.openhabUsersEnabled;
            this.apiKeyHeader = source.apiKeyHeader;
            this.apiKeyValue = source.apiKeyValue;
            this.apiKeyEnabled = source.apiKeyEnabled;
            this.jwtSecret = source.jwtSecret;
            this.jwtIssuer = source.jwtIssuer;
            this.jwtExpirationMinutes = source.jwtExpirationMinutes;
            this.jwtEnabled = source.jwtEnabled;
            this.enableMetrics = source.enableMetrics;
            this.enableHealthChecks = source.enableHealthChecks;
            this.healthCheckInterval = source.healthCheckInterval;
            this.enablePerformanceMonitoring = source.enablePerformanceMonitoring;
            this.productionMode = source.productionMode;
            this.requestTimeout = source.requestTimeout;
            this.connectionTimeout = source.connectionTimeout;
            this.enableGracefulShutdown = source.enableGracefulShutdown;
            this.shutdownTimeout = source.shutdownTimeout;
            this.enableTaskManagement = source.enableTaskManagement;
            this.enableMessageRouting = source.enableMessageRouting;
            this.maxTaskQueueSize = source.maxTaskQueueSize;
            this.taskTimeoutSeconds = source.taskTimeoutSeconds;
            this.enableTaskPersistence = source.enableTaskPersistence;
            this.serverOptions = new HashMap<>(source.serverOptions);
        }

        public Builder withServerId(String serverId) {
            this.serverId = Objects.requireNonNull(serverId, "serverId");
            return this;
        }

        public Builder withServerName(String serverName) {
            this.serverName = Objects.requireNonNull(serverName, "serverName");
            return this;
        }

        public Builder withServerVersion(String serverVersion) {
            this.serverVersion = Objects.requireNonNull(serverVersion, "serverVersion");
            return this;
        }

        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        public Builder withServletPath(String servletPath) {
            this.servletPath = Objects.requireNonNull(servletPath, "servletPath");
            return this;
        }

        public Builder withServletPattern(String servletPattern) {
            this.servletPattern = Objects.requireNonNull(servletPattern, "servletPattern");
            return this;
        }

        public Builder withMessageSendEndpoint(String messageSendEndpoint) {
            this.messageSendEndpoint = Objects.requireNonNull(messageSendEndpoint, "messageSendEndpoint");
            return this;
        }

        public Builder withTaskGetEndpoint(String taskGetEndpoint) {
            this.taskGetEndpoint = Objects.requireNonNull(taskGetEndpoint, "taskGetEndpoint");
            return this;
        }

        public Builder withTaskCancelEndpoint(String taskCancelEndpoint) {
            this.taskCancelEndpoint = Objects.requireNonNull(taskCancelEndpoint, "taskCancelEndpoint");
            return this;
        }

        public Builder withHealthEndpoint(String healthEndpoint) {
            this.healthEndpoint = Objects.requireNonNull(healthEndpoint, "healthEndpoint");
            return this;
        }

        public Builder withStatusEndpoint(String statusEndpoint) {
            this.statusEndpoint = Objects.requireNonNull(statusEndpoint, "statusEndpoint");
            return this;
        }

        public Builder withAgentCardEndpoint(String agentCardEndpoint) {
            this.agentCardEndpoint = Objects.requireNonNull(agentCardEndpoint, "agentCardEndpoint");
            return this;
        }

        public Builder withEnableAuthentication(boolean enableAuthentication) {
            this.enableAuthentication = enableAuthentication;
            return this;
        }

        public Builder withPrimaryAuthMethod(String primaryAuthMethod) {
            this.primaryAuthMethod = Objects.requireNonNull(primaryAuthMethod, "primaryAuthMethod");
            return this;
        }

        public Builder withFallbackAuthMethod(String fallbackAuthMethod) {
            this.fallbackAuthMethod = Objects.requireNonNull(fallbackAuthMethod, "fallbackAuthMethod");
            return this;
        }

        public Builder withEnableFallbackAuth(boolean enableFallbackAuth) {
            this.enableFallbackAuth = enableFallbackAuth;
            return this;
        }

        public Builder withMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder withRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
            return this;
        }

        public Builder withEnableRequestValidation(boolean enableRequestValidation) {
            this.enableRequestValidation = enableRequestValidation;
            return this;
        }

        public Builder withOauthIssuerUrl(String oauthIssuerUrl) {
            this.oauthIssuerUrl = Objects.requireNonNull(oauthIssuerUrl, "oauthIssuerUrl");
            return this;
        }

        public Builder withOauthClientId(String oauthClientId) {
            this.oauthClientId = Objects.requireNonNull(oauthClientId, "oauthClientId");
            return this;
        }

        public Builder withOauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = Objects.requireNonNull(oauthClientSecret, "oauthClientSecret");
            return this;
        }

        public Builder withOauthRedirectUri(String oauthRedirectUri) {
            this.oauthRedirectUri = Objects.requireNonNull(oauthRedirectUri, "oauthRedirectUri");
            return this;
        }

        public Builder withOauthPkceEnabled(boolean oauthPkceEnabled) {
            this.oauthPkceEnabled = oauthPkceEnabled;
            return this;
        }

        public Builder withOpenhabUsersFile(String openhabUsersFile) {
            this.openhabUsersFile = Objects.requireNonNull(openhabUsersFile, "openhabUsersFile");
            return this;
        }

        public Builder withOpenhabUsersEnabled(boolean openhabUsersEnabled) {
            this.openhabUsersEnabled = openhabUsersEnabled;
            return this;
        }

        public Builder withApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = Objects.requireNonNull(apiKeyHeader, "apiKeyHeader");
            return this;
        }

        public Builder withApiKeyValue(String apiKeyValue) {
            this.apiKeyValue = Objects.requireNonNull(apiKeyValue, "apiKeyValue");
            return this;
        }

        public Builder withApiKeyEnabled(boolean apiKeyEnabled) {
            this.apiKeyEnabled = apiKeyEnabled;
            return this;
        }

        public Builder withJwtSecret(String jwtSecret) {
            this.jwtSecret = Objects.requireNonNull(jwtSecret, "jwtSecret");
            return this;
        }

        public Builder withJwtIssuer(String jwtIssuer) {
            this.jwtIssuer = Objects.requireNonNull(jwtIssuer, "jwtIssuer");
            return this;
        }

        public Builder withJwtExpirationMinutes(int jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
            return this;
        }

        public Builder withJwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
            return this;
        }

        public Builder withEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }

        public Builder withEnableHealthChecks(boolean enableHealthChecks) {
            this.enableHealthChecks = enableHealthChecks;
            return this;
        }

        public Builder withHealthCheckInterval(int healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
            return this;
        }

        public Builder withEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
            this.enablePerformanceMonitoring = enablePerformanceMonitoring;
            return this;
        }

        public Builder withProductionMode(boolean productionMode) {
            this.productionMode = productionMode;
            return this;
        }

        public Builder withRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder withEnableGracefulShutdown(boolean enableGracefulShutdown) {
            this.enableGracefulShutdown = enableGracefulShutdown;
            return this;
        }

        public Builder withShutdownTimeout(int shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        public Builder withEnableTaskManagement(boolean enableTaskManagement) {
            this.enableTaskManagement = enableTaskManagement;
            return this;
        }

        public Builder withEnableMessageRouting(boolean enableMessageRouting) {
            this.enableMessageRouting = enableMessageRouting;
            return this;
        }

        public Builder withMaxTaskQueueSize(int maxTaskQueueSize) {
            this.maxTaskQueueSize = maxTaskQueueSize;
            return this;
        }

        public Builder withTaskTimeoutSeconds(int taskTimeoutSeconds) {
            this.taskTimeoutSeconds = taskTimeoutSeconds;
            return this;
        }

        public Builder withEnableTaskPersistence(boolean enableTaskPersistence) {
            this.enableTaskPersistence = enableTaskPersistence;
            return this;
        }

        public Builder withServerOption(String key, Object value) {
            this.serverOptions.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public AgentServerConfiguration build() {
            if (serverId == null || serverId.isBlank()) {
                throw new IllegalArgumentException("serverId must not be null or blank");
            }
            if (serverName == null || serverName.isBlank()) {
                throw new IllegalArgumentException("serverName must not be null or blank");
            }
            if (serverVersion == null || serverVersion.isBlank()) {
                throw new IllegalArgumentException("serverVersion must not be null or blank");
            }
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl must not be null or blank");
            }
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("maxConnections must be positive");
            }
            if (rateLimitPerMinute <= 0) {
                throw new IllegalArgumentException("rateLimitPerMinute must be positive");
            }
            if (requestTimeout <= 0) {
                throw new IllegalArgumentException("requestTimeout must be positive");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("connectionTimeout must be positive");
            }
            if (maxTaskQueueSize <= 0) {
                throw new IllegalArgumentException("maxTaskQueueSize must be positive");
            }
            if (taskTimeoutSeconds <= 0) {
                throw new IllegalArgumentException("taskTimeoutSeconds must be positive");
            }
            return new AgentServerConfiguration(this);
        }
    }
}
