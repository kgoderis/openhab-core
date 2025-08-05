package org.openhab.core.ai.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Action Security Validator - Defines the contract for basic action security validation
 * 
 * <p>
 * This validator provides:
 * - Basic action execution validation
 * - Service availability checking
 * - Security policy version tracking
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ActionSecurityValidator {

    /**
     * Validate if an action can be executed based on security policies
     * 
     * @param actionContext the action context to validate
     * @return true if the action is allowed, false otherwise
     */
    boolean validateAction(ActionContext actionContext);

    /**
     * Get the validator status
     * 
     * @return true if the validator is available and ready
     */
    boolean isAvailable();

    /**
     * Get the security policy version
     * 
     * @return the current security policy version
     */
    String getPolicyVersion();
}
