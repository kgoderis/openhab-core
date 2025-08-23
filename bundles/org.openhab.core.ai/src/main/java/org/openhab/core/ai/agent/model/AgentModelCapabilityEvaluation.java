package org.openhab.core.ai.agent.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.evaluation.AbstractEvaluation;

/**
 * Capability evaluation result for an agent model.
 * 
 * <p>
 * This class represents the capability evaluation for an agent model,
 * including required capabilities, supported capabilities, and capability scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelCapabilityEvaluation extends AbstractEvaluation {

    private final Set<String> requiredCapabilities;
    private final Set<String> supportedCapabilities;
    private final double capabilityScore;

    private AgentModelCapabilityEvaluation(Builder b) {
        super(b.overallScore, "CAPABILITY");
        this.requiredCapabilities = Set.copyOf(b.requiredCapabilities);
        this.supportedCapabilities = Set.copyOf(b.supportedCapabilities);
        this.capabilityScore = b.capabilityScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public Set<String> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    public double getCapabilityScore() {
        return capabilityScore;
    }

    public boolean hasRequiredCapability(String capability) {
        return requiredCapabilities.contains(capability);
    }

    public boolean hasSupportedCapability(String capability) {
        return supportedCapabilities.contains(capability);
    }

    public List<String> getMissingCapabilities() {
        return requiredCapabilities.stream().filter(capability -> !supportedCapabilities.contains(capability)).toList();
    }

    public List<String> getExtraCapabilities() {
        return supportedCapabilities.stream().filter(capability -> !requiredCapabilities.contains(capability)).toList();
    }

    public static final class Builder {
        private Set<String> requiredCapabilities = Set.of();
        private Set<String> supportedCapabilities = Set.of();
        private double capabilityScore = 0.0;
        private double overallScore = 0.0;

        public Builder() {
        }

        public Builder(AgentModelCapabilityEvaluation source) {
            this.requiredCapabilities = source.requiredCapabilities;
            this.supportedCapabilities = source.supportedCapabilities;
            this.capabilityScore = source.capabilityScore;
            this.overallScore = source.getOverallScore();
        }

        public Builder withRequiredCapabilities(Set<String> requiredCapabilities) {
            this.requiredCapabilities = Objects.requireNonNull(requiredCapabilities, "requiredCapabilities");
            return this;
        }

        public Builder withSupportedCapabilities(Set<String> supportedCapabilities) {
            this.supportedCapabilities = Objects.requireNonNull(supportedCapabilities, "supportedCapabilities");
            return this;
        }

        public Builder withCapabilityScore(double capabilityScore) {
            this.capabilityScore = capabilityScore;
            return this;
        }

        public Builder withOverallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public AgentModelCapabilityEvaluation build() {
            if (capabilityScore < 0 || capabilityScore > 1) {
                throw new IllegalArgumentException("capabilityScore must be between 0 and 1");
            }
            // overallScore validation is handled by AbstractEvaluation constructor
            return new AgentModelCapabilityEvaluation(this);
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
        AgentModelCapabilityEvaluation other = (AgentModelCapabilityEvaluation) obj;
        return Double.compare(capabilityScore, other.capabilityScore) == 0
                && Objects.equals(requiredCapabilities, other.requiredCapabilities)
                && Objects.equals(supportedCapabilities, other.supportedCapabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requiredCapabilities, supportedCapabilities, capabilityScore);
    }

    @Override
    public String toString() {
        return "AgentModelCapabilityEvaluation{" + "requiredCapabilities=" + requiredCapabilities
                + ", supportedCapabilities=" + supportedCapabilities + ", capabilityScore=" + capabilityScore
                + ", overallScore=" + getOverallScore() + '}';
    }
}
