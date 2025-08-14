package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class BehaviorPattern {
    private final String userId;
    private final List<BehaviorPatternEntry> patterns = new ArrayList<>();
    private double confidence = 0.0;

    public BehaviorPattern(String userId) {
        this.userId = userId;
    }

    public boolean analyzeInteraction(String interactionType, Map<String, Object> interactionData) {
        BehaviorPatternEntry pattern = patterns.stream().filter(p -> interactionType.equals(p.getInteractionType()))
                .findFirst().orElse(null);

        if (pattern == null) {
            pattern = new BehaviorPatternEntry(interactionType);
            patterns.add(pattern);
        }

        boolean detected = pattern.addInteraction(interactionData);
        updateConfidence();
        return detected;
    }

    private void updateConfidence() {
        confidence = patterns.stream().mapToDouble(BehaviorPatternEntry::getConfidence).average().orElse(0.0);
    }

    public String getUserId() { return userId; }
    public List<BehaviorPatternEntry> getPatterns() { return Collections.unmodifiableList(patterns); }
    public double getConfidence() { return confidence; }

    // BehaviorPatternEntry extracted to top-level class org.openhab.core.ai.reasoning.BehaviorPatternEntry
}
