package org.openhab.core.ai.tool.server.transport;

import java.net.URI;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface LoadBalancer {
    URI selectBackend();

    void addBackend(URI backend);

    void removeBackend(URI backend);

    Map<String, Object> getStatistics();
}


