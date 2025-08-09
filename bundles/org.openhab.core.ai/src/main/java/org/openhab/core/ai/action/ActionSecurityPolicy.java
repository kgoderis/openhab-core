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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security policy for an action.
 * 
 * This class defines security policies including required permissions,
 * access control rules, and validation requirements for actions.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionSecurityPolicy {

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

    private ActionSecurityPolicy(Builder builder) {
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

    /**
     * Security levels for actions.
     */
    public enum SecurityLevel {
        LOW, // Basic validation only
        MEDIUM, // Standard security checks
        HIGH, // Enhanced security validation
        CRITICAL // Maximum security validation
    }

    /**
     * Builder for ActionSecurityPolicy.
     */
    public static class Builder {
        private String actionId = "";
        private Set<String> requiredPermissions = Set.of();
        private Set<String> allowedAgents = Set.of();
        private Set<String> allowedRoles = Set.of();
        private boolean requiresAuthentication = true;
        private boolean requiresAuthorization = true;
        private boolean requiresAuditLogging = false;
        private SecurityLevel securityLevel = SecurityLevel.MEDIUM;
        private Set<String> restrictedParameters = Set.of();
        private Set<String> allowedOrigins = Set.of();

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder requiredPermissions(Set<String> requiredPermissions) {
            this.requiredPermissions = requiredPermissions;
            return this;
        }

        public Builder allowedAgents(Set<String> allowedAgents) {
            this.allowedAgents = allowedAgents;
            return this;
        }

        public Builder allowedRoles(Set<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
            return this;
        }

        public Builder requiresAuthentication(boolean requiresAuthentication) {
            this.requiresAuthentication = requiresAuthentication;
            return this;
        }

        public Builder requiresAuthorization(boolean requiresAuthorization) {
            this.requiresAuthorization = requiresAuthorization;
            return this;
        }

        public Builder requiresAuditLogging(boolean requiresAuditLogging) {
            this.requiresAuditLogging = requiresAuditLogging;
            return this;
        }

        public Builder securityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public Builder restrictedParameters(Set<String> restrictedParameters) {
            this.restrictedParameters = restrictedParameters;
            return this;
        }

        public Builder allowedOrigins(Set<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
            return this;
        }

        public ActionSecurityPolicy build() {
            return new ActionSecurityPolicy(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ActionSecurityPolicy{actionId='%s', securityLevel=%s, requiresAuth=%s}", actionId,
                securityLevel, requiresAuthentication);
    }
}
