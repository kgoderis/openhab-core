package org.openhab.core.ai.tool.completions.api.specification;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.SpecificationType;
import org.openhab.core.ai.tool.specification.AbstractBaseSpecification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Completion specification model for MCP completions.
 * 
 * This class represents the specification of a completion, including its capabilities,
 * configuration, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CompletionSpecification extends AbstractBaseSpecification {

    /**
     * Create a new completion specification.
     * 
     * @param id the completion ID
     * @param name the completion name
     * @param description the completion description
     * @param version the completion version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the completion configuration
     * @param metadata the completion metadata
     */
    @JsonCreator
    public CompletionSpecification(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("description") String description, @JsonProperty("version") String version,
            @JsonProperty("inputSchema") Map<String, Object> inputSchema,
            @JsonProperty("outputSchema") Map<String, Object> outputSchema,
            @JsonProperty("configuration") Map<String, Object> configuration,
            @JsonProperty("metadata") Map<String, Object> metadata) {
        super(id, name, description, version, inputSchema, outputSchema, configuration, metadata);

        // Validate the specification during construction
        validate();
    }

    @Override
    public SpecificationType getType() {
        return SpecificationType.COMPLETION;
    }

    /**
     * Validate the completion specification.
     * 
     * @throws IllegalArgumentException if the specification is invalid
     */
    public void validate() {
        if (getId() == null || getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Completion ID cannot be null or empty");
        }
        if (getName() == null || getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Completion name cannot be null or empty");
        }
        if (getDescription() == null || getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Completion description cannot be null or empty");
        }
        if (getVersion() == null || getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("Completion version cannot be null or empty");
        }

        // Validate version format (semantic versioning)
        if (!isValidVersion(getVersion())) {
            throw new IllegalArgumentException(
                    "Completion version must follow semantic versioning format (e.g., 1.0.0)");
        }

        // Validate input schema if present
        if (getInputSchema() != null) {
            validateSchema(getInputSchema(), "input schema");
        }

        // Validate output schema if present
        if (getOutputSchema() != null) {
            validateSchema(getOutputSchema(), "output schema");
        }

        // Validate completion-specific requirements
        validateCompletionSpecificRequirements();
    }

    @Override
    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the version follows semantic versioning format.
     * 
     * @param version the version to validate
     * @return true if the version is valid
     */
    private boolean isValidVersion(String version) {
        // Basic semantic versioning validation (major.minor.patch)
        return version.matches("^d+.d+.d+(-[a-zA-Z0-9.-]+)?(+[a-zA-Z0-9.-]+)?$");
    }

    /**
     * Validate a JSON schema.
     * 
     * @param schema the schema to validate
     * @param schemaName the name of the schema for error messages
     */
    private void validateSchema(Map<String, Object> schema, String schemaName) {
        if (!schema.containsKey("type")) {
            throw new IllegalArgumentException(schemaName + " must contain a 'type' field");
        }

        Object type = schema.get("type");
        if (!(type instanceof String)) {
            throw new IllegalArgumentException(schemaName + " 'type' field must be a string");
        }

        String typeStr = (String) type;
        if (!isValidJsonSchemaType(typeStr)) {
            throw new IllegalArgumentException(schemaName + " 'type' field must be a valid JSON Schema type");
        }
    }

    /**
     * Check if a type is a valid JSON Schema type.
     * 
     * @param type the type to validate
     * @return true if the type is valid
     */
    private boolean isValidJsonSchemaType(String type) {
        return type.equals("object") || type.equals("array") || type.equals("string") || type.equals("number")
                || type.equals("integer") || type.equals("boolean") || type.equals("null");
    }

    /**
     * Validate completion-specific requirements.
     * 
     * @throws IllegalArgumentException if completion-specific requirements are not met
     */
    private void validateCompletionSpecificRequirements() {
        // Check if configuration contains required completion fields
        Map<String, Object> config = getConfiguration();
        if (config != null) {
            // Validate that completion has a prompt reference
            if (!config.containsKey("promptReference")) {
                throw new IllegalArgumentException("Completion configuration must contain 'promptReference' field");
            }

            // Validate that completion has suggestions configuration
            if (!config.containsKey("suggestions")) {
                throw new IllegalArgumentException("Completion configuration must contain 'suggestions' field");
            }
        }
    }

    /**
     * Get the major version number.
     * 
     * @return the major version
     */
    public int getMajorVersion() {
        return Integer.parseInt(getVersion().split(".")[0]);
    }

    /**
     * Get the minor version number.
     * 
     * @return the minor version
     */
    public int getMinorVersion() {
        return Integer.parseInt(getVersion().split(".")[1]);
    }

    /**
     * Get the patch version number.
     * 
     * @return the patch version
     */
    public int getPatchVersion() {
        String[] parts = getVersion().split(".");
        if (parts.length < 3) {
            return 0;
        }
        String patchPart = parts[2].split("-")[0].split("+")[0];
        return Integer.parseInt(patchPart);
    }

    /**
     * Check if this specification is compatible with another specification.
     * 
     * @param other the other specification to compare with
     * @return true if the specifications are compatible
     */
    public boolean isCompatibleWith(CompletionSpecification other) {
        if (other == null) {
            return false;
        }

        // Check if the completions are the same
        if (!getId().equals(other.getId())) {
            return false;
        }

        // Check major version compatibility (same major version required)
        if (getMajorVersion() != other.getMajorVersion()) {
            return false;
        }

        // Check if this version is newer or equal
        return compareVersions(getVersion(), other.getVersion()) >= 0;
    }

    /**
     * Compare two version strings.
     * 
     * @param version1 the first version
     * @param version2 the second version
     * @return negative if version1 < version2, 0 if equal, positive if version1 > version2
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split(".");
        String[] parts2 = version2.split(".");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i].split("-")[0].split("+")[0]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i].split("-")[0].split("+")[0]) : 0;

            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }

        return 0;
    }

    /**
     * Get the prompt reference from the configuration.
     * 
     * @return the prompt reference
     */
    public String getPromptReference() {
        Map<String, Object> config = getConfiguration();
        if (config != null && config.containsKey("promptReference")) {
            Object promptRef = config.get("promptReference");
            return promptRef instanceof String ? (String) promptRef : promptRef.toString();
        }
        return null;
    }

    /**
     * Get the suggestions configuration from the configuration.
     * 
     * @return the suggestions configuration
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSuggestionsConfig() {
        Map<String, Object> config = getConfiguration();
        if (config != null && config.containsKey("suggestions")) {
            Object suggestions = config.get("suggestions");
            if (suggestions instanceof Map) {
                return (Map<String, Object>) suggestions;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CompletionSpecification other = (CompletionSpecification) obj;
        return Objects.equals(getId(), other.getId()) && Objects.equals(getName(), other.getName())
                && Objects.equals(getDescription(), other.getDescription())
                && Objects.equals(getVersion(), other.getVersion())
                && Objects.equals(getInputSchema(), other.getInputSchema())
                && Objects.equals(getOutputSchema(), other.getOutputSchema())
                && Objects.equals(getConfiguration(), other.getConfiguration())
                && Objects.equals(getMetadata(), other.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getVersion(), getInputSchema(), getOutputSchema(),
                getConfiguration(), getMetadata());
    }

    @Override
    public String toString() {
        return "CompletionSpecification{id='" + getId() + "', name='" + getName() + "', description='"
                + getDescription() + "', version='" + getVersion() + "', inputSchema=" + getInputSchema()
                + ", outputSchema=" + getOutputSchema() + ", configuration=" + getConfiguration() + ", metadata="
                + getMetadata() + "}";
    }
}
