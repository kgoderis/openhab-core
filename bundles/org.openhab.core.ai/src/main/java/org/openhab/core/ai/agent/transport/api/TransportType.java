package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum TransportType {
    JSON_RPC("json-rpc"),
    GRPC("grpc"),
    REST("rest");

    private final String identifier;

    TransportType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
