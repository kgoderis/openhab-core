package org.openhab.core.ai.reasoning.security.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SecurityIssue {
    private final SecurityIssueType type;
    private final String description;
    private final SecurityLevel severity;

    public SecurityIssue(SecurityIssueType type, String description, SecurityLevel severity) {
        this.type = type;
        this.description = description;
        this.severity = severity;
    }

    public SecurityIssueType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public SecurityLevel getSeverity() {
        return severity;
    }
}
