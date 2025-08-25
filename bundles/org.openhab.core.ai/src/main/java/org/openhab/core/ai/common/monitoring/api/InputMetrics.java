package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for input processing metrics.
 * 
 * <p>
 * This interface provides input processing-specific functionality including
 * input validation rates, processing throughput, error rates, and input
 * quality metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface InputMetrics {

    /**
     * Get the input validation success rate as a percentage.
     * 
     * @return validation success rate between 0.0 and 100.0
     */
    double validationSuccessRate();

    /**
     * Get the input processing throughput in inputs per second.
     * 
     * @return processing throughput
     */
    double processingThroughput();

    /**
     * Get the input error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double inputErrorRate();

    /**
     * Get the input quality score (0-100).
     * 
     * @return input quality score
     */
    double inputQuality();

    /**
     * Get the average input size in bytes.
     * 
     * @return average input size
     */
    double averageInputSize();

    /**
     * Get the input processing latency in milliseconds.
     * 
     * @return processing latency
     */
    double processingLatency();

    /**
     * Get the input queue depth.
     * 
     * @return queue depth
     */
    long queueDepth();

    /**
     * Get the input rejection rate as a percentage.
     * 
     * @return rejection rate between 0.0 and 100.0
     */
    double rejectionRate();

    /**
     * Get the input format compatibility rate as a percentage.
     * 
     * @return format compatibility rate between 0.0 and 100.0
     */
    double formatCompatibilityRate();
}
