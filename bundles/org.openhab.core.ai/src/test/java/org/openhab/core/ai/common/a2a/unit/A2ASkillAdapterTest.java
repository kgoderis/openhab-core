package org.openhab.core.ai.a2a.unit;

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
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.agent.internal.A2ASkillAdapter;
import org.openhab.core.ai.api.action.Action;

import io.a2a.spec.Message;
import io.a2a.spec.TextPart;

@ExtendWith(MockitoExtension.class)
class A2ASkillAdapterTest {

    @Mock
    private Action mockAction;

    @Mock
    private Message mockMessage;

    @Mock
    private TextPart mockTextPart;

    private A2ASkillAdapter adapter;
    private static final String SKILL_ID = "test.skill";
    private static final String ACTION_NAME = "TestAction";
    private static final String ACTION_DESCRIPTION = "Test action description";

    @BeforeEach
    void setUp() {
        adapter = new A2ASkillAdapter(SKILL_ID, mockAction);

        // Setup mock action
        when(mockAction.getActionName()).thenReturn(ACTION_NAME);
        when(mockAction.getDescription()).thenReturn(ACTION_DESCRIPTION);
        when(mockAction.getActionId()).thenReturn("test.action");
    }

    @Test
    void testConstructor() {
        assertNotNull(adapter);
        assertEquals(SKILL_ID, adapter.getSkillId());
        assertEquals(mockAction, adapter.getAction());
    }

    @Test
    void testGetSkillName() {
        assertEquals(ACTION_NAME, adapter.getSkillName());
    }

    @Test
    void testGetSkillDescription() {
        assertEquals(ACTION_DESCRIPTION, adapter.getSkillDescription());
    }

    @Test
    void testGetSkillCategory() {
        // Default category should be returned
        assertNotNull(adapter.getSkillCategory());
    }

    @Test
    void testToString() {
        String result = adapter.toString();
        assertTrue(result.contains(SKILL_ID));
        assertTrue(result.contains(ACTION_NAME));
    }

    @Test
    void testExecuteSuccess() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action param1=value1 param2=value2");

        // Setup mock action result
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("status", "success");
        resultData.put("data", "test result");

        ActionResult mockActionResult = mock(ActionResult.class);
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockActionResult.getData()).thenReturn(resultData);

        CompletableFuture<ActionResult> future = CompletableFuture.completedFuture(mockActionResult);
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class))).thenReturn(future);

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("success", result.getData().get("status"));
        assertEquals("test result", result.getData().get("data"));
        assertTrue(result.getExecutionTime() >= 0);

        // Verify action was called with correct parameters
        verify(mockAction).executeAsync(any(Map.class), any(ActionContext.class));
    }

    @Test
    void testExecuteFailure() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action param1=value1");

        // Setup mock action result
        ActionResult mockActionResult = mock(ActionResult.class);
        when(mockActionResult.isSuccess()).thenReturn(false);
        when(mockActionResult.getMessage()).thenReturn("Action failed");

        CompletableFuture<ActionResult> future = CompletableFuture.completedFuture(mockActionResult);
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class))).thenReturn(future);

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Action failed", result.getErrorMessage());
        assertEquals("EXECUTION_FAILED", result.getErrorCode());
        assertTrue(result.getExecutionTime() >= 0);
    }

    @Test
    void testExecuteWithException() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action");

        // Setup mock action to throw exception
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Error executing skill"));
        assertTrue(result.getErrorMessage().contains("Test exception"));
        assertEquals("EXECUTION_ERROR", result.getErrorCode());
        assertTrue(result.getExecutionTime() >= 0);
    }

    @Test
    void testExecuteWithEmptyMessage() throws Exception {
        // Setup mock message with no parts
        when(mockMessage.getParts()).thenReturn(java.util.List.of());

        // Setup mock action result
        ActionResult mockActionResult = mock(ActionResult.class);
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockActionResult.getData()).thenReturn(new HashMap<>());

        CompletableFuture<ActionResult> future = CompletableFuture.completedFuture(mockActionResult);
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class))).thenReturn(future);

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testExecuteWithComplexParameters() throws Exception {
        // Setup mock message with complex parameters
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action stringParam=hello intParam=123 boolParam=true");

        // Setup mock action result
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("processed", true);
        resultData.put("count", 3);

        ActionResult mockActionResult = mock(ActionResult.class);
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockActionResult.getData()).thenReturn(resultData);

        CompletableFuture<ActionResult> future = CompletableFuture.completedFuture(mockActionResult);
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class))).thenReturn(future);

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(true, result.getData().get("processed"));
        assertEquals(3, result.getData().get("count"));

        // Verify parameters were extracted correctly
        verify(mockAction).executeAsync(argThat(params -> {
            return params.containsKey("stringParam") && params.containsKey("intParam")
                    && params.containsKey("boolParam");
        }), any(ActionContext.class));
    }

    @Test
    void testExecuteWithNullMessage() {
        // Execute with null message
        assertThrows(AgentSkillException.class, () -> {
            adapter.execute(null);
        });
    }

    @Test
    void testExecuteWithNullActionResult() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action");

        // Setup mock action result
        ActionResult mockActionResult = mock(ActionResult.class);
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockActionResult.getData()).thenReturn(null);

        CompletableFuture<ActionResult> future = CompletableFuture.completedFuture(mockActionResult);
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class))).thenReturn(future);

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData()); // Should be empty map, not null
    }

    @Test
    void testExecuteWithInterruptedException() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action");

        // Setup mock action to throw InterruptedException
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class)))
                .thenThrow(new InterruptedException("Interrupted"));

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Error executing skill"));
        assertTrue(result.getErrorMessage().contains("Interrupted"));
        assertEquals("EXECUTION_ERROR", result.getErrorCode());
    }

    @Test
    void testExecuteWithA2ASkillException() throws Exception {
        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of(mockTextPart));
        when(mockTextPart.getText()).thenReturn("test.action");

        // Setup mock action to throw A2ASkillException
        when(mockAction.executeAsync(any(Map.class), any(ActionContext.class)))
                .thenThrow(new AgentSkillException("A2A Skill Error"));

        // Execute
        AgentSkillResult result = adapter.execute(mockMessage);

        // Verify
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Error executing skill"));
        assertTrue(result.getErrorMessage().contains("A2A Skill Error"));
        assertEquals("EXECUTION_ERROR", result.getErrorCode());
    }
}
