package org.openhab.core.ai.common.builder;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.ToolMetadata;

/**
 * Unified builder for ToolMetadata objects.
 *
 * <p>
 * This builder provides a standardized way to create ToolMetadata objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ToolMetadataBuilder extends ModelBuilder<ToolMetadata> {

    @Override
    public ToolMetadata build() {
        validate();
        return new ToolMetadata(version, author, description);
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ToolMetadata
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }

    /**
     * Create a new ToolMetadataBuilder instance.
     *
     * @return a new builder instance
     */
    public static ToolMetadataBuilder builder() {
        return new ToolMetadataBuilder();
    }

    /**
     * Create a builder from an existing ToolMetadata.
     *
     * @param metadata the existing metadata
     * @return a builder with values from the existing metadata
     */
    public static ToolMetadataBuilder builder(ToolMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
        return (ToolMetadataBuilder) builder().withVersion(metadata.getVersion()).withAuthor(metadata.getAuthor())
                .withDescription(metadata.getDescription());
    }

    // Getters for the ToolMetadata constructor
    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }
}
