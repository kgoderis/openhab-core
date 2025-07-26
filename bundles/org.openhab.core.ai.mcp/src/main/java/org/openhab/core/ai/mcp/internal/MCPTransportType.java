package org.openhab.core.ai.mcp.internal;

/**
 * Enumeration of supported MCP transport types.
 * 
 * This enum defines the different communication transports
 * available for MCP server-client communication.
 * 
 * 
 */
public enum MCPTransportType {

    /**
     * Standard input/output transport for process-based communication.
     */
    STDIO,

    /**
     * Server-Sent Events (SSE) over HTTP transport.
     */
    SSE,

}
