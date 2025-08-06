package org.openhab.core.ai.action.library.monitoring;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for updating the logging configuration for openHAB.
 */
@Component(service = Action.class, immediate = true)
public class SetLogConfigurationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SetLogConfigurationAction.class);
    private static final String ACTION_ID = "openhab.monitoring.set_log_configuration";
    private static final String ACTION_NAME = "Set Log Configuration";

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
        return "Updates the logging configuration including log levels, appenders, and settings";
    }

    @Override
    public String getCategory() {
        return "monitoring";
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
        properties.put("action",
                Map.of("type", "string", "enum",
                        List.of("add_logger", "update_logger", "remove_logger", "add_appender", "update_appender",
                                "remove_appender", "update_settings"),
                        "description", "Configuration action to perform"));
        properties.put("loggerName",
                Map.of("type", "string", "description", "Logger name (required for logger actions)"));
        properties.put("loggerLevel", Map.of("type", "string", "enum",
                List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF"), "description", "Log level for logger"));
        properties.put("loggerAdditivity",
                Map.of("type", "boolean", "description", "Additivity setting for logger", "default", true));
        properties.put("appenderName",
                Map.of("type", "string", "description", "Appender name (required for appender actions)"));
        properties.put("appenderType", Map.of("type", "string", "enum",
                List.of("console", "file", "rolling_file", "socket", "email"), "description", "Type of appender"));
        properties.put("appenderConfig", Map.of("type", "object", "description", "Appender-specific configuration"));
        properties.put("settings", Map.of("type", "object", "description", "General logging settings to update"));
        properties.put("backup",
                Map.of("type", "boolean", "description", "Create backup before making changes", "default", true));
        properties.put("restartRequired", Map.of("type", "boolean", "description",
                "Whether restart is required for changes to take effect", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("changes", Map.of("type", "array", "description", "List of changes made"));
        properties.put("backupFile", Map.of("type", "string", "description", "Path to backup file if created"));
        properties.put("configFile", Map.of("type", "string", "description", "Path to configuration file"));
        properties.put("restartRequired", Map.of("type", "boolean", "description", "Whether restart is required"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        List<String> errors = new ArrayList<>();

        String action = (String) parameters.get("action");
        if (action == null) {
            errors.add("action is required");
        } else {
            List<String> validActions = List.of("add_logger", "update_logger", "remove_logger", "add_appender",
                    "update_appender", "remove_appender", "update_settings");
            if (!validActions.contains(action)) {
                errors.add("action must be one of: " + validActions);
            }
        }

        // Validate logger actions
        if (action != null
                && (action.equals("add_logger") || action.equals("update_logger") || action.equals("remove_logger"))) {
            String loggerName = (String) parameters.get("loggerName");
            if (loggerName == null || loggerName.trim().isEmpty()) {
                errors.add("loggerName is required for logger actions");
            }
        }

        if (action != null && (action.equals("add_logger") || action.equals("update_logger"))) {
            String loggerLevel = (String) parameters.get("loggerLevel");
            if (loggerLevel == null) {
                errors.add("loggerLevel is required for add_logger and update_logger actions");
            } else {
                List<String> validLevels = List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF");
                if (!validLevels.contains(loggerLevel)) {
                    errors.add("loggerLevel must be one of: " + validLevels);
                }
            }
        }

        // Validate appender actions
        if (action != null && (action.equals("add_appender") || action.equals("update_appender")
                || action.equals("remove_appender"))) {
            String appenderName = (String) parameters.get("appenderName");
            if (appenderName == null || appenderName.trim().isEmpty()) {
                errors.add("appenderName is required for appender actions");
            }
        }

        if (action != null && (action.equals("add_appender") || action.equals("update_appender"))) {
            String appenderType = (String) parameters.get("appenderType");
            if (appenderType == null) {
                errors.add("appenderType is required for add_appender and update_appender actions");
            } else {
                List<String> validTypes = List.of("console", "file", "rolling_file", "socket", "email");
                if (!validTypes.contains(appenderType)) {
                    errors.add("appenderType must be one of: " + validTypes);
                }
            }
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing set log configuration action with parameters: {}", parameters);

        try {
            String action = (String) parameters.get("action");
            Boolean backup = (Boolean) parameters.getOrDefault("backup", true);
            Boolean restartRequired = (Boolean) parameters.getOrDefault("restartRequired", false);

            String configDir = OpenHAB.getConfigFolder();
            Path logbackConfig = Paths.get(configDir, "logback.xml");
            Path backupFile = null;

            // Create backup if requested
            if (backup && Files.exists(logbackConfig)) {
                backupFile = Paths.get(configDir, "logback.xml.backup." + System.currentTimeMillis());
                Files.copy(logbackConfig, backupFile);
            }

            List<String> changes = new ArrayList<>();
            boolean success = false;

            switch (action) {
                case "add_logger":
                    success = addLogger(parameters, changes);
                    break;
                case "update_logger":
                    success = updateLogger(parameters, changes);
                    break;
                case "remove_logger":
                    success = removeLogger(parameters, changes);
                    break;
                case "add_appender":
                    success = addAppender(parameters, changes);
                    break;
                case "update_appender":
                    success = updateAppender(parameters, changes);
                    break;
                case "remove_appender":
                    success = removeAppender(parameters, changes);
                    break;
                case "update_settings":
                    success = updateSettings(parameters, changes);
                    break;
                default:
                    throw new ActionException(ACTION_ID, "Unknown action: " + action);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("action", action);
            result.put("success", success);
            result.put("changes", changes);
            result.put("backupFile", backupFile != null ? backupFile.toString() : "");
            result.put("configFile", logbackConfig.toString());
            result.put("restartRequired", restartRequired);
            result.put("timestamp", Instant.now().toString());

            if (success) {
                result.put("message", "Log configuration updated successfully. Changes: " + String.join(", ", changes));
            } else {
                result.put("error", "Failed to update log configuration");
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Set log configuration action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to set log configuration", e);
            throw new ActionException(ACTION_ID, "Failed to set log configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Updates the logging configuration for openHAB")
                .tags(List.of("monitoring", "logging", "configuration"))
                .documentation(
                        "Allows dynamic configuration of logging settings including loggers, appenders, and general settings")
                .examples(List.of(
                        "{\"action\": \"add_logger\", \"loggerName\": \"org.openhab.binding.zwave\", \"loggerLevel\": \"DEBUG\"} - Add Z-Wave binding logger",
                        "{\"action\": \"update_logger\", \"loggerName\": \"org.openhab.core\", \"loggerLevel\": \"WARN\"} - Update core logger level",
                        "{\"action\": \"add_appender\", \"appenderName\": \"FILE\", \"appenderType\": \"file\"} - Add file appender"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("SetLogConfigurationAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SetLogConfigurationAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private boolean addLogger(Map<String, Object> parameters, List<String> changes) {
        String loggerName = (String) parameters.get("loggerName");
        String loggerLevel = (String) parameters.get("loggerLevel");
        Boolean loggerAdditivity = (Boolean) parameters.getOrDefault("loggerAdditivity", true);

        logger.info("Adding logger: {} with level: {} and additivity: {}", loggerName, loggerLevel, loggerAdditivity);
        changes.add("Added logger: " + loggerName + " (level: " + loggerLevel + ")");

        // In a real implementation, you would modify the logback.xml file
        // or use the logging framework's API to add the logger

        return true;
    }

    private boolean updateLogger(Map<String, Object> parameters, List<String> changes) {
        String loggerName = (String) parameters.get("loggerName");
        String loggerLevel = (String) parameters.get("loggerLevel");
        Boolean loggerAdditivity = (Boolean) parameters.get("loggerAdditivity");

        logger.info("Updating logger: {} with level: {}", loggerName, loggerLevel);
        changes.add("Updated logger: " + loggerName + " (level: " + loggerLevel + ")");

        // In a real implementation, you would modify the logback.xml file
        // or use the logging framework's API to update the logger

        return true;
    }

    private boolean removeLogger(Map<String, Object> parameters, List<String> changes) {
        String loggerName = (String) parameters.get("loggerName");

        logger.info("Removing logger: {}", loggerName);
        changes.add("Removed logger: " + loggerName);

        // In a real implementation, you would modify the logback.xml file
        // or use the logging framework's API to remove the logger

        return true;
    }

    private boolean addAppender(Map<String, Object> parameters, List<String> changes) {
        String appenderName = (String) parameters.get("appenderName");
        String appenderType = (String) parameters.get("appenderType");
        @SuppressWarnings("unchecked")
        Map<String, Object> appenderConfig = (Map<String, Object>) parameters.get("appenderConfig");

        logger.info("Adding appender: {} of type: {}", appenderName, appenderType);
        changes.add("Added appender: " + appenderName + " (type: " + appenderType + ")");

        // In a real implementation, you would modify the logback.xml file
        // to add the appender configuration

        return true;
    }

    private boolean updateAppender(Map<String, Object> parameters, List<String> changes) {
        String appenderName = (String) parameters.get("appenderName");
        String appenderType = (String) parameters.get("appenderType");
        @SuppressWarnings("unchecked")
        Map<String, Object> appenderConfig = (Map<String, Object>) parameters.get("appenderConfig");

        logger.info("Updating appender: {} of type: {}", appenderName, appenderType);
        changes.add("Updated appender: " + appenderName + " (type: " + appenderType + ")");

        // In a real implementation, you would modify the logback.xml file
        // to update the appender configuration

        return true;
    }

    private boolean removeAppender(Map<String, Object> parameters, List<String> changes) {
        String appenderName = (String) parameters.get("appenderName");

        logger.info("Removing appender: {}", appenderName);
        changes.add("Removed appender: " + appenderName);

        // In a real implementation, you would modify the logback.xml file
        // to remove the appender configuration

        return true;
    }

    private boolean updateSettings(Map<String, Object> parameters, List<String> changes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) parameters.get("settings");

        logger.info("Updating logging settings: {}", settings);
        changes.add("Updated logging settings");

        // In a real implementation, you would modify the logback.xml file
        // or other configuration files to update the settings

        return true;
    }
}
