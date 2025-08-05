package org.openhab.core.ai.common.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.api.agent.Agent;

/**
 * Test class for the new AgentAgent interface with Agent* prefix naming convention.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentAgentTest {

    /**
     * Test AgentAgent interface functionality with a mock implementation.
     */
    @Test
    void testAgentAgentInterface() throws InterruptedException, ExecutionException {
        // Create a mock implementation of AgentAgent
        Agent mockAgent = new MockAgentAgent();

        // Test basic agent properties
        assertEquals("test-agent", mockAgent.getAgentId());
        assertEquals("Test Agent", mockAgent.getAgentName());
        assertEquals("1.0.0", mockAgent.getAgentVersion());

        // Test capabilities
        String[] capabilities = mockAgent.getCapabilities();
        assertNotNull(capabilities);
        assertEquals(2, capabilities.length);
        assertTrue(mockAgent.hasCapability("task-execution"));
        assertTrue(mockAgent.hasCapability("streaming"));
        assertFalse(mockAgent.hasCapability("nonexistent"));

        // Test agent status
        assertEquals(Agent.AgentStatus.RUNNING, mockAgent.getStatus());

        // Test health check
        assertTrue(mockAgent.isHealthy());

        // Test task limits
        assertEquals(10, mockAgent.getMaxConcurrentTasks());
        assertEquals(0, mockAgent.getActiveTaskCount());

        // Test metrics
        Agent.AgentMetrics metrics = mockAgent.getMetrics();
        assertNotNull(metrics);
        assertEquals(100, metrics.getSuccessCount());
        assertEquals(5, metrics.getFailureCount());
        assertEquals(105, metrics.getTotalExecutions());
        assertEquals(150.0, metrics.getAverageExecutionTime(), 0.01);

        // Test start/stop operations
        CompletableFuture<Boolean> startResult = mockAgent.start();
        assertTrue(startResult.get());

        CompletableFuture<Boolean> stopResult = mockAgent.stop();
        assertTrue(stopResult.get());
    }

    /**
     * Mock implementation of AgentAgent for testing.
     */
    private static class MockAgentAgent implements Agent {

        @Override
        public String getAgentId() {
            return "test-agent";
        }

        @Override
        public String getAgentName() {
            return "Test Agent";
        }

        @Override
        public String getAgentVersion() {
            return "1.0.0";
        }

        @Override
        public String[] getCapabilities() {
            return new String[] { "task-execution", "streaming" };
        }

        @Override
        public boolean hasCapability(String capability) {
            return capability.equals("task-execution") || capability.equals("streaming");
        }

        @Override
        public CompletableFuture<io.a2a.spec.TaskStatusUpdateEvent> executeTaskSync(io.a2a.spec.Task task) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<io.a2a.spec.TaskStatusUpdateEvent> executeTaskAsync(io.a2a.spec.Task task) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<io.a2a.spec.TaskStatusUpdateEvent> executeTaskStream(io.a2a.spec.Task task,
                java.util.function.Consumer<io.a2a.spec.TaskStatusUpdateEvent> progressCallback) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> cancelTask(String taskId) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public AgentStatus getStatus() {
            return AgentStatus.RUNNING;
        }

        @Override
        public CompletableFuture<Boolean> start() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> stop() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public AgentMetrics getMetrics() {
            return new AgentMetrics() {
                @Override
                public long[] getExecutionTimes() {
                    return new long[] { 100, 150, 200 };
                }

                @Override
                public long getSuccessCount() {
                    return 100;
                }

                @Override
                public long getFailureCount() {
                    return 5;
                }

                @Override
                public double getAverageExecutionTime() {
                    return 150.0;
                }

                @Override
                public long getTotalExecutions() {
                    return 105;
                }
            };
        }

        @Override
        public boolean isHealthy() {
            return true;
        }

        @Override
        public int getMaxConcurrentTasks() {
            return 10;
        }

        @Override
        public int getActiveTaskCount() {
            return 0;
        }
    }
}
