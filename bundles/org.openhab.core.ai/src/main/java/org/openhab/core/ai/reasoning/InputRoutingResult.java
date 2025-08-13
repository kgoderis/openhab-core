package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result for routing a reasoning input to a target agent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputRoutingResult {
    private final boolean success;
    private final @Nullable String targetAgent;
    private final @Nullable String inputId;
    private final @Nullable String error;

    private InputRoutingResult(boolean success, @Nullable String targetAgent, @Nullable String inputId,
            @Nullable String error) {
        this.success = success;
        this.targetAgent = targetAgent;
        this.inputId = inputId;
        this.error = error;
    }

    public static InputRoutingResult success(String targetAgent, String inputId) {
        return new InputRoutingResult(true, targetAgent, inputId, null);
    }

    public static InputRoutingResult noTarget(String reason) {
        return new InputRoutingResult(false, null, null, reason);
    }

    public static InputRoutingResult routerNotFound(String reason) {
        return new InputRoutingResult(false, null, null, reason);
    }

    public static InputRoutingResult routingFailed(String reason) {
        return new InputRoutingResult(false, null, null, reason);
    }

    public static InputRoutingResult error(String reason) {
        return new InputRoutingResult(false, null, null, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable String getTargetAgent() {
        return targetAgent;
    }

    public @Nullable String getInputId() {
        return inputId;
    }

    public @Nullable String getError() {
        return error;
    }
}


