/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.servlet;

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
import org.openhab.core.ai.tool.server.transport.ToolServlet;

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

    private ToolServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ToolServlet();
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
        ToolServlet.ServerStatistics stats = servlet.getServerStatistics();

        // Then
        assertNotNull(stats);
        assertFalse(stats.isSyncServerActive());
        assertFalse(stats.isAsyncServerActive());
        assertFalse(stats.isToolRegistryAvailable());
    }

    @Test
    void testServerStatisticsToString() {
        // When
        ToolServlet.ServerStatistics stats = servlet.getServerStatistics();
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
