package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

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
    private final AtomicLong occurrenceCount = new AtomicLong(0);
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
        occurrenceCount.incrementAndGet();
        synchronized (recentConflicts) {
            recentConflicts.add(conflict);
            if (recentConflicts.size() > 10) {
                recentConflicts.remove(0);
            }
        }
    }

    public long getOccurrenceCount() {
        return occurrenceCount.get();
    }

    public List<Conflict> getRecentConflicts() {
        synchronized (recentConflicts) {
            return new ArrayList<>(recentConflicts);
        }
    }
}
