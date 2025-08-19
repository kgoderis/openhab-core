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

    public String getStrategyId() {
        return strategyId;
    }

    public String getDescription() {
        return description;
    }

    public double getEffectiveness() {
        return effectiveness;
    }
}
