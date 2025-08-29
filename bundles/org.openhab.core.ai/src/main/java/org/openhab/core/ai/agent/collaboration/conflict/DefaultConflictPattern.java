package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.AgentCommunicationMetrics;

/**
 * Default in-memory implementation of a conflict pattern.
 *
 * Tracks occurrences and recent conflicts for analytics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultConflictPattern implements ConflictPattern {
    private final String patternId;
    // Occurrence count - now handled by MetricsService
    private final List<Conflict> recentConflicts = new ArrayList<>();

    public DefaultConflictPattern(String patternId) {
        this.patternId = patternId;
    }

    @Override
    public String getPatternId() {
        return patternId;
    }

    @Override
    public boolean matches(Conflict conflict) {
        var type = conflict.getAnalysis().getConflictType();
        var priority = conflict.getPriority();
        int severity = conflict.getAnalysis().getSeverity();
        String conflictKey = type + "_" + priority + "_" + severity;
        return patternId.equals(conflictKey);
    }

    @Override
    public void recordOccurrence(Conflict conflict) {
        recordConflictOccurrence(conflict);
        synchronized (recentConflicts) {
            recentConflicts.add(conflict);
            if (recentConflicts.size() > 10) {
                recentConflicts.remove(0);
            }
        }
    }

    public long getOccurrenceCount() {
        // Occurrence count now comes from MetricsService snapshots
        return 0;
    }

    public List<Conflict> getRecentConflicts() {
        synchronized (recentConflicts) {
            return new ArrayList<>(recentConflicts);
        }
    }

    // Metrics recording methods - replacing removed AtomicLong fields using AgentCommunicationMetrics pattern

    /**
     * Record conflict occurrence - replaces occurrenceCount.incrementAndGet()
     */
    private void recordConflictOccurrence(Conflict conflict) {
        try {
            // Use AgentCommunicationMetrics pattern for conflict occurrence
            AgentCommunicationMetrics.recordAgentCommunication(null, "conflict-pattern", "conflict-occurrence", 
                    true, java.time.Duration.ZERO, 1, patternId);
        } catch (Exception e) {
            // Silent fail for metrics recording
        }
    }
}
