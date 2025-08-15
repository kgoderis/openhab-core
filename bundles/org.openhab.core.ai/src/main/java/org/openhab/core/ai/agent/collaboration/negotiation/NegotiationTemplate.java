package org.openhab.core.ai.agent.collaboration.negotiation;

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

    /* package */ NegotiationTemplate(NegotiationTemplateBuilder builder) {
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

    public static NegotiationTemplateBuilder builder() {
        return new NegotiationTemplateBuilder();
    }
}
