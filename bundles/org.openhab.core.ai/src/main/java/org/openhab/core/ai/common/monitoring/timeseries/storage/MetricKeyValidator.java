package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for MetricKey instances with normalization and validation capabilities.
 * 
 * <p>
 * This class provides comprehensive validation and normalization of MetricKey instances,
 * ensuring they meet the requirements for efficient storage and querying operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricKeyValidator {

    private static final Logger logger = LoggerFactory.getLogger(MetricKeyValidator.class);

    // Validation constants
    private static final int MAX_KIND_LENGTH = 100;
    private static final int MAX_LABEL_KEY_LENGTH = 50;
    private static final int MAX_LABEL_VALUE_LENGTH = 200;
    private static final int MAX_CAPABILITY_LENGTH = 50;
    private static final int MAX_LABELS_COUNT = 20;
    private static final int MAX_CAPABILITIES_COUNT = 10;

    // Validation patterns
    private static final Pattern VALID_KIND_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern VALID_LABEL_KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern VALID_CAPABILITY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");

    // Reserved keywords
    private static final Set<String> RESERVED_KINDS = Set.of(
        "system", "internal", "reserved", "temp", "temporary", "test", "debug"
    );

    private static final Set<String> RESERVED_LABEL_KEYS = Set.of(
        "id", "uuid", "timestamp", "created", "modified", "version", "type", "class"
    );

    private static final Set<String> RESERVED_CAPABILITIES = Set.of(
        "system", "internal", "reserved", "temp", "temporary"
    );

    private MetricKeyValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate a MetricKey instance.
     * 
     * @param key the metric key to validate
     * @return validation result with detailed information
     */
    public static MetricKeyValidationResult validate(MetricKey key) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Validate kind
            validateKind(key.kind(), errors, warnings);
            
            // Validate labels
            validateLabels(key.labels(), errors, warnings);
            
            // Validate capabilities
            validateCapabilities(key.capabilities(), errors, warnings);
            
            // Validate overall structure
            validateOverallStructure(key, errors, warnings);
            
        } catch (Exception e) {
            errors.add("Validation failed with exception: " + e.getMessage());
            logger.warn("MetricKey validation failed for key: {}", key.id(), e);
        }
        
        return new MetricKeyValidationResult(errors.isEmpty(), errors, warnings, key);
    }

    /**
     * Validate and normalize a MetricKey instance.
     * 
     * @param key the metric key to validate and normalize
     * @return validation result with normalized key if valid
     */
    public static MetricKeyValidationResult validateAndNormalize(MetricKey key) {
        MetricKeyValidationResult validationResult = validate(key);
        
        if (validationResult.isValid()) {
            try {
                MetricKey normalizedKey = normalizeMetricKey(key);
                return new MetricKeyValidationResult(true, validationResult.errors(), 
                    validationResult.warnings(), normalizedKey);
            } catch (Exception e) {
                List<String> errors = new ArrayList<>(validationResult.errors());
                errors.add("Normalization failed: " + e.getMessage());
                return new MetricKeyValidationResult(false, errors, validationResult.warnings(), key);
            }
        }
        
        return validationResult;
    }

    /**
     * Validate a MetricKey kind.
     * 
     * @param kind the kind to validate
     * @param errors list to add errors to
     * @param warnings list to add warnings to
     */
    private static void validateKind(@Nullable String kind, List<String> errors, List<String> warnings) {
        if (kind == null || kind.trim().isEmpty()) {
            errors.add("MetricKey kind cannot be null or empty");
            return;
        }
        
        String trimmedKind = kind.trim();
        
        if (trimmedKind.length() > MAX_KIND_LENGTH) {
            errors.add("MetricKey kind too long: " + trimmedKind.length() + " characters (max: " + MAX_KIND_LENGTH + ")");
        }
        
        if (!VALID_KIND_PATTERN.matcher(trimmedKind).matches()) {
            errors.add("MetricKey kind contains invalid characters: " + trimmedKind);
        }
        
        if (RESERVED_KINDS.contains(trimmedKind.toLowerCase())) {
            warnings.add("MetricKey kind uses reserved keyword: " + trimmedKind);
        }
        
        if (trimmedKind.startsWith("_") || trimmedKind.endsWith("_")) {
            warnings.add("MetricKey kind starts or ends with underscore: " + trimmedKind);
        }
    }

    /**
     * Validate MetricKey labels.
     * 
     * @param labels the labels to validate
     * @param errors list to add errors to
     * @param warnings list to add warnings to
     */
    private static void validateLabels(@Nullable Map<String, String> labels, List<String> errors, List<String> warnings) {
        if (labels == null) {
            errors.add("MetricKey labels cannot be null");
            return;
        }
        
        if (labels.isEmpty()) {
            warnings.add("MetricKey has no labels");
            return;
        }
        
        if (labels.size() > MAX_LABELS_COUNT) {
            warnings.add("MetricKey has many labels: " + labels.size() + " (max recommended: " + MAX_LABELS_COUNT + ")");
        }
        
        Set<String> seenKeys = new HashSet<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String labelKey = entry.getKey();
            String labelValue = entry.getValue();
            
            // Validate label key
            if (labelKey == null || labelKey.trim().isEmpty()) {
                errors.add("Label key cannot be null or empty");
                continue;
            }
            
            String trimmedKey = labelKey.trim();
            if (trimmedKey.length() > MAX_LABEL_KEY_LENGTH) {
                errors.add("Label key too long: " + trimmedKey + " (" + trimmedKey.length() + " characters, max: " + MAX_LABEL_KEY_LENGTH + ")");
            }
            
            if (!VALID_LABEL_KEY_PATTERN.matcher(trimmedKey).matches()) {
                errors.add("Label key contains invalid characters: " + trimmedKey);
            }
            
            if (RESERVED_LABEL_KEYS.contains(trimmedKey.toLowerCase())) {
                warnings.add("Label key uses reserved keyword: " + trimmedKey);
            }
            
            if (seenKeys.contains(trimmedKey.toLowerCase())) {
                errors.add("Duplicate label key (case-insensitive): " + trimmedKey);
            }
            seenKeys.add(trimmedKey.toLowerCase());
            
            // Validate label value
            if (labelValue == null) {
                errors.add("Label value cannot be null for key: " + trimmedKey);
                continue;
            }
            
            String trimmedValue = labelValue.trim();
            if (trimmedValue.length() > MAX_LABEL_VALUE_LENGTH) {
                warnings.add("Label value too long for key " + trimmedKey + ": " + trimmedValue.length() + " characters (max: " + MAX_LABEL_VALUE_LENGTH + ")");
            }
            
            if (trimmedValue.isEmpty()) {
                warnings.add("Label value is empty for key: " + trimmedKey);
            }
            
            // Check for high-cardinality values
            if (isHighCardinalityValue(trimmedKey, trimmedValue)) {
                warnings.add("Label value appears to be high-cardinality for key: " + trimmedKey);
            }
        }
    }

    /**
     * Validate MetricKey capabilities.
     * 
     * @param capabilities the capabilities to validate
     * @param errors list to add errors to
     * @param warnings list to add warnings to
     */
    private static void validateCapabilities(@Nullable Set<String> capabilities, List<String> errors, List<String> warnings) {
        if (capabilities == null) {
            errors.add("MetricKey capabilities cannot be null");
            return;
        }
        
        if (capabilities.isEmpty()) {
            warnings.add("MetricKey has no capabilities");
            return;
        }
        
        if (capabilities.size() > MAX_CAPABILITIES_COUNT) {
            warnings.add("MetricKey has many capabilities: " + capabilities.size() + " (max recommended: " + MAX_CAPABILITIES_COUNT + ")");
        }
        
        Set<String> seenCapabilities = new HashSet<>();
        for (String capability : capabilities) {
            if (capability == null || capability.trim().isEmpty()) {
                errors.add("Capability cannot be null or empty");
                continue;
            }
            
            String trimmedCapability = capability.trim();
            if (trimmedCapability.length() > MAX_CAPABILITY_LENGTH) {
                errors.add("Capability too long: " + trimmedCapability + " (" + trimmedCapability.length() + " characters, max: " + MAX_CAPABILITY_LENGTH + ")");
            }
            
            if (!VALID_CAPABILITY_PATTERN.matcher(trimmedCapability).matches()) {
                errors.add("Capability contains invalid characters: " + trimmedCapability);
            }
            
            if (RESERVED_CAPABILITIES.contains(trimmedCapability.toLowerCase())) {
                warnings.add("Capability uses reserved keyword: " + trimmedCapability);
            }
            
            if (seenCapabilities.contains(trimmedCapability.toLowerCase())) {
                errors.add("Duplicate capability (case-insensitive): " + trimmedCapability);
            }
            seenCapabilities.add(trimmedCapability.toLowerCase());
        }
    }

    /**
     * Validate overall MetricKey structure.
     * 
     * @param key the metric key to validate
     * @param errors list to add errors to
     * @param warnings list to add warnings to
     */
    private static void validateOverallStructure(MetricKey key, List<String> errors, List<String> warnings) {
        try {
            // Validate ID generation
            String id = key.id();
            if (id == null || id.isEmpty()) {
                errors.add("MetricKey ID cannot be null or empty");
            } else if (id.length() > 500) {
                warnings.add("MetricKey ID is very long: " + id.length() + " characters");
            }
            
            // Check for potential conflicts
            if (key.labels() != null && key.capabilities() != null) {
                Set<String> labelKeys = key.labels().keySet();
                Set<String> capabilities = key.capabilities();
                
                // Check for overlap between label keys and capabilities
                Set<String> intersection = new HashSet<>(labelKeys);
                intersection.retainAll(capabilities);
                if (!intersection.isEmpty()) {
                    warnings.add("Label keys and capabilities overlap: " + intersection);
                }
            }
            
            // Validate capability consistency
            validateCapabilityConsistency(key, warnings);
            
        } catch (Exception e) {
            errors.add("Overall structure validation failed: " + e.getMessage());
        }
    }

    /**
     * Validate capability consistency with kind and labels.
     * 
     * @param key the metric key
     * @param warnings list to add warnings to
     */
    private static void validateCapabilityConsistency(MetricKey key, List<String> warnings) {
        String kind = key.kind();
        Set<String> capabilities = key.capabilities();
        
        // Check for kind-capability consistency
        if (kind != null && capabilities != null) {
            if (kind.contains("health") && !capabilities.contains("health")) {
                warnings.add("Kind suggests health metrics but 'health' capability is missing");
            }
            
            if (kind.contains("cache") && !capabilities.contains("cache-stats")) {
                warnings.add("Kind suggests cache metrics but 'cache-stats' capability is missing");
            }
            
            if (kind.contains("security") && !capabilities.contains("security")) {
                warnings.add("Kind suggests security metrics but 'security' capability is missing");
            }
            
            if (kind.contains("model") && !capabilities.contains("model")) {
                warnings.add("Kind suggests model metrics but 'model' capability is missing");
            }
            
            if (kind.contains("tool") && !capabilities.contains("tool")) {
                warnings.add("Kind suggests tool metrics but 'tool' capability is missing");
            }
            
            if (kind.contains("agent") && !capabilities.contains("agent")) {
                warnings.add("Kind suggests agent metrics but 'agent' capability is missing");
            }
        }
    }

    /**
     * Normalize a MetricKey by trimming and standardizing values.
     * 
     * @param key the metric key to normalize
     * @return normalized metric key
     */
    private static MetricKey normalizeMetricKey(MetricKey key) {
        // Normalize kind
        String normalizedKind = key.kind() != null ? key.kind().trim() : "";
        
        // Normalize labels
        Map<String, String> normalizedLabels = new HashMap<>();
        if (key.labels() != null) {
            for (Map.Entry<String, String> entry : key.labels().entrySet()) {
                String normalizedKey = entry.getKey() != null ? entry.getKey().trim() : "";
                String normalizedValue = entry.getValue() != null ? entry.getValue().trim() : "";
                if (!normalizedKey.isEmpty()) {
                    normalizedLabels.put(normalizedKey, normalizedValue);
                }
            }
        }
        
        // Normalize capabilities
        Set<String> normalizedCapabilities = new HashSet<>();
        if (key.capabilities() != null) {
            for (String capability : key.capabilities()) {
                String normalizedCapability = capability != null ? capability.trim() : "";
                if (!normalizedCapability.isEmpty()) {
                    normalizedCapabilities.add(normalizedCapability);
                }
            }
        }
        
        return new MetricKeys.SimpleMetricKey(normalizedKind, normalizedLabels, normalizedCapabilities);
    }

    /**
     * Check if a value appears to be high-cardinality.
     * 
     * @param key the label key
     * @param value the label value
     * @return true if high-cardinality
     */
    private static boolean isHighCardinalityValue(String key, String value) {
        // High-cardinality patterns
        if (value.matches(".*\\d{10,}.*")) { // Contains long numbers (timestamps, IDs)
            return true;
        }
        
        if (value.matches(".*[a-f0-9]{8,}.*")) { // Contains hex strings (UUIDs, hashes)
            return true;
        }
        
        if (value.length() > 50) { // Very long values
            return true;
        }
        
        // High-cardinality keys
        String[] highCardinalityKeys = {"id", "uuid", "timestamp", "requestId", "sessionId", "userId"};
        for (String hcKey : highCardinalityKeys) {
            if (key.toLowerCase().contains(hcKey)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Validate a batch of MetricKey instances.
     * 
     * @param keys the metric keys to validate
     * @return batch validation result
     */
    public static MetricKeyBatchValidationResult validateBatch(List<MetricKey> keys) {
        List<MetricKeyValidationResult> results = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;
        
        for (MetricKey key : keys) {
            MetricKeyValidationResult result = validate(key);
            results.add(result);
            
            if (result.isValid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }
        
        return new MetricKeyBatchValidationResult(results, validCount, invalidCount);
    }

    /**
     * Validate and normalize a batch of MetricKey instances.
     * 
     * @param keys the metric keys to validate and normalize
     * @return batch validation result with normalized keys
     */
    public static MetricKeyBatchValidationResult validateAndNormalizeBatch(List<MetricKey> keys) {
        List<MetricKeyValidationResult> results = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;
        
        for (MetricKey key : keys) {
            MetricKeyValidationResult result = validateAndNormalize(key);
            results.add(result);
            
            if (result.isValid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }
        
        return new MetricKeyBatchValidationResult(results, validCount, invalidCount);
    }

    /**
     * Result of MetricKey validation.
     */
    public record MetricKeyValidationResult(
        boolean isValid,
        List<String> errors,
        List<String> warnings,
        MetricKey normalizedKey
    ) {}

    /**
     * Result of batch MetricKey validation.
     */
    public record MetricKeyBatchValidationResult(
        List<MetricKeyValidationResult> results,
        int validCount,
        int invalidCount
    ) {}
}
