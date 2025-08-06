package org.openhab.core.ai.tool.proxy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;

/**
 * Encapsulated prompt proxy for system information.
 * 
 * This class extends AbstractPrompt to provide proper encapsulation,
 * lifecycle management, and prompt generation for system information.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemPromptProxy extends AbstractPrompt {

    private final String promptId;
    private final Map<String, Object> systemInfo;
    private volatile @Nullable String cachedPromptText;

    /**
     * Create a new SystemPromptProxy.
     *
     * @param promptId the prompt ID
     * @param systemInfo the system information
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public SystemPromptProxy(String promptId, Map<String, Object> systemInfo, long refreshIntervalMs) {
        super("System: " + promptId, "System information prompt: " + promptId, createArguments(), refreshIntervalMs);

        this.promptId = promptId;
        this.systemInfo = new ConcurrentHashMap<>(systemInfo);
    }

    /**
     * Create prompt arguments for system prompts.
     *
     * @return the list of prompt arguments
     */
    private static List<Prompt.PromptArgument> createArguments() {
        return List.of(new Prompt.PromptArgument("includeItems", "Include item status information", false),
                new Prompt.PromptArgument("includeThings", "Include thing status information", false),
                new Prompt.PromptArgument("includeRules", "Include rule status information", false),
                new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));
    }

    @Override
    public @Nullable String generatePromptText(@Nullable Map<String, Object> arguments) {
        if (needsRefresh()) {
            refresh();
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an AI assistant for openHAB home automation system.\n\n");

            // Add system information
            prompt.append("System Information:\n");
            for (Map.Entry<String, Object> entry : systemInfo.entrySet()) {
                prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            prompt.append("\n");

            // Add context based on arguments
            if (arguments != null) {
                if (Boolean.TRUE.equals(arguments.get("includeItems"))) {
                    prompt.append("Please include information about openHAB items in your response.\n");
                }
                if (Boolean.TRUE.equals(arguments.get("includeThings"))) {
                    prompt.append("Please include information about openHAB things in your response.\n");
                }
                if (Boolean.TRUE.equals(arguments.get("includeRules"))) {
                    prompt.append("Please include information about openHAB rules in your response.\n");
                }

                String format = (String) arguments.get("format");
                if (format != null) {
                    prompt.append("Please format your response as: ").append(format).append("\n");
                }
            }

            cachedPromptText = prompt.toString();
            return cachedPromptText;
        } catch (Exception e) {
            LOGGER.error("Error generating prompt text for system prompt: {}", promptId, e);
            return null;
        }
    }

    @Override
    public boolean validateArguments(@Nullable Map<String, Object> arguments) {
        if (arguments == null) {
            return true; // No arguments required
        }

        try {
            // Validate format argument if present
            Object format = arguments.get("format");
            if (format != null && !(format instanceof String)) {
                return false;
            }
            if (format instanceof String) {
                String formatStr = (String) format;
                if (!formatStr.equals("json") && !formatStr.equals("xml") && !formatStr.equals("text")) {
                    return false;
                }
            }

            // Validate boolean arguments
            for (String key : List.of("includeItems", "includeThings", "includeRules")) {
                Object value = arguments.get(key);
                if (value != null && !(value instanceof Boolean)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error validating arguments for system prompt: {}", promptId, e);
            return false;
        }
    }

    @Override
    public void refresh() {
        try {
            // Update system information if needed
            // This could involve fetching fresh system status
            cachedPromptText = null; // Clear cached prompt text

            updateRefreshTime();
            LOGGER.debug("Refreshed system prompt: {}", promptId);
        } catch (Exception e) {
            LOGGER.error("Error refreshing system prompt: {}", promptId, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing system prompt: {}", promptId);
        cachedPromptText = null;
        markInvalid();
    }

    /**
     * Get the prompt ID.
     *
     * @return the prompt ID
     */
    public String getPromptId() {
        return promptId;
    }

    /**
     * Get the system information.
     *
     * @return the system information map
     */
    public Map<String, Object> getSystemInfo() {
        return new ConcurrentHashMap<>(systemInfo);
    }

    /**
     * Update system information.
     *
     * @param newSystemInfo the new system information
     */
    public void updateSystemInfo(Map<String, Object> newSystemInfo) {
        systemInfo.clear();
        systemInfo.putAll(newSystemInfo);
        refresh(); // Refresh to update cached prompt text
    }
}
