package org.openhab.core.ai.common.monitoring.api;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.service.snapshot.ActionExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentModelSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.CommunicationSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;


/**
 * Centralized metrics service for collecting and managing operation metrics.
 * 
 * <p>
 * This service provides a unified interface for recording metrics across all
 * AI operations including model completions, tool executions, and agent tasks.
 * It supports both basic counting metrics and advanced domain-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricsService {

    /**
     * Get a builder for flexible operation recording.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @return operation recorder builder
     */
    OperationRecorder recordOperation(String domain, String operation);

    // ===== Unified Generic Retrieval with Type Safety =====

    /**
     * Get a snapshot by MetricKey with type safety.
     * 
     * @param <T> the snapshot type
     * @param key the metric key
     * @param snapshotType the snapshot class
     * @return snapshot of the specified type, or null if not found
     */
    <T extends MetricsSnapshot> T getSnapshot(MetricKey key, Class<T> snapshotType);

    /**
     * Get a snapshot by MetricKey, returning the most appropriate type.
     * 
     * @param key the metric key
     * @return snapshot, or null if not found
     */
    MetricsSnapshot getSnapshot(MetricKey key);

    /**
     * Get snapshots by capability type.
     * 
     * @param <T> the snapshot type
     * @param capabilityType the capability class
     * @return list of snapshots with the specified capability
     */
    <T extends MetricsSnapshot> List<T> getSnapshotsByCapability(Class<T> capabilityType);

    /**
     * Get snapshots by domain.
     * 
     * @param <T> the snapshot type
     * @param domain the domain name
     * @param snapshotType the snapshot class
     * @return list of snapshots for the domain
     */
    <T extends MetricsSnapshot> List<T> getSnapshotsByDomain(String domain, Class<T> snapshotType);

    // ===== Generic Statistics Retrieval =====

    /**
     * Get statistics by MetricKey with type safety.
     * 
     * @param <T> the statistics type
     * @param key the metric key
     * @param statisticsType the statistics class
     * @param timeRange the time range for aggregation
     * @return statistics of the specified type
     */
    <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> T getStatistics(MetricKey key, Class<T> statisticsType, Duration timeRange);

    /**
     * Get statistics by capability type.
     * 
     * @param <T> the statistics type
     * @param capabilityType the capability class
     * @param timeRange the time range for aggregation
     * @return list of statistics with the specified capability
     */
    <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByCapability(Class<T> capabilityType, Duration timeRange);

    /**
     * Get statistics by domain.
     * 
     * @param <T> the statistics type
     * @param domain the domain name
     * @param statisticsType the statistics class
     * @param timeRange the time range for aggregation
     * @return list of statistics for the domain
     */
    <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByDomain(String domain, Class<T> statisticsType, Duration timeRange);

    // ===== Recording Methods =====

    /**
     * Record a basic operation with success/failure status.
     * 
     * @param domain the operation domain (e.g., "model", "tool", "agent")
     * @param operation the operation name (e.g., "completion", "file_read", "task_execution")
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    void recordOperation(String domain, String operation, boolean success, Duration duration);

    /**
     * Record an operation with additional data.
     * 
     * @param domain the operation domain (e.g., "model", "tool", "agent")
     * @param operation the operation name (e.g., "completion", "file_read", "task_execution")
     * @param success whether the operation was successful
     * @param duration the operation duration
     * @param data additional operation data as key-value pairs
     */
    void recordOperationWithData(String domain, String operation, boolean success, Duration duration,
            Map<String, Object> data);

    /**
     * Record a model completion operation.
     * 
     * @param modelId the model identifier
     * @param success whether the completion was successful
     * @param duration the completion duration
     * @param inputTokens number of input tokens
     * @param outputTokens number of output tokens
     * @param cost the operation cost
     */
    void recordModelCompletion(String modelId, boolean success, Duration duration, int inputTokens, int outputTokens,
            double cost);

    /**
     * Record a tool file read operation.
     * 
     * @param toolId the tool identifier
     * @param success whether the read was successful
     * @param duration the read duration
     * @param fileSize the file size in bytes
     * @param fileType the file type
     */
    void recordToolFileRead(String toolId, boolean success, Duration duration, long fileSize, String fileType);

    /**
     * Record an agent task operation.
     * 
     * @param agentId the agent identifier
     * @param success whether the task was successful
     * @param duration the task duration
     * @param taskType the task type
     * @param decisionAccuracy the decision accuracy (0.0 to 1.0)
     * @param learningRate the learning rate (0.0 to 1.0)
     */
    void recordAgentTask(String agentId, boolean success, Duration duration, String taskType, double decisionAccuracy,
            double learningRate);

    /**
     * Record a monitoring operation.
     * 
     * @param monitoringId the monitoring identifier
     * @param success whether the monitoring operation was successful
     * @param duration the monitoring duration
     * @param metricsCollected number of metrics collected
     * @param alertsGenerated number of alerts generated
     * @param metricType the type of metrics collected
     * @param alertSeverity the severity of alerts generated
     */
    void recordMonitoringOperation(String monitoringId, boolean success, Duration duration, int metricsCollected,
            int alertsGenerated, String metricType, String alertSeverity);

    /**
     * Record an error recovery operation.
     * 
     * @param errorType the type of error
     * @param success whether the recovery was successful
     * @param duration the recovery duration
     * @param recoveryStrategy the recovery strategy used
     * @param fallbackUsed whether a fallback was used
     */
    void recordErrorRecovery(String errorType, boolean success, Duration duration, String recoveryStrategy,
            boolean fallbackUsed);



    /**
     * Get a generic snapshot for a specific domain and operation.
     * 
     * @param domain the operation domain (e.g., "model", "tool", "agent")
     * @param operation the operation name (e.g., "completion", "file_read", "task_execution")
     * @return generic snapshot containing metrics for the specified domain and operation
     */
    GenericMetricsSnapshot getSnapshot(String domain, String operation);

    /**
     * Get all snapshots of a specific type.
     * 
     * @param <T> the snapshot type
     * @param snapshotType the snapshot class
     * @return list of snapshots
     */
    <T extends MetricsSnapshot> List<T> getAllSnapshots(Class<T> snapshotType);
}
