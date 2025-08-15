package org.openhab.core.ai.reasoning.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class BehaviorPatternEntry {
    private final String interactionType;
    private final List<Map<String, Object>> recentInteractions = new ArrayList<>();
    private double confidence = 0.0;

    public BehaviorPatternEntry(String interactionType) {
        this.interactionType = interactionType;
    }

    public boolean addInteraction(Map<String, Object> interactionData) {
        recentInteractions.add(interactionData);
        if (recentInteractions.size() > 100) {
            recentInteractions.remove(0);
        }
        boolean patternDetected = detectPattern();
        updateConfidence();
        return patternDetected;
    }

    private boolean detectPattern() {
        if (recentInteractions.size() < 3) {
            return false;
        }
        return recentInteractions.size() >= 5;
    }

    private void updateConfidence() {
        confidence = Math.min(1.0, recentInteractions.size() / 10.0);
    }

    public String getInteractionType() {
        return interactionType;
    }

    public double getConfidence() {
        return confidence;
    }
}
