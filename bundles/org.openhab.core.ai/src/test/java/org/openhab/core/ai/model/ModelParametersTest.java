package org.openhab.core.ai.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModelParameters} and its nested Builder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ModelParametersTest {

    @Test
    void testBuilderCreation() {
        ModelParameters params = ModelParameters.builder().build();

        assertEquals(0.7, params.getTemperature());
        assertEquals(2048, params.getMaxTokens());
        assertNull(params.getModel());
        assertTrue(params.getAdditionalParams().isEmpty());
        assertFalse(params.isStream());
        assertNull(params.getSystemPrompt());
        assertEquals(30000, params.getTimeoutMs());
    }

    @Test
    void testCustomValues() {
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("key1", "value1");
        additionalParams.put("key2", 42);

        ModelParameters params = ModelParameters.builder().withTemperature(1.0).withMaxTokens(1024).withModel("gpt-4")
                .withAdditionalParams(additionalParams).withStream(true).withSystemPrompt("You are a helpful assistant")
                .withTimeoutMs(60000).build();

        assertEquals(1.0, params.getTemperature());
        assertEquals(1024, params.getMaxTokens());
        assertEquals("gpt-4", params.getModel());
        assertEquals("value1", params.getAdditionalParams().get("key1"));
        assertEquals(42, params.getAdditionalParams().get("key2"));
        assertTrue(params.isStream());
        assertEquals("You are a helpful assistant", params.getSystemPrompt());
        assertEquals(60000, params.getTimeoutMs());
    }

    @Test
    void testAdditionalParamGetter() {
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("stringParam", "test");
        additionalParams.put("intParam", 123);

        ModelParameters params = ModelParameters.builder().withAdditionalParams(additionalParams).build();

        assertEquals("test", params.<String> getAdditionalParam("stringParam"));
        assertEquals(123, params.<Integer> getAdditionalParam("intParam"));
        assertNull(params.<String> getAdditionalParam("nonexistent"));
    }

    @Test
    void testToBuilder() {
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("key", "value");

        ModelParameters original = ModelParameters.builder().withTemperature(0.9).withMaxTokens(4096)
                .withModel("claude-3").withAdditionalParams(additionalParams).withStream(true)
                .withSystemPrompt("Original prompt").withTimeoutMs(45000).build();

        ModelParameters modified = original.toBuilder().withTemperature(0.5).withModel("gpt-3.5").build();

        assertEquals(0.5, modified.getTemperature());
        assertEquals("gpt-3.5", modified.getModel());
        assertEquals(4096, modified.getMaxTokens()); // Preserved
        assertEquals("value", modified.getAdditionalParams().get("key")); // Preserved
        assertTrue(modified.isStream()); // Preserved
        assertEquals("Original prompt", modified.getSystemPrompt()); // Preserved
        assertEquals(45000, modified.getTimeoutMs()); // Preserved
    }

    @Test
    void testValidation() {
        // Test invalid max tokens
        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withMaxTokens(0).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withMaxTokens(-1).build();
        });

        // Test invalid temperature
        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withTemperature(-0.1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withTemperature(2.1).build();
        });

        // Test invalid timeout
        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withTimeoutMs(0).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ModelParameters.builder().withTimeoutMs(-1).build();
        });
    }

    @Test
    void testValidBoundaryValues() {
        // Test boundary values that should be valid
        ModelParameters params = ModelParameters.builder().withTemperature(0.0).withMaxTokens(1).withTimeoutMs(1)
                .build();

        assertEquals(0.0, params.getTemperature());
        assertEquals(1, params.getMaxTokens());
        assertEquals(1, params.getTimeoutMs());

        ModelParameters params2 = ModelParameters.builder().withTemperature(2.0).build();
        assertEquals(2.0, params2.getTemperature());
    }

    @Test
    void testImmutability() {
        Map<String, Object> originalParams = new HashMap<>();
        originalParams.put("key", "value");

        ModelParameters params = ModelParameters.builder().withAdditionalParams(originalParams).build();

        // Modify the original map
        originalParams.put("newKey", "newValue");

        // The params should not be affected
        assertFalse(params.getAdditionalParams().containsKey("newKey"));
        assertEquals(1, params.getAdditionalParams().size());

        // Try to modify the returned map
        assertThrows(UnsupportedOperationException.class, () -> {
            params.getAdditionalParams().put("anotherKey", "anotherValue");
        });
    }

    @Test
    void testNullHandling() {
        // Test null model (should be allowed)
        ModelParameters params = ModelParameters.builder().withModel(null).build();
        assertNull(params.getModel());

        // Test null system prompt (should be allowed)
        ModelParameters params2 = ModelParameters.builder().withSystemPrompt(null).build();
        assertNull(params2.getSystemPrompt());

        // Test null additional params (should throw)
        assertThrows(NullPointerException.class, () -> {
            ModelParameters.builder().withAdditionalParams(null).build();
        });
    }

    @Test
    void testEquality() {
        Map<String, Object> params1 = new HashMap<>();
        params1.put("key", "value");

        Map<String, Object> params2 = new HashMap<>();
        params2.put("key", "value");

        ModelParameters model1 = ModelParameters.builder().withTemperature(0.8).withMaxTokens(1000)
                .withAdditionalParams(params1).build();

        ModelParameters model2 = ModelParameters.builder().withTemperature(0.8).withMaxTokens(1000)
                .withAdditionalParams(params2).build();

        // Note: Since ModelParameters doesn't override equals/hashCode,
        // these will not be equal. This test verifies the current behavior.
        assertNotEquals(model1, model2);
    }

    @Test
    void testToString() {
        ModelParameters params = ModelParameters.builder().withTemperature(0.7).withMaxTokens(2048)
                .withModel("test-model").build();

        String toString = params.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ModelParameters"));
    }
}
