package org.openhab.core.ai.reasoning;

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
final class DecisionPattern {
    private final String patternId;
    private final String description;
    private final Map<String, Object> characteristics;

    DecisionPattern(String patternId, String description) {
        this.patternId = patternId;
        this.description = description;
        this.characteristics = new ConcurrentHashMap<>();
    }

    String getPatternId() { return patternId; }
    String getDescription() { return description; }
    Map<String, Object> getCharacteristics() { return characteristics; }
}


