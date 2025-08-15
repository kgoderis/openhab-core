package org.openhab.core.ai.reasoning.optimization;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Optimization opportunity description used by the decision optimizer.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class OptimizationOpportunity {
    private final String opportunityId;
    private final String description;
    private final String strategyId;
    private final double expectedImprovement;

    OptimizationOpportunity(String opportunityId, String description, String strategyId, double expectedImprovement) {
        this.opportunityId = opportunityId;
        this.description = description;
        this.strategyId = strategyId;
        this.expectedImprovement = expectedImprovement;
    }

    String getOpportunityId() {
        return opportunityId;
    }

    String getDescription() {
        return description;
    }

    String getStrategyId() {
        return strategyId;
    }

    double getExpectedImprovement() {
        return expectedImprovement;
    }
}
