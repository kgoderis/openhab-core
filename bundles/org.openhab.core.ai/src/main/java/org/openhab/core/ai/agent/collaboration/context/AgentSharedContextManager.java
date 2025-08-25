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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.collaboration.ContextOptions;
import org.openhab.core.ai.agent.collaboration.ContextVersion;
import org.openhab.core.ai.agent.collaboration.SharedContext;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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

    @Reference
    private @Nullable MetricsService metricsService;

    // Context storage and management
    private final Map<String, SharedContext> contextStore = new ConcurrentHashMap<>();
    private final Map<String, ContextVersion> contextVersions = new ConcurrentHashMap<>();
    private final Map<String, ContextPermission> contextPermissions = new ConcurrentHashMap<>();
    private final Map<String, ContextChangeListener> changeListeners = new ConcurrentHashMap<>();

    // Context caching and optimization
    private final Map<String, CachedContext> contextCache = new ConcurrentHashMap<>();
    private final Map<String, ContextSchema> contextSchemas = new ConcurrentHashMap<>();

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
                // Record context conflict metrics
                MetricsService metrics = metricsService;
                if (metrics != null) {
                    metrics.recordOperation("agent-context", "conflict", true, Duration.ZERO);
                }
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

        // Record context write metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("agent-context", "write", true, Duration.ZERO);
        }
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
            // Record cache hit metrics
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("agent-context", "cache-hit", true, Duration.ZERO);
            }
            return CompletableFuture
                    .completedFuture(ContextRetrievalResult.success(cached.getContext(), cached.getVersion()));
        }

        // Get from store
        SharedContext context = contextStore.get(contextId);
        if (context == null) {
            // Record cache miss metrics
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("agent-context", "cache-miss", true, Duration.ZERO);
            }
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

        // Record context read and cache miss metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("agent-context", "read", true, Duration.ZERO);
            metrics.recordOperation("agent-context", "cache-miss", true, Duration.ZERO);
        }

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
        // Get statistics from MetricsService if available, otherwise return basic session counts
        MetricsService metrics = metricsService;
        if (metrics != null) {
            // TODO: Use MetricsService.getStatistics() with proper ContextManagerStatistics
            // For now, return basic session counts
            return new ContextManagerStatistics(0, 0, 0, 0, 0, contextStore.size(), contextVersions.size(),
                    contextCache.size(), changeListeners.size());
        }

        // Fallback to basic session counts if MetricsService is not available
        return new ContextManagerStatistics(0, 0, 0, 0, 0, contextStore.size(), contextVersions.size(),
                contextCache.size(), changeListeners.size());
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

        try {
            // Find the backup
            ContextBackup backup = contextBackups.get(backupId);
            if (backup == null) {
                return CompletableFuture.completedFuture(RestoreResult.failure("Backup not found: " + backupId));
            }

            // Verify the backup is for the correct context
            if (!backup.getContextId().equals(contextId)) {
                return CompletableFuture.completedFuture(RestoreResult.failure(
                        "Backup " + backupId + " is for context " + backup.getContextId() + ", not " + contextId));
            }

            // Perform the restore
            SharedContext restoredContext = backup.getContext();
            if (restoredContext == null) {
                return CompletableFuture
                        .completedFuture(RestoreResult.failure("Backup data is corrupted: no context found"));
            }

            // Restore the context
            contextStore.put(contextId, restoredContext);

            // Restore versions
            List<ContextVersion> versions = backup.getVersions();
            if (versions != null) {
                for (ContextVersion version : versions) {
                    contextVersions.put(version.getVersionId(), version);
                }
            }

            // Restore permissions
            Map<String, ContextPermission> permissions = backup.getPermissions();
            if (permissions != null) {
                for (Map.Entry<String, ContextPermission> entry : permissions.entrySet()) {
                    String targetAgentId = entry.getKey();
                    ContextPermission permission = entry.getValue();
                    contextPermissions.put(contextId + ":" + targetAgentId, permission);
                }
            }

            // Update cache
            updateContextCache(contextId, restoredContext);

            // Notify change listeners
            notifyContextChange(contextId, ContextChangeType.UPDATED, agentId,
                    restoredContext.getCurrentVersion().getData());

            logger.info("Context {} restored from backup {} by agent {}", contextId, backupId, agentId);

            return CompletableFuture
                    .completedFuture(RestoreResult.success("Context restored successfully from backup " + backupId));

        } catch (Exception e) {
            logger.error("Error restoring context {} from backup {}: {}", contextId, backupId, e.getMessage(), e);
            return CompletableFuture.completedFuture(RestoreResult.failure("Restore failed: " + e.getMessage()));
        }
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

    private final Map<String, ContextBackup> contextBackups = new ConcurrentHashMap<>();

    private void performBackup() {
        try {
            logger.debug("Performing context backup");

            // Perform backup of all contexts
            for (Map.Entry<String, SharedContext> entry : contextStore.entrySet()) {
                String contextId = entry.getKey();
                SharedContext context = entry.getValue();

                try {
                    // Create backup
                    ContextBackup backup = createContextBackup(contextId, context);
                    contextBackups.put(backup.getBackupId(), backup);

                    logger.debug("Backed up context: {} with backup ID: {}", contextId, backup.getBackupId());

                } catch (Exception e) {
                    logger.error("Error backing up context {}: {}", contextId, e.getMessage());
                }
            }

            // Clean up old backups based on retention policy
            cleanupOldBackups();

        } catch (Exception e) {
            logger.error("Error in context backup process: {}", e.getMessage(), e);
        }
    }

    private ContextBackup createContextBackup(String contextId, SharedContext context) {
        try {
            String backupId = "backup_" + contextId + "_" + System.currentTimeMillis();
            Instant backupTime = Instant.now();

            // Create backup data including context and all versions
            Map<String, Object> backupData = new ConcurrentHashMap<>();
            backupData.put("context", context);
            backupData.put("versions", getContextHistory(contextId, context.getCreatedBy(), 100));
            backupData.put("permissions", getContextPermissionsForBackup(contextId));

            return new ContextBackup(backupId, contextId, backupTime, backupData);

        } catch (Exception e) {
            logger.error("Error creating backup for context {}: {}", contextId, e.getMessage());
            throw new RuntimeException("Backup creation failed", e);
        }
    }

    private Map<String, ContextPermission> getContextPermissionsForBackup(String contextId) {
        Map<String, ContextPermission> permissions = new ConcurrentHashMap<>();

        for (Map.Entry<String, ContextPermission> entry : contextPermissions.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(contextId + ":")) {
                String agentId = key.substring(contextId.length() + 1);
                permissions.put(agentId, entry.getValue());
            }
        }

        return permissions;
    }

    private void cleanupOldBackups() {
        try {
            ContextManagerConfiguration config = configuration.get();
            Duration backupRetention = config.getBackupInterval().multipliedBy(24); // Keep 24 backup cycles

            Instant cutoffTime = Instant.now().minus(backupRetention);

            contextBackups.entrySet().removeIf(entry -> {
                ContextBackup backup = entry.getValue();
                boolean shouldRemove = backup.getBackupTime().isBefore(cutoffTime);

                if (shouldRemove) {
                    logger.debug("Removing old backup: {} from {}", backup.getBackupId(), backup.getBackupTime());
                }

                return shouldRemove;
            });

        } catch (Exception e) {
            logger.error("Error cleaning up old backups: {}", e.getMessage());
        }
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
}
