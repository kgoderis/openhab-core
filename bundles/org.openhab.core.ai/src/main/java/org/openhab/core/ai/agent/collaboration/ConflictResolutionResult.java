package org.openhab.core.ai.agent.collaboration;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified interface for conflict resolution results.
 *
 * This interface defines the contract for conflict resolution results that indicate
 * the success or failure of conflict resolution operations and provide resolution data
 * in both generic and specific formats.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
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
     * Get the resolution data as a generic map.
     *
     * @return the resolution data map
     */
    default Map<String, Object> getResolution() {
        return Map.of();
    }

    /**
     * Get the specific conflict object.
     *
     * @return the conflict object, or null if not available
     */
    default @Nullable Object getConflict() {
        return null;
    }

    /**
     * Get the specific resolution object.
     *
     * @return the resolution object, or null if not available
     */
    default @Nullable Object getResolutionObject() {
        return null;
    }

    /**
     * Get the conflict type.
     *
     * @return the conflict type, or null if not available
     */
    default @Nullable String getConflictType() {
        return null;
    }

    /**
     * Get the resolution strategy used.
     *
     * @return the resolution strategy, or null if not available
     */
    default @Nullable String getResolutionStrategy() {
        return null;
    }

    /**
     * Get the timestamp when the resolution was performed.
     *
     * @return the resolution timestamp, or null if not available
     */
    default @Nullable Long getResolutionTimestamp() {
        return null;
    }

    /**
     * Create a successful conflict resolution result with generic data.
     *
     * @param message the success message
     * @param resolution the resolution data
     * @return a successful conflict resolution result
     */
    static ConflictResolutionResult success(String message, Map<String, Object> resolution) {
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
                return resolution;
            }
        };
    }

    /**
     * Create a successful conflict resolution result.
     *
     * @param message the success message
     * @return a successful conflict resolution result
     */
    static ConflictResolutionResult success(String message) {
        return success(message, Map.of());
    }

    /**
     * Create a successful conflict resolution result with specific objects.
     *
     * @param message the success message
     * @param conflict the conflict object
     * @param resolution the resolution object
     * @return a successful conflict resolution result
     */
    static ConflictResolutionResult success(String message, Object conflict, Object resolution) {
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
            public Object getConflict() {
                return conflict;
            }

            @Override
            public Object getResolutionObject() {
                return resolution;
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
        };
    }

    /**
     * Create a "not found" conflict resolution result.
     *
     * @param message the not found message
     * @return a not found conflict resolution result
     */
    static ConflictResolutionResult notFound(String message) {
        return new ConflictResolutionResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }
}
