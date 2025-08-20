package org.openhab.core.ai.common.configuration;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.ServerConfigurationBuilder;

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
     * Public constructor for builder pattern.
     */
    public ServerConfiguration(ServerConfigurationBuilder builder) {
        super("server-" + builder.getHost() + "-" + builder.getPort(), builder.isEnabled(), builder.getName(),
                builder.getVersion(), builder.getSettings());
        // Use builder fields with defaults for missing values
        this.baseUrl = "http://" + builder.getHost() + ":" + builder.getPort();
        this.port = builder.getPort();
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
    public static ServerConfigurationBuilder builder() {
        return new ServerConfigurationBuilder();
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
}
