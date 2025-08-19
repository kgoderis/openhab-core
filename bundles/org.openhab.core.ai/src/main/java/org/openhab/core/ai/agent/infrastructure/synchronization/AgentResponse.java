package org.openhab.core.ai.agent.infrastructure.synchronization;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.response.Response;

/**
 * Agent response representation used by synchronization manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentResponse implements Response<Object> {
    private final String taskId;
    private final boolean success;
    private final String message;
    private final @Nullable Object data;

    public AgentResponse(String taskId, boolean success, String message, @Nullable Object data) {
        this.taskId = taskId;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public String getTaskId() {
        return taskId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public @Nullable Object getData() {
        return data;
    }

    // Response interface implementation
    @Override
    public String getId() {
        return taskId;
    }

    @Override
    public String getErrorMessage() {
        return success ? null : message;
    }

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }
}
