package org.openhab.core.ai.agent.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.evaluation.AbstractEvaluation;

/**
 * Quality evaluation result for an agent model.
 * 
 * <p>
 * This class represents the quality evaluation for an agent model,
 * including accuracy, consistency, and reliability scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelQualityEvaluation extends AbstractEvaluation {

    private final double accuracyScore;
    private final double consistencyScore;
    private final double reliabilityScore;

    private AgentModelQualityEvaluation(Builder b) {
        super(b.overallScore, "QUALITY");
        this.accuracyScore = b.accuracyScore;
        this.consistencyScore = b.consistencyScore;
        this.reliabilityScore = b.reliabilityScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public double getAccuracyScore() {
        return accuracyScore;
    }

    public double getConsistencyScore() {
        return consistencyScore;
    }

    public double getReliabilityScore() {
        return reliabilityScore;
    }

    public static final class Builder {
        private double accuracyScore = 0.0;
        private double consistencyScore = 0.0;
        private double reliabilityScore = 0.0;
        private double overallScore = 0.0;

        public Builder() {
        }

        public Builder(AgentModelQualityEvaluation source) {
            this.accuracyScore = source.accuracyScore;
            this.consistencyScore = source.consistencyScore;
            this.reliabilityScore = source.reliabilityScore;
            this.overallScore = source.getOverallScore();
        }

        public Builder withAccuracyScore(double accuracyScore) {
            this.accuracyScore = accuracyScore;
            return this;
        }

        public Builder withConsistencyScore(double consistencyScore) {
            this.consistencyScore = consistencyScore;
            return this;
        }

        public Builder withReliabilityScore(double reliabilityScore) {
            this.reliabilityScore = reliabilityScore;
            return this;
        }

        public Builder withOverallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public AgentModelQualityEvaluation build() {
            if (accuracyScore < 0 || accuracyScore > 1) {
                throw new IllegalArgumentException("accuracyScore must be between 0 and 1");
            }
            if (consistencyScore < 0 || consistencyScore > 1) {
                throw new IllegalArgumentException("consistencyScore must be between 0 and 1");
            }
            if (reliabilityScore < 0 || reliabilityScore > 1) {
                throw new IllegalArgumentException("reliabilityScore must be between 0 and 1");
            }
            // overallScore validation is handled by AbstractEvaluation constructor
            return new AgentModelQualityEvaluation(this);
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
        AgentModelQualityEvaluation other = (AgentModelQualityEvaluation) obj;
        return Double.compare(accuracyScore, other.accuracyScore) == 0
                && Double.compare(consistencyScore, other.consistencyScore) == 0
                && Double.compare(reliabilityScore, other.reliabilityScore) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accuracyScore, consistencyScore, reliabilityScore);
    }

    @Override
    public String toString() {
        return "AgentModelQualityEvaluation{" + "accuracyScore=" + accuracyScore + ", consistencyScore="
                + consistencyScore + ", reliabilityScore=" + reliabilityScore + ", overallScore=" + getOverallScore()
                + '}';
    }
}
