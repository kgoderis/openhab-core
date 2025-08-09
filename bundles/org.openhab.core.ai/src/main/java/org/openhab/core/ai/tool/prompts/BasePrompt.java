package org.openhab.core.ai.tool.prompts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.dto.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP Prompts with encapsulation and lifecycle management.
 * 
 * This class provides a foundation for prompt implementations that need
 * to encapsulate behavior, manage state, and handle lifecycle events.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BasePrompt {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BasePrompt.class);

    protected final String name;
    protected final String description;
    protected final List<Prompt.PromptArgument> arguments;

    private volatile boolean valid = true;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs;

    /**
     * Create a new abstract prompt.
     * 
     * @param name Prompt name
     * @param description Prompt description
     * @param arguments List of prompt arguments
     * @param refreshIntervalMs Refresh interval in milliseconds (0 for no auto-refresh)
     */
    protected BasePrompt(String name, String description, List<Prompt.PromptArgument> arguments,
            long refreshIntervalMs) {
        this.name = name;
        this.description = description;
        this.arguments = new CopyOnWriteArrayList<>(arguments);
        this.refreshIntervalMs = refreshIntervalMs;
    }

    /**
     * Get the prompt name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the prompt description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the prompt arguments.
     * 
     * @return the arguments
     */
    public List<Prompt.PromptArgument> getArguments() {
        return new CopyOnWriteArrayList<>(arguments);
    }

    /**
     * Check if the prompt is valid.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the last refresh time.
     * 
     * @return the last refresh time in milliseconds since epoch
     */
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * Check if the prompt needs refreshing.
     * 
     * @return true if refresh is needed
     */
    public boolean needsRefresh() {
        if (refreshIntervalMs <= 0) {
            return false;
        }
        return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
    }

    /**
     * Generate the prompt text with the given arguments.
     * This method should be implemented by subclasses to provide the actual prompt generation.
     * 
     * @param arguments the arguments to use
     * @return the generated prompt text or null if not available
     */
    public abstract @Nullable String generatePromptText(@Nullable Map<String, Object> arguments);

    /**
     * Validate the prompt arguments.
     * 
     * @param arguments the arguments to validate
     * @return true if valid
     */
    public abstract boolean validateArguments(@Nullable Map<String, Object> arguments);

    /**
     * Refresh the prompt data.
     * This method should be implemented by subclasses to refresh their internal state.
     */
    public abstract void refresh();

    /**
     * Close the prompt and release any resources.
     * This method should be implemented by subclasses to perform cleanup.
     */
    public abstract void close();

    /**
     * Mark the prompt as invalid.
     */
    public void markInvalid() {
        this.valid = false;
    }

    /**
     * Update the refresh timestamp.
     */
    protected void updateRefreshTime() {
        this.lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Create a Prompt DTO from this abstract prompt.
     * 
     * @return the Prompt DTO
     */
    public Prompt toPrompt() {
        return new Prompt(name, description, arguments);
    }

    @Override
    public String toString() {
        return "BasePrompt{name='" + name + "', description='" + description + "', arguments=" + arguments + ", valid="
                + valid + "}";
    }
}
