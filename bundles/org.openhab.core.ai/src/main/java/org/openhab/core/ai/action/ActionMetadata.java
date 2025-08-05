package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.api.action.Action;

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
        this.tags = builder.tags != null ? builder.tags : List.of();
        this.properties = builder.properties != null ? builder.properties : Map.of();
        this.created = builder.created != null ? builder.created : Instant.now();
        this.lastModified = builder.lastModified != null ? builder.lastModified : Instant.now();
        this.documentation = builder.documentation;
        this.examples = builder.examples != null ? builder.examples : List.of();
        this.requirements = builder.requirements != null ? builder.requirements : Map.of();
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

    /**
     * Builder for ActionMetadata.
     */
    public static class Builder {
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

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder created(Instant created) {
            this.created = created;
            return this;
        }

        public Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public Builder examples(List<String> examples) {
            this.examples = examples;
            return this;
        }

        public Builder requirements(Map<String, Object> requirements) {
            this.requirements = requirements;
            return this;
        }

        public ActionMetadata build() {
            return new ActionMetadata(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ActionMetadata{version='%s', author='%s', tags=%s}", version, author, tags);
    }
}
