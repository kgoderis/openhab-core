package org.openhab.core.ai.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
public class HttpServerConfiguration {

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
        this.serverOptions = new HashMap<>(builder.serverOptions);
    }

    /**
     * Create a new builder for HTTP server configuration.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
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
        HttpServerConfiguration that = (HttpServerConfiguration) o;
        return port == that.port && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(contextPath, that.contextPath) && Objects.equals(mcpServletPath, that.mcpServletPath)
                && Objects.equals(a2aServletPath, that.a2aServletPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, port, contextPath, mcpServletPath, a2aServletPath);
    }

    @Override
    public String toString() {
        return String.format(
                "HttpServerConfiguration{baseUrl='%s', port=%d, contextPath='%s', mcpServletPath='%s', a2aServletPath='%s'}",
                baseUrl, port, contextPath, mcpServletPath, a2aServletPath);
    }

    /**
     * Builder for HTTP server configuration.
     */
    public static class Builder {
        private String baseUrl = "http://localhost:8080";
        private int port = 8080;
        private String contextPath = "/";
        private String mcpServletPath = "/mcp";
        private String mcpServletPattern = "/mcp/*";
        private String a2aServletPath = "/a2a";
        private String a2aServletPattern = "/a2a/*";

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
        private String jwtIssuer = "openhab-http-server";
        private int jwtExpirationMinutes = 60;
        private boolean jwtEnabled = false;

        // Monitoring and metrics
        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private int healthCheckInterval = 30000; // 30 seconds
        private boolean enablePerformanceMonitoring = true;
        private String metricsEndpoint = "/metrics";

        // Production settings
        private boolean productionMode = false;
        private int requestTimeout = 30000; // 30 seconds
        private int connectionTimeout = 10000; // 10 seconds
        private boolean enableGracefulShutdown = true;
        private int shutdownTimeout = 30000; // 30 seconds

        // CORS configuration
        private boolean enableCors = true;
        private String corsAllowedOrigins = "*";
        private String corsAllowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
        private String corsAllowedHeaders = "Content-Type, Authorization, X-API-Key";

        // SSL/TLS configuration
        private boolean enableSsl = false;
        private String sslKeyStore = "";
        private String sslKeyStorePassword = "";
        private String sslTrustStore = "";
        private String sslTrustStorePassword = "";

        private Map<String, Object> serverOptions = new HashMap<>();

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder mcpServletPath(String mcpServletPath) {
            this.mcpServletPath = mcpServletPath;
            return this;
        }

        public Builder mcpServletPattern(String mcpServletPattern) {
            this.mcpServletPattern = mcpServletPattern;
            return this;
        }

        public Builder a2aServletPath(String a2aServletPath) {
            this.a2aServletPath = a2aServletPath;
            return this;
        }

        public Builder a2aServletPattern(String a2aServletPattern) {
            this.a2aServletPattern = a2aServletPattern;
            return this;
        }

        public Builder enableAuthentication(boolean enableAuthentication) {
            this.enableAuthentication = enableAuthentication;
            return this;
        }

        public Builder primaryAuthMethod(String primaryAuthMethod) {
            this.primaryAuthMethod = primaryAuthMethod;
            return this;
        }

        public Builder fallbackAuthMethod(String fallbackAuthMethod) {
            this.fallbackAuthMethod = fallbackAuthMethod;
            return this;
        }

        public Builder enableFallbackAuth(boolean enableFallbackAuth) {
            this.enableFallbackAuth = enableFallbackAuth;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder rateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
            return this;
        }

        public Builder enableRequestValidation(boolean enableRequestValidation) {
            this.enableRequestValidation = enableRequestValidation;
            return this;
        }

        public Builder oauthIssuerUrl(String oauthIssuerUrl) {
            this.oauthIssuerUrl = oauthIssuerUrl;
            return this;
        }

        public Builder oauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public Builder oauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
            return this;
        }

        public Builder oauthRedirectUri(String oauthRedirectUri) {
            this.oauthRedirectUri = oauthRedirectUri;
            return this;
        }

        public Builder oauthPkceEnabled(boolean oauthPkceEnabled) {
            this.oauthPkceEnabled = oauthPkceEnabled;
            return this;
        }

        public Builder openhabUsersFile(String openhabUsersFile) {
            this.openhabUsersFile = openhabUsersFile;
            return this;
        }

        public Builder openhabUsersEnabled(boolean openhabUsersEnabled) {
            this.openhabUsersEnabled = openhabUsersEnabled;
            return this;
        }

        public Builder apiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
            return this;
        }

        public Builder apiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
            return this;
        }

        public Builder apiKeyEnabled(boolean apiKeyEnabled) {
            this.apiKeyEnabled = apiKeyEnabled;
            return this;
        }

        public Builder jwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
            return this;
        }

        public Builder jwtIssuer(String jwtIssuer) {
            this.jwtIssuer = jwtIssuer;
            return this;
        }

        public Builder jwtExpirationMinutes(int jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
            return this;
        }

        public Builder jwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
            return this;
        }

        public Builder enableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }

        public Builder enableHealthChecks(boolean enableHealthChecks) {
            this.enableHealthChecks = enableHealthChecks;
            return this;
        }

        public Builder healthCheckInterval(int healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
            return this;
        }

        public Builder enablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
            this.enablePerformanceMonitoring = enablePerformanceMonitoring;
            return this;
        }

        public Builder metricsEndpoint(String metricsEndpoint) {
            this.metricsEndpoint = metricsEndpoint;
            return this;
        }

        public Builder productionMode(boolean productionMode) {
            this.productionMode = productionMode;
            return this;
        }

        public Builder requestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder enableGracefulShutdown(boolean enableGracefulShutdown) {
            this.enableGracefulShutdown = enableGracefulShutdown;
            return this;
        }

        public Builder shutdownTimeout(int shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        public Builder enableCors(boolean enableCors) {
            this.enableCors = enableCors;
            return this;
        }

        public Builder corsAllowedOrigins(String corsAllowedOrigins) {
            this.corsAllowedOrigins = corsAllowedOrigins;
            return this;
        }

        public Builder corsAllowedMethods(String corsAllowedMethods) {
            this.corsAllowedMethods = corsAllowedMethods;
            return this;
        }

        public Builder corsAllowedHeaders(String corsAllowedHeaders) {
            this.corsAllowedHeaders = corsAllowedHeaders;
            return this;
        }

        public Builder enableSsl(boolean enableSsl) {
            this.enableSsl = enableSsl;
            return this;
        }

        public Builder sslKeyStore(String sslKeyStore) {
            this.sslKeyStore = sslKeyStore;
            return this;
        }

        public Builder sslKeyStorePassword(String sslKeyStorePassword) {
            this.sslKeyStorePassword = sslKeyStorePassword;
            return this;
        }

        public Builder sslTrustStore(String sslTrustStore) {
            this.sslTrustStore = sslTrustStore;
            return this;
        }

        public Builder sslTrustStorePassword(String sslTrustStorePassword) {
            this.sslTrustStorePassword = sslTrustStorePassword;
            return this;
        }

        public Builder serverOption(String key, Object value) {
            this.serverOptions.put(key, value);
            return this;
        }

        public Builder serverOptions(Map<String, Object> serverOptions) {
            this.serverOptions.clear();
            this.serverOptions.putAll(serverOptions);
            return this;
        }

        public HttpServerConfiguration build() {
            return new HttpServerConfiguration(this);
        }
    }
}
