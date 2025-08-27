package org.openhab.core.ai.tool.prompts;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt Template Service for MCP Prompts
 *
 * Provides listing and management of predefined prompt templates and
 * simple performance metrics. Integrates with PromptRegistry if available.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class PromptTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateService.class);

    private final Map<String, Prompt> templates = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable PromptRegistry promptRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private volatile @Nullable MetricsService metricsService;

    @Activate
    protected void activate() {
        logger.debug("Activating PromptTemplateService");
        initializeTemplates();
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating PromptTemplateService");
        templates.clear();
    }

    private void initializeTemplates() {
        try {
            // Basic templates; extend as needed
            templates.put("system_status", new Prompt("System Status", "Report overall system status",
                    List.of(new PromptArgument("format", "Output format", false))));
            templates.put("item_control",
                    new Prompt("Item Control", "Control an item",
                            List.of(new PromptArgument("itemName", "Name of the item", true),
                                    new PromptArgument("command", "Command to send", true))));
            logger.info("Initialized {} prompt templates", templates.size());
        } catch (Exception e) {
            logger.error("Failed to initialize prompt templates", e);
        }
    }

    public Map<String, Prompt> listTemplates() {
        long start = System.currentTimeMillis();
        try {
            // Record template request metrics
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("prompt_template", "request")
                        .withSuccess(true)
                        .withDuration(0L)
                        .withData("templateCount", templates.size())
                        .record();
                } catch (Exception e) {
                    logger.warn("Failed to record prompt template request metrics: {}", e.getMessage());
                    // Graceful degradation: continue with template listing even if metrics recording fails
                }
            }
            
            return Map.copyOf(templates);
        } finally {
            long duration = System.currentTimeMillis() - start;
            // Record template completion metrics
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("prompt_template", "completion")
                        .withSuccess(true)
                        .withDuration(Duration.ofMillis(duration).toNanos())
                        .withData("templateCount", templates.size())
                        .withData("durationMs", duration)
                        .record();
                } catch (Exception e) {
                    logger.warn("Failed to record prompt template completion metrics: {}", e.getMessage());
                    // Graceful degradation: continue with template listing even if metrics recording fails
                }
            }
        }
    }
}
