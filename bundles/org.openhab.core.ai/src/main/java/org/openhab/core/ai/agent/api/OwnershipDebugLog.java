package org.openhab.core.ai.agent.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface OwnershipDebugLog {
    String getAgentId();
    String getOperation();
    long getTimestamp();
    String getMessage();
    Map<String, Object> getData();
}


