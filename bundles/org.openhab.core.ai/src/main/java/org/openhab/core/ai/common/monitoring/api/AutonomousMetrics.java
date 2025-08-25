package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for autonomous behavior metrics.
 * 
 * <p>
 * This interface provides autonomous behavior-specific functionality including
 * decision-making accuracy, autonomous action success rates, learning progress,
 * and adaptation metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AutonomousMetrics {

    /**
     * Get the autonomous decision accuracy as a percentage.
     * 
     * @return decision accuracy between 0.0 and 100.0
     */
    double decisionAccuracy();

    /**
     * Get the autonomous action success rate as a percentage.
     * 
     * @return action success rate between 0.0 and 100.0
     */
    double actionSuccessRate();

    /**
     * Get the learning progress rate (0-100).
     * 
     * @return learning progress score
     */
    double learningProgress();

    /**
     * Get the adaptation rate as a percentage.
     * 
     * @return adaptation rate between 0.0 and 100.0
     */
    double adaptationRate();

    /**
     * Get the autonomous behavior efficiency (0-100).
     * 
     * @return behavior efficiency score
     */
    double behaviorEfficiency();

    /**
     * Get the autonomous decision latency in milliseconds.
     * 
     * @return decision latency
     */
    double decisionLatency();

    /**
     * Get the autonomous action throughput in actions per minute.
     * 
     * @return action throughput
     */
    double actionThroughput();

    /**
     * Get the autonomous error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double autonomousErrorRate();

    /**
     * Get the autonomous confidence level (0-100).
     * 
     * @return confidence level
     */
    double confidenceLevel();

    /**
     * Get the autonomous exploration rate as a percentage.
     * 
     * @return exploration rate between 0.0 and 100.0
     */
    double explorationRate();
}
