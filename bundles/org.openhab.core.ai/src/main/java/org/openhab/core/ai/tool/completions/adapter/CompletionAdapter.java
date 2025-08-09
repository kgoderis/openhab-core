package org.openhab.core.ai.tool.completions.adapter;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.completions.dto.Completion;

/**
 * Adapter for converting between different completion formats.
 * 
 * This interface defines the contract for completion adapters that can convert
 * between different completion representations and formats.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CompletionAdapter {

    /**
     * Get the adapter ID.
     * 
     * @return the adapter ID
     */
    String getAdapterId();

    /**
     * Get the adapter name.
     * 
     * @return the adapter name
     */
    String getAdapterName();

    /**
     * Get the supported source formats.
     * 
     * @return list of supported source formats
     */
    String[] getSupportedSourceFormats();

    /**
     * Get the supported target formats.
     * 
     * @return list of supported target formats
     */
    String[] getSupportedTargetFormats();

    /**
     * Check if the adapter can convert from the source format to the target format.
     * 
     * @param sourceFormat the source format
     * @param targetFormat the target format
     * @return true if conversion is supported
     */
    boolean canConvert(String sourceFormat, String targetFormat);

    /**
     * Convert a completion from one format to another.
     * 
     * @param sourceCompletion the source completion
     * @param sourceFormat the source format
     * @param targetFormat the target format
     * @return the converted completion
     */
    Completion convert(Completion sourceCompletion, String sourceFormat, String targetFormat);

    /**
     * Get the adapter configuration.
     * 
     * @return the adapter configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the adapter configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement completion format conversion logic
    // TODO: Add support for custom completion formats
    // TODO: Implement completion validation during conversion
    // TODO: Add support for conversion caching
}
