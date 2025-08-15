package org.openhab.core.ai.reasoning.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security issue instance with type and description.
 */
@NonNullByDefault
public class SecurityIssue {
    private final SecurityIssueType type;
    private final String description;

    public SecurityIssue(SecurityIssueType type, String description) {
        this.type = type;
        this.description = description;
    }

    public SecurityIssueType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
