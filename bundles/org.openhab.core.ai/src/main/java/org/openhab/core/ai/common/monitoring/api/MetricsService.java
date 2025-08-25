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
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentPersistenceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.CoordinationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ModelCompletionStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.OptimizationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.SecurityMonitoringStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ToolFileReadStatistics;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;
import org.openhab.core.ai.tool.monitoring.MonitoringStatistics;

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
     * Get a builder for flexible operation recording.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @return operation recorder builder
     */
    OperationRecorder recordOperation(String domain, String operation);

    /**
     * Get model completion snapshot.
     * 
     * @param modelId the model identifier
     * @return model completion snapshot
     */
    ModelCompletionSnapshot getModelCompletionSnapshot(String modelId);

    /**
     * Get tool file read snapshot.
     * 
     * @param toolId the tool identifier
     * @return tool file read snapshot
     */
    ToolFileReadSnapshot getToolFileReadSnapshot(String toolId);

    /**
     * Get agent task snapshot.
     * 
     * @param agentId the agent identifier
     * @return agent task snapshot
     */
    AgentTaskSnapshot getAgentTaskSnapshot(String agentId);

    /**
     * Get agent model snapshot.
     * 
     * @param modelId the model identifier
     * @return agent model snapshot
     */
    AgentModelSnapshot getAgentModelSnapshot(String modelId);

    /**
     * Get action execution snapshot.
     * 
     * @param actionId the action identifier
     * @return action execution snapshot
     */
    ActionExecutionSnapshot getActionExecutionSnapshot(String actionId);

    /**
     * Get tool execution snapshot.
     * 
     * @param toolId the tool identifier
     * @return tool execution snapshot
     */
    ToolExecutionSnapshot getToolExecutionSnapshot(String toolId);

    /**
     * Get communication snapshot.
     * 
     * @param communicationId the communication identifier
     * @return communication snapshot
     */
    CommunicationSnapshot getCommunicationSnapshot(String communicationId);

    /**
     * Get error recovery snapshot.
     * 
     * @param errorType the error type
     * @return error recovery snapshot
     */
    ErrorRecoverySnapshot getErrorRecoverySnapshot(String errorType);

    /**
     * Get model completion statistics.
     * 
     * @param modelId the model identifier
     * @param timeRange the time range for statistics
     * @return model completion statistics
     */
    ModelCompletionStatistics getModelCompletionStatistics(String modelId, Duration timeRange);

    /**
     * Get tool file read statistics.
     * 
     * @param toolId the tool identifier
     * @param timeRange the time range for statistics
     * @return tool file read statistics
     */
    ToolFileReadStatistics getToolFileReadStatistics(String toolId, Duration timeRange);

    /**
     * Get agent behavior statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return agent behavior statistics
     */
    AgentBehaviorStatistics getAgentBehaviorStatistics(String agentId, Duration timeRange);

    /**
     * Get agent persistence statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return agent persistence statistics
     */
    AgentPersistenceStatistics getAgentPersistenceStatistics(String agentId, Duration timeRange);

    /**
     * Get monitoring statistics.
     * 
     * @param monitoringId the monitoring identifier
     * @param timeRange the time range for statistics
     * @return monitoring statistics
     */
    MonitoringStatistics getMonitoringStatistics(String monitoringId, Duration timeRange);

    /**
     * Get error recovery statistics.
     * 
     * @param errorType the error type
     * @param timeRange the time range for statistics
     * @return error recovery statistics
     */
    ErrorRecoveryStatistics getErrorRecoveryStatistics(String errorType, Duration timeRange);

    /**
     * Get reasoning performance statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return reasoning performance statistics
     */
    ReasoningPerformanceStatistics getReasoningPerformanceStatistics(String agentId, Duration timeRange);

    /**
     * Get coordination statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return coordination statistics
     */
    CoordinationStatistics getCoordinationStatistics(String agentId, Duration timeRange);

    /**
     * Get coordination statistics for agent coordination manager.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return coordination statistics in local format
     */
    org.openhab.core.ai.agent.collaboration.coordination.CoordinationStatistics getAgentCoordinationStatistics(
            String agentId, Duration timeRange);

    /**
     * Get optimization statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return optimization statistics
     */
    OptimizationStatistics getOptimizationStatistics(String agentId, Duration timeRange);

    /**
     * Get all snapshots of a specific type.
     * 
     * @param <T> the snapshot type
     * @param snapshotType the snapshot class
     * @return list of snapshots
     */
    <T extends MetricsSnapshot> List<T> getAllSnapshots(Class<T> snapshotType);

    /**
     * Get domain aggregated snapshot.
     * 
     * @param domain the domain
     * @return domain aggregated snapshot
     */
    DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain);

    /**
     * Get security monitoring snapshot.
     * 
     * @param securityId the security identifier
     * @return security monitoring snapshot
     */
    SecurityMonitoringSnapshot getSecurityMonitoringSnapshot(String securityId);

    /**
     * Get security monitoring statistics.
     * 
     * @param securityId the security identifier
     * @param timeRange the time range for statistics
     * @return security monitoring statistics
     */
    SecurityMonitoringStatistics getSecurityMonitoringStatistics(String securityId, Duration timeRange);

    /**
     * Get message security statistics.
     * 
     * @param securityId the security identifier
     * @param timeRange the time range for statistics
     * @return message security statistics
     */
    MessageSecurityStatistics getMessageSecurityStatistics(String securityId, Duration timeRange);
}
