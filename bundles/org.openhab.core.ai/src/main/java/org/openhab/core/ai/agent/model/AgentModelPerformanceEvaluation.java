package org.openhab.core.ai.agent.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.evaluation.AbstractEvaluation;

/**
 * Performance evaluation result for an agent model.
 * 
 * <p>
 * This class represents the performance evaluation metrics for an agent model,
 * including response time, throughput, success rate, and availability scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelPerformanceEvaluation extends AbstractEvaluation {

    private final double responseTimeScore;
    private final double throughputScore;
    private final double successRateScore;
    private final double availabilityScore;

    private AgentModelPerformanceEvaluation(Builder b) {
        super(b.overallScore, "PERFORMANCE");
        this.responseTimeScore = b.responseTimeScore;
        this.throughputScore = b.throughputScore;
        this.successRateScore = b.successRateScore;
        this.availabilityScore = b.availabilityScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public double getResponseTimeScore() {
        return responseTimeScore;
    }

    public double getThroughputScore() {
        return throughputScore;
    }

    public double getSuccessRateScore() {
        return successRateScore;
    }

    public double getAvailabilityScore() {
        return availabilityScore;
    }

    public static final class Builder {
        private double responseTimeScore = 0.0;
        private double throughputScore = 0.0;
        private double successRateScore = 0.0;
        private double availabilityScore = 0.0;
        private double overallScore = 0.0;

        public Builder() {
        }

        public Builder(AgentModelPerformanceEvaluation source) {
            this.responseTimeScore = source.responseTimeScore;
            this.throughputScore = source.throughputScore;
            this.successRateScore = source.successRateScore;
            this.availabilityScore = source.availabilityScore;
            this.overallScore = source.getOverallScore();
        }

        public Builder withResponseTimeScore(double responseTimeScore) {
            this.responseTimeScore = responseTimeScore;
            return this;
        }

        public Builder withThroughputScore(double throughputScore) {
            this.throughputScore = throughputScore;
            return this;
        }

        public Builder withSuccessRateScore(double successRateScore) {
            this.successRateScore = successRateScore;
            return this;
        }

        public Builder withAvailabilityScore(double availabilityScore) {
            this.availabilityScore = availabilityScore;
            return this;
        }

        public Builder withOverallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public AgentModelPerformanceEvaluation build() {
            if (responseTimeScore < 0 || responseTimeScore > 1) {
                throw new IllegalArgumentException("responseTimeScore must be between 0 and 1");
            }
            if (throughputScore < 0 || throughputScore > 1) {
                throw new IllegalArgumentException("throughputScore must be between 0 and 1");
            }
            if (successRateScore < 0 || successRateScore > 1) {
                throw new IllegalArgumentException("successRateScore must be between 0 and 1");
            }
            if (availabilityScore < 0 || availabilityScore > 1) {
                throw new IllegalArgumentException("availabilityScore must be between 0 and 1");
            }
            // overallScore validation is handled by AbstractEvaluation constructor
            return new AgentModelPerformanceEvaluation(this);
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
        AgentModelPerformanceEvaluation other = (AgentModelPerformanceEvaluation) obj;
        return Double.compare(responseTimeScore, other.responseTimeScore) == 0
                && Double.compare(throughputScore, other.throughputScore) == 0
                && Double.compare(successRateScore, other.successRateScore) == 0
                && Double.compare(availabilityScore, other.availabilityScore) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), responseTimeScore, throughputScore, successRateScore, availabilityScore);
    }

    @Override
    public String toString() {
        return "AgentModelPerformanceEvaluation{" + "responseTimeScore=" + responseTimeScore + ", throughputScore="
                + throughputScore + ", successRateScore=" + successRateScore + ", availabilityScore="
                + availabilityScore + ", overallScore=" + getOverallScore() + '}';
    }
}
