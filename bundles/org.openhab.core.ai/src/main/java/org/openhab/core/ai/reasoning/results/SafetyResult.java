package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SafetyResult {
    private final boolean success;
    private final String message;
    private final org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig policy;

    private SafetyResult(boolean success, String message,
            org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig policy) {
        this.success = success;
        this.message = message;
        this.policy = policy;
    }

    public static SafetyResult success(org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig policy) {
        return new SafetyResult(true, "Safety policy configuration successful", policy);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig getPolicy() {
        return policy;
    }
}
