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
 * Static utility class for recording tool compliance metrics.
 * 
 * <p>This class provides static methods for recording various tool compliance operations
 * including compliance testing, success/failure tracking, and validation.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record compliance test
 * ToolComplianceMetrics.recordComplianceTest(metricsService, "test-123", "security-scan", 
 *     true, Duration.ofMinutes(2), "COMPLIANT");
 * 
 * // Record compliance success
 * ToolComplianceMetrics.recordComplianceSuccess(metricsService, "test-456", "data-privacy", 
 *     Duration.ofSeconds(30), 0.95, "All checks passed");
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ToolComplianceMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ToolComplianceMetrics() {
        // Utility class
    }

    /**
     * Record compliance test metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param testId the unique identifier of the compliance test
     * @param testType the type of compliance test (security-scan, data-privacy, accessibility, etc.)
     * @param success whether the compliance test was successful
     * @param duration the duration of the compliance test
     * @param testResult the result of the compliance test (COMPLIANT, NON_COMPLIANT, WARNING, etc.)
     */
    public static void recordComplianceTest(MetricsService metricsService, String testId, String testType,
            boolean success, Duration duration, String testResult) {
        try {
            metricsService.recordOperation("compliance_test", success ? "success" : "failure")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("testId", testId)
                    .withData("testType", testType)
                    .withData("testResult", testResult)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance testing
        }
    }

    /**
     * Record compliance test success metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param testId the unique identifier of the compliance test
     * @param testType the type of compliance test
     * @param duration the duration of the compliance test
     * @param complianceScore the compliance score (0.0 to 1.0)
     * @param details additional details about the compliance success
     */
    public static void recordComplianceSuccess(MetricsService metricsService, String testId, String testType,
            Duration duration, double complianceScore, @Nullable String details) {
        try {
            var operation = metricsService.recordOperation("compliance_test", "success")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("testId", testId)
                    .withData("testType", testType)
                    .withData("complianceScore", complianceScore);

            if (details != null) {
                operation.withData("details", details);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance testing
        }
    }

    /**
     * Record compliance test failure metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param testId the unique identifier of the compliance test
     * @param testType the type of compliance test
     * @param duration the duration of the compliance test
     * @param failureReason the reason for the compliance failure
     * @param severity the severity of the compliance failure (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public static void recordComplianceFailure(MetricsService metricsService, String testId, String testType,
            Duration duration, String failureReason, String severity) {
        try {
            metricsService.recordOperation("compliance_test", "failure")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("testId", testId)
                    .withData("testType", testType)
                    .withData("failureReason", failureReason)
                    .withData("severity", severity)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance testing
        }
    }

    /**
     * Record compliance validation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param validationId the unique identifier of the compliance validation
     * @param complianceType the type of compliance being validated
     * @param success whether the compliance validation was successful
     * @param duration the duration of the compliance validation
     * @param validationDetails additional details about the validation process
     */
    public static void recordComplianceValidation(MetricsService metricsService, String validationId,
            String complianceType, boolean success, Duration duration, Map<String, Object> validationDetails) {
        try {
            var operation = metricsService.recordOperation("compliance_test", "validation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("validationId", validationId)
                    .withData("complianceType", complianceType);

            // Add validation details
            validationDetails.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance validation
        }
    }

    /**
     * Record compliance audit metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param auditId the unique identifier of the compliance audit
     * @param auditType the type of compliance audit (internal, external, regulatory, etc.)
     * @param success whether the compliance audit was successful
     * @param duration the duration of the compliance audit
     * @param auditScope the scope of the audit (system-wide, module-specific, etc.)
     * @param findingsCount the number of findings in the audit
     */
    public static void recordComplianceAudit(MetricsService metricsService, String auditId, String auditType,
            boolean success, Duration duration, String auditScope, int findingsCount) {
        try {
            metricsService.recordOperation("compliance_test", "audit")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("auditId", auditId)
                    .withData("auditType", auditType)
                    .withData("auditScope", auditScope)
                    .withData("findingsCount", findingsCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance auditing
        }
    }

    /**
     * Record compliance remediation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param remediationId the unique identifier of the compliance remediation
     * @param complianceType the type of compliance being remediated
     * @param success whether the compliance remediation was successful
     * @param duration the duration of the compliance remediation
     * @param issuesResolved the number of compliance issues resolved
     * @param remediationMethod the method used for remediation
     */
    public static void recordComplianceRemediation(MetricsService metricsService, String remediationId,
            String complianceType, boolean success, Duration duration, int issuesResolved, String remediationMethod) {
        try {
            metricsService.recordOperation("compliance_test", "remediation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("remediationId", remediationId)
                    .withData("complianceType", complianceType)
                    .withData("issuesResolved", issuesResolved)
                    .withData("remediationMethod", remediationMethod)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting compliance remediation
        }
    }
}
