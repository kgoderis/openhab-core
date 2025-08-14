package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Shared context for coordinating agents.
 *
 * Holds participating agent IDs, context data, access level and audit metadata.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class SharedContext {
	private final String contextId;
	private final List<String> agentIds;
	private final Map<String, Object> contextData;
	private final ContextAccessLevel accessLevel;
	private final Instant createdTime;
	private Instant lastModified;
	private int version;

    SharedContext(SharedContextBuilder builder) {
		this.contextId = builder.contextId;
		this.agentIds = builder.agentIds;
		this.contextData = builder.contextData;
		this.accessLevel = builder.accessLevel;
		this.createdTime = builder.createdTime;
		this.lastModified = builder.lastModified;
		this.version = builder.version;
	}

	public String getContextId() { return contextId; }
	public List<String> getAgentIds() { return agentIds; }
	public Map<String, Object> getContextData() { return contextData; }
	public ContextAccessLevel getAccessLevel() { return accessLevel; }
	public Instant getCreatedTime() { return createdTime; }
	public Instant getLastModified() { return lastModified; }
	public int getVersion() { return version; }

    public SharedContextBuilder toBuilder() {
        return new SharedContextBuilder().contextId(contextId).agentIds(agentIds).contextData(contextData)
				.accessLevel(accessLevel).createdTime(createdTime).lastModified(lastModified).version(version);
	}

    public static SharedContextBuilder builder() { return new SharedContextBuilder(); }
}
