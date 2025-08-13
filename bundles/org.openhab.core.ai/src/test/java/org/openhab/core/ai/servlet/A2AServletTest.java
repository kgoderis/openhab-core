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
import org.openhab.core.ai.agent.transport.AgentServlet;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for A2AServlet.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class A2AServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationContext authenticationContext;

    private AgentServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AgentServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        // Set up authentication context
        when(authenticationContext.getPrincipalId()).thenReturn("test-user");
        when(request.getAttribute("authenticationContext")).thenReturn(authenticationContext);
        when(authenticationManager.validateContext(any(AuthenticationContext.class))).thenReturn(true);
        when(authenticationManager.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);

        // Inject the authentication manager
        servlet.setAuthenticationManager(authenticationManager);
    }

    @Test
    void testServletCreation() {
        assertNotNull(servlet);
        assertFalse(servlet.isHealthy()); // Not healthy until request handler is set
    }

    @Test
    void testActivateAndDeactivate() {
        // When/Then
        assertDoesNotThrow(() -> servlet.activate());
        assertDoesNotThrow(() -> servlet.deactivate());
    }

    @Test
    void testIsHealthyWithoutRequestHandler() {
        // When
        boolean healthy = servlet.isHealthy();

        // Then
        assertFalse(healthy);
    }

    @Test
    void testServletLifecycle() {
        // When
        servlet.activate();

        // Then
        // The servlet should be initialized
        assertDoesNotThrow(() -> servlet.deactivate());
    }

    @Test
    void testGetServerStatistics() {
        // When
        AgentServlet.ServerStatistics stats = servlet.getServerStatistics();

        // Then
        assertNotNull(stats);
        assertFalse(stats.isRequestHandlerActive());
        assertEquals(4, stats.getEndpointCount());
    }

    @Test
    void testServerStatisticsToString() {
        // When
        AgentServlet.ServerStatistics stats = servlet.getServerStatistics();
        String statsString = stats.toString();

        // Then
        assertNotNull(statsString);
        assertTrue(statsString.contains("requestHandlerActive=false"));
        assertTrue(statsString.contains("endpointCount=4"));
    }

    @Test
    void testAuthenticationManagerInjection() {
        // Given
        AuthenticationManager newAuthManager = mock(AuthenticationManager.class);

        // When
        servlet.setAuthenticationManager(newAuthManager);

        // Then
        // No exception should be thrown
        assertDoesNotThrow(() -> servlet.unsetAuthenticationManager(newAuthManager));
    }

    @Test
    void testUnsetAuthenticationManager() {
        // When/Then
        assertDoesNotThrow(() -> servlet.unsetAuthenticationManager(authenticationManager));
    }
}
