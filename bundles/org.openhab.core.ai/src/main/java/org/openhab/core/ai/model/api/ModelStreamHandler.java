package org.openhab.core.ai.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handler for processing streaming LLM responses.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public interface ModelStreamHandler {

    /**
     * Called when a new chunk of the response is received.
     * 
     * @param chunk The response chunk
     */
    void onChunk(String chunk);

    /**
     * Called when the stream is complete.
     * 
     * @param finalResponse The final complete response
     */
    void onComplete(ModelResponse finalResponse);

    /**
     * Called when an error occurs during streaming.
     * 
     * @param error The error that occurred
     */
    void onError(Exception error);
}
