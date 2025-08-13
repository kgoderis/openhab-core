package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conflict pattern analysis result.
 *
 * Aggregated metrics for conflicts over a given time range.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictPatternAnalysis {
    private final int totalConflicts;
    private final Map<String, Integer> agentConflictCounts;
    private final Map<ConflictType, Integer> typeConflictCounts;
    private final Map<ConflictPriority, Integer> priorityConflictCounts;
    private final Duration timeRange;

    public ConflictPatternAnalysis(int totalConflicts, Map<String, Integer> agentConflictCounts,
            Map<ConflictType, Integer> typeConflictCounts, Map<ConflictPriority, Integer> priorityConflictCounts,
            Duration timeRange) {
        this.totalConflicts = totalConflicts;
        this.agentConflictCounts = agentConflictCounts;
        this.typeConflictCounts = typeConflictCounts;
        this.priorityConflictCounts = priorityConflictCounts;
        this.timeRange = timeRange;
    }

    public int getTotalConflicts() { return totalConflicts; }
    public Map<String, Integer> getAgentConflictCounts() { return agentConflictCounts; }
    public Map<ConflictType, Integer> getTypeConflictCounts() { return typeConflictCounts; }
    public Map<ConflictPriority, Integer> getPriorityConflictCounts() { return priorityConflictCounts; }
    public Duration getTimeRange() { return timeRange; }
}
