package org.openhab.core.ai.reasoning.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SafetyIssue {
    private final String type;
    private final String description;

    public SafetyIssue(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
