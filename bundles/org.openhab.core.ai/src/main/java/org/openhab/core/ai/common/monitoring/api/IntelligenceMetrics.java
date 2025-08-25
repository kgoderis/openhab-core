package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for intelligence analysis metrics.
 * 
 * <p>
 * This interface provides functionality for calculating intelligence-related metrics
 * including decision quality, learning efficiency, and adaptation rate.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface IntelligenceMetrics {

    /**
     * Calculate decision quality.
     * 
     * @return decision quality score between 0.0 and 1.0
     */
    double decisionQuality();

    /**
     * Calculate learning efficiency.
     * 
     * @return learning efficiency score between 0.0 and 1.0
     */
    double learningEfficiency();

    /**
     * Calculate adaptation rate.
     * 
     * @return adaptation rate percentage
     */
    double adaptationRate();
}
