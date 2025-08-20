/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.reasoning.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.common.validation.ReasoningValidationResult;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.constraints.SafetyConstraintManager;
import org.openhab.core.ai.reasoning.engine.api.ReasoningPlanStep;
import org.openhab.core.ai.reasoning.learning.LearningAdaptationSystem;
import org.openhab.core.ai.reasoning.memory.AgentMemory;
import org.openhab.core.ai.reasoning.metrics.OrchestrationMetrics;
import org.openhab.core.ai.reasoning.session.ReasoningSession;
import org.openhab.core.ai.reasoning.strategies.ParallelReasoningStrategy;
import org.openhab.core.ai.reasoning.strategies.SequentialReasoningStrategy;
import org.openhab.core.ai.reasoning.strategies.api.ReasoningStrategy;
import org.openhab.core.ai.reasoning.strategies.execution.AdaptiveReasoningStrategy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for orchestrating multi-step reasoning processes.
 * 
 * This service coordinates complex reasoning workflows, manages reasoning steps,
 * handles dependencies between steps, and provides performance optimization
 * and monitoring capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@Component(service = ReasoningOrchestrationService.class)
@NonNullByDefault
public class ReasoningOrchestrationService {

    private final Logger logger = LoggerFactory.getLogger(ReasoningOrchestrationService.class);

    private final Map<String, ReasoningSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, ReasoningStrategy> strategies = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final AtomicLong sessionCounter = new AtomicLong(0);

    private boolean activated = false;

    @Reference
    private @Nullable MultiStepReasoningEngine reasoningEngine;

    @Reference
    private @Nullable AgentMemory agentMemory;

    @Reference
    private @Nullable LearningAdaptationSystem learningSystem;

    @Reference
    private @Nullable SafetyConstraintManager safetyManager;

    /**
     * Creates a new ReasoningOrchestrationService.
     */
    public ReasoningOrchestrationService() {
        this.executorService = Executors.newCachedThreadPool();
        initializeDefaultStrategies();
    }

    @Activate
    public void activate() {
        if (activated) {
            return;
        }

        logger.info("Activating ReasoningOrchestrationService");
        activated = true;

        // Initialize default strategies if not already done
        initializeDefaultStrategies();
    }

    @Deactivate
    public void deactivate() {
        if (!activated) {
            return;
        }

        logger.info("Deactivating ReasoningOrchestrationService");
        activated = false;

        // Cancel all active sessions
        activeSessions.values().forEach(ReasoningSession::cancel);
        activeSessions.clear();

        // Shutdown executor service
        executorService.shutdown();
    }

    /**
     * Orchestrates a multi-step reasoning process.
     * 
     * @param client The intelligent tool client to use
     * @param context The initial reasoning context
     * @param params Configuration parameters
     * @return A CompletableFuture containing the reasoning result
     */
    public CompletableFuture<MultiStepReasoningResult> orchestrateReasoning(IntelligentToolClient client,
            ReasoningContext context, ModelParameters params) {
        if (!activated) {
            return CompletableFuture.failedFuture(new IllegalStateException("Service not activated"));
        }

        String sessionId = "session-" + sessionCounter.incrementAndGet();
        ReasoningSession session = new ReasoningSession(sessionId, client, context, params, executorService);
        activeSessions.put(sessionId, session);

        return session.execute().whenComplete((result, throwable) -> {
            activeSessions.remove(sessionId);
            if (throwable != null) {
                logger.error("Reasoning session {} failed", sessionId, throwable);
            } else {
                logger.debug("Reasoning session {} completed successfully", sessionId);
            }
        });
    }

    /**
     * Executes reasoning steps in parallel where possible.
     * 
     * @param client The intelligent tool client
     * @param steps The reasoning steps to execute
     * @param context The reasoning context
     * @param params Configuration parameters
     * @return A CompletableFuture containing the aggregated results
     */
    public CompletableFuture<List<ModelResponse>> executeStepsParallel(IntelligentToolClient client,
            List<ReasoningPlanStep> steps, ReasoningContext context, ModelParameters params) {
        if (!activated) {
            return CompletableFuture.failedFuture(new IllegalStateException("Service not activated"));
        }

        List<CompletableFuture<ModelResponse>> futures = new ArrayList<>();
        Map<String, Object> memoryContext = new HashMap<>();

        for (ReasoningPlanStep step : steps) {
            CompletableFuture<ModelResponse> future = client.executeReasoningStep(step.getId(), step.getPrompt(),
                    context, memoryContext, params);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    /**
     * Executes reasoning steps sequentially with dependency management.
     * 
     * @param client The intelligent tool client
     * @param steps The reasoning steps to execute
     * @param context The reasoning context
     * @param params Configuration parameters
     * @return A CompletableFuture containing the sequential results
     */
    public CompletableFuture<List<ModelResponse>> executeStepsSequential(IntelligentToolClient client,
            List<ReasoningPlanStep> steps, ReasoningContext context, ModelParameters params) {
        if (!activated) {
            return CompletableFuture.failedFuture(new IllegalStateException("Service not activated"));
        }

        CompletableFuture<List<ModelResponse>> result = CompletableFuture.completedFuture(new ArrayList<>());
        Map<String, Object> memoryContext = new HashMap<>();

        for (ReasoningPlanStep step : steps) {
            result = result.thenCompose(responses -> {
                return client.executeReasoningStep(step.getId(), step.getPrompt(), context, memoryContext, params)
                        .thenApply(response -> {
                            responses.add(response);
                            return responses;
                        });
            });
        }

        return result;
    }

    /**
     * Validates reasoning steps and their dependencies.
     * 
     * @param steps The reasoning steps to validate
     * @return Validation result with any issues found
     */
    public ReasoningValidationResult validateSteps(List<ReasoningPlanStep> steps) {
        ReasoningValidationResult result = ReasoningValidationResult.create();
        Map<String, ReasoningPlanStep> stepMap = new HashMap<>();

        // Build step map and check for duplicates
        for (ReasoningPlanStep step : steps) {
            if (stepMap.containsKey(step.getId())) {
                result.addError("Duplicate step ID: " + step.getId());
            }
            stepMap.put(step.getId(), step);
        }

        // Check dependencies
        for (ReasoningPlanStep step : steps) {
            for (String dependencyId : step.getDependencies()) {
                if (!stepMap.containsKey(dependencyId)) {
                    result.addError("Step " + step.getId() + " depends on non-existent step: " + dependencyId);
                }
            }
        }

        // Check for circular dependencies
        if (hasCircularDependencies(steps)) {
            result.addError("Circular dependencies detected in reasoning steps");
        }

        return result;
    }

    /**
     * Gets performance metrics for the orchestration service.
     * 
     * @return Performance metrics
     */
    public OrchestrationMetrics getMetrics() {
        return new OrchestrationMetrics(activeSessions.size(), sessionCounter.get());
    }

    /**
     * Registers a reasoning strategy.
     * 
     * @param name Strategy name
     * @param strategy The strategy implementation
     */
    public void registerStrategy(String name, ReasoningStrategy strategy) {
        strategies.put(name, strategy);
        logger.debug("Registered reasoning strategy: {}", name);
    }

    /**
     * Gets a reasoning strategy by name.
     * 
     * @param name Strategy name
     * @return The strategy or null if not found
     */
    public @Nullable ReasoningStrategy getStrategy(String name) {
        return strategies.get(name);
    }

    private void initializeDefaultStrategies() {
        registerStrategy("sequential", new SequentialReasoningStrategy(this));
        registerStrategy("parallel", new ParallelReasoningStrategy(this));
        registerStrategy("adaptive", new AdaptiveReasoningStrategy(this));
    }

    private boolean hasCircularDependencies(List<ReasoningPlanStep> steps) {
        Map<String, List<String>> adjacencyList = new HashMap<>();

        for (ReasoningPlanStep step : steps) {
            adjacencyList.put(step.getId(), new ArrayList<>(step.getDependencies()));
        }

        return hasCycle(adjacencyList);
    }

    private boolean hasCycle(Map<String, List<String>> adjacencyList) {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();

        for (String node : adjacencyList.keySet()) {
            if (isCyclicUtil(node, visited, recursionStack, adjacencyList)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCyclicUtil(String node, Map<String, Boolean> visited, Map<String, Boolean> recursionStack,
            Map<String, List<String>> adjacencyList) {
        if (recursionStack.getOrDefault(node, false)) {
            return true;
        }

        if (visited.getOrDefault(node, false)) {
            return false;
        }

        visited.put(node, true);
        recursionStack.put(node, true);

        List<String> neighbors = adjacencyList.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (isCyclicUtil(neighbor, visited, recursionStack, adjacencyList)) {
                    return true;
                }
            }
        }

        recursionStack.put(node, false);
        return false;
    }

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.ReasoningSession

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.ValidationResult

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.OrchestrationMetrics

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.SequentialReasoningStrategy

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.ParallelReasoningStrategy

    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.AdaptiveReasoningStrategy
}
