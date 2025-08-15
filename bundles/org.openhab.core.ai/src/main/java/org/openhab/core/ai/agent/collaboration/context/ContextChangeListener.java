package org.openhab.core.ai.agent.collaboration.context;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Listener for shared context changes.
 */
@NonNullByDefault
public interface ContextChangeListener {
    void onContextChange(String contextId, ContextChangeType changeType, String agentId,
            @Nullable Map<String, Object> data);
}
