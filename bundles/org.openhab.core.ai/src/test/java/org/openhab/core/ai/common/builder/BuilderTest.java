package org.openhab.core.ai.common.builder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Builder interface and AbstractBuilder class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class BuilderTest {

    @Test
    void testAbstractBuilderValidation() {
        TestBuilder builder = new TestBuilder();

        // Initially should be invalid (missing required field)
        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors().contains("name is required"));

        // Set required field
        builder.withName("test");
        assertTrue(builder.isValid());
        assertNull(builder.getValidationErrors());
    }

    @Test
    void testAbstractBuilderReset() {
        TestBuilder builder = new TestBuilder();

        // Set some values
        builder.withName("test").withValue(42);
        assertTrue(builder.isValid());

        // Reset should clear all values
        builder.reset();
        assertFalse(builder.isValid());
        assertTrue(builder.getValidationErrors().contains("name is required"));
    }

    @Test
    void testAbstractBuilderValidationMethods() {
        TestBuilder builder = new TestBuilder();

        // Test required validation
        assertFalse(builder.validateRequired(null, "testField"));
        assertTrue(builder.validateRequired("value", "testField"));

        // Test required string validation
        assertFalse(builder.validateRequiredString(null, "testField"));
        assertFalse(builder.validateRequiredString("", "testField"));
        assertFalse(builder.validateRequiredString("   ", "testField"));
        assertTrue(builder.validateRequiredString("value", "testField"));

        // Test range validation
        assertFalse(builder.validateRange(0, "testField", 1, 10));
        assertFalse(builder.validateRange(11, "testField", 1, 10));
        assertTrue(builder.validateRange(5, "testField", 1, 10));

        // Test positive validation
        assertFalse(builder.validatePositive(0, "testField"));
        assertFalse(builder.validatePositive(-1, "testField"));
        assertTrue(builder.validatePositive(1, "testField"));

        // Test non-negative validation
        assertFalse(builder.validateNonNegative(-1, "testField"));
        assertTrue(builder.validateNonNegative(0, "testField"));
        assertTrue(builder.validateNonNegative(1, "testField"));
    }

    @Test
    void testBuilderBuildWithValidation() {
        TestBuilder builder = new TestBuilder();

        // Should throw exception when invalid
        assertThrows(IllegalStateException.class, () -> builder.build());

        // Should build successfully when valid
        builder.withName("test");
        TestObject result = builder.build();
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    /**
     * Test implementation of AbstractBuilder for testing purposes.
     */
    private static class TestBuilder extends AbstractBuilder<TestObject> {
        private String name = "";
        private int value = 0;

        public TestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TestBuilder withValue(int value) {
            this.value = value;
            return this;
        }

        @Override
        protected void validate() {
            validateRequiredString(name, "name");
            validateNonNegative(value, "value");
        }

        @Override
        protected void doReset() {
            name = "";
            value = 0;
        }

        @Override
        public TestObject build() {
            if (!isValid()) {
                throw new IllegalStateException("Invalid configuration: " + getValidationErrors());
            }
            return new TestObject(name, value);
        }
    }

    /**
     * Test object for builder testing.
     */
    private static class TestObject {
        private final String name;
        private final int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}
