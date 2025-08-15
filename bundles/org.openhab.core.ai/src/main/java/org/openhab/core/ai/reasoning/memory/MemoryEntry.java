package org.openhab.core.ai.reasoning.memory;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Memory entry for storing information.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MemoryEntry {
    private final String id;
    private final String content;
    private final String category;
    private final double importance;
    private final double relevance;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    private int accessCount;

    public MemoryEntry(String id, String content, String category, double importance, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.importance = importance;
        this.relevance = 1.0;
        this.timestamp = Instant.now();
        this.metadata = metadata;
        this.accessCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        accessCount++;
        return content;
    }

    public String getCategory() {
        return category;
    }

    public double getImportance() {
        return importance;
    }

    public double getRelevance() {
        return relevance;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public int getAccessCount() {
        return accessCount;
    }
}
