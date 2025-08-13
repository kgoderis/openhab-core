package org.openhab.core.ai.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Aggregated usage stats per provider across models and agents.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderUsageStats {
	private final ModelProviderType providerType;
	private final Map<String, Integer> modelUsage = new ConcurrentHashMap<>();
	private final Map<String, Integer> agentUsage = new ConcurrentHashMap<>();
	private final AtomicLong totalRequests = new AtomicLong(0);
	private final AtomicLong totalTokens = new AtomicLong(0);
	private final AtomicLong totalCost = new AtomicLong(0);
	private final AtomicLong totalResponseTime = new AtomicLong(0);
	private final AtomicLong successfulRequests = new AtomicLong(0);
	private final AtomicLong failedRequests = new AtomicLong(0);

	public ProviderUsageStats(ModelProviderType providerType) {
		this.providerType = providerType;
	}

	public void recordUsage(String agentId, String modelName) {
		modelUsage.merge(modelName, 1, Integer::sum);
		agentUsage.merge(agentId, 1, Integer::sum);
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
	}

	public ModelProviderType getProviderType() {
		return providerType;
	}

	public Map<String, Integer> getModelUsage() {
		return new HashMap<>(modelUsage);
	}

	public Map<String, Integer> getAgentUsage() {
		return new HashMap<>(agentUsage);
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

	public double getSuccessRate() {
		long total = totalRequests.get();
		return total > 0 ? (double) successfulRequests.get() / total : 0.0;
	}

	public double getAverageResponseTime() {
		long total = totalRequests.get();
		return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
	}
}
