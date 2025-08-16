package org.openhab.core.ai.reasoning.security.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security validation result for AI model requests
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityValidationResult {
    private final String requestId;
    private boolean valid;
    private final List<SecurityIssue> issues;
    private final long validationTime;
    private boolean authenticationValid = true;
    private boolean authorizationValid = true;
    private boolean accessControlValid = true;
    private boolean contentSafetyValid = true;
    private boolean rateLimitValid = true;

    public SecurityValidationResult(String requestId) {
        this.requestId = requestId;
        this.valid = true;
        this.issues = new ArrayList<>();
        this.validationTime = System.currentTimeMillis();
    }

    public SecurityValidationResult(String requestId, boolean valid, SecurityIssue[] issues, long validationTime) {
        this.requestId = requestId;
        this.valid = valid;
        this.issues = new ArrayList<>();
        if (issues != null) {
            for (SecurityIssue issue : issues) {
                this.issues.add(issue);
            }
        }
        this.validationTime = validationTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isValid() {
        return valid;
    }

    public SecurityIssue[] getIssues() {
        return issues.toArray(new SecurityIssue[0]);
    }

    public List<SecurityIssue> getSecurityIssues() {
        return new ArrayList<>(issues);
    }

    public long getValidationTime() {
        return validationTime;
    }

    public void addSecurityIssue(SecurityIssueType type, String description) {
        issues.add(new SecurityIssue(type, description, SecurityLevel.MEDIUM));
        valid = false;
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public boolean isAuthenticationValid() {
        return authenticationValid;
    }

    public void setAuthenticationValid(boolean authenticationValid) {
        this.authenticationValid = authenticationValid;
        if (!authenticationValid) {
            valid = false;
        }
    }

    public boolean isAuthorizationValid() {
        return authorizationValid;
    }

    public void setAuthorizationValid(boolean authorizationValid) {
        this.authorizationValid = authorizationValid;
        if (!authorizationValid) {
            valid = false;
        }
    }

    public boolean isAccessControlValid() {
        return accessControlValid;
    }

    public void setAccessControlValid(boolean accessControlValid) {
        this.accessControlValid = accessControlValid;
        if (!accessControlValid) {
            valid = false;
        }
    }

    public boolean isContentSafetyValid() {
        return contentSafetyValid;
    }

    public void setContentSafetyValid(boolean contentSafetyValid) {
        this.contentSafetyValid = contentSafetyValid;
        if (!contentSafetyValid) {
            valid = false;
        }
    }

    public boolean isRateLimitValid() {
        return rateLimitValid;
    }

    public void setRateLimitValid(boolean rateLimitValid) {
        this.rateLimitValid = rateLimitValid;
        if (!rateLimitValid) {
            valid = false;
        }
    }
}
