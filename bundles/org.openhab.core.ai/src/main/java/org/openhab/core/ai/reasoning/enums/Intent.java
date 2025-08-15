package org.openhab.core.ai.reasoning.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Intent classification result.
 *
 * Represents the recognized intent, its confidence and extracted parameters.
 * Extracted from {@link AgentModelNLPProcessor}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Intent {

    private final IntentType type;
    private final double confidence;
    private final Map<String, Object> parameters;

    public Intent(IntentType type, double confidence, Map<String, Object> parameters) {
        this.type = type;
        this.confidence = confidence;
        this.parameters = new ConcurrentHashMap<>(parameters);
    }

    public IntentType getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    public Map<String, Object> getParameters() {
        return new ConcurrentHashMap<>(parameters);
    }
}
