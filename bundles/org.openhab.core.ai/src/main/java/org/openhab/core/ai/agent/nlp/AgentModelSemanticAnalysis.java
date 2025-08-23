package org.openhab.core.ai.agent.nlp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents semantic analysis result from NLP processing.
 * 
 * This class encapsulates entities, sentiment, context information, and language
 * detection results from semantic analysis of natural language input.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelSemanticAnalysis {

    private final List<AgentModelEntity> entities;
    private final AgentModelSentiment sentiment;
    private final Map<String, Object> contextInfo;
    private final String language;
    private final double confidence;

    private AgentModelSemanticAnalysis(Builder builder) {
        this.entities = List.copyOf(Objects.requireNonNull(builder.entities, "entities"));
        this.sentiment = Objects.requireNonNull(builder.sentiment, "sentiment");
        this.contextInfo = Map.copyOf(Objects.requireNonNull(builder.contextInfo, "contextInfo"));
        this.language = Objects.requireNonNull(builder.language, "language");
        this.confidence = Math.max(0.0, Math.min(1.0, builder.confidence));
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
     * Get the extracted entities.
     * 
     * @return the entities list
     */
    public List<AgentModelEntity> getEntities() {
        return entities;
    }

    /**
     * Get the sentiment analysis.
     * 
     * @return the sentiment analysis
     */
    public AgentModelSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Get the context information.
     * 
     * @return the context information map
     */
    public Map<String, Object> getContextInfo() {
        return contextInfo;
    }

    /**
     * Get the detected language.
     * 
     * @return the language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Get the confidence score.
     * 
     * @return the confidence score
     */
    public double getConfidence() {
        return confidence;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelSemanticAnalysis other = (AgentModelSemanticAnalysis) obj;
        return Double.compare(other.confidence, confidence) == 0 && Objects.equals(entities, other.entities)
                && Objects.equals(sentiment, other.sentiment) && Objects.equals(contextInfo, other.contextInfo)
                && Objects.equals(language, other.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entities, sentiment, contextInfo, language, confidence);
    }

    @Override
    public String toString() {
        return String.format("AgentModelSemanticAnalysis{entities=%d, language='%s', confidence=%.2f}", entities.size(),
                language, confidence);
    }

    /**
     * Builder for AgentModelSemanticAnalysis.
     */
    public static final class Builder {
        private List<AgentModelEntity> entities = List.of();
        private AgentModelSentiment sentiment;
        private Map<String, Object> contextInfo = Map.of();
        private String language = "en";
        private double confidence = 0.0;

        public Builder withEntities(List<AgentModelEntity> entities) {
            this.entities = Objects.requireNonNull(entities, "entities");
            return this;
        }

        public Builder withSentiment(AgentModelSentiment sentiment) {
            this.sentiment = Objects.requireNonNull(sentiment, "sentiment");
            return this;
        }

        public Builder withContextInfo(Map<String, Object> contextInfo) {
            this.contextInfo = Objects.requireNonNull(contextInfo, "contextInfo");
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = Objects.requireNonNull(language, "language");
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public AgentModelSemanticAnalysis build() {
            return new AgentModelSemanticAnalysis(this);
        }
    }
}
