package org.openhab.core.ai.tool.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.tool.CompletionRegistry;
import org.openhab.core.ai.tool.dto.Completion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Implementation of CompletionRegistry for MCP Completions.
 *
 * This class manages the registration and discovery of MCP completions,
 * providing access to completion specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CompletionRegistryImpl implements CompletionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionRegistryImpl.class);

    /** Map of completions by prompt reference. */
    private final Map<String, Completion> completions = new ConcurrentHashMap<>();

    @Override
    public void registerCompletion(final Completion completion) {
        String promptReference = completion.getPromptReference();
        completions.put(promptReference, completion);
        LOGGER.debug("Registered completion: {}", promptReference);
    }

    @Override
    public void unregisterCompletion(final String promptReference) {
        completions.remove(promptReference);
        LOGGER.debug("Unregistered completion: {}", promptReference);
    }

    @Override
    public @Nullable Completion getCompletion(final String promptReference) {
        return completions.get(promptReference);
    }

    @Override
    public Map<String, Completion> getAllCompletions() {
        return new ConcurrentHashMap<>(completions);
    }

    @Override
    public int getCompletionCount() {
        return completions.size();
    }

    @Override
    public boolean isCompletionRegistered(final String promptReference) {
        return completions.containsKey(promptReference);
    }

    @Override
    public McpServerFeatures.SyncCompletionSpecification[] getSyncCompletionSpecifications() {
        // TODO: Implement actual MCP completion specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty sync completion specifications (not yet implemented)");
        return new McpServerFeatures.SyncCompletionSpecification[0];
    }

    @Override
    public McpServerFeatures.AsyncCompletionSpecification[] getAsyncCompletionSpecifications() {
        // TODO: Implement actual MCP completion specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty async completion specifications (not yet implemented)");
        return new McpServerFeatures.AsyncCompletionSpecification[0];
    }
}
