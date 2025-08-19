package org.openhab.core.ai.common.builder;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.ExecutionRequest;

/**
 * Builder for ExecutionRequest.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ExecutionRequestBuilder extends ExecutionBuilder<ExecutionRequest> {

    private String requestId = "";
    private String agentId = "";
    private String taskDescription = "";
    private @Nullable Map<String, Object> parameters;
    private @Nullable String priority;
    private boolean requiresValidation = true;
    private boolean requiresSafetyChecks = true;
    private @Nullable String correlationId;
    private long timeoutMs = 30000;

    /**
     * Create a new ExecutionRequestBuilder with the given request ID.
     * 
     * @param requestId the request ID
     */
    public ExecutionRequestBuilder(String requestId) {
        super();
        this.requestId = Objects.requireNonNull(requestId, "Request ID cannot be null");
    }

    /**
     * Set the request ID.
     * 
     * @param requestId the request ID
     * @return this builder
     */
    public ExecutionRequestBuilder withRequestId(String requestId) {
        this.requestId = Objects.requireNonNull(requestId, "Request ID cannot be null");
        return this;
    }

    /**
     * Set the agent ID.
     * 
     * @param agentId the agent ID
     * @return this builder
     */
    public ExecutionRequestBuilder withAgentId(String agentId) {
        this.agentId = Objects.requireNonNull(agentId, "Agent ID cannot be null");
        return this;
    }

    /**
     * Set the task description.
     * 
     * @param taskDescription the task description
     * @return this builder
     */
    public ExecutionRequestBuilder withTaskDescription(String taskDescription) {
        this.taskDescription = Objects.requireNonNull(taskDescription, "Task description cannot be null");
        return this;
    }

    /**
     * Set the parameters.
     * 
     * @param parameters the parameters
     * @return this builder
     */
    public ExecutionRequestBuilder withParameters(@Nullable Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Set the priority.
     * 
     * @param priority the priority
     * @return this builder
     */
    public ExecutionRequestBuilder withPriority(@Nullable String priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Set whether validation is required.
     * 
     * @param requiresValidation whether validation is required
     * @return this builder
     */
    public ExecutionRequestBuilder withRequiresValidation(boolean requiresValidation) {
        this.requiresValidation = requiresValidation;
        return this;
    }

    /**
     * Set whether safety checks are required.
     * 
     * @param requiresSafetyChecks whether safety checks are required
     * @return this builder
     */
    public ExecutionRequestBuilder withRequiresSafetyChecks(boolean requiresSafetyChecks) {
        this.requiresSafetyChecks = requiresSafetyChecks;
        return this;
    }

    /**
     * Set the correlation ID.
     * 
     * @param correlationId the correlation ID
     * @return this builder
     */
    public ExecutionRequestBuilder withCorrelationId(@Nullable String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Set the timeout in milliseconds.
     * 
     * @param timeoutMs the timeout in milliseconds
     * @return this builder
     */
    public ExecutionRequestBuilder withTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    @Override
    public ExecutionRequest build() {
        validate();
        return new ExecutionRequest(this);
    }

    @Override
    protected void validate() {
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID cannot be blank");
        }
        if (agentId.isBlank()) {
            throw new IllegalArgumentException("Agent ID cannot be blank");
        }
        if (taskDescription.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be blank");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
    }

    @Override
    protected void doReset() {
        requestId = "";
        agentId = "";
        taskDescription = "";
        parameters = null;
        priority = null;
        requiresValidation = true;
        requiresSafetyChecks = true;
        correlationId = null;
        timeoutMs = 30000;
    }

    // Getter methods for ExecutionRequest constructor
    public String getRequestId() {
        return requestId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public @Nullable Map<String, Object> getParameters() {
        return parameters;
    }

    public @Nullable String getPriority() {
        return priority;
    }

    public boolean isRequiresValidation() {
        return requiresValidation;
    }

    public boolean isRequiresSafetyChecks() {
        return requiresSafetyChecks;
    }

    public @Nullable String getCorrelationId() {
        return correlationId;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
