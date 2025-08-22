package org.openhab.core.ai.common.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified server configuration for AI components.
 * 
 * This class consolidates common server configuration patterns from various
 * AI components, providing a unified interface for server settings including
 * HTTP server, security, authentication, and monitoring configuration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServerConfiguration extends BaseConfiguration {

    // Server settings
    private final String baseUrl;
    private final int port;
    private final String contextPath;
    private final String servletPath;
    private final String servletPattern;

    // Protocol-specific endpoints
    private final String messageEndpoint;
    private final String healthEndpoint;
    private final String statusEndpoint;
    private final String metricsEndpoint;

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

    // CORS configuration
    private final boolean enableCors;
    private final String corsAllowedOrigins;
    private final String corsAllowedMethods;
    private final String corsAllowedHeaders;

    // SSL/TLS configuration
    private final boolean enableSsl;
    private final String sslKeyStore;
    private final String sslKeyStorePassword;
    private final String sslTrustStore;
    private final String sslTrustStorePassword;

    /**
     * Private constructor for builder pattern.
     */
    private ServerConfiguration(Builder builder) {
        super("server-" + builder.host + "-" + builder.port, builder.enabled, builder.name, builder.version,
                builder.settings);
        // Use builder fields with defaults for missing values
        this.baseUrl = "http://" + builder.host + ":" + builder.port;
        this.port = builder.port;
        this.contextPath = "/";
        this.servletPath = "/";
        this.servletPattern = "/*";
        this.messageEndpoint = "/api/messages";
        this.healthEndpoint = "/health";
        this.statusEndpoint = "/status";
        this.metricsEndpoint = "/metrics";
        this.enableAuthentication = false;
        this.primaryAuthMethod = "none";
        this.fallbackAuthMethod = "none";
        this.enableFallbackAuth = false;
        this.maxConnections = 100;
        this.rateLimitPerMinute = 1000;
        this.enableRequestValidation = true;
        this.oauthIssuerUrl = "";
        this.oauthClientId = "";
        this.oauthClientSecret = "";
        this.oauthRedirectUri = "";
        this.oauthPkceEnabled = false;
        this.openhabUsersFile = "";
        this.openhabUsersEnabled = false;
        this.apiKeyHeader = "";
        this.apiKeyValue = "";
        this.apiKeyEnabled = false;
        this.jwtSecret = "";
        this.jwtIssuer = "";
        this.jwtExpirationMinutes = 60;
        this.jwtEnabled = false;
        this.enableMetrics = true;
        this.enableHealthChecks = true;
        this.healthCheckInterval = 30;
        this.enablePerformanceMonitoring = false;
        this.productionMode = false;
        this.requestTimeout = 30000;
        this.connectionTimeout = 10000;
        this.enableGracefulShutdown = true;
        this.shutdownTimeout = 30;
        this.enableCors = false;
        this.corsAllowedOrigins = "*";
        this.corsAllowedMethods = "GET,POST,PUT,DELETE";
        this.corsAllowedHeaders = "*";
        this.enableSsl = false;
        this.sslKeyStore = "";
        this.sslKeyStorePassword = "";
        this.sslTrustStore = "";
        this.sslTrustStorePassword = "";
    }

    /**
     * Protected constructor for subclasses.
     */
    protected ServerConfiguration(String id, boolean enabled, String name, String version,
            @Nullable Map<String, Object> customOptions, String baseUrl, int port, String contextPath,
            String servletPath, String servletPattern, String messageEndpoint, String healthEndpoint,
            String statusEndpoint, String metricsEndpoint, boolean enableAuthentication, String primaryAuthMethod,
            String fallbackAuthMethod, boolean enableFallbackAuth, int maxConnections, int rateLimitPerMinute,
            boolean enableRequestValidation, String oauthIssuerUrl, String oauthClientId, String oauthClientSecret,
            String oauthRedirectUri, boolean oauthPkceEnabled, String openhabUsersFile, boolean openhabUsersEnabled,
            String apiKeyHeader, String apiKeyValue, boolean apiKeyEnabled, String jwtSecret, String jwtIssuer,
            int jwtExpirationMinutes, boolean jwtEnabled, boolean enableMetrics, boolean enableHealthChecks,
            int healthCheckInterval, boolean enablePerformanceMonitoring, boolean productionMode, int requestTimeout,
            int connectionTimeout, boolean enableGracefulShutdown, int shutdownTimeout, boolean enableCors,
            String corsAllowedOrigins, String corsAllowedMethods, String corsAllowedHeaders, boolean enableSsl,
            String sslKeyStore, String sslKeyStorePassword, String sslTrustStore, String sslTrustStorePassword) {
        super(id, enabled, name, version, customOptions);
        this.baseUrl = baseUrl;
        this.port = port;
        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.servletPattern = servletPattern;
        this.messageEndpoint = messageEndpoint;
        this.healthEndpoint = healthEndpoint;
        this.statusEndpoint = statusEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.enableAuthentication = enableAuthentication;
        this.primaryAuthMethod = primaryAuthMethod;
        this.fallbackAuthMethod = fallbackAuthMethod;
        this.enableFallbackAuth = enableFallbackAuth;
        this.maxConnections = maxConnections;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.enableRequestValidation = enableRequestValidation;
        this.oauthIssuerUrl = oauthIssuerUrl;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthRedirectUri = oauthRedirectUri;
        this.oauthPkceEnabled = oauthPkceEnabled;
        this.openhabUsersFile = openhabUsersFile;
        this.openhabUsersEnabled = openhabUsersEnabled;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeyValue = apiKeyValue;
        this.apiKeyEnabled = apiKeyEnabled;
        this.jwtSecret = jwtSecret;
        this.jwtIssuer = jwtIssuer;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        this.jwtEnabled = jwtEnabled;
        this.enableMetrics = enableMetrics;
        this.enableHealthChecks = enableHealthChecks;
        this.healthCheckInterval = healthCheckInterval;
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        this.productionMode = productionMode;
        this.requestTimeout = requestTimeout;
        this.connectionTimeout = connectionTimeout;
        this.enableGracefulShutdown = enableGracefulShutdown;
        this.shutdownTimeout = shutdownTimeout;
        this.enableCors = enableCors;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.corsAllowedMethods = corsAllowedMethods;
        this.corsAllowedHeaders = corsAllowedHeaders;
        this.enableSsl = enableSsl;
        this.sslKeyStore = sslKeyStore;
        this.sslKeyStorePassword = sslKeyStorePassword;
        this.sslTrustStore = sslTrustStore;
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    /**
     * Create a new builder for server configuration.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder from this configuration.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters
    public String getBaseUrl() {
        return baseUrl;
    }

    public int getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public String getServletPattern() {
        return servletPattern;
    }

    public String getMessageEndpoint() {
        return messageEndpoint;
    }

    public String getHealthEndpoint() {
        return healthEndpoint;
    }

    public String getStatusEndpoint() {
        return statusEndpoint;
    }

    public String getMetricsEndpoint() {
        return metricsEndpoint;
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

    public boolean isEnableCors() {
        return enableCors;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public String getSslTrustStore() {
        return sslTrustStore;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        ServerConfiguration other = (ServerConfiguration) obj;
        return Objects.equals(baseUrl, other.baseUrl) && port == other.port
                && Objects.equals(contextPath, other.contextPath) && Objects.equals(servletPath, other.servletPath)
                && Objects.equals(servletPattern, other.servletPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseUrl, port, contextPath, servletPath, servletPattern);
    }

    @Override
    public String toString() {
        return String.format(
                "ServerConfiguration{id='%s', name='%s', baseUrl='%s', port=%d, contextPath='%s', servletPath='%s'}",
                getId(), getName(), baseUrl, port, contextPath, servletPath);
    }

    /**
     * Builder for ServerConfiguration.
     * 
     * This builder provides a fluent API for creating ServerConfiguration instances.
     * The builder is not thread-safe and should be used for single-threaded construction.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String host = "localhost";
        private int port = 8080;
        private boolean enabled = true;
        private String name = "Default Server";
        private String version = "1.0.0";
        private Map<String, Object> settings = new HashMap<>();
        private String contextPath = "/";
        private String servletPath = "/api";
        private String servletPattern = "/api/*";
        private String messageEndpoint = "/messages";
        private String healthEndpoint = "/health";
        private String statusEndpoint = "/status";
        private String metricsEndpoint = "/metrics";
        private boolean enableAuthentication = false;
        private String primaryAuthMethod = "none";
        private String fallbackAuthMethod = "none";
        private boolean enableFallbackAuth = false;
        private int maxConnections = 100;
        private int rateLimitPerMinute = 60;
        private boolean enableRequestValidation = true;
        private String oauthIssuerUrl = "";
        private String oauthClientId = "";
        private String oauthClientSecret = "";
        private String oauthRedirectUri = "";
        private boolean oauthPkceEnabled = false;
        private String openhabUsersFile = "";
        private boolean openhabUsersEnabled = false;
        private String apiKeyHeader = "X-API-Key";
        private String apiKeyValue = "";
        private boolean apiKeyEnabled = false;
        private String jwtSecret = "";
        private String jwtIssuer = "";
        private int jwtExpirationMinutes = 60;
        private boolean jwtEnabled = false;
        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private int healthCheckInterval = 30;
        private boolean enablePerformanceMonitoring = false;
        private boolean productionMode = false;
        private int requestTimeout = 30000;
        private int connectionTimeout = 10000;
        private boolean enableGracefulShutdown = true;
        private int shutdownTimeout = 5000;
        private boolean enableCors = true;
        private String corsAllowedOrigins = "*";
        private String corsAllowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
        private String corsAllowedHeaders = "Content-Type, Authorization, X-API-Key";
        private boolean enableSsl = false;
        private String sslKeyStore = "";
        private String sslKeyStorePassword = "";
        private String sslTrustStore = "";
        private String sslTrustStorePassword = "";

        /**
         * Create a new builder with default values.
         */
        public Builder() {
        }

        /**
         * Create a new builder from an existing ServerConfiguration.
         * 
         * @param source the source configuration to copy from
         */
        public Builder(ServerConfiguration source) {
            this.host = "localhost"; // Extract from baseUrl if needed
            this.port = source.port;
            this.enabled = source.isEnabled();
            this.name = source.getName();
            this.version = source.getVersion();
            this.settings = new HashMap<>(source.getCustomOptions());
            this.contextPath = source.contextPath;
            this.servletPath = source.servletPath;
            this.servletPattern = source.servletPattern;
            this.messageEndpoint = source.messageEndpoint;
            this.healthEndpoint = source.healthEndpoint;
            this.statusEndpoint = source.statusEndpoint;
            this.metricsEndpoint = source.metricsEndpoint;
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
            this.enableCors = source.enableCors;
            this.corsAllowedOrigins = source.corsAllowedOrigins;
            this.corsAllowedMethods = source.corsAllowedMethods;
            this.corsAllowedHeaders = source.corsAllowedHeaders;
            this.enableSsl = source.enableSsl;
            this.sslKeyStore = source.sslKeyStore;
            this.sslKeyStorePassword = source.sslKeyStorePassword;
            this.sslTrustStore = source.sslTrustStore;
            this.sslTrustStorePassword = source.sslTrustStorePassword;
        }

        /**
         * Set the host.
         * 
         * @param host the host (cannot be null or blank)
         * @return this builder
         */
        public Builder withHost(String host) {
            this.host = Objects.requireNonNull(host, "host");
            return this;
        }

        /**
         * Set the port.
         * 
         * @param port the port (must be between 1 and 65535)
         * @return this builder
         */
        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set whether the server is enabled.
         * 
         * @param enabled whether the server is enabled
         * @return this builder
         */
        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Set the server name.
         * 
         * @param name the server name (cannot be null or blank)
         * @return this builder
         */
        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        /**
         * Set the server version.
         * 
         * @param version the server version (cannot be null or blank)
         * @return this builder
         */
        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        /**
         * Set the settings map.
         * 
         * @param settings the settings map (cannot be null)
         * @return this builder
         */
        public Builder withSettings(Map<String, Object> settings) {
            this.settings = new HashMap<>(Objects.requireNonNull(settings, "settings"));
            return this;
        }

        /**
         * Add a setting.
         * 
         * @param key the setting key (cannot be null)
         * @param value the setting value (cannot be null)
         * @return this builder
         */
        public Builder withSetting(String key, Object value) {
            this.settings.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set the context path.
         * 
         * @param contextPath the context path (cannot be null or blank)
         * @return this builder
         */
        public Builder withContextPath(String contextPath) {
            this.contextPath = Objects.requireNonNull(contextPath, "contextPath");
            return this;
        }

        /**
         * Set the servlet path.
         * 
         * @param servletPath the servlet path (cannot be null or blank)
         * @return this builder
         */
        public Builder withServletPath(String servletPath) {
            this.servletPath = Objects.requireNonNull(servletPath, "servletPath");
            return this;
        }

        /**
         * Set the servlet pattern.
         * 
         * @param servletPattern the servlet pattern (cannot be null or blank)
         * @return this builder
         */
        public Builder withServletPattern(String servletPattern) {
            this.servletPattern = Objects.requireNonNull(servletPattern, "servletPattern");
            return this;
        }

        /**
         * Set the message endpoint.
         * 
         * @param messageEndpoint the message endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withMessageEndpoint(String messageEndpoint) {
            this.messageEndpoint = Objects.requireNonNull(messageEndpoint, "messageEndpoint");
            return this;
        }

        /**
         * Set the health endpoint.
         * 
         * @param healthEndpoint the health endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withHealthEndpoint(String healthEndpoint) {
            this.healthEndpoint = Objects.requireNonNull(healthEndpoint, "healthEndpoint");
            return this;
        }

        /**
         * Set the status endpoint.
         * 
         * @param statusEndpoint the status endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withStatusEndpoint(String statusEndpoint) {
            this.statusEndpoint = Objects.requireNonNull(statusEndpoint, "statusEndpoint");
            return this;
        }

        /**
         * Set the metrics endpoint.
         * 
         * @param metricsEndpoint the metrics endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withMetricsEndpoint(String metricsEndpoint) {
            this.metricsEndpoint = Objects.requireNonNull(metricsEndpoint, "metricsEndpoint");
            return this;
        }

        /**
         * Set whether authentication is enabled.
         * 
         * @param enableAuthentication whether authentication is enabled
         * @return this builder
         */
        public Builder withEnableAuthentication(boolean enableAuthentication) {
            this.enableAuthentication = enableAuthentication;
            return this;
        }

        /**
         * Set the primary authentication method.
         * 
         * @param primaryAuthMethod the primary authentication method (cannot be null or blank)
         * @return this builder
         */
        public Builder withPrimaryAuthMethod(String primaryAuthMethod) {
            this.primaryAuthMethod = Objects.requireNonNull(primaryAuthMethod, "primaryAuthMethod");
            return this;
        }

        /**
         * Set the fallback authentication method.
         * 
         * @param fallbackAuthMethod the fallback authentication method (cannot be null or blank)
         * @return this builder
         */
        public Builder withFallbackAuthMethod(String fallbackAuthMethod) {
            this.fallbackAuthMethod = Objects.requireNonNull(fallbackAuthMethod, "fallbackAuthMethod");
            return this;
        }

        /**
         * Set whether fallback authentication is enabled.
         * 
         * @param enableFallbackAuth whether fallback authentication is enabled
         * @return this builder
         */
        public Builder withEnableFallbackAuth(boolean enableFallbackAuth) {
            this.enableFallbackAuth = enableFallbackAuth;
            return this;
        }

        /**
         * Set the maximum connections.
         * 
         * @param maxConnections the maximum connections (must be positive)
         * @return this builder
         */
        public Builder withMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Set the rate limit per minute.
         * 
         * @param rateLimitPerMinute the rate limit per minute (must be positive)
         * @return this builder
         */
        public Builder withRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
            return this;
        }

        /**
         * Set whether request validation is enabled.
         * 
         * @param enableRequestValidation whether request validation is enabled
         * @return this builder
         */
        public Builder withEnableRequestValidation(boolean enableRequestValidation) {
            this.enableRequestValidation = enableRequestValidation;
            return this;
        }

        /**
         * Set the OAuth issuer URL.
         * 
         * @param oauthIssuerUrl the OAuth issuer URL (cannot be null or blank)
         * @return this builder
         */
        public Builder withOauthIssuerUrl(String oauthIssuerUrl) {
            this.oauthIssuerUrl = Objects.requireNonNull(oauthIssuerUrl, "oauthIssuerUrl");
            return this;
        }

        /**
         * Set the OAuth client ID.
         * 
         * @param oauthClientId the OAuth client ID (cannot be null or blank)
         * @return this builder
         */
        public Builder withOauthClientId(String oauthClientId) {
            this.oauthClientId = Objects.requireNonNull(oauthClientId, "oauthClientId");
            return this;
        }

        /**
         * Set the OAuth client secret.
         * 
         * @param oauthClientId the OAuth client secret (cannot be null or blank)
         * @return this builder
         */
        public Builder withOauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = Objects.requireNonNull(oauthClientSecret, "oauthClientSecret");
            return this;
        }

        /**
         * Set the OAuth redirect URI.
         * 
         * @param oauthRedirectUri the OAuth redirect URI (cannot be null or blank)
         * @return this builder
         */
        public Builder withOauthRedirectUri(String oauthRedirectUri) {
            this.oauthRedirectUri = Objects.requireNonNull(oauthRedirectUri, "oauthRedirectUri");
            return this;
        }

        /**
         * Set whether OAuth PKCE is enabled.
         * 
         * @param oauthPkceEnabled whether OAuth PKCE is enabled
         * @return this builder
         */
        public Builder withOauthPkceEnabled(boolean oauthPkceEnabled) {
            this.oauthPkceEnabled = oauthPkceEnabled;
            return this;
        }

        /**
         * Set the openHAB users file.
         * 
         * @param openhabUsersFile the openHAB users file (cannot be null or blank)
         * @return this builder
         */
        public Builder withOpenhabUsersFile(String openhabUsersFile) {
            this.openhabUsersFile = Objects.requireNonNull(openhabUsersFile, "openhabUsersFile");
            return this;
        }

        /**
         * Set whether openHAB users authentication is enabled.
         * 
         * @param openhabUsersEnabled whether openHAB users authentication is enabled
         * @return this builder
         */
        public Builder withOpenhabUsersEnabled(boolean openhabUsersEnabled) {
            this.openhabUsersEnabled = openhabUsersEnabled;
            return this;
        }

        /**
         * Set the API key header.
         * 
         * @param apiKeyHeader the API key header (cannot be null or blank)
         * @return this builder
         */
        public Builder withApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = Objects.requireNonNull(apiKeyHeader, "apiKeyHeader");
            return this;
        }

        /**
         * Set the API key value.
         * 
         * @param apiKeyValue the API key value (cannot be null or blank)
         * @return this builder
         */
        public Builder withApiKeyValue(String apiKeyValue) {
            this.apiKeyValue = Objects.requireNonNull(apiKeyValue, "apiKeyValue");
            return this;
        }

        /**
         * Set whether API key authentication is enabled.
         * 
         * @param apiKeyEnabled whether API key authentication is enabled
         * @return this builder
         */
        public Builder withApiKeyEnabled(boolean apiKeyEnabled) {
            this.apiKeyEnabled = apiKeyEnabled;
            return this;
        }

        /**
         * Set the JWT secret.
         * 
         * @param jwtSecret the JWT secret (cannot be null or blank)
         * @return this builder
         */
        public Builder withJwtSecret(String jwtSecret) {
            this.jwtSecret = Objects.requireNonNull(jwtSecret, "jwtSecret");
            return this;
        }

        /**
         * Set the JWT issuer.
         * 
         * @param jwtIssuer the JWT issuer (cannot be null or blank)
         * @return this builder
         */
        public Builder withJwtIssuer(String jwtIssuer) {
            this.jwtIssuer = Objects.requireNonNull(jwtIssuer, "jwtIssuer");
            return this;
        }

        /**
         * Set the JWT expiration minutes.
         * 
         * @param jwtExpirationMinutes the JWT expiration minutes (must be positive)
         * @return this builder
         */
        public Builder withJwtExpirationMinutes(int jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
            return this;
        }

        /**
         * Set whether JWT authentication is enabled.
         * 
         * @param jwtEnabled whether JWT authentication is enabled
         * @return this builder
         */
        public Builder withJwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
            return this;
        }

        /**
         * Set whether metrics are enabled.
         * 
         * @param enableMetrics whether metrics are enabled
         * @return this builder
         */
        public Builder withEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }

        /**
         * Set whether health checks are enabled.
         * 
         * @param enableHealthChecks whether health checks are enabled
         * @return this builder
         */
        public Builder withEnableHealthChecks(boolean enableHealthChecks) {
            this.enableHealthChecks = enableHealthChecks;
            return this;
        }

        /**
         * Set the health check interval.
         * 
         * @param healthCheckInterval the health check interval (must be positive)
         * @return this builder
         */
        public Builder withHealthCheckInterval(int healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
            return this;
        }

        /**
         * Set whether performance monitoring is enabled.
         * 
         * @param enablePerformanceMonitoring whether performance monitoring is enabled
         * @return this builder
         */
        public Builder withEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
            this.enablePerformanceMonitoring = enablePerformanceMonitoring;
            return this;
        }

        /**
         * Set whether production mode is enabled.
         * 
         * @param productionMode whether production mode is enabled
         * @return this builder
         */
        public Builder withProductionMode(boolean productionMode) {
            this.productionMode = productionMode;
            return this;
        }

        /**
         * Set the request timeout.
         * 
         * @param requestTimeout the request timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Set the connection timeout.
         * 
         * @param connectionTimeout the connection timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Set whether graceful shutdown is enabled.
         * 
         * @param enableGracefulShutdown whether graceful shutdown is enabled
         * @return this builder
         */
        public Builder withEnableGracefulShutdown(boolean enableGracefulShutdown) {
            this.enableGracefulShutdown = enableGracefulShutdown;
            return this;
        }

        /**
         * Set the shutdown timeout.
         * 
         * @param shutdownTimeout the shutdown timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withShutdownTimeout(int shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        /**
         * Set whether CORS is enabled.
         * 
         * @param enableCors whether CORS is enabled
         * @return this builder
         */
        public Builder withEnableCors(boolean enableCors) {
            this.enableCors = enableCors;
            return this;
        }

        /**
         * Set the CORS allowed origins.
         * 
         * @param corsAllowedOrigins the CORS allowed origins (cannot be null or blank)
         * @return this builder
         */
        public Builder withCorsAllowedOrigins(String corsAllowedOrigins) {
            this.corsAllowedOrigins = Objects.requireNonNull(corsAllowedOrigins, "corsAllowedOrigins");
            return this;
        }

        /**
         * Set the CORS allowed methods.
         * 
         * @param corsAllowedMethods the CORS allowed methods (cannot be null or blank)
         * @return this builder
         */
        public Builder withCorsAllowedMethods(String corsAllowedMethods) {
            this.corsAllowedMethods = Objects.requireNonNull(corsAllowedMethods, "corsAllowedMethods");
            return this;
        }

        /**
         * Set the CORS allowed headers.
         * 
         * @param corsAllowedHeaders the CORS allowed headers (cannot be null or blank)
         * @return this builder
         */
        public Builder withCorsAllowedHeaders(String corsAllowedHeaders) {
            this.corsAllowedHeaders = Objects.requireNonNull(corsAllowedHeaders, "corsAllowedHeaders");
            return this;
        }

        /**
         * Set whether SSL is enabled.
         * 
         * @param enableSsl whether SSL is enabled
         * @return this builder
         */
        public Builder withEnableSsl(boolean enableSsl) {
            this.enableSsl = enableSsl;
            return this;
        }

        /**
         * Set the SSL key store.
         * 
         * @param sslKeyStore the SSL key store (cannot be null or blank)
         * @return this builder
         */
        public Builder withSslKeyStore(String sslKeyStore) {
            this.sslKeyStore = Objects.requireNonNull(sslKeyStore, "sslKeyStore");
            return this;
        }

        /**
         * Set the SSL key store password.
         * 
         * @param sslKeyStorePassword the SSL key store password (cannot be null or blank)
         * @return this builder
         */
        public Builder withSslKeyStorePassword(String sslKeyStorePassword) {
            this.sslKeyStorePassword = Objects.requireNonNull(sslKeyStorePassword, "sslKeyStorePassword");
            return this;
        }

        /**
         * Set the SSL trust store.
         * 
         * @param sslTrustStore the SSL trust store (cannot be null or blank)
         * @return this builder
         */
        public Builder withSslTrustStore(String sslTrustStore) {
            this.sslTrustStore = Objects.requireNonNull(sslTrustStore, "sslTrustStore");
            return this;
        }

        /**
         * Set the SSL trust store password.
         * 
         * @param sslTrustStorePassword the SSL trust store password (cannot be null or blank)
         * @return this builder
         */
        public Builder withSslTrustStorePassword(String sslTrustStorePassword) {
            this.sslTrustStorePassword = Objects.requireNonNull(sslTrustStorePassword, "sslTrustStorePassword");
            return this;
        }

        /**
         * Build the ServerConfiguration instance.
         * 
         * @return the configured ServerConfiguration
         * @throws IllegalArgumentException if validation fails
         */
        public ServerConfiguration build() {
            if (host.isBlank()) {
                throw new IllegalArgumentException("host must not be blank");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (version.isBlank()) {
                throw new IllegalArgumentException("version must not be blank");
            }
            if (contextPath.isBlank()) {
                throw new IllegalArgumentException("contextPath must not be blank");
            }
            if (servletPath.isBlank()) {
                throw new IllegalArgumentException("servletPath must not be blank");
            }
            if (servletPattern.isBlank()) {
                throw new IllegalArgumentException("servletPattern must not be blank");
            }
            if (messageEndpoint.isBlank()) {
                throw new IllegalArgumentException("messageEndpoint must not be blank");
            }
            if (healthEndpoint.isBlank()) {
                throw new IllegalArgumentException("healthEndpoint must not be blank");
            }
            if (statusEndpoint.isBlank()) {
                throw new IllegalArgumentException("statusEndpoint must not be blank");
            }
            if (metricsEndpoint.isBlank()) {
                throw new IllegalArgumentException("metricsEndpoint must not be blank");
            }
            if (primaryAuthMethod.isBlank()) {
                throw new IllegalArgumentException("primaryAuthMethod must not be blank");
            }
            if (fallbackAuthMethod.isBlank()) {
                throw new IllegalArgumentException("fallbackAuthMethod must not be blank");
            }
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("maxConnections must be positive");
            }
            if (rateLimitPerMinute <= 0) {
                throw new IllegalArgumentException("rateLimitPerMinute must be positive");
            }
            if (jwtExpirationMinutes <= 0) {
                throw new IllegalArgumentException("jwtExpirationMinutes must be positive");
            }
            if (healthCheckInterval <= 0) {
                throw new IllegalArgumentException("healthCheckInterval must be positive");
            }
            if (requestTimeout <= 0) {
                throw new IllegalArgumentException("requestTimeout must be positive");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("connectionTimeout must be positive");
            }
            if (shutdownTimeout <= 0) {
                throw new IllegalArgumentException("shutdownTimeout must be positive");
            }
            return new ServerConfiguration(this);
        }
    }
}
