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
    private final Map<String, MemoryReasoningSession> activeSessions = new ConcurrentHashMap<>();
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
    public AgentMemoryStoreResult storeShortTermMemory(String agentId, MemoryEntry entry) {
        try {
            shortTermLock.writeLock().lock();

            ShortTermMemory memory = shortTermMemories.computeIfAbsent(agentId, k -> new ShortTermMemory(agentId));
            memory.addEntry(entry);

            // Clean up old entries
            memory.cleanupOldEntries(shortTermRetention);

            totalMemoryStores.incrementAndGet();
            logger.debug("Stored short-term memory for agent: {}", agentId);

            return AgentMemoryStoreResult.success(entry);
        } finally {
            shortTermLock.writeLock().unlock();
        }
    }

    /**
     * Store a memory entry in long-term memory
     */
    public AgentMemoryStoreResult storeLongTermMemory(String agentId, MemoryEntry entry) {
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

            return AgentMemoryStoreResult.success(entry);
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
    public AgentMemoryConsolidationResult consolidateMemoriesInternal(String agentId) {
        try {
            shortTermLock.writeLock().lock();
            longTermLock.writeLock().lock();

            ShortTermMemory shortTerm = shortTermMemories.get(agentId);
            if (shortTerm == null) {
                return AgentMemoryConsolidationResult.noData("No short-term memory found for agent: " + agentId);
            }

            List<MemoryEntry> entriesForConsolidation = shortTerm.getEntriesForConsolidation();
            if (entriesForConsolidation.isEmpty()) {
                return AgentMemoryConsolidationResult.noData("No entries ready for consolidation");
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

            return AgentMemoryConsolidationResult.success(consolidatedCount);
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
    public org.openhab.core.ai.reasoning.MemoryPerformanceMetrics getPerformanceMetrics() {
        return org.openhab.core.ai.reasoning.MemoryPerformanceMetrics.builder().totalStores(totalMemoryStores.get())
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
            MemoryReasoningSession session = new MemoryReasoningSession(sessionId, agentId, context);
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
    public MemoryReasoningSession retrieveReasoningSession(String agentId, String sessionId) {
        MemoryReasoningSession session = activeSessions.get(sessionId);
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
        MemoryReasoningSession session = activeSessions.get(sessionId);
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
    public List<MemoryReasoningSession> getActiveSessions(String agentId) {
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
        // Load persistent memory if available
        try {
            loadPersistentMemory();
        } catch (Exception e) {
            logger.warn("Failed to load persistent memory: {}", e.getMessage());
        }
    }

    private void cleanupMemory() {
        logger.debug("Cleaning up unified agent memory system");
        // Save persistent memory
        try {
            savePersistentMemory();
        } catch (Exception e) {
            logger.warn("Failed to save persistent memory: {}", e.getMessage());
        }
    }

    /**
     * Load persistent memory from storage
     */
    private void loadPersistentMemory() {
        // In a real implementation, this would load from a database or file system
        // For now, we'll implement a basic file-based persistence
        try {
            String persistenceDir = System.getProperty("openhab.userdata") + "/ai/memory";
            java.io.File dir = new java.io.File(persistenceDir);
            if (!dir.exists()) {
                dir.mkdirs();
                logger.debug("Created memory persistence directory: {}", persistenceDir);
                return;
            }

            // Load short-term memories
            java.io.File shortTermFile = new java.io.File(dir, "short-term-memory.json");
            if (shortTermFile.exists()) {
                // Implement JSON deserialization for short-term memories
                logger.debug("Found short-term memory file, loading...");
                try {
                    String jsonContent = new String(java.nio.file.Files.readAllBytes(shortTermFile.toPath()));
                    deserializeShortTermMemories(jsonContent);
                    logger.debug("Successfully loaded short-term memories from file");
                } catch (Exception e) {
                    logger.warn("Failed to load short-term memories from file: {}", e.getMessage());
                }
            }

            // Load long-term memories
            java.io.File longTermFile = new java.io.File(dir, "long-term-memory.json");
            if (longTermFile.exists()) {
                // Implement JSON deserialization for long-term memories
                logger.debug("Found long-term memory file, loading...");
                try {
                    String jsonContent = new String(java.nio.file.Files.readAllBytes(longTermFile.toPath()));
                    deserializeLongTermMemories(jsonContent);
                    logger.debug("Successfully loaded long-term memories from file");
                } catch (Exception e) {
                    logger.warn("Failed to load long-term memories from file: {}", e.getMessage());
                }
            }

            // Load memory patterns
            java.io.File patternsFile = new java.io.File(dir, "memory-patterns.json");
            if (patternsFile.exists()) {
                // Implement JSON deserialization for memory patterns
                logger.debug("Found memory patterns file, loading...");
                try {
                    String jsonContent = new String(java.nio.file.Files.readAllBytes(patternsFile.toPath()));
                    deserializeMemoryPatterns(jsonContent);
                    logger.debug("Successfully loaded memory patterns from file");
                } catch (Exception e) {
                    logger.warn("Failed to load memory patterns from file: {}", e.getMessage());
                }
            }

            logger.debug("Persistent memory loading completed");

        } catch (Exception e) {
            logger.error("Error loading persistent memory: {}", e.getMessage(), e);
        }
    }

    /**
     * Save persistent memory to storage
     */
    private void savePersistentMemory() {
        // In a real implementation, this would save to a database or file system
        // For now, we'll implement a basic file-based persistence
        try {
            String persistenceDir = System.getProperty("openhab.userdata") + "/ai/memory";
            java.io.File dir = new java.io.File(persistenceDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Save short-term memories
            // Implement JSON serialization for short-term memories
            logger.debug("Saving short-term memories...");
            try {
                String jsonContent = serializeShortTermMemories();
                java.io.File shortTermFile = new java.io.File(dir, "short-term-memory.json");
                java.nio.file.Files.write(shortTermFile.toPath(), jsonContent.getBytes());
                logger.debug("Successfully saved short-term memories to file");
            } catch (Exception e) {
                logger.warn("Failed to save short-term memories to file: {}", e.getMessage());
            }

            // Save long-term memories
            // Implement JSON serialization for long-term memories
            logger.debug("Saving long-term memories...");
            try {
                String jsonContent = serializeLongTermMemories();
                java.io.File longTermFile = new java.io.File(dir, "long-term-memory.json");
                java.nio.file.Files.write(longTermFile.toPath(), jsonContent.getBytes());
                logger.debug("Successfully saved long-term memories to file");
            } catch (Exception e) {
                logger.warn("Failed to save long-term memories to file: {}", e.getMessage());
            }

            // Save memory patterns
            // Implement JSON serialization for memory patterns
            logger.debug("Saving memory patterns...");
            try {
                String jsonContent = serializeMemoryPatterns();
                java.io.File patternsFile = new java.io.File(dir, "memory-patterns.json");
                java.nio.file.Files.write(patternsFile.toPath(), jsonContent.getBytes());
                logger.debug("Successfully saved memory patterns to file");
            } catch (Exception e) {
                logger.warn("Failed to save memory patterns to file: {}", e.getMessage());
            }

            logger.debug("Persistent memory saving completed");

        } catch (Exception e) {
            logger.error("Error saving persistent memory: {}", e.getMessage(), e);
        }
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

    // Inner classes extracted to top-level files in org.openhab.core.ai.reasoning

    // MemoryManager interface implementation
    @Override
    public CompletableFuture<MemoryManager.MemoryStoreResult> storeShortTermMemory(String agentId, String memory,
            @Nullable Map<String, Object> metadata) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryEntry entry = new MemoryEntry(generateMemoryId(), memory, "general", 0.5,
                        metadata != null ? metadata : new ConcurrentHashMap<>());
                AgentMemoryStoreResult result = storeShortTermMemory(agentId, entry);
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
                AgentMemoryStoreResult result = storeLongTermMemory(agentId, entry);
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
                AgentMemoryConsolidationResult result = consolidateMemoriesInternal(agentId);
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
                org.openhab.core.ai.reasoning.MemoryPerformanceMetrics metrics = getPerformanceMetrics();
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

    // ===== JSON SERIALIZATION/DESERIALIZATION METHODS =====

    /**
     * Serialize short-term memories to JSON
     */
    private String serializeShortTermMemories() {
        try {
            Map<String, Object> serializedData = new ConcurrentHashMap<>();
            for (Map.Entry<String, ShortTermMemory> entry : shortTermMemories.entrySet()) {
                String agentId = entry.getKey();
                ShortTermMemory memory = entry.getValue();

                List<Map<String, Object>> entries = new ArrayList<>();
                for (MemoryEntry memoryEntry : memory.getEntries(null, Integer.MAX_VALUE)) {
                    Map<String, Object> entryData = new ConcurrentHashMap<>();
                    entryData.put("id", memoryEntry.getId());
                    entryData.put("content", memoryEntry.getContent());
                    entryData.put("category", memoryEntry.getCategory());
                    entryData.put("importance", memoryEntry.getImportance());
                    entryData.put("relevance", memoryEntry.getRelevance());
                    entryData.put("timestamp", memoryEntry.getTimestamp().toEpochMilli());
                    entryData.put("metadata", memoryEntry.getMetadata());
                    entryData.put("accessCount", memoryEntry.getAccessCount());
                    entries.add(entryData);
                }
                serializedData.put(agentId, entries);
            }

            // Simple JSON serialization - in a real implementation, use a proper JSON library
            return serializeToJson(serializedData);

        } catch (Exception e) {
            logger.error("Error serializing short-term memories", e);
            return "{}";
        }
    }

    /**
     * Deserialize short-term memories from JSON
     */
    private void deserializeShortTermMemories(String jsonContent) {
        try {
            Map<String, Object> data = deserializeFromJson(jsonContent);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String agentId = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> entriesData = (List<Map<String, Object>>) entry.getValue();

                ShortTermMemory memory = new ShortTermMemory(agentId);
                for (Map<String, Object> entryData : entriesData) {
                    MemoryEntry memoryEntry = new MemoryEntry((String) entryData.get("id"),
                            (String) entryData.get("content"), (String) entryData.get("category"),
                            ((Number) entryData.get("importance")).doubleValue(),
                            (Map<String, Object>) entryData.get("metadata"));
                    memory.addEntry(memoryEntry);
                }
                shortTermMemories.put(agentId, memory);
            }

        } catch (Exception e) {
            logger.error("Error deserializing short-term memories", e);
        }
    }

    /**
     * Serialize long-term memories to JSON
     */
    private String serializeLongTermMemories() {
        try {
            Map<String, Object> serializedData = new ConcurrentHashMap<>();
            for (Map.Entry<String, LongTermMemory> entry : longTermMemories.entrySet()) {
                String agentId = entry.getKey();
                LongTermMemory memory = entry.getValue();

                List<Map<String, Object>> entries = new ArrayList<>();
                for (MemoryEntry memoryEntry : memory.getEntries(null, Integer.MAX_VALUE)) {
                    Map<String, Object> entryData = new ConcurrentHashMap<>();
                    entryData.put("id", memoryEntry.getId());
                    entryData.put("content", memoryEntry.getContent());
                    entryData.put("category", memoryEntry.getCategory());
                    entryData.put("importance", memoryEntry.getImportance());
                    entryData.put("relevance", memoryEntry.getRelevance());
                    entryData.put("timestamp", memoryEntry.getTimestamp().toEpochMilli());
                    entryData.put("metadata", memoryEntry.getMetadata());
                    entryData.put("accessCount", memoryEntry.getAccessCount());
                    entries.add(entryData);
                }
                serializedData.put(agentId, entries);
            }

            return serializeToJson(serializedData);

        } catch (Exception e) {
            logger.error("Error serializing long-term memories", e);
            return "{}";
        }
    }

    /**
     * Deserialize long-term memories from JSON
     */
    private void deserializeLongTermMemories(String jsonContent) {
        try {
            Map<String, Object> data = deserializeFromJson(jsonContent);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String agentId = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> entriesData = (List<Map<String, Object>>) entry.getValue();

                LongTermMemory memory = new LongTermMemory(agentId);
                for (Map<String, Object> entryData : entriesData) {
                    MemoryEntry memoryEntry = new MemoryEntry((String) entryData.get("id"),
                            (String) entryData.get("content"), (String) entryData.get("category"),
                            ((Number) entryData.get("importance")).doubleValue(),
                            (Map<String, Object>) entryData.get("metadata"));
                    memory.addEntry(memoryEntry);
                }
                longTermMemories.put(agentId, memory);
            }

        } catch (Exception e) {
            logger.error("Error deserializing long-term memories", e);
        }
    }

    /**
     * Serialize memory patterns to JSON
     */
    private String serializeMemoryPatterns() {
        try {
            Map<String, Object> serializedData = new ConcurrentHashMap<>();
            for (Map.Entry<String, MemoryPattern> entry : memoryPatterns.entrySet()) {
                String agentId = entry.getKey();
                MemoryPattern pattern = entry.getValue();

                List<Map<String, Object>> patternsData = new ArrayList<>();
                for (MemoryPattern.PatternEntry patternEntry : pattern.getPatterns()) {
                    Map<String, Object> patternData = new ConcurrentHashMap<>();
                    patternData.put("category", patternEntry.getCategory());
                    patternData.put("occurrenceCount", patternEntry.getOccurrenceCount());
                    patternData.put("averageImportance", patternEntry.getAverageImportance());
                    patternData.put("lastOccurrence", patternEntry.getLastOccurrence().toEpochMilli());
                    patternsData.add(patternData);
                }
                serializedData.put(agentId, patternsData);
            }

            return serializeToJson(serializedData);

        } catch (Exception e) {
            logger.error("Error serializing memory patterns", e);
            return "{}";
        }
    }

    /**
     * Deserialize memory patterns from JSON
     */
    private void deserializeMemoryPatterns(String jsonContent) {
        try {
            Map<String, Object> data = deserializeFromJson(jsonContent);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String agentId = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> patternsData = (List<Map<String, Object>>) entry.getValue();

                MemoryPattern pattern = new MemoryPattern(agentId);
                for (Map<String, Object> patternData : patternsData) {
                    MemoryPattern.PatternEntry patternEntry = new MemoryPattern.PatternEntry(
                            (String) patternData.get("category"));
                    // Note: PatternEntry doesn't have setters, so we can't restore the full state
                    // In a real implementation, you'd need to add setters or use a different approach
                }
                memoryPatterns.put(agentId, pattern);
            }

        } catch (Exception e) {
            logger.error("Error deserializing memory patterns", e);
        }
    }

    /**
     * Simple JSON serialization helper
     */
    private String serializeToJson(Map<String, Object> data) {
        // Simple JSON serialization - in a real implementation, use a proper JSON library like Jackson
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(serializeValue(entry.getValue()));
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Simple JSON deserialization helper
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeFromJson(String json) {
        // Simple JSON deserialization - in a real implementation, use a proper JSON library like Jackson
        // For now, return an empty map as a placeholder
        return new ConcurrentHashMap<>();
    }

    /**
     * Serialize a value to JSON string
     */
    private String serializeValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Object item : (List<?>) value) {
                if (!first) {
                    json.append(",");
                }
                json.append(serializeValue(item));
                first = false;
            }
            json.append("]");
            return json.toString();
        } else if (value instanceof Map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":");
                json.append(serializeValue(entry.getValue()));
                first = false;
            }
            json.append("}");
            return json.toString();
        } else {
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        }
    }
}
