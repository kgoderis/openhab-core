# Dockerfile for openHAB MCP Bundle
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy MCP bundle jar
COPY target/org.openhab.core.ai.mcp-*.jar /app/mcp-bundle.jar

# Expose HTTP port (for health/metrics endpoints)
EXPOSE 8080

# Set environment variables (can be overridden)
ENV MCP_SERVER_ID=openhab-mcp-server \
    MCP_SERVER_NAME="openHAB MCP Server" \
    MCP_SERVER_VERSION=1.0.0 \
    MCP_TRANSPORT_TYPE=STDIO

# Start the MCP bundle (as a standalone Java process)
CMD ["java", "-jar", "/app/mcp-bundle.jar"]