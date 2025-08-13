package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Per-agent conflict history.
 *
 * Stores historical conflicts for an agent for analytics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictHistory {
    private final String agentId;
    private final List<Conflict> conflicts;

    public ConflictHistory(String agentId) {
        this.agentId = agentId;
        this.conflicts = new ArrayList<>();
    }

    public String getAgentId() { return agentId; }
    public List<Conflict> getConflicts() { return conflicts; }
    public void addConflict(Conflict conflict) { conflicts.add(conflict); }
}
