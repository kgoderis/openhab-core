package org.openhab.core.ai.resource.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.adapter.ResourceInterfaceAdapter;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceMetadata;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.ResourceValidationResult;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

/**
 * Test class for ResourceInterfaceAdapter.
 * 
 * @author Karel Goderis - Initial Contribution
 */
class ResourceInterfaceAdapterTest {

    private TestResource testResource;

    @BeforeEach
    void setUp() {
        testResource = new TestResource();
    }

    @Test
    void testCreateSyncResourceSpecification() {
        Object spec = ResourceInterfaceAdapter.createSyncResourceSpecification(testResource);
        assertNotNull(spec, "Sync resource specification should not be null");
    }

    @Test
    void testCreateAsyncResourceSpecification() {
        Object spec = ResourceInterfaceAdapter.createAsyncResourceSpecification(testResource);
        assertNotNull(spec, "Async resource specification should not be null");
    }

    @Test
    void testValidateParameters() {
        Map<String, Object> validParams = new HashMap<>();
        validParams.put("testParam", "testValue");

        ResourceValidationResult result = ResourceInterfaceAdapter.validateParameters(testResource, validParams);
        assertTrue(result.isValid(), "Validation should pass with valid parameters");
        assertEquals("Validation successful", result.getMessage());
    }

    @Test
    void testValidateParametersWithInvalidParams() {
        Map<String, Object> invalidParams = new HashMap<>();
        // Empty parameters should fail validation

        ResourceValidationResult result = ResourceInterfaceAdapter.validateParameters(testResource, invalidParams);
        assertFalse(result.isValid(), "Validation should fail with invalid parameters");
        assertNotNull(result.getMessage(), "Error message should not be null");
    }

    @Test
    void testValidateParametersWithNullParams() {
        ResourceValidationResult result = ResourceInterfaceAdapter.validateParameters(testResource, null);
        assertFalse(result.isValid(), "Validation should fail with null parameters");
        assertNotNull(result.getMessage(), "Error message should not be null");
    }

    @Test
    void testCreateSyncResourceSpecificationWithNullResource() {
        Object spec = ResourceInterfaceAdapter.createSyncResourceSpecification(null);
        assertNull(spec, "Sync resource specification should be null for null resource");
    }

    @Test
    void testCreateAsyncResourceSpecificationWithNullResource() {
        Object spec = ResourceInterfaceAdapter.createAsyncResourceSpecification(null);
        assertNull(spec, "Async resource specification should be null for null resource");
    }

    @Test
    void testValidateParametersWithNullResource() {
        Map<String, Object> params = new HashMap<>();
        params.put("testParam", "testValue");

        ResourceValidationResult result = ResourceInterfaceAdapter.validateParameters(null, params);
        assertFalse(result.isValid(), "Validation should fail with null resource");
        assertNotNull(result.getMessage(), "Error message should not be null");
    }

    /**
     * Test implementation of ResourceSpecification for testing purposes
     */
    private static class TestResource implements ResourceSpecification {

        @Override
        public String getId() {
            return "test-resource";
        }

        @Override
        public String getName() {
            return "Test Resource";
        }

        @Override
        public String getDescription() {
            return "A test resource for unit testing";
        }

        @Override
        public String getUriPattern() {
            return "test://resource/{id}";
        }

        @Override
        public String getMimeType() {
            return "application/test+json";
        }

        @Override
        public Map<String, Object> getInputSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");

            Map<String, Object> properties = new HashMap<>();
            properties.put("testParam", Map.of("type", "string", "description", "A test parameter"));

            schema.put("properties", properties);
            return schema;
        }

        @Override
        public Map<String, Object> getOutputSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");

            Map<String, Object> properties = new HashMap<>();
            properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));

            schema.put("properties", properties);
            return schema;
        }

        @Override
        public ResourceMetadata getMetadata() {
            Map<String, Object> properties = new HashMap<>();
            properties.put("category", "test");
            properties.put("version", "1.0.0");

            return new ResourceMetadata("1.0.0", "Test Author", properties);
        }

        @Override
        public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
            if (parameters == null || parameters.isEmpty()) {
                return ResourceValidationResult.failure("Parameters cannot be null or empty");
            }

            Object testParam = parameters.get("testParam");
            if (testParam == null || !(testParam instanceof String)) {
                return ResourceValidationResult.failure("testParam is required and must be a string");
            }

            return ResourceValidationResult.success();
        }

        @Override
        public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
            long startTime = System.currentTimeMillis();

            try {
                // Validate parameters
                ResourceValidationResult validation = validateParameters(parameters);
                if (!validation.isValid()) {
                    return ResourceResult.failure(validation.getMessage(), System.currentTimeMillis() - startTime);
                }

                // Return success result
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("testParam", parameters.get("testParam"));

                return ResourceResult.success(result, System.currentTimeMillis() - startTime);

            } catch (Exception e) {
                return ResourceResult.failure("Execution error: " + e.getMessage(),
                        System.currentTimeMillis() - startTime);
            }
        }
    }
}
