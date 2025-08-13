package org.openhab.core.ai.agent.infrastructure.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audit log for agent security events
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuditLog {
    private final String agentId;
    private final List<AuditLogEntry> entries;

    public AuditLog(String agentId) {
        this.agentId = agentId;
        this.entries = new ArrayList<>();
    }

    // Getters
    public String getAgentId() {
        return agentId;
    }

    public List<AuditLogEntry> getEntries() {
        return entries;
    }

    public void addEntry(AuditLogEntry entry) {
        entries.add(entry);
    }
}
