package org.openhab.core.ai.agent.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface TransportCapabilities {
    TransportType getTransportType();
    boolean supportsStreaming();
    boolean supportsBidirectional();
    boolean supportsAuthentication();
    int getMaxMessageSize();
    Map<String, Object> getConfiguration();
}


