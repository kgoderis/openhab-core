package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.automation.AdvancedAutomationAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.scheduler.CronScheduler;

/**
 * Unit tests for AdvancedAutomationAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (workflow management, task scheduling, job queue scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class AdvancedAutomationActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private RuleRegistry mockRuleRegistry;

    @Mock
    private CronScheduler mockCronScheduler;

    private AdvancedAutomationAction action;

    @BeforeEach
    void setUp() {
        action = new AdvancedAutomationAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.automation.advanced", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Advanced Automation Management", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Advanced automation management"));
    }

    @Test
    void testGetCategory() {
        assertEquals("automation", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidCreateWorkflow() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "create_workflow");
        parameters.put("name", "Test Workflow");
        parameters.put("description", "A test workflow");
        parameters.put("steps", java.util.List.of("step1", "step2"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidExecuteWorkflow() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "execute_workflow");
        parameters.put("workflowId", "test-workflow-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidScheduleTask() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "schedule_task");
        parameters.put("name", "Test Task");
        parameters.put("interval", "5m");
        parameters.put("parameters", Map.of("param1", "value1"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "Test Workflow");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithInvalidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Invalid action")));
    }

    @Test
    void testValidateParametersWithMissingRequiredFields() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "create_workflow");
        // Missing required 'name' field

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("name")));
    }

    @Test
    void testExecuteCreateWorkflow() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "create_workflow");
        parameters.put("name", "Test Workflow");
        parameters.put("description", "A test workflow");
        parameters.put("steps", java.util.List.of("step1", "step2"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("create_workflow", data.get("action"));
        assertNotNull(data.get("workflowId"));
        assertEquals("Test Workflow", data.get("name"));
        assertEquals("created", data.get("status"));
    }

    @Test
    void testExecuteListWorkflows() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_workflows");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_workflows", data.get("action"));
        assertNotNull(data.get("workflows"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScheduleTask() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "schedule_task");
        parameters.put("name", "Test Task");
        parameters.put("interval", "5m");
        parameters.put("parameters", Map.of("param1", "value1"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("schedule_task", data.get("action"));
        assertNotNull(data.get("taskId"));
        assertEquals("Test Task", data.get("name"));
        assertEquals("scheduled", data.get("status"));
    }

    @Test
    void testExecuteListScheduledTasks() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_scheduled_tasks");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_scheduled_tasks", data.get("action"));
        assertNotNull(data.get("tasks"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteCreateTemplate() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "create_template");
        parameters.put("name", "Test Template");
        parameters.put("description", "A test template");
        parameters.put("steps", java.util.List.of("step1", "step2"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("create_template", data.get("action"));
        assertNotNull(data.get("templateId"));
        assertEquals("Test Template", data.get("name"));
        assertEquals("created", data.get("status"));
    }

    @Test
    void testExecuteGetAutomationStats() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "get_stats");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("get_stats", data.get("action"));
        assertNotNull(data.get("activeWorkflows"));
        assertNotNull(data.get("scheduledTasks"));
        assertNotNull(data.get("jobQueueSize"));
        assertNotNull(data.get("templatesCount"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_workflows");

        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        assertNotNull(future);
        assertTrue(future.isDone());

        AIActionResult result = future.join();
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testExecuteWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_workflows");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_workflows");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, invalidContext);
        });
    }

    @Test
    void testGetParameterSchema() {
        Map<String, Object> schema = action.getParameterSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("name"));
        assertTrue(properties.containsKey("description"));
        assertTrue(properties.containsKey("steps"));
        assertTrue(properties.containsKey("interval"));
        assertTrue(properties.containsKey("parameters"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("action"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("message"));
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("workflowId"));
        assertTrue(properties.containsKey("taskId"));
        assertTrue(properties.containsKey("templateId"));
    }

    @Test
    void testGetMetadata() {
        AIActionMetadata metadata = action.getMetadata();

        assertNotNull(metadata);
        assertEquals("1.0.0", metadata.getVersion());
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getTags());
    }

    @Test
    void testGetCapabilities() {
        Map<String, Object> capabilities = action.getCapabilities();

        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportsAsync"));
        assertTrue(capabilities.containsKey("supportsValidation"));
        assertTrue(capabilities.containsKey("supportsWorkflows"));
        assertTrue(capabilities.containsKey("supportsScheduling"));
        assertTrue(capabilities.containsKey("supportsTemplates"));
        assertTrue(capabilities.containsKey("supportsJobQueue"));
    }

    @Test
    void testIsReady() {
        assertTrue(action.isReady());
    }

    @Test
    void testCleanup() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.cleanup());
    }

    @Test
    void testInitialize() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.initialize(mockContext));
    }
}
