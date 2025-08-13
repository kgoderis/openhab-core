package org.openhab.core.ai.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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