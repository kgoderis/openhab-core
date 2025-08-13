package org.openhab.core.ai.stub.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.stub.StubResponse;
import org.openhab.core.ai.stub.StubService;
import org.openhab.core.ai.stub.StubServiceStatistics;

/**
 * Test class for the new Stub* classes with Stub* prefix naming convention.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class StubServiceTest {

    /**
     * Test StubResponse functionality.
     */
    @Test
    void testStubResponse() {
        // Test success response with data
        StubResponse successResponse = StubResponse.success("test data");
        assertTrue(successResponse.isSuccess());
        assertEquals("test data", successResponse.getData().orElse(null));
        assertEquals(200, successResponse.getStatusCode());

        // Test success response with message
        StubResponse messageResponse = StubResponse.success("Operation completed");
        assertTrue(messageResponse.isSuccess());
        assertEquals("Operation completed", messageResponse.getMessage().orElse(null));

        // Test error response
        StubResponse errorResponse = StubResponse.error("Something went wrong");
        assertFalse(errorResponse.isSuccess());
        assertEquals("Something went wrong", errorResponse.getMessage().orElse(null));
        assertEquals(500, errorResponse.getStatusCode());

        // Test error response with custom status code
        StubResponse customErrorResponse = StubResponse.error("Not found", 404);
        assertFalse(customErrorResponse.isSuccess());
        assertEquals(404, customErrorResponse.getStatusCode());

        // Test builder pattern
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        StubResponse builderResponse = StubResponse.builder().success(true).message("Built response").data("test")
                .statusCode(201).headers(headers).processingTimeMs(150).build();

        assertTrue(builderResponse.isSuccess());
        assertEquals("Built response", builderResponse.getMessage().orElse(null));
        assertEquals("test", builderResponse.getData().orElse(null));
        assertEquals(201, builderResponse.getStatusCode());
        assertEquals(headers, builderResponse.getHeaders().orElse(null));
        assertEquals(150, builderResponse.getProcessingTimeMs());
        assertNotNull(builderResponse.getTimestamp());
    }

    /**
     * Test StubServiceStatistics functionality.
     */
    @Test
    void testStubServiceStatistics() {
        StubServiceStatistics stats = new StubServiceStatistics();

        // Test initial state
        assertEquals(0, stats.getRequestCount());
        assertEquals(0, stats.getSuccessCount());
        assertEquals(0, stats.getErrorCount());
        assertEquals(0, stats.getTotalProcessingTimeMs());
        assertEquals(0, stats.getAverageProcessingTimeMs());

        // Test setting values
        stats.setRequestCount(10);
        stats.setSuccessCount(8);
        stats.setErrorCount(2);
        stats.setTotalProcessingTimeMs(1000);

        assertEquals(10, stats.getRequestCount());
        assertEquals(8, stats.getSuccessCount());
        assertEquals(2, stats.getErrorCount());
        assertEquals(1000, stats.getTotalProcessingTimeMs());

        // Test average calculation
        stats.updateAverageProcessingTime();
        assertEquals(100, stats.getAverageProcessingTimeMs());

        // Test reset
        stats.reset();
        assertEquals(0, stats.getRequestCount());
        assertEquals(0, stats.getSuccessCount());
        assertEquals(0, stats.getErrorCount());
        assertEquals(0, stats.getTotalProcessingTimeMs());
        assertEquals(0, stats.getAverageProcessingTimeMs());
    }

    /**
     * Test StubService interface with mock implementation.
     */
    @Test
    void testStubServiceInterface() {
        // Create a mock implementation of StubService
        StubService mockService = new StubService() {
            private boolean running = false;
            private final StubServiceStatistics statistics = new StubServiceStatistics();

            @Override
            public boolean start() {
                running = true;
                return true;
            }

            @Override
            public boolean stop() {
                running = false;
                return true;
            }

            @Override
            public void reset() {
                statistics.reset();
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public StubResponse handleRequest(Object request) {
                return StubResponse.success("Processed: " + request);
            }

            @Override
            public void configure(Object configuration) {
                // Mock configuration
            }

            @Override
            public String getServiceName() {
                return "MockStubService";
            }

            @Override
            public StubServiceStatistics getStatistics() {
                return statistics;
            }
        };

        // Test service lifecycle
        assertFalse(mockService.isRunning());
        assertTrue(mockService.start());
        assertTrue(mockService.isRunning());
        assertTrue(mockService.stop());
        assertFalse(mockService.isRunning());

        // Test request handling
        StubResponse response = mockService.handleRequest("test request");
        assertTrue(response.isSuccess());
        assertEquals("Processed: test request", response.getData().orElse(null));

        // Test service name
        assertEquals("MockStubService", mockService.getServiceName());

        // Test statistics
        assertNotNull(mockService.getStatistics());
    }
}
