package org.openhab.core.ai.tool.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.tool.PromptRegistry;
import org.openhab.core.ai.tool.dto.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Implementation of PromptRegistry for MCP Prompts.
 *
 * This class manages the registration and discovery of MCP prompts,
 * providing access to prompt specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptRegistryImpl implements PromptRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptRegistryImpl.class);

    /** Map of prompts by name. */
    private final Map<String, Prompt> prompts = new ConcurrentHashMap<>();

    @Override
    public void registerPrompt(final Prompt prompt) {
        String name = prompt.getName();
        prompts.put(name, prompt);
        LOGGER.debug("Registered prompt: {}", name);
    }

    @Override
    public void unregisterPrompt(final String name) {
        prompts.remove(name);
        LOGGER.debug("Unregistered prompt: {}", name);
    }

    @Override
    public @Nullable Prompt getPrompt(final String name) {
        return prompts.get(name);
    }

    @Override
    public Map<String, Prompt> getAllPrompts() {
        return new ConcurrentHashMap<>(prompts);
    }

    @Override
    public int getPromptCount() {
        return prompts.size();
    }

    @Override
    public boolean isPromptRegistered(final String name) {
        return prompts.containsKey(name);
    }

    @Override
    public McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications() {
        // TODO: Implement actual MCP prompt specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty sync prompt specifications (not yet implemented)");
        return new McpServerFeatures.SyncPromptSpecification[0];
    }

    @Override
    public McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications() {
        // TODO: Implement actual MCP prompt specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty async prompt specifications (not yet implemented)");
        return new McpServerFeatures.AsyncPromptSpecification[0];
    }
}
