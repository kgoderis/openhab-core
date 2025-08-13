package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ShortTermMemory {
    private final String agentId;
    private final List<MemoryEntry> entries = new ArrayList<>();

    public ShortTermMemory(String agentId) { this.agentId = agentId; }

    public void addEntry(MemoryEntry entry) {
        entries.add(0, entry);
        if (entries.size() > 1000) { entries.remove(entries.size() - 1); }
    }

    public List<MemoryEntry> getEntries(@Nullable String category, int limit) {
        List<MemoryEntry> filtered = entries;
        if (category != null) {
            filtered = entries.stream().filter(entry -> category.equals(entry.getCategory())).toList();
        }
        return filtered.stream().limit(limit).toList();
    }

    public List<MemoryEntry> getEntriesForConsolidation() {
        return entries.stream().filter(entry -> entry.getAccessCount() > 2).toList();
    }

    public void removeConsolidatedEntries(List<MemoryEntry> consolidated) { entries.removeAll(consolidated); }

    public void cleanupOldEntries(Duration retention) {
        Instant cutoff = Instant.now().minus(retention);
        entries.removeIf(entry -> entry.getTimestamp().isBefore(cutoff));
    }

    public List<MemoryEntry> searchEntries(String query, int limit) {
        return entries.stream().filter(entry -> entry.getContent().toLowerCase().contains(query.toLowerCase()))
                .limit(limit).toList();
    }
}


