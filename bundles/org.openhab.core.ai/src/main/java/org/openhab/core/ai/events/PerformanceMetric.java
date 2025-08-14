package org.openhab.core.ai.events;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PerformanceMetric {
    private final String component;
    private final String operation;
    private final List<Duration> durations = new ArrayList<>();
    private final List<Boolean> successes = new ArrayList<>();
    private final List<Instant> timestamps = new ArrayList<>();

    public PerformanceMetric(String component, String operation) {
        this.component = component;
        this.operation = operation;
    }

    public void recordExecution(Duration duration, boolean success) {
        durations.add(duration);
        successes.add(success);
        timestamps.add(Instant.now());
        if (durations.size() > 100) {
            durations.remove(0);
            successes.remove(0);
            timestamps.remove(0);
        }
    }

    public String getComponent() { return component; }
    public String getOperation() { return operation; }

    public Duration getAverageDuration() {
        if (durations.isEmpty()) { return Duration.ZERO; }
        long totalMillis = durations.stream().mapToLong(Duration::toMillis).sum();
        return Duration.ofMillis(totalMillis / durations.size());
    }

    public double getSuccessRate() {
        if (successes.isEmpty()) { return 1.0; }
        long successCount = successes.stream().filter(s -> s).count();
        return (double) successCount / successes.size();
    }

    public List<Duration> getDurations() { return new ArrayList<>(durations); }
    public List<Boolean> getSuccesses() { return new ArrayList<>(successes); }
    public List<Instant> getTimestamps() { return new ArrayList<>(timestamps); }
}
