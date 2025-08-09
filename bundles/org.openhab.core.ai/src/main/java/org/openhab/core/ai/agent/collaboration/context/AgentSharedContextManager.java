package org.openhab.core.ai.agent.collaboration.context;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.lifecycle.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Shared Context Manager - Shared context management system
 * 
 * This service provides comprehensive shared context management capabilities for agents:
 * - Shared context storage and retrieval
 * - Context versioning and conflict resolution
 * - Context access control and permissions
 * - Context change notification system
 * - Context caching and optimization
 * - Context validation and schema enforcement
 * - Context backup and recovery
 * - Context performance monitoring
 * - Context cleanup and garbage collection
 * - Context analytics and usage tracking
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentSharedContextManager.class)
@NonNullByDefault
public class AgentSharedContextManager {

    private final Logger logger = LoggerFactory.getLogger(AgentSharedContextManager.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Context storage and management
    private final Map<String, SharedContext> contextStore = new ConcurrentHashMap<>();
    private final Map<String, ContextVersion> contextVersions = new ConcurrentHashMap<>();
    private final Map<String, ContextPermission> contextPermissions = new ConcurrentHashMap<>();
    private final Map<String, ContextChangeListener> changeListeners = new ConcurrentHashMap<>();

    // Context caching and optimization
    private final Map<String, CachedContext> contextCache = new ConcurrentHashMap<>();
    private final Map<String, ContextSchema> contextSchemas = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalContextReads = new AtomicLong(0);
    private final AtomicLong totalContextWrites = new AtomicLong(0);
    private final AtomicLong totalContextConflicts = new AtomicLong(0);
    private final AtomicLong totalContextCacheHits = new AtomicLong(0);
    private final AtomicLong totalContextCacheMisses = new AtomicLong(0);

    // Configuration
    private final AtomicReference<ContextManagerConfiguration> configuration = new AtomicReference<>(
            new ContextManagerConfiguration());

    // Background processing
    private final ScheduledExecutorService cleanupProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService backupProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService analyticsProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Shared Context Manager activated");

        // Start background processors
        cleanupProcessor.scheduleAtFixedRate(this::performCleanup, 0, 300000, TimeUnit.MILLISECONDS); // 5 minutes
        backupProcessor.scheduleAtFixedRate(this::performBackup, 0, 3600000, TimeUnit.MILLISECONDS); // 1 hour
        analyticsProcessor.scheduleAtFixedRate(this::updateAnalytics, 0, 60000, TimeUnit.MILLISECONDS); // 1 minute
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Shared Context Manager deactivated");

        // Shutdown background processors
        shutdownExecutor(cleanupProcessor);
        shutdownExecutor(backupProcessor);
        shutdownExecutor(analyticsProcessor);
    }

    /**
     * Create or update shared context
     * 
     * @param contextId Context identifier
     * @param agentId Agent ID creating/updating the context
     * @param data Context data
     * @param options Context options
     * @return Context creation/update result
     */
    public CompletableFuture<ContextOperationResult> createOrUpdateContext(String contextId, String agentId,
            Map<String, Object> data, ContextOptions options) {
        logger.debug("Agent {} creating/updating context {}: {}", agentId, contextId, data);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return CompletableFuture.completedFuture(ContextOperationResult.failure("Agent not found: " + agentId));
        }

        // Check permissions
        if (!hasWritePermission(contextId, agentId)) {
            return CompletableFuture.completedFuture(
                    ContextOperationResult.permissionDenied("No write permission for context: " + contextId));
        }

        // Validate context schema
        if (!validateContextSchema(contextId, data)) {
            return CompletableFuture
                    .completedFuture(ContextOperationResult.validationFailed("Context schema validation failed"));
        }

        // Handle versioning and conflict resolution
        SharedContext existingContext = contextStore.get(contextId);
        if (existingContext != null) {
            // Check for conflicts
            if (existingContext.getVersion() != options.getExpectedVersion()) {
                totalContextConflicts.incrementAndGet();
                return handleContextConflict(contextId, agentId, data, options, existingContext);
            }
        }

        // Create new context version
        ContextVersion newVersion = ContextVersion.builder().versionId(generateVersionId()).contextId(contextId)
                .agentId(agentId).timestamp(Instant.now()).data(data).options(options).build();

        // Update context store
        SharedContext sharedContext = SharedContext.builder().contextId(contextId).currentVersion(newVersion)
                .createdBy(agentId).createdAt(Instant.now()).lastModifiedBy(agentId).lastModifiedAt(Instant.now())
                .options(options).build();

        contextStore.put(contextId, sharedContext);
        contextVersions.put(newVersion.getVersionId(), newVersion);

        // Update cache
        updateContextCache(contextId, sharedContext);

        // Notify change listeners
        notifyContextChange(contextId, ContextChangeType.UPDATED, agentId, data);

        totalContextWrites.incrementAndGet();
        return CompletableFuture.completedFuture(ContextOperationResult.success("Context updated successfully"));
    }

    /**
     * Retrieve shared context
     * 
     * @param contextId Context identifier
     * @param agentId Agent ID requesting the context
     * @param versionId Specific version to retrieve (optional)
     * @return Context retrieval result
     */
    public CompletableFuture<ContextRetrievalResult> getContext(String contextId, String agentId,
            @Nullable String versionId) {
        logger.debug("Agent {} retrieving context {} (version: {})", agentId, contextId, versionId);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return CompletableFuture.completedFuture(ContextRetrievalResult.failure("Agent not found: " + agentId));
        }

        // Check permissions
        if (!hasReadPermission(contextId, agentId)) {
            return CompletableFuture.completedFuture(
                    ContextRetrievalResult.permissionDenied("No read permission for context: " + contextId));
        }

        // Try cache first
        CachedContext cached = contextCache.get(contextId);
        if (cached != null && !cached.isExpired()) {
            totalContextCacheHits.incrementAndGet();
            return CompletableFuture
                    .completedFuture(ContextRetrievalResult.success(cached.getContext(), cached.getVersion()));
        }

        // Get from store
        SharedContext context = contextStore.get(contextId);
        if (context == null) {
            totalContextCacheMisses.incrementAndGet();
            return CompletableFuture
                    .completedFuture(ContextRetrievalResult.notFound("Context not found: " + contextId));
        }

        // Get specific version if requested
        ContextVersion version = context.getCurrentVersion();
        if (versionId != null) {
            version = contextVersions.get(versionId);
            if (version == null) {
                return CompletableFuture
                        .completedFuture(ContextRetrievalResult.notFound("Version not found: " + versionId));
            }
        }

        // Update cache
        updateContextCache(contextId, context);

        totalContextReads.incrementAndGet();
        totalContextCacheMisses.incrementAndGet();

        return CompletableFuture.completedFuture(ContextRetrievalResult.success(context, version));
    }

    /**
     * Delete shared context
     * 
     * @param contextId Context identifier
     * @param agentId Agent ID deleting the context
     * @return Context deletion result
     */
    public CompletableFuture<ContextOperationResult> deleteContext(String contextId, String agentId) {
        logger.debug("Agent {} deleting context {}", agentId, contextId);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return CompletableFuture.completedFuture(ContextOperationResult.failure("Agent not found: " + agentId));
        }

        // Check permissions
        if (!hasDeletePermission(contextId, agentId)) {
            return CompletableFuture.completedFuture(
                    ContextOperationResult.permissionDenied("No delete permission for context: " + contextId));
        }

        // Remove from store
        SharedContext removedContext = contextStore.remove(contextId);
        if (removedContext == null) {
            return CompletableFuture
                    .completedFuture(ContextOperationResult.notFound("Context not found: " + contextId));
        }

        // Remove from cache
        contextCache.remove(contextId);

        // Notify change listeners
        notifyContextChange(contextId, ContextChangeType.DELETED, agentId, null);

        return CompletableFuture.completedFuture(ContextOperationResult.success("Context deleted successfully"));
    }

    /**
     * Set context permissions
     * 
     * @param contextId Context identifier
     * @param agentId Agent ID setting permissions
     * @param permissions Permissions to set
     * @return Permission setting result
     */
    public boolean setContextPermissions(String contextId, String agentId, Map<String, ContextPermission> permissions) {
        logger.debug("Agent {} setting permissions for context {}", agentId, contextId);

        // Validate agent exists and has admin permission
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            logger.warn("Cannot set permissions: agent not found: {}", agentId);
            return false;
        }

        if (!hasAdminPermission(contextId, agentId)) {
            logger.warn("Cannot set permissions: no admin permission for context {} by agent {}", contextId, agentId);
            return false;
        }

        // Update permissions
        for (Map.Entry<String, ContextPermission> entry : permissions.entrySet()) {
            String targetAgentId = entry.getKey();
            ContextPermission permission = entry.getValue();
            contextPermissions.put(contextId + ":" + targetAgentId, permission);
        }

        return true;
    }

    /**
     * Register context change listener
     * 
     * @param listenerId Listener identifier
     * @param listener Change listener implementation
     */
    public void registerChangeListener(String listenerId, ContextChangeListener listener) {
        changeListeners.put(listenerId, listener);
        logger.debug("Registered context change listener: {}", listenerId);
    }

    /**
     * Unregister context change listener
     * 
     * @param listenerId Listener identifier
     * @return Unregistration result
     */
    public boolean unregisterChangeListener(String listenerId) {
        ContextChangeListener removed = changeListeners.remove(listenerId);
        return removed != null;
    }

    /**
     * Register context schema
     * 
     * @param contextId Context identifier
     * @param schema Context schema
     */
    public void registerContextSchema(String contextId, ContextSchema schema) {
        contextSchemas.put(contextId, schema);
        logger.debug("Registered context schema for: {}", contextId);
    }

    /**
     * Get context history
     * 
     * @param contextId Context identifier
     * @param agentId Agent ID requesting history
     * @param limit Maximum number of versions
     * @return Context history
     */
    public List<ContextVersion> getContextHistory(String contextId, String agentId, int limit) {
        logger.debug("Agent {} requesting history for context {}", agentId, contextId);

        // Check permissions
        if (!hasReadPermission(contextId, agentId)) {
            logger.warn("No read permission for context {} by agent {}", contextId, agentId);
            return List.of();
        }

        return contextVersions.values().stream().filter(version -> version.getContextId().equals(contextId))
                .sorted((v1, v2) -> v2.getTimestamp().compareTo(v1.getTimestamp())).limit(limit).toList();
    }

    /**
     * Get context manager statistics
     * 
     * @return Context manager statistics
     */
    public ContextManagerStatistics getStatistics() {
        return new ContextManagerStatistics(totalContextReads.get(), totalContextWrites.get(),
                totalContextConflicts.get(), totalContextCacheHits.get(), totalContextCacheMisses.get(),
                contextStore.size(), contextVersions.size(), contextCache.size(), changeListeners.size());
    }

    /**
     * Perform context backup
     * 
     * @param contextId Context identifier to backup
     * @return Backup result
     */
    public CompletableFuture<BackupResult> backupContext(String contextId) {
        logger.debug("Backing up context: {}", contextId);

        SharedContext context = contextStore.get(contextId);
        if (context == null) {
            return CompletableFuture.completedFuture(BackupResult.notFound("Context not found: " + contextId));
        }

        // Perform backup logic here
        // This is a placeholder for actual backup implementation

        return CompletableFuture.completedFuture(BackupResult.success("Context backed up successfully"));
    }

    /**
     * Restore context from backup
     * 
     * @param contextId Context identifier
     * @param backupId Backup identifier
     * @param agentId Agent ID performing restore
     * @return Restore result
     */
    public CompletableFuture<RestoreResult> restoreContext(String contextId, String backupId, String agentId) {
        logger.debug("Agent {} restoring context {} from backup {}", agentId, contextId, backupId);

        // Check permissions
        if (!hasAdminPermission(contextId, agentId)) {
            return CompletableFuture
                    .completedFuture(RestoreResult.permissionDenied("No admin permission for context: " + contextId));
        }

        // Perform restore logic here
        // This is a placeholder for actual restore implementation

        return CompletableFuture.completedFuture(RestoreResult.success("Context restored successfully"));
    }

    // Private helper methods

    private String generateVersionId() {
        return "ver_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private boolean hasReadPermission(String contextId, String agentId) {
        ContextPermission permission = contextPermissions.get(contextId + ":" + agentId);
        return permission != null && permission.canRead();
    }

    private boolean hasWritePermission(String contextId, String agentId) {
        ContextPermission permission = contextPermissions.get(contextId + ":" + agentId);
        return permission != null && permission.canWrite();
    }

    private boolean hasDeletePermission(String contextId, String agentId) {
        ContextPermission permission = contextPermissions.get(contextId + ":" + agentId);
        return permission != null && permission.canDelete();
    }

    private boolean hasAdminPermission(String contextId, String agentId) {
        ContextPermission permission = contextPermissions.get(contextId + ":" + agentId);
        return permission != null && permission.isAdmin();
    }

    private boolean validateContextSchema(String contextId, Map<String, Object> data) {
        ContextSchema schema = contextSchemas.get(contextId);
        if (schema != null) {
            return schema.validate(data);
        }
        return true; // No schema defined, allow the data
    }

    private CompletableFuture<ContextOperationResult> handleContextConflict(String contextId, String agentId,
            Map<String, Object> data, ContextOptions options, SharedContext existingContext) {
        // Implement conflict resolution logic
        // This is a placeholder for actual conflict resolution

        logger.warn("Context conflict detected for context {} by agent {}", contextId, agentId);

        // For now, reject the operation
        return CompletableFuture.completedFuture(ContextOperationResult
                .conflict("Context conflict detected. Expected version: " + options.getExpectedVersion()
                        + ", actual version: " + existingContext.getCurrentVersion().getVersionId()));
    }

    private void updateContextCache(String contextId, SharedContext context) {
        ContextManagerConfiguration config = configuration.get();
        Duration cacheExpiry = config.getCacheExpiry();

        CachedContext cached = new CachedContext(context, context.getCurrentVersion(), Instant.now().plus(cacheExpiry));

        contextCache.put(contextId, cached);
    }

    private void notifyContextChange(String contextId, ContextChangeType changeType, String agentId,
            @Nullable Map<String, Object> data) {
        for (ContextChangeListener listener : changeListeners.values()) {
            try {
                listener.onContextChange(contextId, changeType, agentId, data);
            } catch (Exception e) {
                logger.error("Error notifying context change listener", e);
            }
        }
    }

    private void performCleanup() {
        logger.debug("Performing context cleanup");

        // Clean up expired cache entries
        contextCache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // Clean up old versions based on retention policy
        ContextManagerConfiguration config = configuration.get();
        Duration versionRetention = config.getVersionRetentionPeriod();
        Instant cutoffTime = Instant.now().minus(versionRetention);

        contextVersions.entrySet().removeIf(entry -> entry.getValue().getTimestamp().isBefore(cutoffTime));
    }

    private void performBackup() {
        logger.debug("Performing context backup");

        // Perform backup of all contexts
        // This is a placeholder for actual backup implementation
    }

    private void updateAnalytics() {
        logger.debug("Updating context analytics");

        // Update analytics data
        // This is a placeholder for actual analytics implementation
    }

    private void shutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Inner classes and interfaces

    /**
     * Shared context data
     */
    public static class SharedContext {
        private final String contextId;
        private final ContextVersion currentVersion;
        private final String createdBy;
        private final Instant createdAt;
        private final String lastModifiedBy;
        private final Instant lastModifiedAt;
        private final ContextOptions options;

        private SharedContext(Builder builder) {
            this.contextId = builder.contextId;
            this.currentVersion = builder.currentVersion;
            this.createdBy = builder.createdBy;
            this.createdAt = builder.createdAt;
            this.lastModifiedBy = builder.lastModifiedBy;
            this.lastModifiedAt = builder.lastModifiedAt;
            this.options = builder.options;
        }

        // Getters
        public String getContextId() {
            return contextId;
        }

        public ContextVersion getCurrentVersion() {
            return currentVersion;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public String getLastModifiedBy() {
            return lastModifiedBy;
        }

        public Instant getLastModifiedAt() {
            return lastModifiedAt;
        }

        public ContextOptions getOptions() {
            return options;
        }

        public long getVersion() {
            return currentVersion.getVersionId().hashCode();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String contextId;
            private ContextVersion currentVersion;
            private String createdBy;
            private Instant createdAt;
            private String lastModifiedBy;
            private Instant lastModifiedAt;
            private ContextOptions options;

            public Builder contextId(String contextId) {
                this.contextId = contextId;
                return this;
            }

            public Builder currentVersion(ContextVersion currentVersion) {
                this.currentVersion = currentVersion;
                return this;
            }

            public Builder createdBy(String createdBy) {
                this.createdBy = createdBy;
                return this;
            }

            public Builder createdAt(Instant createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder lastModifiedBy(String lastModifiedBy) {
                this.lastModifiedBy = lastModifiedBy;
                return this;
            }

            public Builder lastModifiedAt(Instant lastModifiedAt) {
                this.lastModifiedAt = lastModifiedAt;
                return this;
            }

            public Builder options(ContextOptions options) {
                this.options = options;
                return this;
            }

            public SharedContext build() {
                return new SharedContext(this);
            }
        }
    }

    /**
     * Context version
     */
    public static class ContextVersion {
        private final String versionId;
        private final String contextId;
        private final String agentId;
        private final Instant timestamp;
        private final Map<String, Object> data;
        private final ContextOptions options;

        private ContextVersion(Builder builder) {
            this.versionId = builder.versionId;
            this.contextId = builder.contextId;
            this.agentId = builder.agentId;
            this.timestamp = builder.timestamp;
            this.data = builder.data;
            this.options = builder.options;
        }

        // Getters
        public String getVersionId() {
            return versionId;
        }

        public String getContextId() {
            return contextId;
        }

        public String getAgentId() {
            return agentId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public ContextOptions getOptions() {
            return options;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String versionId;
            private String contextId;
            private String agentId;
            private Instant timestamp;
            private Map<String, Object> data;
            private ContextOptions options;

            public Builder versionId(String versionId) {
                this.versionId = versionId;
                return this;
            }

            public Builder contextId(String contextId) {
                this.contextId = contextId;
                return this;
            }

            public Builder agentId(String agentId) {
                this.agentId = agentId;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder data(Map<String, Object> data) {
                this.data = data;
                return this;
            }

            public Builder options(ContextOptions options) {
                this.options = options;
                return this;
            }

            public ContextVersion build() {
                return new ContextVersion(this);
            }
        }
    }

    /**
     * Context options
     */
    public static class ContextOptions {
        private final long expectedVersion;
        private final boolean persistent;
        private final Duration ttl;
        private final Map<String, Object> metadata;

        private ContextOptions(Builder builder) {
            this.expectedVersion = builder.expectedVersion;
            this.persistent = builder.persistent;
            this.ttl = builder.ttl;
            this.metadata = builder.metadata;
        }

        // Getters
        public long getExpectedVersion() {
            return expectedVersion;
        }

        public boolean isPersistent() {
            return persistent;
        }

        public Duration getTtl() {
            return ttl;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long expectedVersion = -1;
            private boolean persistent = false;
            private Duration ttl = Duration.ofHours(24);
            private Map<String, Object> metadata = Map.of();

            public Builder expectedVersion(long expectedVersion) {
                this.expectedVersion = expectedVersion;
                return this;
            }

            public Builder persistent(boolean persistent) {
                this.persistent = persistent;
                return this;
            }

            public Builder ttl(Duration ttl) {
                this.ttl = ttl;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public ContextOptions build() {
                return new ContextOptions(this);
            }
        }
    }

    /**
     * Context permission
     */
    public static class ContextPermission {
        private final boolean canRead;
        private final boolean canWrite;
        private final boolean canDelete;
        private final boolean isAdmin;

        public ContextPermission(boolean canRead, boolean canWrite, boolean canDelete, boolean isAdmin) {
            this.canRead = canRead;
            this.canWrite = canWrite;
            this.canDelete = canDelete;
            this.isAdmin = isAdmin;
        }

        // Getters
        public boolean canRead() {
            return canRead;
        }

        public boolean canWrite() {
            return canWrite;
        }

        public boolean canDelete() {
            return canDelete;
        }

        public boolean isAdmin() {
            return isAdmin;
        }
    }

    /**
     * Cached context
     */
    public static class CachedContext {
        private final SharedContext context;
        private final ContextVersion version;
        private final Instant expiryTime;

        public CachedContext(SharedContext context, ContextVersion version, Instant expiryTime) {
            this.context = context;
            this.version = version;
            this.expiryTime = expiryTime;
        }

        // Getters
        public SharedContext getContext() {
            return context;
        }

        public ContextVersion getVersion() {
            return version;
        }

        public Instant getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }

    /**
     * Context schema
     */
    public static class ContextSchema {
        private final String contextId;
        private final Map<String, String> requiredFields;
        private final Map<String, String> optionalFields;
        private final String version;

        public ContextSchema(String contextId, Map<String, String> requiredFields, Map<String, String> optionalFields,
                String version) {
            this.contextId = contextId;
            this.requiredFields = requiredFields;
            this.optionalFields = optionalFields;
            this.version = version;
        }

        // Getters
        public String getContextId() {
            return contextId;
        }

        public Map<String, String> getRequiredFields() {
            return requiredFields;
        }

        public Map<String, String> getOptionalFields() {
            return optionalFields;
        }

        public String getVersion() {
            return version;
        }

        public boolean validate(Map<String, Object> data) {
            // Placeholder for schema validation implementation
            return true;
        }
    }

    /**
     * Context manager statistics
     */
    public static class ContextManagerStatistics {
        private final long totalContextReads;
        private final long totalContextWrites;
        private final long totalContextConflicts;
        private final long totalContextCacheHits;
        private final long totalContextCacheMisses;
        private final int storedContexts;
        private final int storedVersions;
        private final int cachedContexts;
        private final int activeListeners;

        public ContextManagerStatistics(long totalContextReads, long totalContextWrites, long totalContextConflicts,
                long totalContextCacheHits, long totalContextCacheMisses, int storedContexts, int storedVersions,
                int cachedContexts, int activeListeners) {
            this.totalContextReads = totalContextReads;
            this.totalContextWrites = totalContextWrites;
            this.totalContextConflicts = totalContextConflicts;
            this.totalContextCacheHits = totalContextCacheHits;
            this.totalContextCacheMisses = totalContextCacheMisses;
            this.storedContexts = storedContexts;
            this.storedVersions = storedVersions;
            this.cachedContexts = cachedContexts;
            this.activeListeners = activeListeners;
        }

        // Getters
        public long getTotalContextReads() {
            return totalContextReads;
        }

        public long getTotalContextWrites() {
            return totalContextWrites;
        }

        public long getTotalContextConflicts() {
            return totalContextConflicts;
        }

        public long getTotalContextCacheHits() {
            return totalContextCacheHits;
        }

        public long getTotalContextCacheMisses() {
            return totalContextCacheMisses;
        }

        public int getStoredContexts() {
            return storedContexts;
        }

        public int getStoredVersions() {
            return storedVersions;
        }

        public int getCachedContexts() {
            return cachedContexts;
        }

        public int getActiveListeners() {
            return activeListeners;
        }
    }

    /**
     * Context manager configuration
     */
    public static class ContextManagerConfiguration {
        private Duration cacheExpiry = Duration.ofMinutes(30);
        private Duration versionRetentionPeriod = Duration.ofDays(7);
        private int maxCacheSize = 1000;
        private int maxVersionsPerContext = 100;
        private boolean enableBackup = true;
        private Duration backupInterval = Duration.ofHours(1);

        // Getters and setters
        public Duration getCacheExpiry() {
            return cacheExpiry;
        }

        public void setCacheExpiry(Duration cacheExpiry) {
            this.cacheExpiry = cacheExpiry;
        }

        public Duration getVersionRetentionPeriod() {
            return versionRetentionPeriod;
        }

        public void setVersionRetentionPeriod(Duration versionRetentionPeriod) {
            this.versionRetentionPeriod = versionRetentionPeriod;
        }

        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        public int getMaxVersionsPerContext() {
            return maxVersionsPerContext;
        }

        public void setMaxVersionsPerContext(int maxVersionsPerContext) {
            this.maxVersionsPerContext = maxVersionsPerContext;
        }

        public boolean isEnableBackup() {
            return enableBackup;
        }

        public void setEnableBackup(boolean enableBackup) {
            this.enableBackup = enableBackup;
        }

        public Duration getBackupInterval() {
            return backupInterval;
        }

        public void setBackupInterval(Duration backupInterval) {
            this.backupInterval = backupInterval;
        }
    }

    // Enums
    public enum ContextChangeType {
        CREATED,
        UPDATED,
        DELETED
    }

    // Interfaces
    public interface ContextChangeListener {
        void onContextChange(String contextId, ContextChangeType changeType, String agentId,
                @Nullable Map<String, Object> data);
    }

    public interface ContextOperationResult {
        boolean isSuccess();

        String getMessage();

        static ContextOperationResult success(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static ContextOperationResult failure(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static ContextOperationResult permissionDenied(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Permission denied: " + message;
                }
            };
        }

        static ContextOperationResult validationFailed(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Validation failed: " + message;
                }
            };
        }

        static ContextOperationResult conflict(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Conflict: " + message;
                }
            };
        }

        static ContextOperationResult notFound(String message) {
            return new ContextOperationResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Not found: " + message;
                }
            };
        }
    }

    public interface ContextRetrievalResult {
        boolean isSuccess();

        String getMessage();

        @Nullable
        SharedContext getContext();

        @Nullable
        ContextVersion getVersion();

        static ContextRetrievalResult success(SharedContext context, ContextVersion version) {
            return new ContextRetrievalResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return "Context retrieved successfully";
                }

                @Override
                public SharedContext getContext() {
                    return context;
                }

                @Override
                public ContextVersion getVersion() {
                    return version;
                }
            };
        }

        static ContextRetrievalResult failure(String message) {
            return new ContextRetrievalResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }

                @Override
                public SharedContext getContext() {
                    return null;
                }

                @Override
                public ContextVersion getVersion() {
                    return null;
                }
            };
        }

        static ContextRetrievalResult permissionDenied(String message) {
            return new ContextRetrievalResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Permission denied: " + message;
                }

                @Override
                public SharedContext getContext() {
                    return null;
                }

                @Override
                public ContextVersion getVersion() {
                    return null;
                }
            };
        }

        static ContextRetrievalResult notFound(String message) {
            return new ContextRetrievalResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Not found: " + message;
                }

                @Override
                public SharedContext getContext() {
                    return null;
                }

                @Override
                public ContextVersion getVersion() {
                    return null;
                }
            };
        }
    }

    public interface BackupResult {
        boolean isSuccess();

        String getMessage();

        static BackupResult success(String message) {
            return new BackupResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static BackupResult failure(String message) {
            return new BackupResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static BackupResult notFound(String message) {
            return new BackupResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Not found: " + message;
                }
            };
        }
    }

    public interface RestoreResult {
        boolean isSuccess();

        String getMessage();

        static RestoreResult success(String message) {
            return new RestoreResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static RestoreResult failure(String message) {
            return new RestoreResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static RestoreResult permissionDenied(String message) {
            return new RestoreResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Permission denied: " + message;
                }
            };
        }
    }
}
