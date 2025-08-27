package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.StatisticsMetrics;

/**
 * Dialogue manager for autonomous agents
 * 
 * <p>
 * This interface provides:
 * - Dialogue management and conversation flow control
 * - Context-aware conversation handling
 * - Multi-turn dialogue management
 * - Conversation state tracking and management
 * - Dialogue optimization and improvement
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentModelDialogueManager {

    /**
     * Start a new dialogue session
     * 
     * @param agentId the agent ID
     * @param initialContext the initial dialogue context
     * @return CompletableFuture with the dialogue session
     */
    CompletableFuture<AgentModelDialogueSession> startDialogue(String agentId, Map<String, Object> initialContext);

    /**
     * Process a user message in the dialogue
     * 
     * @param sessionId the dialogue session ID
     * @param userMessage the user message
     * @param context the current context
     * @return CompletableFuture with the dialogue response
     */
    CompletableFuture<AgentModelDialogueResponse> processMessage(String sessionId, String userMessage,
            @Nullable Map<String, Object> context);

    /**
     * Continue an existing dialogue
     * 
     * @param sessionId the dialogue session ID
     * @param continuationContext the continuation context
     * @return CompletableFuture with the dialogue response
     */
    CompletableFuture<AgentModelDialogueResponse> continueDialogue(String sessionId,
            @Nullable Map<String, Object> continuationContext);

    /**
     * End a dialogue session
     * 
     * @param sessionId the dialogue session ID
     * @param reason the reason for ending the dialogue
     * @return CompletableFuture with the dialogue summary
     */
    CompletableFuture<AgentModelDialogueSummary> endDialogue(String sessionId, @Nullable String reason);

    /**
     * Get dialogue session information
     * 
     * @param sessionId the dialogue session ID
     * @return CompletableFuture with the dialogue session
     */
    CompletableFuture<AgentModelDialogueSession> getDialogueSession(String sessionId);

    /**
     * Get dialogue history
     * 
     * @param sessionId the dialogue session ID
     * @param maxMessages the maximum number of messages to return
     * @return CompletableFuture with the dialogue history
     */
    CompletableFuture<List<AgentModelDialogueMessage>> getDialogueHistory(String sessionId, int maxMessages);

    /**
     * Update dialogue context
     * 
     * @param sessionId the dialogue session ID
     * @param contextUpdates the context updates
     * @return CompletableFuture with the updated dialogue session
     */
    CompletableFuture<AgentModelDialogueSession> updateContext(String sessionId, Map<String, Object> contextUpdates);

    /**
     * Get dialogue statistics
     * 
     * @param sessionId the dialogue session ID
     * @return CompletableFuture with the dialogue statistics
     */
    CompletableFuture<StatisticsMetrics> getDialogueStatistics(String sessionId);

    /**
     * Optimize dialogue flow
     * 
     * @param sessionId the dialogue session ID
     * @param optimizationHints hints for optimization
     * @return CompletableFuture with the optimization result
     */
    CompletableFuture<AgentModelDialogueOptimizationResult> optimizeDialogue(String sessionId,
            @Nullable Map<String, Object> optimizationHints);

    /**
     * Validate dialogue session
     * 
     * @param sessionId the dialogue session ID
     * @return validation result
     */
    AgentModelDialogueValidationResult validateDialogue(String sessionId);

    /**
     * Get active dialogue sessions for an agent
     * 
     * @param agentId the agent ID
     * @return CompletableFuture with the list of active sessions
     */
    CompletableFuture<List<AgentModelDialogueSession>> getActiveSessions(String agentId);

    /**
     * Get dialogue performance metrics
     * 
     * @param agentId the agent ID
     * @return CompletableFuture with the performance metrics
     */
    CompletableFuture<AgentModelDialoguePerformanceMetrics> getPerformanceMetrics(String agentId);

    /**
     * Set dialogue strategy
     * 
     * @param sessionId the dialogue session ID
     * @param strategy the dialogue strategy
     */
    void setDialogueStrategy(String sessionId, String strategy);

    /**
     * Get current dialogue strategy
     * 
     * @param sessionId the dialogue session ID
     * @return the current dialogue strategy
     */
    String getDialogueStrategy(String sessionId);

    /**
     * Get available dialogue strategies
     * 
     * @return list of available dialogue strategies
     */
    List<String> getAvailableStrategies();
}
