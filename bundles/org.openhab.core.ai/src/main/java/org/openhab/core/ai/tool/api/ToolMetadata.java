package org.openhab.core.ai.tool.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.ToolMetadataBuilder;

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

    public ToolMetadata(String version, String author, String description) {
        this.version = version;
        this.author = author;
        this.description = description;
    }

    public static ToolMetadataBuilder builder() {
        return new ToolMetadataBuilder();
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

    // Builder extracted to org.openhab.core.ai.tool.api.ToolMetadataBuilder
}
