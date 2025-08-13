package org.openhab.core.ai.agents;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionResult;

/**
 * Learning example record capturing action execution details for agent learning.
 *
 * <p>Contains action name, parameters, result, success flag, context, and timestamp.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class LearningExample {
    private final String actionName;
    private final Map<String, Object> parameters;
    private final ActionResult result;
    private final boolean success;
    private final Map<String, Object> context;
    private final Instant timestamp;

    public LearningExample(String actionName, Map<String, Object> parameters, ActionResult result, boolean success,
            Map<String, Object> context, Instant timestamp) {
        this.actionName = actionName;
        this.parameters = new HashMap<>(parameters);
        this.result = result;
        this.success = success;
        this.context = new HashMap<>(context);
        this.timestamp = timestamp;
    }

    public String getActionName() {
        return actionName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public ActionResult getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}


