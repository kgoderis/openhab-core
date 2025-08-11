package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.api.MemoryManager;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified Agent Memory System - Manages all memory for agents including reasoning sessions, context, and learning
 * 
 * <p>
 * This system provides:
 * - Short-term memory for recent events and interactions
 * - Long-term memory for patterns and preferences
 * - Reasoning session management and context storage
 * - Learning history and pattern recognition
 * - Memory consolidation and optimization
 * - Memory retrieval and search capabilities
 * - Memory capacity management and cleanup
 * - Memory analytics and insights
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = { AgentMemory.class, MemoryManager.class })
@NonNullByDefault
public class AgentMemory implements MemoryManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentMemory.class);

    // Memory storage
    private final Map<String, ShortTermMemory> shortTermMemories = new ConcurrentHashMap<>();
    private final Map<String, LongTermMemory> longTermMemories = new ConcurrentHashMap<>();
    private final Map<String, MemoryPattern> memoryPatterns = new ConcurrentHashMap<>();

    // Reasoning session management (UNIFIED MEMORY ARCHITECTURE)
    private final Map<String, ReasoningSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, SessionContext> sessionContexts = new ConcurrentHashMap<>();
    private final Map<String, LearningHistory> learningHistory = new ConcurrentHashMap<>();

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
        logger.debug("Unified Agent Memory System activated");
        initializeMemory();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Unified Agent Memory System deactivated");
        cleanupMemory();
    }

    // ===== EXISTING MEMORY METHODS =====

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

            // Recognize patterns if enabled
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
     * Retrieve short-term memory entries
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
     * Retrieve long-term memory entries
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
    public List<MemoryEntry> searchMemoriesInternal(String agentId, String query, int limit) {
        List<MemoryEntry> results = new ArrayList<>();

        // Search short-term memory
        results.addAll(searchShortTermMemory(agentId, query, limit));

        // Search long-term memory
        results.addAll(searchLongTermMemory(agentId, query, limit));

        // Sort by relevance and limit
        results.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    /**
     * Consolidate memories from short-term to long-term
     */
    public MemoryConsolidationResult consolidateMemoriesInternal(String agentId) {
        try {
            shortTermLock.writeLock().lock();
            longTermLock.writeLock().lock();

            ShortTermMemory shortTerm = shortTermMemories.get(agentId);
            if (shortTerm == null) {
                return MemoryConsolidationResult.noData("No short-term memory found for agent: " + agentId);
            }

            List<MemoryEntry> entriesForConsolidation = shortTerm.getEntriesForConsolidation();
            if (entriesForConsolidation.isEmpty()) {
                return MemoryConsolidationResult.noData("No entries ready for consolidation");
            }

            LongTermMemory longTerm = longTermMemories.computeIfAbsent(agentId, k -> new LongTermMemory(agentId));

            int consolidatedCount = 0;
            for (MemoryEntry entry : entriesForConsolidation) {
                if (shouldConsolidate(entry)) {
                    longTerm.addEntry(entry);
                    consolidatedCount++;
                }
            }

            // Remove consolidated entries from short-term memory
            shortTerm.removeConsolidatedEntries(entriesForConsolidation);

            totalMemoryConsolidations.incrementAndGet();
            logger.debug("Consolidated {} memories for agent: {}", consolidatedCount, agentId);

            return MemoryConsolidationResult.success(consolidatedCount);
        } finally {
            longTermLock.writeLock().unlock();
            shortTermLock.writeLock().unlock();
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

    // ===== UNIFIED MEMORY ARCHITECTURE - NEW METHODS =====

    /**
     * Store a reasoning session for an agent
     */
    public ReasoningSessionResult storeReasoningSession(String agentId, String sessionId, ReasoningContext context) {
        try {
            ReasoningSession session = new ReasoningSession(sessionId, agentId, context);
            activeSessions.put(sessionId, session);

            // Also store in short-term memory for quick access
            MemoryEntry sessionEntry = new MemoryEntry(sessionId, "Reasoning session: " + context.getCurrentContext(),
                    "reasoning_session", 0.8, Map.of("sessionId", sessionId, "agentId", agentId));
            storeShortTermMemory(agentId, sessionEntry);

            logger.debug("Stored reasoning session {} for agent {}", sessionId, agentId);
            return ReasoningSessionResult.success(session);
        } catch (Exception e) {
            logger.error("Failed to store reasoning session {} for agent {}", sessionId, agentId, e);
            return ReasoningSessionResult.error("Failed to store session: " + e.getMessage());
        }
    }

    /**
     * Retrieve a reasoning session for an agent
     */
    public ReasoningSession retrieveReasoningSession(String agentId, String sessionId) {
        ReasoningSession session = activeSessions.get(sessionId);
        if (session != null && session.getAgentId().equals(agentId)) {
            return session;
        }
        return null;
    }

    /**
     * Update a reasoning session with new interaction
     */
    public void updateReasoningSession(String agentId, String sessionId, String input, String output,
            Map<String, Object> metadata) {
        ReasoningSession session = activeSessions.get(sessionId);
        if (session != null && session.getAgentId().equals(agentId)) {
            session.addInteraction(input, output, metadata);
            logger.debug("Updated reasoning session {} for agent {}", sessionId, agentId);
        }
    }

    /**
     * Store session context for reasoning sessions
     */
    public void storeSessionContext(String agentId, String sessionId, Map<String, Object> contextData) {
        String key = agentId + ":" + sessionId;
        SessionContext context = new SessionContext(sessionId, agentId, contextData);
        sessionContexts.put(key, context);
        logger.debug("Stored session context for agent {} session {}", agentId, sessionId);
    }

    /**
     * Get session context for reasoning sessions
     */
    public Map<String, Object> getSessionContext(String agentId, String sessionId) {
        String key = agentId + ":" + sessionId;
        SessionContext context = sessionContexts.get(key);
        if (context != null) {
            return context.getContextData();
        }
        return Map.of();
    }

    /**
     * Store learning history for an agent
     */
    public void storeLearning(String agentId, String interaction, String result, boolean success,
            Map<String, Object> metadata) {
        LearningHistory history = learningHistory.computeIfAbsent(agentId, k -> new LearningHistory(agentId));
        history.addEntry(interaction, result, success, metadata);

        // Also store in long-term memory for pattern recognition
        MemoryEntry learningEntry = new MemoryEntry("learning-" + System.currentTimeMillis(),
                "Learning: " + interaction + " -> " + result, "learning", success ? 0.9 : 0.3, metadata);
        storeLongTermMemory(agentId, learningEntry);

        logger.debug("Stored learning for agent {}: {}", agentId, interaction);
    }

    /**
     * Get learning history for an agent
     */
    public List<LearningHistory.LearningEntry> getLearningHistory(String agentId, String category) {
        LearningHistory history = learningHistory.get(agentId);
        if (history != null) {
            return history.getEntriesByCategory(category);
        }
        return List.of();
    }

    /**
     * Get all active sessions for an agent
     */
    public List<ReasoningSession> getActiveSessions(String agentId) {
        return activeSessions.values().stream().filter(session -> session.getAgentId().equals(agentId)).toList();
    }

    /**
     * Clean up old sessions
     */
    public void cleanupOldSessions(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        activeSessions.entrySet().removeIf(entry -> entry.getValue().getLastActivityAt().isBefore(cutoff));
        logger.debug("Cleaned up old sessions, remaining: {}", activeSessions.size());
    }

    // ===== PRIVATE HELPER METHODS =====

    private void initializeMemory() {
        logger.debug("Initializing unified agent memory system");
        // TODO: Load persistent memory if available
    }

    private void cleanupMemory() {
        logger.debug("Cleaning up unified agent memory system");
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

    // ===== INNER CLASSES =====

    /**
     * Memory entry for storing information
     */
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

    /**
     * Short-term memory management
     */
    public static class ShortTermMemory {
        private final String agentId;
        private final List<MemoryEntry> entries = new ArrayList<>();

        public ShortTermMemory(String agentId) {
            this.agentId = agentId;
        }

        public void addEntry(MemoryEntry entry) {
            entries.add(0, entry); // Add to beginning for LIFO behavior
            if (entries.size() > 1000) { // Limit size
                entries.remove(entries.size() - 1);
            }
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

        public void removeConsolidatedEntries(List<MemoryEntry> consolidated) {
            entries.removeAll(consolidated);
        }

        public void cleanupOldEntries(Duration retention) {
            Instant cutoff = Instant.now().minus(retention);
            entries.removeIf(entry -> entry.getTimestamp().isBefore(cutoff));
        }

        public List<MemoryEntry> searchEntries(String query, int limit) {
            return entries.stream().filter(entry -> entry.getContent().toLowerCase().contains(query.toLowerCase()))
                    .limit(limit).toList();
        }
    }

    /**
     * Long-term memory management
     */
    public static class LongTermMemory {
        private final String agentId;
        private final List<MemoryEntry> entries = new ArrayList<>();

        public LongTermMemory(String agentId) {
            this.agentId = agentId;
        }

        public void addEntry(MemoryEntry entry) {
            entries.add(entry);
            if (entries.size() > 10000) { // Limit size
                entries.remove(0); // Remove oldest
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

    /**
     * Memory pattern recognition
     */
    public static class MemoryPattern {
        private final String agentId;
        private final List<PatternEntry> patterns = new ArrayList<>();

        public MemoryPattern(String agentId) {
            this.agentId = agentId;
        }

        public void analyzeEntry(MemoryEntry entry) {
            PatternEntry pattern = patterns.stream().filter(p -> p.getCategory().equals(entry.getCategory()))
                    .findFirst().orElseGet(() -> {
                        PatternEntry newPattern = new PatternEntry(entry.getCategory());
                        patterns.add(newPattern);
                        return newPattern;
                    });
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

    /**
     * Reasoning session management for agents
     */
    public static class ReasoningSession {
        private final String sessionId;
        private final String agentId;
        private final ReasoningContext initialContext;
        private final Instant createdAt;
        private Instant lastActivityAt;
        private final List<SessionInteraction> interactions = new ArrayList<>();

        public ReasoningSession(String sessionId, String agentId, ReasoningContext initialContext) {
            this.sessionId = sessionId;
            this.agentId = agentId;
            this.initialContext = initialContext;
            this.createdAt = Instant.now();
            this.lastActivityAt = Instant.now();
        }

        public void addInteraction(String input, String output, Map<String, Object> metadata) {
            interactions.add(new SessionInteraction(input, output, metadata, Instant.now()));
            lastActivityAt = Instant.now();
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getAgentId() {
            return agentId;
        }

        public ReasoningContext getInitialContext() {
            return initialContext;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getLastActivityAt() {
            return lastActivityAt;
        }

        public List<SessionInteraction> getInteractions() {
            return new ArrayList<>(interactions);
        }

        public static class SessionInteraction {
            private final String input;
            private final String output;
            private final Map<String, Object> metadata;
            private final Instant timestamp;

            public SessionInteraction(String input, String output, Map<String, Object> metadata, Instant timestamp) {
                this.input = input;
                this.output = output;
                this.metadata = metadata;
                this.timestamp = timestamp;
            }

            public String getInput() {
                return input;
            }

            public String getOutput() {
                return output;
            }

            public Map<String, Object> getMetadata() {
                return metadata;
            }

            public Instant getTimestamp() {
                return timestamp;
            }
        }
    }

    /**
     * Session context management for reasoning sessions
     */
    public static class SessionContext {
        private final String sessionId;
        private final String agentId;
        private final Map<String, Object> contextData;
        private final Instant createdAt;
        private Instant lastUpdatedAt;

        public SessionContext(String sessionId, String agentId, Map<String, Object> contextData) {
            this.sessionId = sessionId;
            this.agentId = agentId;
            this.contextData = new ConcurrentHashMap<>(contextData);
            this.createdAt = Instant.now();
            this.lastUpdatedAt = Instant.now();
        }

        public void updateContext(String key, Object value) {
            contextData.put(key, value);
            lastUpdatedAt = Instant.now();
        }

        public void updateContext(Map<String, Object> updates) {
            contextData.putAll(updates);
            lastUpdatedAt = Instant.now();
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getAgentId() {
            return agentId;
        }

        public Map<String, Object> getContextData() {
            return new ConcurrentHashMap<>(contextData);
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getLastUpdatedAt() {
            return lastUpdatedAt;
        }
    }

    /**
     * Learning history management for agents
     */
    public static class LearningHistory {
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

            public String getInteraction() {
                return interaction;
            }

            public String getResult() {
                return result;
            }

            public boolean isSuccess() {
                return success;
            }

            public Map<String, Object> getMetadata() {
                return metadata;
            }

            public Instant getTimestamp() {
                return timestamp;
            }
        }
    }

    /**
     * Result for reasoning session operations
     */
    public static class ReasoningSessionResult {
        private final boolean success;
        private final @Nullable ReasoningSession session;
        private final @Nullable String error;

        private ReasoningSessionResult(boolean success, @Nullable ReasoningSession session, @Nullable String error) {
            this.success = success;
            this.session = session;
            this.error = error;
        }

        public static ReasoningSessionResult success(ReasoningSession session) {
            return new ReasoningSessionResult(true, session, null);
        }

        public static ReasoningSessionResult error(String error) {
            return new ReasoningSessionResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable ReasoningSession getSession() {
            return session;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    /**
     * Memory store result
     */
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

    /**
     * Memory consolidation result
     */
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

    /**
     * Memory performance metrics
     */
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

    // MemoryManager interface implementation
    @Override
    public CompletableFuture<MemoryManager.MemoryStoreResult> storeShortTermMemory(String agentId, String memory,
            @Nullable Map<String, Object> metadata) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryEntry entry = new MemoryEntry(generateMemoryId(), memory, "general", 0.5,
                        metadata != null ? metadata : new ConcurrentHashMap<>());
                MemoryStoreResult result = storeShortTermMemory(agentId, entry);
                if (result.isSuccess()) {
                    return new MemoryManager.MemoryStoreResult(true, result.getEntry().getId(), null);
                } else {
                    return new MemoryManager.MemoryStoreResult(false, null, result.getError());
                }
            } catch (Exception e) {
                logger.error("Error storing short-term memory for agent: {}", agentId, e);
                return new MemoryManager.MemoryStoreResult(false, null, e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<MemoryManager.MemoryStoreResult> storeLongTermMemory(String agentId, String memory,
            @Nullable Map<String, Object> metadata) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryEntry entry = new MemoryEntry(generateMemoryId(), memory, "general", 0.5,
                        metadata != null ? metadata : new ConcurrentHashMap<>());
                MemoryStoreResult result = storeLongTermMemory(agentId, entry);
                if (result.isSuccess()) {
                    return new MemoryManager.MemoryStoreResult(true, result.getEntry().getId(), null);
                } else {
                    return new MemoryManager.MemoryStoreResult(false, null, result.getError());
                }
            } catch (Exception e) {
                logger.error("Error storing long-term memory for agent: {}", agentId, e);
                return new MemoryManager.MemoryStoreResult(false, null, e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<List<MemoryManager.MemorySearchResult>> searchMemories(String agentId, String query,
            int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MemoryEntry> results = searchMemoriesInternal(agentId, query, limit);
                List<MemoryManager.MemorySearchResult> searchResults = new ArrayList<>();
                for (MemoryEntry entry : results) {
                    searchResults.add(new MemoryManager.MemorySearchResult(entry.getId(), entry.getContent(),
                            entry.getRelevance(), entry.getTimestamp().toEpochMilli()));
                }
                return searchResults;
            } catch (Exception e) {
                logger.error("Error searching memories for agent: {}", agentId, e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<MemoryManager.MemoryConsolidationResult> consolidateMemories(String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryConsolidationResult result = consolidateMemoriesInternal(agentId);
                return new MemoryManager.MemoryConsolidationResult(result.isSuccess(), result.getConsolidatedCount(),
                        result.getError());
            } catch (Exception e) {
                logger.error("Error consolidating memories for agent: {}", agentId, e);
                return new MemoryManager.MemoryConsolidationResult(false, 0, e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<MemoryManager.MemoryPerformanceMetrics> getPerformanceMetrics(String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryPerformanceMetrics metrics = getPerformanceMetrics();
                return new MemoryManager.MemoryPerformanceMetrics(
                        metrics.getTotalStores() + metrics.getTotalRetrievals(), metrics.getShortTermMemoryCount(),
                        metrics.getLongTermMemoryCount(), 0.0, // averageSearchTime - not tracked in current
                                                               // implementation
                        0.0 // averageStorageTime - not tracked in current implementation
                );
            } catch (Exception e) {
                logger.error("Error getting performance metrics for agent: {}", agentId, e);
                return new MemoryManager.MemoryPerformanceMetrics(0, 0, 0, 0.0, 0.0);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> clearMemories(String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                shortTermMemories.remove(agentId);
                longTermMemories.remove(agentId);
                memoryPatterns.remove(agentId);
                activeSessions.remove(agentId);
                sessionContexts.remove(agentId);
                learningHistory.remove(agentId);
                logger.debug("Cleared all memories for agent: {}", agentId);
                return true;
            } catch (Exception e) {
                logger.error("Error clearing memories for agent: {}", agentId, e);
                return false;
            }
        });
    }

    private String generateMemoryId() {
        return "memory_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
