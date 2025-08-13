package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class EventPattern {
    private final String agentId;
    private final List<AutonomousEvent> recentEvents = new ArrayList<>();
    private boolean patternDetected;

    public EventPattern(String agentId) {
        this.agentId = agentId;
        this.patternDetected = false;
    }

    public void analyzeEvent(AutonomousEvent event) {
        recentEvents.add(event);
        if (recentEvents.size() > 100) { recentEvents.remove(0); }
        patternDetected = detectSimplePattern();
    }

    private boolean detectSimplePattern() {
        if (recentEvents.size() < 3) { return false; }
        String lastType = recentEvents.get(recentEvents.size() - 1).getType();
        int count = 0;
        for (int i = recentEvents.size() - 1; i >= 0 && count < 3; i--) {
            if (lastType.equals(recentEvents.get(i).getType())) { count++; } else { break; }
        }
        return count >= 3;
    }

    public boolean isPatternDetected() { return patternDetected; }
}
