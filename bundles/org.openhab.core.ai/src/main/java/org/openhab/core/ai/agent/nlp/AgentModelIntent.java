package org.openhab.core.ai.agent.nlp;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a recognized intent from natural language processing.
 * 
 * This class encapsulates the intent type, device type, action, and confidence
 * level for a recognized user intent.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelIntent {

    private final String intentType;
    private final @Nullable String deviceType;
    private final String action;
    private final double confidence;
    private final Map<String, Object> parameters;

    private AgentModelIntent(Builder builder) {
        this.intentType = Objects.requireNonNull(builder.intentType, "intentType");
        this.deviceType = builder.deviceType;
        this.action = Objects.requireNonNull(builder.action, "action");
        this.confidence = Math.max(0.0, Math.min(1.0, builder.confidence));
        this.parameters = Map.copyOf(Objects.requireNonNull(builder.parameters, "parameters"));
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
     * Get the intent type.
     * 
     * @return the intent type
     */
    public String getIntentType() {
        return intentType;
    }

    /**
     * Get the device type.
     * 
     * @return the device type or null
     */
    public @Nullable String getDeviceType() {
        return deviceType;
    }

    /**
     * Get the action.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
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
     * Get the parameters.
     * 
     * @return the parameters map
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelIntent other = (AgentModelIntent) obj;
        return Double.compare(other.confidence, confidence) == 0 && Objects.equals(intentType, other.intentType)
                && Objects.equals(deviceType, other.deviceType) && Objects.equals(action, other.action)
                && Objects.equals(parameters, other.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intentType, deviceType, action, confidence, parameters);
    }

    @Override
    public String toString() {
        return String.format("AgentModelIntent{intentType='%s', deviceType='%s', action='%s', confidence=%.2f}",
                intentType, deviceType, action, confidence);
    }

    /**
     * Builder for AgentModelIntent.
     */
    public static final class Builder {
        private String intentType = "";
        private @Nullable String deviceType;
        private String action = "";
        private double confidence = 0.0;
        private Map<String, Object> parameters = Map.of();

        public Builder withIntentType(String intentType) {
            this.intentType = Objects.requireNonNull(intentType, "intentType");
            return this;
        }

        public Builder withDeviceType(@Nullable String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder withAction(String action) {
            this.action = Objects.requireNonNull(action, "action");
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = Objects.requireNonNull(parameters, "parameters");
            return this;
        }

        public AgentModelIntent build() {
            return new AgentModelIntent(this);
        }
    }
}
