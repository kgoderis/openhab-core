package org.openhab.core.ai.action;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ContextDebugLog {
    String getContextId();
    String getOperation();
    long getTimestamp();
    String getMessage();
    Map<String, Object> getData();
}


