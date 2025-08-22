package org.openhab.core.ai.common.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModelResponse} and its nested Builder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ModelResponseTest {

    @Test
    void testBuilderCreation() {
        ModelResponse response = ModelResponse.builder().withContent("Test response").withModelName("test-model")
                .withProviderType("test-provider").build();

        assertEquals("Test response", response.getContent());
        assertEquals("test-model", response.getModelName());
        assertEquals("test-provider", response.getProviderType());
        assertTrue(response.isSuccess());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testDefaultValues() {
        ModelResponse response = ModelResponse.builder().withContent("Test").withModelName("test-model")
                .withProviderType("test-provider").build();

        assertEquals(0, response.getPromptTokens());
        assertEquals(0, response.getCompletionTokens());
        assertEquals(0, response.getTotalTokens());
        assertEquals(0.0, response.getCost());
        assertEquals(0L, response.getResponseTimeMs());
        assertTrue(response.getMetadata().isEmpty());
        assertNull(response.getFinishReason());
    }

    @Test
    void testCustomValues() {
        ModelResponse response = ModelResponse.builder().withContent("Test response").withModelName("test-model")
                .withProviderType("test-provider").withPromptTokens(10).withCompletionTokens(20).withTotalTokens(30)
                .withCost(0.05).withResponseTimeMs(1500L).withFinishReason("stop").build();

        assertEquals(10, response.getPromptTokens());
        assertEquals(20, response.getCompletionTokens());
        assertEquals(30, response.getTotalTokens());
        assertEquals(0.05, response.getCost());
        assertEquals(1500L, response.getResponseTimeMs());
        assertEquals("stop", response.getFinishReason());
    }

    @Test
    void testMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 42);

        ModelResponse response = ModelResponse.builder().withContent("Test").withModelName("test-model")
                .withProviderType("test-provider").withMetadata(metadata).build();

        assertEquals("value1", response.getMetadata().get("key1"));
        assertEquals(42, response.getMetadata().get("key2"));
        assertEquals(2, response.getMetadata().size());
    }

    @Test
    void testErrorResponse() {
        ModelResponse response = ModelResponse.builder().withContent("").withModelName("test-model")
                .withProviderType("test-provider").withErrorMessage("Test error").build();

        assertFalse(response.isSuccess());
        assertEquals("Test error", response.getErrorMessage());
    }

    @Test
    void testStaticFactoryMethods() {
        ModelResponse success = ModelResponse.success("Success content", "test-model", "test-provider");
        assertEquals("Success content", success.getContent());
        assertTrue(success.isSuccess());
        assertNull(success.getErrorMessage());

        ModelResponse error = ModelResponse.error("Error message", "test-model", "test-provider");
        assertEquals("", error.getContent());
        assertFalse(error.isSuccess());
        assertEquals("Error message", error.getErrorMessage());
    }

    @Test
    void testToBuilder() {
        ModelResponse original = ModelResponse.builder().withContent("Original content").withModelName("original-model")
                .withProviderType("original-provider").withPromptTokens(10).withCost(0.05).build();

        ModelResponse modified = original.toBuilder().withContent("Modified content").withModelName("modified-model")
                .build();

        assertEquals("Modified content", modified.getContent());
        assertEquals("modified-model", modified.getModelName());
        assertEquals("original-provider", modified.getProviderType()); // Preserved
        assertEquals(10, modified.getPromptTokens()); // Preserved
        assertEquals(0.05, modified.getCost()); // Preserved
    }

    @Test
    void testValidation() {
        // Test blank content
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("").withModelName("test-model").withProviderType("test-provider")
                    .build();
        });

        // Test blank model name
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("").withProviderType("test-provider")
                    .build();
        });

        // Test blank provider type
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model").withProviderType("")
                    .build();
        });

        // Test negative prompt tokens
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model")
                    .withProviderType("test-provider").withPromptTokens(-1).build();
        });

        // Test negative completion tokens
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model")
                    .withProviderType("test-provider").withCompletionTokens(-1).build();
        });

        // Test negative total tokens
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model")
                    .withProviderType("test-provider").withTotalTokens(-1).build();
        });

        // Test negative response time
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model")
                    .withProviderType("test-provider").withResponseTimeMs(-1).build();
        });

        // Test negative cost
        assertThrows(IllegalArgumentException.class, () -> {
            ModelResponse.builder().withContent("Test content").withModelName("test-model")
                    .withProviderType("test-provider").withCost(-0.01).build();
        });
    }

    @Test
    void testEquality() {
        ModelResponse response1 = ModelResponse.builder().withContent("Test content").withModelName("test-model")
                .withProviderType("test-provider").withPromptTokens(10).withCost(0.05).build();

        ModelResponse response2 = ModelResponse.builder().withContent("Test content").withModelName("test-model")
                .withProviderType("test-provider").withPromptTokens(10).withCost(0.05).build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        ModelResponse response = ModelResponse.builder().withContent("Test content").withModelName("test-model")
                .withProviderType("test-provider").build();

        String toString = response.toString();
        assertTrue(toString.contains("Test content"));
        assertTrue(toString.contains("test-model"));
        assertTrue(toString.contains("test-provider"));
    }
}
