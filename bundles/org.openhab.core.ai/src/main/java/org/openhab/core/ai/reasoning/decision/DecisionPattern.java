package org.openhab.core.ai.reasoning.decision;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a decision pattern for optimization analysis.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class DecisionPattern {
    private final String patternId;
    private final String description;
    private final Map<String, Object> characteristics;

    public DecisionPattern(String patternId, String description) {
        this.patternId = patternId;
        this.description = description;
        this.characteristics = new ConcurrentHashMap<>();
    }

    public String getPatternId() {
        return patternId;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getCharacteristics() {
        return characteristics;
    }
}
