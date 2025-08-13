package org.openhab.core.ai.tool.resources;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Per-provider resource usage tracking state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderResourceUsage {
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
