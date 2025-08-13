package org.openhab.core.ai.reasoning;

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
    private final List<SecurityIssue> securityIssues = new ArrayList<>();
    private final List<SafetyIssue> safetyIssues = new ArrayList<>();

    public ComprehensiveSecurityResult(String requestId) { this.requestId = requestId; }

    public String getRequestId() { return requestId; }
    public boolean isAuthenticationValid() { return authenticationValid; }
    public boolean isAuthorizationValid() { return authorizationValid; }
    public boolean isSafetyValid() { return safetyValid; }
    public boolean isOverallValid() { return overallValid; }
    public List<SecurityIssue> getSecurityIssues() { return new ArrayList<>(securityIssues); }
    public List<SafetyIssue> getSafetyIssues() { return new ArrayList<>(safetyIssues); }

    public void setAuthenticationValid(boolean valid) { this.authenticationValid = valid; }
    public void setAuthorizationValid(boolean valid) { this.authorizationValid = valid; }
    public void setSafetyValid(boolean valid) { this.safetyValid = valid; }
    public void setOverallValid(boolean valid) { this.overallValid = valid; }

    public void addSecurityIssues(List<org.openhab.core.ai.reasoning.SecurityIssue> issues) {
        for (org.openhab.core.ai.reasoning.SecurityIssue issue : issues) {
            securityIssues.add(new SecurityIssue(issue.getType().name(), issue.getDescription()));
        }
    }
    public void addSecurityIssue(String type, String description) { securityIssues.add(new SecurityIssue(type, description)); }
    public void addSafetyIssue(String type, String description) { safetyIssues.add(new SafetyIssue(type, description)); }

    @NonNullByDefault
    public static class SecurityIssue {
        private final String type;
        private final String description;
        public SecurityIssue(String type, String description) { this.type = type; this.description = description; }
        public String getType() { return type; }
        public String getDescription() { return description; }
    }

    @NonNullByDefault
    public static class SafetyIssue {
        private final String type;
        private final String description;
        public SafetyIssue(String type, String description) { this.type = type; this.description = description; }
        public String getType() { return type; }
        public String getDescription() { return description; }
    }
}
