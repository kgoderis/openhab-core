package org.openhab.core.ai.tool.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.transport.McpServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for McpServlet.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class McpServletTest {

    @Mock
    private ToolRegistry toolRegistry;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private McpServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new McpServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testServletCreation() {
        assertNotNull(servlet);
        assertFalse(servlet.isHealthy()); // Not healthy until tool registry is set
    }

    @Test
    void testDoGetRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");

        // When
        servlet.doGet(request, response);

        // Then
        verify(request).getRequestURI();
        // The actual behavior depends on the parent class implementation
    }

    @Test
    void testDoPostRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        // The actual behavior depends on the parent class implementation
    }

    @Test
    void testDoOptionsRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");

        // When
        servlet.doOptions(request, response);

        // Then
        verify(request).getRequestURI();
        // The actual behavior depends on the parent class implementation
    }

    @Test
    void testSetToolRegistry() {
        // When
        servlet.setToolRegistry(toolRegistry);

        // Then
        // Verify the tool registry is set (this would require reflection or a getter method)
        // For now, we just verify no exception is thrown
        assertDoesNotThrow(() -> servlet.setToolRegistry(toolRegistry));
    }

    @Test
    void testUnsetToolRegistry() {
        // When
        servlet.unsetToolRegistry(toolRegistry);

        // Then
        // Verify no exception is thrown
        assertDoesNotThrow(() -> servlet.unsetToolRegistry(toolRegistry));
    }

    @Test
    void testGetServerStatistics() {
        // When
        McpServlet.ServerStatistics stats = servlet.getServerStatistics();

        // Then
        assertNotNull(stats);
        assertFalse(stats.isSyncServerActive());
        assertFalse(stats.isAsyncServerActive());
        assertFalse(stats.isToolRegistryAvailable());
    }

    @Test
    void testServerStatisticsToString() {
        // When
        McpServlet.ServerStatistics stats = servlet.getServerStatistics();
        String statsString = stats.toString();

        // Then
        assertNotNull(statsString);
        assertTrue(statsString.contains("syncServerActive=false"));
        assertTrue(statsString.contains("asyncServerActive=false"));
        assertTrue(statsString.contains("toolRegistryAvailable=false"));
    }

    @Test
    void testActivateAndDeactivate() {
        // When/Then
        assertDoesNotThrow(() -> servlet.activate());
        assertDoesNotThrow(() -> servlet.deactivate());
    }

    @Test
    void testIsHealthyWithoutToolRegistry() {
        // When
        boolean healthy = servlet.isHealthy();

        // Then
        assertFalse(healthy);
    }

    @Test
    void testServletLifecycle() {
        // Given
        servlet.setToolRegistry(toolRegistry);

        // When
        servlet.activate();

        // Then
        // The servlet should be initialized (though actual server creation depends on tool registry implementation)
        assertDoesNotThrow(() -> servlet.deactivate());
    }

    @Test
    void testMultipleToolRegistryOperations() {
        // When/Then
        assertDoesNotThrow(() -> {
            servlet.setToolRegistry(toolRegistry);
            servlet.unsetToolRegistry(toolRegistry);
            servlet.setToolRegistry(toolRegistry);
        });
    }

    @Test
    void testServletWithNullToolRegistry() {
        // When/Then
        assertDoesNotThrow(() -> servlet.unsetToolRegistry(null));
    }
}
