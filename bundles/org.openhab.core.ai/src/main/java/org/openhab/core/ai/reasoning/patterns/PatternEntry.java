package org.openhab.core.ai.reasoning.patterns;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.memory.MemoryEntry;

@NonNullByDefault
public class PatternEntry {
    private final String category;
    private int occurrenceCount;
    private double averageImportance;
    private Instant lastOccurrence;

    public PatternEntry(String category) {
        this.category = category;
        this.occurrenceCount = 0;
        this.averageImportance = 0.0;
        this.lastOccurrence = Instant.now();
    }

    public void addOccurrence(MemoryEntry entry) {
        occurrenceCount++;
        averageImportance = ((averageImportance * (occurrenceCount - 1)) + entry.getImportance()) / occurrenceCount;
        lastOccurrence = entry.getTimestamp();
    }

    public String getCategory() {
        return category;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public double getAverageImportance() {
        return averageImportance;
    }

    public Instant getLastOccurrence() {
        return lastOccurrence;
    }
}
