package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;

/**
 * Encapsulated completion proxy for openHAB configurations.
 * 
 * This class extends AbstractCompletion to provide proper encapsulation,
 * lifecycle management, and suggestion generation for configuration-specific completions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationCompletionProxy extends AbstractCompletion {

    private final String completionType;
    private final String context;
    private volatile @Nullable List<String> cachedSuggestions;

    /**
     * Create a new ConfigurationCompletionProxy.
     *
     * @param completionType the type of completion (keys, values, types, sections)
     * @param context the context for the completion (e.g., partial key, section name)
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ConfigurationCompletionProxy(String completionType, String context, long refreshIntervalMs) {
        super("config_" + completionType, "Configuration " + completionType + " suggestions for: " + context, List.of(),
                0, false, refreshIntervalMs);

        this.completionType = completionType;
        this.context = context;
    }

    @Override
    public @Nullable List<String> getAdditionalSuggestions(@Nullable String additionalContext) {
        if (needsRefresh()) {
            refresh();
        }

        try {
            switch (completionType.toLowerCase()) {
                case "keys":
                    return generateConfigurationKeySuggestions();
                case "values":
                    return generateConfigurationValueSuggestions();
                case "types":
                    return generateConfigurationTypeSuggestions();
                case "sections":
                    return generateConfigurationSectionSuggestions();
                default:
                    LOGGER.warn("Unknown completion type: {}", completionType);
                    return List.of();
            }
        } catch (Exception e) {
            LOGGER.error("Error generating configuration completion suggestions: {} - {}", completionType, context, e);
            return List.of();
        }
    }

    private List<String> generateConfigurationKeySuggestions() {
        try {
            List<String> commonKeys = List.of("host", "port", "username", "password", "url", "apiKey", "token",
                    "timeout", "retries", "enabled", "pollingInterval", "updateInterval", "maxConnections",
                    "bufferSize", "encoding", "format", "protocol", "ssl", "tls", "certificate", "privateKey");

            List<String> suggestions = commonKeys.stream()
                    .filter(key -> key.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating configuration key suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateConfigurationValueSuggestions() {
        try {
            List<String> commonValues = List.of("true", "false", "localhost", "127.0.0.1", "8080", "443", "admin",
                    "user", "password", "https://", "http://", "ws://", "wss://", "json", "xml", "text", "binary",
                    "utf-8", "ascii", "iso-8859-1", "30", "60", "300", "600", "3600");

            List<String> suggestions = commonValues.stream()
                    .filter(value -> value.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating configuration value suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateConfigurationTypeSuggestions() {
        try {
            List<String> typeSuggestions = List.of("String", "Integer", "Long", "Double", "Float", "Boolean",
                    "BigDecimal", "BigInteger", "Date", "LocalDate", "LocalTime", "LocalDateTime", "ZonedDateTime",
                    "Duration", "Period", "URL", "URI", "File", "Path", "List", "Map", "Set", "Array", "Object",
                    "Enum");

            List<String> suggestions = typeSuggestions.stream()
                    .filter(type -> type.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating configuration type suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateConfigurationSectionSuggestions() {
        try {
            List<String> sectionSuggestions = List.of("connection", "authentication", "security", "logging",
                    "performance", "network", "database", "cache", "monitoring", "alerts", "notifications",
                    "scheduling", "backup", "restore", "maintenance", "advanced", "experimental", "debug",
                    "development", "production", "testing", "staging");

            List<String> suggestions = sectionSuggestions.stream()
                    .filter(section -> section.toLowerCase().contains(context.toLowerCase()))
                    .collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating configuration section suggestions for: {}", context, e);
            return List.of();
        }
    }

    @Override
    public void refresh() {
        try {
            // Clear cached suggestions to force regeneration
            cachedSuggestions = null;

            // Regenerate suggestions based on completion type
            switch (completionType.toLowerCase()) {
                case "keys":
                    generateConfigurationKeySuggestions();
                    break;
                case "values":
                    generateConfigurationValueSuggestions();
                    break;
                case "types":
                    generateConfigurationTypeSuggestions();
                    break;
                case "sections":
                    generateConfigurationSectionSuggestions();
                    break;
                default:
                    LOGGER.warn("Unknown completion type during refresh: {}", completionType);
            }

            updateRefreshTime();
            LOGGER.debug("Refreshed configuration completion: {} - {}", completionType, context);
        } catch (Exception e) {
            LOGGER.error("Error refreshing configuration completion: {} - {}", completionType, context, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing configuration completion: {} - {}", completionType, context);
        cachedSuggestions = null;
        markInvalid();
    }

    /**
     * Get the completion type.
     *
     * @return the completion type
     */
    public String getCompletionType() {
        return completionType;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the cached suggestions.
     *
     * @return the cached suggestions or null if not available
     */
    public @Nullable List<String> getCachedSuggestions() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedSuggestions;
    }
}
