#!/bin/bash

# Example deployment script for openHAB MCP Bundle
# Usage: ./deploy.sh [JVM_OPTS]

# Set environment variables (edit as needed)
export MCP_SERVER_ID=${MCP_SERVER_ID:-openhab-mcp-server}
export MCP_SERVER_NAME=${MCP_SERVER_NAME:-"openHAB MCP Server"}
export MCP_SERVER_VERSION=${MCP_SERVER_VERSION:-1.0.0}
export MCP_TRANSPORT_TYPE=${MCP_TRANSPORT_TYPE:-STDIO}

# JVM options (can be passed as arguments)
JVM_OPTS="$@"

# Path to OSGi container (edit as needed)
OSGI_HOME=${OSGI_HOME:-/opt/openhab}
BUNDLE_JAR="bundles/org.openhab.core.ai.mcp/target/org.openhab.core.ai.mcp-*.jar"

# Start the OSGi container with the MCP bundle
cd "$OSGI_HOME"
echo "Deploying MCP bundle..."
java $JVM_OPTS -jar start.jar &

# Wait for OSGi to start, then install the bundle
sleep 10
# (Assumes OSGi console is available on port 8081)
echo "install file:$(pwd)/$BUNDLE_JAR" | nc localhost 8081

echo "MCP bundle deployed." 