package org.openhab.core.ai.common.evaluation;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for evaluation implementations.
 * 
 * <p>
 * This class provides common functionality for all evaluation implementations:
 * - Common validation logic for scores
 * - Standard equals/hashCode/toString implementations
 * - Builder pattern support
 * - Immutable design patterns
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractEvaluation implements BaseEvaluation {

    private final double overallScore;
    private final String evaluationType;

    /**
     * Create a new evaluation.
     * 
     * @param overallScore the overall evaluation score
     * @param evaluationType the type of evaluation
     */
    protected AbstractEvaluation(double overallScore, String evaluationType) {
        this.overallScore = validateScore(overallScore, "overallScore");
        this.evaluationType = Objects.requireNonNull(evaluationType, "evaluationType");
    }

    @Override
    public double getOverallScore() {
        return overallScore;
    }

    @Override
    public String getEvaluationType() {
        return evaluationType;
    }

    /**
     * Validate that a score is within the valid range.
     * 
     * @param score the score to validate
     * @param fieldName the field name for error messages
     * @return the validated score
     * @throws IllegalArgumentException if score is invalid
     */
    protected static double validateScore(double score, String fieldName) {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + score);
        }
        return score;
    }

    /**
     * Validate that a score is non-negative.
     * 
     * @param score the score to validate
     * @param fieldName the field name for error messages
     * @return the validated score
     * @throws IllegalArgumentException if score is negative
     */
    protected static double validateNonNegative(double score, String fieldName) {
        if (score < 0.0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative, got: " + score);
        }
        return score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractEvaluation other = (AbstractEvaluation) obj;
        return Double.compare(overallScore, other.overallScore) == 0
                && Objects.equals(evaluationType, other.evaluationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overallScore, evaluationType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "overallScore=" + overallScore + ", evaluationType=" + evaluationType
                + '}';
    }
}
