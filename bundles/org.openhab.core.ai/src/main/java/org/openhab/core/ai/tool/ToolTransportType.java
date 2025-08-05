package org.openhab.core.ai.tool.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport type enumeration for MCP Tools.
 * 
 * This enum defines the available transport types for the MCP Tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ToolTransportType {

    /**
     * Standard input/output transport for process-based communication.
     */
    STDIO,

    /**
     * Server-Sent Events (SSE) over HTTP transport.
     */
    SSE,

}
