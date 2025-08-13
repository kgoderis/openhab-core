package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of security validation.
 */
@NonNullByDefault
public class SecurityValidationResult {
    private final String requestId;
    private final List<SecurityIssue> securityIssues;
    private boolean authenticationValid;
    private boolean authorizationValid;
    private boolean contentSafetyValid;
    private boolean accessControlValid;
    private boolean rateLimitValid;

    public SecurityValidationResult(String requestId) {
        this.requestId = requestId;
        this.securityIssues = new ArrayList<>();
        this.authenticationValid = false;
        this.authorizationValid = false;
        this.contentSafetyValid = false;
        this.accessControlValid = false;
        this.rateLimitValid = false;
    }

    public String getRequestId() {
        return requestId;
    }

    public List<SecurityIssue> getSecurityIssues() {
        return new ArrayList<>(securityIssues);
    }

    public boolean isAuthenticationValid() {
        return authenticationValid;
    }

    public boolean isAuthorizationValid() {
        return authorizationValid;
    }

    public boolean isContentSafetyValid() {
        return contentSafetyValid;
    }

    public boolean isAccessControlValid() {
        return accessControlValid;
    }

    public boolean isRateLimitValid() {
        return rateLimitValid;
    }

    public void setAuthenticationValid(boolean valid) {
        this.authenticationValid = valid;
    }

    public void setAuthorizationValid(boolean valid) {
        this.authorizationValid = valid;
    }

    public void setContentSafetyValid(boolean valid) {
        this.contentSafetyValid = valid;
    }

    public void setAccessControlValid(boolean valid) {
        this.accessControlValid = valid;
    }

    public void setRateLimitValid(boolean valid) {
        this.rateLimitValid = valid;
    }

    public void addSecurityIssue(SecurityIssueType issueType, String description) {
        securityIssues.add(new SecurityIssue(issueType, description));
    }

    public boolean isValid() {
        return authenticationValid && authorizationValid && contentSafetyValid && accessControlValid && rateLimitValid
                && securityIssues.isEmpty();
    }

    public boolean hasIssues() {
        return !securityIssues.isEmpty();
    }
}


