package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.response.MessageResponse;

/**
 * Message handler interface for agents
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MessageHandler {
    /**
     * Handle an incoming message.
     * 
     * @param message the message to handle
     * @return response indicating if message was processed successfully
     */
    MessageResponse handleMessage(AgentMessage message);
}
