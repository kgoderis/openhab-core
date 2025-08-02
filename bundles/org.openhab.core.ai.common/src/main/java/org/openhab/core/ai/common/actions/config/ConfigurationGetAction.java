package org.openhab.core.ai.common.actions.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
 * AIAction for retrieving openHAB configuration values.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class ConfigurationGetAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationGetAction.class);
    private static final String ACTION_ID = "openhab.config.get";
    private static final String ACTION_NAME = "Configuration Get";

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
        return "Retrieves openHAB configuration values from files, including items, things, rules, scripts, and system configurations";
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
        properties.put("configType",
                Map.of("type", "string", "enum",
                        List.of("items", "things", "rules", "scripts", "sitemaps", "persistence", "transforms",
                                "services", "all"),
                        "description", "Type of configuration to retrieve", "default", "all"));
        properties.put("configName",
                Map.of("type", "string", "description", "Specific configuration file name (without extension)"));
        properties.put("includeContent",
                Map.of("type", "boolean", "description", "Include file content in response", "default", true));
        properties.put("includeMetadata", Map.of("type", "boolean", "description",
                "Include file metadata (size, modified date, etc.)", "default", true));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("configType", Map.of("type", "string", "description", "Type of configuration retrieved"));
        properties.put("configurations", Map.of("type", "array", "description", "List of configuration files"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of configurations found"));
        properties.put("confDirectory", Map.of("type", "string", "description", "Configuration directory path"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.valid(parameters);
        }

        // Check for invalid parameters
        List<String> validParams = List.of("configType", "configName", "includeContent", "includeMetadata");
        List<String> invalidParams = new ArrayList<>();

        for (String param : parameters.keySet()) {
            if (!validParams.contains(param)) {
                invalidParams.add(param);
            }
        }

        if (!invalidParams.isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Invalid parameters: " + String.join(", ", invalidParams)
                    + ". Valid parameters are: " + String.join(", ", validParams)));
        }

        String configType = (String) parameters.getOrDefault("configType", "all");
        List<String> validTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                "transforms", "services", "all");

        // Handle null configType by using default
        if (configType == null) {
            configType = "all";
        }

        if (!validTypes.contains(configType)) {
            return AIActionValidationResult.invalid(List
                    .of("Invalid configType: " + configType + ". Must be one of: " + String.join(", ", validTypes)));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing configuration get action with parameters: {}", parameters);

        // Validate parameters first
        AIActionValidationResult validation = validateParameters(parameters);
        if (!validation.isValid()) {
            throw new AIActionException(ACTION_ID, "Invalid parameters: " + validation.getErrors());
        }

        // Validate context
        if (context == null) {
            throw new AIActionException(ACTION_ID, "Context cannot be null");
        }

        // Validate context has required fields
        if (context.getProtocol() == null || context.getProtocol().trim().isEmpty()) {
            throw new AIActionException(ACTION_ID, "Context protocol cannot be null or empty");
        }

        try {
            String configType = (String) parameters.getOrDefault("configType", "all");
            String configName = (String) parameters.get("configName");
            boolean includeContent = (Boolean) parameters.getOrDefault("includeContent", true);
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);

            Map<String, Object> result = retrieveConfiguration(configType, configName, includeContent, includeMetadata);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration get action completed in {}ms, found {} configurations", executionTime,
                    result.get("totalCount"));

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to retrieve configuration", e);
            throw new AIActionException(ACTION_ID, "Failed to retrieve configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        // Validate parameters synchronously and complete future exceptionally for validation errors
        AIActionValidationResult validation = validateParameters(parameters);
        if (!validation.isValid()) {
            CompletableFuture<AIActionResult> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new AIActionException(ACTION_ID, "Invalid parameters: " + validation.getErrors()));
            return future;
        }

        // Validate context and complete future exceptionally
        if (context == null) {
            CompletableFuture<AIActionResult> future = new CompletableFuture<>();
            future.completeExceptionally(new AIActionException(ACTION_ID, "Context cannot be null"));
            return future;
        }

        // Validate context has required fields
        if (context.getProtocol() == null || context.getProtocol().trim().isEmpty()) {
            CompletableFuture<AIActionResult> future = new CompletableFuture<>();
            future.completeExceptionally(new AIActionException(ACTION_ID, "Context protocol cannot be null or empty"));
            return future;
        }

        // For simple operations, complete immediately
        try {
            AIActionResult result = execute(parameters, context);
            return CompletableFuture.completedFuture(result);
        } catch (AIActionException e) {
            CompletableFuture<AIActionResult> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieves openHAB configuration files and their content")
                .tags(List.of("config", "configuration", "files", "content", "metadata"))
                .documentation(
                        "Retrieves openHAB configuration values from files, including items, things, rules, scripts, and system configurations")
                .examples(List.of("{\"configType\": \"items\"} - Get all item configuration files",
                        "{\"configType\": \"things\", \"configName\": \"zwave\"} - Get Z-Wave thing configurations",
                        "{\"configType\": \"all\", \"includeContent\": false} - Get all configurations without content",
                        "{\"configType\": \"rules\", \"includeMetadata\": true} - Get rule configurations with metadata"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("supportsAsync", true, "supportsValidation", true, "supportsFileAccess", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ConfigurationGetAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationGetAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> retrieveConfiguration(String configType, String configName, boolean includeContent,
            boolean includeMetadata) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("configType", configType);

        // Use the proper openHAB configuration directory
        String confDir = OpenHAB.getConfigFolder();
        Path confPath = Paths.get(confDir);

        if (!Files.exists(confPath)) {
            result.put("error", "Configuration directory not found: " + confDir);
            result.put("configurations", List.of());
            result.put("totalCount", 0);
            result.put("confDirectory", confDir);
            return result;
        }

        List<Map<String, Object>> configurations = new ArrayList<>();

        if ("all".equals(configType)) {
            // Retrieve all configuration types
            configurations
                    .addAll(getConfigurationsOfType(confPath, "items", configName, includeContent, includeMetadata));
            configurations
                    .addAll(getConfigurationsOfType(confPath, "things", configName, includeContent, includeMetadata));
            configurations
                    .addAll(getConfigurationsOfType(confPath, "rules", configName, includeContent, includeMetadata));
            configurations
                    .addAll(getConfigurationsOfType(confPath, "scripts", configName, includeContent, includeMetadata));
            configurations
                    .addAll(getConfigurationsOfType(confPath, "sitemaps", configName, includeContent, includeMetadata));
            configurations.addAll(
                    getConfigurationsOfType(confPath, "persistence", configName, includeContent, includeMetadata));
            configurations.addAll(
                    getConfigurationsOfType(confPath, "transform", configName, includeContent, includeMetadata));
            configurations
                    .addAll(getConfigurationsOfType(confPath, "services", configName, includeContent, includeMetadata));
        } else {
            configurations
                    .addAll(getConfigurationsOfType(confPath, configType, configName, includeContent, includeMetadata));
        }

        result.put("configurations", configurations);
        result.put("totalCount", configurations.size());
        result.put("confDirectory", confDir);

        return result;
    }

    private List<Map<String, Object>> getConfigurationsOfType(Path confPath, String type, String configName,
            boolean includeContent, boolean includeMetadata) throws IOException {
        List<Map<String, Object>> configs = new ArrayList<>();

        // Map config types to their directory names
        String dirName = switch (type) {
            case "transforms" -> "transform";
            case "persistence" -> "persistence";
            default -> type;
        };

        Path typeDir = confPath.resolve(dirName);

        if (!Files.exists(typeDir) || !Files.isDirectory(typeDir)) {
            return configs; // Return empty list if directory doesn't exist
        }

        try (Stream<Path> files = Files.list(typeDir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> configName == null || path.getFileName().toString().startsWith(configName))
                    .forEach(path -> {
                        try {
                            Map<String, Object> config = new HashMap<>();
                            String fileName = path.getFileName().toString();

                            config.put("type", type);
                            config.put("name", fileName);
                            config.put("path", path.toString());

                            if (includeMetadata) {
                                config.put("size", Files.size(path));
                                config.put("lastModified", Files.getLastModifiedTime(path).toInstant().toString());
                                config.put("readable", Files.isReadable(path));
                                config.put("writable", Files.isWritable(path));
                            }

                            if (includeContent) {
                                // Only include content for text files, limit size to prevent memory issues
                                long fileSize = Files.size(path);
                                if (fileSize > 1024 * 1024) { // 1MB limit
                                    config.put("content",
                                            "[File too large to display - " + formatBytes(fileSize) + "]");
                                    config.put("contentTruncated", true);
                                } else if (isTextFile(fileName)) {
                                    String content = Files.readString(path);
                                    config.put("content", content);
                                    config.put("contentTruncated", false);
                                    config.put("lineCount", content.split("\r\n|\r|\n").length);
                                } else {
                                    config.put("content", "[Binary file - content not displayed]");
                                    config.put("contentTruncated", true);
                                }
                            }

                            configs.add(config);
                        } catch (IOException e) {
                            logger.warn("Failed to read configuration file: {}", path, e);
                            Map<String, Object> config = new HashMap<>();
                            config.put("type", type);
                            config.put("name", path.getFileName().toString());
                            config.put("path", path.toString());
                            config.put("error", "Failed to read file: " + e.getMessage());
                            configs.add(config);
                        }
                    });
        }

        return configs;
    }

    private boolean isTextFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".items") || lowerName.endsWith(".things") || lowerName.endsWith(".rules")
                || lowerName.endsWith(".script") || lowerName.endsWith(".js") || lowerName.endsWith(".py")
                || lowerName.endsWith(".sitemap") || lowerName.endsWith(".persist") || lowerName.endsWith(".cfg")
                || lowerName.endsWith(".config") || lowerName.endsWith(".properties") || lowerName.endsWith(".json")
                || lowerName.endsWith(".xml") || lowerName.endsWith(".yaml") || lowerName.endsWith(".yml")
                || lowerName.endsWith(".txt") || lowerName.endsWith(".map") || lowerName.endsWith(".scale")
                || lowerName.endsWith(".transform");
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
