package org.openhab.core.ai.action.library.events.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for SSE sinks.
 *
 * This interface defines the contract for Server-Sent Events (SSE) sinks
 * that can receive and forward events to connected clients.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface SSESink {

    /**
     * Send data to the SSE client.
     * 
     * @param data The data to send
     * @throws IOException if sending fails
     */
    void send(String data) throws IOException;

    /**
     * Check if the sink is still open.
     * 
     * @return true if open, false if closed
     */
    boolean isOpen();
}
