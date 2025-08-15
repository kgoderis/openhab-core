package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ResourceMetric {
    private final String resource;
    private final String operation;
    private final List<Double> utilizations = new ArrayList<>();
    private final List<String> details = new ArrayList<>();
    private final List<Instant> timestamps = new ArrayList<>();

    public ResourceMetric(String resource, String operation) {
        this.resource = resource;
        this.operation = operation;
    }

    public void recordUtilization(double utilization, String details) {
        utilizations.add(utilization);
        this.details.add(details);
        timestamps.add(Instant.now());
        if (utilizations.size() > 100) {
            utilizations.remove(0);
            this.details.remove(0);
            timestamps.remove(0);
        }
    }

    public String getResource() {
        return resource;
    }

    public String getOperation() {
        return operation;
    }

    public double getAverageUtilization() {
        return utilizations.isEmpty() ? 0.0
                : utilizations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public String getLastDetails() {
        return details.isEmpty() ? "" : details.get(details.size() - 1);
    }

    public List<Double> getUtilizations() {
        return new ArrayList<>(utilizations);
    }

    public List<String> getDetails() {
        return new ArrayList<>(details);
    }

    public List<Instant> getTimestamps() {
        return new ArrayList<>(timestamps);
    }
}
