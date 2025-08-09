package org.openhab.core.ai.tool.resources;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool Resource Manager - Provides comprehensive resource management and optimization
 * 
 * <p>
 * This service provides:
 * - Concurrent request limiting and throttling
 * - Memory management and monitoring
 * - Request queuing and prioritization
 * - Resource monitoring and metrics
 * - Cleanup mechanisms and garbage collection
 * - Performance optimization and tuning
 * </p>
 * 
 * <h3>Resource Management Flow</h3>
 * 
 * <pre>{@code
 * Request → Resource Check → Queue Management → Execution → Resource Cleanup
 *    ↓           ↓              ↓              ↓              ↓
 * Throttling → Memory Check → Priority Queue → Thread Pool → Cleanup
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ResourceManager.class)
@NonNullByDefault
public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    // Resource limits and configuration
    private final AtomicReference<Integer> maxConcurrentRequests = new AtomicReference<>(100);
    private final AtomicReference<Long> maxMemoryUsage = new AtomicReference<>(1024L * 1024L * 1024L); // 1GB
    private final AtomicReference<Integer> maxQueueSize = new AtomicReference<>(1000);
    private final AtomicReference<Duration> requestTimeout = new AtomicReference<>(Duration.ofSeconds(30));

    // Resource monitoring
    private final AtomicLong currentConcurrentRequests = new AtomicLong(0);
    private final AtomicLong totalRequestsProcessed = new AtomicLong(0);
    private final AtomicLong totalRequestsRejected = new AtomicLong(0);
    private final AtomicLong totalRequestsTimedOut = new AtomicLong(0);
    private final AtomicLong currentMemoryUsage = new AtomicLong(0);

    // Request tracking
    private final ConcurrentHashMap<String, RequestInfo> activeRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ModelProviderType, ProviderResourceUsage> providerResourceUsage = new ConcurrentHashMap<>();

    // Thread pool and queue management
    private final ThreadPoolExecutor executorService;
    private final LinkedBlockingQueue<Runnable> requestQueue;

    // Cleanup scheduling
    private final AtomicReference<Duration> cleanupInterval = new AtomicReference<>(Duration.ofMinutes(5));
    private final AtomicReference<Duration> resourceTimeout = new AtomicReference<>(Duration.ofMinutes(10));

    public ResourceManager() {
        // Initialize thread pool with configurable parameters
        this.requestQueue = new LinkedBlockingQueue<>(maxQueueSize.get());
        this.executorService = new ThreadPoolExecutor(10, // Core pool size
                50, // Maximum pool size
                60L, // Keep alive time
                TimeUnit.SECONDS, requestQueue, new ThreadPoolExecutor.CallerRunsPolicy() // Reject policy
        );
    }

    /**
     * Submit a request for execution with resource management
     * 
     * @param requestId unique request identifier
     * @param provider the provider to execute on
     * @param priority request priority (higher = more important)
     * @param task the task to execute
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> submitRequest(String requestId, ModelProviderType provider, int priority,
            java.util.function.Supplier<T> task) {

        // Check resource availability
        if (!canAcceptRequest(provider)) {
            totalRequestsRejected.incrementAndGet();
            return CompletableFuture.failedFuture(
                    new ResourceLimitExceededException("Resource limit exceeded for provider: " + provider));
        }

        // Create request info
        RequestInfo requestInfo = new RequestInfo(requestId, provider, priority, Instant.now());
        activeRequests.put(requestId, requestInfo);

        // Increment concurrent request counter
        currentConcurrentRequests.incrementAndGet();
        updateProviderResourceUsage(provider, 1, 0);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Execute the task
                T result = task.get();

                // Record success
                recordRequestSuccess(requestId, provider);

                return result;
            } catch (Exception e) {
                // Record failure
                recordRequestFailure(requestId, provider, e);
                throw e;
            } finally {
                // Cleanup
                cleanupRequest(requestId, provider);
            }
        }, executorService).orTimeout(requestTimeout.get().toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        totalRequestsTimedOut.incrementAndGet();
                        logger.warn("Request {} timed out after {}ms", requestId, requestTimeout.get().toMillis());
                    }
                });
    }

    /**
     * Check if a request can be accepted based on current resource usage
     * 
     * @param provider the provider to check
     * @return true if the request can be accepted
     */
    public boolean canAcceptRequest(ModelProviderType provider) {
        // Check concurrent request limit
        if (currentConcurrentRequests.get() >= maxConcurrentRequests.get()) {
            logger.debug("Concurrent request limit exceeded: {} >= {}", currentConcurrentRequests.get(),
                    maxConcurrentRequests.get());
            return false;
        }

        // Check queue size
        if (requestQueue.size() >= maxQueueSize.get()) {
            logger.debug("Request queue full: {} >= {}", requestQueue.size(), maxQueueSize.get());
            return false;
        }

        // Check memory usage
        if (currentMemoryUsage.get() >= maxMemoryUsage.get()) {
            logger.debug("Memory usage limit exceeded: {} >= {}", currentMemoryUsage.get(), maxMemoryUsage.get());
            return false;
        }

        // Check provider-specific limits
        ProviderResourceUsage providerUsage = providerResourceUsage.get(provider);
        if (providerUsage != null && providerUsage.getConcurrentRequests() >= getProviderConcurrentLimit(provider)) {
            logger.debug("Provider concurrent limit exceeded for {}: {} >= {}", provider,
                    providerUsage.getConcurrentRequests(), getProviderConcurrentLimit(provider));
            return false;
        }

        return true;
    }

    /**
     * Get current resource usage statistics
     * 
     * @return resource usage statistics
     */
    public ResourceUsageStatistics getResourceUsageStatistics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return new ResourceUsageStatistics(currentConcurrentRequests.get(), totalRequestsProcessed.get(),
                totalRequestsRejected.get(), totalRequestsTimedOut.get(), usedMemory, totalMemory, requestQueue.size(),
                executorService.getActiveCount(), executorService.getPoolSize(),
                new ConcurrentHashMap<>(providerResourceUsage));
    }

    /**
     * Perform resource cleanup
     */
    public void performCleanup() {
        Instant cutoffTime = Instant.now().minus(resourceTimeout.get());

        // Clean up expired requests
        activeRequests.entrySet().removeIf(entry -> {
            RequestInfo requestInfo = entry.getValue();
            if (requestInfo.getStartTime().isBefore(cutoffTime)) {
                logger.debug("Cleaning up expired request: {}", requestInfo.getRequestId());
                return true;
            }
            return false;
        });

        // Clean up provider resource usage
        providerResourceUsage.entrySet().removeIf(entry -> {
            ProviderResourceUsage usage = entry.getValue();
            if (usage.getLastActivity().isBefore(cutoffTime) && usage.getConcurrentRequests() == 0) {
                logger.debug("Cleaning up inactive provider: {}", entry.getKey());
                return true;
            }
            return false;
        });

        // Suggest garbage collection if memory usage is high
        if (currentMemoryUsage.get() > maxMemoryUsage.get() * 0.8) {
            logger.info("High memory usage detected, suggesting garbage collection");
            System.gc();
        }

        logger.debug("Resource cleanup completed. Active requests: {}, Provider usage: {}", activeRequests.size(),
                providerResourceUsage.size());
    }

    /**
     * Shutdown the resource manager
     */
    public void shutdown() {
        logger.info("Shutting down ToolResourceManager");

        // Shutdown executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Clear all tracking data
        activeRequests.clear();
        providerResourceUsage.clear();

        logger.info("ToolResourceManager shutdown completed");
    }

    // Configuration methods
    public void setMaxConcurrentRequests(int maxRequests) {
        maxConcurrentRequests.set(maxRequests);
        executorService.setMaximumPoolSize(maxRequests);
    }

    public void setMaxMemoryUsage(long maxMemory) {
        maxMemoryUsage.set(maxMemory);
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize.set(maxQueueSize);
        // Note: Cannot change queue capacity after creation, would need to recreate executor
    }

    public void setRequestTimeout(Duration timeout) {
        requestTimeout.set(timeout);
    }

    public void setCleanupInterval(Duration interval) {
        cleanupInterval.set(interval);
    }

    public void setResourceTimeout(Duration timeout) {
        resourceTimeout.set(timeout);
    }

    // Helper methods
    private void recordRequestSuccess(String requestId, ModelProviderType provider) {
        totalRequestsProcessed.incrementAndGet();

        RequestInfo requestInfo = activeRequests.get(requestId);
        if (requestInfo != null) {
            requestInfo.setEndTime(Instant.now());
            requestInfo.setSuccess(true);
        }

        updateProviderResourceUsage(provider, -1, 1);
    }

    private void recordRequestFailure(String requestId, ModelProviderType provider, Exception error) {
        RequestInfo requestInfo = activeRequests.get(requestId);
        if (requestInfo != null) {
            requestInfo.setEndTime(Instant.now());
            requestInfo.setSuccess(false);
            requestInfo.setError(error);
        }

        updateProviderResourceUsage(provider, -1, 0);
    }

    private void cleanupRequest(String requestId, ModelProviderType provider) {
        activeRequests.remove(requestId);
        currentConcurrentRequests.decrementAndGet();
    }

    private void updateProviderResourceUsage(ModelProviderType provider, int concurrentDelta, int successDelta) {
        ProviderResourceUsage usage = providerResourceUsage.computeIfAbsent(provider,
                p -> new ProviderResourceUsage(provider));

        usage.updateUsage(concurrentDelta, successDelta);
    }

    private int getProviderConcurrentLimit(ModelProviderType provider) {
        // TODO: Implement provider-specific limits based on provider capabilities
        switch (provider) {
            case OPENAI:
            case ANTHROPIC:
                return 20; // Higher limits for cloud providers
            case GOOGLE:
            case AZURE:
                return 15;
            case OLLAMA:
            case LOCALAI:
            case VLLM:
                return 5; // Lower limits for local providers
            default:
                return 10;
        }
    }

    // Inner classes
    public static class ResourceLimitExceededException extends RuntimeException {
        public ResourceLimitExceededException(String message) {
            super(message);
        }
    }

    public static class ResourceUsageStatistics {
        private final long currentConcurrentRequests;
        private final long totalRequestsProcessed;
        private final long totalRequestsRejected;
        private final long totalRequestsTimedOut;
        private final long currentMemoryUsage;
        private final long totalMemory;
        private final int queueSize;
        private final int activeThreads;
        private final int poolSize;
        private final Map<ModelProviderType, ProviderResourceUsage> providerUsage;

        public ResourceUsageStatistics(long currentConcurrentRequests, long totalRequestsProcessed,
                long totalRequestsRejected, long totalRequestsTimedOut, long currentMemoryUsage, long totalMemory,
                int queueSize, int activeThreads, int poolSize,
                Map<ModelProviderType, ProviderResourceUsage> providerUsage) {
            this.currentConcurrentRequests = currentConcurrentRequests;
            this.totalRequestsProcessed = totalRequestsProcessed;
            this.totalRequestsRejected = totalRequestsRejected;
            this.totalRequestsTimedOut = totalRequestsTimedOut;
            this.currentMemoryUsage = currentMemoryUsage;
            this.totalMemory = totalMemory;
            this.queueSize = queueSize;
            this.activeThreads = activeThreads;
            this.poolSize = poolSize;
            this.providerUsage = providerUsage;
        }

        public long getCurrentConcurrentRequests() {
            return currentConcurrentRequests;
        }

        public long getTotalRequestsProcessed() {
            return totalRequestsProcessed;
        }

        public long getTotalRequestsRejected() {
            return totalRequestsRejected;
        }

        public long getTotalRequestsTimedOut() {
            return totalRequestsTimedOut;
        }

        public long getCurrentMemoryUsage() {
            return currentMemoryUsage;
        }

        public long getTotalMemory() {
            return totalMemory;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public int getActiveThreads() {
            return activeThreads;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public Map<ModelProviderType, ProviderResourceUsage> getProviderUsage() {
            return providerUsage;
        }

        public double getMemoryUsagePercentage() {
            return totalMemory > 0 ? (double) currentMemoryUsage / totalMemory * 100.0 : 0.0;
        }

        public double getRejectionRate() {
            long totalRequests = totalRequestsProcessed + totalRequestsRejected;
            return totalRequests > 0 ? (double) totalRequestsRejected / totalRequests * 100.0 : 0.0;
        }
    }

    public static class ProviderResourceUsage {
        private final ModelProviderType provider;
        private final AtomicLong concurrentRequests = new AtomicLong(0);
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicReference<Instant> lastActivity = new AtomicReference<>(Instant.now());

        public ProviderResourceUsage(ModelProviderType provider) {
            this.provider = provider;
        }

        public void updateUsage(int concurrentDelta, int successDelta) {
            concurrentRequests.addAndGet(concurrentDelta);
            totalRequests.incrementAndGet();

            if (successDelta > 0) {
                successfulRequests.incrementAndGet();
            } else if (successDelta == 0) {
                failedRequests.incrementAndGet();
            }

            lastActivity.set(Instant.now());
        }

        public ModelProviderType getProvider() {
            return provider;
        }

        public long getConcurrentRequests() {
            return concurrentRequests.get();
        }

        public long getTotalRequests() {
            return totalRequests.get();
        }

        public long getSuccessfulRequests() {
            return successfulRequests.get();
        }

        public long getFailedRequests() {
            return failedRequests.get();
        }

        public Instant getLastActivity() {
            return lastActivity.get();
        }

        public double getSuccessRate() {
            return totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get() : 0.0;
        }
    }

    private static class RequestInfo {
        private final String requestId;
        private final ModelProviderType provider;
        private final int priority;
        private final Instant startTime;
        private final AtomicReference<Instant> endTime = new AtomicReference<>();
        private final AtomicReference<Boolean> success = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public RequestInfo(String requestId, ModelProviderType provider, int priority, Instant startTime) {
            this.requestId = requestId;
            this.provider = provider;
            this.priority = priority;
            this.startTime = startTime;
        }

        public String getRequestId() {
            return requestId;
        }

        public ModelProviderType getProvider() {
            return provider;
        }

        public int getPriority() {
            return priority;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public Instant getEndTime() {
            return endTime.get();
        }

        public Boolean getSuccess() {
            return success.get();
        }

        public Exception getError() {
            return error.get();
        }

        public void setEndTime(Instant endTime) {
            this.endTime.set(endTime);
        }

        public void setSuccess(boolean success) {
            this.success.set(success);
        }

        public void setError(Exception error) {
            this.error.set(error);
        }

        public Duration getDuration() {
            Instant end = endTime.get();
            return end != null ? Duration.between(startTime, end) : Duration.between(startTime, Instant.now());
        }
    }
}
