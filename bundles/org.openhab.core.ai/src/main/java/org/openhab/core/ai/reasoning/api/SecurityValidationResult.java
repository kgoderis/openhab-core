package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SecurityValidationResult {
    private final String requestId;
    private final boolean valid;
    private final SecurityIssue[] issues;
    private final long validationTime;

    public SecurityValidationResult(String requestId, boolean valid, SecurityIssue[] issues, long validationTime) {
        this.requestId = requestId;
        this.valid = valid;
        this.issues = issues;
        this.validationTime = validationTime;
    }

    public String getRequestId() { return requestId; }
    public boolean isValid() { return valid; }
    public SecurityIssue[] getIssues() { return issues; }
    public long getValidationTime() { return validationTime; }
}


