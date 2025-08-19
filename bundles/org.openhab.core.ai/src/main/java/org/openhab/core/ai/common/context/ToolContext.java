package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context for MCP tool execution operations.
 * 
 * <p>
 * This class provides a unified context for tool-related operations:
 * - Tool execution context management
 * - Protocol-specific tool information
 * - Tool execution metadata
 * - Thread-safe context access
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolContext extends BaseContext {

    private final String toolId;
    private final String toolName;
    private final String toolVersion;
    private final @Nullable String clientId;
    private final @Nullable String sessionId;
    private final long executionStartTime;

    /**
     * Create a new tool context.
     * 
     * @param contextId the unique context identifier
     * @param toolId the tool identifier
     * @param toolName the tool name
     * @param toolVersion the tool version
     * @param clientId the client identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     */
    public ToolContext(String contextId, String toolId, String toolName, String toolVersion, @Nullable String clientId,
            @Nullable String sessionId, @Nullable Map<String, Object> values, @Nullable Map<String, Object> metadata) {
        super(contextId, "tool", "1.0.0", values, metadata);
        this.toolId = Objects.requireNonNull(toolId, "Tool ID cannot be null");
        this.toolName = Objects.requireNonNull(toolName, "Tool name cannot be null");
        this.toolVersion = Objects.requireNonNull(toolVersion, "Tool version cannot be null");
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.executionStartTime = System.currentTimeMillis();
    }

    /**
     * Create a new tool context with custom timestamps.
     * 
     * @param contextId the unique context identifier
     * @param toolId the tool identifier
     * @param toolName the tool name
     * @param toolVersion the tool version
     * @param clientId the client identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     * @param createdAt the creation timestamp
     * @param lastModifiedAt the last modification timestamp
     * @param executionStartTime the execution start time
     */
    public ToolContext(String contextId, String toolId, String toolName, String toolVersion, @Nullable String clientId,
            @Nullable String sessionId, @Nullable Map<String, Object> values, @Nullable Map<String, Object> metadata,
            Instant createdAt, Instant lastModifiedAt, long executionStartTime) {
        super(contextId, "tool", "1.0.0", values, metadata, createdAt, lastModifiedAt);
        this.toolId = Objects.requireNonNull(toolId, "Tool ID cannot be null");
        this.toolName = Objects.requireNonNull(toolName, "Tool name cannot be null");
        this.toolVersion = Objects.requireNonNull(toolVersion, "Tool version cannot be null");
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.executionStartTime = executionStartTime;
    }

    /**
     * Get the tool identifier.
     * 
     * @return the tool ID
     */
    public String getToolId() {
        return toolId;
    }

    /**
     * Get the tool name.
     * 
     * @return the tool name
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * Get the tool version.
     * 
     * @return the tool version
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * Get the client identifier.
     * 
     * @return the client ID, or null if not set
     */
    public @Nullable String getClientId() {
        return clientId;
    }

    /**
     * Get the session identifier.
     * 
     * @return the session ID, or null if not set
     */
    public @Nullable String getSessionId() {
        return sessionId;
    }

    /**
     * Get the execution start time.
     * 
     * @return the execution start time in milliseconds
     */
    public long getExecutionStartTime() {
        return executionStartTime;
    }

    /**
     * Get the execution duration.
     * 
     * @return the execution duration in milliseconds
     */
    public long getExecutionDuration() {
        return System.currentTimeMillis() - executionStartTime;
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new ToolContext(getContextId(), toolId, toolName, toolVersion, clientId, sessionId, newValues,
                newMetadata);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        ToolContext other = (ToolContext) obj;
        return Objects.equals(toolId, other.toolId) && Objects.equals(toolName, other.toolName)
                && Objects.equals(toolVersion, other.toolVersion) && Objects.equals(clientId, other.clientId)
                && Objects.equals(sessionId, other.sessionId) && executionStartTime == other.executionStartTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toolId, toolName, toolVersion, clientId, sessionId, executionStartTime);
    }

    @Override
    public String toString() {
        return String.format(
                "ToolContext{id='%s', toolId='%s', toolName='%s', toolVersion='%s', clientId='%s', sessionId='%s', executionTime=%dms}",
                getContextId(), toolId, toolName, toolVersion, clientId, sessionId, getExecutionDuration());
    }
}
