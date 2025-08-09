package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.reasoning.api.ReasoningContext;

/**
 * Test for ContextMemoryManager
 * 
 * @author Karel Goderis - Initial Contribution
 */
class ContextMemoryManagerTest {

    private ContextMemoryManager contextMemoryManager;

    @BeforeEach
    void setUp() {
        contextMemoryManager = new ContextMemoryManager();
        contextMemoryManager.activate();
    }

    @Test
    void testStoreAndRetrieveContext() {
        // Create a test context
        ReasoningContext context = ReasoningContext.builder().initialContext("Initial test context")
                .currentContext("Current test context").domain("test").userId("testUser").build();

        // Store the context
        ContextMemoryManager.ContextStoreResult storeResult = contextMemoryManager.storeContext("test-context-1",
                context, "testUser");
        assertTrue(storeResult.isSuccess());
        assertNotNull(storeResult.getEntry());
        assertEquals("test-context-1", storeResult.getEntry().getContextId());

        // Retrieve the context
        ContextMemoryManager.ContextRetrieveResult retrieveResult = contextMemoryManager
                .retrieveContext("test-context-1", "testUser");
        assertTrue(retrieveResult.isSuccess());
        assertNotNull(retrieveResult.getEntry());
        assertEquals("test-context-1", retrieveResult.getEntry().getContextId());
        assertEquals(context, retrieveResult.getEntry().getContext());
    }

    @Test
    void testUpdateContext() {
        // Create initial context
        ReasoningContext initialContext = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Current context").build();

        contextMemoryManager.storeContext("test-context-2", initialContext, "testUser");

        // Update the context
        ReasoningContext updatedContext = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Updated context").build();

        ContextMemoryManager.ContextUpdateResult updateResult = contextMemoryManager.updateContext("test-context-2",
                updatedContext, "testUser");
        assertTrue(updateResult.isSuccess());
        assertNotNull(updateResult.getEntry());
        assertEquals(updatedContext, updateResult.getEntry().getContext());
    }

    @Test
    void testDeleteContext() {
        // Create and store a context
        ReasoningContext context = ReasoningContext.builder().initialContext("Test context")
                .currentContext("Test context").build();

        contextMemoryManager.storeContext("test-context-3", context, "testUser");

        // Delete the context
        ContextMemoryManager.ContextDeleteResult deleteResult = contextMemoryManager.deleteContext("test-context-3",
                "testUser");
        assertTrue(deleteResult.isSuccess());
        assertNotNull(deleteResult.getEntry());

        // Verify it's gone
        ContextMemoryManager.ContextRetrieveResult retrieveResult = contextMemoryManager
                .retrieveContext("test-context-3", "testUser");
        assertFalse(retrieveResult.isSuccess());
        assertEquals("Context not found: test-context-3", retrieveResult.getError());
    }

    @Test
    void testGetVersionHistory() {
        // Create and store a context
        ReasoningContext context = ReasoningContext.builder().initialContext("Test context")
                .currentContext("Test context").build();

        contextMemoryManager.storeContext("test-context-4", context, "testUser");

        // Get version history
        List<ContextMemoryManager.ContextVersion.VersionEntry> history = contextMemoryManager
                .getVersionHistory("test-context-4", "testUser");
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals(context, history.get(0).getContext());
    }

    @Test
    void testPerformanceMetrics() {
        // Perform some operations
        ReasoningContext context = ReasoningContext.builder().initialContext("Test context")
                .currentContext("Test context").build();

        contextMemoryManager.storeContext("test-context-5", context, "testUser");
        contextMemoryManager.retrieveContext("test-context-5", "testUser");

        // Get performance metrics
        ContextMemoryManager.ContextPerformanceMetrics metrics = contextMemoryManager.getPerformanceMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getTotalStores() > 0);
        assertTrue(metrics.getTotalRetrievals() > 0);
        assertTrue(metrics.getCurrentContextCount() > 0);
    }
}
