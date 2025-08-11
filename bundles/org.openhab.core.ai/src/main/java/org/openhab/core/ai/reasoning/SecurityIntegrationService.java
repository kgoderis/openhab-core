package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Integration Service - Coordinates authentication, authorization, and safety validation.
 * 
 * This service provides a unified security interface that coordinates between:
 * - AgentModelSecurityManager (authentication, authorization, model access)
 * - SafetyConstraintManager (action safety, user constraints)
 * 
 * Provides a single point of entry for comprehensive security validation.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = SecurityIntegrationService.class)
@NonNullByDefault
public class SecurityIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityIntegrationService.class);

    @Reference
    private AgentModelSecurityManager agentModelSecurityManager;

    @Reference
    private SafetyConstraintManager safetyConstraintManager;

    /**
     * Comprehensive security validation for AI model requests.
     * 
     * @param request The model request
     * @param context The agent context
     * @param actionType The action type for safety validation
     * @param actionParameters The action parameters for safety validation
     * @param userId The user ID for safety validation
     * @return A CompletableFuture containing the comprehensive security validation result
     */
    public CompletableFuture<ComprehensiveSecurityResult> validateSecurity(
            AgentModelSecurityManager.ModelRequest request, AgentModelContextBuilder.AgentModelContext context,
            String actionType, Map<String, Object> actionParameters, String userId) {

        logger.debug("Performing comprehensive security validation for request: {}", request.getRequestId());

        return CompletableFuture.supplyAsync(() -> {
            ComprehensiveSecurityResult result = new ComprehensiveSecurityResult(request.getRequestId());

            try {
                // Step 1: Authentication and Authorization (AgentModelSecurityManager)
                AgentModelSecurityManager.SecurityValidationResult authResult = agentModelSecurityManager
                        .validateSecurity(request, context).get();

                if (!authResult.isValid()) {
                    result.setAuthenticationValid(false);
                    result.setAuthorizationValid(false);
                    result.addSecurityIssues(authResult.getSecurityIssues());
                    result.setOverallValid(false);
                    logger.warn("Authentication/Authorization failed for request: {}", request.getRequestId());
                    return result;
                }

                result.setAuthenticationValid(authResult.isAuthenticationValid());
                result.setAuthorizationValid(authResult.isAuthorizationValid());
                result.addSecurityIssues(authResult.getSecurityIssues());

                // Step 2: Action Safety Validation (SafetyConstraintManager)
                SafetyConstraintManager.SafetyValidationResult safetyResult = safetyConstraintManager
                        .validateAction(request.getAgentId(), actionType, actionParameters, userId);

                if (!safetyResult.isValid()) {
                    result.setSafetyValid(false);
                    result.addSafetyIssue("SAFETY_VIOLATION", safetyResult.getReason());
                    result.setOverallValid(false);
                    logger.warn("Safety validation failed for request: {}", request.getRequestId());
                    return result;
                }

                result.setSafetyValid(true);

                // All validations passed
                result.setOverallValid(true);
                logger.debug("Comprehensive security validation passed for request: {}", request.getRequestId());

            } catch (Exception e) {
                logger.error("Error during comprehensive security validation: {}", e.getMessage(), e);
                result.setOverallValid(false);
                result.addSecurityIssue("SYSTEM_ERROR", "Security validation error: " + e.getMessage());
            }

            return result;
        });
    }

    /**
     * Quick security check for low-risk operations.
     * 
     * @param agentId The agent ID
     * @param actionType The action type
     * @param userId The user ID
     * @return A CompletableFuture containing a simplified security result
     */
    public CompletableFuture<QuickSecurityResult> quickSecurityCheck(String agentId, String actionType, String userId) {
        logger.debug("Performing quick security check for agent: {} action: {}", agentId, actionType);

        return CompletableFuture.supplyAsync(() -> {
            QuickSecurityResult result = new QuickSecurityResult(agentId, actionType);

            try {
                // Only perform safety validation for quick checks
                SafetyConstraintManager.SafetyValidationResult safetyResult = safetyConstraintManager
                        .validateAction(agentId, actionType, Map.of(), userId);

                result.setValid(safetyResult.isValid());
                if (!safetyResult.isValid()) {
                    result.setReason(safetyResult.getReason());
                }

            } catch (Exception e) {
                logger.error("Error during quick security check: {}", e.getMessage(), e);
                result.setValid(false);
                result.setReason("Security check error: " + e.getMessage());
            }

            return result;
        });
    }

    /**
     * Comprehensive security validation result.
     */
    public static class ComprehensiveSecurityResult {
        private final String requestId;
        private boolean authenticationValid;
        private boolean authorizationValid;
        private boolean safetyValid;
        private boolean overallValid;
        private final java.util.List<SecurityIssue> securityIssues = new java.util.ArrayList<>();
        private final java.util.List<SafetyIssue> safetyIssues = new java.util.ArrayList<>();

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

        public java.util.List<SecurityIssue> getSecurityIssues() {
            return new java.util.ArrayList<>(securityIssues);
        }

        public java.util.List<SafetyIssue> getSafetyIssues() {
            return new java.util.ArrayList<>(safetyIssues);
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

        public void addSecurityIssues(java.util.List<AgentModelSecurityManager.SecurityIssue> issues) {
            for (AgentModelSecurityManager.SecurityIssue issue : issues) {
                securityIssues.add(new SecurityIssue(issue.getType().name(), issue.getDescription()));
            }
        }

        public void addSecurityIssue(String type, String description) {
            securityIssues.add(new SecurityIssue(type, description));
        }

        public void addSafetyIssue(String type, String description) {
            safetyIssues.add(new SafetyIssue(type, description));
        }

        public static class SecurityIssue {
            private final String type;
            private final String description;

            public SecurityIssue(String type, String description) {
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

        public static class SafetyIssue {
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
    }

    /**
     * Quick security check result.
     */
    public static class QuickSecurityResult {
        private final String agentId;
        private final String actionType;
        private boolean valid;
        private String reason;

        public QuickSecurityResult(String agentId, String actionType) {
            this.agentId = agentId;
            this.actionType = actionType;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getActionType() {
            return actionType;
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
