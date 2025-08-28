/*
 * Copyright (c) 2010-2024 openHAB e.V. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Static utility class for recording tool security metrics.
 * 
 * <p>This class provides static methods for recording various tool security operations
 * including protocol security, authentication, authorization, and rate limiting.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record protocol security
 * ToolSecurityMetrics.recordProtocolSecurity(metricsService, "HTTP", "request", 
 *     true, Duration.ofMillis(10), "LOW");
 * 
 * // Record authentication
 * ToolSecurityMetrics.recordAuthentication(metricsService, "JWT", true, 
 *     Duration.ofMillis(50), "user-123", "token");
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ToolSecurityMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ToolSecurityMetrics() {
        // Utility class
    }

    /**
     * Record protocol security metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param protocol the protocol being secured (HTTP, HTTPS, MQTT, etc.)
     * @param operation the type of security operation (request, response, filter, etc.)
     * @param success whether the security operation was successful
     * @param duration the duration of the security operation
     * @param threatLevel the threat level detected (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public static void recordProtocolSecurity(MetricsService metricsService, String protocol, String operation,
            boolean success, Duration duration, String threatLevel) {
        try {
            metricsService.recordOperation("protocol-security", "filter")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("protocol", protocol)
                    .withData("operation", operation)
                    .withData("threatLevel", threatLevel)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting security operations
        }
    }

    /**
     * Record security filter metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param filterType the type of security filter (authentication, authorization, rate-limit, etc.)
     * @param operation the type of filter operation (check, apply, bypass, etc.)
     * @param success whether the filter operation was successful
     * @param duration the duration of the filter operation
     * @param filterResult the result of the filter operation (ALLOW, DENY, BYPASS, etc.)
     */
    public static void recordSecurityFilter(MetricsService metricsService, String filterType, String operation,
            boolean success, Duration duration, String filterResult) {
        try {
            metricsService.recordOperation("security-filter", "authentication")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("filterType", filterType)
                    .withData("operation", operation)
                    .withData("filterResult", filterResult)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting security operations
        }
    }

    /**
     * Record authentication metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param authType the type of authentication (JWT, OAuth, Basic, etc.)
     * @param success whether the authentication was successful
     * @param duration the duration of the authentication process
     * @param userId the identifier of the user being authenticated
     * @param authMethod the authentication method used
     */
    public static void recordAuthentication(MetricsService metricsService, String authType, boolean success,
            Duration duration, @Nullable String userId, String authMethod) {
        try {
            var operation = metricsService.recordOperation("security-filter", "authentication")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("authType", authType)
                    .withData("authMethod", authMethod);

            if (userId != null) {
                operation.withData("userId", userId);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting authentication
        }
    }

    /**
     * Record authorization metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param resource the resource being accessed
     * @param operation the operation being performed on the resource
     * @param success whether the authorization was successful
     * @param duration the duration of the authorization check
     * @param permissionLevel the permission level required/granted
     */
    public static void recordAuthorization(MetricsService metricsService, String resource, String operation,
            boolean success, Duration duration, String permissionLevel) {
        try {
            metricsService.recordOperation("security-filter", "authorization")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("resource", resource)
                    .withData("operation", operation)
                    .withData("permissionLevel", permissionLevel)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting authorization
        }
    }

    /**
     * Record rate limiting metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param endpoint the endpoint being rate limited
     * @param operation the type of rate limiting operation (check, apply, reset, etc.)
     * @param success whether the rate limiting operation was successful
     * @param duration the duration of the rate limiting operation
     * @param rateLimit the configured rate limit (requests per time unit)
     * @param currentRate the current rate of requests
     */
    public static void recordRateLimiting(MetricsService metricsService, String endpoint, String operation,
            boolean success, Duration duration, int rateLimit, int currentRate) {
        try {
            metricsService.recordOperation("security-filter", "rate-limit")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("endpoint", endpoint)
                    .withData("operation", operation)
                    .withData("rateLimit", rateLimit)
                    .withData("currentRate", currentRate)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting rate limiting
        }
    }

    /**
     * Record security audit metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param auditType the type of security audit (access, permission, violation, etc.)
     * @param success whether the audit was successful
     * @param duration the duration of the audit process
     * @param auditResult the result of the audit (COMPLIANT, NON_COMPLIANT, WARNING, etc.)
     * @param context additional context data for the audit
     */
    public static void recordSecurityAudit(MetricsService metricsService, String auditType, boolean success,
            Duration duration, String auditResult, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("security-filter", "audit")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("auditType", auditType)
                    .withData("auditResult", auditResult);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting security audits
        }
    }

    /**
     * Record security violation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param violationType the type of security violation (unauthorized_access, rate_limit_exceeded, etc.)
     * @param severity the severity of the violation (LOW, MEDIUM, HIGH, CRITICAL)
     * @param duration the time taken to detect the violation
     * @param sourceIp the source IP address of the violation
     * @param context additional context data about the violation
     */
    public static void recordSecurityViolation(MetricsService metricsService, String violationType, String severity,
            Duration duration, @Nullable String sourceIp, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("security-filter", "violation")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("violationType", violationType)
                    .withData("severity", severity);

            if (sourceIp != null) {
                operation.withData("sourceIp", sourceIp);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting security monitoring
        }
    }
}
