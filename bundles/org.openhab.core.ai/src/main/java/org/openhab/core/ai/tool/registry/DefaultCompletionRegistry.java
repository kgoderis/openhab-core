package org.openhab.core.ai.tool.registry;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.api.CompletionRegistry;
import org.openhab.core.ai.tool.completions.api.dto.Completion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * Implementation of CompletionRegistry for MCP Completions.
 *
 * This class manages the registration and discovery of MCP completions,
 * providing access to completion specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultCompletionRegistry implements CompletionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCompletionRegistry.class);

    /** Map of completions by prompt reference. */
    private final Map<String, Completion> completions = new ConcurrentHashMap<>();

    /** Security filtering - completions that should be excluded */
    private final Map<String, Boolean> securityFilters = new ConcurrentHashMap<>();

    /** Performance monitoring */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    @Override
    public void registerCompletion(final Completion completion) {
        String promptReference = completion.getPromptReference();

        // Apply security filtering
        if (isCompletionBlocked(promptReference)) {
            LOGGER.warn("Completion registration blocked by security filter: {}", promptReference);
            return;
        }

        completions.put(promptReference, completion);
        LOGGER.debug("Registered completion: {}", promptReference);
    }

    @Override
    public void unregisterCompletion(final String promptReference) {
        completions.remove(promptReference);
        LOGGER.debug("Unregistered completion: {}", promptReference);
    }

    @Override
    public @Nullable Completion getCompletion(final String promptReference) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            // Apply security filtering
            if (isCompletionBlocked(promptReference)) {
                LOGGER.warn("Completion access blocked by security filter: {}", promptReference);
                failedRequests.incrementAndGet();
                return null;
            }

            Completion completion = completions.get(promptReference);
            if (completion != null) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            return completion;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public Map<String, Completion> getAllCompletions() {
        return completions.entrySet().stream().filter(entry -> !isCompletionBlocked(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public int getCompletionCount() {
        return (int) completions.entrySet().stream().filter(entry -> !isCompletionBlocked(entry.getKey())).count();
    }

    @Override
    public boolean isCompletionRegistered(final String promptReference) {
        return completions.containsKey(promptReference) && !isCompletionBlocked(promptReference);
    }

    @Override
    public McpServerFeatures.SyncCompletionSpecification[] getSyncCompletionSpecifications() {
        try {
            LOGGER.debug("Creating sync completion specifications for {} completions", completions.size());

            // Create completion specifications using MCP SDK builders
            var specs = new ArrayList<McpServerFeatures.SyncCompletionSpecification>();

            for (var entry : getAllCompletions().entrySet()) {
                var completion = entry.getValue();

                try {
                    // Create MCP completion specification using the correct SDK structure
                    var promptReference = new McpSchema.PromptReference(completion.getPromptReference());

                    var syncCompletionSpec = new McpServerFeatures.SyncCompletionSpecification(promptReference,
                            (exchange, request) -> {
                                LOGGER.debug("Handling sync completion for: {}", completion.getPromptReference());

                                return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(
                                        completion.getSuggestions(), completion.getTotal(), completion.hasMore()));
                            });

                    specs.add(syncCompletionSpec);
                    LOGGER.debug("Created sync completion specification for: {}", completion.getPromptReference());

                } catch (Exception e) {
                    LOGGER.error("Error creating sync completion specification for: {}",
                            completion.getPromptReference(), e);
                }
            }

            LOGGER.debug("Created {} sync completion specifications", specs.size());
            return specs.toArray(new McpServerFeatures.SyncCompletionSpecification[0]);

        } catch (Exception e) {
            LOGGER.error("Error creating sync completion specifications", e);
            return new McpServerFeatures.SyncCompletionSpecification[0];
        }
    }

    @Override
    public McpServerFeatures.AsyncCompletionSpecification[] getAsyncCompletionSpecifications() {
        try {
            LOGGER.debug("Creating async completion specifications for {} completions", completions.size());

            // Create completion specifications using MCP SDK builders
            var specs = new ArrayList<McpServerFeatures.AsyncCompletionSpecification>();

            for (var entry : getAllCompletions().entrySet()) {
                var completion = entry.getValue();

                try {
                    // Create MCP completion specification using the correct SDK structure
                    var promptReference = new McpSchema.PromptReference(completion.getPromptReference());

                    var asyncCompletionSpec = new McpServerFeatures.AsyncCompletionSpecification(promptReference,
                            (exchange, request) -> {
                                LOGGER.debug("Handling async completion for: {}", completion.getPromptReference());

                                return Mono.fromCallable(() -> {
                                    return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(
                                            completion.getSuggestions(), completion.getTotal(), completion.hasMore()));
                                });
                            });

                    specs.add(asyncCompletionSpec);
                    LOGGER.debug("Created async completion specification for: {}", completion.getPromptReference());

                } catch (Exception e) {
                    LOGGER.error("Error creating async completion specification for: {}",
                            completion.getPromptReference(), e);
                }
            }

            LOGGER.debug("Created {} async completion specifications", specs.size());
            return specs.toArray(new McpServerFeatures.AsyncCompletionSpecification[0]);

        } catch (Exception e) {
            LOGGER.error("Error creating async completion specifications", e);
            return new McpServerFeatures.AsyncCompletionSpecification[0];
        }
    }

    @Override
    public Map<String, Object>[] getCompletionDescriptors() {
        try {
            var list = new ArrayList<Map<String, Object>>();
            for (var entry : getAllCompletions().entrySet()) {
                var c = entry.getValue();
                list.add(Map.of("promptReference", c.getPromptReference(), "description", c.getDescription(),
                        "suggestions", c.getSuggestions(), "total", c.getTotal(), "hasMore", c.hasMore()));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object>[] arr = list.toArray(new Map[0]);
            return arr;
        } catch (Exception e) {
            LOGGER.warn("Failed to build completion descriptors", e);
            return new Map[0];
        }
    }

    /**
     * Check if a completion is blocked by security filters.
     *
     * @param promptReference the prompt reference
     * @return true if the completion is blocked
     */
    private boolean isCompletionBlocked(String promptReference) {
        return securityFilters.containsKey(promptReference) && securityFilters.get(promptReference);
    }

    /**
     * Add a security filter to block a completion.
     *
     * @param promptReference the prompt reference to block
     */
    public void blockCompletion(String promptReference) {
        securityFilters.put(promptReference, true);
        LOGGER.info("Completion blocked by security filter: {}", promptReference);
    }

    /**
     * Remove a security filter for a completion.
     *
     * @param promptReference the prompt reference to unblock
     */
    public void unblockCompletion(String promptReference) {
        securityFilters.remove(promptReference);
        LOGGER.info("Completion unblocked: {}", promptReference);
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
