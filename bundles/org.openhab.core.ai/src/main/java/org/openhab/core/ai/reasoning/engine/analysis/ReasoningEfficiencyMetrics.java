package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Efficiency metrics for reasoning steps.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningEfficiencyMetrics {

    private final double overallEfficiency;
    private final double timeEfficiency;
    private final double costEfficiency;
    private final double tokenEfficiency;
    private final Map<String, Double> efficiencyByModel;
    private final Instant calculationTime;

    private ReasoningEfficiencyMetrics(Builder builder) {
        this.overallEfficiency = builder.overallEfficiency;
        this.timeEfficiency = builder.timeEfficiency;
        this.costEfficiency = builder.costEfficiency;
        this.tokenEfficiency = builder.tokenEfficiency;
        this.efficiencyByModel = Map.copyOf(builder.efficiencyByModel);
        this.calculationTime = builder.calculationTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getOverallEfficiency() {
        return overallEfficiency;
    }

    public double getTimeEfficiency() {
        return timeEfficiency;
    }

    public double getCostEfficiency() {
        return costEfficiency;
    }

    public double getTokenEfficiency() {
        return tokenEfficiency;
    }

    public Map<String, Double> getEfficiencyByModel() {
        return efficiencyByModel;
    }

    public Instant getCalculationTime() {
        return calculationTime;
    }

    public static final class Builder {
        private double overallEfficiency = 0.0;
        private double timeEfficiency = 0.0;
        private double costEfficiency = 0.0;
        private double tokenEfficiency = 0.0;
        private Map<String, Double> efficiencyByModel = Map.of();
        private Instant calculationTime = Instant.now();

        public Builder withOverallEfficiency(double overallEfficiency) {
            this.overallEfficiency = overallEfficiency;
            return this;
        }

        public Builder withTimeEfficiency(double timeEfficiency) {
            this.timeEfficiency = timeEfficiency;
            return this;
        }

        public Builder withCostEfficiency(double costEfficiency) {
            this.costEfficiency = costEfficiency;
            return this;
        }

        public Builder withTokenEfficiency(double tokenEfficiency) {
            this.tokenEfficiency = tokenEfficiency;
            return this;
        }

        public Builder withEfficiencyByModel(Map<String, Double> efficiencyByModel) {
            this.efficiencyByModel = efficiencyByModel;
            return this;
        }

        public Builder withCalculationTime(Instant calculationTime) {
            this.calculationTime = calculationTime;
            return this;
        }

        public ReasoningEfficiencyMetrics build() {
            if (overallEfficiency < 0.0 || overallEfficiency > 1.0) {
                throw new IllegalArgumentException("overallEfficiency must be between 0.0 and 1.0");
            }
            if (timeEfficiency < 0.0 || timeEfficiency > 1.0) {
                throw new IllegalArgumentException("timeEfficiency must be between 0.0 and 1.0");
            }
            if (costEfficiency < 0.0 || costEfficiency > 1.0) {
                throw new IllegalArgumentException("costEfficiency must be between 0.0 and 1.0");
            }
            if (tokenEfficiency < 0.0 || tokenEfficiency > 1.0) {
                throw new IllegalArgumentException("tokenEfficiency must be between 0.0 and 1.0");
            }
            return new ReasoningEfficiencyMetrics(this);
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
        ReasoningEfficiencyMetrics other = (ReasoningEfficiencyMetrics) obj;
        return Double.compare(overallEfficiency, other.overallEfficiency) == 0
                && Double.compare(timeEfficiency, other.timeEfficiency) == 0
                && Double.compare(costEfficiency, other.costEfficiency) == 0
                && Double.compare(tokenEfficiency, other.tokenEfficiency) == 0
                && Objects.equals(efficiencyByModel, other.efficiencyByModel)
                && Objects.equals(calculationTime, other.calculationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overallEfficiency, timeEfficiency, costEfficiency, tokenEfficiency, efficiencyByModel,
                calculationTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningEfficiencyMetrics{overall=%.2f, time=%.2f, cost=%.2f, tokens=%.2f}",
                overallEfficiency, timeEfficiency, costEfficiency, tokenEfficiency);
    }
}
