package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.agent.internal.AgentProtocolHandler;
import org.openhab.core.ai.internal.AIBundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@ExtendWith(MockitoExtension.class)
class A2ABundleActivatorTest {

    @Mock
    private BundleContext mockBundleContext;

    @Mock
    private ServiceReference<ActionRegistry> mockServiceReference;

    @Mock
    private ActionRegistry mockActionRegistry;

    private AIBundleActivator bundleActivator;

    @BeforeEach
    void setUp() {
        bundleActivator = new AIBundleActivator();
    }

    @Test
    void testStartWithValidServiceReference() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Execute
        bundleActivator.start(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Verify protocol handler is accessible
        assertNotNull(bundleActivator.getAgentProtocolHandler());
    }

    @Test
    void testStartWithNullServiceReference() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, never()).getService(any());
    }

    @Test
    void testStartWithNullService() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);
    }

    @Test
    void testStartWithException() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class))
                .thenThrow(new RuntimeException("Service reference error"));

        // Execute - should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        assertEquals("Service reference error", exception.getMessage());
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
    }

    @Test
    void testStopWithValidServiceReference() throws Exception {
        // Setup - start first
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);
        bundleActivator.start(mockBundleContext);

        // Execute
        bundleActivator.stop(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(1)).ungetService(mockServiceReference);
    }

    @Test
    void testStopWithNullServiceReference() throws Exception {
        // Setup - start with null service reference
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(null);
        bundleActivator.start(mockBundleContext);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            bundleActivator.stop(mockBundleContext);
        });

        // Verify
        verify(mockBundleContext, never()).ungetService(any());
    }

    @Test
    void testStopWithException() throws Exception {
        // Setup - start first
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);
        bundleActivator.start(mockBundleContext);

        // Setup - stop throws exception
        doThrow(new RuntimeException("Stop error")).when(mockBundleContext).ungetService(mockServiceReference);

        // Execute - should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bundleActivator.stop(mockBundleContext);
        });

        // Verify
        assertEquals("Stop error", exception.getMessage());
        verify(mockBundleContext, times(1)).ungetService(mockServiceReference);
    }

    @Test
    void testGetAgentProtocolHandler() {
        // Test that protocol handler is accessible
        assertNotNull(bundleActivator.getAgentProtocolHandler());
    }

    @Test
    void testStartStopLifecycle() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Execute start
        bundleActivator.start(mockBundleContext);

        // Verify start
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Execute stop
        bundleActivator.stop(mockBundleContext);

        // Verify stop
        verify(mockBundleContext, times(1)).ungetService(mockServiceReference);
    }

    @Test
    void testMultipleStartStopCycles() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // First cycle
        bundleActivator.start(mockBundleContext);
        bundleActivator.stop(mockBundleContext);

        // Second cycle
        bundleActivator.start(mockBundleContext);
        bundleActivator.stop(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(2)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, times(2)).getService(mockServiceReference);
        verify(mockBundleContext, times(2)).ungetService(mockServiceReference);
    }

    @Test
    void testStartWithServiceReferenceButNullService() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(null);

        // Execute
        bundleActivator.start(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Should still be able to get protocol handler
        assertNotNull(bundleActivator.getAgentProtocolHandler());
    }

    @Test
    void testStopWithoutStart() throws Exception {
        // Execute stop without starting first
        assertDoesNotThrow(() -> {
            bundleActivator.stop(mockBundleContext);
        });

        // Verify no service operations
        verify(mockBundleContext, never()).getServiceReference(ActionRegistry.class);
        verify(mockBundleContext, never()).getService(any());
        verify(mockBundleContext, never()).ungetService(any());
    }

    @Test
    void testBundleActivatorConstructor() {
        // Test that constructor works
        AIBundleActivator newActivator = new AIBundleActivator();
        assertNotNull(newActivator);
    }

    @Test
    void testAgentProtocolHandlerConsistency() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(ActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Start bundle
        bundleActivator.start(mockBundleContext);

        // Get protocol handler multiple times
        AgentProtocolHandler protocolHandler1 = bundleActivator.getAgentProtocolHandler();
        AgentProtocolHandler protocolHandler2 = bundleActivator.getAgentProtocolHandler();

        // Verify consistency
        assertNotNull(protocolHandler1);
        assertNotNull(protocolHandler2);
        assertSame(protocolHandler1, protocolHandler2);

        // Stop bundle
        bundleActivator.stop(mockBundleContext);

        // Protocol handler should still be accessible after stop
        AgentProtocolHandler protocolHandler3 = bundleActivator.getAgentProtocolHandler();
        assertNotNull(protocolHandler3);
    }

    @Test
    void testGetBundleContext() {
        // Test that bundle context is accessible
        assertNull(AIBundleActivator.getBundleContext());

        // After start, it should be set
        try {
            bundleActivator.start(mockBundleContext);
            assertEquals(mockBundleContext, AIBundleActivator.getBundleContext());
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }
}
