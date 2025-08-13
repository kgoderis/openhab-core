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

	private SharedContext(Builder builder) {
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

	public Builder toBuilder() {
		return new Builder().contextId(contextId).agentIds(agentIds).contextData(contextData)
				.accessLevel(accessLevel).createdTime(createdTime).lastModified(lastModified).version(version);
	}

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private String contextId;
		private List<String> agentIds;
		private Map<String, Object> contextData;
		private ContextAccessLevel accessLevel;
		private Instant createdTime;
		private Instant lastModified;
		private int version;

		public Builder contextId(String contextId) { this.contextId = contextId; return this; }
		public Builder agentIds(List<String> agentIds) { this.agentIds = agentIds; return this; }
		public Builder contextData(Map<String, Object> contextData) { this.contextData = contextData; return this; }
		public Builder accessLevel(ContextAccessLevel accessLevel) { this.accessLevel = accessLevel; return this; }
		public Builder createdTime(Instant createdTime) { this.createdTime = createdTime; return this; }
		public Builder lastModified(Instant lastModified) { this.lastModified = lastModified; return this; }
		public Builder version(int version) { this.version = version; return this; }
		public SharedContext build() { return new SharedContext(this); }
	}
}
