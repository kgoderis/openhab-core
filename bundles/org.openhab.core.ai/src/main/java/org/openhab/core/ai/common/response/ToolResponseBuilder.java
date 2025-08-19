package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link ToolResponse} in the unified response hierarchy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ToolResponseBuilder extends AbstractBuilder<ToolResponse> {

    private @Nullable String id;
    private @Nullable Object data;
    private long timestamp = System.currentTimeMillis();
    private String toolId = "";
    private String operation = "";
    private long executionTimeMs = 0L;
    private Map<String, Object> metadata = Map.of();
    private @Nullable String errorMessage;

    public ToolResponseBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public ToolResponseBuilder withData(@Nullable Object data) {
        this.data = data;
        return this;
    }

    public ToolResponseBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ToolResponseBuilder withToolId(String toolId) {
        this.toolId = Objects.requireNonNull(toolId, "toolId");
        return this;
    }

    public ToolResponseBuilder withOperation(String operation) {
        this.operation = Objects.requireNonNull(operation, "operation");
        return this;
    }

    public ToolResponseBuilder withExecutionTimeMs(long ms) {
        this.executionTimeMs = ms;
        return this;
    }

    public ToolResponseBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        return this;
    }

    public ToolResponseBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(toolId, "toolId");
        validateRequiredString(operation, "operation");
        if (executionTimeMs < 0) {
            addValidationError("executionTimeMs must be >= 0");
        }
    }

    @Override
    protected void doReset() {
        id = null;
        data = null;
        timestamp = System.currentTimeMillis();
        toolId = "";
        operation = "";
        executionTimeMs = 0L;
        metadata = Map.of();
        errorMessage = null;
    }

    @Override
    public ToolResponse build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid ToolResponseBuilder state: " + getValidationErrors());
        }
        String resolvedId = id != null ? id : ("tool-response-" + System.currentTimeMillis() + "-" + System.nanoTime());
        return new ToolResponse(resolvedId, data, timestamp, toolId, operation, executionTimeMs, metadata,
                errorMessage);
    }
}
