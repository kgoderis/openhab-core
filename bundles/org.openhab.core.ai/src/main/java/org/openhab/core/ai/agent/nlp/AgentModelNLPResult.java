package org.openhab.core.ai.agent.nlp;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of NLP processing for intelligent agents.
 * 
 * This class represents the outcome of natural language processing,
 * including extracted goals, recognized intents, and semantic analysis.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelNLPResult {

    private final String originalInput;
    private final List<String> goals;
    private final List<AgentModelIntent> intents;
    private final AgentModelSemanticAnalysis semanticAnalysis;
    private final Map<String, Object> structuredData;
    private final double confidence;
    private final Instant processedAt;

    private AgentModelNLPResult(Builder builder) {
        this.originalInput = Objects.requireNonNull(builder.originalInput, "originalInput");
        this.goals = List.copyOf(Objects.requireNonNull(builder.goals, "goals"));
        this.intents = List.copyOf(Objects.requireNonNull(builder.intents, "intents"));
        this.semanticAnalysis = Objects.requireNonNull(builder.semanticAnalysis, "semanticAnalysis");
        this.structuredData = Map.copyOf(Objects.requireNonNull(builder.structuredData, "structuredData"));
        this.confidence = Math.max(0.0, Math.min(1.0, builder.confidence));
        this.processedAt = Objects.requireNonNull(builder.processedAt, "processedAt");
    }

    /**
     * Create a new builder.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the original input.
     * 
     * @return the original input
     */
    public String getOriginalInput() {
        return originalInput;
    }

    /**
     * Get the extracted goals.
     * 
     * @return the goals list
     */
    public List<String> getGoals() {
        return goals;
    }

    /**
     * Get the recognized intents.
     * 
     * @return the intents list
     */
    public List<AgentModelIntent> getIntents() {
        return intents;
    }

    /**
     * Get the semantic analysis.
     * 
     * @return the semantic analysis
     */
    public AgentModelSemanticAnalysis getSemanticAnalysis() {
        return semanticAnalysis;
    }

    /**
     * Get the structured data.
     * 
     * @return the structured data map
     */
    public Map<String, Object> getStructuredData() {
        return structuredData;
    }

    /**
     * Get the confidence score.
     * 
     * @return the confidence score
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Get the processing timestamp.
     * 
     * @return the processing timestamp
     */
    public Instant getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelNLPResult other = (AgentModelNLPResult) obj;
        return Double.compare(other.confidence, confidence) == 0 && Objects.equals(originalInput, other.originalInput)
                && Objects.equals(goals, other.goals) && Objects.equals(intents, other.intents)
                && Objects.equals(semanticAnalysis, other.semanticAnalysis)
                && Objects.equals(structuredData, other.structuredData)
                && Objects.equals(processedAt, other.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalInput, goals, intents, semanticAnalysis, structuredData, confidence, processedAt);
    }

    @Override
    public String toString() {
        return String.format("AgentModelNLPResult{goals=%d, intents=%d, confidence=%.2f}", goals.size(), intents.size(),
                confidence);
    }

    /**
     * Builder for AgentModelNLPResult.
     */
    public static final class Builder {
        private String originalInput = "";
        private List<String> goals = List.of();
        private List<AgentModelIntent> intents = List.of();
        private AgentModelSemanticAnalysis semanticAnalysis;
        private Map<String, Object> structuredData = Map.of();
        private double confidence = 0.0;
        private Instant processedAt = Instant.now();

        public Builder withOriginalInput(String originalInput) {
            this.originalInput = Objects.requireNonNull(originalInput, "originalInput");
            return this;
        }

        public Builder withGoals(List<String> goals) {
            this.goals = Objects.requireNonNull(goals, "goals");
            return this;
        }

        public Builder withIntents(List<AgentModelIntent> intents) {
            this.intents = Objects.requireNonNull(intents, "intents");
            return this;
        }

        public Builder withSemanticAnalysis(AgentModelSemanticAnalysis semanticAnalysis) {
            this.semanticAnalysis = Objects.requireNonNull(semanticAnalysis, "semanticAnalysis");
            return this;
        }

        public Builder withStructuredData(Map<String, Object> structuredData) {
            this.structuredData = Objects.requireNonNull(structuredData, "structuredData");
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder withProcessedAt(Instant processedAt) {
            this.processedAt = Objects.requireNonNull(processedAt, "processedAt");
            return this;
        }

        public AgentModelNLPResult build() {
            return new AgentModelNLPResult(this);
        }
    }
}
