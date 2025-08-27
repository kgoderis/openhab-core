package org.openhab.core.ai.common.monitoring.service.factory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentPersistenceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.CoordinationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ModelCompletionStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.OptimizationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.SecurityMonitoringStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;
import org.openhab.core.ai.common.monitoring.service.statistics.ToolFileReadStatistics;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;
import org.openhab.core.ai.tool.monitoring.MonitoringStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating statistics from metrics snapshots with capability validation.
 * 
 * <p>
 * This factory provides a centralized way to create various types of statistics
 * from metrics snapshots, with validation that the metric key supports the
 * requested statistics type.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class StatisticsFactory {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsFactory.class);

    private StatisticsFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Create statistics from snapshots with capability validation.
     * 
     * @param <T> the statistics type
     * @param snapshots the list of snapshots to aggregate
     * @param statisticsType the class of the statistics type
     * @param key the metric key
     * @param timeRange the time range for the statistics
     * @return the created statistics
     * @throws IllegalArgumentException if the statistics type is not supported or incompatible with key capabilities
     */
    @SuppressWarnings("unchecked")
    public static <T extends StatisticsSnapshot> T createStatistics(
            List<MetricsSnapshot> snapshots,
            Class<T> statisticsType,
            MetricKey key,
            Duration timeRange) {

        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(statisticsType, "statisticsType");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(timeRange, "timeRange");

        // Validate capabilities first
        if (!isCompatible(statisticsType, key.capabilities())) {
            throw new IllegalArgumentException("Statistics type " + statisticsType.getSimpleName() + 
                " not compatible with key capabilities: " + key.capabilities() + 
                " for key: " + key.id());
        }

        // Filter snapshots within time range
        Instant cutoff = Instant.now().minus(timeRange);
        List<MetricsSnapshot> filteredSnapshots = snapshots.stream()
            .filter(snapshot -> snapshot.getTimestampMs() >= cutoff.toEpochMilli())
            .collect(Collectors.toList());

        // Route to appropriate statistics creation method
        if (statisticsType == ModelCompletionStatistics.class) {
            return (T) createModelCompletionStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == ToolFileReadStatistics.class) {
            return (T) createToolFileReadStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == AgentBehaviorStatistics.class) {
            return (T) createAgentBehaviorStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == AgentPersistenceStatistics.class) {
            return (T) createAgentPersistenceStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == MonitoringStatistics.class) {
            return (T) createMonitoringStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == ErrorRecoveryStatistics.class) {
            return (T) createErrorRecoveryStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == ReasoningPerformanceStatistics.class) {
            return (T) createReasoningPerformanceStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == CoordinationStatistics.class) {
            return (T) createCoordinationStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == OptimizationStatistics.class) {
            return (T) createOptimizationStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == SecurityMonitoringStatistics.class) {
            return (T) createSecurityMonitoringStatistics(filteredSnapshots, timeRange);
        } else if (statisticsType == MessageSecurityStatistics.class) {
            return (T) createMessageSecurityStatistics(filteredSnapshots, timeRange);
        } else {
            throw new IllegalArgumentException("Unsupported statistics type: " + statisticsType.getSimpleName());
        }
    }

    // ===== Statistics Creation Methods =====

    private static ModelCompletionStatistics createModelCompletionStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to ModelCompletionSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot> modelSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot) s)
                .collect(Collectors.toList());
        
        return new ModelCompletionStatistics(modelSnapshots, timeRange, System.currentTimeMillis());
    }

    private static ToolFileReadStatistics createToolFileReadStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to ToolFileReadSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot> toolSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot) s)
                .collect(Collectors.toList());
        
        return new ToolFileReadStatistics(toolSnapshots, timeRange, System.currentTimeMillis());
    }

    private static AgentBehaviorStatistics createAgentBehaviorStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to AgentTaskSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot> agentSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot) s)
                .collect(Collectors.toList());
        
        return new AgentBehaviorStatistics(agentSnapshots, timeRange, System.currentTimeMillis());
    }

    private static AgentPersistenceStatistics createAgentPersistenceStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to AgentTaskSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot> agentSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot) s)
                .collect(Collectors.toList());
        
        return new AgentPersistenceStatistics(agentSnapshots, timeRange, System.currentTimeMillis());
    }

    private static MonitoringStatistics createMonitoringStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // For monitoring statistics, we'll use a simplified approach
        return MonitoringStatistics.empty(timeRange);
    }

    private static ErrorRecoveryStatistics createErrorRecoveryStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to ErrorRecoverySnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot> errorSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot) s)
                .collect(Collectors.toList());
        
        return new ErrorRecoveryStatistics(errorSnapshots, timeRange, System.currentTimeMillis());
    }

    private static ReasoningPerformanceStatistics createReasoningPerformanceStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to AgentTaskSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot> agentSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot) s)
                .collect(Collectors.toList());
        
        return new ReasoningPerformanceStatistics(agentSnapshots, timeRange, System.currentTimeMillis());
    }

    private static CoordinationStatistics createCoordinationStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to AgentTaskSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot> agentSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot) s)
                .collect(Collectors.toList());
        
        return new CoordinationStatistics(agentSnapshots, timeRange, System.currentTimeMillis());
    }

    private static OptimizationStatistics createOptimizationStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to AgentTaskSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot> agentSnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot) s)
                .collect(Collectors.toList());
        
        return new OptimizationStatistics(agentSnapshots, timeRange, System.currentTimeMillis());
    }

    private static SecurityMonitoringStatistics createSecurityMonitoringStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // Convert snapshots to SecurityMonitoringSnapshot if needed
        List<org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot> securitySnapshots = 
            snapshots.stream()
                .filter(s -> s instanceof org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot)
                .map(s -> (org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot) s)
                .collect(Collectors.toList());
        
        return new SecurityMonitoringStatistics(securitySnapshots, timeRange, System.currentTimeMillis());
    }

    private static MessageSecurityStatistics createMessageSecurityStatistics(
            List<MetricsSnapshot> snapshots, Duration timeRange) {
        
        // For message security statistics, we'll use a simplified approach
        return MessageSecurityStatistics.empty(timeRange);
    }

    // ===== Capability Compatibility Check =====

    /**
     * Check if a statistics type is compatible with the key's capabilities.
     * 
     * @param statisticsType the statistics type to check
     * @param capabilities the key's capabilities
     * @return true if compatible, false otherwise
     */
    private static boolean isCompatible(Class<?> statisticsType, Set<String> capabilities) {
        if (statisticsType == ModelCompletionStatistics.class) {
            return capabilities.contains("model-stats");
        }
        if (statisticsType == ToolFileReadStatistics.class) {
            return capabilities.contains("tool-stats");
        }
        if (statisticsType == AgentBehaviorStatistics.class) {
            return capabilities.contains("behavior-stats");
        }
        if (statisticsType == AgentPersistenceStatistics.class) {
            return capabilities.contains("persistence-stats");
        }
        if (statisticsType == MonitoringStatistics.class) {
            return capabilities.contains("monitoring-stats");
        }
        if (statisticsType == ErrorRecoveryStatistics.class) {
            return capabilities.contains("error-stats");
        }
        if (statisticsType == ReasoningPerformanceStatistics.class) {
            return capabilities.contains("reasoning-stats");
        }
        if (statisticsType == CoordinationStatistics.class) {
            return capabilities.contains("coordination-stats");
        }
        if (statisticsType == OptimizationStatistics.class) {
            return capabilities.contains("optimization-stats");
        }
        if (statisticsType == SecurityMonitoringStatistics.class) {
            return capabilities.contains("security-stats");
        }
        if (statisticsType == MessageSecurityStatistics.class) {
            return capabilities.contains("security-stats");
        }
        
        // Default to compatible for unknown types
        return true;
    }
}
