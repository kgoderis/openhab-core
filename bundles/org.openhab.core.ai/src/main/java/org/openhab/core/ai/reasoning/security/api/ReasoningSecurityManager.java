package org.openhab.core.ai.reasoning.security.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.QuickSecurityResult;
import org.openhab.core.ai.common.security.SecurityContext;
import org.openhab.core.ai.common.security.SecurityManager;
import org.openhab.core.ai.common.security.SecurityRequest;
import org.openhab.core.ai.common.security.SecurityValidationResult;

/**
 * Domain-specific Security Manager Interface for Reasoning
 * 
 * This interface extends the common SecurityManager interface and provides
 * reasoning-specific security operations for model access, content validation,
 * and reasoning-specific security policies.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ReasoningSecurityManager extends SecurityManager {

    /**
     * Validate security for a reasoning request.
     * 
     * @param request The security request
     * @return A CompletableFuture containing the security validation result
     */
    CompletableFuture<SecurityValidationResult> validateSecurity(SecurityRequest request);

    /**
     * Perform a quick security check for reasoning operations.
     * 
     * @param agentId The agent identifier
     * @param action The action to validate
     * @return A CompletableFuture containing the quick security result
     */
    CompletableFuture<QuickSecurityResult> quickSecurityCheck(String agentId, String action);

    /**
     * Validate model access for reasoning operations.
     * 
     * @param agentId The agent identifier
     * @param modelId The model identifier
     * @return A CompletableFuture containing the validation result
     */
    CompletableFuture<SecurityValidationResult> validateModelAccess(String agentId, String modelId);

    /**
     * Validate content safety for reasoning operations.
     * 
     * @param content The content to validate
     * @param context The security context
     * @return A CompletableFuture containing the validation result
     */
    CompletableFuture<SecurityValidationResult> validateContentSafety(String content, SecurityContext context);
}
