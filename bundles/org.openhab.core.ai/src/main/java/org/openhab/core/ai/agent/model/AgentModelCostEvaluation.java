package org.openhab.core.ai.agent.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.evaluation.AbstractEvaluation;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;

/**
 * Cost evaluation result for an agent model.
 * 
 * <p>
 * This class represents the cost evaluation for an agent model,
 * including cost per token, budget comparison, and cost efficiency scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelCostEvaluation extends AbstractEvaluation {

    @Reference
    private @Nullable MetricsService metricsService;

    private final double costPerToken;
    private final double budget;
    private final double costScore;

    private AgentModelCostEvaluation(Builder b) {
        super(b.overallScore, "COST");
        this.costPerToken = b.costPerToken;
        this.budget = b.budget;
        this.costScore = b.costScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public double getCostPerToken() {
        return costPerToken;
    }

    public double getBudget() {
        return budget;
    }

    public double getCostScore() {
        return costScore;
    }

    public boolean isWithinBudget() {
        return costPerToken <= budget;
    }

    public double getBudgetUtilization() {
        return budget > 0 ? costPerToken / budget : 0.0;
    }

    /**
     * Record cost evaluation metrics using MetricsService.
     */
    public void recordEvaluation() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = isWithinBudget();
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("agent-model", "cost-evaluation", success, java.time.Duration.ofMillis(duration));
        }
    }

    public static final class Builder {
        private double costPerToken = 0.0;
        private double budget = 1.0;
        private double costScore = 0.0;
        private double overallScore = 0.0;

        public Builder() {
        }

        public Builder(AgentModelCostEvaluation source) {
            this.costPerToken = source.costPerToken;
            this.budget = source.budget;
            this.costScore = source.costScore;
            this.overallScore = source.getOverallScore();
        }

        public Builder withCostPerToken(double costPerToken) {
            this.costPerToken = costPerToken;
            return this;
        }

        public Builder withBudget(double budget) {
            this.budget = budget;
            return this;
        }

        public Builder withCostScore(double costScore) {
            this.costScore = costScore;
            return this;
        }

        public Builder withOverallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public AgentModelCostEvaluation build() {
            if (costPerToken < 0) {
                throw new IllegalArgumentException("costPerToken must be >= 0");
            }
            if (budget < 0) {
                throw new IllegalArgumentException("budget must be >= 0");
            }
            if (costScore < 0 || costScore > 1) {
                throw new IllegalArgumentException("costScore must be between 0 and 1");
            }
            // overallScore validation is handled by AbstractEvaluation constructor
            return new AgentModelCostEvaluation(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        AgentModelCostEvaluation other = (AgentModelCostEvaluation) obj;
        return Double.compare(costPerToken, other.costPerToken) == 0 && Double.compare(budget, other.budget) == 0
                && Double.compare(costScore, other.costScore) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), costPerToken, budget, costScore);
    }

    @Override
    public String toString() {
        return "AgentModelCostEvaluation{" + "costPerToken=" + costPerToken + ", budget=" + budget + ", costScore="
                + costScore + ", overallScore=" + getOverallScore() + '}';
    }
}
