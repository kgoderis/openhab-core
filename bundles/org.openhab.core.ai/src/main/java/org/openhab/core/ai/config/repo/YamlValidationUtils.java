package org.openhab.core.ai.config.repo;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for YAML validation.
 * 
 * <p>
 * This class provides common validation methods for YAML content including:
 * - Structure validation
 * - Required field validation
 * - Type validation
 * - Range validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class YamlValidationUtils {

    private static final Logger logger = LoggerFactory.getLogger(YamlValidationUtils.class);

    private YamlValidationUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Validates that a required field is present and not null.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the required field
     * @param errors the list to add validation errors to
     */
    public static void validateRequiredField(Map<String, Object> data, String fieldName, List<String> errors) {
        if (!data.containsKey(fieldName) || data.get(fieldName) == null) {
            errors.add("Required field '" + fieldName + "' is missing or null");
        }
    }

    /**
     * Validates that a field is a string and not empty.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param errors the list to add validation errors to
     */
    public static void validateStringField(Map<String, Object> data, String fieldName, List<String> errors) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof String)) {
            errors.add("Field '" + fieldName + "' must be a string, got: " + value.getClass().getSimpleName());
        } else if (value instanceof String && ((String) value).trim().isEmpty()) {
            errors.add("Field '" + fieldName + "' cannot be empty");
        }
    }

    /**
     * Validates that a field is a number within a specified range.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param errors the list to add validation errors to
     */
    public static void validateNumberRange(Map<String, Object> data, String fieldName, double min, double max,
            List<String> errors) {
        Object value = data.get(fieldName);
        if (value != null) {
            if (!(value instanceof Number)) {
                errors.add("Field '" + fieldName + "' must be a number, got: " + value.getClass().getSimpleName());
            } else {
                double numValue = ((Number) value).doubleValue();
                if (numValue < min || numValue > max) {
                    errors.add(
                            "Field '" + fieldName + "' must be between " + min + " and " + max + ", got: " + numValue);
                }
            }
        }
    }

    /**
     * Validates that a field is a boolean.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param errors the list to add validation errors to
     */
    public static void validateBooleanField(Map<String, Object> data, String fieldName, List<String> errors) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof Boolean)) {
            errors.add("Field '" + fieldName + "' must be a boolean, got: " + value.getClass().getSimpleName());
        }
    }

    /**
     * Validates that a field is a list.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param errors the list to add validation errors to
     */
    public static void validateListField(Map<String, Object> data, String fieldName, List<String> errors) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof List)) {
            errors.add("Field '" + fieldName + "' must be a list, got: " + value.getClass().getSimpleName());
        }
    }

    /**
     * Validates that a field is a map.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param errors the list to add validation errors to
     */
    @SuppressWarnings("unchecked")
    public static void validateMapField(Map<String, Object> data, String fieldName, List<String> errors) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof Map)) {
            errors.add("Field '" + fieldName + "' must be a map, got: " + value.getClass().getSimpleName());
        }
    }

    /**
     * Validates that a field matches a specific pattern (regex).
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param pattern the regex pattern to match
     * @param errors the list to add validation errors to
     */
    public static void validatePattern(Map<String, Object> data, String fieldName, String pattern,
            List<String> errors) {
        Object value = data.get(fieldName);
        if (value instanceof String) {
            String strValue = (String) value;
            if (!strValue.matches(pattern)) {
                errors.add("Field '" + fieldName + "' does not match pattern '" + pattern + "', got: " + strValue);
            }
        }
    }

    /**
     * Validates that a field is one of the allowed values.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param allowedValues the list of allowed values
     * @param errors the list to add validation errors to
     */
    public static void validateEnum(Map<String, Object> data, String fieldName, List<String> allowedValues,
            List<String> errors) {
        Object value = data.get(fieldName);
        if (value instanceof String) {
            String strValue = (String) value;
            if (!allowedValues.contains(strValue)) {
                errors.add("Field '" + fieldName + "' must be one of " + allowedValues + ", got: " + strValue);
            }
        }
    }

    /**
     * Validates the structure of a nested map.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param requiredFields the list of required fields in the nested map
     * @param errors the list to add validation errors to
     */
    @SuppressWarnings("unchecked")
    public static void validateNestedMap(Map<String, Object> data, String fieldName, List<String> requiredFields,
            List<String> errors) {
        Object value = data.get(fieldName);
        if (value instanceof Map) {
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            for (String requiredField : requiredFields) {
                validateRequiredField(nestedMap, requiredField, errors);
            }
        } else if (value != null) {
            errors.add("Field '" + fieldName + "' must be a map, got: " + value.getClass().getSimpleName());
        }
    }

    /**
     * Validates the structure of a list of maps.
     * 
     * @param data the data map to validate
     * @param fieldName the name of the field to validate
     * @param requiredFields the list of required fields in each map
     * @param errors the list to add validation errors to
     */
    @SuppressWarnings("unchecked")
    public static void validateListOfMaps(Map<String, Object> data, String fieldName, List<String> requiredFields,
            List<String> errors) {
        Object value = data.get(fieldName);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Map) {
                    Map<String, Object> mapItem = (Map<String, Object>) item;
                    for (String requiredField : requiredFields) {
                        validateRequiredField(mapItem, requiredField, errors);
                    }
                } else {
                    errors.add("Field '" + fieldName + "'[" + i + "] must be a map, got: "
                            + item.getClass().getSimpleName());
                }
            }
        } else if (value != null) {
            errors.add("Field '" + fieldName + "' must be a list, got: " + value.getClass().getSimpleName());
        }
    }

    /**
     * Logs validation errors.
     * 
     * @param errors the list of validation errors
     * @param context the context for the validation (e.g., file name)
     */
    public static void logValidationErrors(List<String> errors, String context) {
        if (!errors.isEmpty()) {
            logger.warn("YAML validation errors in {}: {}", context, errors);
        }
    }

    /**
     * Creates a validation error message.
     * 
     * @param fieldName the name of the field with the error
     * @param message the error message
     * @return the formatted error message
     */
    public static String createValidationError(String fieldName, String message) {
        return "Field '" + fieldName + "': " + message;
    }
}
