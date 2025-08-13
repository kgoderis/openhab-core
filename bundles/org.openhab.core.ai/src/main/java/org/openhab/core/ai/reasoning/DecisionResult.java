package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of a decision produced by {@link AgentModelDecisionEngine}.
 *
 * Contains decision text, reasoning, confidence and expected outcomes along
 * with audit metadata.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DecisionResult {

    private final String decisionId;
    private final String contextId;
    private final String decision;
    private final String reasoning;
    private final double confidence;
    private final Map<String, Object> expectedOutcomes;
    private final long timestamp;
    private DecisionStatus status;

    public DecisionResult(String decisionId, String contextId, String decision, String reasoning, double confidence,
            Map<String, Object> expectedOutcomes, long timestamp, DecisionStatus status) {
        this.decisionId = decisionId;
        this.contextId = contextId;
        this.decision = decision;
        this.reasoning = reasoning;
        this.confidence = confidence;
        this.expectedOutcomes = new ConcurrentHashMap<>(expectedOutcomes);
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getDecision() {
        return decision;
    }

    public String getReasoning() {
        return reasoning;
    }

    public double getConfidence() {
        return confidence;
    }

    public Map<String, Object> getExpectedOutcomes() {
        return new ConcurrentHashMap<>(expectedOutcomes);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }
}


