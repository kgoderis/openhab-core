package org.openhab.core.ai.agent.infrastructure.synchronization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.TaskState;

/**
 * Aggregated task execution statistics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class TaskExecutionStats {
    private final int totalTasks;
    private final Map<TaskState, Integer> stateCounts;
    private final int activeLocks;
    private final int lockedResources;

    public TaskExecutionStats(int totalTasks, Map<TaskState, Integer> stateCounts, int activeLocks,
            int lockedResources) {
        this.totalTasks = totalTasks;
        this.stateCounts = new HashMap<>(stateCounts);
        this.activeLocks = activeLocks;
        this.lockedResources = lockedResources;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public Map<TaskState, Integer> getStateCounts() {
        return Collections.unmodifiableMap(stateCounts);
    }

    public int getActiveLocks() {
        return activeLocks;
    }

    public int getLockedResources() {
        return lockedResources;
    }
}
