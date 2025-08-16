package org.openhab.core.ai.tool.completions;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.completions.api.specification.CompletionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * Completion Adapter Bridge for MCP SDK integration
 *
 * Bridges internal completion adapters/specifications to MCP SDK completion specifications.
 * Provides mapping between internal completion specifications and MCP protocol specifications.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class CompletionAdapterBridge {

    private static final Logger logger = LoggerFactory.getLogger(CompletionAdapterBridge.class);

    public static McpServerFeatures.SyncCompletionSpecification createSyncCompletionSpecification(
            CompletionSpecification internalSpec) {
        try {
            logger.debug("Creating sync completion specification for: {}", internalSpec.getId());

            // Create MCP prompt reference from internal specification
            String promptReference = internalSpec.getPromptReference();
            if (promptReference == null) {
                logger.warn("No prompt reference found for completion: {}", internalSpec.getId());
                return null;
            }

            McpSchema.PromptReference mcpPromptReference = new McpSchema.PromptReference(promptReference);

            // Create sync completion specification
            McpServerFeatures.SyncCompletionSpecification spec = new McpServerFeatures.SyncCompletionSpecification(
                    mcpPromptReference, (exchange, request) -> {
                        logger.debug("Handling sync completion for: {}", internalSpec.getId());

                        try {
                            // Get suggestions from internal specification
                            Map<String, Object> suggestionsConfig = internalSpec.getSuggestionsConfig();
                            List<String> suggestions = List.of(); // Default empty list
                            int total = 0;
                            boolean hasMore = false;

                            if (suggestionsConfig != null) {
                                // Extract suggestions from configuration
                                @SuppressWarnings("unchecked")
                                List<String> configSuggestions = (List<String>) suggestionsConfig.get("suggestions");
                                if (configSuggestions != null) {
                                    suggestions = configSuggestions;
                                }

                                // Extract metadata
                                total = ((Number) suggestionsConfig.getOrDefault("total", 0)).intValue();
                                hasMore = (Boolean) suggestionsConfig.getOrDefault("hasMore", false);
                            }

                            // Create completion result
                            McpSchema.CompleteResult.CompleteCompletion completion = new McpSchema.CompleteResult.CompleteCompletion(
                                    suggestions, total, hasMore);

                            return new McpSchema.CompleteResult(completion);

                        } catch (Exception e) {
                            logger.error("Error handling sync completion for: {}", internalSpec.getId(), e);
                            return new McpSchema.CompleteResult(
                                    new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false));
                        }
                    });

            logger.debug("Successfully created sync completion specification for: {}", internalSpec.getId());
            return spec;

        } catch (Exception e) {
            logger.error("Failed to create sync completion specification for: {}", internalSpec.getId(), e);
            return null;
        }
    }

    public static McpServerFeatures.AsyncCompletionSpecification createAsyncCompletionSpecification(
            CompletionSpecification internalSpec) {
        try {
            logger.debug("Creating async completion specification for: {}", internalSpec.getId());

            // Create MCP prompt reference from internal specification
            String promptReference = internalSpec.getPromptReference();
            if (promptReference == null) {
                logger.warn("No prompt reference found for completion: {}", internalSpec.getId());
                return null;
            }

            McpSchema.PromptReference mcpPromptReference = new McpSchema.PromptReference(promptReference);

            // Create async completion specification
            McpServerFeatures.AsyncCompletionSpecification spec = new McpServerFeatures.AsyncCompletionSpecification(
                    mcpPromptReference, (exchange, request) -> {
                        logger.debug("Handling async completion for: {}", internalSpec.getId());

                        return Mono.fromCallable(() -> {
                            try {
                                // Get suggestions from internal specification
                                Map<String, Object> suggestionsConfig = internalSpec.getSuggestionsConfig();
                                List<String> suggestions = List.of(); // Default empty list
                                int total = 0;
                                boolean hasMore = false;

                                if (suggestionsConfig != null) {
                                    // Extract suggestions from configuration
                                    @SuppressWarnings("unchecked")
                                    List<String> configSuggestions = (List<String>) suggestionsConfig
                                            .get("suggestions");
                                    if (configSuggestions != null) {
                                        suggestions = configSuggestions;
                                    }

                                    // Extract metadata
                                    total = ((Number) suggestionsConfig.getOrDefault("total", 0)).intValue();
                                    hasMore = (Boolean) suggestionsConfig.getOrDefault("hasMore", false);
                                }

                                // Create completion result
                                McpSchema.CompleteResult.CompleteCompletion completion = new McpSchema.CompleteResult.CompleteCompletion(
                                        suggestions, total, hasMore);

                                return new McpSchema.CompleteResult(completion);

                            } catch (Exception e) {
                                logger.error("Error handling async completion for: {}", internalSpec.getId(), e);
                                return new McpSchema.CompleteResult(
                                        new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false));
                            }
                        });
                    });

            logger.debug("Successfully created async completion specification for: {}", internalSpec.getId());
            return spec;

        } catch (Exception e) {
            logger.error("Failed to create async completion specification for: {}", internalSpec.getId(), e);
            return null;
        }
    }
}
