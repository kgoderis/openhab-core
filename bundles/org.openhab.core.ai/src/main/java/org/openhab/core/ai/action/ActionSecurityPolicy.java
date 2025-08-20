/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityLevel;
import org.openhab.core.ai.common.security.SecurityPolicy;
import org.openhab.core.ai.common.security.SecurityPolicyType;

/**
 * Security policy for an action.
 * 
 * This class defines security policies including required permissions,
 * access control rules, and validation requirements for actions.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionSecurityPolicy implements SecurityPolicy {

    private final String actionId;
    private final Set<String> requiredPermissions;
    private final Set<String> allowedAgents;
    private final Set<String> allowedRoles;
    private final boolean requiresAuthentication;
    private final boolean requiresAuthorization;
    private final boolean requiresAuditLogging;
    private final SecurityLevel securityLevel;
    private final Set<String> restrictedParameters;
    private final Set<String> allowedOrigins;

    /* package */ ActionSecurityPolicy(ActionSecurityPolicyBuilder builder) {
        this.actionId = builder.actionId;
        this.requiredPermissions = builder.requiredPermissions;
        this.allowedAgents = builder.allowedAgents;
        this.allowedRoles = builder.allowedRoles;
        this.requiresAuthentication = builder.requiresAuthentication;
        this.requiresAuthorization = builder.requiresAuthorization;
        this.requiresAuditLogging = builder.requiresAuditLogging;
        this.securityLevel = builder.securityLevel;
        this.restrictedParameters = builder.restrictedParameters;
        this.allowedOrigins = builder.allowedOrigins;
    }

    // Getters
    public String getActionId() {
        return actionId;
    }

    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public Set<String> getAllowedAgents() {
        return allowedAgents;
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public boolean isRequiresAuthorization() {
        return requiresAuthorization;
    }

    public boolean isRequiresAuditLogging() {
        return requiresAuditLogging;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public Set<String> getRestrictedParameters() {
        return restrictedParameters;
    }

    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Check if an agent is allowed to execute this action.
     * 
     * @param agentId the agent ID to check
     * @return true if the agent is allowed, false otherwise
     */
    public boolean isAgentAllowed(String agentId) {
        return allowedAgents.isEmpty() || allowedAgents.contains(agentId);
    }

    /**
     * Check if a role is allowed to execute this action.
     * 
     * @param role the role to check
     * @return true if the role is allowed, false otherwise
     */
    public boolean isRoleAllowed(String role) {
        return allowedRoles.isEmpty() || allowedRoles.contains(role);
    }

    /**
     * Check if a parameter is restricted.
     * 
     * @param parameterName the parameter name to check
     * @return true if the parameter is restricted, false otherwise
     */
    public boolean isParameterRestricted(String parameterName) {
        return restrictedParameters.contains(parameterName);
    }

    /**
     * Check if an origin is allowed.
     * 
     * @param origin the origin to check
     * @return true if the origin is allowed, false otherwise
     */
    public boolean isOriginAllowed(String origin) {
        return allowedOrigins.isEmpty() || allowedOrigins.contains(origin);
    }

    // Implementation of SecurityPolicy interface methods
    @Override
    public String getPolicyId() {
        return actionId;
    }

    @Override
    public String getPolicyName() {
        return "Action Security Policy for " + actionId;
    }

    @Override
    public boolean isEnabled() {
        return true; // Action policies are always enabled
    }

    @Override
    public SecurityPolicyType getPolicyType() {
        return SecurityPolicyType.ACTION_SECURITY;
    }

    @Override
    public boolean validateContext(org.openhab.core.ai.common.security.SecurityContext context) {
        // Check if the agent is allowed
        if (!isAgentAllowed(context.getPrincipalId())) {
            return false;
        }

        // Check if the action matches
        if (!actionId.equals(context.getAction())) {
            return false;
        }

        // Check if the resource is allowed (if specified)
        if (context.getResourceId() != null && !context.getResourceId().isEmpty()) {
            // Additional resource validation could be added here
        }

        return true;
    }

    @Override
    public Map<String, Object> getPolicyMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionId", actionId);
        metadata.put("requiredPermissions", requiredPermissions);
        metadata.put("allowedAgents", allowedAgents);
        metadata.put("allowedRoles", allowedRoles);
        metadata.put("requiresAuthentication", requiresAuthentication);
        metadata.put("requiresAuthorization", requiresAuthorization);
        metadata.put("requiresAuditLogging", requiresAuditLogging);
        metadata.put("securityLevel", securityLevel);
        metadata.put("restrictedParameters", restrictedParameters);
        metadata.put("allowedOrigins", allowedOrigins);
        metadata.put("policyType", getPolicyType());
        return metadata;
    }

    public static ActionSecurityPolicyBuilder builder() {
        return new ActionSecurityPolicyBuilder();
    }

    @Override
    public String toString() {
        return String.format("ActionSecurityPolicy{actionId='%s', securityLevel=%s, requiresAuth=%s}", actionId,
                securityLevel, requiresAuthentication);
    }
}
