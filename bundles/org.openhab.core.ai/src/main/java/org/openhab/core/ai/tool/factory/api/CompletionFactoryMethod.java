package org.openhab.core.ai.tool.factory.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.BaseCompletion;

/**
 * Functional interface for creating completions.
 *
 * This interface defines the contract for creating BaseCompletion objects
 * with a specified name and refresh interval.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface CompletionFactoryMethod {

    /**
     * Create a completion with the specified name and refresh interval.
     *
     * @param completionName the name of the completion to create
     * @param refreshIntervalMs the refresh interval in milliseconds
     * @return the created BaseCompletion, or null if creation failed
     * @throws Exception if an error occurs during creation
     */
    @Nullable
    BaseCompletion create(String completionName, long refreshIntervalMs) throws Exception;
}
