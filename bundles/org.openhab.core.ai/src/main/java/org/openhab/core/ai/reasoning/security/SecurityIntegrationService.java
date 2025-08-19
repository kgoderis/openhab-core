package org.openhab.core.ai.reasoning.security;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.openhab.core.ai.reasoning.constraints.SafetyConstraintManager;
import org.openhab.core.ai.reasoning.model.ModelRequest;
import org.openhab.core.ai.reasoning.policies.SafetyValidationResult;
import org.openhab.core.ai.reasoning.results.ComprehensiveSecurityResult;
import org.openhab.core.ai.reasoning.results.QuickSecurityResult;
import org.openhab.core.ai.reasoning.security.api.SecurityIssue;
import org.openhab.core.ai.reasoning.security.api.SecurityIssueType;
import org.openhab.core.ai.reasoning.security.api.SecurityLevel;
import org.openhab.core.ai.reasoning.security.api.SecurityValidationResult;
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
    public CompletableFuture<ComprehensiveSecurityResult> validateSecurity(ModelRequest request,
            AgentModelContext context, String actionType, Map<String, Object> actionParameters, String userId) {

        logger.debug("Performing comprehensive security validation for request: {}", request.getRequestId());

        return CompletableFuture.supplyAsync(() -> {
            ComprehensiveSecurityResult result = new ComprehensiveSecurityResult(request.getRequestId());

            try {
                // Step 1: Authentication and Authorization (AgentModelSecurityManager)
                SecurityValidationResult authResult = agentModelSecurityManager.validateSecurity(request, context)
                        .get();

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
                SafetyValidationResult safetyResult = safetyConstraintManager.validateAction(request.getAgentId(),
                        actionType, actionParameters, userId);

                if (!safetyResult.isValid()) {
                    result.setSafetyValid(false);
                    result.addSafetyIssue(new SafetyIssue("CONTENT_SAFETY_VIOLATION", safetyResult.getReason()));
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
                result.addSecurityIssue(new SecurityIssue(SecurityIssueType.SYSTEM_ERROR,
                        "Security validation error: " + e.getMessage(), SecurityLevel.HIGH));
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
                SafetyValidationResult safetyResult = safetyConstraintManager.validateAction(agentId, actionType,
                        Map.of(), userId);

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

    // Extracted: org.openhab.core.ai.reasoning.ComprehensiveSecurityResult
    // Extracted: org.openhab.core.ai.reasoning.QuickSecurityResult
}
