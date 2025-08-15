package org.openhab.core.ai.tool.prompts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
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
    private final AtomicLong totalTemplateRequests = new AtomicLong(0);
    private final AtomicLong totalTemplateCompletions = new AtomicLong(0);
    private final AtomicLong totalTemplateTime = new AtomicLong(0);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable PromptRegistry promptRegistry;

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
                    java.util.List.of(new PromptArgument("format", "Output format", false))));
            templates.put("item_control",
                    new Prompt("Item Control", "Control an item",
                            java.util.List.of(new PromptArgument("itemName", "Name of the item", true),
                                    new PromptArgument("command", "Command to send", true))));
            logger.info("Initialized {} prompt templates", templates.size());
        } catch (Exception e) {
            logger.error("Failed to initialize prompt templates", e);
        }
    }

    public Map<String, Prompt> listTemplates() {
        totalTemplateRequests.incrementAndGet();
        long start = System.currentTimeMillis();
        try {
            return java.util.Map.copyOf(templates);
        } finally {
            totalTemplateCompletions.incrementAndGet();
            totalTemplateTime.addAndGet(System.currentTimeMillis() - start);
        }
    }
}
