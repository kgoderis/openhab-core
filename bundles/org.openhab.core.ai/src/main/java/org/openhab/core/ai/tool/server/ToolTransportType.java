package org.openhab.core.ai.tool.server;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport type enumeration for MCP Tool Server.
 * 
 * Defines the different types of transport protocols that can be used
 * for communication with the tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ToolTransportType {

    /**
     * Standard Input/Output transport.
     * Uses stdin/stdout for communication.
     */
    STDIO("stdio", "Standard Input/Output"),

    /**
     * Server-Sent Events transport.
     * Uses HTTP with SSE for real-time communication.
     */
    SSE("sse", "Server-Sent Events"),

    /**
     * WebSocket transport.
     * Uses WebSocket for bidirectional communication.
     */
    WEBSOCKET("websocket", "WebSocket"),

    /**
     * HTTP transport.
     * Uses standard HTTP requests/responses.
     */
    HTTP("http", "HTTP"),

    /**
     * TCP transport.
     * Uses raw TCP socket communication.
     */
    TCP("tcp", "TCP Socket"),

    /**
     * Unix Domain Socket transport.
     * Uses Unix domain sockets for local communication.
     */
    UNIX("unix", "Unix Domain Socket");

    private final String code;
    private final String description;

    /**
     * Create a new transport type.
     *
     * @param code the transport code
     * @param description the transport description
     */
    ToolTransportType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the transport code.
     *
     * @return the transport code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the transport description.
     *
     * @return the transport description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get transport type by code.
     *
     * @param code the transport code
     * @return the transport type, or null if not found
     */
    public static ToolTransportType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (ToolTransportType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if this transport type supports bidirectional communication.
     *
     * @return true if bidirectional
     */
    public boolean isBidirectional() {
        return this == WEBSOCKET || this == TCP || this == UNIX;
    }

    /**
     * Check if this transport type is suitable for local communication.
     *
     * @return true if local
     */
    public boolean isLocal() {
        return this == STDIO || this == UNIX;
    }

    /**
     * Check if this transport type is suitable for web-based communication.
     *
     * @return true if web-based
     */
    public boolean isWebBased() {
        return this == SSE || this == WEBSOCKET || this == HTTP;
    }

    @Override
    public String toString() {
        return code + " (" + description + ")";
    }
}
