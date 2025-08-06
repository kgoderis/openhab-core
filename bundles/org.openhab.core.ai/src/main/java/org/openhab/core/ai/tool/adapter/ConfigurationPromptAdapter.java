package org.openhab.core.ai.tool.adapter;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.dto.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB configuration.
 * 
 * This adapter provides MCP prompt access to openHAB configuration prompts,
 * allowing AI agents to get contextual prompts for configuration operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationPromptAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPromptAdapter.class);

    /**
     * Create a configuration get prompt.
     *
     * @param configId the configuration ID
     * @return the configuration get prompt
     */
    public Prompt createConfigGetPrompt(String configId) {
        String name = "Configuration Get: " + configId;
        String description = "Get configuration for: " + configId;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("configId", "Configuration ID", true),
                new Prompt.PromptArgument("property", "Specific property to get", false),
                new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration set prompt.
     *
     * @param configId the configuration ID
     * @return the configuration set prompt
     */
    public Prompt createConfigSetPrompt(String configId) {
        String name = "Configuration Set: " + configId;
        String description = "Set configuration for: " + configId;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("configId", "Configuration ID", true),
                new Prompt.PromptArgument("property", "Property to set", true),
                new Prompt.PromptArgument("value", "Value to set", true),
                new Prompt.PromptArgument("confirm", "Confirm the change", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration update prompt.
     *
     * @param configId the configuration ID
     * @return the configuration update prompt
     */
    public Prompt createConfigUpdatePrompt(String configId) {
        String name = "Configuration Update: " + configId;
        String description = "Update configuration for: " + configId;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("configId", "Configuration ID", true),
                new Prompt.PromptArgument("properties", "Properties to update (JSON)", true),
                new Prompt.PromptArgument("confirm", "Confirm the changes", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration reset prompt.
     *
     * @param configId the configuration ID
     * @return the configuration reset prompt
     */
    public Prompt createConfigResetPrompt(String configId) {
        String name = "Configuration Reset: " + configId;
        String description = "Reset configuration for: " + configId;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("configId", "Configuration ID", true),
                new Prompt.PromptArgument("property", "Specific property to reset", false),
                new Prompt.PromptArgument("confirm", "Confirm the reset", true));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration backup prompt.
     *
     * @return the configuration backup prompt
     */
    public Prompt createConfigBackupPrompt() {
        String name = "Configuration Backup";
        String description = "Backup system configuration";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("includeItems", "Include item configurations", false),
                new Prompt.PromptArgument("includeThings", "Include thing configurations", false),
                new Prompt.PromptArgument("includeRules", "Include rule configurations", false),
                new Prompt.PromptArgument("format", "Backup format (json, xml, zip)", false),
                new Prompt.PromptArgument("destination", "Backup destination path", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration restore prompt.
     *
     * @return the configuration restore prompt
     */
    public Prompt createConfigRestorePrompt() {
        String name = "Configuration Restore";
        String description = "Restore system configuration from backup";

        List<Prompt.PromptArgument> arguments = List
                .of(new Prompt.PromptArgument("backupFile", "Backup file path", true),
                        new Prompt.PromptArgument("components", "Components to restore (items, things, rules, all)",
                                false),
                        new Prompt.PromptArgument("confirm", "Confirm the restore operation", true),
                        new Prompt.PromptArgument("validate", "Validate configuration before restore", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a configuration validation prompt.
     *
     * @return the configuration validation prompt
     */
    public Prompt createConfigValidationPrompt() {
        String name = "Configuration Validation";
        String description = "Validate system configuration";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("scope", "Validation scope (items, things, rules, all)", false),
                new Prompt.PromptArgument("fixIssues", "Automatically fix validation issues", false),
                new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));

        return new Prompt(name, description, arguments);
    }
}
