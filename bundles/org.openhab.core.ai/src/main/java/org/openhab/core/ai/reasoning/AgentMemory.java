package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Memory System - Manages short-term and long-term memory for agents
 * 
 * <p>
 * This system provides:
 * - Short-term memory for recent events and interactions
 * - Long-term memory for patterns and preferences
 * - Memory consolidation and learning mechanisms
 * - Memory retrieval and search capabilities
 * - Memory capacity management and optimization
 * - Memory backup and persistence
 * - Memory analytics and insights
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentMemory.class)
@NonNullByDefault
public class AgentMemory {

    private static final Logger logger = LoggerFactory.getLogger(AgentMemory.class);

    // Memory storage
    private final Map<String, ShortTermMemory> shortTermMemories = new ConcurrentHashMap<>();
    private final Map<String, LongTermMemory> longTermMemories = new ConcurrentHashMap<>();
    private final Map<String, MemoryPattern> memoryPatterns = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalMemoryStores = new AtomicLong(0);
    private final AtomicLong totalMemoryRetrievals = new AtomicLong(0);
    private final AtomicLong totalMemoryConsolidations = new AtomicLong(0);
    private final AtomicLong totalPatternRecognitions = new AtomicLong(0);

    // Thread safety
    private final ReadWriteLock shortTermLock = new ReentrantReadWriteLock();
    private final ReadWriteLock longTermLock = new ReentrantReadWriteLock();
    private final ReadWriteLock patternLock = new ReentrantReadWriteLock();

    // Configuration
    private int maxShortTermEntries = 1000;
    private int maxLongTermEntries = 10000;
    private Duration shortTermRetention = Duration.ofHours(24);
    private Duration consolidationInterval = Duration.ofMinutes(30);
    private boolean enableLearning = true;
    private boolean enablePatternRecognition = true;

    @Activate
    public void activate() {
        logger.debug("Agent Memory System activated");
        initializeMemory();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Agent Memory System deactivated");
        cleanupMemory();
    }

    /**
     * Store a memory entry in short-term memory
     */
    public MemoryStoreResult storeShortTermMemory(String agentId, MemoryEntry entry) {
        try {
            shortTermLock.writeLock().lock();

            ShortTermMemory memory = shortTermMemories.computeIfAbsent(agentId, k -> new ShortTermMemory(agentId));
            memory.addEntry(entry);

            // Clean up old entries
            memory.cleanupOldEntries(shortTermRetention);

            totalMemoryStores.incrementAndGet();
            logger.debug("Stored short-term memory for agent: {}", agentId);

            return MemoryStoreResult.success(entry);
        } finally {
            shortTermLock.writeLock().unlock();
        }
    }

    /**
     * Store a memory entry in long-term memory
     */
    public MemoryStoreResult storeLongTermMemory(String agentId, MemoryEntry entry) {
        try {
            longTermLock.writeLock().lock();

            LongTermMemory memory = longTermMemories.computeIfAbsent(agentId, k -> new LongTermMemory(agentId));
            memory.addEntry(entry);

            // Trigger pattern recognition
            if (enablePatternRecognition) {
                recognizePatterns(agentId, entry);
            }

            totalMemoryStores.incrementAndGet();
            logger.debug("Stored long-term memory for agent: {}", agentId);

            return MemoryStoreResult.success(entry);
        } finally {
            longTermLock.writeLock().unlock();
        }
    }

    /**
     * Retrieve memories from short-term memory
     */
    public List<MemoryEntry> retrieveShortTermMemory(String agentId, @Nullable String category, int limit) {
        try {
            shortTermLock.readLock().lock();

            ShortTermMemory memory = shortTermMemories.get(agentId);
            if (memory == null) {
                return Collections.emptyList();
            }

            totalMemoryRetrievals.incrementAndGet();
            return memory.getEntries(category, limit);
        } finally {
            shortTermLock.readLock().unlock();
        }
    }

    /**
     * Retrieve memories from long-term memory
     */
    public List<MemoryEntry> retrieveLongTermMemory(String agentId, @Nullable String category, int limit) {
        try {
            longTermLock.readLock().lock();

            LongTermMemory memory = longTermMemories.get(agentId);
            if (memory == null) {
                return Collections.emptyList();
            }

            totalMemoryRetrievals.incrementAndGet();
            return memory.getEntries(category, limit);
        } finally {
            longTermLock.readLock().unlock();
        }
    }

    /**
     * Search memories across both short-term and long-term
     */
    public List<MemoryEntry> searchMemories(String agentId, String query, int limit) {
        List<MemoryEntry> results = new ArrayList<>();

        // Search short-term memory
        results.addAll(searchShortTermMemory(agentId, query, limit / 2));

        // Search long-term memory
        results.addAll(searchLongTermMemory(agentId, query, limit / 2));

        // Sort by relevance and timestamp
        results.sort((a, b) -> {
            int relevanceCompare = Double.compare(b.getRelevance(), a.getRelevance());
            if (relevanceCompare != 0) {
                return relevanceCompare;
            }
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        // Limit results
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        totalMemoryRetrievals.incrementAndGet();
        return results;
    }

    /**
     * Consolidate short-term memories into long-term memory
     */
    public MemoryConsolidationResult consolidateMemories(String agentId) {
        try {
            shortTermLock.readLock().lock();
            longTermLock.writeLock().lock();

            ShortTermMemory shortTerm = shortTermMemories.get(agentId);
            if (shortTerm == null) {
                return MemoryConsolidationResult.noData("No short-term memory found for agent: " + agentId);
            }

            LongTermMemory longTerm = longTermMemories.computeIfAbsent(agentId, k -> new LongTermMemory(agentId));

            List<MemoryEntry> entriesToConsolidate = shortTerm.getEntriesForConsolidation();
            int consolidatedCount = 0;

            for (MemoryEntry entry : entriesToConsolidate) {
                if (shouldConsolidate(entry)) {
                    longTerm.addEntry(entry);
                    consolidatedCount++;
                }
            }

            // Remove consolidated entries from short-term memory
            shortTerm.removeConsolidatedEntries(entriesToConsolidate);

            totalMemoryConsolidations.incrementAndGet();
            logger.debug("Consolidated {} memories for agent: {}", consolidatedCount, agentId);

            return MemoryConsolidationResult.success(consolidatedCount);
        } finally {
            shortTermLock.readLock().unlock();
            longTermLock.writeLock().unlock();
        }
    }

    /**
     * Get memory patterns for an agent
     */
    public List<MemoryPattern.PatternEntry> getMemoryPatterns(String agentId) {
        try {
            patternLock.readLock().lock();

            MemoryPattern pattern = memoryPatterns.get(agentId);
            if (pattern == null) {
                return Collections.emptyList();
            }

            return pattern.getPatterns();
        } finally {
            patternLock.readLock().unlock();
        }
    }

    /**
     * Get memory performance metrics
     */
    public MemoryPerformanceMetrics getPerformanceMetrics() {
        return MemoryPerformanceMetrics.builder().totalStores(totalMemoryStores.get())
                .totalRetrievals(totalMemoryRetrievals.get()).totalConsolidations(totalMemoryConsolidations.get())
                .totalPatternRecognitions(totalPatternRecognitions.get()).shortTermMemoryCount(shortTermMemories.size())
                .longTermMemoryCount(longTermMemories.size()).patternCount(memoryPatterns.size()).build();
    }

    // Private helper methods

    private void initializeMemory() {
        logger.debug("Initializing agent memory system");
        // TODO: Load persistent memory if available
    }

    private void cleanupMemory() {
        logger.debug("Cleaning up agent memory system");
        // TODO: Save persistent memory
    }

    private void recognizePatterns(String agentId, MemoryEntry entry) {
        try {
            patternLock.writeLock().lock();

            MemoryPattern pattern = memoryPatterns.computeIfAbsent(agentId, k -> new MemoryPattern(agentId));
            pattern.analyzeEntry(entry);

            totalPatternRecognitions.incrementAndGet();
        } finally {
            patternLock.writeLock().unlock();
        }
    }

    private List<MemoryEntry> searchShortTermMemory(String agentId, String query, int limit) {
        ShortTermMemory memory = shortTermMemories.get(agentId);
        if (memory == null) {
            return Collections.emptyList();
        }

        return memory.searchEntries(query, limit);
    }

    private List<MemoryEntry> searchLongTermMemory(String agentId, String query, int limit) {
        LongTermMemory memory = longTermMemories.get(agentId);
        if (memory == null) {
            return Collections.emptyList();
        }

        return memory.searchEntries(query, limit);
    }

    private boolean shouldConsolidate(MemoryEntry entry) {
        // Basic consolidation logic - can be enhanced with ML
        return entry.getImportance() > 0.5 && entry.getAccessCount() > 2;
    }

    // Inner classes

    public static class MemoryEntry {
        private final String id;
        private final String content;
        private final String category;
        private final double importance;
        private final double relevance;
        private final Instant timestamp;
        private final Map<String, Object> metadata;
        private int accessCount;

        public MemoryEntry(String id, String content, String category, double importance,
                Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.category = category;
            this.importance = importance;
            this.relevance = 1.0; // Default relevance
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

    public static class ShortTermMemory {
        private final String agentId;
        private final List<MemoryEntry> entries = new ArrayList<>();

        public ShortTermMemory(String agentId) {
            this.agentId = agentId;
        }

        public void addEntry(MemoryEntry entry) {
            entries.add(entry);

            // Limit size
            if (entries.size() > 1000) {
                entries.remove(0);
            }
        }

        public List<MemoryEntry> getEntries(@Nullable String category, int limit) {
            List<MemoryEntry> filtered = entries;
            if (category != null) {
                filtered = entries.stream().filter(e -> category.equals(e.getCategory())).toList();
            }

            // Sort by timestamp (newest first)
            filtered = new ArrayList<>(filtered);
            filtered.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            if (filtered.size() > limit) {
                filtered = filtered.subList(0, limit);
            }

            return filtered;
        }

        public List<MemoryEntry> getEntriesForConsolidation() {
            return new ArrayList<>(entries);
        }

        public void removeConsolidatedEntries(List<MemoryEntry> consolidated) {
            entries.removeAll(consolidated);
        }

        public void cleanupOldEntries(Duration retention) {
            Instant cutoff = Instant.now().minus(retention);
            entries.removeIf(entry -> entry.getTimestamp().isBefore(cutoff));
        }

        public List<MemoryEntry> searchEntries(String query, int limit) {
            // Simple search implementation - can be enhanced with semantic search
            return entries.stream().filter(entry -> entry.getContent().toLowerCase().contains(query.toLowerCase()))
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(limit).toList();
        }
    }

    public static class LongTermMemory {
        private final String agentId;
        private final List<MemoryEntry> entries = new ArrayList<>();

        public LongTermMemory(String agentId) {
            this.agentId = agentId;
        }

        public void addEntry(MemoryEntry entry) {
            entries.add(entry);

            // Limit size
            if (entries.size() > 10000) {
                entries.remove(0);
            }
        }

        public List<MemoryEntry> getEntries(@Nullable String category, int limit) {
            List<MemoryEntry> filtered = entries;
            if (category != null) {
                filtered = entries.stream().filter(e -> category.equals(e.getCategory())).toList();
            }

            // Sort by importance and timestamp
            filtered = new ArrayList<>(filtered);
            filtered.sort((a, b) -> {
                int importanceCompare = Double.compare(b.getImportance(), a.getImportance());
                if (importanceCompare != 0) {
                    return importanceCompare;
                }
                return b.getTimestamp().compareTo(a.getTimestamp());
            });

            if (filtered.size() > limit) {
                filtered = filtered.subList(0, limit);
            }

            return filtered;
        }

        public List<MemoryEntry> searchEntries(String query, int limit) {
            // Simple search implementation - can be enhanced with semantic search
            return entries.stream().filter(entry -> entry.getContent().toLowerCase().contains(query.toLowerCase()))
                    .sorted((a, b) -> {
                        int importanceCompare = Double.compare(b.getImportance(), a.getImportance());
                        if (importanceCompare != 0) {
                            return importanceCompare;
                        }
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    }).limit(limit).toList();
        }
    }

    public static class MemoryPattern {
        private final String agentId;
        private final List<PatternEntry> patterns = new ArrayList<>();

        public MemoryPattern(String agentId) {
            this.agentId = agentId;
        }

        public void analyzeEntry(MemoryEntry entry) {
            // Simple pattern analysis - can be enhanced with ML
            String category = entry.getCategory();
            PatternEntry pattern = patterns.stream().filter(p -> category.equals(p.getCategory())).findFirst()
                    .orElse(null);

            if (pattern == null) {
                pattern = new PatternEntry(category);
                patterns.add(pattern);
            }

            pattern.addOccurrence(entry);
        }

        public List<PatternEntry> getPatterns() {
            return new ArrayList<>(patterns);
        }

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
                averageImportance = ((averageImportance * (occurrenceCount - 1)) + entry.getImportance())
                        / occurrenceCount;
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
    }

    // Result classes

    public static class MemoryStoreResult {
        private final boolean success;
        private final @Nullable MemoryEntry entry;
        private final @Nullable String error;

        private MemoryStoreResult(boolean success, @Nullable MemoryEntry entry, @Nullable String error) {
            this.success = success;
            this.entry = entry;
            this.error = error;
        }

        public static MemoryStoreResult success(MemoryEntry entry) {
            return new MemoryStoreResult(true, entry, null);
        }

        public static MemoryStoreResult error(String error) {
            return new MemoryStoreResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable MemoryEntry getEntry() {
            return entry;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class MemoryConsolidationResult {
        private final boolean success;
        private final int consolidatedCount;
        private final @Nullable String error;

        private MemoryConsolidationResult(boolean success, int consolidatedCount, @Nullable String error) {
            this.success = success;
            this.consolidatedCount = consolidatedCount;
            this.error = error;
        }

        public static MemoryConsolidationResult success(int consolidatedCount) {
            return new MemoryConsolidationResult(true, consolidatedCount, null);
        }

        public static MemoryConsolidationResult noData(String error) {
            return new MemoryConsolidationResult(false, 0, error);
        }

        public static MemoryConsolidationResult error(String error) {
            return new MemoryConsolidationResult(false, 0, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public int getConsolidatedCount() {
            return consolidatedCount;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class MemoryPerformanceMetrics {
        private final long totalStores;
        private final long totalRetrievals;
        private final long totalConsolidations;
        private final long totalPatternRecognitions;
        private final int shortTermMemoryCount;
        private final int longTermMemoryCount;
        private final int patternCount;

        private MemoryPerformanceMetrics(Builder builder) {
            this.totalStores = builder.totalStores;
            this.totalRetrievals = builder.totalRetrievals;
            this.totalConsolidations = builder.totalConsolidations;
            this.totalPatternRecognitions = builder.totalPatternRecognitions;
            this.shortTermMemoryCount = builder.shortTermMemoryCount;
            this.longTermMemoryCount = builder.longTermMemoryCount;
            this.patternCount = builder.patternCount;
        }

        public long getTotalStores() {
            return totalStores;
        }

        public long getTotalRetrievals() {
            return totalRetrievals;
        }

        public long getTotalConsolidations() {
            return totalConsolidations;
        }

        public long getTotalPatternRecognitions() {
            return totalPatternRecognitions;
        }

        public int getShortTermMemoryCount() {
            return shortTermMemoryCount;
        }

        public int getLongTermMemoryCount() {
            return longTermMemoryCount;
        }

        public int getPatternCount() {
            return patternCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalStores;
            private long totalRetrievals;
            private long totalConsolidations;
            private long totalPatternRecognitions;
            private int shortTermMemoryCount;
            private int longTermMemoryCount;
            private int patternCount;

            public Builder totalStores(long totalStores) {
                this.totalStores = totalStores;
                return this;
            }

            public Builder totalRetrievals(long totalRetrievals) {
                this.totalRetrievals = totalRetrievals;
                return this;
            }

            public Builder totalConsolidations(long totalConsolidations) {
                this.totalConsolidations = totalConsolidations;
                return this;
            }

            public Builder totalPatternRecognitions(long totalPatternRecognitions) {
                this.totalPatternRecognitions = totalPatternRecognitions;
                return this;
            }

            public Builder shortTermMemoryCount(int shortTermMemoryCount) {
                this.shortTermMemoryCount = shortTermMemoryCount;
                return this;
            }

            public Builder longTermMemoryCount(int longTermMemoryCount) {
                this.longTermMemoryCount = longTermMemoryCount;
                return this;
            }

            public Builder patternCount(int patternCount) {
                this.patternCount = patternCount;
                return this;
            }

            public MemoryPerformanceMetrics build() {
                return new MemoryPerformanceMetrics(this);
            }
        }
    }
}
