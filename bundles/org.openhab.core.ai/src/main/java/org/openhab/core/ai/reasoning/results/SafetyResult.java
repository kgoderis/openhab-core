package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SafetyResult {
    private final boolean success;
    private final String message;
    private final SafetyPolicyConfig policy;

    private SafetyResult(boolean success, String message, SafetyPolicyConfig policy) {
        this.success = success;
        this.message = message;
        this.policy = policy;
    }

    public static SafetyResult success(SafetyPolicyConfig policy) {
        return new SafetyResult(true, "Safety policy configuration successful", policy);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public SafetyPolicyConfig getPolicy() {
        return policy;
    }
}
