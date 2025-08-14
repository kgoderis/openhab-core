package org.openhab.core.ai.agent.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class TransportNegotiationResult {
    private final TransportType selectedTransport;
    private final Map<String, Object> negotiationData;
    private final boolean success;
    private final String reason;

    public TransportNegotiationResult(TransportType selectedTransport, Map<String, Object> negotiationData,
            boolean success, String reason) {
        this.selectedTransport = selectedTransport;
        this.negotiationData = negotiationData;
        this.success = success;
        this.reason = reason;
    }

    public TransportType getSelectedTransport() { return selectedTransport; }
    public Map<String, Object> getNegotiationData() { return negotiationData; }
    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
}
