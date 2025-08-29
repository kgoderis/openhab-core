package org.openhab.core.ai.tool.completions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion Template Service for MCP Completions
 *
 * Provides listing and management of predefined completion templates and
 * simple performance metrics.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = CompletionTemplateService.class)
public class CompletionTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(CompletionTemplateService.class);

    private final Map<String, List<String>> templates = new ConcurrentHashMap<>();
    // Performance metrics - now handled by MetricsService

    // Metrics service
    @Reference
    private @Nullable MetricsService metricsService;

    public CompletionTemplateService() {
        initializeTemplates();
    }

    private void initializeTemplates() {
        try {
            templates.put("general_commands",
                    List.of("ON", "OFF", "TOGGLE", "REFRESH", "PLAY", "PAUSE", "STOP", "NEXT", "PREVIOUS"));
            templates.put("numeric_commands", List.of("0", "25", "50", "75", "100", "0.0", "0.5", "1.0"));
            templates.put("color_commands",
                    List.of("RED", "GREEN", "BLUE", "WHITE", "BLACK", "YELLOW", "CYAN", "MAGENTA"));
            logger.info("Initialized {} completion templates", templates.size());
        } catch (Exception e) {
            logger.error("Failed to initialize completion templates", e);
        }
    }

    public Map<String, List<String>> listTemplates() {
        long start = System.currentTimeMillis();
        try {
            return Map.copyOf(templates);
        } finally {
            long duration = System.currentTimeMillis() - start;
            recordTemplateOperation("list-templates", true, duration);
        }
    }

    // Metrics recording methods - replacing removed AtomicLong fields using SystemPerformanceMetrics pattern

    /**
     * Record template operation - replaces totalTemplateRequests.incrementAndGet(), totalTemplateCompletions.incrementAndGet(), and totalTemplateTime.addAndGet()
     * ONE-FOR-ONE REPLACEMENT: Single MetricsService call handles all three AtomicLong operations automatically
     */
    private void recordTemplateOperation(String operation, boolean success, long durationMs) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // ONE-FOR-ONE REPLACEMENT: 
                // - totalTemplateRequests.incrementAndGet() -> automatically handled by recordOperation()
                // - totalTemplateCompletions.incrementAndGet() -> automatically handled by recordOperation() 
                // - totalTemplateTime.addAndGet(duration) -> handled by withDuration()
                SystemPerformanceMetrics.recordMessageLatency(metrics, "completion-template", operation, 
                        durationMs, success);
            }
        } catch (Exception e) {
            logger.warn("Failed to record template operation metric for {}: {}", operation, e.getMessage());
        }
    }
}
