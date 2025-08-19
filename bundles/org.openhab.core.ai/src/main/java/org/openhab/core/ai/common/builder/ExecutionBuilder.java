package org.openhab.core.ai.common.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for execution-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating execution-related objects
 * such as ExecutionRequest, ExecutionContext, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ExecutionBuilder<T> extends AbstractBuilder<T> {

    protected @Nullable Object type;
    protected String targetName = "";
    protected Map<String, Object> parameters = new HashMap<>();
    protected @Nullable Object priority;
    protected Map<String, Object> context = new HashMap<>();
    protected boolean requiresValidation = true;
    protected boolean requiresSafetyChecks = true;
    protected String executionId = "";
    protected @Nullable String correlationId;
    protected long timeoutMs = 30000;
    protected boolean async = false;
    protected Map<String, Object> executionOptions = new HashMap<>();

    /**
     * Set the execution type.
     *
     * @param type the execution type
     * @return this builder
     */
    public ExecutionBuilder<T> withType(@Nullable Object type) {
        this.type = type;
        return this;
    }

    /**
     * Set the target name.
     *
     * @param targetName the target name
     * @return this builder
     */
    public ExecutionBuilder<T> withTargetName(String targetName) {
        this.targetName = Objects.requireNonNull(targetName, "targetName");
        return this;
    }

    /**
     * Set the parameters.
     *
     * @param parameters the parameters
     * @return this builder
     */
    public ExecutionBuilder<T> withParameters(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(Objects.requireNonNull(parameters, "parameters"));
        return this;
    }

    /**
     * Add a parameter.
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return this builder
     */
    public ExecutionBuilder<T> withParameter(String key, Object value) {
        this.parameters.put(Objects.requireNonNull(key, "key"), value);
        return this;
    }

    /**
     * Set the priority.
     *
     * @param priority the priority
     * @return this builder
     */
    public ExecutionBuilder<T> withPriority(@Nullable Object priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Set the context.
     *
     * @param context the context
     * @return this builder
     */
    public ExecutionBuilder<T> withContext(Map<String, Object> context) {
        this.context = new HashMap<>(Objects.requireNonNull(context, "context"));
        return this;
    }

    /**
     * Add a context entry.
     *
     * @param key the context key
     * @param value the context value
     * @return this builder
     */
    public ExecutionBuilder<T> withContext(String key, Object value) {
        this.context.put(Objects.requireNonNull(key, "key"), value);
        return this;
    }

    /**
     * Set whether validation is required.
     *
     * @param requiresValidation true if validation is required, false otherwise
     * @return this builder
     */
    public ExecutionBuilder<T> withRequiresValidation(boolean requiresValidation) {
        this.requiresValidation = requiresValidation;
        return this;
    }

    /**
     * Set whether safety checks are required.
     *
     * @param requiresSafetyChecks true if safety checks are required, false otherwise
     * @return this builder
     */
    public ExecutionBuilder<T> withRequiresSafetyChecks(boolean requiresSafetyChecks) {
        this.requiresSafetyChecks = requiresSafetyChecks;
        return this;
    }

    /**
     * Set the execution ID.
     *
     * @param executionId the execution ID
     * @return this builder
     */
    public ExecutionBuilder<T> withExecutionId(String executionId) {
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        return this;
    }

    /**
     * Set the correlation ID.
     *
     * @param correlationId the correlation ID
     * @return this builder
     */
    public ExecutionBuilder<T> withCorrelationId(@Nullable String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Set the timeout in milliseconds.
     *
     * @param timeoutMs the timeout in milliseconds
     * @return this builder
     */
    public ExecutionBuilder<T> withTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Set whether execution is asynchronous.
     *
     * @param async true if asynchronous, false otherwise
     * @return this builder
     */
    public ExecutionBuilder<T> withAsync(boolean async) {
        this.async = async;
        return this;
    }

    /**
     * Set the execution options.
     *
     * @param executionOptions the execution options
     * @return this builder
     */
    public ExecutionBuilder<T> withExecutionOptions(Map<String, Object> executionOptions) {
        this.executionOptions = new HashMap<>(Objects.requireNonNull(executionOptions, "executionOptions"));
        return this;
    }

    /**
     * Add an execution option.
     *
     * @param key the option key
     * @param value the option value
     * @return this builder
     */
    public ExecutionBuilder<T> withExecutionOption(String key, Object value) {
        this.executionOptions.put(Objects.requireNonNull(key, "key"), value);
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ExecutionBuilder
        if (targetName.isBlank()) {
            throw new IllegalArgumentException("targetName must not be blank");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        type = null;
        targetName = "";
        parameters = new HashMap<>();
        priority = null;
        context = new HashMap<>();
        requiresValidation = true;
        requiresSafetyChecks = true;
        executionId = "";
        correlationId = null;
        timeoutMs = 30000;
        async = false;
        executionOptions = new HashMap<>();
    }
}
