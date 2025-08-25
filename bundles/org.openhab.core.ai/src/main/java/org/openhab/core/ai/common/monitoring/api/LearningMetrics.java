package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for learning progress metrics.
 * 
 * <p>
 * This interface provides learning progress-specific functionality including
 * learning rate, knowledge acquisition, skill improvement, and learning
 * efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface LearningMetrics {

    /**
     * Get the learning rate as a percentage.
     * 
     * @return learning rate between 0.0 and 100.0
     */
    double learningRate();

    /**
     * Get the knowledge acquisition rate per hour.
     * 
     * @return knowledge acquisition rate
     */
    double knowledgeAcquisitionRate();

    /**
     * Get the skill improvement rate as a percentage.
     * 
     * @return skill improvement rate between 0.0 and 100.0
     */
    double skillImprovementRate();

    /**
     * Get the learning efficiency score (0-100).
     * 
     * @return learning efficiency score
     */
    double learningEfficiency();

    /**
     * Get the learning progress as a percentage.
     * 
     * @return learning progress between 0.0 and 100.0
     */
    double learningProgress();

    /**
     * Get the learning accuracy as a percentage.
     * 
     * @return learning accuracy between 0.0 and 100.0
     */
    double learningAccuracy();

    /**
     * Get the learning retention rate as a percentage.
     * 
     * @return retention rate between 0.0 and 100.0
     */
    double learningRetentionRate();

    /**
     * Get the learning adaptation rate as a percentage.
     * 
     * @return adaptation rate between 0.0 and 100.0
     */
    double learningAdaptationRate();

    /**
     * Get the learning confidence level (0-100).
     * 
     * @return learning confidence level
     */
    double learningConfidence();

    /**
     * Get the learning throughput in concepts per hour.
     * 
     * @return learning throughput
     */
    double learningThroughput();
}
