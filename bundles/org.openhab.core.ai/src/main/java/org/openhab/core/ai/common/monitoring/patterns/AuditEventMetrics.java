package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Static utility class for recording audit event metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides static methods to record comprehensive metrics for audit events
 * with categories, severity levels, and user behavior patterns with full context.
 * All methods require a MetricsService instance as the first parameter.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AuditEventMetrics {

    private AuditEventMetrics() {
        // Utility class - prevent instantiation
    }

    /**
     * Record audit event metrics.
     * 
     * @param metricsService the metrics service instance
     * @param eventId the unique event identifier
     * @param eventCategory the category of the audit event
     * @param eventType the type of event
     * @param severityLevel the severity level (1-5)
     * @param userId the ID of the user who triggered the event
     * @param eventData additional event data
     * @param processingTime the time taken to process the event
     */
    public static void recordAuditEvent(MetricsService metricsService, String eventId, String eventCategory, String eventType, int severityLevel,
            String userId, Map<String, Object> eventData, Duration processingTime) {
        boolean success = processingTime.toMillis() < 100; // Consider successful if under 100ms

        long eventDataSize = eventData.values().stream().mapToLong(v -> v.toString().length()).sum();

        metricsService.recordOperation("audit", "event").withSuccess(success).withDuration(processingTime.toNanos())
                .withData("eventId", eventId).withData("eventCategory", eventCategory).withData("eventType", eventType)
                .withData("severityLevel", severityLevel).withData("userId", userId)
                .withData("eventDataSize", eventDataSize)
                // Performance Metrics Enhancements
                .withTimingContext(3, eventDataSize, 0) // Simple audit event processing
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "audit-processing-timeout", success ? null : 1, 0, false)
                .withUserExperienceMetrics(processingTime.toMillis(), 0, 4) // Good user experience for audit
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record security audit event metrics.
     * 
     * @param metricsService the metrics service instance
     * @param eventId the unique event identifier
     * @param securityEventType the type of security event
     * @param threatLevel the threat level (1-5)
     * @param sourceIp the source IP address
     * @param targetResource the target resource
     * @param actionTaken the action taken in response
     * @param processingTime the time taken to process the security event
     */
    public static void recordSecurityAuditEvent(MetricsService metricsService, String eventId, String securityEventType, int threatLevel, String sourceIp,
            String targetResource, String actionTaken, Duration processingTime) {
        boolean success = processingTime.toMillis() < 50; // Security events should be processed quickly

        metricsService.recordOperation("audit", "security-event").withSuccess(success)
                .withDuration(processingTime.toNanos()).withData("eventId", eventId)
                .withData("securityEventType", securityEventType).withData("threatLevel", threatLevel)
                .withData("sourceIp", sourceIp).withData("targetResource", targetResource)
                .withData("actionTaken", actionTaken)
                // Performance Metrics Enhancements
                .withTimingContext(6, 0, 0) // High complexity for security processing
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "security-processing-delay", success ? null : threatLevel, 0,
                        false)
                .withUserExperienceMetrics(processingTime.toMillis(), 0, success ? 5 : 1) // Critical for security
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record user behavior audit metrics.
     * 
     * @param metricsService the metrics service instance
     * @param userId the unique user identifier
     * @param behaviorType the type of behavior being audited
     * @param sessionId the session identifier
     * @param actionCount the number of actions in the behavior pattern
     * @param riskScore the calculated risk score (0-100)
     * @param analysisTime the time taken to analyze the behavior
     */
    public static void recordUserBehaviorAudit(MetricsService metricsService, String userId, String behaviorType, String sessionId, int actionCount,
            double riskScore, Duration analysisTime) {
        boolean success = analysisTime.toMillis() < 500; // Behavior analysis should be reasonably fast

        metricsService.recordOperation("audit", "user-behavior").withSuccess(success)
                .withDuration(analysisTime.toNanos()).withData("userId", userId).withData("behaviorType", behaviorType)
                .withData("sessionId", sessionId).withData("actionCount", actionCount).withData("riskScore", riskScore)
                // Performance Metrics Enhancements
                .withTimingContext(7, actionCount * 50L, 0) // High complexity for behavior analysis
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "behavior-analysis-timeout", success ? null : 2, 0, false)
                .withUserExperienceMetrics(analysisTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record audit event sequence metrics.
     * 
     * @param metricsService the metrics service instance
     * @param sequenceId the unique sequence identifier
     * @param eventCount the number of events in the sequence
     * @param sequenceDuration the total duration of the sequence
     * @param patternType the type of pattern detected
     * @param confidenceScore the confidence score for the pattern (0-100)
     * @param processingTime the time taken to process the sequence
     */
    public static void recordAuditEventSequence(MetricsService metricsService, String sequenceId, int eventCount, Duration sequenceDuration,
            String patternType, double confidenceScore, Duration processingTime) {
        boolean success = processingTime.toMillis() < 1000; // Sequence processing can take longer

        metricsService.recordOperation("audit", "event-sequence").withSuccess(success)
                .withDuration(processingTime.toNanos()).withData("sequenceId", sequenceId)
                .withData("eventCount", eventCount).withData("sequenceDuration", sequenceDuration.toMillis())
                .withData("patternType", patternType).withData("confidenceScore", confidenceScore)
                // Performance Metrics Enhancements
                .withTimingContext(8, eventCount * 100L, 0) // Very high complexity for sequence analysis
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "sequence-processing-timeout", success ? null : 3, 0, false)
                .withUserExperienceMetrics(processingTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record audit compliance metrics.
     * 
     * @param metricsService the metrics service instance
     * @param complianceId the unique compliance identifier
     * @param complianceType the type of compliance check
     * @param checkResult whether the compliance check passed
     * @param violationCount the number of violations found
     * @param remediationActions the number of remediation actions taken
     * @param checkTime the time taken to perform the compliance check
     */
    public static void recordAuditCompliance(MetricsService metricsService, String complianceId, String complianceType, boolean checkResult,
            int violationCount, int remediationActions, Duration checkTime) {
        boolean success = checkTime.toMillis() < 2000; // Compliance checks can take time

        metricsService.recordOperation("audit", "compliance").withSuccess(success).withDuration(checkTime.toNanos())
                .withData("complianceId", complianceId).withData("complianceType", complianceType)
                .withData("checkResult", checkResult).withData("violationCount", violationCount)
                .withData("remediationActions", remediationActions)
                // Performance Metrics Enhancements
                .withTimingContext(6, 0, 0) // High complexity for compliance checking
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "compliance-check-timeout", success ? null : 2, 0, false)
                .withUserExperienceMetrics(checkTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record audit retention metrics.
     * 
     * @param metricsService the metrics service instance
     * @param retentionId the unique retention identifier
     * @param retentionType the type of retention operation
     * @param recordCount the number of records processed
     * @param retentionPeriod the retention period in days
     * @param operationTime the time taken for the retention operation
     */
    public static void recordAuditRetention(MetricsService metricsService, String retentionId, String retentionType, int recordCount, int retentionPeriod,
            Duration operationTime) {
        boolean success = operationTime.toMillis() < 5000; // Retention operations can take time

        metricsService.recordOperation("audit", "retention").withSuccess(success).withDuration(operationTime.toNanos())
                .withData("retentionId", retentionId).withData("retentionType", retentionType)
                .withData("recordCount", recordCount).withData("retentionPeriod", retentionPeriod)
                // Performance Metrics Enhancements
                .withTimingContext(5, recordCount * 10L, 0) // Moderate complexity for retention
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "retention-operation-timeout", success ? null : 2, 0, false)
                .withUserExperienceMetrics(operationTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }
}
