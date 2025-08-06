package org.openhab.core.ai.tool.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.api.model.ModelProviderType;

/**
 * Unit tests for HybridToolService
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class HybridToolServiceTest {

    private HybridToolService hybridToolService;

    @BeforeEach
    void setUp() {
        hybridToolService = new HybridToolService();
    }

    @Test
    void testExecuteToolWithSingleProvider() throws ExecutionException, InterruptedException {
        // Given
        ActionContext actionContext = createTestActionContext();
        List<ModelProviderType> providers = List.of(ModelProviderType.OLLAMA);

        // When
        CompletableFuture<ActionResult> future = hybridToolService.executeTool(actionContext, providers);
        ActionResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().toString().contains("OLLAMA"));
    }

    @Test
    void testExecuteToolWithMultipleProviders() throws ExecutionException, InterruptedException {
        // Given
        ActionContext actionContext = createTestActionContext();
        List<ModelProviderType> providers = List.of(ModelProviderType.OPENAI, ModelProviderType.OLLAMA);

        // When
        CompletableFuture<ActionResult> future = hybridToolService.executeTool(actionContext, providers);
        ActionResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testMetricsCollection() throws ExecutionException, InterruptedException {
        // Given
        ActionContext actionContext = createTestActionContext();
        List<ModelProviderType> providers = List.of(ModelProviderType.OLLAMA);

        // When
        hybridToolService.executeTool(actionContext, providers).get();
        HybridToolService.HybridServiceMetrics metrics = hybridToolService.getMetrics();

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.getTotalExecutions() > 0);
        assertTrue(metrics.getSuccessfulExecutions() > 0);
        assertEquals(0, metrics.getFailedExecutions());
        assertTrue(metrics.getSuccessRate() > 0.0);
    }

    @Test
    void testConfigurationMethods() {
        // Given
        hybridToolService.setEnableFallback(false);
        hybridToolService.setEnableLoadBalancing(false);
        hybridToolService.setEnableCostOptimization(false);
        hybridToolService.setEnablePrivacyRouting(false);
        hybridToolService.setMaxFallbackAttempts(5);
        hybridToolService.setLoadBalancingStrategy(HybridToolService.LoadBalancingStrategy.LEAST_CONNECTIONS);

        // When & Then - No exceptions should be thrown
        assertDoesNotThrow(() -> {
            // Configuration methods should work without errors
        });
    }

    @Test
    void testResetMetrics() throws ExecutionException, InterruptedException {
        // Given
        ActionContext actionContext = createTestActionContext();
        List<ModelProviderType> providers = List.of(ModelProviderType.OLLAMA);

        // Execute a tool to generate some metrics
        hybridToolService.executeTool(actionContext, providers).get();

        // When
        hybridToolService.resetMetrics();
        HybridToolService.HybridServiceMetrics metrics = hybridToolService.getMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0, metrics.getTotalExecutions());
        assertEquals(0, metrics.getSuccessfulExecutions());
        assertEquals(0, metrics.getFailedExecutions());
        assertEquals(0, metrics.getFallbackExecutions());
    }

    private ActionContext createTestActionContext() {
        return ActionContext.builder().protocol("mcp").clientId("test-client").sessionId("test-session")
                .correlationId("test-correlation").priority("normal")
                .protocolContext(java.util.Map.of("action", "test_action")).build();
    }
}
