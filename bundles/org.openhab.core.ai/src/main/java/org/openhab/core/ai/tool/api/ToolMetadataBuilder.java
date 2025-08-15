package org.openhab.core.ai.tool.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder extracted for `ToolMetadata`.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolMetadataBuilder {
    private String version = "1.0.0";
    private String author = "Unknown";
    private String description = "No description provided";

    public ToolMetadataBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ToolMetadataBuilder author(String author) {
        this.author = author;
        return this;
    }

    public ToolMetadataBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ToolMetadata build() {
        return new ToolMetadata(version, author, description);
    }
}
