package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemorySearchResult {
    private final String memoryId;
    private final String content;
    private final double relevance;
    private final long timestamp;

    public MemorySearchResult(String memoryId, String content, double relevance, long timestamp) {
        this.memoryId = memoryId;
        this.content = content;
        this.relevance = relevance;
        this.timestamp = timestamp;
    }

    public String getMemoryId() { return memoryId; }
    public String getContent() { return content; }
    public double getRelevance() { return relevance; }
    public long getTimestamp() { return timestamp; }
}


