package org.openhab.core.ai.agent.planning;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of executing a single action in an action plan.
 * 
 * This class represents the outcome of executing an individual action,
 * including success status, execution time, and any error information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelActionExecutionResult {

    private final String actionId;
    private final boolean success;
    private final @Nullable String errorMessage;
    private final long executionTimeMs;
    private final Instant completedAt;
    private final Map<String, Object> output;
    private final Map<String, Object> metadata;

    private AgentModelActionExecutionResult(Builder builder) {
        this.actionId = Objects.requireNonNull(builder.actionId, "actionId");
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
        this.executionTimeMs = builder.executionTimeMs;
        this.completedAt = Objects.requireNonNull(builder.completedAt, "completedAt");
        this.output = Map.copyOf(Objects.requireNonNull(builder.output, "output"));
        this.metadata = Map.copyOf(Objects.requireNonNull(builder.metadata, "metadata"));
    }

    /**
     * Create a new builder.
     * 
     * @param actionId the action ID
     * @return the builder
     */
    public static Builder builder(String actionId) {
        return new Builder(actionId);
    }

    /**
     * Create a success result.
     * 
     * @param actionId the action ID
     * @param executionTimeMs the execution time in milliseconds
     * @return the result
     */
    public static AgentModelActionExecutionResult success(String actionId, long executionTimeMs) {
        return builder(actionId).withSuccess(true).withExecutionTimeMs(executionTimeMs).withCompletedAt(Instant.now())
                .build();
    }

    /**
     * Create an error result.
     * 
     * @param actionId the action ID
     * @param errorMessage the error message
     * @param executionTimeMs the execution time in milliseconds
     * @return the result
     */
    public static AgentModelActionExecutionResult error(String actionId, String errorMessage, long executionTimeMs) {
        return builder(actionId).withSuccess(false).withErrorMessage(errorMessage).withExecutionTimeMs(executionTimeMs)
                .withCompletedAt(Instant.now()).build();
    }

    /**
     * Get the action ID.
     * 
     * @return the action ID
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Check if the execution was successful.
     * 
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the error message if execution failed.
     * 
     * @return the error message or null
     */
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the execution time in milliseconds.
     * 
     * @return the execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Get the completion timestamp.
     * 
     * @return the completion timestamp
     */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Get the action output.
     * 
     * @return the output map
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * Get the metadata.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelActionExecutionResult other = (AgentModelActionExecutionResult) obj;
        return success == other.success && executionTimeMs == other.executionTimeMs
                && Objects.equals(actionId, other.actionId) && Objects.equals(errorMessage, other.errorMessage)
                && Objects.equals(completedAt, other.completedAt) && Objects.equals(output, other.output)
                && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId, success, errorMessage, executionTimeMs, completedAt, output, metadata);
    }

    @Override
    public String toString() {
        return String.format("AgentModelActionExecutionResult{actionId='%s', success=%s, executionTimeMs=%d}", actionId,
                success, executionTimeMs);
    }

    /**
     * Builder for AgentModelActionExecutionResult.
     */
    public static final class Builder {
        private String actionId;
        private boolean success = false;
        private @Nullable String errorMessage;
        private long executionTimeMs = 0;
        private Instant completedAt = Instant.now();
        private Map<String, Object> output = Map.of();
        private Map<String, Object> metadata = Map.of();

        public Builder(String actionId) {
            this.actionId = Objects.requireNonNull(actionId, "actionId");
        }

        public Builder withSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder withCompletedAt(Instant completedAt) {
            this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
            return this;
        }

        public Builder withOutput(Map<String, Object> output) {
            this.output = Objects.requireNonNull(output, "output");
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public AgentModelActionExecutionResult build() {
            return new AgentModelActionExecutionResult(this);
        }
    }
}
