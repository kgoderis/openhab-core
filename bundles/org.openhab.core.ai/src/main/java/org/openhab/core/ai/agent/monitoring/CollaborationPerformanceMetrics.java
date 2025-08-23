package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance metrics for agent collaboration operations.
 * 
 * <p>
 * This class provides comprehensive metrics for agent collaboration operations:
 * - Collaboration session statistics
 * - Communication performance metrics
 * - Resource sharing and coordination metrics
 * - Error tracking and failure analysis
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = CollaborationPerformanceMetrics.class)
@NonNullByDefault
public class CollaborationPerformanceMetrics {

    private static final Logger logger = LoggerFactory.getLogger(CollaborationPerformanceMetrics.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Record collaboration session using the new monitoring framework
     */
    public void recordCollaborationSession(String sessionId, String agentId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("collaboration-session"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Collaboration session successful - Session: {}, Agent: {}, Duration: {}ms", sessionId,
                        agentId, duration);
            } else {
                logger.warn("Collaboration session failed - Session: {}, Agent: {}, Duration: {}ms", sessionId, agentId,
                        duration);
            }

        } catch (Exception e) {
            logger.error("Error recording collaboration session metrics for session: {}", sessionId, e);
        }
    }

    /**
     * Record communication performance using the new monitoring framework
     */
    public void recordCommunicationPerformance(String agentId, String targetAgentId, long latency, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("agent-communication"));
                collector.recordExecution(success, latency * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Communication successful - From: {}, To: {}, Latency: {}ms", agentId, targetAgentId,
                        latency);
            } else {
                logger.warn("Communication failed - From: {}, To: {}, Latency: {}ms", agentId, targetAgentId, latency);
            }

        } catch (Exception e) {
            logger.error("Error recording communication performance metrics for agent: {}", agentId, e);
        }
    }

    /**
     * Record resource sharing using the new monitoring framework
     */
    public void recordResourceSharing(String resourceId, String agentId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("resource-sharing"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Resource sharing successful - Resource: {}, Agent: {}, Duration: {}ms", resourceId,
                        agentId, duration);
            } else {
                logger.warn("Resource sharing failed - Resource: {}, Agent: {}, Duration: {}ms", resourceId, agentId,
                        duration);
            }

        } catch (Exception e) {
            logger.error("Error recording resource sharing metrics for resource: {}", resourceId, e);
        }
    }

    /**
     * Record coordination operation using the new monitoring framework
     */
    public void recordCoordinationOperation(String operationId, String agentId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("coordination-operation"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Coordination operation successful - Operation: {}, Agent: {}, Duration: {}ms",
                        operationId, agentId, duration);
            } else {
                logger.warn("Coordination operation failed - Operation: {}, Agent: {}, Duration: {}ms", operationId,
                        agentId, duration);
            }

        } catch (Exception e) {
            logger.error("Error recording coordination operation metrics for operation: {}", operationId, e);
        }
    }

    /**
     * Get collaboration performance statistics using the new monitoring framework
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry
            ExecutionMetricsCollector sessionCollector = monitoringRegistry
                    .executionCollector(MetricKeys.action("collaboration-session"));
            ExecutionMetricsCollector communicationCollector = monitoringRegistry
                    .executionCollector(MetricKeys.action("agent-communication"));
            ExecutionMetricsCollector resourceCollector = monitoringRegistry
                    .executionCollector(MetricKeys.action("resource-sharing"));
            ExecutionMetricsCollector coordinationCollector = monitoringRegistry
                    .executionCollector(MetricKeys.action("coordination-operation"));

            var sessionSnapshot = sessionCollector.snapshot();
            var communicationSnapshot = communicationCollector.snapshot();
            var resourceSnapshot = resourceCollector.snapshot();
            var coordinationSnapshot = coordinationCollector.snapshot();

            statistics.put("totalCollaborationSessions", sessionSnapshot.total());
            statistics.put("successfulCollaborationSessions", sessionSnapshot.success());
            statistics.put("failedCollaborationSessions", sessionSnapshot.failure());
            statistics.put("totalCommunications", communicationSnapshot.total());
            statistics.put("successfulCommunications", communicationSnapshot.success());
            statistics.put("failedCommunications", communicationSnapshot.failure());
            statistics.put("totalResourceSharings", resourceSnapshot.total());
            statistics.put("successfulResourceSharings", resourceSnapshot.success());
            statistics.put("failedResourceSharings", resourceSnapshot.failure());
            statistics.put("totalCoordinationOperations", coordinationSnapshot.total());
            statistics.put("successfulCoordinationOperations", coordinationSnapshot.success());
            statistics.put("failedCoordinationOperations", coordinationSnapshot.failure());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("totalCollaborationSessions", 0);
            statistics.put("successfulCollaborationSessions", 0);
            statistics.put("failedCollaborationSessions", 0);
            statistics.put("totalCommunications", 0);
            statistics.put("successfulCommunications", 0);
            statistics.put("failedCommunications", 0);
            statistics.put("totalResourceSharings", 0);
            statistics.put("successfulResourceSharings", 0);
            statistics.put("failedResourceSharings", 0);
            statistics.put("totalCoordinationOperations", 0);
            statistics.put("successfulCoordinationOperations", 0);
            statistics.put("failedCoordinationOperations", 0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }
}
