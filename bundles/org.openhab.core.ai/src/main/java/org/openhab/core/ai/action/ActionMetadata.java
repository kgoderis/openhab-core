package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.Action;

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

    /* package */ ActionMetadata(ActionMetadataBuilder builder) {
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

    public static ActionMetadataBuilder builder() {
        return new ActionMetadataBuilder();
    }

    @Override
    public String toString() {
        return String.format("ActionMetadata{version='%s', author='%s', tags=%s}", version, author, tags);
    }
}
