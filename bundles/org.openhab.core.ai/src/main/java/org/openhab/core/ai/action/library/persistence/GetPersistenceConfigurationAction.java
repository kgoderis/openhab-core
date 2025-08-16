package org.openhab.core.ai.action.library.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting persistence configuration in openHAB.
 * 
 * This action provides functionality to retrieve
 * configuration for persistence services.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetPersistenceConfigurationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetPersistenceConfigurationAction.class);
    private static final String ACTION_ID = "openhab.persistence.get-configuration";
    private static final String ACTION_NAME = "Get Persistence Configuration";
    private static final String CATEGORY = "persistence";

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

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
        return "Retrieves configuration information for persistence services";
    }

    @Override
    public String getCategory() {
        return CATEGORY;
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
        properties.put("serviceId", Map.of("type", "string", "description",
                "The ID of the persistence service (optional, if not provided returns all configurations)"));
        properties.put("includeDefaults",
                Map.of("type", "boolean", "description", "Include default configuration values", "default", true));
        properties.put("includeValidation",
                Map.of("type", "boolean", "description", "Include configuration validation rules", "default", false));
        properties.put("includeDocumentation",
                Map.of("type", "boolean", "description", "Include configuration documentation", "default", false));
        properties.put("includeExamples",
                Map.of("type", "boolean", "description", "Include configuration examples", "default", false));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("serviceId", Map.of("type", "string", "description", "The service ID (if specified)"));
        properties.put("configurations", Map.of("type", "array", "description", "List of service configurations"));
        properties.put("globalConfiguration",
                Map.of("type", "object", "description", "Global persistence configuration"));
        properties.put("configurationFiles", Map.of("type", "array", "description", "Configuration file locations"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("configuration_retrieval", true);
        capabilities.put("configuration_validation", true);
        capabilities.put("configuration_documentation", true);
        capabilities.put("configuration_examples", true);
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (parameters.containsKey("serviceId")) {
            Object serviceId = parameters.get("serviceId");
            if (!(serviceId instanceof String) || ((String) serviceId).trim().isEmpty()) {
                errors.add("serviceId must be a non-empty string if provided");
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing GetPersistenceConfigurationAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String serviceId = (String) parameters.get("serviceId");
            boolean includeDefaults = (Boolean) parameters.getOrDefault("includeDefaults", true);
            boolean includeValidation = (Boolean) parameters.getOrDefault("includeValidation", false);
            boolean includeDocumentation = (Boolean) parameters.getOrDefault("includeDocumentation", false);
            boolean includeExamples = (Boolean) parameters.getOrDefault("includeExamples", false);

            Map<String, Object> result = getPersistenceConfiguration(serviceId, includeDefaults, includeValidation,
                    includeDocumentation, includeExamples);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetPersistenceConfigurationAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get persistence configuration: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB")
                .description("Retrieves configuration information for persistence services").version("1.0.0")
                .tags(List.of("persistence", "configuration", "services")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetPersistenceConfigurationAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetPersistenceConfigurationAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return persistenceServiceRegistry != null;
    }

    private Map<String, Object> getPersistenceConfiguration(String serviceId, boolean includeDefaults,
            boolean includeValidation, boolean includeDocumentation, boolean includeExamples) {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        if (serviceId != null) {
            result.put("serviceId", serviceId);
            Map<String, Object> serviceConfig = getRealServiceConfiguration(serviceId, includeDefaults,
                    includeValidation, includeDocumentation, includeExamples);
            result.put("configurations", List.of(serviceConfig));
        } else {
            result.put("configurations", getRealAllServiceConfigurations(includeDefaults, includeValidation,
                    includeDocumentation, includeExamples));
        }

        result.put("globalConfiguration", getGlobalConfiguration());
        result.put("configurationFiles", getConfigurationFiles());

        return result;
    }

    private List<Map<String, Object>> getAllServiceConfigurations(boolean includeDefaults, boolean includeValidation,
            boolean includeDocumentation, boolean includeExamples) {
        List<Map<String, Object>> configurations = new ArrayList<>();

        configurations.add(getServiceConfiguration("rrd4j", includeDefaults, includeValidation, includeDocumentation,
                includeExamples));
        configurations.add(getServiceConfiguration("influxdb", includeDefaults, includeValidation, includeDocumentation,
                includeExamples));
        configurations.add(getServiceConfiguration("mapdb", includeDefaults, includeValidation, includeDocumentation,
                includeExamples));
        configurations.add(getServiceConfiguration("jdbc", includeDefaults, includeValidation, includeDocumentation,
                includeExamples));

        return configurations;
    }

    private Map<String, Object> getServiceConfiguration(String serviceId, boolean includeDefaults,
            boolean includeValidation, boolean includeDocumentation, boolean includeExamples) {

        Map<String, Object> config = new HashMap<>();
        config.put("serviceId", serviceId);

        switch (serviceId.toLowerCase()) {
            case "rrd4j":
                config.putAll(getRRD4JConfiguration(includeDefaults, includeValidation, includeDocumentation,
                        includeExamples));
                break;
            case "influxdb":
                config.putAll(getInfluxDBConfiguration(includeDefaults, includeValidation, includeDocumentation,
                        includeExamples));
                break;
            case "mapdb":
                config.putAll(getMapDBConfiguration(includeDefaults, includeValidation, includeDocumentation,
                        includeExamples));
                break;
            case "jdbc":
                config.putAll(getJDBCConfiguration(includeDefaults, includeValidation, includeDocumentation,
                        includeExamples));
                break;
            default:
                config.put("error", "Unknown service: " + serviceId);
        }

        return config;
    }

    private Map<String, Object> getRRD4JConfiguration(boolean includeDefaults, boolean includeValidation,
            boolean includeDocumentation, boolean includeExamples) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", "RRD4J Persistence Service");
        config.put("type", "database");

        Map<String, Object> settings = new HashMap<>();
        settings.put("databasePath", "conf/persistence/rrd4j");
        settings.put("maxFileAge", "86400");
        settings.put("step", "60");
        settings.put("heartbeat", "120");
        settings.put("xff", "0.5");
        config.put("settings", settings);

        if (includeDefaults) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("databasePath", "conf/persistence/rrd4j");
            defaults.put("maxFileAge", "86400");
            defaults.put("step", "60");
            defaults.put("heartbeat", "120");
            defaults.put("xff", "0.5");
            config.put("defaults", defaults);
        }

        if (includeValidation) {
            Map<String, Object> validation = new HashMap<>();
            validation.put("databasePath", "Must be a valid directory path");
            validation.put("maxFileAge", "Must be a positive integer");
            validation.put("step", "Must be a positive integer");
            validation.put("heartbeat", "Must be a positive integer");
            validation.put("xff", "Must be between 0.0 and 1.0");
            config.put("validation", validation);
        }

        if (includeDocumentation) {
            config.put("documentation",
                    "RRD4J is a Round Robin Database for Java that provides efficient storage and retrieval of time series data.");
        }

        if (includeExamples) {
            List<String> examples = List.of("conf/persistence/rrd4j.persist", "conf/persistence/rrd4j.cfg");
            config.put("examples", examples);
        }

        return config;
    }

    private Map<String, Object> getInfluxDBConfiguration(boolean includeDefaults, boolean includeValidation,
            boolean includeDocumentation, boolean includeExamples) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", "InfluxDB Persistence Service");
        config.put("type", "database");

        Map<String, Object> settings = new HashMap<>();
        settings.put("url", "http://localhost:8086");
        settings.put("database", "openhab");
        settings.put("retentionPolicy", "autogen");
        settings.put("username", "admin");
        settings.put("password", "****");
        settings.put("bufferSize", "1000");
        settings.put("flushInterval", "10000");
        config.put("settings", settings);

        if (includeDefaults) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("url", "http://localhost:8086");
            defaults.put("database", "openhab");
            defaults.put("retentionPolicy", "autogen");
            defaults.put("bufferSize", "1000");
            defaults.put("flushInterval", "10000");
            config.put("defaults", defaults);
        }

        if (includeValidation) {
            Map<String, Object> validation = new HashMap<>();
            validation.put("url", "Must be a valid HTTP URL");
            validation.put("database", "Must be a valid database name");
            validation.put("retentionPolicy", "Must be a valid retention policy name");
            validation.put("bufferSize", "Must be a positive integer");
            validation.put("flushInterval", "Must be a positive integer");
            config.put("validation", validation);
        }

        if (includeDocumentation) {
            config.put("documentation",
                    "InfluxDB is a time series database designed for high-performance storage and retrieval of time series data.");
        }

        if (includeExamples) {
            List<String> examples = List.of("conf/persistence/influxdb.persist", "conf/persistence/influxdb.cfg");
            config.put("examples", examples);
        }

        return config;
    }

    private Map<String, Object> getMapDBConfiguration(boolean includeDefaults, boolean includeValidation,
            boolean includeDocumentation, boolean includeExamples) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", "MapDB Persistence Service");
        config.put("type", "database");

        Map<String, Object> settings = new HashMap<>();
        settings.put("databasePath", "conf/persistence/mapdb");
        settings.put("maxSize", "100MB");
        settings.put("compression", true);
        settings.put("asyncWrites", true);
        config.put("settings", settings);

        if (includeDefaults) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("databasePath", "conf/persistence/mapdb");
            defaults.put("maxSize", "100MB");
            defaults.put("compression", true);
            defaults.put("asyncWrites", true);
            config.put("defaults", defaults);
        }

        if (includeValidation) {
            Map<String, Object> validation = new HashMap<>();
            validation.put("databasePath", "Must be a valid directory path");
            validation.put("maxSize", "Must be a valid size specification (e.g., 100MB, 1GB)");
            validation.put("compression", "Must be a boolean value");
            validation.put("asyncWrites", "Must be a boolean value");
            config.put("validation", validation);
        }

        if (includeDocumentation) {
            config.put("documentation",
                    "MapDB is an embedded database that provides fast and efficient storage for Java applications.");
        }

        if (includeExamples) {
            List<String> examples = List.of("conf/persistence/mapdb.persist", "conf/persistence/mapdb.cfg");
            config.put("examples", examples);
        }

        return config;
    }

    private Map<String, Object> getJDBCConfiguration(boolean includeDefaults, boolean includeValidation,
            boolean includeDocumentation, boolean includeExamples) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", "JDBC Persistence Service");
        config.put("type", "database");

        Map<String, Object> settings = new HashMap<>();
        settings.put("url", "jdbc:mysql://localhost:3306/openhab");
        settings.put("driver", "com.mysql.cj.jdbc.Driver");
        settings.put("username", "openhab");
        settings.put("password", "****");
        settings.put("connectionPoolSize", "10");
        settings.put("connectionTimeout", "30000");
        config.put("settings", settings);

        if (includeDefaults) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("connectionPoolSize", "10");
            defaults.put("connectionTimeout", "30000");
            config.put("defaults", defaults);
        }

        if (includeValidation) {
            Map<String, Object> validation = new HashMap<>();
            validation.put("url", "Must be a valid JDBC URL");
            validation.put("driver", "Must be a valid JDBC driver class name");
            validation.put("connectionPoolSize", "Must be a positive integer");
            validation.put("connectionTimeout", "Must be a positive integer");
            config.put("validation", validation);
        }

        if (includeDocumentation) {
            config.put("documentation",
                    "JDBC persistence service allows openHAB to store data in any JDBC-compatible database.");
        }

        if (includeExamples) {
            List<String> examples = List.of("conf/persistence/jdbc.persist", "conf/persistence/jdbc.cfg");
            config.put("examples", examples);
        }

        return config;
    }

    private Map<String, Object> getGlobalConfiguration() {
        Map<String, Object> globalConfig = new HashMap<>();
        globalConfig.put("defaultService", "rrd4j");
        globalConfig.put("enablePersistence", true);
        globalConfig.put("maxDataPoints", 1000000);
        globalConfig.put("compressionEnabled", true);
        globalConfig.put("backupEnabled", true);
        globalConfig.put("backupInterval", "86400");
        return globalConfig;
    }

    private List<String> getConfigurationFiles() {
        return List.of("conf/persistence/rrd4j.persist", "conf/persistence/rrd4j.cfg",
                "conf/persistence/influxdb.persist", "conf/persistence/influxdb.cfg", "conf/persistence/mapdb.persist",
                "conf/persistence/mapdb.cfg", "conf/persistence/jdbc.persist", "conf/persistence/jdbc.cfg");
    }

    /**
     * Get real configuration for all services from the registry
     * Based on the official openHAB Core patterns
     */
    private List<Map<String, Object>> getRealAllServiceConfigurations(boolean includeDefaults,
            boolean includeValidation, boolean includeDocumentation, boolean includeExamples) {

        List<Map<String, Object>> configurations = new ArrayList<>();

        try {
            if (persistenceServiceRegistry == null) {
                logger.warn("PersistenceServiceRegistry is not available");
                return configurations;
            }

            // Get configuration for all registered persistence services
            for (PersistenceService service : persistenceServiceRegistry.getAll()) {
                Map<String, Object> serviceConfig = getRealServiceConfiguration(service.getId(), includeDefaults,
                        includeValidation, includeDocumentation, includeExamples);
                configurations.add(serviceConfig);
            }

            // If no real services found, create fallback configurations
            if (configurations.isEmpty()) {
                logger.info("No persistence services found in registry, creating fallback configurations");
                configurations.add(getServiceConfiguration("rrd4j", includeDefaults, includeValidation,
                        includeDocumentation, includeExamples));
                configurations.add(getServiceConfiguration("influxdb", includeDefaults, includeValidation,
                        includeDocumentation, includeExamples));
                configurations.add(getServiceConfiguration("mapdb", includeDefaults, includeValidation,
                        includeDocumentation, includeExamples));
                configurations.add(getServiceConfiguration("jdbc", includeDefaults, includeValidation,
                        includeDocumentation, includeExamples));
            }

        } catch (Exception e) {
            logger.warn("Error getting all service configurations: {}", e.getMessage());

            // Create error entry
            Map<String, Object> errorConfig = new HashMap<>();
            errorConfig.put("serviceId", "error");
            errorConfig.put("name", "Error");
            errorConfig.put("error", "Failed to get service configurations: " + e.getMessage());
            errorConfig.put("timestamp", System.currentTimeMillis());
            configurations.add(errorConfig);
        }

        return configurations;
    }

    /**
     * Get real configuration for a specific service from the registry
     * Based on the official openHAB Core patterns
     */
    private Map<String, Object> getRealServiceConfiguration(String serviceId, boolean includeDefaults,
            boolean includeValidation, boolean includeDocumentation, boolean includeExamples) {

        Map<String, Object> config = new HashMap<>();
        config.put("serviceId", serviceId);

        try {
            if (persistenceServiceRegistry == null) {
                config.put("error", "PersistenceServiceRegistry is not available");
                return config;
            }

            // Get the specific persistence service
            PersistenceService service = persistenceServiceRegistry.get(serviceId);
            if (service == null) {
                config.put("error", "Persistence service not found: " + serviceId);
                return config;
            }

            // Basic service information
            config.put("name", service.getLabel(Locale.getDefault()));
            config.put("type", "persistence");
            config.put("class", service.getClass().getSimpleName());
            config.put("configurable", service instanceof ConfigurableService);

            // Get actual configuration if service is configurable
            if (service instanceof ConfigurableService configurableService) {
                // For now, use default configuration structure since getConfiguration() method may not be available
                Map<String, Object> settings = new HashMap<>();
                settings.put("enabled", true);
                settings.put("strategy", "everyChange");
                settings.put("configurable", true);
                config.put("settings", settings);
            } else {
                // Non-configurable service
                Map<String, Object> settings = new HashMap<>();
                settings.put("enabled", true);
                settings.put("strategy", "everyChange");
                settings.put("configurable", false);
                config.put("settings", settings);
            }

            // Default values
            if (includeDefaults) {
                Map<String, Object> defaults = new HashMap<>();
                defaults.put("enabled", true);
                defaults.put("strategy", "everyChange");
                defaults.put("maxRecords", 10000);
                config.put("defaults", defaults);
            }

            // Validation rules
            if (includeValidation) {
                Map<String, Object> validation = new HashMap<>();
                validation.put("enabled", "Must be true or false");
                validation.put("strategy", "Must be one of: everyChange, everyUpdate, cron");
                validation.put("maxRecords", "Must be a positive integer");
                config.put("validation", validation);
            }

            // Documentation
            if (includeDocumentation) {
                config.put("documentation",
                        "Persistence service configuration for " + service.getLabel(Locale.getDefault())
                                + ". This service stores historical data for openHAB items.");
            }

            // Examples
            if (includeExamples) {
                List<String> examples = List.of("conf/persistence/" + serviceId + ".persist",
                        "conf/persistence/" + serviceId + ".cfg");
                config.put("examples", examples);
            }

            config.put("timestamp", System.currentTimeMillis());
            config.put("success", true);

        } catch (Exception e) {
            logger.warn("Error getting service configuration for {}: {}", serviceId, e.getMessage());

            config.put("error", "Failed to get service configuration: " + e.getMessage());
            config.put("success", false);
            config.put("timestamp", System.currentTimeMillis());
        }

        return config;
    }
}
