package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Context version snapshot.
 *
 * Represents an immutable snapshot of a context's state at a particular time.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextVersion {

    private final String versionId;
    private final String contextId;
    private final String agentId;
    private final Instant timestamp;
    private final Map<String, Object> data;
    private final ContextOptions options;

    ContextVersion(ContextVersionBuilder builder) {
        this.versionId = builder.getVersionId();
        this.contextId = builder.getContextId();
        this.agentId = builder.getAgentId();
        this.timestamp = builder.getTimestamp();
        this.data = builder.getData();
        this.options = builder.getOptions();
    }

    public String getVersionId() {
        return versionId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ContextOptions getOptions() {
        return options;
    }

    public static ContextVersionBuilder builder() {
        return new ContextVersionBuilder();
    }
}


