package org.openhab.core.ai.agent.api;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentOwnership {
    String getAgentId();
    Set<String> getOwners();
    String getPrimaryOwner();
    long getCreatedAt();
    long getLastModified();
    Map<String, Object> getMetadata();
}


