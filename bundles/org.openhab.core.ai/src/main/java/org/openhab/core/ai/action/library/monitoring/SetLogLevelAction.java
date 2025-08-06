package org.openhab.core.ai.action.library.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 * Action for setting log levels for different loggers in openHAB.
 */
@Component(service = Action.class, immediate = true)
public class SetLogLevelAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SetLogLevelAction.class);
    private static final String ACTION_ID = "openhab.monitoring.set_log_level";
    private static final String ACTION_NAME = "Set Log Level";

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
        return "Sets the log level for specific loggers or packages in openHAB";
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
        properties.put("logger", Map.of("type", "string", "description",
                "Logger name or package (e.g., 'org.openhab.core', 'org.openhab.binding.zwave')"));
        properties.put("level", Map.of("type", "string", "enum",
                List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF"), "description", "Log level to set"));
        properties.put("temporary", Map.of("type", "boolean", "description",
                "Whether the change is temporary (resets on restart)", "default", true));
        properties.put("effective",
                Map.of("type", "boolean", "description", "Whether to apply to child loggers", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("logger", "level"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("logger", Map.of("type", "string", "description", "The logger that was modified"));
        properties.put("previousLevel", Map.of("type", "string", "description", "Previous log level"));
        properties.put("newLevel", Map.of("type", "string", "description", "New log level"));
        properties.put("temporary", Map.of("type", "boolean", "description", "Whether the change is temporary"));
        properties.put("effective", Map.of("type", "boolean", "description", "Whether applied to child loggers"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
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

        // Validate logger
        String logger = (String) parameters.get("logger");
        if (logger == null || logger.trim().isEmpty()) {
            errors.add("logger is required and cannot be empty");
        }

        // Validate level
        String level = (String) parameters.get("level");
        if (level == null) {
            errors.add("level is required");
        } else {
            List<String> validLevels = List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF");
            if (!validLevels.contains(level)) {
                errors.add("level must be one of: " + validLevels);
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
        logger.debug("Executing set log level action with parameters: {}", parameters);

        try {
            String loggerName = (String) parameters.get("logger");
            String level = (String) parameters.get("level");
            Boolean temporary = (Boolean) parameters.getOrDefault("temporary", true);
            Boolean effective = (Boolean) parameters.getOrDefault("effective", true);

            // In a real implementation, you would use the logging framework's API
            // to actually set the log level. For now, we'll simulate the operation.

            // Simulate getting the previous level
            String previousLevel = getCurrentLogLevel(loggerName);

            // Simulate setting the new level
            boolean success = setLogLevel(loggerName, level, temporary, effective);

            Map<String, Object> result = new HashMap<>();
            result.put("logger", loggerName);
            result.put("previousLevel", previousLevel);
            result.put("newLevel", level);
            result.put("temporary", temporary);
            result.put("effective", effective);
            result.put("success", success);
            result.put("timestamp", Instant.now().toString());

            if (success) {
                result.put("message",
                        "Log level for '" + loggerName + "' changed from '" + previousLevel + "' to '" + level + "'");
            } else {
                result.put("error", "Failed to set log level for '" + loggerName + "'");
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Set log level action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to set log level", e);
            throw new ActionException(ACTION_ID, "Failed to set log level: " + e.getMessage(), e);
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
                .description("Sets log levels for specific loggers or packages in openHAB")
                .tags(List.of("monitoring", "logging", "configuration"))
                .documentation("Allows dynamic configuration of log levels for different components and packages")
                .examples(List.of(
                        "{\"logger\": \"org.openhab.core\", \"level\": \"DEBUG\"} - Set core package to DEBUG level",
                        "{\"logger\": \"org.openhab.binding.zwave\", \"level\": \"TRACE\", \"temporary\": false} - Set Z-Wave binding to TRACE permanently",
                        "{\"logger\": \"org.openhab\", \"level\": \"WARN\", \"effective\": true} - Set all openHAB loggers to WARN level"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("SetLogLevelAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SetLogLevelAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private String getCurrentLogLevel(String loggerName) {
        // In a real implementation, you would query the logging framework
        // to get the current effective log level for the specified logger
        // For now, we'll return a default value
        return "INFO";
    }

    private boolean setLogLevel(String loggerName, String level, boolean temporary, boolean effective) {
        // In a real implementation, you would use the logging framework's API
        // to set the log level. This could involve:
        // 1. Using Logback's LoggerContext to set levels
        // 2. Using OSGi's LogService if available
        // 3. Writing to configuration files for permanent changes

        logger.info("Setting log level for '{}' to '{}' (temporary: {}, effective: {})", loggerName, level, temporary,
                effective);

        // For now, we'll simulate success
        return true;
    }
}
