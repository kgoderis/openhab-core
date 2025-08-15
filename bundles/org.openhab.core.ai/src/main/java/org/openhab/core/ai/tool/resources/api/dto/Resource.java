package org.openhab.core.ai.tool.resources.api.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * MCP Resource data model.
 * 
 * This class represents a resource in the MCP protocol, providing access to
 * external data sources like files, databases, or web services.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Resource {

    private final String uri;
    private final String name;
    private final String description;
    private final String mimeType;
    private final @Nullable Map<String, Object> metadata;

    /**
     * Create a new resource.
     * 
     * @param uri Resource URI
     * @param name Resource name
     * @param description Resource description
     * @param mimeType MIME type
     * @param metadata Optional metadata
     */
    public Resource(String uri, String name, String description, String mimeType,
            @Nullable Map<String, Object> metadata) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.mimeType = mimeType;
        this.metadata = metadata;
    }

    /**
     * Get the resource URI.
     * 
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the resource name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the resource description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the MIME type.
     * 
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Get the metadata.
     * 
     * @return the metadata or null
     */
    public @Nullable Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Resource{uri='" + uri + "', name='" + name + "', description='" + description + "', mimeType='"
                + mimeType + "'}";
    }
}
