package org.openhab.core.ai.agent.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.ServerConfiguration;

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
public class AgentServerConfiguration extends ServerConfiguration {

    // A2A-specific protocol endpoints
    private final String messageSendEndpoint;
    private final String taskGetEndpoint;
    private final String taskCancelEndpoint;
    private final String agentCardEndpoint;

    // A2A-specific settings
    private final boolean enableTaskManagement;
    private final boolean enableMessageRouting;
    private final int maxTaskQueueSize;
    private final int taskTimeoutSeconds;
    private final boolean enableTaskPersistence;

    private AgentServerConfiguration(Builder builder) {
        super(builder.serverId, builder.enabled, builder.serverName, builder.serverVersion, builder.serverOptions,
                builder.baseUrl, builder.port, builder.contextPath, builder.servletPath, builder.servletPattern,
                builder.messageSendEndpoint, builder.healthEndpoint, builder.statusEndpoint, builder.metricsEndpoint,
                builder.enableAuthentication, builder.primaryAuthMethod, builder.fallbackAuthMethod,
                builder.enableFallbackAuth, builder.maxConnections, builder.rateLimitPerMinute,
                builder.enableRequestValidation, builder.oauthIssuerUrl, builder.oauthClientId,
                builder.oauthClientSecret, builder.oauthRedirectUri, builder.oauthPkceEnabled, builder.openhabUsersFile,
                builder.openhabUsersEnabled, builder.apiKeyHeader, builder.apiKeyValue, builder.apiKeyEnabled,
                builder.jwtSecret, builder.jwtIssuer, builder.jwtExpirationMinutes, builder.jwtEnabled,
                builder.enableMetrics, builder.enableHealthChecks, builder.healthCheckInterval,
                builder.enablePerformanceMonitoring, builder.productionMode, builder.requestTimeout,
                builder.connectionTimeout, builder.enableGracefulShutdown, builder.shutdownTimeout, builder.enableCors,
                builder.corsAllowedOrigins, builder.corsAllowedMethods, builder.corsAllowedHeaders, builder.enableSsl,
                builder.sslKeyStore, builder.sslKeyStorePassword, builder.sslTrustStore, builder.sslTrustStorePassword);

        this.messageSendEndpoint = builder.messageSendEndpoint;
        this.taskGetEndpoint = builder.taskGetEndpoint;
        this.taskCancelEndpoint = builder.taskCancelEndpoint;
        this.agentCardEndpoint = builder.agentCardEndpoint;
        this.enableTaskManagement = builder.enableTaskManagement;
        this.enableMessageRouting = builder.enableMessageRouting;
        this.maxTaskQueueSize = builder.maxTaskQueueSize;
        this.taskTimeoutSeconds = builder.taskTimeoutSeconds;
        this.enableTaskPersistence = builder.enableTaskPersistence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    // A2A-specific getters
    public String getMessageSendEndpoint() {
        return messageSendEndpoint;
    }

    public String getTaskGetEndpoint() {
        return taskGetEndpoint;
    }

    public String getTaskCancelEndpoint() {
        return taskCancelEndpoint;
    }

    public String getAgentCardEndpoint() {
        return agentCardEndpoint;
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o) || getClass() != o.getClass()) {
            return false;
        }
        AgentServerConfiguration that = (AgentServerConfiguration) o;
        return Objects.equals(messageSendEndpoint, that.messageSendEndpoint)
                && Objects.equals(taskGetEndpoint, that.taskGetEndpoint)
                && Objects.equals(taskCancelEndpoint, that.taskCancelEndpoint)
                && Objects.equals(agentCardEndpoint, that.agentCardEndpoint)
                && enableTaskManagement == that.enableTaskManagement
                && enableMessageRouting == that.enableMessageRouting && maxTaskQueueSize == that.maxTaskQueueSize
                && taskTimeoutSeconds == that.taskTimeoutSeconds && enableTaskPersistence == that.enableTaskPersistence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messageSendEndpoint, taskGetEndpoint, taskCancelEndpoint,
                agentCardEndpoint, enableTaskManagement, enableMessageRouting, maxTaskQueueSize, taskTimeoutSeconds,
                enableTaskPersistence);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentServerConfiguration{id='%s', name='%s', baseUrl='%s', messageSendEndpoint='%s', taskGetEndpoint='%s', agentCardEndpoint='%s'}",
                getId(), getName(), getBaseUrl(), messageSendEndpoint, taskGetEndpoint, agentCardEndpoint);
    }

    public static final class Builder {
        private String serverId = "default-a2a-server-id";
        private String serverName = "openHAB A2A Server";
        private String serverVersion = "1.0.0";
        private boolean enabled = true;
        private String baseUrl = "http://localhost:8080";
        private int port = 8080;
        private String contextPath = "/";
        private String servletPath = "/a2a";
        private String servletPattern = "/a2a/*";
        private String metricsEndpoint = "/a2a/metrics";

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

        // CORS configuration
        private boolean enableCors = true;
        private String corsAllowedOrigins = "*";
        private String corsAllowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String corsAllowedHeaders = "Content-Type,Authorization,X-API-Key";

        // SSL/TLS configuration
        private boolean enableSsl = false;
        private String sslKeyStore = "";
        private String sslKeyStorePassword = "";
        private String sslTrustStore = "";
        private String sslTrustStorePassword = "";

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
            // Copy inherited fields using parent getters
            this.serverId = source.getId();
            this.serverName = source.getName();
            this.serverVersion = source.getVersion();
            this.enabled = source.isEnabled();
            this.baseUrl = source.getBaseUrl();
            this.port = source.getPort();
            this.contextPath = source.getContextPath();
            this.servletPath = source.getServletPath();
            this.servletPattern = source.getServletPattern();
            this.metricsEndpoint = source.getMetricsEndpoint();
            this.messageSendEndpoint = source.getMessageSendEndpoint();
            this.taskGetEndpoint = source.getTaskGetEndpoint();
            this.taskCancelEndpoint = source.getTaskCancelEndpoint();
            this.healthEndpoint = source.getHealthEndpoint();
            this.statusEndpoint = source.getStatusEndpoint();
            this.agentCardEndpoint = source.getAgentCardEndpoint();
            this.enableAuthentication = source.isEnableAuthentication();
            this.primaryAuthMethod = source.getPrimaryAuthMethod();
            this.fallbackAuthMethod = source.getFallbackAuthMethod();
            this.enableFallbackAuth = source.isEnableFallbackAuth();
            this.maxConnections = source.getMaxConnections();
            this.rateLimitPerMinute = source.getRateLimitPerMinute();
            this.enableRequestValidation = source.isEnableRequestValidation();
            this.oauthIssuerUrl = source.getOauthIssuerUrl();
            this.oauthClientId = source.getOauthClientId();
            this.oauthClientSecret = source.getOauthClientSecret();
            this.oauthRedirectUri = source.getOauthRedirectUri();
            this.oauthPkceEnabled = source.isOauthPkceEnabled();
            this.openhabUsersFile = source.getOpenhabUsersFile();
            this.openhabUsersEnabled = source.isOpenhabUsersEnabled();
            this.apiKeyHeader = source.getApiKeyHeader();
            this.apiKeyValue = source.getApiKeyValue();
            this.apiKeyEnabled = source.isApiKeyEnabled();
            this.jwtSecret = source.getJwtSecret();
            this.jwtIssuer = source.getJwtIssuer();
            this.jwtExpirationMinutes = source.getJwtExpirationMinutes();
            this.jwtEnabled = source.isJwtEnabled();
            this.enableMetrics = source.isEnableMetrics();
            this.enableHealthChecks = source.isEnableHealthChecks();
            this.healthCheckInterval = source.getHealthCheckInterval();
            this.enablePerformanceMonitoring = source.isEnablePerformanceMonitoring();
            this.productionMode = source.isProductionMode();
            this.requestTimeout = source.getRequestTimeout();
            this.connectionTimeout = source.getConnectionTimeout();
            this.enableGracefulShutdown = source.isEnableGracefulShutdown();
            this.shutdownTimeout = source.getShutdownTimeout();
            this.enableCors = source.isEnableCors();
            this.corsAllowedOrigins = source.getCorsAllowedOrigins();
            this.corsAllowedMethods = source.getCorsAllowedMethods();
            this.corsAllowedHeaders = source.getCorsAllowedHeaders();
            this.enableSsl = source.isEnableSsl();
            this.sslKeyStore = source.getSslKeyStore();
            this.sslKeyStorePassword = source.getSslKeyStorePassword();
            this.sslTrustStore = source.getSslTrustStore();
            this.sslTrustStorePassword = source.getSslTrustStorePassword();
            this.enableTaskManagement = source.isEnableTaskManagement();
            this.enableMessageRouting = source.isEnableMessageRouting();
            this.maxTaskQueueSize = source.getMaxTaskQueueSize();
            this.taskTimeoutSeconds = source.getTaskTimeoutSeconds();
            this.enableTaskPersistence = source.isEnableTaskPersistence();
            this.serverOptions = new HashMap<>(source.getCustomOptions());
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
