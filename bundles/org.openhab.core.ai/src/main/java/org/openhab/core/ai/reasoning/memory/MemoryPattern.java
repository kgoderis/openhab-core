package org.openhab.core.ai.reasoning.memory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.patterns.PatternEntry;

@NonNullByDefault
public class MemoryPattern {
    private final String agentId;
    private final List<PatternEntry> patterns = new ArrayList<>();

    public MemoryPattern(String agentId) {
        this.agentId = agentId;
    }

    public void analyzeEntry(MemoryEntry entry) {
        PatternEntry pattern = patterns.stream().filter(p -> p.getCategory().equals(entry.getCategory())).findFirst()
                .orElseGet(() -> {
                    PatternEntry newPattern = new PatternEntry(entry.getCategory());
                    patterns.add(newPattern);
                    return newPattern;
                });
        pattern.addOccurrence(entry);
    }

    public List<PatternEntry> getPatterns() {
        return new ArrayList<>(patterns);
    }

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.PatternEntry
}
