package org.openhab.core.ai.resource.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.ResourceValidationResult;
import org.openhab.core.ai.tool.resource.ResourceReadingService;
import org.openhab.core.ai.tool.resource.registry.ResourceRegistry;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

/**
 * Test class for ResourceReadingService.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
class ResourceReadingServiceTest {

    @Mock
    private ResourceRegistry resourceRegistry;

    @Mock
    private ResourceSpecification mockResource;

    private ResourceReadingService readingService;

    @BeforeEach
    void setUp() {
        readingService = new ResourceReadingService();
        // Use reflection to set the mocked registry
        try {
            java.lang.reflect.Field field = ResourceReadingService.class.getDeclaredField("resourceRegistry");
            field.setAccessible(true);
            field.set(readingService, resourceRegistry);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    void testReadResourceSuccess() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters)).thenReturn(ResourceValidationResult.success());

        Map<String, Object> expectedContent = new HashMap<>();
        expectedContent.put("result", "success");
        when(mockResource.execute(parameters, context)).thenReturn(ResourceResult.success(expectedContent, 100L));

        // Act
        ResourceResult result = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getContent());
        assertEquals(expectedContent, result.getContent());
        assertTrue(result.getExecutionTimeMs() > 0);
        assertNull(result.getErrorMessage());
    }

    @Test
    void testReadResourceWithNullResourceId() {
        // Arrange
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        // Act
        ResourceResult result = readingService.readResource(null, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource ID cannot be null or empty"));
    }

    @Test
    void testReadResourceWithEmptyResourceId() {
        // Arrange
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        // Act
        ResourceResult result = readingService.readResource("", parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource ID cannot be null or empty"));
    }

    @Test
    void testReadResourceWithNullRegistry() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        // Set registry to null
        try {
            java.lang.reflect.Field field = ResourceReadingService.class.getDeclaredField("resourceRegistry");
            field.setAccessible(true);
            field.set(readingService, null);
        } catch (Exception e) {
            fail("Failed to set registry to null: " + e.getMessage());
        }

        // Act
        ResourceResult result = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource registry not available"));
    }

    @Test
    void testReadResourceWithResourceNotFound() {
        // Arrange
        String resourceId = "non-existent-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(null);

        // Act
        ResourceResult result = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource not found"));
    }

    @Test
    void testReadResourceWithValidationFailure() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters))
                .thenReturn(ResourceValidationResult.failure("Validation failed"));

        // Act
        ResourceResult result = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Parameter validation failed"));
    }

    @Test
    void testReadResourceWithExecutionFailure() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters)).thenReturn(ResourceValidationResult.success());
        when(mockResource.execute(parameters, context)).thenReturn(ResourceResult.failure("Execution failed", 50L));

        // Act
        ResourceResult result = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Execution failed"));
    }

    @Test
    void testSubscribeToResourceSuccess() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);

        // Act
        ResourceResult result = readingService.subscribeToResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getContent());
        assertTrue(result.getExecutionTimeMs() > 0);
        assertNull(result.getErrorMessage());
    }

    @Test
    void testSubscribeToResourceWithNullResourceId() {
        // Arrange
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        // Act
        ResourceResult result = readingService.subscribeToResource(null, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource ID cannot be null or empty"));
    }

    @Test
    void testSubscribeToResourceWithResourceNotFound() {
        // Arrange
        String resourceId = "non-existent-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(null);

        // Act
        ResourceResult result = readingService.subscribeToResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Resource not found"));
    }

    @Test
    void testGetPerformanceMetrics() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters)).thenReturn(ResourceValidationResult.success());
        when(mockResource.execute(parameters, context)).thenReturn(ResourceResult.success(new HashMap<>(), 100L));

        // Act - perform some reads
        readingService.readResource(resourceId, parameters, context);
        readingService.readResource(resourceId, parameters, context);

        Map<String, Object> metrics = readingService.getPerformanceMetrics();

        // Assert
        assertNotNull(metrics);
        assertEquals(2L, metrics.get("totalReads"));
        assertTrue((Long) metrics.get("cacheHits") >= 0);
        assertTrue((Long) metrics.get("cacheMisses") >= 0);
        assertTrue((Double) metrics.get("cacheHitRate") >= 0.0);
        assertTrue((Double) metrics.get("averageReadTime") > 0.0);
        assertTrue((Integer) metrics.get("cacheSize") >= 0);
    }

    @Test
    void testClearCache() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters)).thenReturn(ResourceValidationResult.success());
        when(mockResource.execute(parameters, context)).thenReturn(ResourceResult.success(new HashMap<>(), 100L));

        // Act - perform a read to populate cache
        readingService.readResource(resourceId, parameters, context);

        // Verify cache has content
        Map<String, Object> metricsBefore = readingService.getPerformanceMetrics();
        assertTrue((Integer) metricsBefore.get("cacheSize") > 0);

        // Clear cache
        readingService.clearCache();

        // Verify cache is empty
        Map<String, Object> metricsAfter = readingService.getPerformanceMetrics();
        assertEquals(0, metricsAfter.get("cacheSize"));
    }

    @Test
    void testCacheFunctionality() {
        // Arrange
        String resourceId = "test-resource";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        ResourceContext context = new ResourceContext();

        when(resourceRegistry.getResource(resourceId)).thenReturn(mockResource);
        when(mockResource.validateParameters(parameters)).thenReturn(ResourceValidationResult.success());
        when(mockResource.execute(parameters, context)).thenReturn(ResourceResult.success(new HashMap<>(), 100L));

        // Act - perform first read (cache miss)
        ResourceResult result1 = readingService.readResource(resourceId, parameters, context);

        // Perform second read with same parameters (should be cache hit)
        ResourceResult result2 = readingService.readResource(resourceId, parameters, context);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());

        // Verify metrics show cache hit
        Map<String, Object> metrics = readingService.getPerformanceMetrics();
        assertEquals(2L, metrics.get("totalReads"));
        assertTrue((Long) metrics.get("cacheHits") > 0);
        assertTrue((Long) metrics.get("cacheMisses") > 0);
    }
}
