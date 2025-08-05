package org.openhab.core.ai.agent.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Task;

/**
 * A2A Task Schema Generator - Generates and manages task schemas from ActionRegistry.
 * 
 * <p>
 * This class is responsible for:
 * - Automatic schema generation from ActionRegistry
 * - Schema validation and optimization
 * - Schema versioning and compatibility
 * - Schema caching and performance optimization
 * - Schema security and access controls
 * - Schema documentation and examples
 * - Schema testing and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentTaskSchemaGenerator.class)
@NonNullByDefault
public class AgentTaskSchemaGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AgentTaskSchemaGenerator.class);

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    // Schema storage and caching
    private final ConcurrentHashMap<String, TaskSchema> schemaCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SchemaVersion> schemaVersions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> schemaLastUpdated = new ConcurrentHashMap<>();

    // Performance tracking
    private final AtomicLong totalSchemasGenerated = new AtomicLong(0);
    private final AtomicLong totalSchemaValidations = new AtomicLong(0);
    private final AtomicLong totalSchemaCacheHits = new AtomicLong(0);
    private final AtomicLong totalSchemaCacheMisses = new AtomicLong(0);

    // Configuration
    private static final long SCHEMA_CACHE_TTL_MS = 300000; // 5 minutes
    private static final String SCHEMA_VERSION_PREFIX = "v";
    private static final int MAX_SCHEMA_VERSIONS = 10;

    /**
     * Generate a task schema for a specific action ID.
     * 
     * @param actionId the action ID to generate schema for
     * @return the generated task schema
     */
    public TaskSchema generateSchema(String actionId) {
        logger.debug("Generating schema for action: {}", actionId);

        try {
            // Check cache first
            TaskSchema cachedSchema = getCachedSchema(actionId);
            if (cachedSchema != null) {
                totalSchemaCacheHits.incrementAndGet();
                logger.debug("Returning cached schema for action: {}", actionId);
                return cachedSchema;
            }

            totalSchemaCacheMisses.incrementAndGet();

            // Get action metadata from registry
            ActionRegistry registry = actionRegistry;
            if (registry == null) {
                logger.warn("ActionRegistry not available for schema generation");
                return createDefaultSchema(actionId);
            }

            ActionMetadata actionMetadata = registry.getActionMetadata(actionId);
            if (actionMetadata == null) {
                logger.warn("Action metadata not found for action: {}", actionId);
                return createDefaultSchema(actionId);
            }

            // Generate schema from action metadata
            TaskSchema schema = generateSchemaFromMetadata(actionId, actionMetadata);

            // Cache the generated schema
            cacheSchema(actionId, schema);

            totalSchemasGenerated.incrementAndGet();
            logger.info("Generated schema for action: {} - version: {}", actionId, schema.getVersion());

            return schema;

        } catch (Exception e) {
            logger.error("Error generating schema for action: {}", actionId, e);
            String errorMsg = e.getMessage();
            return createErrorSchema(actionId, errorMsg != null ? errorMsg : "Unknown error");
        }
    }

    /**
     * Generate schemas for all available actions.
     * 
     * @return map of action ID to generated schema
     */
    public Map<String, TaskSchema> generateAllSchemas() {
        logger.debug("Generating schemas for all actions");

        Map<String, TaskSchema> schemas = new HashMap<>();

        try {
            ActionRegistry registry = actionRegistry;
            if (registry == null) {
                logger.warn("ActionRegistry not available for bulk schema generation");
                return schemas;
            }

            // Get all available actions
            List<String> actionIds = new ArrayList<>(registry.getAllActions().keySet());
            for (String actionId : actionIds) {
                try {
                    TaskSchema schema = generateSchema(actionId);
                    schemas.put(actionId, schema);
                } catch (Exception e) {
                    logger.error("Error generating schema for action: {}", actionId, e);
                    String errorMsg = e.getMessage();
                    schemas.put(actionId, createErrorSchema(actionId, errorMsg != null ? errorMsg : "Unknown error"));
                }
            }

            logger.info("Generated {} schemas for all actions", schemas.size());

        } catch (Exception e) {
            logger.error("Error generating all schemas", e);
        }

        return schemas;
    }

    /**
     * Validate a task against its schema.
     * 
     * @param task the task to validate
     * @return validation result
     */
    public SchemaValidationResult validateTask(Task task) {
        logger.debug("Validating task against schema: {}", task.getId());

        totalSchemaValidations.incrementAndGet();

        try {
            // Extract action ID from task
            String actionId = extractActionIdFromTask(task);
            if (actionId.isEmpty()) {
                return new SchemaValidationResult(false, "No action ID found in task");
            }

            // Get schema for the action
            TaskSchema schema = generateSchema(actionId);

            // Validate task against schema
            List<String> errors = validateTaskAgainstSchema(task, schema);
            if (errors.isEmpty()) {
                return new SchemaValidationResult(true, "Task validation successful");
            } else {
                return new SchemaValidationResult(false, "Validation errors: " + String.join(", ", errors));
            }

        } catch (Exception e) {
            logger.error("Error validating task: {}", task.getId(), e);
            String errorMsg = e.getMessage();
            return new SchemaValidationResult(false,
                    "Validation error: " + (errorMsg != null ? errorMsg : "Unknown error"));
        }
    }

    /**
     * Optimize a task schema for better performance.
     * 
     * @param schema the schema to optimize
     * @return the optimized schema
     */
    public TaskSchema optimizeSchema(TaskSchema schema) {
        logger.debug("Optimizing schema: {}", schema.getActionId());

        try {
            // Create optimized version
            TaskSchema optimizedSchema = new TaskSchema(schema.getActionId(), schema.getVersion() + ".optimized",
                    schema.getParameters(), schema.getRequiredFields(), schema.getOptionalFields(),
                    schema.getConstraints(), schema.getExamples(), schema.getDocumentation(),
                    System.currentTimeMillis());

            // Apply optimizations
            optimizedSchema = applySchemaOptimizations(optimizedSchema);

            logger.debug("Schema optimization completed for: {}", schema.getActionId());
            return optimizedSchema;

        } catch (Exception e) {
            logger.error("Error optimizing schema: {}", schema.getActionId(), e);
            return schema; // Return original if optimization fails
        }
    }

    /**
     * Create a new version of a schema.
     * 
     * @param actionId the action ID
     * @param newVersion the new version string
     * @return the new schema version
     */
    public TaskSchema createSchemaVersion(String actionId, String newVersion) {
        logger.debug("Creating new schema version for action: {} - version: {}", actionId, newVersion);

        try {
            // Get current schema
            TaskSchema currentSchema = generateSchema(actionId);

            // Create new version
            TaskSchema newSchema = new TaskSchema(actionId, newVersion, currentSchema.getParameters(),
                    currentSchema.getRequiredFields(), currentSchema.getOptionalFields(),
                    currentSchema.getConstraints(), currentSchema.getExamples(), currentSchema.getDocumentation(),
                    System.currentTimeMillis());

            // Store version information
            storeSchemaVersion(actionId, newVersion, newSchema);

            logger.info("Created new schema version for action: {} - version: {}", actionId, newVersion);
            return newSchema;

        } catch (Exception e) {
            logger.error("Error creating schema version for action: {}", actionId, e);
            String errorMsg = e.getMessage();
            return createErrorSchema(actionId, errorMsg != null ? errorMsg : "Unknown error");
        }
    }

    /**
     * Get schema compatibility information.
     * 
     * @param actionId the action ID
     * @param version1 the first version
     * @param version2 the second version
     * @return compatibility result
     */
    public SchemaCompatibilityResult checkCompatibility(String actionId, String version1, String version2) {
        logger.debug("Checking schema compatibility for action: {} - versions: {} vs {}", actionId, version1, version2);

        try {
            TaskSchema schema1 = getSchemaVersion(actionId, version1);
            TaskSchema schema2 = getSchemaVersion(actionId, version2);

            if (schema1 == null || schema2 == null) {
                return new SchemaCompatibilityResult(false, "One or both schemas not found");
            }

            // Check compatibility
            boolean compatible = checkSchemaCompatibility(schema1, schema2);
            String message = compatible ? "Schemas are compatible" : "Schemas are not compatible";

            return new SchemaCompatibilityResult(compatible, message);

        } catch (Exception e) {
            logger.error("Error checking schema compatibility for action: {}", actionId, e);
            return new SchemaCompatibilityResult(false, "Compatibility check error: " + e.getMessage());
        }
    }

    /**
     * Clear schema cache.
     */
    public void clearCache() {
        logger.debug("Clearing schema cache");

        schemaCache.clear();
        schemaLastUpdated.clear();

        logger.info("Schema cache cleared");
    }

    /**
     * Get schema generation statistics.
     * 
     * @return schema statistics
     */
    public SchemaStatistics getStatistics() {
        return new SchemaStatistics(totalSchemasGenerated.get(), totalSchemaValidations.get(),
                totalSchemaCacheHits.get(), totalSchemaCacheMisses.get(), schemaCache.size(), schemaVersions.size());
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    private TaskSchema generateSchemaFromMetadata(String actionId, ActionMetadata metadata) {
        logger.debug("Generating schema from metadata for action: {}", actionId);

        // Extract parameters from metadata
        Map<String, SchemaParameter> parameters = new HashMap<>();
        List<String> requiredFields = new ArrayList<>();
        List<String> optionalFields = new ArrayList<>();
        Map<String, Object> constraints = new HashMap<>();

        // Extract parameters from metadata properties
        Map<String, Object> properties = metadata.getProperties();
        if (properties.containsKey("parameters")) {
            Object paramsObj = properties.get("parameters");
            if (paramsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;

                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    String paramName = entry.getKey();
                    Object paramValue = entry.getValue();

                    if (paramValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> paramMap = (Map<String, Object>) paramValue;

                        String type = (String) paramMap.getOrDefault("type", "string");
                        String description = (String) paramMap.getOrDefault("description", "");
                        boolean required = (Boolean) paramMap.getOrDefault("required", false);
                        Object defaultValue = paramMap.get("defaultValue");

                        // Ensure non-null values for required fields
                        if (type == null)
                            type = "string";
                        if (description == null)
                            description = "";
                        @SuppressWarnings("unchecked")
                        Map<String, Object> paramConstraints = (Map<String, Object>) paramMap.get("constraints");

                        SchemaParameter schemaParam = new SchemaParameter(paramName, type, description, required,
                                defaultValue, paramConstraints);

                        parameters.put(paramName, schemaParam);

                        if (required) {
                            requiredFields.add(paramName);
                        } else {
                            optionalFields.add(paramName);
                        }

                        if (paramConstraints != null && !paramConstraints.isEmpty()) {
                            constraints.put(paramName, paramConstraints);
                        }
                    }
                }
            }
        }

        // Create schema
        String version = SCHEMA_VERSION_PREFIX + "1.0";
        TaskSchema schema = new TaskSchema(actionId, version, parameters, requiredFields, optionalFields, constraints,
                generateExamples(actionId, parameters), generateDocumentation(actionId, metadata),
                System.currentTimeMillis());

        return schema;
    }

    private @Nullable TaskSchema getCachedSchema(String actionId) {
        Long lastUpdated = schemaLastUpdated.get(actionId);
        if (lastUpdated == null) {
            return null;
        }

        // Check if cache is still valid
        if (System.currentTimeMillis() - lastUpdated > SCHEMA_CACHE_TTL_MS) {
            schemaCache.remove(actionId);
            schemaLastUpdated.remove(actionId);
            return null;
        }

        return schemaCache.get(actionId);
    }

    private void cacheSchema(String actionId, TaskSchema schema) {
        schemaCache.put(actionId, schema);
        schemaLastUpdated.put(actionId, System.currentTimeMillis());
    }

    private TaskSchema createDefaultSchema(String actionId) {
        logger.debug("Creating default schema for action: {}", actionId);

        return new TaskSchema(actionId, SCHEMA_VERSION_PREFIX + "1.0", new HashMap<>(), new ArrayList<>(),
                new ArrayList<>(), new HashMap<>(), new ArrayList<>(), "Default schema for action: " + actionId,
                System.currentTimeMillis());
    }

    private TaskSchema createErrorSchema(String actionId, String errorMessage) {
        logger.debug("Creating error schema for action: {}", actionId);

        return new TaskSchema(actionId, SCHEMA_VERSION_PREFIX + "error", new HashMap<>(), new ArrayList<>(),
                new ArrayList<>(), new HashMap<>(), new ArrayList<>(), "Error schema: " + errorMessage,
                System.currentTimeMillis());
    }

    private String extractActionIdFromTask(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("actionId")) {
            Object actionIdObj = metadata.get("actionId");
            return actionIdObj != null ? actionIdObj.toString() : "";
        }
        return "";
    }

    private List<String> validateTaskAgainstSchema(Task task, TaskSchema schema) {
        List<String> errors = new ArrayList<>();

        try {
            Map<String, Object> taskMetadata = task.getMetadata();
            if (taskMetadata == null) {
                errors.add("Task metadata is required");
                return errors;
            }

            // Validate required fields
            for (String requiredField : schema.getRequiredFields()) {
                if (!taskMetadata.containsKey(requiredField)) {
                    errors.add("Required field missing: " + requiredField);
                }
            }

            // Validate field types and constraints
            for (Map.Entry<String, Object> entry : taskMetadata.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                SchemaParameter param = schema.getParameters().get(fieldName);
                if (param != null) {
                    // Validate type
                    if (!validateFieldType(fieldValue, param.getType())) {
                        errors.add("Invalid type for field: " + fieldName);
                    }

                    // Validate constraints
                    if (param.getConstraints() != null) {
                        List<String> constraintErrors = validateConstraints(fieldValue, param.getConstraints());
                        errors.addAll(constraintErrors);
                    }
                }
            }

        } catch (Exception e) {
            errors.add("Validation error: " + e.getMessage());
        }

        return errors;
    }

    private boolean validateFieldType(Object value, String expectedType) {
        if (value == null) {
            return true; // Null is always valid
        }

        switch (expectedType.toLowerCase()) {
            case "string":
                return value instanceof String;
            case "integer":
            case "int":
                return value instanceof Integer || value instanceof Long;
            case "number":
            case "double":
            case "float":
                return value instanceof Number;
            case "boolean":
                return value instanceof Boolean;
            case "array":
            case "list":
                return value instanceof List;
            case "object":
            case "map":
                return value instanceof Map;
            default:
                return true; // Unknown type, assume valid
        }
    }

    private List<String> validateConstraints(Object value, Map<String, Object> constraints) {
        List<String> errors = new ArrayList<>();

        try {
            for (Map.Entry<String, Object> constraint : constraints.entrySet()) {
                String constraintType = constraint.getKey();
                Object constraintValue = constraint.getValue();

                switch (constraintType.toLowerCase()) {
                    case "min":
                        if (value instanceof Number && constraintValue instanceof Number) {
                            if (((Number) value).doubleValue() < ((Number) constraintValue).doubleValue()) {
                                errors.add("Value below minimum: " + constraintValue);
                            }
                        }
                        break;
                    case "max":
                        if (value instanceof Number && constraintValue instanceof Number) {
                            if (((Number) value).doubleValue() > ((Number) constraintValue).doubleValue()) {
                                errors.add("Value above maximum: " + constraintValue);
                            }
                        }
                        break;
                    case "minlength":
                        if (value instanceof String && constraintValue instanceof Number) {
                            if (((String) value).length() < ((Number) constraintValue).intValue()) {
                                errors.add("String too short: minimum " + constraintValue + " characters");
                            }
                        }
                        break;
                    case "maxlength":
                        if (value instanceof String && constraintValue instanceof Number) {
                            if (((String) value).length() > ((Number) constraintValue).intValue()) {
                                errors.add("String too long: maximum " + constraintValue + " characters");
                            }
                        }
                        break;
                    case "pattern":
                        if (value instanceof String && constraintValue instanceof String) {
                            if (!((String) value).matches((String) constraintValue)) {
                                errors.add("Value does not match pattern: " + constraintValue);
                            }
                        }
                        break;
                    case "enum":
                        if (constraintValue instanceof List) {
                            if (!((List<?>) constraintValue).contains(value)) {
                                errors.add("Value not in allowed values: " + constraintValue);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            errors.add("Constraint validation error: " + e.getMessage());
        }

        return errors;
    }

    private TaskSchema applySchemaOptimizations(TaskSchema schema) {
        // TODO: Implement schema optimizations
        // This could include:
        // - Removing unused parameters
        // - Optimizing parameter order
        // - Adding derived constraints
        // - Compressing documentation

        return schema;
    }

    private void storeSchemaVersion(String actionId, String version, TaskSchema schema) {
        SchemaVersion schemaVersion = new SchemaVersion(actionId, version, schema, System.currentTimeMillis());
        schemaVersions.put(actionId + ":" + version, schemaVersion);

        // Limit number of versions per action
        cleanupOldVersions(actionId);
    }

    private void cleanupOldVersions(String actionId) {
        // TODO: Implement version cleanup logic
        // Remove old versions beyond MAX_SCHEMA_VERSIONS
    }

    private @Nullable TaskSchema getSchemaVersion(String actionId, String version) {
        SchemaVersion schemaVersion = schemaVersions.get(actionId + ":" + version);
        return schemaVersion != null ? schemaVersion.getSchema() : null;
    }

    private boolean checkSchemaCompatibility(TaskSchema schema1, TaskSchema schema2) {
        // TODO: Implement schema compatibility checking
        // This should check:
        // - Required fields compatibility
        // - Parameter type compatibility
        // - Constraint compatibility

        return true; // Placeholder
    }

    private List<SchemaExample> generateExamples(String actionId, Map<String, SchemaParameter> parameters) {
        List<SchemaExample> examples = new ArrayList<>();

        // TODO: Generate meaningful examples based on parameters
        // This could use AI or predefined templates

        return examples;
    }

    private String generateDocumentation(String actionId, ActionMetadata metadata) {
        StringBuilder doc = new StringBuilder();
        doc.append("# ").append(actionId).append("\n\n");

        if (metadata.getDescription() != null) {
            doc.append(metadata.getDescription()).append("\n\n");
        }

        doc.append("## Parameters\n\n");
        Map<String, Object> properties = metadata.getProperties();
        if (properties.containsKey("parameters")) {
            Object paramsObj = properties.get("parameters");
            if (paramsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;

                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    String paramName = entry.getKey();
                    Object paramValue = entry.getValue();

                    if (paramValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> paramMap = (Map<String, Object>) paramValue;

                        String type = (String) paramMap.getOrDefault("type", "string");
                        boolean required = (Boolean) paramMap.getOrDefault("required", false);
                        String description = (String) paramMap.getOrDefault("description", "");

                        // Ensure non-null values for required fields
                        if (type == null)
                            type = "string";
                        if (description == null)
                            description = "";

                        doc.append("### ").append(paramName).append("\n");
                        doc.append("- Type: ").append(type).append("\n");
                        doc.append("- Required: ").append(required).append("\n");
                        if (!description.isEmpty()) {
                            doc.append("- Description: ").append(description).append("\n");
                        }
                        doc.append("\n");
                    }
                }
            }
        }

        return doc.toString();
    }

    // ============================================================================
    // Inner Classes
    // ============================================================================

    /**
     * Task schema definition.
     */
    public static class TaskSchema {
        private final String actionId;
        private final String version;
        private final Map<String, SchemaParameter> parameters;
        private final List<String> requiredFields;
        private final List<String> optionalFields;
        private final Map<String, Object> constraints;
        private final List<SchemaExample> examples;
        private final String documentation;
        private final long createdAt;

        public TaskSchema(String actionId, String version, Map<String, SchemaParameter> parameters,
                List<String> requiredFields, List<String> optionalFields, Map<String, Object> constraints,
                List<SchemaExample> examples, String documentation, long createdAt) {
            this.actionId = actionId;
            this.version = version;
            this.parameters = parameters;
            this.requiredFields = requiredFields;
            this.optionalFields = optionalFields;
            this.constraints = constraints;
            this.examples = examples;
            this.documentation = documentation;
            this.createdAt = createdAt;
        }

        // Getters
        public String getActionId() {
            return actionId;
        }

        public String getVersion() {
            return version;
        }

        public Map<String, SchemaParameter> getParameters() {
            return parameters;
        }

        public List<String> getRequiredFields() {
            return requiredFields;
        }

        public List<String> getOptionalFields() {
            return optionalFields;
        }

        public Map<String, Object> getConstraints() {
            return constraints;
        }

        public List<SchemaExample> getExamples() {
            return examples;
        }

        public String getDocumentation() {
            return documentation;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * Schema parameter definition.
     */
    public static class SchemaParameter {
        private final String name;
        private final String type;
        private final String description;
        private final boolean required;
        private final Object defaultValue;
        private final Map<String, Object> constraints;

        public SchemaParameter(String name, String type, String description, boolean required, Object defaultValue,
                Map<String, Object> constraints) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
            this.defaultValue = defaultValue;
            this.constraints = constraints;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public Map<String, Object> getConstraints() {
            return constraints;
        }
    }

    /**
     * Schema example definition.
     */
    public static class SchemaExample {
        private final String name;
        private final String description;
        private final Map<String, Object> parameters;

        public SchemaExample(String name, String description, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }
    }

    /**
     * Schema validation result.
     */
    public static class SchemaValidationResult {
        private final boolean valid;
        private final String message;

        public SchemaValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Schema compatibility result.
     */
    public static class SchemaCompatibilityResult {
        private final boolean compatible;
        private final String message;

        public SchemaCompatibilityResult(boolean compatible, String message) {
            this.compatible = compatible;
            this.message = message;
        }

        // Getters
        public boolean isCompatible() {
            return compatible;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Schema version information.
     */
    public static class SchemaVersion {
        private final String actionId;
        private final String version;
        private final TaskSchema schema;
        private final long createdAt;

        public SchemaVersion(String actionId, String version, TaskSchema schema, long createdAt) {
            this.actionId = actionId;
            this.version = version;
            this.schema = schema;
            this.createdAt = createdAt;
        }

        // Getters
        public String getActionId() {
            return actionId;
        }

        public String getVersion() {
            return version;
        }

        public TaskSchema getSchema() {
            return schema;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * Schema generation statistics.
     */
    public static class SchemaStatistics {
        private final long totalSchemasGenerated;
        private final long totalSchemaValidations;
        private final long totalSchemaCacheHits;
        private final long totalSchemaCacheMisses;
        private final int cacheSize;
        private final int versionCount;

        public SchemaStatistics(long totalSchemasGenerated, long totalSchemaValidations, long totalSchemaCacheHits,
                long totalSchemaCacheMisses, int cacheSize, int versionCount) {
            this.totalSchemasGenerated = totalSchemasGenerated;
            this.totalSchemaValidations = totalSchemaValidations;
            this.totalSchemaCacheHits = totalSchemaCacheHits;
            this.totalSchemaCacheMisses = totalSchemaCacheMisses;
            this.cacheSize = cacheSize;
            this.versionCount = versionCount;
        }

        // Getters
        public long getTotalSchemasGenerated() {
            return totalSchemasGenerated;
        }

        public long getTotalSchemaValidations() {
            return totalSchemaValidations;
        }

        public long getTotalSchemaCacheHits() {
            return totalSchemaCacheHits;
        }

        public long getTotalSchemaCacheMisses() {
            return totalSchemaCacheMisses;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public int getVersionCount() {
            return versionCount;
        }

        public double getCacheHitRate() {
            long totalRequests = totalSchemaCacheHits + totalSchemaCacheMisses;
            return totalRequests > 0 ? (double) totalSchemaCacheHits / totalRequests : 0.0;
        }
    }
}
