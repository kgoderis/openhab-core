package org.openhab.core.ai.tool.elicitation.input;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Elicitation request data for MCP Client Features.
 * 
 * This represents a request for user input during interactions.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ElicitationRequest {

    private final String id;
    private final String prompt;
    private final Object schema;
    private final long createdAt;

    public ElicitationRequest(String id, String prompt, Object schema) {
        this.id = id;
        this.prompt = prompt;
        this.schema = schema;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Get the request ID.
     * 
     * @return the request ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the prompt text.
     * 
     * @return the prompt text
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Get the JSON schema for validation.
     * 
     * @return the JSON schema
     */
    public Object getSchema() {
        return schema;
    }

    /**
     * Get the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "ElicitationRequest{id='" + id + "', prompt='" + prompt + "'}";
    }
}
