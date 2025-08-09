package org.openhab.core.ai.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.transport.AgentServlet;

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

    private AgentServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AgentServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testServletCreation() {
        assertNotNull(servlet);
        assertFalse(servlet.isHealthy()); // Not healthy until request handler is set
    }

    @Test
    void testDoGetHealthCheck() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/health");

        // When
        servlet.doGet(request, response);

        // Then
        verify(request).getRequestURI();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"status\":\"healthy\""));
        assertTrue(responseContent.contains("\"service\":\"a2a-servlet\""));
    }

    @Test
    void testDoGetStatusCheck() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/status");

        // When
        servlet.doGet(request, response);

        // Then
        verify(request).getRequestURI();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"service\":\"a2a-servlet\""));
        assertTrue(responseContent.contains("\"active\":false"));
        assertTrue(responseContent.contains("\"endpoints\":4"));
    }

    @Test
    void testDoGetUnsupportedEndpoint() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/unsupported");

        // When
        servlet.doGet(request, response);

        // Then
        verify(request).getRequestURI();
        verify(response).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(response).getWriter().write("GET method not supported for this endpoint");
    }

    @Test
    void testDoPostMessageSend() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/message/send");
        String requestBody = "{\"message\":\"test\"}";
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        verify(request).getReader();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"success\":true"));
        assertTrue(responseContent.contains("\"messageId\":"));
    }

    @Test
    void testDoPostTaskGet() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/task/get");
        String requestBody = "{\"taskId\":\"test-task\"}";
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        verify(request).getReader();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"success\":true"));
        assertTrue(responseContent.contains("\"taskId\":"));
        assertTrue(responseContent.contains("\"status\":\"pending\""));
    }

    @Test
    void testDoPostTaskCancel() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/task/cancel");
        String requestBody = "{\"taskId\":\"test-task\"}";
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        verify(request).getReader();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"success\":true"));
        assertTrue(responseContent.contains("\"cancelled\":true"));
    }

    @Test
    void testDoPostUnsupportedEndpoint() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/unsupported");

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).getWriter().write("Endpoint not found");
    }

    @Test
    void testDoOptions() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/test");

        // When
        servlet.doOptions(request, response);

        // Then
        verify(request).getRequestURI();
        verify(response).setHeader("Access-Control-Allow-Origin", "*");
        verify(response).setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        verify(response).setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testDoPostWithInvalidJson() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/message/send");
        String invalidJson = "invalid json";
        BufferedReader reader = new BufferedReader(new StringReader(invalidJson));
        when(request.getReader()).thenReturn(reader);

        // When
        servlet.doPost(request, response);

        // Then
        verify(request).getRequestURI();
        verify(request).getReader();
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"error\":true"));
        assertTrue(responseContent.contains("Internal server error"));
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
}
