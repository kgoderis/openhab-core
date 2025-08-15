package org.openhab.core.ai.reasoning.optimization;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for optimization strategies used by the decision optimizer.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class OptimizationStrategy {
    private final String strategyId;
    private final String description;
    private final double effectiveness;

    protected OptimizationStrategy(String strategyId, String description, double effectiveness) {
        this.strategyId = strategyId;
        this.description = description;
        this.effectiveness = effectiveness;
    }

    String getStrategyId() {
        return strategyId;
    }

    String getDescription() {
        return description;
    }

    double getEffectiveness() {
        return effectiveness;
    }
}
