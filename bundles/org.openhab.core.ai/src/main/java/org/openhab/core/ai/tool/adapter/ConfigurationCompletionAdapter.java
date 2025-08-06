package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.openhab.core.ai.tool.dto.Completion;
import org.openhab.core.ai.tool.factory.CompletionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB configurations.
 * 
 * This adapter provides MCP completion access to openHAB configuration suggestions,
 * allowing AI agents to get contextual completions for configuration operations.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationCompletionAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationCompletionAdapter.class);
    private final CompletionFactory completionFactory;

    /**
     * Create a new ConfigurationCompletionAdapter.
     */
    public ConfigurationCompletionAdapter() {
        this.completionFactory = new CompletionFactory();
    }

    /**
     * Create configuration key completions.
     *
     * @param partialKey the partial configuration key
     * @return the completion suggestions
     */
    public Completion createConfigurationKeyCompletions(String partialKey) {
        try {
            List<String> commonKeys = List.of("host", "port", "username", "password", "url", "apiKey", "token",
                    "timeout", "retries", "enabled", "pollingInterval", "updateInterval", "maxConnections",
                    "bufferSize", "encoding", "format", "protocol", "ssl", "tls", "certificate", "privateKey");

            List<String> suggestions = commonKeys.stream()
                    .filter(key -> key.toLowerCase().contains(partialKey.toLowerCase())).collect(Collectors.toList());

            return new Completion("config_keys", "Configuration key suggestions for: " + partialKey, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating configuration key completions for: {}", partialKey, e);
            return new Completion("config_keys", "Configuration key suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create configuration value completions.
     *
     * @param partialValue the partial configuration value
     * @return the completion suggestions
     */
    public Completion createConfigurationValueCompletions(String partialValue) {
        try {
            List<String> commonValues = List.of("true", "false", "localhost", "127.0.0.1", "8080", "443", "admin",
                    "user", "password", "https://", "http://", "ws://", "wss://", "json", "xml", "text", "binary",
                    "utf-8", "ascii", "iso-8859-1", "30", "60", "300", "600", "3600");

            List<String> suggestions = commonValues.stream()
                    .filter(value -> value.toLowerCase().contains(partialValue.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("config_values", "Configuration value suggestions for: " + partialValue, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating configuration value completions for: {}", partialValue, e);
            return new Completion("config_values", "Configuration value suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create configuration type completions.
     *
     * @param partialType the partial configuration type
     * @return the completion suggestions
     */
    public Completion createConfigurationTypeCompletions(String partialType) {
        try {
            List<String> typeSuggestions = List.of("String", "Integer", "Long", "Double", "Float", "Boolean",
                    "BigDecimal", "BigInteger", "Date", "LocalDate", "LocalTime", "LocalDateTime", "ZonedDateTime",
                    "Duration", "Period", "URL", "URI", "File", "Path", "List", "Map", "Set", "Array", "Object",
                    "Enum");

            List<String> suggestions = typeSuggestions.stream()
                    .filter(type -> type.toLowerCase().contains(partialType.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("config_types", "Configuration type suggestions for: " + partialType, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating configuration type completions for: {}", partialType, e);
            return new Completion("config_types", "Configuration type suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create configuration section completions.
     *
     * @param partialSection the partial configuration section
     * @return the completion suggestions
     */
    public Completion createConfigurationSectionCompletions(String partialSection) {
        try {
            List<String> sectionSuggestions = List.of("connection", "authentication", "security", "logging",
                    "performance", "network", "database", "cache", "monitoring", "alerts", "notifications",
                    "scheduling", "backup", "restore", "maintenance", "advanced", "experimental", "debug",
                    "development", "production", "testing", "staging");

            List<String> suggestions = sectionSuggestions.stream()
                    .filter(section -> section.toLowerCase().contains(partialSection.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("config_sections", "Configuration section suggestions for: " + partialSection,
                    suggestions, suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating configuration section completions for: {}", partialSection, e);
            return new Completion("config_sections", "Configuration section suggestions", List.of(), 0, false);
        }
    }

    /**
     * Get or create an encapsulated configuration completion.
     *
     * @param completionType the type of completion (keys, values, types, sections)
     * @param context the context for the completion (e.g., partial key, section name)
     * @return the encapsulated completion or null if creation fails
     */
    public @Nullable AbstractCompletion getOrCreateEncapsulatedConfigurationCompletion(String completionType,
            String context) {
        String completionName = "config_" + completionType + "_" + context;

        return completionFactory.createCompletion(completionName, (name, refreshIntervalMs) -> {
            try {
                return new ConfigurationCompletionProxy(completionType, context, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated configuration completion: {} - {}", completionType, context,
                        e);
                return null;
            }
        });
    }

    /**
     * Get additional suggestions for a configuration completion.
     *
     * @param completionType the type of completion
     * @param context the context for the completion
     * @param additionalContext additional context for suggestions
     * @return the additional suggestions or null if not available
     */
    public @Nullable List<String> getAdditionalConfigurationSuggestions(String completionType, String context,
            @Nullable String additionalContext) {
        AbstractCompletion completion = getOrCreateEncapsulatedConfigurationCompletion(completionType, context);
        return completion != null ? completion.getAdditionalSuggestions(additionalContext) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ConfigurationCompletionAdapter resources");
        completionFactory.cleanup();
    }
}
