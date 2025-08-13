package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemoryPattern {
    private final String agentId;
    private final List<PatternEntry> patterns = new ArrayList<>();

    public MemoryPattern(String agentId) { this.agentId = agentId; }

    public void analyzeEntry(MemoryEntry entry) {
        PatternEntry pattern = patterns.stream().filter(p -> p.getCategory().equals(entry.getCategory())).findFirst()
                .orElseGet(() -> {
                    PatternEntry newPattern = new PatternEntry(entry.getCategory());
                    patterns.add(newPattern);
                    return newPattern;
                });
        pattern.addOccurrence(entry);
    }

    public List<PatternEntry> getPatterns() { return new ArrayList<>(patterns); }

    public static class PatternEntry {
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

        public String getCategory() { return category; }
        public int getOccurrenceCount() { return occurrenceCount; }
        public double getAverageImportance() { return averageImportance; }
        public Instant getLastOccurrence() { return lastOccurrence; }
    }
}


