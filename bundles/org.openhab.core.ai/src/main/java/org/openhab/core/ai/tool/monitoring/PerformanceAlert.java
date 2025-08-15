package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record PerformanceAlert(String alertId, String specificationId, String type, String message, String severity,
        Instant timestamp) {
    public String getAlertId() {
        return alertId;
    }

    public String getSpecificationId() {
        return specificationId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
