package org.openhab.core.ai.tool.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for ProtocolSecurityFilter.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ProtocolSecurityFilterTest {

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ProtocolSecurityFilter filter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new ProtocolSecurityFilter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testFilterCreation() {
        assertNotNull(filter);
    }

    @Test
    void testInitAndDestroy() throws Exception {
        // When/Then
        assertDoesNotThrow(() -> filter.init(filterConfig));
        assertDoesNotThrow(() -> filter.destroy());
    }

    @Test
    void testDoFilterWithNonHttpRequest() throws Exception {
        // Given
        jakarta.servlet.ServletRequest nonHttpRequest = mock(jakarta.servlet.ServletRequest.class);
        jakarta.servlet.ServletResponse nonHttpResponse = mock(jakarta.servlet.ServletResponse.class);

        // When
        filter.doFilter(nonHttpRequest, nonHttpResponse, filterChain);

        // Then
        verify(filterChain).doFilter(nonHttpRequest, nonHttpResponse);
    }

    @Test
    void testDoFilterWithMcpRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getRequestURI();
        verify(request).getMethod();
        verify(request).getRemoteAddr();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithA2ARequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/a2a/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getRequestURI();
        verify(request).getMethod();
        verify(request).getRemoteAddr();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithUnknownProtocol() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/unknown/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getRequestURI();
        verify(response).setStatus(403); // FORBIDDEN
        verify(response).setContentType("application/json");
        verify(response).getWriter().write(contains("Security validation failed"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithXForwardedFor() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getHeader("X-Forwarded-For");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithXRealIP() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getHeader("X-Real-IP");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithUserAgent() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).getHeader("User-Agent");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testGetFilterStatistics() {
        // When
        ProtocolSecurityFilter.FilterStatistics stats = filter.getFilterStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(0, stats.getTotalRequests());
        assertEquals(0, stats.getBlockedRequests());
        assertTrue(stats.getUptimeMs() >= 0);
        assertEquals(0.0, stats.getBlockRate());
        assertEquals(0.0, stats.getRequestsPerMinute());
    }

    @Test
    void testFilterStatisticsToString() {
        // When
        ProtocolSecurityFilter.FilterStatistics stats = filter.getFilterStatistics();
        String statsString = stats.toString();

        // Then
        assertNotNull(statsString);
        assertTrue(statsString.contains("totalRequests=0"));
        assertTrue(statsString.contains("blockedRequests=0"));
        assertTrue(statsString.contains("blockRate=0.00%"));
        assertTrue(statsString.contains("requestsPerMinute=0.00"));
    }

    @Test
    void testResetStatistics() {
        // Given
        ProtocolSecurityFilter.FilterStatistics initialStats = filter.getFilterStatistics();

        // When
        filter.resetStatistics();

        // Then
        ProtocolSecurityFilter.FilterStatistics resetStats = filter.getFilterStatistics();
        assertEquals(0, resetStats.getTotalRequests());
        assertEquals(0, resetStats.getBlockedRequests());
        assertTrue(resetStats.getUptimeMs() < initialStats.getUptimeMs());
    }

    @Test
    void testActivateAndDeactivate() {
        // When/Then
        assertDoesNotThrow(() -> filter.activate());
        assertDoesNotThrow(() -> filter.deactivate());
    }

    @Test
    void testFilterWithException() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(any(), any());

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterStatisticsWithRequests() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/mcp/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - make multiple requests
        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, response, filterChain);
        }

        // Then
        ProtocolSecurityFilter.FilterStatistics stats = filter.getFilterStatistics();
        assertEquals(5, stats.getTotalRequests());
        assertEquals(0, stats.getBlockedRequests());
        assertEquals(0.0, stats.getBlockRate());
    }

    @Test
    void testFilterStatisticsWithBlockedRequests() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/unknown/test");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - make multiple requests to unknown protocol
        for (int i = 0; i < 3; i++) {
            filter.doFilter(request, response, filterChain);
        }

        // Then
        ProtocolSecurityFilter.FilterStatistics stats = filter.getFilterStatistics();
        assertEquals(3, stats.getTotalRequests());
        assertEquals(3, stats.getBlockedRequests());
        assertEquals(1.0, stats.getBlockRate());
    }
}
