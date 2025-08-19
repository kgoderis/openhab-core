package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.ActionMetadata;

/**
 * Unified builder for ActionMetadata objects.
 *
 * <p>
 * This builder provides a standardized way to create ActionMetadata objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionMetadataBuilder extends ActionBuilder<ActionMetadata> {

    /* package */ String version = "1.0.0";
    /* package */ String author = "openHAB AI Team";
    /* package */ String description = "";
    /* package */ List<String> tags = List.of();
    /* package */ Map<String, Object> properties = Map.of();
    /* package */ Instant created = Instant.now();
    /* package */ Instant lastModified = Instant.now();
    /* package */ String documentation = "";
    /* package */ List<String> examples = List.of();
    /* package */ Map<String, Object> requirements = Map.of();

    /**
     * Set the version.
     *
     * @param version the version
     * @return this builder
     */
    public ActionMetadataBuilder withVersion(String version) {
        this.version = Objects.requireNonNull(version, "version");
        return this;
    }

    /**
     * Set the author.
     *
     * @param author the author
     * @return this builder
     */
    public ActionMetadataBuilder withAuthor(String author) {
        this.author = Objects.requireNonNull(author, "author");
        return this;
    }

    /**
     * Set the description.
     *
     * @param description the description
     * @return this builder
     */
    public ActionMetadataBuilder withDescription(String description) {
        this.description = Objects.requireNonNull(description, "description");
        return this;
    }

    /**
     * Set the tags.
     *
     * @param tags the tags
     * @return this builder
     */
    public ActionMetadataBuilder withTags(List<String> tags) {
        this.tags = Objects.requireNonNull(tags, "tags");
        return this;
    }

    /**
     * Set the properties.
     *
     * @param properties the properties
     * @return this builder
     */
    public ActionMetadataBuilder withProperties(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
        return this;
    }

    /**
     * Set the created timestamp.
     *
     * @param created the created timestamp
     * @return this builder
     */
    public ActionMetadataBuilder withCreated(Instant created) {
        this.created = Objects.requireNonNull(created, "created");
        return this;
    }

    /**
     * Set the last modified timestamp.
     *
     * @param lastModified the last modified timestamp
     * @return this builder
     */
    public ActionMetadataBuilder withLastModified(Instant lastModified) {
        this.lastModified = Objects.requireNonNull(lastModified, "lastModified");
        return this;
    }

    /**
     * Set the documentation.
     *
     * @param documentation the documentation
     * @return this builder
     */
    public ActionMetadataBuilder withDocumentation(String documentation) {
        this.documentation = Objects.requireNonNull(documentation, "documentation");
        return this;
    }

    /**
     * Set the examples.
     *
     * @param examples the examples
     * @return this builder
     */
    public ActionMetadataBuilder withExamples(List<String> examples) {
        this.examples = Objects.requireNonNull(examples, "examples");
        return this;
    }

    /**
     * Set the requirements.
     *
     * @param requirements the requirements
     * @return this builder
     */
    public ActionMetadataBuilder withRequirements(Map<String, Object> requirements) {
        this.requirements = Objects.requireNonNull(requirements, "requirements");
        return this;
    }

    @Override
    public ActionMetadata build() {
        validate();
        return new ActionMetadata(this);
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ActionMetadata
        if (version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
        if (author.isBlank()) {
            throw new IllegalArgumentException("author must not be blank");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        version = "1.0.0";
        author = "openHAB AI Team";
        description = "";
        tags = List.of();
        properties = Map.of();
        created = Instant.now();
        lastModified = Instant.now();
        documentation = "";
        examples = List.of();
        requirements = Map.of();
    }

    /**
     * Create a new ActionMetadataBuilder instance.
     *
     * @return a new builder instance
     */
    public static ActionMetadataBuilder builder() {
        return new ActionMetadataBuilder();
    }

    /**
     * Create a builder from an existing ActionMetadata.
     *
     * @param metadata the existing metadata
     * @return a builder with values from the existing metadata
     */
    public static ActionMetadataBuilder builder(ActionMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
        return builder().withVersion(metadata.getVersion()).withAuthor(metadata.getAuthor())
                .withDescription(metadata.getDescription()).withTags(metadata.getTags())
                .withProperties(metadata.getProperties()).withCreated(metadata.getCreated())
                .withLastModified(metadata.getLastModified()).withDocumentation(metadata.getDocumentation())
                .withExamples(metadata.getExamples()).withRequirements(metadata.getRequirements());
    }

    // Getters for the ActionMetadata constructor
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
}
