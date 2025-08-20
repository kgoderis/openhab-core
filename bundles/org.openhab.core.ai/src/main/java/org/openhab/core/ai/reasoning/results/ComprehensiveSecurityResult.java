package org.openhab.core.ai.reasoning.results;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityIssue;
import org.openhab.core.ai.reasoning.security.SafetyIssue;

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
    private final List<SecurityIssue> securityIssues = new ArrayList<>();
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

    public List<SecurityIssue> getSecurityIssues() {
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

    public void addSecurityIssues(List<SecurityIssue> issues) {
        securityIssues.addAll(issues);
    }

    public void addSecurityIssue(SecurityIssue issue) {
        securityIssues.add(issue);
    }

    public void addSafetyIssue(SafetyIssue issue) {
        safetyIssues.add(issue);
    }
}
