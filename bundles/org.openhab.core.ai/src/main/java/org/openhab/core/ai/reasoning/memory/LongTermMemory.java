package org.openhab.core.ai.reasoning.memory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class LongTermMemory {
    private final String agentId;
    private final List<MemoryEntry> entries = new ArrayList<>();

    public LongTermMemory(String agentId) {
        this.agentId = agentId;
    }

    public void addEntry(MemoryEntry entry) {
        entries.add(entry);
        if (entries.size() > 10000) {
            entries.remove(0);
        }
    }

    public List<MemoryEntry> getEntries(@Nullable String category, int limit) {
        List<MemoryEntry> filtered = entries;
        if (category != null) {
            filtered = entries.stream().filter(entry -> category.equals(entry.getCategory())).toList();
        }
        return filtered.stream().sorted((a, b) -> Double.compare(b.getImportance(), a.getImportance())).limit(limit)
                .toList();
    }

    public List<MemoryEntry> searchEntries(String query, int limit) {
        return entries.stream().filter(entry -> entry.getContent().toLowerCase().contains(query.toLowerCase()))
                .sorted((a, b) -> Double.compare(b.getImportance(), a.getImportance())).limit(limit).toList();
    }
}
