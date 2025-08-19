package org.openhab.core.ai.reasoning.policies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.events.AutonomousEvent;

@NonNullByDefault
public class UserPreference {
    // TODO: Move AutonomousEvent to org.openhab.core.ai.reasoning.events and adjust imports
    private final String agentId;
    private final Map<String, Object> preferences = new ConcurrentHashMap<>();

    public UserPreference(String agentId) {
        this.agentId = agentId;
    }

    public void learnFromEvent(AutonomousEvent event) {
        String eventType = event.getType();
        preferences.put("last_" + eventType + "_timestamp", event.getTimestamp());
        preferences.put(eventType + "_count", ((Integer) preferences.getOrDefault(eventType + "_count", 0)) + 1);
    }

    public Map<String, Object> getPreferences() {
        return new ConcurrentHashMap<>(preferences);
    }
}
