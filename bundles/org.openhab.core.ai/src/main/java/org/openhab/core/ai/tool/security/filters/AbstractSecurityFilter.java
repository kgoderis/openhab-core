package org.openhab.core.ai.tool.security.filters;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SecurityFilter with authentication filter logic,
 * multiple authentication methods, caching, and metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractSecurityFilter implements SecurityFilter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSecurityFilter.class);

    protected final String filterId;
    protected final String filterName;
    protected final int priority;
    protected boolean enabled = true;
    protected final Map<String, Object> configuration = new ConcurrentHashMap<>();

    // Authentication caching
    protected final Map<String, CachedAuthResult> authCache = new ConcurrentHashMap<>();
    protected final long cacheExpirationMs = 300000; // 5 minutes

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable MetricsService metricsService;

    protected AbstractSecurityFilter(String filterId, String filterName, int priority) {
        this.filterId = filterId;
        this.filterName = filterName;
        this.priority = priority;
    }

    @Override
    public String getFilterId() {
        return filterId;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new ConcurrentHashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
    }

    @Override
    public SecurityResult authenticate(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        long executionTime = 0;

        try {
            if (!enabled) {
                executionTime = System.currentTimeMillis() - startTime;
                return SecurityResult.success("Filter disabled");
            }

            String cacheKey = generateCacheKey(request);
            CachedAuthResult cached = authCache.get(cacheKey);

            if (cached != null && !cached.isExpired()) {
                // Cache hit
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("security-filter", "cache-hit").withSuccess(true)
                                .withDuration(0L).withData("filterId", filterId).withData("filterName", filterName)
                                .record();
                    } catch (Exception e) {
                        logger.warn("Failed to record cache hit metrics", e);
                    }
                }
                return cached.result;
            }

            // Cache miss - perform authentication
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("security-filter", "cache-miss").withSuccess(true).withDuration(0L)
                            .withData("filterId", filterId).withData("filterName", filterName).record();
                } catch (Exception e) {
                    logger.warn("Failed to record cache miss metrics", e);
                }
            }

            SecurityResult result = performAuthentication(request);
            executionTime = System.currentTimeMillis() - startTime;

            if (result.isSuccess()) {
                success = true;
                authCache.put(cacheKey, new CachedAuthResult(result, System.currentTimeMillis() + cacheExpirationMs));
            }

            return result;

        } catch (Exception e) {
            executionTime = System.currentTimeMillis() - startTime;
            logger.error("Authentication error in filter: {}", filterId, e);
            return SecurityResult.failure("Authentication error: " + e.getMessage());
        } finally {
            // Record authentication metrics using centralized MetricsService
            if (metricsService != null) {
                try {
                    Map<String, Object> context = Map.of(
                        "filterId", filterId,
                        "filterName", filterName,
                        "enabled", enabled,
                        "cacheSize", authCache.size()
                    );
                    metricsService.recordOperationWithData("security-filter", "authentication", success,
                            Duration.ofMillis(executionTime), context);
                } catch (Exception e) {
                    logger.warn("Failed to record authentication metrics", e);
                }
            }
        }
    }

    protected String generateCacheKey(Map<String, Object> request) {
        return request.toString();
    }

    protected abstract SecurityResult performAuthentication(Map<String, Object> request);

    /**
     * Get authentication metrics.
     * 
     * @return authentication metrics
     * @deprecated Use MetricsService to get AuthStatistics instead
     */
    @Deprecated
    public Object getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", 0L);
        metrics.put("successfulAuthentications", 0L);
        metrics.put("failedAuthentications", 0L);
        metrics.put("cacheHits", 0L);
        metrics.put("cacheMisses", 0L);
        metrics.put("cacheSize", authCache.size());

        if (metricsService != null) {
            try {
                // Get authentication metrics
                MetricKey authKey = MetricKeys.execution("authentication");
                ExecutionMetricsSnapshot authSnapshot = metricsService.getSnapshot(authKey,
                        ExecutionMetricsSnapshot.class);
                if (authSnapshot != null) {
                    metrics.put("totalRequests", authSnapshot.total());
                    metrics.put("successfulAuthentications", authSnapshot.success());
                    metrics.put("failedAuthentications", authSnapshot.failure());
                }

                // Get cache hit metrics
                MetricKey cacheHitKey = MetricKeys.execution("cache-hit");
                ExecutionMetricsSnapshot cacheHitSnapshot = metricsService.getSnapshot(cacheHitKey,
                        ExecutionMetricsSnapshot.class);
                if (cacheHitSnapshot != null) {
                    metrics.put("cacheHits", cacheHitSnapshot.total());
                }

                // Get cache miss metrics
                MetricKey cacheMissKey = MetricKeys.execution("cache-miss");
                ExecutionMetricsSnapshot cacheMissSnapshot = metricsService.getSnapshot(cacheMissKey,
                        ExecutionMetricsSnapshot.class);
                if (cacheMissSnapshot != null) {
                    metrics.put("cacheMisses", cacheMissSnapshot.total());
                }

            } catch (Exception e) {
                logger.warn("Failed to retrieve metrics from MetricsService", e);
            }
        }

        return metrics;
    }

    public void clearCache() {
        authCache.clear();
    }
}
