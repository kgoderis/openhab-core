package org.openhab.core.ai.agent.nlp;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an extracted entity from NLP processing.
 * 
 * This class encapsulates the entity type, value, and confidence level
 * for entities extracted from natural language input.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelEntity {

    private final String entityType;
    private final String value;
    private final double confidence;

    private AgentModelEntity(Builder builder) {
        this.entityType = Objects.requireNonNull(builder.entityType, "entityType");
        this.value = Objects.requireNonNull(builder.value, "value");
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
     * Get the entity type.
     * 
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Get the entity value.
     * 
     * @return the entity value
     */
    public String getValue() {
        return value;
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
        AgentModelEntity other = (AgentModelEntity) obj;
        return Double.compare(other.confidence, confidence) == 0 && Objects.equals(entityType, other.entityType)
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, value, confidence);
    }

    @Override
    public String toString() {
        return String.format("AgentModelEntity{entityType='%s', value='%s', confidence=%.2f}", entityType, value,
                confidence);
    }

    /**
     * Builder for AgentModelEntity.
     */
    public static final class Builder {
        private String entityType = "";
        private String value = "";
        private double confidence = 0.0;

        public Builder withEntityType(String entityType) {
            this.entityType = Objects.requireNonNull(entityType, "entityType");
            return this;
        }

        public Builder withValue(String value) {
            this.value = Objects.requireNonNull(value, "value");
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public AgentModelEntity build() {
            return new AgentModelEntity(this);
        }
    }
}
