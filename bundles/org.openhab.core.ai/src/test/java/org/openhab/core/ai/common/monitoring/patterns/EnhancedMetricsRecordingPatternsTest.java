package org.openhab.core.ai.common.monitoring.patterns;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Comprehensive test class for all enhanced metrics recording patterns.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EnhancedMetricsRecordingPatternsTest {

    @Mock
    private MetricsService metricsService;

    private TaskLifecycleMetrics taskMetrics;
    private ValidationRuleMetrics validationMetrics;
    private SkillExecutionMetrics skillMetrics;
    private AuditEventMetrics auditMetrics;
    private ConfigurationOperationMetrics configMetrics;
    private CardBuildingMetrics cardMetrics;

    @BeforeEach
    void setUp() {
        taskMetrics = new TaskLifecycleMetrics(metricsService);
        validationMetrics = new ValidationRuleMetrics(metricsService);
        skillMetrics = new SkillExecutionMetrics(metricsService);
        auditMetrics = new AuditEventMetrics(metricsService);
        configMetrics = new ConfigurationOperationMetrics(metricsService);
        cardMetrics = new CardBuildingMetrics(metricsService);
    }

    // ===== TASK LIFECYCLE METRICS TESTS =====

    @Test
    void testTaskCreationMetrics() {
        taskMetrics.recordTaskCreation("task-123", "data-processing", "high", Duration.ofMinutes(5),
                Duration.ofMillis(50));

        verify(metricsService).recordOperationWithData(eq("task"), eq("creation"), eq(true), eq(Duration.ofMillis(50)),
                argThat(data -> data.containsKey("taskId") && data.get("taskId").equals("task-123")));
    }

    @Test
    void testTaskActivationMetrics() {
        taskMetrics.recordTaskActivation("task-123", "data-processing", Duration.ofMillis(200), Duration.ofSeconds(5),
                250_000_000L);

        verify(metricsService).recordOperationWithData(eq("task"), eq("activation"), eq(true),
                eq(Duration.ofMillis(200)),
                argThat(data -> data.containsKey("taskId") && data.get("taskId").equals("task-123")));
    }

    @Test
    void testTaskCompletionMetrics() {
        taskMetrics.recordTaskCompletion("task-123", "data-processing", Duration.ofMinutes(3), 5_000_000L, true, null);

        verify(metricsService).recordOperationWithData(eq("task"), eq("completion"), eq(true),
                eq(Duration.ofMinutes(3)),
                argThat(data -> data.containsKey("taskId") && data.get("taskId").equals("task-123")));
    }

    @Test
    void testTaskCancellationMetrics() {
        taskMetrics.recordTaskCancellation("task-123", "data-processing", Duration.ofMillis(100), 75.5,
                "user-requested");

        verify(metricsService).recordOperationWithData(eq("task"), eq("cancellation"), eq(true),
                eq(Duration.ofMillis(100)),
                argThat(data -> data.containsKey("taskId") && data.get("taskId").equals("task-123")));
    }

    @Test
    void testTaskFailureMetrics() {
        taskMetrics.recordTaskFailure("task-123", "data-processing", Duration.ofMinutes(2), "resource-exhaustion", 3,
                true);

        verify(metricsService).recordOperationWithData(eq("task"), eq("failure"), eq(false), eq(Duration.ofMinutes(2)),
                argThat(data -> data.containsKey("taskId") && data.get("taskId").equals("task-123")));
    }

    // ===== VALIDATION RULE METRICS TESTS =====

    @Test
    void testValidationRuleExecutionMetrics() {
        validationMetrics.recordValidationRuleExecution("rule-456", "data-validation", Duration.ofMillis(25), true,
                5000L, null);

        verify(metricsService).recordOperationWithData(eq("validation"), eq("rule-execution"), eq(true),
                eq(Duration.ofMillis(25)),
                argThat(data -> data.containsKey("ruleId") && data.get("ruleId").equals("rule-456")));
    }

    @Test
    void testValidationRuleBatchMetrics() {
        validationMetrics.recordValidationRuleBatch("batch-789", 10, Duration.ofMillis(500), 8, 2, 100_000L);

        verify(metricsService).recordOperationWithData(eq("validation"), eq("rule-batch"), eq(false),
                eq(Duration.ofMillis(500)),
                argThat(data -> data.containsKey("batchId") && data.get("batchId").equals("batch-789")));
    }

    @Test
    void testValidationRulePerformanceMetrics() {
        validationMetrics.recordValidationRulePerformance("rule-456", Duration.ofMillis(30), 500, 95.5, 8);

        verify(metricsService).recordOperationWithData(eq("validation"), eq("rule-performance"), eq(true),
                eq(Duration.ofMillis(30)),
                argThat(data -> data.containsKey("ruleId") && data.get("ruleId").equals("rule-456")));
    }

    // ===== SKILL EXECUTION METRICS TESTS =====

    @Test
    void testSkillInvocationMetrics() {
        Map<String, Object> inputParams = Map.of("param1", "value1", "param2", 42);
        skillMetrics.recordSkillInvocation("skill-101", "data-analysis", Duration.ofMillis(100), inputParams,
                "agent-001");

        verify(metricsService).recordOperationWithData(eq("skill"), eq("invocation"), eq(true),
                eq(Duration.ofMillis(100)),
                argThat(data -> data.containsKey("skillId") && data.get("skillId").equals("skill-101")));
    }

    @Test
    void testSkillExecutionSuccessMetrics() {
        skillMetrics.recordSkillExecutionSuccess("skill-101", "data-analysis", Duration.ofSeconds(5), 1_000_000L,
                "agent-001", "batch-processing");

        verify(metricsService).recordOperationWithData(eq("skill"), eq("execution-success"), eq(true),
                eq(Duration.ofSeconds(5)),
                argThat(data -> data.containsKey("skillId") && data.get("skillId").equals("skill-101")));
    }

    @Test
    void testSkillExecutionFailureMetrics() {
        skillMetrics.recordSkillExecutionFailure("skill-101", "data-analysis", Duration.ofSeconds(2), "timeout",
                "Operation timed out", "agent-001", 2);

        verify(metricsService).recordOperationWithData(eq("skill"), eq("execution-failure"), eq(false),
                eq(Duration.ofSeconds(2)),
                argThat(data -> data.containsKey("skillId") && data.get("skillId").equals("skill-101")));
    }

    // ===== AUDIT EVENT METRICS TESTS =====

    @Test
    void testAuditEventMetrics() {
        Map<String, Object> eventData = Map.of("action", "login", "resource", "admin-panel");
        auditMetrics.recordAuditEvent("event-202", "authentication", "login", 2, "user-123", eventData,
                Duration.ofMillis(10));

        verify(metricsService).recordOperationWithData(eq("audit"), eq("event"), eq(true), eq(Duration.ofMillis(10)),
                argThat(data -> data.containsKey("eventId") && data.get("eventId").equals("event-202")));
    }

    @Test
    void testSecurityAuditEventMetrics() {
        auditMetrics.recordSecurityAuditEvent("security-303", "unauthorized-access", 4, "192.168.1.100", "admin-panel",
                "blocked", Duration.ofMillis(5));

        verify(metricsService).recordOperationWithData(eq("audit"), eq("security-event"), eq(true),
                eq(Duration.ofMillis(5)),
                argThat(data -> data.containsKey("eventId") && data.get("eventId").equals("security-303")));
    }

    @Test
    void testUserBehaviorAuditMetrics() {
        auditMetrics.recordUserBehaviorAudit("user-123", "suspicious-activity", "session-456", 25, 65.5,
                Duration.ofMillis(200));

        verify(metricsService).recordOperationWithData(eq("audit"), eq("user-behavior"), eq(true),
                eq(Duration.ofMillis(200)),
                argThat(data -> data.containsKey("userId") && data.get("userId").equals("user-123")));
    }

    // ===== CONFIGURATION OPERATION METRICS TESTS =====

    @Test
    void testCacheOperationMetrics() {
        configMetrics.recordCacheOperation("config-cache", "hit", "system.timeout", 5000L, Duration.ofNanos(5000));

        verify(metricsService).recordOperationWithData(eq("configuration"), eq("cache-hit"), eq(true),
                eq(Duration.ofNanos(5000)),
                argThat(data -> data.containsKey("cacheName") && data.get("cacheName").equals("config-cache")));
    }

    @Test
    void testConfigurationReloadMetrics() {
        configMetrics.recordConfigurationReload("system", "/conf/system.properties", 50_000L, Duration.ofMillis(300),
                0);

        verify(metricsService).recordOperationWithData(eq("configuration"), eq("reload"), eq(true),
                eq(Duration.ofMillis(300)),
                argThat(data -> data.containsKey("configType") && data.get("configType").equals("system")));
    }

    @Test
    void testFileOperationMetrics() {
        configMetrics.recordFileOperation("read", "/conf/system.properties", 50_000L, Duration.ofMillis(50), true);

        verify(metricsService).recordOperationWithData(eq("configuration"), eq("file-read"), eq(true),
                eq(Duration.ofMillis(50)),
                argThat(data -> data.containsKey("operationType") && data.get("operationType").equals("read")));
    }

    @Test
    void testConfigurationChangeMetrics() {
        configMetrics.recordConfigurationChange("system.timeout", "5000", "10000", Duration.ofMillis(25), 2, false);

        verify(metricsService).recordOperationWithData(eq("configuration"), eq("change"), eq(true),
                eq(Duration.ofMillis(25)),
                argThat(data -> data.containsKey("configKey") && data.get("configKey").equals("system.timeout")));
    }

    // ===== CARD BUILDING METRICS TESTS =====

    @Test
    void testCardGenerationMetrics() {
        cardMetrics.recordCardGeneration("card-404", "agent-dashboard", "agent-001", Duration.ofMillis(1000), 250_000L,
                true);

        verify(metricsService).recordOperationWithData(eq("card"), eq("generation"), eq(true),
                eq(Duration.ofMillis(1000)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardValidationMetrics() {
        cardMetrics.recordCardValidation("card-404", 5, Duration.ofMillis(100), 0, 1, true);

        verify(metricsService).recordOperationWithData(eq("card"), eq("validation"), eq(true),
                eq(Duration.ofMillis(100)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardRenderingMetrics() {
        cardMetrics.recordCardRendering("card-404", "html", Duration.ofMillis(200), 500_000L, true,
                "dashboard-template");

        verify(metricsService).recordOperationWithData(eq("card"), eq("rendering"), eq(true),
                eq(Duration.ofMillis(200)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardCachingMetrics() {
        cardMetrics.recordCardCaching("card-404", "store", Duration.ofNanos(5000), 250_000L, true);

        verify(metricsService).recordOperationWithData(eq("card"), eq("cache-store"), eq(true),
                eq(Duration.ofNanos(5000)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardUpdateMetrics() {
        cardMetrics.recordCardUpdate("card-404", "content", Duration.ofMillis(150), 3, true, false);

        verify(metricsService).recordOperationWithData(eq("card"), eq("update"), eq(true), eq(Duration.ofMillis(150)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardPerformanceMetrics() {
        cardMetrics.recordCardPerformance("card-404", "render-time", 250.5, Duration.ofMillis(50), 8);

        verify(metricsService).recordOperationWithData(eq("card"), eq("performance"), eq(true),
                eq(Duration.ofMillis(50)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    @Test
    void testCardLifecycleMetrics() {
        cardMetrics.recordCardLifecycle("card-404", "activated", Duration.ofMillis(25), 5000L, 10);

        verify(metricsService).recordOperationWithData(eq("card"), eq("lifecycle-activated"), eq(true),
                eq(Duration.ofMillis(25)),
                argThat(data -> data.containsKey("cardId") && data.get("cardId").equals("card-404")));
    }

    // ===== INTEGRATION TESTS =====

    @Test
    void testAllMetricsPatternsIntegration() {
        // Test that all metrics patterns work together without conflicts
        taskMetrics.recordTaskCreation("task-999", "integration-test", "medium", Duration.ofMinutes(1),
                Duration.ofMillis(10));
        validationMetrics.recordValidationRuleExecution("rule-999", "integration-test", Duration.ofMillis(5), true,
                1000L, null);
        skillMetrics.recordSkillInvocation("skill-999", "integration-test", Duration.ofMillis(20), Map.of(),
                "agent-999");
        auditMetrics.recordAuditEvent("event-999", "integration", "test", 1, "user-999", Map.of(),
                Duration.ofMillis(5));
        configMetrics.recordCacheOperation("test-cache", "hit", "test-key", 1000L, Duration.ofNanos(1000));
        cardMetrics.recordCardGeneration("card-999", "test-card", "agent-999", Duration.ofMillis(100), 10000L, true);

        // Verify all operations were recorded
        verify(metricsService, times(6)).recordOperationWithData(anyString(), anyString(), anyBoolean(),
                any(Duration.class), any());
    }
}
