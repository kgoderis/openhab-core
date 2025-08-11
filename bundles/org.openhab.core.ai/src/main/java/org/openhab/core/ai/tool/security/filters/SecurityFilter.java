package org.openhab.core.ai.tool.security.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication filter for MCP tool security.
 * 
 * This interface defines the contract for authentication filters that
 * validate and authenticate requests to the MCP tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SecurityFilter {

    /**
     * Get the filter ID.
     * 
     * @return the filter ID
     */
    String getFilterId();

    /**
     * Get the filter name.
     * 
     * @return the filter name
     */
    String getFilterName();

    /**
     * Get the filter priority.
     * 
     * @return the filter priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the filter is enabled.
     * 
     * @return true if the filter is enabled
     */
    boolean isEnabled();

    /**
     * Authenticate a request.
     * 
     * @param request the request to authenticate
     * @return authentication result
     */
    SecurityResult authenticate(Map<String, Object> request);

    /**
     * Get the filter configuration.
     * 
     * @return the filter configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the filter configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Default implementation of SecurityFilter with authentication filter logic,
     * multiple authentication methods, caching, and metrics.
     */
    abstract class AbstractSecurityFilter implements SecurityFilter {

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

            // Check cache first
            String cacheKey = generateCacheKey(request);
            CachedAuthResult cached = authCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                cacheHits.incrementAndGet();
                return cached.result;
            }

            cacheMisses.incrementAndGet();

            // Perform actual authentication
            SecurityResult result = performAuthentication(request);

            // Cache the result
            if (result.isSuccess()) {
                successfulAuthentications.incrementAndGet();
                authCache.put(cacheKey, new CachedAuthResult(result, System.currentTimeMillis() + cacheExpirationMs));
            } else {
                failedAuthentications.incrementAndGet();
            }

            return result;
        }

        /**
         * Generate a cache key for the request
         */
        protected String generateCacheKey(Map<String, Object> request) {
            // Simple cache key generation - can be overridden for more sophisticated caching
            return request.toString();
        }

        /**
         * Perform the actual authentication logic
         */
        protected abstract SecurityResult performAuthentication(Map<String, Object> request);

        /**
         * Get authentication metrics
         */
        public AuthMetrics getMetrics() {
            return new AuthMetrics(totalRequests.get(), successfulAuthentications.get(), failedAuthentications.get(),
                    cacheHits.get(), cacheMisses.get(), authCache.size());
        }

        /**
         * Clear the authentication cache
         */
        public void clearCache() {
            authCache.clear();
        }

        /**
         * Cached authentication result
         */
        protected static class CachedAuthResult {
            final SecurityResult result;
            final long expirationTime;

            CachedAuthResult(SecurityResult result, long expirationTime) {
                this.result = result;
                this.expirationTime = expirationTime;
            }

            boolean isExpired() {
                return System.currentTimeMillis() > expirationTime;
            }
        }

        /**
         * Authentication metrics
         */
        public static class AuthMetrics {
            public final long totalRequests;
            public final long successfulAuthentications;
            public final long failedAuthentications;
            public final long cacheHits;
            public final long cacheMisses;
            public final int cacheSize;

            public AuthMetrics(long totalRequests, long successfulAuthentications, long failedAuthentications,
                    long cacheHits, long cacheMisses, int cacheSize) {
                this.totalRequests = totalRequests;
                this.successfulAuthentications = successfulAuthentications;
                this.failedAuthentications = failedAuthentications;
                this.cacheHits = cacheHits;
                this.cacheMisses = cacheMisses;
                this.cacheSize = cacheSize;
            }

            public double getSuccessRate() {
                return totalRequests > 0 ? (double) successfulAuthentications / totalRequests : 0.0;
            }

            public double getCacheHitRate() {
                long totalCacheAccess = cacheHits + cacheMisses;
                return totalCacheAccess > 0 ? (double) cacheHits / totalCacheAccess : 0.0;
            }
        }
    }
}
