package org.openhab.core.ai.tool.api.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default implementation of ValidationService for MCP tools.
 * 
 * This service provides comprehensive validation capabilities for tool configurations,
 * parameters, and schemas, with support for custom validation rules and caching.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultValidationService implements ValidationService {

    private final Map<String, Object> validationRules;
    private final ConcurrentHashMap<String, ValidationResult> validationCache;
    private final List<ValidationListener> listeners;

    /**
     * Create a new validation service.
     */
    public DefaultValidationService() {
        this.validationRules = new HashMap<>();
        this.validationCache = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();

        // Initialize default validation rules
        initializeDefaultRules();
    }

    @Override
    public ValidationResult validateConfiguration(Map<String, Object> configuration) {
        if (configuration == null) {
            return ValidationResult.invalid(List.of("Configuration cannot be null"));
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate required fields
        validateRequiredFields(configuration, errors, warnings);

        // Validate field types
        validateFieldTypes(configuration, errors, warnings);

        // Validate field constraints
        validateFieldConstraints(configuration, errors, warnings);

        boolean isValid = errors.isEmpty();
        Map<String, Object> details = Map.of("errors", errors, "warnings", warnings);
        return new ValidationResult(isValid, errors, warnings, details);
    }

    @Override
    public ValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate parameter types
        validateParameterTypes(parameters, errors, warnings);

        // Validate parameter constraints
        validateParameterConstraints(parameters, errors, warnings);

        // Validate parameter dependencies
        validateParameterDependencies(parameters, errors, warnings);

        boolean isValid = errors.isEmpty();
        Map<String, Object> details = Map.of("errors", errors, "warnings", warnings);
        return new ValidationResult(isValid, errors, warnings, details);
    }

    @Override
    public ValidationResult validateSchema(Map<String, Object> schema) {
        if (schema == null) {
            return ValidationResult.invalid(List.of("Schema cannot be null"));
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate schema structure
        validateSchemaStructure(schema, errors, warnings);

        // Validate schema types
        validateSchemaTypes(schema, errors, warnings);

        // Validate schema constraints
        validateSchemaConstraints(schema, errors, warnings);

        boolean isValid = errors.isEmpty();
        Map<String, Object> details = Map.of("errors", errors, "warnings", warnings);
        return new ValidationResult(isValid, errors, warnings, details);
    }

    @Override
    public boolean isToolValid(String toolId) {
        if (toolId == null || toolId.isEmpty()) {
            return false;
        }

        // Check cache first
        ValidationResult cachedResult = validationCache.get(toolId);
        if (cachedResult != null) {
            return cachedResult.isValid();
        }

        // Implement actual tool validation logic
        try {
            // Load tool configuration
            Map<String, Object> toolConfig = loadToolConfiguration(toolId);
            if (toolConfig == null) {
                return false;
            }

            // Validate tool configuration
            ValidationResult configResult = validateConfiguration(toolConfig);
            if (!configResult.isValid()) {
                return false;
            }

            // Validate tool schema
            Map<String, Object> toolSchema = (Map<String, Object>) toolConfig.get("schema");
            if (toolSchema != null) {
                ValidationResult schemaResult = validateSchema(toolSchema);
                if (!schemaResult.isValid()) {
                    return false;
                }
            }

            // Check tool dependencies
            List<String> dependencies = (List<String>) toolConfig.get("dependencies");
            if (dependencies != null) {
                for (String dependency : dependencies) {
                    if (!isToolValid(dependency)) {
                        return false;
                    }
                }
            }

            // Validate tool permissions
            Map<String, Object> permissions = (Map<String, Object>) toolConfig.get("permissions");
            if (permissions != null) {
                if (!validateToolPermissions(permissions)) {
                    return false;
                }
            }

            // Cache the validation result
            ValidationResult result = ValidationResult.valid();
            validationCache.put(toolId, result);

            return true;

        } catch (Exception e) {
            // Log error and return false
            System.err.println("Error validating tool " + toolId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getValidationRules(String toolType) {
        Map<String, Object> rules = new HashMap<>();

        // Get base rules
        rules.putAll(validationRules);

        // Get tool-specific rules
        Map<String, Object> toolRules = (Map<String, Object>) validationRules.get(toolType);
        if (toolRules != null) {
            rules.putAll(toolRules);
        }

        return rules;
    }

    /**
     * Add a validation listener.
     * 
     * @param listener the listener to add
     */
    public void addListener(ValidationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a validation listener.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(ValidationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Add custom validation rules.
     * 
     * @param toolType the tool type
     * @param rules the validation rules
     */
    public void addValidationRules(String toolType, Map<String, Object> rules) {
        validationRules.put(toolType, rules);
    }

    /**
     * Clear validation cache.
     */
    public void clearCache() {
        validationCache.clear();
    }

    /**
     * Initialize default validation rules.
     */
    private void initializeDefaultRules() {
        // Common validation rules
        Map<String, Object> commonRules = new HashMap<>();
        commonRules.put("maxStringLength", 1000);
        commonRules.put("maxArraySize", 100);
        commonRules.put("maxObjectDepth", 10);
        commonRules.put("allowedTypes", List.of("string", "number", "boolean", "object", "array"));

        validationRules.put("common", commonRules);

        // Tool-specific rules
        Map<String, Object> toolRules = new HashMap<>();
        toolRules.put("maxParameters", 50);
        toolRules.put("maxDescriptionLength", 500);
        toolRules.put("requiredFields", List.of("name", "description", "version"));

        validationRules.put("tool", toolRules);
    }

    /**
     * Validate required fields in configuration.
     * 
     * @param configuration the configuration to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateRequiredFields(Map<String, Object> configuration, List<String> errors, List<String> warnings) {
        List<String> requiredFields = List.of("name", "description", "version");

        for (String field : requiredFields) {
            if (!configuration.containsKey(field) || configuration.get(field) == null) {
                errors.add("Required field '" + field + "' is missing");
            }
        }
    }

    /**
     * Validate field types in configuration.
     * 
     * @param configuration the configuration to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateFieldTypes(Map<String, Object> configuration, List<String> errors, List<String> warnings) {
        for (Map.Entry<String, Object> entry : configuration.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "name":
                case "description":
                case "version":
                    if (!(value instanceof String)) {
                        errors.add("Field '" + key + "' must be a string");
                    }
                    break;
                case "enabled":
                    if (!(value instanceof Boolean)) {
                        errors.add("Field '" + key + "' must be a boolean");
                    }
                    break;
                case "timeout":
                case "maxRetries":
                    if (!(value instanceof Number)) {
                        errors.add("Field '" + key + "' must be a number");
                    }
                    break;
                default:
                    // Unknown field type, skip validation
                    break;
            }
        }
    }

    /**
     * Validate field constraints in configuration.
     * 
     * @param configuration the configuration to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateFieldConstraints(Map<String, Object> configuration, List<String> errors,
            List<String> warnings) {
        // Validate string length constraints
        Object name = configuration.get("name");
        if (name instanceof String && ((String) name).length() > 100) {
            errors.add("Field 'name' must not exceed 100 characters");
        }

        Object description = configuration.get("description");
        if (description instanceof String && ((String) description).length() > 500) {
            warnings.add("Field 'description' should not exceed 500 characters");
        }

        // Validate numeric constraints
        Object timeout = configuration.get("timeout");
        if (timeout instanceof Number) {
            double timeoutValue = ((Number) timeout).doubleValue();
            if (timeoutValue < 0 || timeoutValue > 3600) {
                errors.add("Field 'timeout' must be between 0 and 3600 seconds");
            }
        }
    }

    /**
     * Validate parameter types.
     * 
     * @param parameters the parameters to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateParameterTypes(Map<String, Object> parameters, List<String> errors, List<String> warnings) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null) {
                if (!(value instanceof String || value instanceof Number || value instanceof Boolean
                        || value instanceof Map || value instanceof List)) {
                    errors.add("Parameter '" + key + "' has unsupported type: " + value.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Validate parameter constraints.
     * 
     * @param parameters the parameters to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateParameterConstraints(Map<String, Object> parameters, List<String> errors,
            List<String> warnings) {
        // Implement parameter constraint validation
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.length() > 1000) {
                    errors.add("Parameter '" + key + "' exceeds maximum length of 1000 characters");
                }
            } else if (value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                if (numValue < -1000000 || numValue > 1000000) {
                    warnings.add("Parameter '" + key + "' is outside recommended range (-1000000 to 1000000)");
                }
            } else if (value instanceof List) {
                List<?> listValue = (List<?>) value;
                if (listValue.size() > 100) {
                    errors.add("Parameter '" + key + "' exceeds maximum array size of 100");
                }
            } else if (value instanceof Map) {
                Map<?, ?> mapValue = (Map<?, ?>) value;
                if (mapValue.size() > 50) {
                    warnings.add("Parameter '" + key + "' has large object size (" + mapValue.size() + " properties)");
                }
            }
        }
    }

    /**
     * Validate parameter dependencies.
     * 
     * @param parameters the parameters to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateParameterDependencies(Map<String, Object> parameters, List<String> errors,
            List<String> warnings) {
        // Implement parameter dependency validation

        // Check for required parameter combinations
        if (parameters.containsKey("username") && !parameters.containsKey("password")) {
            warnings.add("Parameter 'username' is provided but 'password' is missing");
        }

        if (parameters.containsKey("password") && !parameters.containsKey("username")) {
            warnings.add("Parameter 'password' is provided but 'username' is missing");
        }

        // Check for mutually exclusive parameters
        if (parameters.containsKey("file") && parameters.containsKey("data")) {
            warnings.add("Parameters 'file' and 'data' are mutually exclusive - only one should be provided");
        }

        // Check for parameter precedence rules
        if (parameters.containsKey("priority") && parameters.containsKey("urgent")) {
            Object priority = parameters.get("priority");
            Object urgent = parameters.get("urgent");

            if (priority instanceof Number && urgent instanceof Boolean) {
                double priorityValue = ((Number) priority).doubleValue();
                boolean urgentValue = (Boolean) urgent;

                if (priorityValue < 5 && urgentValue) {
                    warnings.add("Parameter 'urgent' is true but 'priority' is low - consider increasing priority");
                }
            }
        }
    }

    /**
     * Validate schema structure.
     * 
     * @param schema the schema to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateSchemaStructure(Map<String, Object> schema, List<String> errors, List<String> warnings) {
        // Implement schema structure validation

        // Check for required schema fields
        if (!schema.containsKey("type")) {
            errors.add("Schema is missing required 'type' field");
        }

        if (!schema.containsKey("properties")) {
            errors.add("Schema is missing required 'properties' field");
        }

        // Validate schema format
        Object type = schema.get("type");
        if (type != null && !(type instanceof String)) {
            errors.add("Schema 'type' field must be a string");
        }

        Object properties = schema.get("properties");
        if (properties != null && !(properties instanceof Map)) {
            errors.add("Schema 'properties' field must be an object");
        }

        // Check schema version compatibility
        Object version = schema.get("$schema");
        if (version != null && version instanceof String) {
            String versionStr = (String) version;
            if (!versionStr.contains("json-schema.org")) {
                warnings.add("Schema version may not be compatible with JSON Schema standard");
            }
        }
    }

    /**
     * Validate schema types.
     * 
     * @param schema the schema to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateSchemaTypes(Map<String, Object> schema, List<String> errors, List<String> warnings) {
        // Implement schema type validation

        // Validate type definitions
        Object type = schema.get("type");
        if (type instanceof String) {
            String typeStr = (String) type;
            List<String> validTypes = List.of("object", "array", "string", "number", "integer", "boolean", "null");
            if (!validTypes.contains(typeStr)) {
                errors.add("Schema type '" + typeStr + "' is not a valid JSON Schema type");
            }
        }

        // Check type compatibility
        Object properties = schema.get("properties");
        if (properties instanceof Map && type instanceof String) {
            String typeStr = (String) type;
            if (!"object".equals(typeStr)) {
                warnings.add("Schema has 'properties' but type is not 'object'");
            }
        }

        Object items = schema.get("items");
        if (items != null && type instanceof String) {
            String typeStr = (String) type;
            if (!"array".equals(typeStr)) {
                warnings.add("Schema has 'items' but type is not 'array'");
            }
        }

        // Validate type constraints
        Object minLength = schema.get("minLength");
        if (minLength != null && type instanceof String) {
            String typeStr = (String) type;
            if (!"string".equals(typeStr)) {
                errors.add("Schema has 'minLength' constraint but type is not 'string'");
            }
        }

        Object minimum = schema.get("minimum");
        if (minimum != null && type instanceof String) {
            String typeStr = (String) type;
            if (!"number".equals(typeStr) && !"integer".equals(typeStr)) {
                errors.add("Schema has 'minimum' constraint but type is not numeric");
            }
        }
    }

    /**
     * Validate schema constraints.
     * 
     * @param schema the schema to validate
     * @param errors list to collect errors
     * @param warnings list to collect warnings
     */
    private void validateSchemaConstraints(Map<String, Object> schema, List<String> errors, List<String> warnings) {
        // Implement schema constraint validation

        // Check constraint definitions
        Object minLength = schema.get("minLength");
        Object maxLength = schema.get("maxLength");
        if (minLength instanceof Number && maxLength instanceof Number) {
            int min = ((Number) minLength).intValue();
            int max = ((Number) maxLength).intValue();
            if (min > max) {
                errors.add("Schema constraint 'minLength' (" + min + ") is greater than 'maxLength' (" + max + ")");
            }
        }

        Object minimum = schema.get("minimum");
        Object maximum = schema.get("maximum");
        if (minimum instanceof Number && maximum instanceof Number) {
            double min = ((Number) minimum).doubleValue();
            double max = ((Number) maximum).doubleValue();
            if (min > max) {
                errors.add("Schema constraint 'minimum' (" + min + ") is greater than 'maximum' (" + max + ")");
            }
        }

        // Validate constraint logic
        Object minItems = schema.get("minItems");
        Object maxItems = schema.get("maxItems");
        if (minItems instanceof Number && maxItems instanceof Number) {
            int min = ((Number) minItems).intValue();
            int max = ((Number) maxItems).intValue();
            if (min > max) {
                errors.add("Schema constraint 'minItems' (" + min + ") is greater than 'maxItems' (" + max + ")");
            }
        }

        // Check constraint consistency
        Object pattern = schema.get("pattern");
        Object format = schema.get("format");
        if (pattern != null && format != null) {
            warnings.add("Schema has both 'pattern' and 'format' constraints - consider using only one");
        }

        Object enumValues = schema.get("enum");
        Object constValue = schema.get("const");
        if (enumValues != null && constValue != null) {
            warnings.add("Schema has both 'enum' and 'const' constraints - 'const' will override 'enum'");
        }
    }

    /**
     * Validation listener interface.
     */
    public interface ValidationListener {
        void onValidationEvent(ValidationEvent event);
    }

    /**
     * Validation event.
     */
    // ValidationEvent extracted to org.openhab.core.ai.tool.api.validation.ValidationEvent

    /**
     * Load tool configuration for validation.
     * 
     * @param toolId the tool ID
     * @return tool configuration or null if not found
     */
    private Map<String, Object> loadToolConfiguration(String toolId) {
        try {
            // In a real implementation, this would load from a configuration store
            // For now, return a basic configuration
            Map<String, Object> config = new HashMap<>();
            config.put("name", toolId);
            config.put("description", "Tool " + toolId);
            config.put("version", "1.0.0");
            config.put("enabled", true);
            config.put("timeout", 30);
            config.put("maxRetries", 3);
            config.put("dependencies", List.of());
            config.put("permissions", Map.of("read", true, "write", false));

            return config;

        } catch (Exception e) {
            System.err.println("Error loading tool configuration for " + toolId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate tool permissions.
     * 
     * @param permissions the permissions to validate
     * @return true if permissions are valid
     */
    private boolean validateToolPermissions(Map<String, Object> permissions) {
        try {
            // Check for required permissions
            if (!permissions.containsKey("read")) {
                return false;
            }

            // Validate permission types
            for (Map.Entry<String, Object> entry : permissions.entrySet()) {
                if (!(entry.getValue() instanceof Boolean)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error validating tool permissions: " + e.getMessage());
            return false;
        }
    }
}
