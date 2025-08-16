package org.openhab.core.ai.action.library.automation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Workflow execution state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class WorkflowExecution {
    final String executionId;
    final String workflowId;
    final Map<?, ?> parameters;
    final Instant startTime;
    String status;
    @Nullable
    Instant endTime;
    @Nullable
    String error;

    WorkflowExecution(String workflowId, Map<?, ?> parameters) {
        this.executionId = UUID.randomUUID().toString();
        this.workflowId = workflowId;
        this.parameters = parameters;
        this.startTime = Instant.now();
        this.status = "pending";
    }
}
