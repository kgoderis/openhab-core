package org.openhab.core.ai.mcp.api;

public class MCPToolMetadata {
    private final String version;
    private final String author;
    private final String description;

    private MCPToolMetadata(String version, String author, String description) {
        this.version = version;
        this.author = author;
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private String version = "1.0.0";
        private String author;
        private String description;

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

        public MCPToolMetadata build() {
            return new MCPToolMetadata(version, author, description);
        }
    }
}
