package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class NegotiationTemplateBuilder {
    String templateId = "";
    String name = "";
    String description = "";
    Duration timeout = Duration.ofMinutes(5);
    int maxRounds = 3;

    public NegotiationTemplateBuilder templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public NegotiationTemplateBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NegotiationTemplateBuilder description(String description) {
        this.description = description;
        return this;
    }

    public NegotiationTemplateBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public NegotiationTemplateBuilder maxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
        return this;
    }

    public NegotiationTemplate build() {
        return new NegotiationTemplate(this);
    }
}
