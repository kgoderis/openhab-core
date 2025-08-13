package org.openhab.core.ai.agent.collaboration.coordination.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for coordination results.
 *
 * This interface defines the contract for coordination results that indicate
 * the success or failure of coordination operations and provide result data.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface CoordinationResult {

    /**
     * Check if the coordination was successful.
     *
     * @return true if successful, false otherwise
     */
    boolean isSuccess();

    /**
     * Get the result message.
     *
     * @return the result message
     */
    String getMessage();

    /**
     * Get the result data.
     *
     * @return the result data
     */
    Map<String, Object> getData();
}
