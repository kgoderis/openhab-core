package org.openhab.core.ai.common.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified security validation result for openHAB AI components.
 * 
 * This class provides comprehensive security validation results including
 * authentication, authorization, access control, content safety, and rate limiting.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityValidationResult {
    private final String requestId;
    private final List<SecurityIssue> securityIssues;
    private boolean authenticationValid;
    private boolean authorizationValid;
    private boolean accessControlValid;
    private boolean contentSafetyValid;
    private boolean rateLimitValid;
    private final long validationTime;

    /**
     * Create a new SecurityValidationResult for the given request.
     * 
     * @param requestId the request identifier
     */
    public SecurityValidationResult(String requestId) {
        this.requestId = requestId;
        this.securityIssues = new ArrayList<>();
        this.authenticationValid = true;
        this.authorizationValid = true;
        this.accessControlValid = true;
        this.contentSafetyValid = true;
        this.rateLimitValid = true;
        this.validationTime = System.currentTimeMillis();
    }

    /**
     * Create a new SecurityValidationResult with specific validation results.
     * 
     * @param requestId the request identifier
     * @param valid overall validation result
     * @param issues security issues found
     * @param validationTime timestamp of validation
     */
    public SecurityValidationResult(String requestId, boolean valid, SecurityIssue[] issues, long validationTime) {
        this.requestId = requestId;
        this.securityIssues = new ArrayList<>();
        if (issues != null) {
            for (SecurityIssue issue : issues) {
                this.securityIssues.add(issue);
            }
        }
        this.validationTime = validationTime;

        // Set validation flags based on overall validity
        this.authenticationValid = valid;
        this.authorizationValid = valid;
        this.accessControlValid = valid;
        this.contentSafetyValid = valid;
        this.rateLimitValid = valid;
    }

    /**
     * Get the request identifier.
     * 
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Check if the overall validation is valid.
     * 
     * @return true if all validations pass and no issues exist
     */
    public boolean isValid() {
        return authenticationValid && authorizationValid && accessControlValid && contentSafetyValid && rateLimitValid
                && securityIssues.isEmpty();
    }

    /**
     * Get all security issues found during validation.
     * 
     * @return array of security issues
     */
    public SecurityIssue[] getIssues() {
        return securityIssues.toArray(new SecurityIssue[0]);
    }

    /**
     * Get all security issues as a list.
     * 
     * @return list of security issues
     */
    public List<SecurityIssue> getSecurityIssues() {
        return new ArrayList<>(securityIssues);
    }

    /**
     * Get the validation timestamp.
     * 
     * @return validation time in milliseconds
     */
    public long getValidationTime() {
        return validationTime;
    }

    /**
     * Add a security issue to the result.
     * 
     * @param type the issue type
     * @param description the issue description
     */
    public void addSecurityIssue(SecurityIssueType type, String description) {
        securityIssues.add(new SecurityIssue(type, description, SecurityLevel.MEDIUM));
    }

    /**
     * Check if there are any security issues.
     * 
     * @return true if issues exist
     */
    public boolean hasIssues() {
        return !securityIssues.isEmpty();
    }

    /**
     * Check if authentication is valid.
     * 
     * @return true if authentication passed
     */
    public boolean isAuthenticationValid() {
        return authenticationValid;
    }

    /**
     * Set authentication validation result.
     * 
     * @param authenticationValid authentication result
     */
    public void setAuthenticationValid(boolean authenticationValid) {
        this.authenticationValid = authenticationValid;
    }

    /**
     * Check if authorization is valid.
     * 
     * @return true if authorization passed
     */
    public boolean isAuthorizationValid() {
        return authorizationValid;
    }

    /**
     * Set authorization validation result.
     * 
     * @param authorizationValid authorization result
     */
    public void setAuthorizationValid(boolean authorizationValid) {
        this.authorizationValid = authorizationValid;
    }

    /**
     * Check if access control is valid.
     * 
     * @return true if access control passed
     */
    public boolean isAccessControlValid() {
        return accessControlValid;
    }

    /**
     * Set access control validation result.
     * 
     * @param accessControlValid access control result
     */
    public void setAccessControlValid(boolean accessControlValid) {
        this.accessControlValid = accessControlValid;
    }

    /**
     * Check if content safety is valid.
     * 
     * @return true if content safety passed
     */
    public boolean isContentSafetyValid() {
        return contentSafetyValid;
    }

    /**
     * Set content safety validation result.
     * 
     * @param contentSafetyValid content safety result
     */
    public void setContentSafetyValid(boolean contentSafetyValid) {
        this.contentSafetyValid = contentSafetyValid;
    }

    /**
     * Check if rate limiting is valid.
     * 
     * @return true if rate limiting passed
     */
    public boolean isRateLimitValid() {
        return rateLimitValid;
    }

    /**
     * Set rate limiting validation result.
     * 
     * @param rateLimitValid rate limiting result
     */
    public void setRateLimitValid(boolean rateLimitValid) {
        this.rateLimitValid = rateLimitValid;
    }
}
