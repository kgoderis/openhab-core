package org.openhab.core.ai.mcp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport type enumeration for MCP.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
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
