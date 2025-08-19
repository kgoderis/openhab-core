package org.openhab.core.ai.transport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link HttpServerConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HttpServerConfigurationBuilder extends AbstractBuilder<HttpServerConfiguration> {
    String baseUrl = "http://localhost:8080";
    int port = 8080;
    String contextPath = "/";
    String mcpServletPath = "/mcp";
    String mcpServletPattern = "/mcp/*";
    String a2aServletPath = "/a2a";
    String a2aServletPattern = "/a2a/*";

    boolean enableAuthentication = false;
    String primaryAuthMethod = "oauth2.1";
    String fallbackAuthMethod = "openhab_users";
    boolean enableFallbackAuth = true;
    int maxConnections = 100;
    int rateLimitPerMinute = 1000;
    boolean enableRequestValidation = true;

    String oauthIssuerUrl = "";
    String oauthClientId = "";
    String oauthClientSecret = "";
    String oauthRedirectUri = "";
    boolean oauthPkceEnabled = true;

    String openhabUsersFile = "";
    boolean openhabUsersEnabled = true;

    String apiKeyHeader = "X-API-Key";
    String apiKeyValue = "";
    boolean apiKeyEnabled = false;

    String jwtSecret = "";
    String jwtIssuer = "openhab-http-server";
    int jwtExpirationMinutes = 60;
    boolean jwtEnabled = false;

    boolean enableMetrics = true;
    boolean enableHealthChecks = true;
    int healthCheckInterval = 30000;
    boolean enablePerformanceMonitoring = true;
    String metricsEndpoint = "/metrics";

    boolean productionMode = false;
    int requestTimeout = 30000;
    int connectionTimeout = 10000;
    boolean enableGracefulShutdown = true;
    int shutdownTimeout = 30000;

    boolean enableCors = true;
    String corsAllowedOrigins = "*";
    String corsAllowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
    String corsAllowedHeaders = "Content-Type, Authorization, X-API-Key";

    boolean enableSsl = false;
    String sslKeyStore = "";
    String sslKeyStorePassword = "";
    String sslTrustStore = "";
    String sslTrustStorePassword = "";

    Map<String, Object> serverOptions = new HashMap<>();

    public HttpServerConfigurationBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public HttpServerConfigurationBuilder port(int port) {
        this.port = port;
        return this;
    }

    public HttpServerConfigurationBuilder contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public HttpServerConfigurationBuilder mcpServletPath(String mcpServletPath) {
        this.mcpServletPath = mcpServletPath;
        return this;
    }

    public HttpServerConfigurationBuilder mcpServletPattern(String mcpServletPattern) {
        this.mcpServletPattern = mcpServletPattern;
        return this;
    }

    public HttpServerConfigurationBuilder a2aServletPath(String a2aServletPath) {
        this.a2aServletPath = a2aServletPath;
        return this;
    }

    public HttpServerConfigurationBuilder a2aServletPattern(String a2aServletPattern) {
        this.a2aServletPattern = a2aServletPattern;
        return this;
    }

    public HttpServerConfigurationBuilder enableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
        return this;
    }

    public HttpServerConfigurationBuilder primaryAuthMethod(String primaryAuthMethod) {
        this.primaryAuthMethod = primaryAuthMethod;
        return this;
    }

    public HttpServerConfigurationBuilder fallbackAuthMethod(String fallbackAuthMethod) {
        this.fallbackAuthMethod = fallbackAuthMethod;
        return this;
    }

    public HttpServerConfigurationBuilder enableFallbackAuth(boolean enableFallbackAuth) {
        this.enableFallbackAuth = enableFallbackAuth;
        return this;
    }

    public HttpServerConfigurationBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public HttpServerConfigurationBuilder rateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
        return this;
    }

    public HttpServerConfigurationBuilder enableRequestValidation(boolean enableRequestValidation) {
        this.enableRequestValidation = enableRequestValidation;
        return this;
    }

    public HttpServerConfigurationBuilder oauthIssuerUrl(String oauthIssuerUrl) {
        this.oauthIssuerUrl = oauthIssuerUrl;
        return this;
    }

    public HttpServerConfigurationBuilder oauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
        return this;
    }

    public HttpServerConfigurationBuilder oauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
        return this;
    }

    public HttpServerConfigurationBuilder oauthRedirectUri(String oauthRedirectUri) {
        this.oauthRedirectUri = oauthRedirectUri;
        return this;
    }

    public HttpServerConfigurationBuilder oauthPkceEnabled(boolean oauthPkceEnabled) {
        this.oauthPkceEnabled = oauthPkceEnabled;
        return this;
    }

    public HttpServerConfigurationBuilder openhabUsersFile(String openhabUsersFile) {
        this.openhabUsersFile = openhabUsersFile;
        return this;
    }

    public HttpServerConfigurationBuilder openhabUsersEnabled(boolean openhabUsersEnabled) {
        this.openhabUsersEnabled = openhabUsersEnabled;
        return this;
    }

    public HttpServerConfigurationBuilder apiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
        return this;
    }

    public HttpServerConfigurationBuilder apiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
        return this;
    }

    public HttpServerConfigurationBuilder apiKeyEnabled(boolean apiKeyEnabled) {
        this.apiKeyEnabled = apiKeyEnabled;
        return this;
    }

    public HttpServerConfigurationBuilder jwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
        return this;
    }

    public HttpServerConfigurationBuilder jwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
        return this;
    }

    public HttpServerConfigurationBuilder jwtExpirationMinutes(int jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        return this;
    }

    public HttpServerConfigurationBuilder jwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
        return this;
    }

    public HttpServerConfigurationBuilder enableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }

    public HttpServerConfigurationBuilder enableHealthChecks(boolean enableHealthChecks) {
        this.enableHealthChecks = enableHealthChecks;
        return this;
    }

    public HttpServerConfigurationBuilder healthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    public HttpServerConfigurationBuilder enablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        return this;
    }

    public HttpServerConfigurationBuilder metricsEndpoint(String metricsEndpoint) {
        this.metricsEndpoint = metricsEndpoint;
        return this;
    }

    public HttpServerConfigurationBuilder productionMode(boolean productionMode) {
        this.productionMode = productionMode;
        return this;
    }

    public HttpServerConfigurationBuilder requestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public HttpServerConfigurationBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpServerConfigurationBuilder enableGracefulShutdown(boolean enableGracefulShutdown) {
        this.enableGracefulShutdown = enableGracefulShutdown;
        return this;
    }

    public HttpServerConfigurationBuilder shutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    public HttpServerConfigurationBuilder enableCors(boolean enableCors) {
        this.enableCors = enableCors;
        return this;
    }

    public HttpServerConfigurationBuilder corsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
        return this;
    }

    public HttpServerConfigurationBuilder corsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
        return this;
    }

    public HttpServerConfigurationBuilder corsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
        return this;
    }

    public HttpServerConfigurationBuilder enableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
        return this;
    }

    public HttpServerConfigurationBuilder sslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
        return this;
    }

    public HttpServerConfigurationBuilder sslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
        return this;
    }

    public HttpServerConfigurationBuilder sslTrustStore(String sslTrustStore) {
        this.sslTrustStore = sslTrustStore;
        return this;
    }

    public HttpServerConfigurationBuilder sslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = sslTrustStorePassword;
        return this;
    }

    public HttpServerConfigurationBuilder serverOption(String key, Object value) {
        this.serverOptions.put(key, value);
        return this;
    }

    public HttpServerConfigurationBuilder serverOptions(Map<String, Object> serverOptions) {
        this.serverOptions.clear();
        this.serverOptions.putAll(serverOptions);
        return this;
    }

    @Override
    public HttpServerConfiguration build() {
        return new HttpServerConfiguration(this);
    }

    @Override
    protected void validate() {
        validateRequiredString(baseUrl, "baseUrl");
        validateRange(port, "port", 1, 65535);
        validateRequiredString(contextPath, "contextPath");
        validatePositive(maxConnections, "maxConnections");
        validatePositive(rateLimitPerMinute, "rateLimitPerMinute");
        validatePositive(healthCheckInterval, "healthCheckInterval");
        validatePositive(requestTimeout, "requestTimeout");
        validatePositive(connectionTimeout, "connectionTimeout");
        validatePositive(shutdownTimeout, "shutdownTimeout");
        validatePositive(jwtExpirationMinutes, "jwtExpirationMinutes");
    }

    @Override
    protected void doReset() {
        baseUrl = "http://localhost:8080";
        port = 8080;
        contextPath = "/";
        mcpServletPath = "/mcp";
        mcpServletPattern = "/mcp/*";
        a2aServletPath = "/a2a";
        a2aServletPattern = "/a2a/*";
        enableAuthentication = false;
        primaryAuthMethod = "oauth2.1";
        fallbackAuthMethod = "openhab_users";
        enableFallbackAuth = true;
        maxConnections = 100;
        rateLimitPerMinute = 1000;
        enableRequestValidation = true;
        oauthIssuerUrl = "";
        oauthClientId = "";
        oauthClientSecret = "";
        oauthRedirectUri = "";
        oauthPkceEnabled = true;
        openhabUsersFile = "";
        openhabUsersEnabled = true;
        apiKeyHeader = "X-API-Key";
        apiKeyValue = "";
        apiKeyEnabled = false;
        jwtSecret = "";
        jwtIssuer = "openhab-http-server";
        jwtExpirationMinutes = 60;
        jwtEnabled = false;
        enableMetrics = true;
        enableHealthChecks = true;
        healthCheckInterval = 30000;
        enablePerformanceMonitoring = true;
        metricsEndpoint = "/metrics";
        productionMode = false;
        requestTimeout = 30000;
        connectionTimeout = 10000;
        enableGracefulShutdown = true;
        shutdownTimeout = 30000;
        enableCors = true;
        corsAllowedOrigins = "*";
        corsAllowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
        corsAllowedHeaders = "Content-Type, Authorization, X-API-Key";
        enableSsl = false;
        sslKeyStore = "";
        sslKeyStorePassword = "";
        sslTrustStore = "";
        sslTrustStorePassword = "";
        serverOptions = new HashMap<>();
    }
}
