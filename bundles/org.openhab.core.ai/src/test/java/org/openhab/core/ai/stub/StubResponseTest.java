package org.openhab.core.ai.stub;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StubResponse}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class StubResponseTest {

    @Test
    void testBuilderCreation() {
        StubResponse response = StubResponse.builder().withSuccess(true).withMessage("Test message")
                .withData("test data").withStatusCode(200).withHeaders(Map.of("Content-Type", "application/json"))
                .withTimestamp(System.currentTimeMillis()).withProcessingTimeMs(100L).build();

        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage().orElse(null));
        assertEquals("test data", response.getData().orElse(null));
        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getHeader("Content-Type").orElse(null));
        assertEquals(100L, response.getProcessingTimeMs());
    }

    @Test
    void testDefaultValues() {
        StubResponse response = StubResponse.builder().build();

        assertTrue(response.isSuccess());
        assertEquals("", response.getMessage().orElse(null));
        assertTrue(response.getData().isEmpty());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getHeaders().isEmpty());
        assertTrue(response.getProcessingTimeMs() >= 0);
    }

    @Test
    void testToBuilder() {
        StubResponse original = StubResponse.builder().withSuccess(true).withMessage("Original message")
                .withData("original data").withStatusCode(200).build();

        StubResponse modified = original.toBuilder().withMessage("Modified message").withStatusCode(201).build();

        assertTrue(modified.isSuccess());
        assertEquals("Modified message", modified.getMessage().orElse(null));
        assertEquals("original data", modified.getData().orElse(null));
        assertEquals(201, modified.getStatusCode());
    }

    @Test
    void testValidation() {
        // Test invalid status code
        assertThrows(IllegalArgumentException.class, () -> {
            StubResponse.builder().withStatusCode(999).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            StubResponse.builder().withStatusCode(50).build();
        });

        // Test negative timestamp
        assertThrows(IllegalArgumentException.class, () -> {
            StubResponse.builder().withTimestamp(-1L).build();
        });

        // Test negative processing time
        assertThrows(IllegalArgumentException.class, () -> {
            StubResponse.builder().withProcessingTimeMs(-1L).build();
        });
    }

    @Test
    void testImmutability() {
        Map<String, String> originalHeaders = Map.of("key", "value");
        StubResponse response = StubResponse.builder().withHeaders(originalHeaders).build();

        // Verify headers are immutable
        Optional<Map<String, String>> headers = response.getHeaders();
        assertTrue(headers.isPresent());
        assertThrows(UnsupportedOperationException.class, () -> {
            headers.get().put("newKey", "newValue");
        });
    }

    @Test
    void testNullHandling() {
        StubResponse response = StubResponse.builder().withMessage(null).withData(null).withHeaders(null).build();

        assertTrue(response.getMessage().isEmpty());
        assertTrue(response.getData().isEmpty());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    void testEquality() {
        long timestamp = System.currentTimeMillis();
        StubResponse response1 = StubResponse.builder().withSuccess(true).withMessage("test").withData("data")
                .withStatusCode(200).withHeaders(Map.of("key", "value")).withTimestamp(timestamp)
                .withProcessingTimeMs(100L).build();

        StubResponse response2 = StubResponse.builder().withSuccess(true).withMessage("test").withData("data")
                .withStatusCode(200).withHeaders(Map.of("key", "value")).withTimestamp(timestamp)
                .withProcessingTimeMs(100L).build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        StubResponse response = StubResponse.builder().withSuccess(true).withMessage("test message")
                .withData("test data").withStatusCode(200).build();

        String toString = response.toString();
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("message='test message'"));
        assertTrue(toString.contains("data=test data"));
        assertTrue(toString.contains("statusCode=200"));
    }

    @Test
    void testBuilderReuse() {
        StubResponse.Builder builder = StubResponse.builder().withSuccess(true).withMessage("test");

        StubResponse response1 = builder.build();
        StubResponse response2 = builder.withMessage("different").build();

        assertNotEquals(response1, response2);
        assertEquals("test", response1.getMessage().orElse(null));
        assertEquals("different", response2.getMessage().orElse(null));
    }

    @Test
    void testStaticFactoryMethods() {
        // Test success with data
        StubResponse successData = StubResponse.success("test data");
        assertTrue(successData.isSuccess());
        assertEquals("test data", successData.getData().orElse(null));

        // Test success with message
        StubResponse successMessage = StubResponse.success("success message");
        assertTrue(successMessage.isSuccess());
        assertEquals("success message", successMessage.getMessage().orElse(null));

        // Test error with message
        StubResponse error = StubResponse.error("error message");
        assertFalse(error.isSuccess());
        assertEquals("error message", error.getMessage().orElse(null));
        assertEquals(500, error.getStatusCode());

        // Test error with message and status code
        StubResponse errorWithCode = StubResponse.error("not found", 404);
        assertFalse(errorWithCode.isSuccess());
        assertEquals("not found", errorWithCode.getMessage().orElse(null));
        assertEquals(404, errorWithCode.getStatusCode());
    }

    @Test
    void testTypedDataAccess() {
        String testData = "test string";
        StubResponse response = StubResponse.builder().withData(testData).build();

        Optional<String> stringData = response.getData(String.class);
        assertTrue(stringData.isPresent());
        assertEquals(testData, stringData.get());

        Optional<Integer> intData = response.getData(Integer.class);
        assertFalse(intData.isPresent());
    }

    @Test
    void testResponseInterfaceImplementation() {
        StubResponse response = StubResponse.builder().withSuccess(false).withMessage("error occurred")
                .withTimestamp(System.currentTimeMillis()).build();

        assertNotNull(response.getId());
        assertEquals("error occurred", response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testTimestampInstant() {
        long timestamp = System.currentTimeMillis();
        StubResponse response = StubResponse.builder().withTimestamp(timestamp).build();

        Instant instant = response.getTimestampInstant();
        assertEquals(timestamp, instant.toEpochMilli());
    }

    @Test
    void testHeaderAccess() {
        Map<String, String> headers = Map.of("Content-Type", "application/json", "Authorization", "Bearer token");

        StubResponse response = StubResponse.builder().withHeaders(headers).build();

        assertEquals("application/json", response.getHeader("Content-Type").orElse(null));
        assertEquals("Bearer token", response.getHeader("Authorization").orElse(null));
        assertTrue(response.getHeader("NonExistent").isEmpty());
    }
}
