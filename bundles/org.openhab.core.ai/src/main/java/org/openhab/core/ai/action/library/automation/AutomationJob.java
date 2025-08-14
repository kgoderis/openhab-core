package org.openhab.core.ai.action.library.automation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Automation job state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class AutomationJob {
    final String jobId;
    final String name;
    final String type;
    final Map<?, ?> parameters;
    final Instant createdTime;
    String status;
    @Nullable
    Instant endTime;
    @Nullable
    String error;

    AutomationJob(String taskId, String name, String type, Map<?, ?> parameters) {
        this.jobId = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.parameters = parameters;
        this.createdTime = Instant.now();
        this.status = "pending";
    }
}


