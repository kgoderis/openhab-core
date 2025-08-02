package org.openhab.core.ai.common.actions.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for setting/updating openHAB configuration values.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class ConfigurationSetAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSetAction.class);
    private static final String ACTION_ID = "openhab.config.set";
    private static final String ACTION_NAME = "Configuration Set";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Sets/updates openHAB configuration files including items, things, rules, scripts, and system configurations";
    }

    @Override
    public String getCategory() {
        return "config";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("configType", Map.of("type", "string", "enum",
                List.of("items", "things", "rules", "scripts", "sitemaps", "persistence", "transforms", "services"),
                "description", "Type of configuration to set"));
        properties.put("configName",
                Map.of("type", "string", "description", "Configuration file name (with extension)"));
        properties.put("content", Map.of("type", "string", "description", "Configuration file content"));
        properties.put("createBackup", Map.of("type", "boolean", "description",
                "Create backup before updating existing file", "default", true));
        properties.put("validateSyntax", Map.of("type", "boolean", "description",
                "Validate configuration syntax before writing", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("configType", "configName", "content"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("configType", Map.of("type", "string", "description", "Type of configuration set"));
        properties.put("configName", Map.of("type", "string", "description", "Name of configuration file"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether operation was successful"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("fileExisted",
                Map.of("type", "boolean", "description", "Whether file existed before operation"));
        properties.put("directoryCreated", Map.of("type", "boolean", "description", "Whether directory was created"));
        properties.put("backupCreated", Map.of("type", "boolean", "description", "Whether backup was created"));
        properties.put("backupPath", Map.of("type", "string", "description", "Path to backup file if created"));
        properties.put("syntaxValid", Map.of("type", "boolean", "description", "Whether syntax validation passed"));
        properties.put("validationErrors", Map.of("type", "array", "description", "List of validation errors"));
        properties.put("fileSize", Map.of("type", "integer", "description", "Size of written file in bytes"));
        properties.put("filePath", Map.of("type", "string", "description", "Full path to written file"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String configType = (String) parameters.get("configType");
        String configName = (String) parameters.get("configName");
        String content = (String) parameters.get("content");

        if (configType == null || configType.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("configType is required"));
        }

        if (configName == null || configName.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("configName is required"));
        }

        if (content == null) {
            return AIActionValidationResult.invalid(List.of("content is required"));
        }

        List<String> validTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                "transforms", "services");
        if (!validTypes.contains(configType)) {
            return AIActionValidationResult.invalid(List
                    .of("Invalid configType: " + configType + ". Must be one of: " + String.join(", ", validTypes)));
        }

        // Basic file name validation
        if (configName.contains("..") || configName.contains("/") || configName.contains("\\")) {
            return AIActionValidationResult.invalid(List.of("Invalid configName: path traversal not allowed"));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing configuration set action with parameters: {}", parameters);

        try {
            String configType = (String) parameters.get("configType");
            String configName = (String) parameters.get("configName");
            String content = (String) parameters.get("content");
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", true);
            boolean validateSyntax = (Boolean) parameters.getOrDefault("validateSyntax", true);

            Map<String, Object> result = setConfiguration(configType, configName, content, createBackup,
                    validateSyntax);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration set action completed in {}ms", executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to set configuration", e);
            throw new AIActionException(ACTION_ID, "Failed to set configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Sets and updates openHAB configuration files with backup and validation support")
                .tags(List.of("config", "configuration", "files", "backup", "validation", "security"))
                .documentation(
                        "Sets/updates openHAB configuration files including items, things, rules, scripts, and system configurations")
                .examples(List.of(
                        "{\"configType\": \"items\", \"configName\": \"myItems.items\", \"content\": \"Switch MySwitch\"} - Create items file",
                        "{\"configType\": \"rules\", \"configName\": \"automation.rules\", \"content\": \"rule 'Test' when...\"} - Create rules file",
                        "{\"configType\": \"things\", \"configName\": \"zwave.things\", \"content\": \"Thing zwave:device...\"} - Create things file",
                        "{\"configType\": \"scripts\", \"configName\": \"test.js\", \"content\": \"console.log('test');\"} - Create script file"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "backup", true, "validation", true, "security", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ConfigurationSetAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationSetAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> setConfiguration(String configType, String configName, String content,
            boolean createBackup, boolean validateSyntax) throws IOException, AIActionException {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("configType", configType);
        result.put("configName", configName);

        // Use the proper openHAB configuration directory
        String confDir = OpenHAB.getConfigFolder();
        Path confPath = Paths.get(confDir);

        if (!Files.exists(confPath)) {
            throw new AIActionException(ACTION_ID, "Configuration directory not found: " + confDir);
        }

        // Map config types to their directory names
        String dirName = switch (configType) {
            case "transforms" -> "transform";
            default -> configType;
        };

        Path typeDir = confPath.resolve(dirName);
        Path configFile = typeDir.resolve(configName);

        // Create directory if it doesn't exist
        if (!Files.exists(typeDir)) {
            Files.createDirectories(typeDir);
            result.put("directoryCreated", true);
            logger.debug("Created configuration directory: {}", typeDir);
        } else {
            result.put("directoryCreated", false);
        }

        boolean fileExists = Files.exists(configFile);
        result.put("fileExisted", fileExists);

        // Validate syntax if requested
        if (validateSyntax) {
            List<String> validationErrors = validateConfigurationSyntax(configType, configName, content);
            if (!validationErrors.isEmpty()) {
                result.put("validationErrors", validationErrors);
                result.put("success", false);
                result.put("message", "Configuration validation failed");
                logger.warn("Configuration validation failed for {}: {}", configName, validationErrors);
                return result;
            }
            result.put("syntaxValid", true);
            logger.debug("Configuration syntax validation passed for: {}", configName);
        }

        // Create backup if file exists and backup is requested
        String backupPath = null;
        if (fileExists && createBackup) {
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                    .format(Instant.now().atZone(java.time.ZoneId.systemDefault()));
            String backupName = configName + ".backup." + timestamp;
            Path backupFile = typeDir.resolve(backupName);
            Files.copy(configFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            backupPath = backupFile.toString();
            result.put("backupCreated", true);
            result.put("backupPath", backupPath);
            logger.debug("Created backup file: {}", backupPath);
        } else {
            result.put("backupCreated", false);
        }

        // Write the new content
        Files.writeString(configFile, content);

        // Collect file information
        long fileSize = Files.size(configFile);
        result.put("fileSize", fileSize);
        result.put("filePath", configFile.toString());
        result.put("success", true);
        result.put("message", "Configuration file written successfully");

        logger.info("Configuration file written successfully: {} ({} bytes)", configFile, fileSize);

        return result;
    }

    private List<String> validateConfigurationSyntax(String configType, String configName, String content) {
        List<String> errors = new ArrayList<>();

        switch (configType) {
            case "items" -> validateItemsSyntax(content, errors);
            case "things" -> validateThingsSyntax(content, errors);
            case "rules" -> validateRulesSyntax(content, errors);
            case "sitemaps" -> validateSitemapSyntax(content, errors);
            case "persistence" -> validatePersistenceSyntax(content, errors);
            default -> validateBasicSyntax(content, errors);
        }

        return errors;
    }

    private void validateItemsSyntax(String content, List<String> errors) {
        String[] lines = content.split("\r\n|\r|\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")) {
                // Basic item syntax validation
                if (!line.contains(" ") && !line.startsWith("Group")) {
                    errors.add("Line " + (i + 1) + ": Invalid item syntax - missing type or name");
                }
            }
        }
    }

    private void validateThingsSyntax(String content, List<String> errors) {
        String[] lines = content.split("\r\n|\r|\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")) {
                // Basic thing syntax validation
                if (!line.startsWith("Thing") && !line.startsWith("Bridge")) {
                    errors.add("Line " + (i + 1) + ": Invalid thing syntax - must start with 'Thing' or 'Bridge'");
                }
            }
        }
    }

    private void validateRulesSyntax(String content, List<String> errors) {
        String[] lines = content.split("\r\n|\r|\n");
        boolean hasRule = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")) {
                if (line.startsWith("rule")) {
                    hasRule = true;
                }
            }
        }
        if (!hasRule) {
            errors.add("No rule definition found in content");
        }
    }

    private void validateSitemapSyntax(String content, List<String> errors) {
        String[] lines = content.split("\r\n|\r|\n");
        boolean hasSitemap = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")) {
                if (line.startsWith("sitemap")) {
                    hasSitemap = true;
                }
            }
        }
        if (!hasSitemap) {
            errors.add("No sitemap definition found in content");
        }
    }

    private void validatePersistenceSyntax(String content, List<String> errors) {
        // Basic persistence syntax validation
        if (content.trim().isEmpty()) {
            errors.add("Persistence configuration cannot be empty");
        }
    }

    private void validateBasicSyntax(String content, List<String> errors) {
        // Basic syntax validation for other config types
        if (content.trim().isEmpty()) {
            errors.add("Configuration content cannot be empty");
        }
    }
}
