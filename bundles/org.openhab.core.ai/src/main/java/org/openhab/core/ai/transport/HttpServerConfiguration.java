package org.openhab.core.ai.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.BaseConfiguration;

/**
 * Unified HTTP Server Configuration for MCP and A2A protocols.
 * 
 * This configuration class provides unified settings for HTTP server integration
 * with openHAB's HTTP server using the OSGi HTTP Whiteboard pattern. It consolidates
 * configuration for both MCP and A2A protocols into a single, manageable interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HttpServerConfiguration extends BaseConfiguration {

    private final String baseUrl;
    private final int port;
    private final String contextPath;

    // Servlet configuration
    private final String mcpServletPath;
    private final String mcpServletPattern;
    private final String a2aServletPath;
    private final String a2aServletPattern;

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
    private final String metricsEndpoint;

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

    private final Map<String, Object> serverOptions;

    /**
     * Private constructor for builder pattern.
     */
    private HttpServerConfiguration(Builder builder) {
        super("http-server-" + builder.port, true, "HTTP Server Configuration", "1.0.0", builder.serverOptions);
        this.baseUrl = builder.baseUrl;
        this.port = builder.port;
        this.contextPath = builder.contextPath;
        this.mcpServletPath = builder.mcpServletPath;
        this.mcpServletPattern = builder.mcpServletPattern;
        this.a2aServletPath = builder.a2aServletPath;
        this.a2aServletPattern = builder.a2aServletPattern;
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
        this.metricsEndpoint = builder.metricsEndpoint;
        this.productionMode = builder.productionMode;
        this.requestTimeout = builder.requestTimeout;
        this.connectionTimeout = builder.connectionTimeout;
        this.enableGracefulShutdown = builder.enableGracefulShutdown;
        this.shutdownTimeout = builder.shutdownTimeout;
        this.enableCors = builder.enableCors;
        this.corsAllowedOrigins = builder.corsAllowedOrigins;
        this.corsAllowedMethods = builder.corsAllowedMethods;
        this.corsAllowedHeaders = builder.corsAllowedHeaders;
        this.enableSsl = builder.enableSsl;
        this.sslKeyStore = builder.sslKeyStore;
        this.sslKeyStorePassword = builder.sslKeyStorePassword;
        this.sslTrustStore = builder.sslTrustStore;
        this.sslTrustStorePassword = builder.sslTrustStorePassword;
        this.serverOptions = Map.copyOf(builder.serverOptions);
    }

    /**
     * Create a new builder for HTTP server configuration.
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

    public String getMcpServletPath() {
        return mcpServletPath;
    }

    public String getMcpServletPattern() {
        return mcpServletPattern;
    }

    public String getA2aServletPath() {
        return a2aServletPath;
    }

    public String getA2aServletPattern() {
        return a2aServletPattern;
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

    public String getMetricsEndpoint() {
        return metricsEndpoint;
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
        if (!super.equals(o)) {
            return false;
        }
        HttpServerConfiguration that = (HttpServerConfiguration) o;
        return port == that.port && enableAuthentication == that.enableAuthentication
                && enableFallbackAuth == that.enableFallbackAuth && maxConnections == that.maxConnections
                && rateLimitPerMinute == that.rateLimitPerMinute
                && enableRequestValidation == that.enableRequestValidation && oauthPkceEnabled == that.oauthPkceEnabled
                && openhabUsersEnabled == that.openhabUsersEnabled && apiKeyEnabled == that.apiKeyEnabled
                && jwtExpirationMinutes == that.jwtExpirationMinutes && jwtEnabled == that.jwtEnabled
                && enableMetrics == that.enableMetrics && enableHealthChecks == that.enableHealthChecks
                && healthCheckInterval == that.healthCheckInterval
                && enablePerformanceMonitoring == that.enablePerformanceMonitoring
                && productionMode == that.productionMode && requestTimeout == that.requestTimeout
                && connectionTimeout == that.connectionTimeout && enableGracefulShutdown == that.enableGracefulShutdown
                && shutdownTimeout == that.shutdownTimeout && enableCors == that.enableCors
                && enableSsl == that.enableSsl && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(contextPath, that.contextPath) && Objects.equals(mcpServletPath, that.mcpServletPath)
                && Objects.equals(mcpServletPattern, that.mcpServletPattern)
                && Objects.equals(a2aServletPath, that.a2aServletPath)
                && Objects.equals(a2aServletPattern, that.a2aServletPattern)
                && Objects.equals(primaryAuthMethod, that.primaryAuthMethod)
                && Objects.equals(fallbackAuthMethod, that.fallbackAuthMethod)
                && Objects.equals(oauthIssuerUrl, that.oauthIssuerUrl)
                && Objects.equals(oauthClientId, that.oauthClientId)
                && Objects.equals(oauthClientSecret, that.oauthClientSecret)
                && Objects.equals(oauthRedirectUri, that.oauthRedirectUri)
                && Objects.equals(openhabUsersFile, that.openhabUsersFile)
                && Objects.equals(apiKeyHeader, that.apiKeyHeader) && Objects.equals(apiKeyValue, that.apiKeyValue)
                && Objects.equals(jwtSecret, that.jwtSecret) && Objects.equals(jwtIssuer, that.jwtIssuer)
                && Objects.equals(metricsEndpoint, that.metricsEndpoint)
                && Objects.equals(corsAllowedOrigins, that.corsAllowedOrigins)
                && Objects.equals(corsAllowedMethods, that.corsAllowedMethods)
                && Objects.equals(corsAllowedHeaders, that.corsAllowedHeaders)
                && Objects.equals(sslKeyStore, that.sslKeyStore)
                && Objects.equals(sslKeyStorePassword, that.sslKeyStorePassword)
                && Objects.equals(sslTrustStore, that.sslTrustStore)
                && Objects.equals(sslTrustStorePassword, that.sslTrustStorePassword)
                && Objects.equals(serverOptions, that.serverOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseUrl, port, contextPath, mcpServletPath, mcpServletPattern,
                a2aServletPath, a2aServletPattern, enableAuthentication, primaryAuthMethod, fallbackAuthMethod,
                enableFallbackAuth, maxConnections, rateLimitPerMinute, enableRequestValidation, oauthIssuerUrl,
                oauthClientId, oauthClientSecret, oauthRedirectUri, oauthPkceEnabled, openhabUsersFile,
                openhabUsersEnabled, apiKeyHeader, apiKeyValue, apiKeyEnabled, jwtSecret, jwtIssuer,
                jwtExpirationMinutes, jwtEnabled, enableMetrics, enableHealthChecks, healthCheckInterval,
                enablePerformanceMonitoring, metricsEndpoint, productionMode, requestTimeout, connectionTimeout,
                enableGracefulShutdown, shutdownTimeout, enableCors, corsAllowedOrigins, corsAllowedMethods,
                corsAllowedHeaders, enableSsl, sslKeyStore, sslKeyStorePassword, sslTrustStore, sslTrustStorePassword,
                serverOptions);
    }

    @Override
    public String toString() {
        return String.format(
                "HttpServerConfiguration{baseUrl='%s', port=%d, contextPath='%s', mcpServletPath='%s', a2aServletPath='%s'}",
                baseUrl, port, contextPath, mcpServletPath, a2aServletPath);
    }

    /**
     * Builder for HttpServerConfiguration.
     * 
     * This builder provides a fluent API for creating HttpServerConfiguration instances.
     * The builder is not thread-safe and should be used for single-threaded construction.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String baseUrl = "http://localhost:8080";
        private int port = 8080;
        private String contextPath = "/";
        private String mcpServletPath = "/mcp";
        private String mcpServletPattern = "/mcp/*";
        private String a2aServletPath = "/a2a";
        private String a2aServletPattern = "/a2a/*";

        private boolean enableAuthentication = false;
        private String primaryAuthMethod = "oauth2.1";
        private String fallbackAuthMethod = "openhab_users";
        private boolean enableFallbackAuth = true;
        private int maxConnections = 100;
        private int rateLimitPerMinute = 1000;
        private boolean enableRequestValidation = true;

        private String oauthIssuerUrl = "";
        private String oauthClientId = "";
        private String oauthClientSecret = "";
        private String oauthRedirectUri = "";
        private boolean oauthPkceEnabled = true;

        private String openhabUsersFile = "";
        private boolean openhabUsersEnabled = true;

        private String apiKeyHeader = "X-API-Key";
        private String apiKeyValue = "";
        private boolean apiKeyEnabled = false;

        private String jwtSecret = "";
        private String jwtIssuer = "openhab-http-server";
        private int jwtExpirationMinutes = 60;
        private boolean jwtEnabled = false;

        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private int healthCheckInterval = 30000;
        private boolean enablePerformanceMonitoring = true;
        private String metricsEndpoint = "/metrics";

        private boolean productionMode = false;
        private int requestTimeout = 30000;
        private int connectionTimeout = 10000;
        private boolean enableGracefulShutdown = true;
        private int shutdownTimeout = 30000;

        private boolean enableCors = true;
        private String corsAllowedOrigins = "*";
        private String corsAllowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
        private String corsAllowedHeaders = "Content-Type, Authorization, X-API-Key";

        private boolean enableSsl = false;
        private String sslKeyStore = "";
        private String sslKeyStorePassword = "";
        private String sslTrustStore = "";
        private String sslTrustStorePassword = "";

        private Map<String, Object> serverOptions = new HashMap<>();

        /**
         * Create a new builder with default values.
         */
        public Builder() {
        }

        /**
         * Create a new builder from an existing HttpServerConfiguration.
         * 
         * @param source the source configuration to copy from
         */
        public Builder(HttpServerConfiguration source) {
            this.baseUrl = source.baseUrl;
            this.port = source.port;
            this.contextPath = source.contextPath;
            this.mcpServletPath = source.mcpServletPath;
            this.mcpServletPattern = source.mcpServletPattern;
            this.a2aServletPath = source.a2aServletPath;
            this.a2aServletPattern = source.a2aServletPattern;
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
            this.metricsEndpoint = source.metricsEndpoint;
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
            this.serverOptions = new HashMap<>(source.serverOptions);
        }

        /**
         * Set the base URL.
         * 
         * @param baseUrl the base URL (cannot be null or blank)
         * @return this builder
         */
        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        /**
         * Set the port.
         * 
         * @param port the port number (must be between 1 and 65535)
         * @return this builder
         */
        public Builder withPort(int port) {
            this.port = port;
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
         * Set the MCP servlet path.
         * 
         * @param mcpServletPath the MCP servlet path (cannot be null or blank)
         * @return this builder
         */
        public Builder withMcpServletPath(String mcpServletPath) {
            this.mcpServletPath = Objects.requireNonNull(mcpServletPath, "mcpServletPath");
            return this;
        }

        /**
         * Set the MCP servlet pattern.
         * 
         * @param mcpServletPattern the MCP servlet pattern (cannot be null or blank)
         * @return this builder
         */
        public Builder withMcpServletPattern(String mcpServletPattern) {
            this.mcpServletPattern = Objects.requireNonNull(mcpServletPattern, "mcpServletPattern");
            return this;
        }

        /**
         * Set the A2A servlet path.
         * 
         * @param a2aServletPath the A2A servlet path (cannot be null or blank)
         * @return this builder
         */
        public Builder withA2aServletPath(String a2aServletPath) {
            this.a2aServletPath = Objects.requireNonNull(a2aServletPath, "a2aServletPath");
            return this;
        }

        /**
         * Set the A2A servlet pattern.
         * 
         * @param a2aServletPattern the A2A servlet pattern (cannot be null or blank)
         * @return this builder
         */
        public Builder withA2aServletPattern(String a2aServletPattern) {
            this.a2aServletPattern = Objects.requireNonNull(a2aServletPattern, "a2aServletPattern");
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
         * Set the maximum number of connections.
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
         * @param oauthIssuerUrl the OAuth issuer URL (can be null)
         * @return this builder
         */
        public Builder withOauthIssuerUrl(@Nullable String oauthIssuerUrl) {
            this.oauthIssuerUrl = oauthIssuerUrl != null ? oauthIssuerUrl : "";
            return this;
        }

        /**
         * Set the OAuth client ID.
         * 
         * @param oauthClientId the OAuth client ID (can be null)
         * @return this builder
         */
        public Builder withOauthClientId(@Nullable String oauthClientId) {
            this.oauthClientId = oauthClientId != null ? oauthClientId : "";
            return this;
        }

        /**
         * Set the OAuth client secret.
         * 
         * @param oauthClientSecret the OAuth client secret (can be null)
         * @return this builder
         */
        public Builder withOauthClientSecret(@Nullable String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret != null ? oauthClientSecret : "";
            return this;
        }

        /**
         * Set the OAuth redirect URI.
         * 
         * @param oauthRedirectUri the OAuth redirect URI (can be null)
         * @return this builder
         */
        public Builder withOauthRedirectUri(@Nullable String oauthRedirectUri) {
            this.oauthRedirectUri = oauthRedirectUri != null ? oauthRedirectUri : "";
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
         * @param openhabUsersFile the openHAB users file (can be null)
         * @return this builder
         */
        public Builder withOpenhabUsersFile(@Nullable String openhabUsersFile) {
            this.openhabUsersFile = openhabUsersFile != null ? openhabUsersFile : "";
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
         * @param apiKeyValue the API key value (can be null)
         * @return this builder
         */
        public Builder withApiKeyValue(@Nullable String apiKeyValue) {
            this.apiKeyValue = apiKeyValue != null ? apiKeyValue : "";
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
         * @param jwtSecret the JWT secret (can be null)
         * @return this builder
         */
        public Builder withJwtSecret(@Nullable String jwtSecret) {
            this.jwtSecret = jwtSecret != null ? jwtSecret : "";
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
         * @param healthCheckInterval the health check interval in milliseconds (must be positive)
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
         * @param sslKeyStore the SSL key store (can be null)
         * @return this builder
         */
        public Builder withSslKeyStore(@Nullable String sslKeyStore) {
            this.sslKeyStore = sslKeyStore != null ? sslKeyStore : "";
            return this;
        }

        /**
         * Set the SSL key store password.
         * 
         * @param sslKeyStorePassword the SSL key store password (can be null)
         * @return this builder
         */
        public Builder withSslKeyStorePassword(@Nullable String sslKeyStorePassword) {
            this.sslKeyStorePassword = sslKeyStorePassword != null ? sslKeyStorePassword : "";
            return this;
        }

        /**
         * Set the SSL trust store.
         * 
         * @param sslTrustStore the SSL trust store (can be null)
         * @return this builder
         */
        public Builder withSslTrustStore(@Nullable String sslTrustStore) {
            this.sslTrustStore = sslTrustStore != null ? sslTrustStore : "";
            return this;
        }

        /**
         * Set the SSL trust store password.
         * 
         * @param sslTrustStorePassword the SSL trust store password (can be null)
         * @return this builder
         */
        public Builder withSslTrustStorePassword(@Nullable String sslTrustStorePassword) {
            this.sslTrustStorePassword = sslTrustStorePassword != null ? sslTrustStorePassword : "";
            return this;
        }

        /**
         * Add a server option.
         * 
         * @param key the option key (cannot be null)
         * @param value the option value (cannot be null)
         * @return this builder
         */
        public Builder withServerOption(String key, Object value) {
            this.serverOptions.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set server options.
         * 
         * @param serverOptions the server options map (cannot be null)
         * @return this builder
         */
        public Builder withServerOptions(Map<String, Object> serverOptions) {
            this.serverOptions.clear();
            this.serverOptions.putAll(Objects.requireNonNull(serverOptions, "serverOptions"));
            return this;
        }

        /**
         * Build the HttpServerConfiguration instance.
         * 
         * @return the configured HttpServerConfiguration
         * @throws IllegalArgumentException if validation fails
         */
        public HttpServerConfiguration build() {
            if (baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl must not be blank");
            }
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
            if (contextPath.isBlank()) {
                throw new IllegalArgumentException("contextPath must not be blank");
            }
            if (mcpServletPath.isBlank()) {
                throw new IllegalArgumentException("mcpServletPath must not be blank");
            }
            if (mcpServletPattern.isBlank()) {
                throw new IllegalArgumentException("mcpServletPattern must not be blank");
            }
            if (a2aServletPath.isBlank()) {
                throw new IllegalArgumentException("a2aServletPath must not be blank");
            }
            if (a2aServletPattern.isBlank()) {
                throw new IllegalArgumentException("a2aServletPattern must not be blank");
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
            if (jwtExpirationMinutes <= 0) {
                throw new IllegalArgumentException("jwtExpirationMinutes must be positive");
            }
            if (apiKeyHeader.isBlank()) {
                throw new IllegalArgumentException("apiKeyHeader must not be blank");
            }
            if (jwtIssuer.isBlank()) {
                throw new IllegalArgumentException("jwtIssuer must not be blank");
            }
            if (metricsEndpoint.isBlank()) {
                throw new IllegalArgumentException("metricsEndpoint must not be blank");
            }
            if (corsAllowedOrigins.isBlank()) {
                throw new IllegalArgumentException("corsAllowedOrigins must not be blank");
            }
            if (corsAllowedMethods.isBlank()) {
                throw new IllegalArgumentException("corsAllowedMethods must not be blank");
            }
            if (corsAllowedHeaders.isBlank()) {
                throw new IllegalArgumentException("corsAllowedHeaders must not be blank");
            }
            return new HttpServerConfiguration(this);
        }
    }
}
