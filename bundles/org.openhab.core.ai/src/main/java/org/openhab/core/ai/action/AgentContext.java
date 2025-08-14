package org.openhab.core.ai.action;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentContext {
    String getAgentId();
    Map<String, Object> getContextData();
    List<String> getAvailableActions();
    Map<String, Object> getEnvironmentState();
    long getTimestamp();
    String getContextId();
}


