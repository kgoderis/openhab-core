package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class QualityMetric {
    private final String component;
    private final String operation;
    private final List<Double> qualities = new ArrayList<>();
    private final List<String> details = new ArrayList<>();
    private final List<Instant> timestamps = new ArrayList<>();

    public QualityMetric(String component, String operation) {
        this.component = component;
        this.operation = operation;
    }

    public void recordQuality(double quality, String details) {
        qualities.add(quality);
        this.details.add(details);
        timestamps.add(Instant.now());
        if (qualities.size() > 100) {
            qualities.remove(0);
            this.details.remove(0);
            timestamps.remove(0);
        }
    }

    public String getComponent() { return component; }
    public String getOperation() { return operation; }
    public double getAverageQuality() { return qualities.isEmpty() ? 1.0 : qualities.stream().mapToDouble(Double::doubleValue).average().orElse(1.0); }
    public String getLastDetails() { return details.isEmpty() ? "" : details.get(details.size() - 1); }
    public List<Double> getQualities() { return new ArrayList<>(qualities); }
    public List<String> getDetails() { return new ArrayList<>(details); }
    public List<Instant> getTimestamps() { return new ArrayList<>(timestamps); }
}
