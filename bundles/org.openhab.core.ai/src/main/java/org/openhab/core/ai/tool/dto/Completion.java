package org.openhab.core.ai.tool.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * MCP Completion data model.
 * 
 * This class represents a completion in the MCP protocol, providing
 * autocomplete suggestions for various contexts.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Completion {

    private final String promptReference;
    private final String description;
    private final List<String> suggestions;
    private final int total;
    private final boolean hasMore;

    /**
     * Create a new completion.
     * 
     * @param promptReference Reference to the prompt
     * @param description Completion description
     * @param suggestions List of suggestions
     * @param total Total number of suggestions
     * @param hasMore Whether there are more suggestions available
     */
    public Completion(String promptReference, String description, List<String> suggestions, int total,
            boolean hasMore) {
        this.promptReference = promptReference;
        this.description = description;
        this.suggestions = suggestions;
        this.total = total;
        this.hasMore = hasMore;
    }

    /**
     * Get the prompt reference.
     * 
     * @return the prompt reference
     */
    public String getPromptReference() {
        return promptReference;
    }

    /**
     * Get the completion description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the suggestions.
     * 
     * @return the suggestions
     */
    public List<String> getSuggestions() {
        return suggestions;
    }

    /**
     * Get the total number of suggestions.
     * 
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Check if there are more suggestions available.
     * 
     * @return true if there are more suggestions
     */
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public String toString() {
        return "Completion{promptReference='" + promptReference + "', description='" + description + "', suggestions="
                + suggestions + ", total=" + total + ", hasMore=" + hasMore + "}";
    }
}
