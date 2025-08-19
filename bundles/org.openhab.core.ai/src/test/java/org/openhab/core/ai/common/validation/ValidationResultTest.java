package org.openhab.core.ai.common.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ValidationResult interface and BaseValidationResult class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ValidationResultTest {

    @Test
    void testValidationResultInterface() {
        // Test that the interface defines the expected methods
        ValidationResult result = new TestValidationResult(true, List.of("error1"), List.of("warning1"),
                Map.of("key", "value"), Instant.now());

        assertTrue(result.isValid());
        assertEquals(List.of("error1"), result.getErrors());
        assertEquals(List.of("warning1"), result.getWarnings());
        assertEquals(Map.of("key", "value"), result.getDetails());
        assertNotNull(result.getValidationTime());

        // Test calculated properties
        assertEquals(1, result.getErrorCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertEquals("Valid with 1 warning(s)", result.getSummary());
    }

    @Test
    void testBaseValidationResult() {
        Instant now = Instant.now();
        BaseValidationResult result = new TestValidationResult(true, List.of("error1"), List.of("warning1"),
                Map.of("key", "value"), now);

        assertTrue(result.isValid());
        assertEquals(List.of("error1"), result.getErrors());
        assertEquals(List.of("warning1"), result.getWarnings());
        assertEquals(Map.of("key", "value"), result.getDetails());
        assertEquals(now, result.getValidationTime());

        // Test equals and hashCode
        BaseValidationResult result2 = new TestValidationResult(true, List.of("error1"), List.of("warning1"),
                Map.of("key", "value"), now);
        assertEquals(result, result2);
        assertEquals(result.hashCode(), result2.hashCode());

        // Test toString
        String toString = result.toString();
        assertTrue(toString.contains("BaseValidationResult"));
        assertTrue(toString.contains("valid=true"));
        assertTrue(toString.contains("errors=[error1]"));
        assertTrue(toString.contains("warnings=[warning1]"));
    }

    @Test
    void testInvalidValidationResult() {
        ValidationResult result = new TestValidationResult(false, List.of("error1", "error2"), List.of(), Map.of(),
                Instant.now());

        assertFalse(result.isValid());
        assertEquals(2, result.getErrorCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals("Invalid with 2 error(s)", result.getSummary());
    }

    @Test
    void testValidValidationResult() {
        ValidationResult result = new TestValidationResult(true, List.of(), List.of(), Map.of(), Instant.now());

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
        assertEquals(0, result.getWarningCount());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals("Valid", result.getSummary());
    }

    @Test
    void testToolValidationResult() {
        ToolValidationResult result = ToolValidationResult.valid();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
        assertEquals(0, result.getWarningCount());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals("Valid", result.getSummary());
        assertNotNull(result.getValidationTime());

        // Test invalid result
        ToolValidationResult invalidResult = ToolValidationResult.invalid(List.of("error1", "error2"));
        assertFalse(invalidResult.isValid());
        assertEquals(2, invalidResult.getErrorCount());
        assertEquals("Invalid with 2 error(s)", invalidResult.getSummary());

        // Test result with warnings
        ToolValidationResult warningResult = ToolValidationResult.withWarnings(List.of("warning1"));
        assertTrue(warningResult.isValid());
        assertEquals(1, warningResult.getWarningCount());
        assertEquals("Valid with 1 warning(s)", warningResult.getSummary());
    }

    @Test
    void testReasoningValidationResult() {
        ReasoningValidationResult result = ReasoningValidationResult.create();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
        assertEquals(0, result.getWarningCount());

        // Test adding errors and warnings
        result.addError("error1");
        result.addError("error2");
        result.addWarning("warning1");

        assertFalse(result.isValid());
        assertEquals(2, result.getErrorCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertEquals("Invalid with 2 error(s)", result.getSummary());

        // Test that getErrors and getWarnings return copies
        List<String> errors = result.getErrors();
        List<String> warnings = result.getWarnings();
        errors.add("error3"); // This should not affect the original
        warnings.add("warning2"); // This should not affect the original

        assertEquals(2, result.getErrorCount());
        assertEquals(1, result.getWarningCount());
    }

    /**
     * Test implementation of ValidationResult for testing purposes.
     */
    private static class TestValidationResult extends BaseValidationResult {
        public TestValidationResult(boolean valid, List<String> errors, List<String> warnings,
                Map<String, Object> details, Instant validationTime) {
            super(valid, errors, warnings, details, validationTime);
        }
    }
}
