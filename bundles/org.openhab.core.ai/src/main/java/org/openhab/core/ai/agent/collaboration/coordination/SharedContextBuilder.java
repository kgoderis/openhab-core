package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link SharedContext}.
 */
@NonNullByDefault
public class SharedContextBuilder {
    private String contextId;
    private List<String> agentIds;
    private Map<String, Object> contextData;
    private ContextAccessLevel accessLevel;
    private Instant createdTime;
    private Instant lastModified;
    private int version;

    public SharedContextBuilder contextId(String contextId) { this.contextId = contextId; return this; }
    public SharedContextBuilder agentIds(List<String> agentIds) { this.agentIds = agentIds; return this; }
    public SharedContextBuilder contextData(Map<String, Object> contextData) { this.contextData = contextData; return this; }
    public SharedContextBuilder accessLevel(ContextAccessLevel accessLevel) { this.accessLevel = accessLevel; return this; }
    public SharedContextBuilder createdTime(Instant createdTime) { this.createdTime = createdTime; return this; }
    public SharedContextBuilder lastModified(Instant lastModified) { this.lastModified = lastModified; return this; }
    public SharedContextBuilder version(int version) { this.version = version; return this; }

    public SharedContext build() {
        return SharedContext.builder()
                .contextId(contextId)
                .agentIds(agentIds)
                .contextData(contextData)
                .accessLevel(accessLevel)
                .createdTime(createdTime)
                .lastModified(lastModified)
                .version(version)
                .build();
    }
}


