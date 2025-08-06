package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;
import org.openhab.core.ai.tool.factory.PromptFactory;
import org.openhab.core.ai.tool.proxy.SystemPromptProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for system information.
 * 
 * This adapter provides MCP prompt access to openHAB system information,
 * allowing AI agents to get contextual system prompts.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemPromptAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPromptAdapter.class);
    private final PromptFactory promptFactory;

    /**
     * Create a new SystemPromptAdapter.
     */
    public SystemPromptAdapter() {
        this.promptFactory = new PromptFactory();
    }

    /**
     * Create a system information prompt.
     *
     * @param promptId the prompt ID
     * @param systemInfo the system information
     * @return the prompt or null if creation fails
     */
    public @Nullable Prompt createSystemPrompt(String promptId, Map<String, Object> systemInfo) {
        try {
            String name = "System: " + promptId;
            String description = "System information prompt: " + promptId;

            // Create prompt arguments based on system info
            List<Prompt.PromptArgument> arguments = List.of(
                    new Prompt.PromptArgument("includeItems", "Include item status information", false),
                    new Prompt.PromptArgument("includeThings", "Include thing status information", false),
                    new Prompt.PromptArgument("includeRules", "Include rule status information", false),
                    new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));

            return new Prompt(name, description, arguments);
        } catch (Exception e) {
            LOGGER.error("Error creating system prompt: {}", promptId, e);
            return null;
        }
    }

    /**
     * Create a system status prompt.
     *
     * @return the system status prompt
     */
    public Prompt createSystemStatusPrompt() {
        Map<String, Object> systemInfo = new ConcurrentHashMap<>();
        systemInfo.put("system", "openHAB");
        systemInfo.put("version", "5.0.0");
        systemInfo.put("status", "running");

        return createSystemPrompt("status", systemInfo);
    }

    /**
     * Create a system configuration prompt.
     *
     * @param configInfo the configuration information
     * @return the system configuration prompt
     */
    public @Nullable Prompt createSystemConfigPrompt(Map<String, Object> configInfo) {
        return createSystemPrompt("config", configInfo);
    }

    /**
     * Create a system health prompt.
     *
     * @param healthInfo the health information
     * @return the system health prompt
     */
    public @Nullable Prompt createSystemHealthPrompt(Map<String, Object> healthInfo) {
        return createSystemPrompt("health", healthInfo);
    }

    /**
     * Get or create an encapsulated system prompt.
     *
     * @param promptId the prompt ID
     * @param systemInfo the system information
     * @return the encapsulated prompt or null if creation fails
     */
    public @Nullable AbstractPrompt getOrCreateEncapsulatedSystemPrompt(String promptId,
            Map<String, Object> systemInfo) {
        String promptName = "System: " + promptId;

        return promptFactory.createPrompt(promptName, (name, refreshIntervalMs) -> {
            try {
                return new SystemPromptProxy(promptId, systemInfo, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated system prompt: {}", promptId, e);
                return null;
            }
        });
    }

    /**
     * Generate prompt text for a system prompt.
     *
     * @param promptId the prompt ID
     * @param systemInfo the system information
     * @param arguments the prompt arguments
     * @return the generated prompt text or null if generation fails
     */
    public @Nullable String generateSystemPromptText(String promptId, Map<String, Object> systemInfo,
            @Nullable Map<String, Object> arguments) {
        AbstractPrompt prompt = getOrCreateEncapsulatedSystemPrompt(promptId, systemInfo);
        return prompt != null ? prompt.generatePromptText(arguments) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up SystemPromptAdapter resources");
        promptFactory.cleanup();
    }
}
