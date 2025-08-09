package org.openhab.core.ai.tool.completions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
public class CompletionTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(CompletionTemplateService.class);

    private final Map<String, List<String>> templates = new ConcurrentHashMap<>();
    private final AtomicLong totalTemplateRequests = new AtomicLong(0);
    private final AtomicLong totalTemplateCompletions = new AtomicLong(0);
    private final AtomicLong totalTemplateTime = new AtomicLong(0);

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
