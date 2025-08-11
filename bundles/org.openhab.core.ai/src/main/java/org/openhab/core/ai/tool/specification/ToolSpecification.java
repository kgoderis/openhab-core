package org.openhab.core.ai.tool.specification;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Specification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tool specification model for MCP tools.
 * 
 * This class represents the specification of a tool, including its capabilities,
 * configuration, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolSpecification implements Specification {

    private final String id;
    private final String name;
    private final String description;
    private final String version;
    private final Map<String, Object> inputSchema;
    private final Map<String, Object> outputSchema;
    private final Map<String, Object> configuration;
    private final Map<String, Object> metadata;

    /**
     * Create a new tool specification.
     * 
     * @param id the tool ID
     * @param name the tool name
     * @param description the tool description
     * @param version the tool version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the tool configuration
     * @param metadata the tool metadata
     */
    @JsonCreator
    public ToolSpecification(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("description") String description, @JsonProperty("version") String version,
            @JsonProperty("inputSchema") Map<String, Object> inputSchema,
            @JsonProperty("outputSchema") Map<String, Object> outputSchema,
            @JsonProperty("configuration") Map<String, Object> configuration,
            @JsonProperty("metadata") Map<String, Object> metadata) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
        this.configuration = configuration;
        this.metadata = metadata;

        // Validate the specification during construction
        validate();
    }

    /**
     * Get the tool ID.
     * 
     * @return the tool ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the tool name.
     * 
     * @return the tool name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the tool description.
     * 
     * @return the tool description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the tool version.
     * 
     * @return the tool version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the input schema.
     * 
     * @return the input schema
     */
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    /**
     * Get the output schema.
     * 
     * @return the output schema
     */
    public Map<String, Object> getOutputSchema() {
        return outputSchema;
    }

    /**
     * Get the tool configuration.
     * 
     * @return the tool configuration
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Get the tool metadata.
     * 
     * @return the tool metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public SpecificationType getType() {
        return SpecificationType.RESOURCE; // Tools are treated as resources in the specification system
    }

    /**
     * Validate the tool specification.
     * 
     * @throws IllegalArgumentException if the specification is invalid
     */
    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool description cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool version cannot be null or empty");
        }

        // Validate version format (semantic versioning)
        if (!isValidVersion(version)) {
            throw new IllegalArgumentException("Tool version must follow semantic versioning format (e.g., 1.0.0)");
        }

        // Validate input schema if present
        if (inputSchema != null) {
            validateSchema(inputSchema, "input schema");
        }

        // Validate output schema if present
        if (outputSchema != null) {
            validateSchema(outputSchema, "output schema");
        }
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
        return version.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?(\\+[a-zA-Z0-9.-]+)?$");
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
     * Get the major version number.
     * 
     * @return the major version
     */
    public int getMajorVersion() {
        return Integer.parseInt(version.split("\\.")[0]);
    }

    /**
     * Get the minor version number.
     * 
     * @return the minor version
     */
    public int getMinorVersion() {
        return Integer.parseInt(version.split("\\.")[1]);
    }

    /**
     * Get the patch version number.
     * 
     * @return the patch version
     */
    public int getPatchVersion() {
        String[] parts = version.split("\\.");
        if (parts.length < 3) {
            return 0;
        }
        String patchPart = parts[2].split("-")[0].split("\\+")[0];
        return Integer.parseInt(patchPart);
    }

    /**
     * Check if this specification is compatible with another specification.
     * 
     * @param other the other specification to compare with
     * @return true if the specifications are compatible
     */
    public boolean isCompatibleWith(ToolSpecification other) {
        if (other == null) {
            return false;
        }

        // Check if the tools are the same
        if (!id.equals(other.id)) {
            return false;
        }

        // Check major version compatibility (same major version required)
        if (getMajorVersion() != other.getMajorVersion()) {
            return false;
        }

        // Check if this version is newer or equal
        return compareVersions(version, other.version) >= 0;
    }

    /**
     * Compare two version strings.
     * 
     * @param version1 the first version
     * @param version2 the second version
     * @return negative if version1 < version2, 0 if equal, positive if version1 > version2
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i].split("-")[0].split("\\+")[0]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i].split("-")[0].split("\\+")[0]) : 0;

            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ToolSpecification other = (ToolSpecification) obj;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name)
                && Objects.equals(description, other.description) && Objects.equals(version, other.version)
                && Objects.equals(inputSchema, other.inputSchema) && Objects.equals(outputSchema, other.outputSchema)
                && Objects.equals(configuration, other.configuration) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, version, inputSchema, outputSchema, configuration, metadata);
    }

    @Override
    public String toString() {
        return "ToolSpecification{id='" + id + "', name='" + name + "', description='" + description + "', version='"
                + version + "', inputSchema=" + inputSchema + ", outputSchema=" + outputSchema + ", configuration="
                + configuration + ", metadata=" + metadata + "}";
    }
}
