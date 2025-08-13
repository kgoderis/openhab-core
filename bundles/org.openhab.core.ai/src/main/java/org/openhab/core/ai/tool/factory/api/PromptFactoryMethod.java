package org.openhab.core.ai.tool.factory.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.BasePrompt;

/**
 * Functional interface for creating prompts.
 *
 * This interface defines the contract for creating BasePrompt objects
 * with a specified name and refresh interval.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface PromptFactoryMethod {

    /**
     * Create a prompt with the specified name and refresh interval.
     *
     * @param promptName the name of the prompt to create
     * @param refreshIntervalMs the refresh interval in milliseconds
     * @return the created BasePrompt, or null if creation failed
     * @throws Exception if an error occurs during creation
     */
    @Nullable
    BasePrompt create(String promptName, long refreshIntervalMs) throws Exception;
}
