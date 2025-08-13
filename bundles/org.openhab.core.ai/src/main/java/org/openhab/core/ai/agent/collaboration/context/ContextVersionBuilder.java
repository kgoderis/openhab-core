package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ContextVersion}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextVersionBuilder {

    private String versionId;
    private String contextId;
    private String agentId;
    private Instant timestamp;
    private Map<String, Object> data;
    private ContextOptions options;

    public ContextVersionBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public ContextVersionBuilder contextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public ContextVersionBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public ContextVersionBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ContextVersionBuilder data(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public ContextVersionBuilder options(ContextOptions options) {
        this.options = options;
        return this;
    }

    public ContextVersion build() {
        return new ContextVersion(this);
    }

    String getVersionId() {
        return versionId;
    }

    String getContextId() {
        return contextId;
    }

    String getAgentId() {
        return agentId;
    }

    Instant getTimestamp() {
        return timestamp;
    }

    Map<String, Object> getData() {
        return data;
    }

    ContextOptions getOptions() {
        return options;
    }
}


