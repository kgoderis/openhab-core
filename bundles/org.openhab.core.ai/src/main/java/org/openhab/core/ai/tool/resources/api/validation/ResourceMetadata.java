package org.openhab.core.ai.tool.resources.api.validation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resource Metadata for MCP Resources
 * 
 * This class defines metadata for MCP resource specifications
 * including version, author, and additional properties.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ResourceMetadata {

    private final String version;
    private final String author;
    private final Map<String, Object> properties;

    /**
     * Create a new ResourceMetadata instance
     * 
     * @param version The version of the resource
     * @param author The author of the resource
     * @param properties Additional properties
     */
    public ResourceMetadata(String version, String author, Map<String, Object> properties) {
        this.version = version;
        this.author = author;
        this.properties = properties;
    }

    /**
     * Get the version of the resource
     * 
     * @return The version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the author of the resource
     * 
     * @return The author string
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get additional properties
     * 
     * @return The properties map
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Get a specific property
     * 
     * @param key The property key
     * @return The property value or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }
}
