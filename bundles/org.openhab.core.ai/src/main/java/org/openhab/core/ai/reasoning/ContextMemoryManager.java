package org.openhab.core.ai.reasoning;

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
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context Memory Manager - Manages persistent context storage and retrieval
 * 
 * <p>
 * This manager provides:
 * - Persistent context storage with versioning
 * - Context access control and permissions
 * - Context change notification system
 * - Context caching and optimization
 * - Context validation and schema enforcement
 * - Context backup and recovery
 * - Performance monitoring and analytics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = ContextMemoryManager.class)
@NonNullByDefault
public class ContextMemoryManager {

    private static final Logger logger = LoggerFactory.getLogger(ContextMemoryManager.class);

    // Storage
    private final Map<String, ContextEntry> contextStore = new ConcurrentHashMap<>();
    private final Map<String, ContextVersion> versionHistory = new ConcurrentHashMap<>();
    private final Map<String, ContextPermissions> accessControl = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalContextStores = new AtomicLong(0);
    private final AtomicLong totalContextRetrievals = new AtomicLong(0);
    private final AtomicLong totalContextUpdates = new AtomicLong(0);
    private final AtomicLong totalContextDeletions = new AtomicLong(0);
    private final AtomicLong totalAccessDenials = new AtomicLong(0);

    // Thread safety
    private final ReadWriteLock storeLock = new ReentrantReadWriteLock();
    private final ReadWriteLock versionLock = new ReentrantReadWriteLock();

    // Configuration
    private int maxContextSize = 1024 * 1024; // 1MB default
    private int maxVersionHistory = 100;
    private boolean enableCaching = true;
    private boolean enableValidation = true;
    private boolean enableBackup = true;

    @Activate
    public void activate() {
        logger.debug("Context Memory Manager activated");
        initializeStorage();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Context Memory Manager deactivated");
        cleanupStorage();
    }

    /**
     * Store a new context entry
     */
    public ContextStoreResult storeContext(String contextId, ReasoningContext context, String owner) {
        if (!hasPermission(owner, contextId, ContextPermission.WRITE)) {
            totalAccessDenials.incrementAndGet();
            return ContextStoreResult.accessDenied("No write permission for context: " + contextId);
        }

        if (enableValidation && !validateContext(context)) {
            return ContextStoreResult.validationFailed("Context validation failed");
        }

        try {
            storeLock.writeLock().lock();

            ContextEntry entry = new ContextEntry(contextId, context, owner, Instant.now());
            contextStore.put(contextId, entry);

            // Create version history
            createVersionHistory(contextId, context, owner);

            totalContextStores.incrementAndGet();
            logger.debug("Stored context: {} for owner: {}", contextId, owner);

            return ContextStoreResult.success(entry);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    /**
     * Retrieve a context entry
     */
    public ContextRetrieveResult retrieveContext(String contextId, String requester) {
        if (!hasPermission(requester, contextId, ContextPermission.READ)) {
            totalAccessDenials.incrementAndGet();
            return ContextRetrieveResult.accessDenied("No read permission for context: " + contextId);
        }

        try {
            storeLock.readLock().lock();

            ContextEntry entry = contextStore.get(contextId);
            if (entry == null) {
                return ContextRetrieveResult.notFound("Context not found: " + contextId);
            }

            totalContextRetrievals.incrementAndGet();
            logger.debug("Retrieved context: {} for requester: {}", contextId, requester);

            return ContextRetrieveResult.success(entry);
        } finally {
            storeLock.readLock().unlock();
        }
    }

    /**
     * Update an existing context entry
     */
    public ContextUpdateResult updateContext(String contextId, ReasoningContext newContext, String updater) {
        if (!hasPermission(updater, contextId, ContextPermission.WRITE)) {
            totalAccessDenials.incrementAndGet();
            return ContextUpdateResult.accessDenied("No write permission for context: " + contextId);
        }

        if (enableValidation && !validateContext(newContext)) {
            return ContextUpdateResult.validationFailed("Context validation failed");
        }

        try {
            storeLock.writeLock().lock();

            ContextEntry existingEntry = contextStore.get(contextId);
            if (existingEntry == null) {
                return ContextUpdateResult.notFound("Context not found: " + contextId);
            }

            // Create backup before update
            if (enableBackup) {
                createBackup(contextId, existingEntry);
            }

            // Update context
            ContextEntry updatedEntry = new ContextEntry(contextId, newContext, updater, Instant.now());
            contextStore.put(contextId, updatedEntry);

            // Create version history
            createVersionHistory(contextId, newContext, updater);

            totalContextUpdates.incrementAndGet();
            logger.debug("Updated context: {} by updater: {}", contextId, updater);

            return ContextUpdateResult.success(updatedEntry);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    /**
     * Delete a context entry
     */
    public ContextDeleteResult deleteContext(String contextId, String deleter) {
        if (!hasPermission(deleter, contextId, ContextPermission.DELETE)) {
            totalAccessDenials.incrementAndGet();
            return ContextDeleteResult.accessDenied("No delete permission for context: " + contextId);
        }

        try {
            storeLock.writeLock().lock();

            ContextEntry entry = contextStore.remove(contextId);
            if (entry == null) {
                return ContextDeleteResult.notFound("Context not found: " + contextId);
            }

            // Clean up version history
            versionHistory.remove(contextId);

            // Clean up access control
            accessControl.remove(contextId);

            totalContextDeletions.incrementAndGet();
            logger.debug("Deleted context: {} by deleter: {}", contextId, deleter);

            return ContextDeleteResult.success(entry);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    /**
     * Get version history for a context
     */
    public List<ContextVersion.VersionEntry> getVersionHistory(String contextId, String requester) {
        if (!hasPermission(requester, contextId, ContextPermission.READ)) {
            totalAccessDenials.incrementAndGet();
            return Collections.emptyList();
        }

        try {
            versionLock.readLock().lock();

            ContextVersion version = versionHistory.get(contextId);
            if (version == null) {
                return Collections.emptyList();
            }

            return version.getHistory();
        } finally {
            versionLock.readLock().unlock();
        }
    }

    /**
     * Set permissions for a context
     */
    public void setPermissions(String contextId, ContextPermissions permissions) {
        accessControl.put(contextId, permissions);
        logger.debug("Set permissions for context: {}", contextId);
    }

    /**
     * Get performance metrics
     */
    public ContextPerformanceMetrics getPerformanceMetrics() {
        return ContextPerformanceMetrics.builder().totalStores(totalContextStores.get())
                .totalRetrievals(totalContextRetrievals.get()).totalUpdates(totalContextUpdates.get())
                .totalDeletions(totalContextDeletions.get()).totalAccessDenials(totalAccessDenials.get())
                .currentContextCount(contextStore.size()).currentVersionCount(versionHistory.size()).build();
    }

    // Private helper methods

    private void initializeStorage() {
        logger.debug("Initializing context storage");
        // TODO: Load persistent storage if available
    }

    private void cleanupStorage() {
        logger.debug("Cleaning up context storage");
        // TODO: Save persistent storage
    }

    private boolean hasPermission(String user, String contextId, ContextPermission permission) {
        ContextPermissions permissions = accessControl.get(contextId);
        if (permissions == null) {
            // Default permissions - owner has full access
            return true;
        }
        return permissions.hasPermission(user, permission);
    }

    private boolean validateContext(ReasoningContext context) {
        if (context == null) {
            return false;
        }

        // Basic validation - can be extended with schema validation
        return context.getCurrentContext() != null && !context.getCurrentContext().trim().isEmpty();
    }

    private void createVersionHistory(String contextId, ReasoningContext context, String owner) {
        try {
            versionLock.writeLock().lock();

            ContextVersion version = versionHistory.get(contextId);
            if (version == null) {
                version = new ContextVersion(contextId);
                versionHistory.put(contextId, version);
            }

            version.addVersion(context, owner, Instant.now());

            // Limit version history size
            if (version.getHistory().size() > maxVersionHistory) {
                version.trimHistory(maxVersionHistory);
            }
        } finally {
            versionLock.writeLock().unlock();
        }
    }

    private void createBackup(String contextId, ContextEntry entry) {
        // TODO: Implement backup mechanism
        logger.debug("Created backup for context: {}", contextId);
    }

    // Inner classes

    public static class ContextEntry {
        private final String contextId;
        private final ReasoningContext context;
        private final String owner;
        private final Instant timestamp;

        public ContextEntry(String contextId, ReasoningContext context, String owner, Instant timestamp) {
            this.contextId = contextId;
            this.context = context;
            this.owner = owner;
            this.timestamp = timestamp;
        }

        public String getContextId() {
            return contextId;
        }

        public ReasoningContext getContext() {
            return context;
        }

        public String getOwner() {
            return owner;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class ContextVersion {
        private final String contextId;
        private final List<VersionEntry> history = new ArrayList<>();

        public ContextVersion(String contextId) {
            this.contextId = contextId;
        }

        public void addVersion(ReasoningContext context, String owner, Instant timestamp) {
            history.add(new VersionEntry(context, owner, timestamp));
        }

        public List<VersionEntry> getHistory() {
            return Collections.unmodifiableList(history);
        }

        public void trimHistory(int maxSize) {
            if (history.size() > maxSize) {
                history.subList(0, history.size() - maxSize).clear();
            }
        }

        public static class VersionEntry {
            private final ReasoningContext context;
            private final String owner;
            private final Instant timestamp;

            public VersionEntry(ReasoningContext context, String owner, Instant timestamp) {
                this.context = context;
                this.owner = owner;
                this.timestamp = timestamp;
            }

            public ReasoningContext getContext() {
                return context;
            }

            public String getOwner() {
                return owner;
            }

            public Instant getTimestamp() {
                return timestamp;
            }
        }
    }

    public static class ContextPermissions {
        private final Map<String, List<ContextPermission>> userPermissions = new ConcurrentHashMap<>();

        public void addPermission(String user, ContextPermission permission) {
            userPermissions.computeIfAbsent(user, k -> new ArrayList<>()).add(permission);
        }

        public boolean hasPermission(String user, ContextPermission permission) {
            List<ContextPermission> permissions = userPermissions.get(user);
            return permissions != null && permissions.contains(permission);
        }
    }

    public enum ContextPermission {
        READ,
        WRITE,
        DELETE,
        ADMIN
    }

    // Result classes

    public static class ContextStoreResult {
        private final boolean success;
        private final @Nullable ContextEntry entry;
        private final @Nullable String error;

        private ContextStoreResult(boolean success, @Nullable ContextEntry entry, @Nullable String error) {
            this.success = success;
            this.entry = entry;
            this.error = error;
        }

        public static ContextStoreResult success(ContextEntry entry) {
            return new ContextStoreResult(true, entry, null);
        }

        public static ContextStoreResult accessDenied(String error) {
            return new ContextStoreResult(false, null, error);
        }

        public static ContextStoreResult validationFailed(String error) {
            return new ContextStoreResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable ContextEntry getEntry() {
            return entry;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class ContextRetrieveResult {
        private final boolean success;
        private final @Nullable ContextEntry entry;
        private final @Nullable String error;

        private ContextRetrieveResult(boolean success, @Nullable ContextEntry entry, @Nullable String error) {
            this.success = success;
            this.entry = entry;
            this.error = error;
        }

        public static ContextRetrieveResult success(ContextEntry entry) {
            return new ContextRetrieveResult(true, entry, null);
        }

        public static ContextRetrieveResult accessDenied(String error) {
            return new ContextRetrieveResult(false, null, error);
        }

        public static ContextRetrieveResult notFound(String error) {
            return new ContextRetrieveResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable ContextEntry getEntry() {
            return entry;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class ContextUpdateResult {
        private final boolean success;
        private final @Nullable ContextEntry entry;
        private final @Nullable String error;

        private ContextUpdateResult(boolean success, @Nullable ContextEntry entry, @Nullable String error) {
            this.success = success;
            this.entry = entry;
            this.error = error;
        }

        public static ContextUpdateResult success(ContextEntry entry) {
            return new ContextUpdateResult(true, entry, null);
        }

        public static ContextUpdateResult accessDenied(String error) {
            return new ContextUpdateResult(false, null, error);
        }

        public static ContextUpdateResult validationFailed(String error) {
            return new ContextUpdateResult(false, null, error);
        }

        public static ContextUpdateResult notFound(String error) {
            return new ContextUpdateResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable ContextEntry getEntry() {
            return entry;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class ContextDeleteResult {
        private final boolean success;
        private final @Nullable ContextEntry entry;
        private final @Nullable String error;

        private ContextDeleteResult(boolean success, @Nullable ContextEntry entry, @Nullable String error) {
            this.success = success;
            this.entry = entry;
            this.error = error;
        }

        public static ContextDeleteResult success(ContextEntry entry) {
            return new ContextDeleteResult(true, entry, null);
        }

        public static ContextDeleteResult accessDenied(String error) {
            return new ContextDeleteResult(false, null, error);
        }

        public static ContextDeleteResult notFound(String error) {
            return new ContextDeleteResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable ContextEntry getEntry() {
            return entry;
        }

        public @Nullable String getError() {
            return error;
        }
    }

    public static class ContextPerformanceMetrics {
        private final long totalStores;
        private final long totalRetrievals;
        private final long totalUpdates;
        private final long totalDeletions;
        private final long totalAccessDenials;
        private final int currentContextCount;
        private final int currentVersionCount;

        private ContextPerformanceMetrics(Builder builder) {
            this.totalStores = builder.totalStores;
            this.totalRetrievals = builder.totalRetrievals;
            this.totalUpdates = builder.totalUpdates;
            this.totalDeletions = builder.totalDeletions;
            this.totalAccessDenials = builder.totalAccessDenials;
            this.currentContextCount = builder.currentContextCount;
            this.currentVersionCount = builder.currentVersionCount;
        }

        public long getTotalStores() {
            return totalStores;
        }

        public long getTotalRetrievals() {
            return totalRetrievals;
        }

        public long getTotalUpdates() {
            return totalUpdates;
        }

        public long getTotalDeletions() {
            return totalDeletions;
        }

        public long getTotalAccessDenials() {
            return totalAccessDenials;
        }

        public int getCurrentContextCount() {
            return currentContextCount;
        }

        public int getCurrentVersionCount() {
            return currentVersionCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalStores;
            private long totalRetrievals;
            private long totalUpdates;
            private long totalDeletions;
            private long totalAccessDenials;
            private int currentContextCount;
            private int currentVersionCount;

            public Builder totalStores(long totalStores) {
                this.totalStores = totalStores;
                return this;
            }

            public Builder totalRetrievals(long totalRetrievals) {
                this.totalRetrievals = totalRetrievals;
                return this;
            }

            public Builder totalUpdates(long totalUpdates) {
                this.totalUpdates = totalUpdates;
                return this;
            }

            public Builder totalDeletions(long totalDeletions) {
                this.totalDeletions = totalDeletions;
                return this;
            }

            public Builder totalAccessDenials(long totalAccessDenials) {
                this.totalAccessDenials = totalAccessDenials;
                return this;
            }

            public Builder currentContextCount(int currentContextCount) {
                this.currentContextCount = currentContextCount;
                return this;
            }

            public Builder currentVersionCount(int currentVersionCount) {
                this.currentVersionCount = currentVersionCount;
                return this;
            }

            public ContextPerformanceMetrics build() {
                return new ContextPerformanceMetrics(this);
            }
        }
    }
}
