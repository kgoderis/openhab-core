package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Tracks usage metrics for a specific provider/model combination.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ClientUsageInfo {
	private final ModelProviderType providerType;
	private final String modelName;
	private final Map<String, Instant> activeAgents = new ConcurrentHashMap<>();
	private final AtomicLong totalRequests = new AtomicLong(0);
	private final AtomicLong totalTokens = new AtomicLong(0);
	private final AtomicLong totalCost = new AtomicLong(0);
	private final AtomicLong totalResponseTime = new AtomicLong(0);
	private final AtomicLong successfulRequests = new AtomicLong(0);
	private final AtomicLong failedRequests = new AtomicLong(0);
	private final AtomicReference<Instant> lastUsed = new AtomicReference<>(Instant.now());

	public ClientUsageInfo(ModelProviderType providerType, String modelName) {
		this.providerType = providerType;
		this.modelName = modelName;
	}

	public void addAgent(String agentId, Instant time) {
		activeAgents.put(agentId, time);
		lastUsed.set(time);
	}

	public void removeAgent(String agentId) {
		activeAgents.remove(agentId);
	}

	public void recordRequest(int tokens, double cost, long responseTime, boolean success) {
		totalRequests.incrementAndGet();
		totalTokens.addAndGet(tokens);
		totalCost.addAndGet((long) (cost * 1000));
		totalResponseTime.addAndGet(responseTime);
		if (success) {
			successfulRequests.incrementAndGet();
		} else {
			failedRequests.incrementAndGet();
		}
		lastUsed.set(Instant.now());
	}

	public ModelProviderType getProviderType() {
		return providerType;
	}

	public String getModelName() {
		return modelName;
	}

	public Map<String, Instant> getActiveAgents() {
		return new HashMap<>(activeAgents);
	}

	public long getTotalRequests() {
		return totalRequests.get();
	}

	public long getTotalTokens() {
		return totalTokens.get();
	}

	public double getTotalCost() {
		return totalCost.get() / 1000.0;
	}

	public long getTotalResponseTime() {
		return totalResponseTime.get();
	}

	public long getSuccessfulRequests() {
		return successfulRequests.get();
	}

	public long getFailedRequests() {
		return failedRequests.get();
	}

	public Instant getLastUsed() {
		return lastUsed.get();
	}

	public double getSuccessRate() {
		long total = totalRequests.get();
		return total > 0 ? (double) successfulRequests.get() / total : 0.0;
	}

	public double getAverageResponseTime() {
		long total = totalRequests.get();
		return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
	}
}
