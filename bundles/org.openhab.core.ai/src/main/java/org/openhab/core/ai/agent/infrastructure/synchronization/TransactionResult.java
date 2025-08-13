package org.openhab.core.ai.agent.infrastructure.synchronization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Transaction result value object for synchronization operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class TransactionResult {
    private final boolean success;
    private final String message;
    private final List<TaskStatusUpdateEvent> responses;

    public TransactionResult(boolean success, String message, List<TaskStatusUpdateEvent> responses) {
        this.success = success;
        this.message = message;
        this.responses = new ArrayList<>(responses);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<TaskStatusUpdateEvent> getResponses() { return Collections.unmodifiableList(responses); }
}


