package org.openhab.core.ai.common.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for ServerConfiguration instances.
 * 
 * This builder provides a fluent interface for creating server configuration
 * objects with all the necessary settings for HTTP server, security, and monitoring.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServerConfigurationBuilder extends AbstractBuilder<ServerConfiguration> {

    // Base configuration
    String id = "default-server";
    boolean enabled = true;
    String name = "AI Server";
    String version = "1.0.0";
    final Map<String, Object> customOptions = new HashMap<>();

    // Server settings
    String baseUrl = "http://localhost:8080";
    int port = 8080;
    String contextPath = "/ai";
    String servletPath = "/api";
    String servletPattern = "/*";

    // Protocol-specific endpoints
    String messageEndpoint = "/message";
    String healthEndpoint = "/health";
    String statusEndpoint = "/status";
    String metricsEndpoint = "/metrics";

    // Security configuration
    boolean enableAuthentication = false;
    String primaryAuthMethod = "none";
    String fallbackAuthMethod = "none";
    boolean enableFallbackAuth = false;
    int maxConnections = 100;
    int rateLimitPerMinute = 1000;
    boolean enableRequestValidation = true;

    // OAuth 2.1 configuration
    String oauthIssuerUrl = "";
    String oauthClientId = "";
    String oauthClientSecret = "";
    String oauthRedirectUri = "";
    boolean oauthPkceEnabled = false;

    // openHAB users authentication
    String openhabUsersFile = "";
    boolean openhabUsersEnabled = false;

    // API key authentication
    String apiKeyHeader = "X-API-Key";
    String apiKeyValue = "";
    boolean apiKeyEnabled = false;

    // JWT authentication
    String jwtSecret = "";
    String jwtIssuer = "";
    int jwtExpirationMinutes = 60;
    boolean jwtEnabled = false;

    // Monitoring and metrics
    boolean enableMetrics = true;
    boolean enableHealthChecks = true;
    int healthCheckInterval = 30;
    boolean enablePerformanceMonitoring = false;

    // Production settings
    boolean productionMode = false;
    int requestTimeout = 30000;
    int connectionTimeout = 10000;
    boolean enableGracefulShutdown = true;
    int shutdownTimeout = 30000;

    // CORS configuration
    boolean enableCors = false;
    String corsAllowedOrigins = "*";
    String corsAllowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
    String corsAllowedHeaders = "*";

    // SSL/TLS configuration
    boolean enableSsl = false;
    String sslKeyStore = "";
    String sslKeyStorePassword = "";
    String sslTrustStore = "";
    String sslTrustStorePassword = "";

    /**
     * Set the server identifier.
     * 
     * @param id Server identifier
     * @return this builder
     */
    public ServerConfigurationBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "Server ID cannot be null");
        return this;
    }

    /**
     * Set whether the server is enabled.
     * 
     * @param enabled Whether the server is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Set the server name.
     * 
     * @param name Server name
     * @return this builder
     */
    public ServerConfigurationBuilder withName(String name) {
        this.name = Objects.requireNonNull(name, "Server name cannot be null");
        return this;
    }

    /**
     * Set the server version.
     * 
     * @param version Server version
     * @return this builder
     */
    public ServerConfigurationBuilder withVersion(String version) {
        this.version = Objects.requireNonNull(version, "Server version cannot be null");
        return this;
    }

    /**
     * Set the base URL.
     * 
     * @param baseUrl Base URL
     * @return this builder
     */
    public ServerConfigurationBuilder withBaseUrl(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        return this;
    }

    /**
     * Set the port.
     * 
     * @param port Port number
     * @return this builder
     */
    public ServerConfigurationBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the context path.
     * 
     * @param contextPath Context path
     * @return this builder
     */
    public ServerConfigurationBuilder withContextPath(String contextPath) {
        this.contextPath = Objects.requireNonNull(contextPath, "Context path cannot be null");
        return this;
    }

    /**
     * Set the servlet path.
     * 
     * @param servletPath Servlet path
     * @return this builder
     */
    public ServerConfigurationBuilder withServletPath(String servletPath) {
        this.servletPath = Objects.requireNonNull(servletPath, "Servlet path cannot be null");
        return this;
    }

    /**
     * Set the servlet pattern.
     * 
     * @param servletPattern Servlet pattern
     * @return this builder
     */
    public ServerConfigurationBuilder withServletPattern(String servletPattern) {
        this.servletPattern = Objects.requireNonNull(servletPattern, "Servlet pattern cannot be null");
        return this;
    }

    /**
     * Set the message endpoint.
     * 
     * @param messageEndpoint Message endpoint
     * @return this builder
     */
    public ServerConfigurationBuilder withMessageEndpoint(String messageEndpoint) {
        this.messageEndpoint = Objects.requireNonNull(messageEndpoint, "Message endpoint cannot be null");
        return this;
    }

    /**
     * Set the health endpoint.
     * 
     * @param healthEndpoint Health endpoint
     * @return this builder
     */
    public ServerConfigurationBuilder withHealthEndpoint(String healthEndpoint) {
        this.healthEndpoint = Objects.requireNonNull(healthEndpoint, "Health endpoint cannot be null");
        return this;
    }

    /**
     * Set the status endpoint.
     * 
     * @param statusEndpoint Status endpoint
     * @return this builder
     */
    public ServerConfigurationBuilder withStatusEndpoint(String statusEndpoint) {
        this.statusEndpoint = Objects.requireNonNull(statusEndpoint, "Status endpoint cannot be null");
        return this;
    }

    /**
     * Set the metrics endpoint.
     * 
     * @param metricsEndpoint Metrics endpoint
     * @return this builder
     */
    public ServerConfigurationBuilder withMetricsEndpoint(String metricsEndpoint) {
        this.metricsEndpoint = Objects.requireNonNull(metricsEndpoint, "Metrics endpoint cannot be null");
        return this;
    }

    /**
     * Set whether authentication is enabled.
     * 
     * @param enableAuthentication Whether authentication is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
        return this;
    }

    /**
     * Set the primary authentication method.
     * 
     * @param primaryAuthMethod Primary authentication method
     * @return this builder
     */
    public ServerConfigurationBuilder withPrimaryAuthMethod(String primaryAuthMethod) {
        this.primaryAuthMethod = Objects.requireNonNull(primaryAuthMethod, "Primary auth method cannot be null");
        return this;
    }

    /**
     * Set the fallback authentication method.
     * 
     * @param fallbackAuthMethod Fallback authentication method
     * @return this builder
     */
    public ServerConfigurationBuilder withFallbackAuthMethod(String fallbackAuthMethod) {
        this.fallbackAuthMethod = Objects.requireNonNull(fallbackAuthMethod, "Fallback auth method cannot be null");
        return this;
    }

    /**
     * Set whether fallback authentication is enabled.
     * 
     * @param enableFallbackAuth Whether fallback authentication is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableFallbackAuth(boolean enableFallbackAuth) {
        this.enableFallbackAuth = enableFallbackAuth;
        return this;
    }

    /**
     * Set the maximum number of connections.
     * 
     * @param maxConnections Maximum connections
     * @return this builder
     */
    public ServerConfigurationBuilder withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * Set the rate limit per minute.
     * 
     * @param rateLimitPerMinute Rate limit per minute
     * @return this builder
     */
    public ServerConfigurationBuilder withRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
        return this;
    }

    /**
     * Set whether request validation is enabled.
     * 
     * @param enableRequestValidation Whether request validation is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableRequestValidation(boolean enableRequestValidation) {
        this.enableRequestValidation = enableRequestValidation;
        return this;
    }

    /**
     * Set OAuth issuer URL.
     * 
     * @param oauthIssuerUrl OAuth issuer URL
     * @return this builder
     */
    public ServerConfigurationBuilder withOauthIssuerUrl(String oauthIssuerUrl) {
        this.oauthIssuerUrl = Objects.requireNonNull(oauthIssuerUrl, "OAuth issuer URL cannot be null");
        return this;
    }

    /**
     * Set OAuth client ID.
     * 
     * @param oauthClientId OAuth client ID
     * @return this builder
     */
    public ServerConfigurationBuilder withOauthClientId(String oauthClientId) {
        this.oauthClientId = Objects.requireNonNull(oauthClientId, "OAuth client ID cannot be null");
        return this;
    }

    /**
     * Set OAuth client secret.
     * 
     * @param oauthClientSecret OAuth client secret
     * @return this builder
     */
    public ServerConfigurationBuilder withOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = Objects.requireNonNull(oauthClientSecret, "OAuth client secret cannot be null");
        return this;
    }

    /**
     * Set OAuth redirect URI.
     * 
     * @param oauthRedirectUri OAuth redirect URI
     * @return this builder
     */
    public ServerConfigurationBuilder withOauthRedirectUri(String oauthRedirectUri) {
        this.oauthRedirectUri = Objects.requireNonNull(oauthRedirectUri, "OAuth redirect URI cannot be null");
        return this;
    }

    /**
     * Set whether OAuth PKCE is enabled.
     * 
     * @param oauthPkceEnabled Whether OAuth PKCE is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withOauthPkceEnabled(boolean oauthPkceEnabled) {
        this.oauthPkceEnabled = oauthPkceEnabled;
        return this;
    }

    /**
     * Set openHAB users file.
     * 
     * @param openhabUsersFile openHAB users file
     * @return this builder
     */
    public ServerConfigurationBuilder withOpenhabUsersFile(String openhabUsersFile) {
        this.openhabUsersFile = Objects.requireNonNull(openhabUsersFile, "openHAB users file cannot be null");
        return this;
    }

    /**
     * Set whether openHAB users authentication is enabled.
     * 
     * @param openhabUsersEnabled Whether openHAB users authentication is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withOpenhabUsersEnabled(boolean openhabUsersEnabled) {
        this.openhabUsersEnabled = openhabUsersEnabled;
        return this;
    }

    /**
     * Set API key header.
     * 
     * @param apiKeyHeader API key header
     * @return this builder
     */
    public ServerConfigurationBuilder withApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = Objects.requireNonNull(apiKeyHeader, "API key header cannot be null");
        return this;
    }

    /**
     * Set API key value.
     * 
     * @param apiKeyValue API key value
     * @return this builder
     */
    public ServerConfigurationBuilder withApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = Objects.requireNonNull(apiKeyValue, "API key value cannot be null");
        return this;
    }

    /**
     * Set whether API key authentication is enabled.
     * 
     * @param apiKeyEnabled Whether API key authentication is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withApiKeyEnabled(boolean apiKeyEnabled) {
        this.apiKeyEnabled = apiKeyEnabled;
        return this;
    }

    /**
     * Set JWT secret.
     * 
     * @param jwtSecret JWT secret
     * @return this builder
     */
    public ServerConfigurationBuilder withJwtSecret(String jwtSecret) {
        this.jwtSecret = Objects.requireNonNull(jwtSecret, "JWT secret cannot be null");
        return this;
    }

    /**
     * Set JWT issuer.
     * 
     * @param jwtIssuer JWT issuer
     * @return this builder
     */
    public ServerConfigurationBuilder withJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = Objects.requireNonNull(jwtIssuer, "JWT issuer cannot be null");
        return this;
    }

    /**
     * Set JWT expiration minutes.
     * 
     * @param jwtExpirationMinutes JWT expiration minutes
     * @return this builder
     */
    public ServerConfigurationBuilder withJwtExpirationMinutes(int jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        return this;
    }

    /**
     * Set whether JWT authentication is enabled.
     * 
     * @param jwtEnabled Whether JWT authentication is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withJwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
        return this;
    }

    /**
     * Set whether metrics are enabled.
     * 
     * @param enableMetrics Whether metrics are enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }

    /**
     * Set whether health checks are enabled.
     * 
     * @param enableHealthChecks Whether health checks are enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableHealthChecks(boolean enableHealthChecks) {
        this.enableHealthChecks = enableHealthChecks;
        return this;
    }

    /**
     * Set health check interval.
     * 
     * @param healthCheckInterval Health check interval in seconds
     * @return this builder
     */
    public ServerConfigurationBuilder withHealthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    /**
     * Set whether performance monitoring is enabled.
     * 
     * @param enablePerformanceMonitoring Whether performance monitoring is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        return this;
    }

    /**
     * Set whether production mode is enabled.
     * 
     * @param productionMode Whether production mode is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
        return this;
    }

    /**
     * Set request timeout.
     * 
     * @param requestTimeout Request timeout in milliseconds
     * @return this builder
     */
    public ServerConfigurationBuilder withRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Set connection timeout.
     * 
     * @param connectionTimeout Connection timeout in milliseconds
     * @return this builder
     */
    public ServerConfigurationBuilder withConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Set whether graceful shutdown is enabled.
     * 
     * @param enableGracefulShutdown Whether graceful shutdown is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableGracefulShutdown(boolean enableGracefulShutdown) {
        this.enableGracefulShutdown = enableGracefulShutdown;
        return this;
    }

    /**
     * Set shutdown timeout.
     * 
     * @param shutdownTimeout Shutdown timeout in milliseconds
     * @return this builder
     */
    public ServerConfigurationBuilder withShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    /**
     * Set whether CORS is enabled.
     * 
     * @param enableCors Whether CORS is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableCors(boolean enableCors) {
        this.enableCors = enableCors;
        return this;
    }

    /**
     * Set CORS allowed origins.
     * 
     * @param corsAllowedOrigins CORS allowed origins
     * @return this builder
     */
    public ServerConfigurationBuilder withCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = Objects.requireNonNull(corsAllowedOrigins, "CORS allowed origins cannot be null");
        return this;
    }

    /**
     * Set CORS allowed methods.
     * 
     * @param corsAllowedMethods CORS allowed methods
     * @return this builder
     */
    public ServerConfigurationBuilder withCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = Objects.requireNonNull(corsAllowedMethods, "CORS allowed methods cannot be null");
        return this;
    }

    /**
     * Set CORS allowed headers.
     * 
     * @param corsAllowedHeaders CORS allowed headers
     * @return this builder
     */
    public ServerConfigurationBuilder withCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = Objects.requireNonNull(corsAllowedHeaders, "CORS allowed headers cannot be null");
        return this;
    }

    /**
     * Set whether SSL is enabled.
     * 
     * @param enableSsl Whether SSL is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
        return this;
    }

    /**
     * Set SSL key store.
     * 
     * @param sslKeyStore SSL key store
     * @return this builder
     */
    public ServerConfigurationBuilder withSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = Objects.requireNonNull(sslKeyStore, "SSL key store cannot be null");
        return this;
    }

    /**
     * Set SSL key store password.
     * 
     * @param sslKeyStorePassword SSL key store password
     * @return this builder
     */
    public ServerConfigurationBuilder withSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = Objects.requireNonNull(sslKeyStorePassword, "SSL key store password cannot be null");
        return this;
    }

    /**
     * Set SSL trust store.
     * 
     * @param sslTrustStore SSL trust store
     * @return this builder
     */
    public ServerConfigurationBuilder withSslTrustStore(String sslTrustStore) {
        this.sslTrustStore = Objects.requireNonNull(sslTrustStore, "SSL trust store cannot be null");
        return this;
    }

    /**
     * Set SSL trust store password.
     * 
     * @param sslTrustStorePassword SSL trust store password
     * @return this builder
     */
    public ServerConfigurationBuilder withSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = Objects.requireNonNull(sslTrustStorePassword,
                "SSL trust store password cannot be null");
        return this;
    }

    /**
     * Add a custom option.
     * 
     * @param key Option key
     * @param value Option value
     * @return this builder
     */
    public ServerConfigurationBuilder withCustomOption(String key, Object value) {
        this.customOptions.put(Objects.requireNonNull(key, "Option key cannot be null"), value);
        return this;
    }

    @Override
    protected void validate() {
        // Validate required fields
        validateRequiredString(id, "id");
        validateRequiredString(name, "name");
        validateRequiredString(version, "version");
        validateRequiredString(baseUrl, "baseUrl");

        // Validate numeric ranges
        validateRange(port, "port", 1, 65535);
        validatePositive(maxConnections, "maxConnections");
        validateNonNegative(rateLimitPerMinute, "rateLimitPerMinute");
        validatePositive(jwtExpirationMinutes, "jwtExpirationMinutes");
        validatePositive(healthCheckInterval, "healthCheckInterval");
        validatePositive(requestTimeout, "requestTimeout");
        validatePositive(connectionTimeout, "connectionTimeout");
        validatePositive(shutdownTimeout, "shutdownTimeout");

        // Validate SSL configuration
        if (enableSsl) {
            validateRequiredString(sslKeyStore, "sslKeyStore");
            validateRequiredString(sslKeyStorePassword, "sslKeyStorePassword");
        }

        // Validate OAuth configuration
        if (enableAuthentication && "oauth".equals(primaryAuthMethod)) {
            validateRequiredString(oauthIssuerUrl, "oauthIssuerUrl");
            validateRequiredString(oauthClientId, "oauthClientId");
            validateRequiredString(oauthClientSecret, "oauthClientSecret");
        }
    }

    @Override
    protected void doReset() {
        // Reset all fields to their default values
        id = "default-server";
        enabled = true;
        name = "AI Server";
        version = "1.0.0";
        customOptions.clear();

        baseUrl = "http://localhost:8080";
        port = 8080;
        contextPath = "/ai";
        servletPath = "/api";
        servletPattern = "/*";

        messageEndpoint = "/message";
        healthEndpoint = "/health";
        statusEndpoint = "/status";
        metricsEndpoint = "/metrics";

        enableAuthentication = false;
        primaryAuthMethod = "none";
        fallbackAuthMethod = "none";
        enableFallbackAuth = false;
        maxConnections = 100;
        rateLimitPerMinute = 1000;
        enableRequestValidation = true;

        oauthIssuerUrl = "";
        oauthClientId = "";
        oauthClientSecret = "";
        oauthRedirectUri = "";
        oauthPkceEnabled = false;

        openhabUsersFile = "";
        openhabUsersEnabled = false;

        apiKeyHeader = "X-API-Key";
        apiKeyValue = "";
        apiKeyEnabled = false;

        jwtSecret = "";
        jwtIssuer = "";
        jwtExpirationMinutes = 60;
        jwtEnabled = false;

        enableMetrics = true;
        enableHealthChecks = true;
        healthCheckInterval = 30;
        enablePerformanceMonitoring = false;

        productionMode = false;
        requestTimeout = 30000;
        connectionTimeout = 10000;
        enableGracefulShutdown = true;
        shutdownTimeout = 30000;

        enableCors = false;
        corsAllowedOrigins = "*";
        corsAllowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        corsAllowedHeaders = "*";

        enableSsl = false;
        sslKeyStore = "";
        sslKeyStorePassword = "";
        sslTrustStore = "";
        sslTrustStorePassword = "";
    }

    /**
     * Build the ServerConfiguration instance.
     * 
     * @return the configured ServerConfiguration
     */
    @Override
    public ServerConfiguration build() {
        if (!isValid()) {
            throw new IllegalStateException("Invalid configuration: " + getValidationErrors());
        }
        return new ServerConfiguration(this);
    }
}
