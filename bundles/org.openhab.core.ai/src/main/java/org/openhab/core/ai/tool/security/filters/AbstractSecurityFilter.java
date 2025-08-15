package org.openhab.core.ai.tool.security.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default implementation of SecurityFilter with authentication filter logic,
 * multiple authentication methods, caching, and metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractSecurityFilter implements SecurityFilter {

    protected final String filterId;
    protected final String filterName;
    protected final int priority;
    protected boolean enabled = true;
    protected final Map<String, Object> configuration = new ConcurrentHashMap<>();

    // Authentication caching
    protected final Map<String, CachedAuthResult> authCache = new ConcurrentHashMap<>();
    protected final long cacheExpirationMs = 300000; // 5 minutes

    // Authentication metrics
    protected final AtomicLong totalRequests = new AtomicLong(0);
    protected final AtomicLong successfulAuthentications = new AtomicLong(0);
    protected final AtomicLong failedAuthentications = new AtomicLong(0);
    protected final AtomicLong cacheHits = new AtomicLong(0);
    protected final AtomicLong cacheMisses = new AtomicLong(0);

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
        totalRequests.incrementAndGet();
        if (!enabled) {
            return SecurityResult.success("Filter disabled");
        }
        String cacheKey = generateCacheKey(request);
        CachedAuthResult cached = authCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            cacheHits.incrementAndGet();
            return cached.result;
        }
        cacheMisses.incrementAndGet();
        SecurityResult result = performAuthentication(request);
        if (result.isSuccess()) {
            successfulAuthentications.incrementAndGet();
            authCache.put(cacheKey, new CachedAuthResult(result, System.currentTimeMillis() + cacheExpirationMs));
        } else {
            failedAuthentications.incrementAndGet();
        }
        return result;
    }

    protected String generateCacheKey(Map<String, Object> request) {
        return request.toString();
    }

    protected abstract SecurityResult performAuthentication(Map<String, Object> request);

    public AuthMetrics getMetrics() {
        return new AuthMetrics(totalRequests.get(), successfulAuthentications.get(), failedAuthentications.get(),
                cacheHits.get(), cacheMisses.get(), authCache.size());
    }

    public void clearCache() {
        authCache.clear();
    }
}
