package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link Conflict}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictBuilder {
    String conflictId;
    ConflictData conflictData;
    String reportedBy;
    Instant detectedAt;
    ConflictAnalysis analysis;
    ConflictStatus status;
    ConflictPriority priority;

    public ConflictBuilder conflictId(String conflictId) { this.conflictId = conflictId; return this; }
    public ConflictBuilder conflictData(ConflictData conflictData) { this.conflictData = conflictData; return this; }
    public ConflictBuilder reportedBy(String reportedBy) { this.reportedBy = reportedBy; return this; }
    public ConflictBuilder detectedAt(Instant detectedAt) { this.detectedAt = detectedAt; return this; }
    public ConflictBuilder analysis(ConflictAnalysis analysis) { this.analysis = analysis; return this; }
    public ConflictBuilder status(ConflictStatus status) { this.status = status; return this; }
    public ConflictBuilder priority(ConflictPriority priority) { this.priority = priority; return this; }

    public Conflict build() {
        return Conflict.builder()
                .conflictId(conflictId)
                .conflictData(conflictData)
                .reportedBy(reportedBy)
                .detectedAt(detectedAt)
                .analysis(analysis)
                .status(status)
                .priority(priority)
                .build();
    }
}


