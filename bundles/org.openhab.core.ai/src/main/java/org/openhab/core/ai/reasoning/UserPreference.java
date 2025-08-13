package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class UserPreference {
    private final String agentId;
    private final Map<String, Object> preferences = new ConcurrentHashMap<>();

    public UserPreference(String agentId) { this.agentId = agentId; }

    public void learnFromEvent(AutonomousEvent event) {
        String eventType = event.getType();
        preferences.put("last_" + eventType + "_timestamp", event.getTimestamp());
        preferences.put(eventType + "_count", ((Integer) preferences.getOrDefault(eventType + "_count", 0)) + 1);
    }

    public Map<String, Object> getPreferences() { return new ConcurrentHashMap<>(preferences); }
}
