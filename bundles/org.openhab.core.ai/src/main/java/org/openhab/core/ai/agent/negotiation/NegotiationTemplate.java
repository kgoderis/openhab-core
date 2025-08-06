package org.openhab.core.ai.agent.negotiation;

import java.time.Duration;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String templateId = "";
        private String name = "";
        private String description = "";
        private Duration timeout = Duration.ofMinutes(5);
        private int maxRounds = 3;

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
            return this;
        }

        public NegotiationTemplate build() {
            return new NegotiationTemplate(this);
        }
    }
}
