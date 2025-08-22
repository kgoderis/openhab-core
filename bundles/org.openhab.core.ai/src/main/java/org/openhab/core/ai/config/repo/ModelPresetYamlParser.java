package org.openhab.core.ai.config.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YAML parser for model presets with provider mapping.
 * 
 * <p>
 * This parser handles loading and parsing of model preset YAML files,
 * including provider-specific configurations and parameter validation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ModelPresetYamlParser {

    private final Logger logger = LoggerFactory.getLogger(ModelPresetYamlParser.class);

    /**
     * Parses a model preset YAML file.
     * 
     * @param filePath the path to the YAML file
     * @return the parsed model presets
     * @throws ModelPresetRepositoryException if parsing fails
     */
    public Map<String, ModelPresetRepository.ModelPreset> parseModelPresets(Path filePath)
            throws ModelPresetRepositoryException {
        try {
            String content = Files.readString(filePath);
            return parseModelPresets(content);
        } catch (IOException e) {
            throw new ModelPresetRepositoryException("Failed to read model preset file: " + filePath, e);
        }
    }

    /**
     * Parses model preset content.
     * 
     * @param content the YAML content
     * @return map of preset names to their configurations
     * @throws ModelPresetRepositoryException if parsing fails
     */
    public Map<String, ModelPresetRepository.ModelPreset> parseModelPresets(String content)
            throws ModelPresetRepositoryException {
        try {
            // Parse YAML content into map
            Map<String, Object> yamlData = parseYamlContent(content);

            // Validate the structure
            List<String> validationErrors = validateModelPresets(yamlData);
            if (!validationErrors.isEmpty()) {
                throw new ModelPresetRepositoryException("Model presets validation failed: " + validationErrors);
            }

            // Create model presets
            return createModelPresets(yamlData);

        } catch (Exception e) {
            throw new ModelPresetRepositoryException("Failed to parse model presets: " + e.getMessage(), e);
        }
    }

    /**
     * Parses YAML content into a map.
     * 
     * @param content the YAML content
     * @return the parsed map
     * @throws Exception if parsing fails
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYamlContent(String content) throws Exception {
        // TODO: Implement actual YAML parsing using a library like SnakeYAML
        // For now, return a simple map structure
        Map<String, Object> result = new HashMap<>();

        // This is a placeholder implementation
        // In a real implementation, you would use:
        // Yaml yaml = new Yaml();
        // return yaml.load(content);

        logger.debug("Parsing YAML content (placeholder implementation)");
        return result;
    }

    /**
     * Validates model presets structure.
     * 
     * @param data the parsed YAML data
     * @return list of validation errors, empty if valid
     */
    private List<String> validateModelPresets(Map<String, Object> data) {
        List<String> errors = new ArrayList<>();

        // Validate that we have a models section
        if (!data.containsKey("models")) {
            errors.add("Missing required 'models' section");
            return errors;
        }

        // Validate models section is a map
        YamlValidationUtils.validateMapField(data, "models", errors);

        return errors;
    }

    /**
     * Creates model presets from parsed data.
     * 
     * @param data the parsed YAML data
     * @return map of preset names to their configurations
     */
    @SuppressWarnings("unchecked")
    private Map<String, ModelPresetRepository.ModelPreset> createModelPresets(Map<String, Object> data) {
        Map<String, ModelPresetRepository.ModelPreset> presets = new HashMap<>();
        Map<String, Object> modelsData = (Map<String, Object>) data.get("models");

        for (Map.Entry<String, Object> providerEntry : modelsData.entrySet()) {
            String provider = providerEntry.getKey();
            Map<String, Object> providerData = (Map<String, Object>) providerEntry.getValue();

            if (providerData.containsKey("presets")) {
                Map<String, Object> presetsData = (Map<String, Object>) providerData.get("presets");

                for (Map.Entry<String, Object> presetEntry : presetsData.entrySet()) {
                    String presetName = presetEntry.getKey();
                    Map<String, Object> presetData = (Map<String, Object>) presetEntry.getValue();

                    presets.put(presetName, createModelPreset(presetData, provider, presetName));
                }
            }
        }

        return presets;
    }

    /**
     * Creates a model preset from parsed data.
     * 
     * @param presetData the parsed preset data
     * @param provider the provider name
     * @param presetName the preset name
     * @return the model preset
     */
    private ModelPresetRepository.ModelPreset createModelPreset(Map<String, Object> presetData, String provider,
            String presetName) {
        return new ModelPresetRepository.ModelPreset() {
            @Override
            public String getName() {
                return presetName;
            }

            @Override
            public @Nullable String getDescription() {
                return null; // Not present in the example structure
            }

            @Override
            public String getVersion() {
                return "1.0.0"; // Default version
            }

            @Override
            public String getProvider() {
                return provider;
            }

            @Override
            public String getModel() {
                return (String) presetData.get("model");
            }

            @Override
            public double getTemperature() {
                Object value = presetData.get("temperature");
                return value instanceof Number ? ((Number) value).doubleValue() : 0.3;
            }

            @Override
            public int getMaxTokens() {
                Object value = presetData.get("max_tokens");
                return value instanceof Number ? ((Number) value).intValue() : 1000;
            }

            @Override
            public @Nullable Double getTopP() {
                Object value = presetData.get("top_p");
                return value instanceof Number ? ((Number) value).doubleValue() : null;
            }

            @Override
            public @Nullable Double getFrequencyPenalty() {
                Object value = presetData.get("frequency_penalty");
                return value instanceof Number ? ((Number) value).doubleValue() : null;
            }

            @Override
            public @Nullable Double getPresencePenalty() {
                Object value = presetData.get("presence_penalty");
                return value instanceof Number ? ((Number) value).doubleValue() : null;
            }

            @Override
            public @Nullable String getSystemPrompt() {
                return (String) presetData.get("system_prompt");
            }

            @Override
            public Map<String, Object> getMetadata() {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("provider", provider);
                metadata.put("preset_name", presetName);
                return metadata;
            }

            @Override
            public Map<String, Object> getProviderParameters() {
                // Return all preset data as provider parameters
                return new HashMap<>(presetData);
            }
        };
    }

    /**
     * Serializes model presets to YAML.
     * 
     * @param presets the model presets to serialize
     * @return the YAML content
     * @throws ModelPresetRepositoryException if serialization fails
     */
    public String serializeModelPresets(Map<String, ModelPresetRepository.ModelPreset> presets)
            throws ModelPresetRepositoryException {
        try {
            // TODO: Implement actual YAML serialization using a library like SnakeYAML
            // For now, return a simple YAML structure
            StringBuilder yaml = new StringBuilder();
            yaml.append("models:\n");

            // Group presets by provider
            Map<String, List<ModelPresetRepository.ModelPreset>> providerGroups = new HashMap<>();
            for (ModelPresetRepository.ModelPreset preset : presets.values()) {
                String provider = preset.getProvider();
                providerGroups.computeIfAbsent(provider, k -> new ArrayList<>()).add(preset);
            }

            for (Map.Entry<String, List<ModelPresetRepository.ModelPreset>> entry : providerGroups.entrySet()) {
                String provider = entry.getKey();
                List<ModelPresetRepository.ModelPreset> providerPresets = entry.getValue();

                yaml.append("  ").append(provider).append(":\n");
                yaml.append("    version: \"1.0.0\"\n");
                yaml.append("    description: \"").append(provider).append(" model configurations\"\n");
                yaml.append("    presets:\n");

                for (ModelPresetRepository.ModelPreset preset : providerPresets) {
                    yaml.append("      ").append(preset.getName()).append(":\n");
                    yaml.append("        model: \"").append(preset.getModel()).append("\"\n");
                    yaml.append("        temperature: ").append(preset.getTemperature()).append("\n");
                    yaml.append("        max_tokens: ").append(preset.getMaxTokens()).append("\n");

                    if (preset.getTopP() != null) {
                        yaml.append("        top_p: ").append(preset.getTopP()).append("\n");
                    }
                    if (preset.getFrequencyPenalty() != null) {
                        yaml.append("        frequency_penalty: ").append(preset.getFrequencyPenalty()).append("\n");
                    }
                    if (preset.getPresencePenalty() != null) {
                        yaml.append("        presence_penalty: ").append(preset.getPresencePenalty()).append("\n");
                    }
                    if (preset.getSystemPrompt() != null) {
                        yaml.append("        system_prompt: \"").append(preset.getSystemPrompt()).append("\"\n");
                    }
                }
            }

            return yaml.toString();

        } catch (Exception e) {
            throw new ModelPresetRepositoryException("Failed to serialize model presets: " + e.getMessage(), e);
        }
    }

    /**
     * Validates model preset parameters.
     * 
     * @param preset the model preset to validate
     * @return list of validation errors, empty if valid
     */
    public List<String> validateModelPreset(ModelPresetRepository.ModelPreset preset) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (preset.getModel() == null || preset.getModel().trim().isEmpty()) {
            errors.add("Model preset must have a model name");
        }

        if (preset.getProvider() == null || preset.getProvider().trim().isEmpty()) {
            errors.add("Model preset must have a provider");
        }

        // Validate numeric ranges
        if (preset.getTemperature() < 0.0 || preset.getTemperature() > 2.0) {
            errors.add("Temperature must be between 0.0 and 2.0, got: " + preset.getTemperature());
        }

        if (preset.getMaxTokens() < 1 || preset.getMaxTokens() > 8192) {
            errors.add("Max tokens must be between 1 and 8192, got: " + preset.getMaxTokens());
        }

        if (preset.getTopP() != null && (preset.getTopP() < 0.0 || preset.getTopP() > 1.0)) {
            errors.add("Top-p must be between 0.0 and 1.0, got: " + preset.getTopP());
        }

        if (preset.getFrequencyPenalty() != null
                && (preset.getFrequencyPenalty() < -2.0 || preset.getFrequencyPenalty() > 2.0)) {
            errors.add("Frequency penalty must be between -2.0 and 2.0, got: " + preset.getFrequencyPenalty());
        }

        if (preset.getPresencePenalty() != null
                && (preset.getPresencePenalty() < -2.0 || preset.getPresencePenalty() > 2.0)) {
            errors.add("Presence penalty must be between -2.0 and 2.0, got: " + preset.getPresencePenalty());
        }

        return errors;
    }
}
