package org.openhab.core.ai.action;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ActionSecurityPolicy}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionSecurityPolicyBuilder {
    String actionId = "";
    Set<String> requiredPermissions = Set.of();
    Set<String> allowedAgents = Set.of();
    Set<String> allowedRoles = Set.of();
    boolean requiresAuthentication = true;
    boolean requiresAuthorization = true;
    boolean requiresAuditLogging = false;
    ActionSecurityPolicy.SecurityLevel securityLevel = ActionSecurityPolicy.SecurityLevel.MEDIUM;
    Set<String> restrictedParameters = Set.of();
    Set<String> allowedOrigins = Set.of();

    public ActionSecurityPolicyBuilder actionId(String actionId) { this.actionId = actionId; return this; }
    public ActionSecurityPolicyBuilder requiredPermissions(Set<String> v) { this.requiredPermissions = v; return this; }
    public ActionSecurityPolicyBuilder allowedAgents(Set<String> v) { this.allowedAgents = v; return this; }
    public ActionSecurityPolicyBuilder allowedRoles(Set<String> v) { this.allowedRoles = v; return this; }
    public ActionSecurityPolicyBuilder requiresAuthentication(boolean v) { this.requiresAuthentication = v; return this; }
    public ActionSecurityPolicyBuilder requiresAuthorization(boolean v) { this.requiresAuthorization = v; return this; }
    public ActionSecurityPolicyBuilder requiresAuditLogging(boolean v) { this.requiresAuditLogging = v; return this; }
    public ActionSecurityPolicyBuilder securityLevel(ActionSecurityPolicy.SecurityLevel v) { this.securityLevel = v; return this; }
    public ActionSecurityPolicyBuilder restrictedParameters(Set<String> v) { this.restrictedParameters = v; return this; }
    public ActionSecurityPolicyBuilder allowedOrigins(Set<String> v) { this.allowedOrigins = v; return this; }

    public ActionSecurityPolicy build() { return new ActionSecurityPolicy(this); }
}


