package org.openhab.core.ai.tool.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.dto.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Implementation of PromptRegistry for MCP Prompts.
 *
 * This class manages the registration and discovery of MCP prompts,
 * providing access to prompt specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultPromptRegistry implements PromptRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPromptRegistry.class);

    /** Map of prompts by name. */
    private final Map<String, Prompt> prompts = new ConcurrentHashMap<>();

    /** Security filtering - prompts that should be excluded */
    private final Map<String, Boolean> securityFilters = new ConcurrentHashMap<>();

    /** Performance monitoring */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    @Override
    public void registerPrompt(final Prompt prompt) {
        String name = prompt.getName();

        // Apply security filtering
        if (isPromptBlocked(name)) {
            LOGGER.warn("Prompt registration blocked by security filter: {}", name);
            return;
        }

        prompts.put(name, prompt);
        LOGGER.debug("Registered prompt: {}", name);
    }

    @Override
    public void unregisterPrompt(final String name) {
        prompts.remove(name);
        LOGGER.debug("Unregistered prompt: {}", name);
    }

    @Override
    public @Nullable Prompt getPrompt(final String name) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            // Apply security filtering
            if (isPromptBlocked(name)) {
                LOGGER.warn("Prompt access blocked by security filter: {}", name);
                failedRequests.incrementAndGet();
                return null;
            }

            Prompt prompt = prompts.get(name);
            if (prompt != null) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            return prompt;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public Map<String, Prompt> getAllPrompts() {
        return prompts.entrySet().stream().filter(entry -> !isPromptBlocked(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public int getPromptCount() {
        return (int) prompts.entrySet().stream().filter(entry -> !isPromptBlocked(entry.getKey())).count();
    }

    @Override
    public boolean isPromptRegistered(final String name) {
        return prompts.containsKey(name) && !isPromptBlocked(name);
    }

    @Override
    public McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications() {
        try {
            LOGGER.debug("Creating sync prompt specifications for {} prompts", prompts.size());

            // TODO: Implement actual MCP prompt specification creation
            // For now, return empty array until MCP SDK integration is properly implemented
            // The MCP SDK needs to provide proper builders for prompt specifications
            LOGGER.debug("Returning empty sync prompt specifications (MCP SDK integration pending)");
            return new McpServerFeatures.SyncPromptSpecification[0];

        } catch (Exception e) {
            LOGGER.error("Error creating sync prompt specifications", e);
            return new McpServerFeatures.SyncPromptSpecification[0];
        }
    }

    @Override
    public McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications() {
        try {
            LOGGER.debug("Creating async prompt specifications for {} prompts", prompts.size());

            // TODO: Implement actual MCP prompt specification creation
            // For now, return empty array until MCP SDK integration is properly implemented
            // The MCP SDK needs to provide proper builders for prompt specifications
            LOGGER.debug("Returning empty async prompt specifications (MCP SDK integration pending)");
            return new McpServerFeatures.AsyncPromptSpecification[0];

        } catch (Exception e) {
            LOGGER.error("Error creating async prompt specifications", e);
            return new McpServerFeatures.AsyncPromptSpecification[0];
        }
    }

    @Override
    public Map<String, Object>[] getPromptDescriptors() {
        try {
            var list = new java.util.ArrayList<Map<String, Object>>();
            for (var entry : getAllPrompts().entrySet()) {
                var p = entry.getValue();
                var args = new java.util.ArrayList<Map<String, Object>>();
                for (var a : p.getArguments()) {
                    args.add(
                            Map.of("name", a.getName(), "description", a.getDescription(), "required", a.isRequired()));
                }
                list.add(Map.of("name", p.getName(), "description", p.getDescription(), "arguments", args));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object>[] arr = list.toArray(new Map[0]);
            return arr;
        } catch (Exception e) {
            LOGGER.warn("Failed to build prompt descriptors", e);
            return new Map[0];
        }
    }

    /**
     * Check if a prompt is blocked by security filters.
     *
     * @param name the prompt name
     * @return true if the prompt is blocked
     */
    private boolean isPromptBlocked(String name) {
        return securityFilters.containsKey(name) && securityFilters.get(name);
    }

    /**
     * Add a security filter to block a prompt.
     *
     * @param name the prompt name to block
     */
    public void blockPrompt(String name) {
        securityFilters.put(name, true);
        LOGGER.info("Prompt blocked by security filter: {}", name);
    }

    /**
     * Remove a security filter for a prompt.
     *
     * @param name the prompt name to unblock
     */
    public void unblockPrompt(String name) {
        securityFilters.remove(name);
        LOGGER.info("Prompt unblocked: {}", name);
    }

    /**
     * Get performance metrics.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        metrics.put("successRate",
                totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get() : 0.0);
        return metrics;
    }
}
