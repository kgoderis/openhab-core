package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines the structure and parameters for negotiation sessions
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class NegotiationTemplate {

    private final String templateId;
    private final String name;
    private final String description;
    private final Duration timeout;
    private final int maxRounds;

    private NegotiationTemplate(Builder builder) {
        this.templateId = builder.templateId;
        this.name = builder.name;
        this.description = builder.description;
        this.timeout = builder.timeout;
        this.maxRounds = builder.maxRounds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NegotiationTemplate that = (NegotiationTemplate) o;
        return maxRounds == that.maxRounds && templateId.equals(that.templateId) && name.equals(that.name)
                && description.equals(that.description) && timeout.equals(that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId, name, description, timeout, maxRounds);
    }

    @Override
    public String toString() {
        return "NegotiationTemplate [templateId=" + templateId + ", name=" + name + ", description=" + description
                + ", timeout=" + timeout + ", maxRounds=" + maxRounds + "]";
    }

    public static final class Builder {
        private String templateId = "";
        private String name = "";
        private String description = "";
        private Duration timeout = Duration.ofMinutes(5);
        private int maxRounds = 3;

        public Builder() {
        }

        public Builder(NegotiationTemplate source) {
            this.templateId = source.templateId;
            this.name = source.name;
            this.description = source.description;
            this.timeout = source.timeout;
            this.maxRounds = source.maxRounds;
        }

        public Builder withTemplateId(String templateId) {
            this.templateId = Objects.requireNonNull(templateId, "templateId");
            return this;
        }

        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder withDescription(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        public Builder withTimeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder withMaxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
            return this;
        }

        public NegotiationTemplate build() {
            if (templateId.isBlank()) {
                throw new IllegalArgumentException("templateId must not be blank");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (maxRounds <= 0) {
                throw new IllegalArgumentException("maxRounds must be > 0");
            }
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("timeout must be positive");
            }
            return new NegotiationTemplate(this);
        }
    }
}
