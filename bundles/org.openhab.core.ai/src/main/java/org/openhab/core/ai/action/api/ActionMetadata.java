package org.openhab.core.ai.action.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Metadata information for an action.
 * 
 * This class provides additional information about an action including
 * version, author, tags, and other descriptive metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionMetadata {

    private final String version;
    private final String author;
    private final String description;
    private final List<String> tags;
    private final Map<String, Object> properties;
    private final Instant created;
    private final Instant lastModified;
    private final String documentation;
    private final List<String> examples;
    private final Map<String, Object> requirements;

    private ActionMetadata(Builder builder) {
        this.version = builder.version;
        this.author = builder.author;
        this.description = builder.description;
        this.tags = List.copyOf(builder.tags);
        this.properties = Map.copyOf(builder.properties);
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.documentation = builder.documentation;
        this.examples = List.copyOf(builder.examples);
        this.requirements = Map.copyOf(builder.requirements);
    }

    /**
     * Create ActionMetadata from an Action.
     * 
     * @param action the action to create metadata for
     */
    public ActionMetadata(Action action) {
        this.version = action.getVersion();
        this.author = "openHAB AI Team";
        this.description = action.getDescription();
        this.tags = List.of(action.getCategory());
        this.properties = Map.of();
        this.created = Instant.now();
        this.lastModified = Instant.now();
        this.documentation = "";
        this.examples = List.of();
        this.requirements = Map.of();
    }

    // Getters
    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public String getDocumentation() {
        return documentation;
    }

    public List<String> getExamples() {
        return examples;
    }

    public Map<String, Object> getRequirements() {
        return requirements;
    }

    /**
     * Get the category from the first tag.
     * 
     * @return the category
     */
    public String getCategory() {
        return tags.isEmpty() ? "general" : tags.get(0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for creating ActionMetadata instances.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {

        private String version = "1.0.0";
        private String author = "openHAB AI Team";
        private String description = "";
        private List<String> tags = List.of();
        private Map<String, Object> properties = Map.of();
        private Instant created = Instant.now();
        private Instant lastModified = Instant.now();
        private String documentation = "";
        private List<String> examples = List.of();
        private Map<String, Object> requirements = Map.of();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         * 
         * @param source the source ActionMetadata
         */
        public Builder(ActionMetadata source) {
            this.version = source.version;
            this.author = source.author;
            this.description = source.description;
            this.tags = new ArrayList<>(source.tags);
            this.properties = new HashMap<>(source.properties);
            this.created = source.created;
            this.lastModified = source.lastModified;
            this.documentation = source.documentation;
            this.examples = new ArrayList<>(source.examples);
            this.requirements = new HashMap<>(source.requirements);
        }

        /**
         * Set the version.
         * 
         * @param version the version
         * @return this builder
         */
        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        /**
         * Set the author.
         * 
         * @param author the author
         * @return this builder
         */
        public Builder withAuthor(String author) {
            this.author = Objects.requireNonNull(author, "author");
            return this;
        }

        /**
         * Set the description.
         * 
         * @param description the description
         * @return this builder
         */
        public Builder withDescription(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        /**
         * Set the tags.
         * 
         * @param tags the tags
         * @return this builder
         */
        public Builder withTags(List<String> tags) {
            this.tags = Objects.requireNonNull(tags, "tags");
            return this;
        }

        /**
         * Set the properties.
         * 
         * @param properties the properties
         * @return this builder
         */
        public Builder withProperties(Map<String, Object> properties) {
            this.properties = Objects.requireNonNull(properties, "properties");
            return this;
        }

        /**
         * Set the created timestamp.
         * 
         * @param created the created timestamp
         * @return this builder
         */
        public Builder withCreated(Instant created) {
            this.created = Objects.requireNonNull(created, "created");
            return this;
        }

        /**
         * Set the last modified timestamp.
         * 
         * @param lastModified the last modified timestamp
         * @return this builder
         */
        public Builder withLastModified(Instant lastModified) {
            this.lastModified = Objects.requireNonNull(lastModified, "lastModified");
            return this;
        }

        /**
         * Set the documentation.
         * 
         * @param documentation the documentation
         * @return this builder
         */
        public Builder withDocumentation(String documentation) {
            this.documentation = Objects.requireNonNull(documentation, "documentation");
            return this;
        }

        /**
         * Set the examples.
         * 
         * @param examples the examples
         * @return this builder
         */
        public Builder withExamples(List<String> examples) {
            this.examples = Objects.requireNonNull(examples, "examples");
            return this;
        }

        /**
         * Set the requirements.
         * 
         * @param requirements the requirements
         * @return this builder
         */
        public Builder withRequirements(Map<String, Object> requirements) {
            this.requirements = Objects.requireNonNull(requirements, "requirements");
            return this;
        }

        /**
         * Build the ActionMetadata.
         * 
         * @return the new ActionMetadata
         */
        public ActionMetadata build() {
            if (version.isBlank()) {
                throw new IllegalArgumentException("version must not be blank");
            }
            if (author.isBlank()) {
                throw new IllegalArgumentException("author must not be blank");
            }
            return new ActionMetadata(this);
        }
    }

    @Override
    public String toString() {
        return String.format("ActionMetadata{version='%s', author='%s', tags=%s}", version, author, tags);
    }
}
