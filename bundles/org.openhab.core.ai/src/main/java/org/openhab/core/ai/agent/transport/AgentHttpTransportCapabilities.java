package org.openhab.core.ai.agent.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.transport.api.TransportCapabilities;
import org.openhab.core.ai.agent.transport.api.TransportType;

/**
 * HTTP transport capabilities (extracted from AgentHttpTransport).
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class AgentHttpTransportCapabilities implements TransportCapabilities {
    @Override
    public TransportType getTransportType() {
        return TransportType.REST;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supportsBidirectional() {
        return false;
    }

    @Override
    public boolean supportsAuthentication() {
        return true;
    }

    @Override
    public int getMaxMessageSize() {
        return 10 * 1024 * 1024;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of("baseUrl", "http://localhost:8080", "endpoints",
                Map.of("messageSend", "/a2a/message/send", "messageStream", "/a2a/message/stream", "health",
                        "/a2a/health", "status", "/a2a/status", "agentCard", "/.well-known/agent.json"),
                "cors", Map.of("enabled", true, "allowedOrigins", "*", "allowedMethods", "GET,POST,OPTIONS"),
                "compression", "none");
    }
}
