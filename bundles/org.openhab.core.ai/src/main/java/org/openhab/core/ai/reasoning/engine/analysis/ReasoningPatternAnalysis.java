package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;

/**
 * Analysis results for reasoning patterns.
 * 
 * <p>
 * This class provides comprehensive pattern analysis results including:
 * - Identified patterns and their frequencies
 * - Pattern sequences and transitions
 * - Pattern effectiveness and success rates
 * - Pattern recommendations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningPatternAnalysis {

    private final List<ReasoningPattern> identifiedPatterns;
    private final Map<String, Integer> patternFrequencies;
    private final Map<String, Double> patternSuccessRates;
    private final List<PatternSequence> commonSequences;
    private final Map<ReasoningStepType, List<ReasoningStepType>> stepTransitions;
    private final double overallPatternEffectiveness;
    private final List<PatternRecommendation> recommendations;
    private final Instant analysisTime;
    private final int totalStepsAnalyzed;

    private ReasoningPatternAnalysis(Builder builder) {
        this.identifiedPatterns = List.copyOf(builder.identifiedPatterns);
        this.patternFrequencies = Map.copyOf(builder.patternFrequencies);
        this.patternSuccessRates = Map.copyOf(builder.patternSuccessRates);
        this.commonSequences = List.copyOf(builder.commonSequences);
        this.stepTransitions = Map.copyOf(builder.stepTransitions);
        this.overallPatternEffectiveness = builder.overallPatternEffectiveness;
        this.recommendations = List.copyOf(builder.recommendations);
        this.analysisTime = builder.analysisTime;
        this.totalStepsAnalyzed = builder.totalStepsAnalyzed;
    }

    /**
     * Create a new builder for ReasoningPatternAnalysis.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters
    public List<ReasoningPattern> getIdentifiedPatterns() {
        return identifiedPatterns;
    }

    public Map<String, Integer> getPatternFrequencies() {
        return patternFrequencies;
    }

    /**
     * Get step type frequencies.
     * 
     * @return map of step types to their frequencies
     */
    public Map<ReasoningStepType, Integer> getStepTypeFrequency() {
        // Convert pattern frequencies to step type frequencies
        Map<ReasoningStepType, Integer> stepTypeFrequencies = new HashMap<>();
        for (Map.Entry<String, Integer> entry : patternFrequencies.entrySet()) {
            try {
                ReasoningStepType stepType = ReasoningStepType.valueOf(entry.getKey().toUpperCase());
                stepTypeFrequencies.put(stepType, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Skip patterns that don't map to valid step types
            }
        }
        return stepTypeFrequencies;
    }

    public Map<String, Double> getPatternSuccessRates() {
        return patternSuccessRates;
    }

    public List<PatternSequence> getCommonSequences() {
        return commonSequences;
    }

    public Map<ReasoningStepType, List<ReasoningStepType>> getStepTransitions() {
        return stepTransitions;
    }

    public double getOverallPatternEffectiveness() {
        return overallPatternEffectiveness;
    }

    public List<PatternRecommendation> getRecommendations() {
        return recommendations;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public int getTotalStepsAnalyzed() {
        return totalStepsAnalyzed;
    }

    /**
     * Get the most frequent pattern.
     * 
     * @return the most frequent pattern, or null if no patterns exist
     */
    public @Nullable String getMostFrequentPattern() {
        return patternFrequencies.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the most successful pattern.
     * 
     * @return the most successful pattern, or null if no patterns exist
     */
    public @Nullable String getMostSuccessfulPattern() {
        return patternSuccessRates.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the number of unique patterns identified.
     * 
     * @return the number of unique patterns
     */
    public int getUniquePatternCount() {
        return patternFrequencies.size();
    }

    /**
     * Get the average pattern success rate.
     * 
     * @return the average success rate
     */
    public double getAveragePatternSuccessRate() {
        if (patternSuccessRates.isEmpty()) {
            return 0.0;
        }
        return patternSuccessRates.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Builder for ReasoningPatternAnalysis.
     */
    public static final class Builder {
        private List<ReasoningPattern> identifiedPatterns = List.of();
        private Map<String, Integer> patternFrequencies = Map.of();
        private Map<String, Double> patternSuccessRates = Map.of();
        private List<PatternSequence> commonSequences = List.of();
        private Map<ReasoningStepType, List<ReasoningStepType>> stepTransitions = Map.of();
        private double overallPatternEffectiveness = 0.0;
        private List<PatternRecommendation> recommendations = List.of();
        private Instant analysisTime = Instant.now();
        private int totalStepsAnalyzed = 0;

        public Builder() {
        }

        public Builder(ReasoningPatternAnalysis source) {
            this.identifiedPatterns = List.copyOf(source.identifiedPatterns);
            this.patternFrequencies = Map.copyOf(source.patternFrequencies);
            this.patternSuccessRates = Map.copyOf(source.patternSuccessRates);
            this.commonSequences = List.copyOf(source.commonSequences);
            this.stepTransitions = Map.copyOf(source.stepTransitions);
            this.overallPatternEffectiveness = source.overallPatternEffectiveness;
            this.recommendations = List.copyOf(source.recommendations);
            this.analysisTime = source.analysisTime;
            this.totalStepsAnalyzed = source.totalStepsAnalyzed;
        }

        public Builder withIdentifiedPatterns(List<ReasoningPattern> identifiedPatterns) {
            this.identifiedPatterns = identifiedPatterns;
            return this;
        }

        public Builder withPatternFrequencies(Map<String, Integer> patternFrequencies) {
            this.patternFrequencies = patternFrequencies;
            return this;
        }

        public Builder withPatternSuccessRates(Map<String, Double> patternSuccessRates) {
            this.patternSuccessRates = patternSuccessRates;
            return this;
        }

        public Builder withCommonSequences(List<PatternSequence> commonSequences) {
            this.commonSequences = commonSequences;
            return this;
        }

        public Builder withStepTransitions(Map<ReasoningStepType, List<ReasoningStepType>> stepTransitions) {
            this.stepTransitions = stepTransitions;
            return this;
        }

        public Builder withOverallPatternEffectiveness(double overallPatternEffectiveness) {
            this.overallPatternEffectiveness = overallPatternEffectiveness;
            return this;
        }

        public Builder withRecommendations(List<PatternRecommendation> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public Builder withTotalStepsAnalyzed(int totalStepsAnalyzed) {
            this.totalStepsAnalyzed = totalStepsAnalyzed;
            return this;
        }

        public ReasoningPatternAnalysis build() {
            if (overallPatternEffectiveness < 0.0 || overallPatternEffectiveness > 1.0) {
                throw new IllegalArgumentException("overallPatternEffectiveness must be between 0.0 and 1.0");
            }
            if (totalStepsAnalyzed < 0) {
                throw new IllegalArgumentException("totalStepsAnalyzed must be non-negative");
            }
            return new ReasoningPatternAnalysis(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningPatternAnalysis other = (ReasoningPatternAnalysis) obj;
        return Double.compare(overallPatternEffectiveness, other.overallPatternEffectiveness) == 0
                && totalStepsAnalyzed == other.totalStepsAnalyzed
                && Objects.equals(identifiedPatterns, other.identifiedPatterns)
                && Objects.equals(patternFrequencies, other.patternFrequencies)
                && Objects.equals(patternSuccessRates, other.patternSuccessRates)
                && Objects.equals(commonSequences, other.commonSequences)
                && Objects.equals(stepTransitions, other.stepTransitions)
                && Objects.equals(recommendations, other.recommendations)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiedPatterns, patternFrequencies, patternSuccessRates, commonSequences,
                stepTransitions, overallPatternEffectiveness, recommendations, analysisTime, totalStepsAnalyzed);
    }

    @Override
    public String toString() {
        return String.format(
                "ReasoningPatternAnalysis{patterns=%d, effectiveness=%.2f, stepsAnalyzed=%d, analysisTime=%s}",
                getUniquePatternCount(), overallPatternEffectiveness, totalStepsAnalyzed, analysisTime);
    }

    /**
     * A reasoning pattern identified in the analysis.
     */
    public static final class ReasoningPattern {
        private final String patternId;
        private final String description;
        private final List<String> stepSequence;
        private final double frequency;
        private final double successRate;
        private final double averageDuration;
        private final double averageQuality;

        public ReasoningPattern(String patternId, String description, List<String> stepSequence, double frequency,
                double successRate, double averageDuration, double averageQuality) {
            this.patternId = patternId;
            this.description = description;
            this.stepSequence = List.copyOf(stepSequence);
            this.frequency = frequency;
            this.successRate = successRate;
            this.averageDuration = averageDuration;
            this.averageQuality = averageQuality;
        }

        // Getters
        public String getPatternId() {
            return patternId;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getStepSequence() {
            return stepSequence;
        }

        public double getFrequency() {
            return frequency;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageDuration() {
            return averageDuration;
        }

        public double getAverageQuality() {
            return averageQuality;
        }
    }

    /**
     * A sequence of patterns identified in the analysis.
     */
    public static final class PatternSequence {
        private final List<String> sequence;
        private final int frequency;
        private final double successRate;
        private final double averageDuration;

        public PatternSequence(List<String> sequence, int frequency, double successRate, double averageDuration) {
            this.sequence = List.copyOf(sequence);
            this.frequency = frequency;
            this.successRate = successRate;
            this.averageDuration = averageDuration;
        }

        // Getters
        public List<String> getSequence() {
            return sequence;
        }

        public int getFrequency() {
            return frequency;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageDuration() {
            return averageDuration;
        }
    }

    /**
     * A recommendation based on pattern analysis.
     */
    public static final class PatternRecommendation {
        private final String recommendationId;
        private final String description;
        private final String rationale;
        private final double confidence;
        private final List<String> suggestedActions;

        public PatternRecommendation(String recommendationId, String description, String rationale, double confidence,
                List<String> suggestedActions) {
            this.recommendationId = recommendationId;
            this.description = description;
            this.rationale = rationale;
            this.confidence = confidence;
            this.suggestedActions = List.copyOf(suggestedActions);
        }

        // Getters
        public String getRecommendationId() {
            return recommendationId;
        }

        public String getDescription() {
            return description;
        }

        public String getRationale() {
            return rationale;
        }

        public double getConfidence() {
            return confidence;
        }

        public List<String> getSuggestedActions() {
            return suggestedActions;
        }
    }
}
