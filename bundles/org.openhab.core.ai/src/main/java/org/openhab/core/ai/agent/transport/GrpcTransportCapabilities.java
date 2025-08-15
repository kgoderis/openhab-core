package org.openhab.core.ai.agent.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.transport.api.TransportCapabilities;
import org.openhab.core.ai.agent.transport.api.TransportType;

/**
 * Top-level gRPC transport capabilities extracted from AgentGrpcTransport.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GrpcTransportCapabilities implements TransportCapabilities {

    @Override
    public TransportType getTransportType() {
        return TransportType.GRPC;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supportsBidirectional() {
        return true;
    }

    @Override
    public boolean supportsAuthentication() {
        return true;
    }

    @Override
    public int getMaxMessageSize() {
        return 4 * 1024 * 1024; // 4MB
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of("port", 8083, "compression", "gzip", "maxConcurrentStreams", 100, "keepAliveTime", 30000,
                "keepAliveTimeout", 5000);
    }
}
