package org.openhab.core.ai.tool.api;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Metadata information for MCP tools.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolMetadata {
    private final String version;
    private final String author;
    private final String description;

    /**
     * Private constructor for builder pattern.
     */
    private ToolMetadata(Builder builder) {
        this.version = builder.version;
        this.author = builder.author;
        this.description = builder.description;
    }

    /**
     * Create a new builder for ToolMetadata.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this ToolMetadata for modification.
     *
     * @return a new Builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Builder for creating ToolMetadata instances.
     *
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String version = "1.0.0";
        private String author = "Unknown";
        private String description = "No description provided";

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         *
         * @param source the source ToolMetadata
         */
        public Builder(ToolMetadata source) {
            this.version = source.version;
            this.author = source.author;
            this.description = source.description;
        }

        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder withAuthor(String author) {
            this.author = Objects.requireNonNull(author, "author");
            return this;
        }

        public Builder withDescription(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        /**
         * Build the ToolMetadata.
         *
         * @return the new ToolMetadata
         */
        public ToolMetadata build() {
            if (version.isBlank()) {
                throw new IllegalArgumentException("version must not be blank");
            }
            if (author.isBlank()) {
                throw new IllegalArgumentException("author must not be blank");
            }
            if (description.isBlank()) {
                throw new IllegalArgumentException("description must not be blank");
            }
            return new ToolMetadata(this);
        }
    }
}
