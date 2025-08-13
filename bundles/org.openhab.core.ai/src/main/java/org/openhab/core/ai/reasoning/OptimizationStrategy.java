package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for optimization strategies used by the decision optimizer.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
class OptimizationStrategy {
    private final String strategyId;
    private final String description;
    private final double effectiveness;

    OptimizationStrategy(String strategyId, String description, double effectiveness) {
        this.strategyId = strategyId;
        this.description = description;
        this.effectiveness = effectiveness;
    }

    String getStrategyId() { return strategyId; }
    String getDescription() { return description; }
    double getEffectiveness() { return effectiveness; }
}


