package org.openhab.core.ai.reasoning;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AutonomousEvent extends Event {
    public AutonomousEvent(String id, String type, String agentId, Map<String, Object> data) {
        super(id, type, agentId, data);
    }
}
