package org.openhab.core.ai.reasoning.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.actions.AutonomousAction;

/**
 * Result of autonomous event processing.
 *
 * Indicates whether processing succeeded and includes any generated actions or a message.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventProcessingResult {

    private final boolean success;
    private final String message;
    private final List<org.openhab.core.ai.reasoning.actions.AutonomousAction> actions;

    private EventProcessingResult(boolean success, String message, List<AutonomousAction> actions) {
        this.success = success;
        this.message = message;
        this.actions = new ArrayList<>(actions);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<AutonomousAction> getActions() {
        return new ArrayList<>(actions);
    }

    public static EventProcessingResult success(List<AutonomousAction> actions) {
        return new EventProcessingResult(true, "SUCCESS", actions);
    }

    public static EventProcessingResult noActions(String message) {
        return new EventProcessingResult(true, message, List.of());
    }

    public static EventProcessingResult disabled(String message) {
        return new EventProcessingResult(false, message, List.of());
    }
}
