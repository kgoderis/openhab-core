package org.openhab.core.ai.reasoning.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.actions.AutonomousAction;

/**
 * Safety constraint model for autonomous actions extracted from {@link AutonomousEventProcessor}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyConstraint {
    private final String agentId;
    private final List<String> forbiddenActions = new ArrayList<>();
    private final Map<String, Object> constraints = new ConcurrentHashMap<>();

    public SafetyConstraint(String agentId) {
        this.agentId = agentId;
    }

    public void addForbiddenAction(String actionType) {
        forbiddenActions.add(actionType);
    }

    public void addConstraint(String key, Object value) {
        constraints.put(key, value);
    }

    public boolean validateAction(AutonomousAction action) {
        if (forbiddenActions.contains(action.getType())) {
            return false;
        }
        if (action.getConfidence() < 0.5) {
            return false;
        }
        return true;
    }
}
