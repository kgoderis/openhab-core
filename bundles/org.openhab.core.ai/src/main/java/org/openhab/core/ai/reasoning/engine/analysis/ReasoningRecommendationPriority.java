package org.openhab.core.ai.reasoning.engine.analysis;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Priority levels for reasoning recommendations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ReasoningRecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
