package org.openhab.core.ai.reasoning.results;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Comprehensive security validation result.
 *
 * Aggregates authentication, authorization and safety checks with issue lists.
 */
@NonNullByDefault
public class ComprehensiveSecurityResult {
    private final String requestId;
    private boolean authenticationValid;
    private boolean authorizationValid;
    private boolean safetyValid;
    private boolean overallValid;
    private final List<org.openhab.core.ai.reasoning.security.SecurityIssue> securityIssues = new ArrayList<>();
    private final List<org.openhab.core.ai.reasoning.security.SafetyIssue> safetyIssues = new ArrayList<>();

    public ComprehensiveSecurityResult(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isAuthenticationValid() {
        return authenticationValid;
    }

    public boolean isAuthorizationValid() {
        return authorizationValid;
    }

    public boolean isSafetyValid() {
        return safetyValid;
    }

    public boolean isOverallValid() {
        return overallValid;
    }

    public List<org.openhab.core.ai.reasoning.security.SecurityIssue> getSecurityIssues() {
        return new ArrayList<>(securityIssues);
    }

    public List<org.openhab.core.ai.reasoning.security.SafetyIssue> getSafetyIssues() {
        return new ArrayList<>(safetyIssues);
    }

    public void setAuthenticationValid(boolean valid) {
        this.authenticationValid = valid;
    }

    public void setAuthorizationValid(boolean valid) {
        this.authorizationValid = valid;
    }

    public void setSafetyValid(boolean valid) {
        this.safetyValid = valid;
    }

    public void setOverallValid(boolean valid) {
        this.overallValid = valid;
    }

    public void addSecurityIssues(List<org.openhab.core.ai.reasoning.security.SecurityIssue> issues) {
        securityIssues.addAll(issues);
    }

    public void addSecurityIssue(org.openhab.core.ai.reasoning.security.SecurityIssue issue) {
        securityIssues.add(issue);
    }

    public void addSafetyIssue(org.openhab.core.ai.reasoning.security.SafetyIssue issue) {
        safetyIssues.add(issue);
    }
}
