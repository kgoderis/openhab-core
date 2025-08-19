package org.openhab.core.ai.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.BaseConfiguration;

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
    // Constructor using inner Builder removed; use HttpServerConfigurationBuilder instead

    public HttpServerConfiguration(HttpServerConfigurationBuilder builder) {
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
        this.serverOptions = new HashMap<>(builder.serverOptions);
    }

    /**
     * Create a new builder for HTTP server configuration.
     * 
     * @return the builder
     */
    public static HttpServerConfigurationBuilder builder() {
        return new HttpServerConfigurationBuilder();
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
        return port == that.port && Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(contextPath, that.contextPath) && Objects.equals(mcpServletPath, that.mcpServletPath)
                && Objects.equals(a2aServletPath, that.a2aServletPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseUrl, port, contextPath, mcpServletPath, a2aServletPath);
    }

    @Override
    public String toString() {
        return String.format(
                "HttpServerConfiguration{baseUrl='%s', port=%d, contextPath='%s', mcpServletPath='%s', a2aServletPath='%s'}",
                baseUrl, port, contextPath, mcpServletPath, a2aServletPath);
    }

    // Builder extracted to top-level: see HttpServerConfigurationBuilder
}
