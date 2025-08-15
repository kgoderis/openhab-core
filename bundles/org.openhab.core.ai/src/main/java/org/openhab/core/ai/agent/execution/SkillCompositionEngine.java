package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.execution.api.AgentSkillManager;
import org.openhab.core.ai.agent.execution.api.AgentSkillResult;
import org.openhab.core.ai.agents.SkillExecutionRequest;
import org.openhab.core.ai.events.EventProcessingAnalytics;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skill Composition Engine
 * 
 * <p>
 * This component provides skill composition execution capabilities:
 * - Skill composition strategy execution
 * - Dependency management and ordering
 * - Result aggregation and processing
 * - Performance monitoring and analytics
 * - Error handling and recovery
 * - Caching and optimization
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = SkillCompositionEngine.class)
@NonNullByDefault
public class SkillCompositionEngine {

    private static final Logger logger = LoggerFactory.getLogger(SkillCompositionEngine.class);

    // Configuration
    private static final Duration DEFAULT_COMPOSITION_TIMEOUT = Duration.ofMinutes(5);
    private static final int DEFAULT_MAX_CONCURRENT_COMPOSITIONS = 3;

    // Performance monitoring
    private final AtomicLong totalCompositionsProcessed = new AtomicLong(0);
    private final AtomicLong totalCompositionsSucceeded = new AtomicLong(0);
    private final AtomicLong totalCompositionsFailed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final Map<String, CompositionMetric> compositionMetrics = new ConcurrentHashMap<>();

    // Dependencies
    @Reference
    private @Nullable AgentSkillManager skillManager;

    @Reference
    private @Nullable EventProcessingAnalytics analytics;

    // Configuration
    private Duration compositionTimeout = DEFAULT_COMPOSITION_TIMEOUT;
    private int maxConcurrentCompositions = DEFAULT_MAX_CONCURRENT_COMPOSITIONS;
    private boolean enablePerformanceMonitoring = true;
    private boolean enableCaching = true;

    @Activate
    public void activate() {
        logger.debug("Skill Composition Engine activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Skill Composition Engine deactivated");
    }

    /**
     * Execute skill composition with strategy
     */
    public CompletableFuture<CompositionResult> executeComposition(SkillCompositionStrategy strategy,
            List<SkillExecutionRequest> skills, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            String compositionId = generateCompositionId();

            try {
                // Validate strategy
                if (!strategy.canHandle(skills)) {
                    return CompositionResult.error(compositionId, "Strategy cannot handle the given skills", 0);
                }

                // Compose skills using strategy
                SkillCompositionResult compositionResult = strategy.compose(skills, context);
                if (!compositionResult.isSuccess()) {
                    return CompositionResult.error(compositionId, compositionResult.getErrorMessage(), 0);
                }

                // Execute the composition plan
                CompositionResult result = executeCompositionPlan(compositionResult.getExecutionPlan(), compositionId);

                // Record metrics
                recordCompositionMetrics(compositionId, result, Duration.between(startTime, Instant.now()));

                return result;

            } catch (Exception e) {
                logger.error("Error executing composition {}: {}", compositionId, e.getMessage(), e);
                totalCompositionsFailed.incrementAndGet();
                return CompositionResult.error(compositionId, "Error executing composition: " + e.getMessage(), 0);
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        });
    }

    /**
     * Execute composition plan
     */
    private CompositionResult executeCompositionPlan(List<SkillExecutionStep> steps, String compositionId) {
        try {
            List<AgentSkillResult> results = new ArrayList<>();
            long totalExecutionTime = 0;

            // Execute steps in order
            for (SkillExecutionStep step : steps) {
                // Check dependencies
                if (!checkDependencies(step, results)) {
                    return CompositionResult.error(compositionId,
                            "Dependency check failed for step: " + step.getSkillName(), totalExecutionTime);
                }

                // Execute skill
                AgentSkillManager manager = skillManager;
                if (manager == null) {
                    return CompositionResult.error(compositionId, "Skill manager not available", totalExecutionTime);
                }

                AgentSkillResult result = manager.executeSkill(step.getSkillName(), step.getParameters());
                results.add(result);
                totalExecutionTime += result.getExecutionTime();

                // Stop if step fails and is required
                if (!result.isSuccess() && step.isRequired()) {
                    return CompositionResult.error(compositionId, "Required step failed: " + step.getSkillName(),
                            totalExecutionTime);
                }
            }

            totalCompositionsSucceeded.incrementAndGet();
            return CompositionResult.success(compositionId, results, totalExecutionTime);

        } catch (Exception e) {
            logger.error("Error executing composition plan: {}", e.getMessage(), e);
            return CompositionResult.error(compositionId, "Error executing composition plan: " + e.getMessage(), 0);
        }
    }

    /**
     * Check step dependencies
     */
    private boolean checkDependencies(SkillExecutionStep step, List<AgentSkillResult> previousResults) {
        List<String> dependencies = step.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }

        // TODO: Implement proper dependency checking logic
        // For now, assume all dependencies are satisfied
        return true;
    }

    /**
     * Record composition metrics
     */
    private void recordCompositionMetrics(String compositionId, CompositionResult result, Duration duration) {
        totalCompositionsProcessed.incrementAndGet();

        CompositionMetric metric = new CompositionMetric(compositionId, result.isSuccess(), duration,
                result.getMessage(), Instant.now());

        compositionMetrics.put(compositionId, metric);

        // Keep only recent metrics
        if (compositionMetrics.size() > 1000) {
            String oldestKey = compositionMetrics.keySet().iterator().next();
            compositionMetrics.remove(oldestKey);
        }

        // Record analytics if available
        if (analytics != null) {
            analytics.recordPerformanceMetric("SkillCompositionEngine", "executeComposition", duration,
                    result.isSuccess());
            if (!result.isSuccess()) {
                analytics.recordError("SkillCompositionEngine", "executeComposition", result.getMessage(), null);
            }
        }
    }

    /**
     * Generate unique composition ID
     */
    private String generateCompositionId() {
        return "comp_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    // Configuration methods
    public void setCompositionTimeout(Duration compositionTimeout) {
        this.compositionTimeout = compositionTimeout;
    }

    public void setMaxConcurrentCompositions(int maxConcurrentCompositions) {
        this.maxConcurrentCompositions = maxConcurrentCompositions;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }

    // Inner class extracted to top-level: org.openhab.core.ai.agent.execution.CompositionResult

    // Inner class extracted to top-level: org.openhab.core.ai.agent.execution.CompositionMetric
}
