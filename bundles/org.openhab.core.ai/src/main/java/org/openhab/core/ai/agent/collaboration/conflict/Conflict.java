package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Conflict {
    private final String conflictId;
    private final ConflictData conflictData;
    private final String reportedBy;
    private final Instant detectedAt;
    private final ConflictAnalysis analysis;
    private ConflictStatus status;
    private ConflictPriority priority;
    private String resolutionStrategy;
    private String mediator;
    private ConflictResolution resolution;
    private Instant resolvedAt;
    private ConflictEscalation escalation;

    Conflict(ConflictBuilder builder) {
        this.conflictId = builder.conflictId;
        this.conflictData = builder.conflictData;
        this.reportedBy = builder.reportedBy;
        this.detectedAt = builder.detectedAt;
        this.analysis = builder.analysis;
        this.status = builder.status;
        this.priority = builder.priority;
    }

    public String getConflictId() {
        return conflictId;
    }

    public ConflictData getConflictData() {
        return conflictData;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public ConflictAnalysis getAnalysis() {
        return analysis;
    }

    public ConflictStatus getStatus() {
        return status;
    }

    public ConflictPriority getPriority() {
        return priority;
    }

    public String getResolutionStrategy() {
        return resolutionStrategy;
    }

    public String getMediator() {
        return mediator;
    }

    public ConflictResolution getResolution() {
        return resolution;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public ConflictEscalation getEscalation() {
        return escalation;
    }

    public void setStatus(ConflictStatus status) {
        this.status = status;
    }

    public void setResolutionStrategy(String resolutionStrategy) {
        this.resolutionStrategy = resolutionStrategy;
    }

    public void setMediator(String mediator) {
        this.mediator = mediator;
    }

    public void setResolution(ConflictResolution resolution) {
        this.resolution = resolution;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setEscalation(ConflictEscalation escalation) {
        this.escalation = escalation;
    }

    public static ConflictBuilder builder() {
        return new ConflictBuilder();
    }
}
