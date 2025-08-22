package org.openhab.core.ai.config.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Repository for managing YAML model preset configurations.
 * 
 * <p>
 * This interface provides methods for loading, storing, and managing model preset
 * configurations from YAML files in the /conf/ai/models/ directory. It supports
 * different model providers and their specific configuration parameters.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ModelPresetRepository {

    /**
     * Loads all model presets from the configured directory.
     * 
     * @return map of preset names to their configurations
     * @throws ModelPresetRepositoryException if loading fails
     */
    Map<String, ModelPreset> loadAllPresets() throws ModelPresetRepositoryException;

    /**
     * Loads a specific model preset by name.
     * 
     * @param presetName the name of the preset to load
     * @return the model preset if found
     * @throws ModelPresetRepositoryException if loading fails
     */
    Optional<ModelPreset> loadPreset(String presetName) throws ModelPresetRepositoryException;

    /**
     * Loads model presets for a specific provider.
     * 
     * @param provider the name of the provider
     * @return map of preset names to their configurations for the provider
     * @throws ModelPresetRepositoryException if loading fails
     */
    Map<String, ModelPreset> loadProviderPresets(String provider) throws ModelPresetRepositoryException;

    /**
     * Saves a model preset to the repository.
     * 
     * @param presetName the name of the preset
     * @param preset the preset configuration
     * @throws ModelPresetRepositoryException if saving fails
     */
    void savePreset(String presetName, ModelPreset preset) throws ModelPresetRepositoryException;

    /**
     * Deletes a model preset from the repository.
     * 
     * @param presetName the name of the preset to delete
     * @throws ModelPresetRepositoryException if deletion fails
     */
    void deletePreset(String presetName) throws ModelPresetRepositoryException;

    /**
     * Validates a model preset structure.
     * 
     * @param preset the preset to validate
     * @return list of validation errors, empty if valid
     */
    List<String> validatePreset(ModelPreset preset);

    /**
     * Gets the list of available preset names.
     * 
     * @return list of preset names
     * @throws ModelPresetRepositoryException if listing fails
     */
    List<String> getPresetNames() throws ModelPresetRepositoryException;

    /**
     * Checks if a preset exists.
     * 
     * @param presetName the name of the preset
     * @return true if the preset exists
     */
    boolean presetExists(String presetName);

    /**
     * Reloads all presets from disk.
     * 
     * @throws ModelPresetRepositoryException if reloading fails
     */
    void reload() throws ModelPresetRepositoryException;

    /**
     * Represents a model preset configuration.
     */
    interface ModelPreset {
        /**
         * Gets the preset name.
         * 
         * @return the preset name
         */
        String getName();

        /**
         * Gets the preset description.
         * 
         * @return the preset description
         */
        @Nullable
        String getDescription();

        /**
         * Gets the preset version.
         * 
         * @return the preset version
         */
        String getVersion();

        /**
         * Gets the model provider.
         * 
         * @return the model provider
         */
        String getProvider();

        /**
         * Gets the model name.
         * 
         * @return the model name
         */
        String getModel();

        /**
         * Gets the temperature setting.
         * 
         * @return the temperature value
         */
        double getTemperature();

        /**
         * Gets the maximum tokens setting.
         * 
         * @return the maximum tokens value
         */
        int getMaxTokens();

        /**
         * Gets the top-p setting.
         * 
         * @return the top-p value
         */
        @Nullable
        Double getTopP();

        /**
         * Gets the frequency penalty setting.
         * 
         * @return the frequency penalty value
         */
        @Nullable
        Double getFrequencyPenalty();

        /**
         * Gets the presence penalty setting.
         * 
         * @return the presence penalty value
         */
        @Nullable
        Double getPresencePenalty();

        /**
         * Gets the system prompt.
         * 
         * @return the system prompt
         */
        @Nullable
        String getSystemPrompt();

        /**
         * Gets the preset metadata.
         * 
         * @return map of metadata key-value pairs
         */
        Map<String, Object> getMetadata();

        /**
         * Gets provider-specific parameters.
         * 
         * @return map of provider-specific parameters
         */
        Map<String, Object> getProviderParameters();
    }
}
