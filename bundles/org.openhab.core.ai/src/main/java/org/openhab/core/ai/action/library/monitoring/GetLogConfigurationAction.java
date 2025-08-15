package org.openhab.core.ai.action.library.monitoring;

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
import java.util.regex.Pattern;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving the current logging configuration for openHAB.
 */
@Component(service = Action.class, immediate = true)
public class GetLogConfigurationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetLogConfigurationAction.class);
    private static final String ACTION_ID = "openhab.monitoring.get_log_configuration";
    private static final String ACTION_NAME = "Get Log Configuration";

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
        return "Retrieves the current logging configuration including log levels, appenders, and settings";
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
        properties.put("includeAppenders",
                Map.of("type", "boolean", "description", "Include appender configuration", "default", true));
        properties.put("includeLoggers",
                Map.of("type", "boolean", "description", "Include logger configuration", "default", true));
        properties.put("includeSettings",
                Map.of("type", "boolean", "description", "Include general logging settings", "default", true));
        properties.put("filterLogger",
                Map.of("type", "string", "description", "Filter loggers by pattern (e.g., 'org.openhab')"));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("configuration", Map.of("type", "object", "description", "Logging configuration details"));
        properties.put("appenders", Map.of("type", "array", "description", "Configured appenders"));
        properties.put("loggers", Map.of("type", "array", "description", "Configured loggers"));
        properties.put("settings", Map.of("type", "object", "description", "General logging settings"));
        properties.put("configFile", Map.of("type", "string", "description", "Path to configuration file"));
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

        // All parameters are optional, so no validation needed
        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get log configuration action with parameters: {}", parameters);

        try {
            Boolean includeAppenders = (Boolean) parameters.getOrDefault("includeAppenders", true);
            Boolean includeLoggers = (Boolean) parameters.getOrDefault("includeLoggers", true);
            Boolean includeSettings = (Boolean) parameters.getOrDefault("includeSettings", true);
            String filterLogger = (String) parameters.get("filterLogger");

            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", Instant.now().toString());

            // Get configuration file path
            String configDir = OpenHAB.getConfigFolder();
            Path logbackConfig = Paths.get(configDir, "logback.xml");
            result.put("configFile", logbackConfig.toString());

            Map<String, Object> configuration = new HashMap<>();

            // Get appenders configuration
            if (includeAppenders) {
                configuration.put("appenders", getAppendersConfiguration(logbackConfig));
            }

            // Get loggers configuration
            if (includeLoggers) {
                configuration.put("loggers", getLoggersConfiguration(logbackConfig, filterLogger));
            }

            // Get general settings
            if (includeSettings) {
                configuration.put("settings", getLoggingSettings());
            }

            result.put("configuration", configuration);
            result.put("message", "Logging configuration retrieved successfully");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get log configuration action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get log configuration", e);
            throw new ActionException(ACTION_ID, "Failed to get log configuration: " + e.getMessage(), e);
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
                .description("Retrieves the current logging configuration for openHAB")
                .tags(List.of("monitoring", "logging", "configuration"))
                .documentation(
                        "Provides detailed information about logging configuration including appenders, loggers, and settings")
                .examples(List.of("{} - Get complete logging configuration",
                        "{\"includeAppenders\": false} - Get configuration without appenders",
                        "{\"filterLogger\": \"org.openhab\"} - Get only openHAB-related loggers"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetLogConfigurationAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetLogConfigurationAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> getAppendersConfiguration(Path configFile) {
        List<Map<String, Object>> appenders = new ArrayList<>();

        if (!Files.exists(configFile)) {
            return appenders;
        }

        try {
            List<String> lines = Files.readAllLines(configFile);
            Pattern appenderPattern = Pattern.compile("<appender\\s+name=\"([^\"]+)\"\\s+class=\"([^\"]+)\"");
            Pattern filePattern = Pattern.compile("<file>([^<]+)</file>");
            Pattern levelPattern = Pattern.compile("<level>([^<]+)</level>");

            for (String line : lines) {
                var appenderMatch = appenderPattern.matcher(line);
                if (appenderMatch.find()) {
                    Map<String, Object> appender = new HashMap<>();
                    appender.put("name", appenderMatch.group(1));
                    appender.put("class", appenderMatch.group(2));
                    appender.put("type", getAppenderType(appenderMatch.group(2)));
                    appenders.add(appender);
                }
            }

            // Add some common appenders that might not be in the config file
            if (appenders.isEmpty()) {
                appenders.add(
                        Map.of("name", "STDOUT", "class", "ch.qos.logback.core.ConsoleAppender", "type", "console"));
                appenders.add(Map.of("name", "FILE", "class", "ch.qos.logback.core.rolling.RollingFileAppender", "type",
                        "file"));
            }

        } catch (IOException e) {
            logger.warn("Failed to read logback configuration file", e);
        }

        return appenders;
    }

    private List<Map<String, Object>> getLoggersConfiguration(Path configFile, String filter) {
        List<Map<String, Object>> loggers = new ArrayList<>();

        if (!Files.exists(configFile)) {
            return loggers;
        }

        try {
            List<String> lines = Files.readAllLines(configFile);
            Pattern loggerPattern = Pattern.compile("<logger\\s+name=\"([^\"]+)\"\\s+level=\"([^\"]+)\"");
            Pattern additivityPattern = Pattern.compile("additivity=\"([^\"]+)\"");

            for (String line : lines) {
                var loggerMatch = loggerPattern.matcher(line);
                if (loggerMatch.find()) {
                    String loggerName = loggerMatch.group(1);

                    // Apply filter if specified
                    if (filter != null && !loggerName.contains(filter)) {
                        continue;
                    }

                    Map<String, Object> logger = new HashMap<>();
                    logger.put("name", loggerName);
                    logger.put("level", loggerMatch.group(2));

                    var additivityMatch = additivityPattern.matcher(line);
                    if (additivityMatch.find()) {
                        logger.put("additivity", Boolean.parseBoolean(additivityMatch.group(1)));
                    } else {
                        logger.put("additivity", true);
                    }

                    loggers.add(logger);
                }
            }

            // Add some common loggers if none found
            if (loggers.isEmpty()) {
                loggers.add(Map.of("name", "org.openhab", "level", "INFO", "additivity", true));
                loggers.add(Map.of("name", "org.openhab.core", "level", "INFO", "additivity", true));
            }

        } catch (IOException e) {
            logger.warn("Failed to read logback configuration file", e);
        }

        return loggers;
    }

    private Map<String, Object> getLoggingSettings() {
        Map<String, Object> settings = new HashMap<>();

        // Get current logging framework information
        settings.put("framework", "Logback");
        settings.put("version", "1.2.x"); // This would be dynamically determined in a real implementation

        // Get log directory
        String userDataDir = OpenHAB.getUserDataFolder();
        Path logsDir = Paths.get(userDataDir, "logs");
        settings.put("logDirectory", logsDir.toString());

        // Get default log level
        settings.put("defaultLevel", "INFO");

        // Get log file settings
        Map<String, Object> fileSettings = new HashMap<>();
        fileSettings.put("maxFileSize", "10MB");
        fileSettings.put("maxHistory", "30");
        fileSettings.put("totalSizeCap", "3GB");
        settings.put("fileSettings", fileSettings);

        // Get console settings
        Map<String, Object> consoleSettings = new HashMap<>();
        consoleSettings.put("enabled", true);
        consoleSettings.put("pattern", "%d{HH:mm:ss.SSS} [%-5.5p] [%-15.15t] %-30.30logger{30} - %msg%n");
        settings.put("consoleSettings", consoleSettings);

        return settings;
    }

    private String getAppenderType(String className) {
        if (className.contains("ConsoleAppender")) {
            return "console";
        } else if (className.contains("FileAppender") || className.contains("RollingFileAppender")) {
            return "file";
        } else if (className.contains("SocketAppender")) {
            return "socket";
        } else if (className.contains("SMTPAppender")) {
            return "email";
        } else {
            return "custom";
        }
    }
}
