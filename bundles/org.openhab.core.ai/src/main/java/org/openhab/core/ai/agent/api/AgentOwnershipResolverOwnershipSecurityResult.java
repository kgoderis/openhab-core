package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentOwnershipResolverOwnershipSecurityResult {
    boolean isAllowed();
    String getMessage();
    List<String> getRequiredPermissions();
    Map<String, Object> getSecurityContext();
}


