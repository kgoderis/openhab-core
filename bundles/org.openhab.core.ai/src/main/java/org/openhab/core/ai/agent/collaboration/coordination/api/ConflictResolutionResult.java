package org.openhab.core.ai.agent.collaboration.coordination.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for conflict resolution results.
 *
 * This interface defines the contract for conflict resolution results that indicate
 * the success or failure of conflict resolution operations and provide resolution data.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConflictResolutionResult {

    /**
     * Check if the conflict resolution was successful.
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
     * Get the resolution data.
     *
     * @return the resolution data
     */
    Map<String, Object> getResolution();

    /**
     * Create a successful conflict resolution result.
     *
     * @param message the success message
     * @return a successful conflict resolution result
     */
    static ConflictResolutionResult success(String message) {
        return new ConflictResolutionResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Map<String, Object> getResolution() {
                return Map.of();
            }
        };
    }

    /**
     * Create a failed conflict resolution result.
     *
     * @param message the failure message
     * @return a failed conflict resolution result
     */
    static ConflictResolutionResult failure(String message) {
        return new ConflictResolutionResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Map<String, Object> getResolution() {
                return Map.of();
            }
        };
    }
}
