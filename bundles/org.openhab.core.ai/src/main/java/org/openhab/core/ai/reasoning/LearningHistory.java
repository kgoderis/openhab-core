package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LearningHistory {
    private final String agentId;
    private final List<LearningEntry> entries = new ArrayList<>();

    public LearningHistory(String agentId) { this.agentId = agentId; }

    public void addEntry(String interaction, String result, boolean success, Map<String, Object> metadata) {
        entries.add(new LearningEntry(interaction, result, success, metadata, Instant.now()));
    }

    public String getAgentId() { return agentId; }
    public List<LearningEntry> getEntries() { return new ArrayList<>(entries); }
    public List<LearningEntry> getEntriesByCategory(String category) {
        return entries.stream().filter(entry -> category.equals(entry.getMetadata().get("category"))).toList();
    }

    public static class LearningEntry {
        private final String interaction;
        private final String result;
        private final boolean success;
        private final Map<String, Object> metadata;
        private final Instant timestamp;

        public LearningEntry(String interaction, String result, boolean success, Map<String, Object> metadata,
                Instant timestamp) {
            this.interaction = interaction;
            this.result = result;
            this.success = success;
            this.metadata = metadata;
            this.timestamp = timestamp;
        }

        public String getInteraction() { return interaction; }
        public String getResult() { return result; }
        public boolean isSuccess() { return success; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getTimestamp() { return timestamp; }
    }
}


