package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConflictResolution {
    private final String resolutionId;
    private final String strategy;
    private final String mediator;
    private final ResolutionOutcome outcome;
    private final String description;
    private final Map<String, Object> details;
    private final Instant resolvedAt;

    public ConflictResolution(String resolutionId, String strategy, String mediator, ResolutionOutcome outcome,
            String description, Map<String, Object> details, Instant resolvedAt) {
        this.resolutionId = resolutionId;
        this.strategy = strategy;
        this.mediator = mediator;
        this.outcome = outcome;
        this.description = description;
        this.details = details;
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionId() { return resolutionId; }
    public String getStrategy() { return strategy; }
    public String getMediator() { return mediator; }
    public ResolutionOutcome getOutcome() { return outcome; }
    public String getDescription() { return description; }
    public Map<String, Object> getDetails() { return details; }
    public Instant getResolvedAt() { return resolvedAt; }
}


