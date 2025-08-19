package org.openhab.core.ai.common.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all response types in the openHAB AI system.
 * 
 * This interface provides a unified contract for all response objects,
 * ensuring consistent behavior across different response types.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Response<T> {

    /**
     * Get the unique identifier for this response.
     * 
     * @return the response ID
     */
    String getId();

    /**
     * Check if the response indicates success.
     * 
     * @return true if the response is successful
     */
    boolean isSuccess();

    /**
     * Get the response data.
     * 
     * @return the response data, or null if not available
     */
    @Nullable
    T getData();

    /**
     * Get the error message if the response indicates failure.
     * 
     * @return the error message, or null if successful
     */
    @Nullable
    String getErrorMessage();

    /**
     * Get the timestamp when this response was created.
     * 
     * @return the response timestamp in milliseconds since epoch
     */
    long getTimestamp();
}
