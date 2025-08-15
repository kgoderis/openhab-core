package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record PerformanceOptimization(String optimizationId, String specificationId, String type, String description,
        String impact, Instant timestamp) {
    public String getOptimizationId() {
        return optimizationId;
    }

    public String getSpecificationId() {
        return specificationId;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getImpact() {
        return impact;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
