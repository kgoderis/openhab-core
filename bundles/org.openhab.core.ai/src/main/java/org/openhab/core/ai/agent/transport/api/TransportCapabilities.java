package org.openhab.core.ai.agent.transport.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.transport.TransportType;

@NonNullByDefault
public interface TransportCapabilities {
    TransportType getTransportType();

    boolean supportsStreaming();

    boolean supportsBidirectional();

    boolean supportsAuthentication();

    int getMaxMessageSize();

    Map<String, Object> getConfiguration();
}
