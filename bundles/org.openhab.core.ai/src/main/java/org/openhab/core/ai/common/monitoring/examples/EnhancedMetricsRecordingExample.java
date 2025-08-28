package org.openhab.core.ai.common.monitoring.examples;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.AuditEventMetrics;
import org.openhab.core.ai.common.monitoring.patterns.CardBuildingMetrics;
import org.openhab.core.ai.common.monitoring.patterns.ConfigurationOperationMetrics;
import org.openhab.core.ai.common.monitoring.patterns.SkillExecutionMetrics;
import org.openhab.core.ai.common.monitoring.patterns.TaskLifecycleMetrics;
import org.openhab.core.ai.common.monitoring.patterns.ValidationRuleMetrics;

/**
 * Comprehensive example demonstrating all enhanced metrics recording patterns.
 * 
 * <p>
 * This class shows how to use the new enhanced metrics recording patterns
 * for comprehensive operation monitoring across all domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EnhancedMetricsRecordingExample {

    private final MetricsService metricsService;
    private final TaskLifecycleMetrics taskMetrics;
    private final ValidationRuleMetrics validationMetrics;
    private final SkillExecutionMetrics skillMetrics;
    private final AuditEventMetrics auditMetrics;
    private final ConfigurationOperationMetrics configMetrics;
    private final CardBuildingMetrics cardMetrics;

    public EnhancedMetricsRecordingExample(MetricsService metricsService) {
        this.metricsService = metricsService;
        this.taskMetrics = new TaskLifecycleMetrics(metricsService);
        this.validationMetrics = new ValidationRuleMetrics(metricsService);
        this.skillMetrics = new SkillExecutionMetrics(metricsService);
        this.auditMetrics = new AuditEventMetrics(metricsService);
        this.configMetrics = new ConfigurationOperationMetrics(metricsService);
        this.cardMetrics = new CardBuildingMetrics(metricsService);
    }

    /**
     * Example: Complete task lifecycle with enhanced metrics.
     */
    public void demonstrateTaskLifecycle() {
        String taskId = "task-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String taskType = "data-processing";
        String priority = "high";

        // Task Creation
        taskMetrics.recordTaskCreation(taskId, taskType, priority, Duration.ofMinutes(5),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(10, 100)));

        // Task Activation
        taskMetrics.recordTaskActivation(taskId, taskType,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(50, 500)),
                Duration.ofSeconds(ThreadLocalRandom.current().nextInt(1, 10)),
                ThreadLocalRandom.current().nextLong(100_000_000, 500_000_000));

        // Task Completion (simulate success)
        taskMetrics.recordTaskCompletion(taskId, taskType,
                Duration.ofMinutes(ThreadLocalRandom.current().nextInt(2, 8)),
                ThreadLocalRandom.current().nextLong(1_000_000, 10_000_000), true, null);
    }

    /**
     * Example: Validation rule execution with enhanced metrics.
     */
    public void demonstrateValidationRules() {
        String ruleId = "rule-" + ThreadLocalRandom.current().nextInt(100, 999);
        String ruleType = "data-validation";

        // Individual rule execution
        validationMetrics.recordValidationRuleExecution(ruleId, ruleType,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(5, 50)),
                ThreadLocalRandom.current().nextBoolean(), ThreadLocalRandom.current().nextLong(1000, 10000),
                ThreadLocalRandom.current().nextBoolean() ? "Validation failed" : null);

        // Batch validation
        validationMetrics.recordValidationRuleBatch("batch-" + ThreadLocalRandom.current().nextInt(100, 999),
                ThreadLocalRandom.current().nextInt(5, 20),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(100, 1000)),
                ThreadLocalRandom.current().nextInt(3, 15), ThreadLocalRandom.current().nextInt(0, 3),
                ThreadLocalRandom.current().nextLong(50_000, 500_000));

        // Performance analysis
        validationMetrics.recordValidationRulePerformance(ruleId,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(10, 100)),
                ThreadLocalRandom.current().nextInt(100, 1000), ThreadLocalRandom.current().nextDouble(80, 100),
                ThreadLocalRandom.current().nextInt(6, 10));
    }

    /**
     * Example: Skill execution with enhanced metrics.
     */
    public void demonstrateSkillExecution() {
        String skillId = "skill-" + ThreadLocalRandom.current().nextInt(100, 999);
        String skillName = "data-analysis";
        String agentId = "agent-" + ThreadLocalRandom.current().nextInt(10, 99);

        // Skill invocation
        Map<String, Object> inputParams = Map.of("dataSource", "database", "analysisType", "statistical", "parameters",
                Map.of("confidence", 0.95, "sampleSize", 1000));

        skillMetrics.recordSkillInvocation(skillId, skillName,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(20, 200)), inputParams, agentId);

        // Skill execution success
        skillMetrics.recordSkillExecutionSuccess(skillId, skillName,
                Duration.ofSeconds(ThreadLocalRandom.current().nextInt(1, 10)),
                ThreadLocalRandom.current().nextLong(100_000, 1_000_000), agentId, "batch-processing");

        // Skill performance
        skillMetrics.recordSkillPerformance(skillId, skillName,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(100, 1000)),
                ThreadLocalRandom.current().nextInt(50, 500), ThreadLocalRandom.current().nextDouble(90, 100),
                ThreadLocalRandom.current().nextDouble(10, 100));
    }

    /**
     * Example: Audit events with enhanced metrics.
     */
    public void demonstrateAuditEvents() {
        String eventId = "event-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String userId = "user-" + ThreadLocalRandom.current().nextInt(100, 999);

        // General audit event
        Map<String, Object> eventData = Map.of("action", "configuration-change", "resource", "system-settings",
                "timestamp", System.currentTimeMillis());

        auditMetrics.recordAuditEvent(eventId, "configuration", "change", ThreadLocalRandom.current().nextInt(1, 4),
                userId, eventData, Duration.ofMillis(ThreadLocalRandom.current().nextInt(5, 50)));

        // Security audit event
        auditMetrics.recordSecurityAuditEvent("security-" + ThreadLocalRandom.current().nextInt(100, 999),
                "unauthorized-access-attempt", ThreadLocalRandom.current().nextInt(3, 5),
                "192.168.1." + ThreadLocalRandom.current().nextInt(100, 200), "admin-panel", "blocked",
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(1, 10)));

        // User behavior audit
        auditMetrics.recordUserBehaviorAudit(userId, "suspicious-activity",
                "session-" + ThreadLocalRandom.current().nextInt(1000, 9999),
                ThreadLocalRandom.current().nextInt(10, 100), ThreadLocalRandom.current().nextDouble(20, 80),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(50, 500)));
    }

    /**
     * Example: Configuration operations with enhanced metrics.
     */
    public void demonstrateConfigurationOperations() {
        String configKey = "system.timeout";
        String configPath = "/conf/system.properties";

        // Cache operations
        configMetrics.recordCacheOperation("config-cache", "hit", configKey,
                ThreadLocalRandom.current().nextLong(1000, 10000),
                Duration.ofNanos(ThreadLocalRandom.current().nextInt(1000, 10000)));

        configMetrics.recordCacheOperation("config-cache", "miss", configKey,
                ThreadLocalRandom.current().nextLong(1000, 10000),
                Duration.ofNanos(ThreadLocalRandom.current().nextInt(10000, 100000)));

        // Configuration reload
        configMetrics.recordConfigurationReload("system", configPath,
                ThreadLocalRandom.current().nextLong(10_000, 100_000),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(100, 1000)),
                ThreadLocalRandom.current().nextInt(0, 3));

        // File operations
        configMetrics.recordFileOperation("read", configPath, ThreadLocalRandom.current().nextLong(10_000, 100_000),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(10, 100)), true);

        // Configuration changes
        configMetrics.recordConfigurationChange(configKey, "5000", "10000",
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(10, 100)),
                ThreadLocalRandom.current().nextInt(1, 4), false);

        // Configuration validation
        configMetrics.recordConfigurationValidation("system", ThreadLocalRandom.current().nextInt(5, 20),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(50, 500)),
                ThreadLocalRandom.current().nextInt(0, 2), ThreadLocalRandom.current().nextInt(0, 5));
    }

    /**
     * Example: Card building with enhanced metrics.
     */
    public void demonstrateCardBuilding() {
        String cardId = "card-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String agentId = "agent-" + ThreadLocalRandom.current().nextInt(10, 99);

        // Card generation
        cardMetrics.recordCardGeneration(cardId, "agent-dashboard", agentId,
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(200, 2000)),
                ThreadLocalRandom.current().nextLong(50_000, 500_000), true);

        // Card validation
        cardMetrics.recordCardValidation(cardId, ThreadLocalRandom.current().nextInt(3, 10),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(20, 200)),
                ThreadLocalRandom.current().nextInt(0, 2), ThreadLocalRandom.current().nextInt(0, 3), true);

        // Card rendering
        cardMetrics.recordCardRendering(cardId, "html", Duration.ofMillis(ThreadLocalRandom.current().nextInt(50, 500)),
                ThreadLocalRandom.current().nextLong(100_000, 1_000_000), true, "dashboard-template");

        // Card caching
        cardMetrics.recordCardCaching(cardId, "store",
                Duration.ofNanos(ThreadLocalRandom.current().nextInt(1000, 10000)),
                ThreadLocalRandom.current().nextLong(50_000, 500_000), true);

        // Card performance
        cardMetrics.recordCardPerformance(cardId, "render-time", ThreadLocalRandom.current().nextDouble(100, 1000),
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(10, 100)),
                ThreadLocalRandom.current().nextInt(7, 10));

        // Card lifecycle
        cardMetrics.recordCardLifecycle(cardId, "activated",
                Duration.ofMillis(ThreadLocalRandom.current().nextInt(5, 50)),
                ThreadLocalRandom.current().nextLong(1000, 10000), ThreadLocalRandom.current().nextInt(1, 100));
    }

    /**
     * Example: Complete workflow with all enhanced metrics patterns.
     */
    public void demonstrateCompleteWorkflow() {
        System.out.println("=== Enhanced Metrics Recording Patterns Demo ===");

        System.out.println("1. Task Lifecycle Metrics:");
        demonstrateTaskLifecycle();

        System.out.println("2. Validation Rule Metrics:");
        demonstrateValidationRules();

        System.out.println("3. Skill Execution Metrics:");
        demonstrateSkillExecution();

        System.out.println("4. Audit Event Metrics:");
        demonstrateAuditEvents();

        System.out.println("5. Configuration Operation Metrics:");
        demonstrateConfigurationOperations();

        System.out.println("6. Card Building Metrics:");
        demonstrateCardBuilding();

        System.out.println("=== Demo Complete ===");
    }
}
