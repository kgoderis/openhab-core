package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConflictResolutionConfiguration {
    private boolean enableAutoEscalation = true;
    private Duration autoEscalationDelay = Duration.ofMinutes(30);

    public ConflictResolutionConfiguration() {
    }

    public boolean isEnableAutoEscalation() {
        return enableAutoEscalation;
    }

    public void setEnableAutoEscalation(boolean enableAutoEscalation) {
        this.enableAutoEscalation = enableAutoEscalation;
    }

    public Duration getAutoEscalationDelay() {
        return autoEscalationDelay;
    }

    public void setAutoEscalationDelay(Duration autoEscalationDelay) {
        this.autoEscalationDelay = autoEscalationDelay;
    }
}
