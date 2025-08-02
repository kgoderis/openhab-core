package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2ABundleActivator;
import org.openhab.core.ai.a2a.internal.A2AServerManager;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@ExtendWith(MockitoExtension.class)
class A2ABundleActivatorTest {

    @Mock
    private BundleContext mockBundleContext;

    @Mock
    private ServiceReference<AIActionRegistry> mockServiceReference;

    @Mock
    private AIActionRegistry mockActionRegistry;

    private A2ABundleActivator bundleActivator;

    @BeforeEach
    void setUp() {
        bundleActivator = new A2ABundleActivator();
    }

    @Test
    void testStartWithValidServiceReference() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Execute
        bundleActivator.start(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Verify server manager is accessible
        assertNotNull(bundleActivator.getServerManager());
    }

    @Test
    void testStartWithNullServiceReference() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, never()).getService(any());
    }

    @Test
    void testStartWithNullService() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);
    }

    @Test
    void testStartWithException() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class))
                .thenThrow(new RuntimeException("Service reference error"));

        // Execute - should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bundleActivator.start(mockBundleContext);
        });

        // Verify
        assertEquals("Service reference error", exception.getMessage());
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
    }

    @Test
    void testStopWithValidServiceReference() throws Exception {
        // Setup - start first
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
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
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(null);
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
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
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
    void testGetServerManager() {
        // Test that server manager is accessible
        assertNotNull(bundleActivator.getServerManager());
    }

    @Test
    void testStartStopLifecycle() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Execute start
        bundleActivator.start(mockBundleContext);

        // Verify start
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Execute stop
        bundleActivator.stop(mockBundleContext);

        // Verify stop
        verify(mockBundleContext, times(1)).ungetService(mockServiceReference);
    }

    @Test
    void testMultipleStartStopCycles() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // First cycle
        bundleActivator.start(mockBundleContext);
        bundleActivator.stop(mockBundleContext);

        // Second cycle
        bundleActivator.start(mockBundleContext);
        bundleActivator.stop(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(2)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, times(2)).getService(mockServiceReference);
        verify(mockBundleContext, times(2)).ungetService(mockServiceReference);
    }

    @Test
    void testStartWithServiceReferenceButNullService() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(null);

        // Execute
        bundleActivator.start(mockBundleContext);

        // Verify
        verify(mockBundleContext, times(1)).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, times(1)).getService(mockServiceReference);

        // Should still be able to get server manager
        assertNotNull(bundleActivator.getServerManager());
    }

    @Test
    void testStopWithoutStart() throws Exception {
        // Execute stop without starting first
        assertDoesNotThrow(() -> {
            bundleActivator.stop(mockBundleContext);
        });

        // Verify no service operations
        verify(mockBundleContext, never()).getServiceReference(AIActionRegistry.class);
        verify(mockBundleContext, never()).getService(any());
        verify(mockBundleContext, never()).ungetService(any());
    }

    @Test
    void testBundleActivatorConstructor() {
        // Test that constructor works
        A2ABundleActivator newActivator = new A2ABundleActivator();
        assertNotNull(newActivator);
    }

    @Test
    void testServerManagerConsistency() throws Exception {
        // Setup
        when(mockBundleContext.getServiceReference(AIActionRegistry.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockActionRegistry);

        // Start bundle
        bundleActivator.start(mockBundleContext);

        // Get server manager multiple times
        A2AServerManager serverManager1 = bundleActivator.getServerManager();
        A2AServerManager serverManager2 = bundleActivator.getServerManager();

        // Verify consistency
        assertNotNull(serverManager1);
        assertNotNull(serverManager2);
        assertSame(serverManager1, serverManager2);

        // Stop bundle
        bundleActivator.stop(mockBundleContext);

        // Server manager should still be accessible after stop
        A2AServerManager serverManager3 = bundleActivator.getServerManager();
        assertNotNull(serverManager3);
    }
}
