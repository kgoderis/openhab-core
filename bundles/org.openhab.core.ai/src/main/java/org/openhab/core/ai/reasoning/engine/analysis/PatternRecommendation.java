package org.openhab.core.ai.reasoning.engine.analysis;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Pattern Recommendation for Reasoning Engine Analysis
 * 
 * This class represents a recommendation based on pattern analysis in the reasoning engine.
 * It provides suggestions for improving reasoning patterns, performance, and quality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class PatternRecommendation {

    private final String patternType;
    private final String description;
    private final RecommendationType type;
    private final double confidence;
    private final List<String> actions;
    private final long timestamp;

    /**
     * Create a new pattern recommendation.
     * 
     * @param patternType the type of pattern this recommendation addresses
     * @param description the description of the recommendation
     * @param type the type of recommendation
     * @param confidence the confidence level (0.0 to 1.0)
     * @param actions the list of recommended actions
     */
    public PatternRecommendation(String patternType, String description, RecommendationType type, double confidence,
            List<String> actions) {
        this.patternType = Objects.requireNonNull(patternType, "patternType");
        this.description = Objects.requireNonNull(description, "description");
        this.type = Objects.requireNonNull(type, "type");
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.actions = List.copyOf(Objects.requireNonNull(actions, "actions"));
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the pattern type this recommendation addresses.
     * 
     * @return the pattern type
     */
    public String getPatternType() {
        return patternType;
    }

    /**
     * Get the description of the recommendation.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the type of recommendation.
     * 
     * @return the recommendation type
     */
    public RecommendationType getType() {
        return type;
    }

    /**
     * Get the confidence level of this recommendation.
     * 
     * @return the confidence level (0.0 to 1.0)
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Get the list of recommended actions.
     * 
     * @return the list of actions
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Get the timestamp when this recommendation was created.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if this recommendation is high priority.
     * 
     * @return true if high priority
     */
    public boolean isHighPriority() {
        return confidence > 0.8 && (type == RecommendationType.CRITICAL || type == RecommendationType.IMPORTANT);
    }

    /**
     * Check if this recommendation is actionable.
     * 
     * @return true if actionable
     */
    public boolean isActionable() {
        return !actions.isEmpty() && confidence > 0.5;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PatternRecommendation other = (PatternRecommendation) obj;
        return Objects.equals(patternType, other.patternType) && Objects.equals(description, other.description)
                && type == other.type && Double.compare(confidence, other.confidence) == 0
                && Objects.equals(actions, other.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternType, description, type, confidence, actions);
    }

    @Override
    public String toString() {
        return "PatternRecommendation{" + "patternType='" + patternType + '\'' + ", description='" + description + '\''
                + ", type=" + type + ", confidence=" + confidence + ", actions=" + actions + ", timestamp=" + timestamp
                + '}';
    }

    /**
     * Recommendation types for pattern analysis.
     */
    public enum RecommendationType {
        /** Critical recommendation requiring immediate attention */
        CRITICAL,
        /** Important recommendation for significant improvement */
        IMPORTANT,
        /** Moderate recommendation for optimization */
        MODERATE,
        /** Minor recommendation for fine-tuning */
        MINOR,
        /** Informational recommendation for awareness */
        INFO
    }
}
