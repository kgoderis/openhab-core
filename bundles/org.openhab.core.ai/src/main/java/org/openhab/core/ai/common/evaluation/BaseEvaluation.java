package org.openhab.core.ai.common.evaluation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for all evaluation implementations.
 * 
 * <p>
 * This interface defines the common contract for all evaluation classes,
 * providing standardized access to evaluation scores and quality indicators.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface BaseEvaluation {

    /**
     * Get the overall evaluation score.
     * 
     * @return overall score between 0.0 and 1.0
     */
    double getOverallScore();

    /**
     * Check if the evaluation indicates excellent performance.
     * 
     * @return true if overall score >= 0.8
     */
    default boolean isExcellent() {
        return getOverallScore() >= 0.8;
    }

    /**
     * Check if the evaluation indicates good performance.
     * 
     * @return true if overall score >= 0.6 and < 0.8
     */
    default boolean isGood() {
        double score = getOverallScore();
        return score >= 0.6 && score < 0.8;
    }

    /**
     * Check if the evaluation indicates fair performance.
     * 
     * @return true if overall score >= 0.4 and < 0.6
     */
    default boolean isFair() {
        double score = getOverallScore();
        return score >= 0.4 && score < 0.6;
    }

    /**
     * Check if the evaluation indicates poor performance.
     * 
     * @return true if overall score < 0.4
     */
    default boolean isPoor() {
        return getOverallScore() < 0.4;
    }

    /**
     * Get the evaluation type.
     * 
     * @return the type of evaluation
     */
    String getEvaluationType();
}
