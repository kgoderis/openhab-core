package org.openhab.core.ai.reasoning.learning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LearningHistory {
    private final String agentId;
    private final List<LearningEntry> entries = new ArrayList<>();

    public LearningHistory(String agentId) {
        this.agentId = agentId;
    }

    public void addEntry(String interaction, String result, boolean success, Map<String, Object> metadata) {
        entries.add(new LearningEntry(interaction, result, success, metadata, Instant.now()));
    }

    public String getAgentId() {
        return agentId;
    }

    public List<LearningEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public List<LearningEntry> getEntriesByCategory(String category) {
        return entries.stream().filter(entry -> category.equals(entry.getMetadata().get("category"))).toList();
    }

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.LearningEntry
}
