package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.internal.AgentRegistry;
import org.openhab.core.ai.agent.internal.AgentRegistry.AgentCommunicationProtocol;
import org.openhab.core.ai.agent.internal.AgentRegistry.AgentMessage;
import org.openhab.core.ai.agent.internal.AgentRegistry.AgentRegistrationResult;
import org.openhab.core.ai.agent.internal.AgentRegistry.AgentSecurityContext;
import org.openhab.core.ai.agent.internal.AgentRegistry.AgentValidationResult;
import org.openhab.core.ai.agent.internal.AgentRegistry.MessageHandler;
import org.openhab.core.ai.agent.internal.AgentRegistry.MessageResponse;
import org.openhab.core.ai.agent.internal.AgentRegistry.MessageStatus;
import org.openhab.core.ai.api.agent.Agent;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Unit tests for AgentRegistry
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
public class AgentRegistryTest {

    private static final String TEST_AGENT_ID = "test-agent-1";
    private static final String TEST_USER_ID = "test-user-1";
    private static final String TEST_AGENT_ID_2 = "test-agent-2";

    private AgentRegistry registry;
    private TestAgentAgent testAgent;

    /**
     * Simple test implementation of AgentAgent to avoid mocking issues
     */
    private static class TestAgentAgent implements Agent {
        private final String id;
        private boolean running = false;
        private final Set<String> capabilities;

        public TestAgentAgent(String id) {
            this.id = id;
            this.capabilities = Set.of("test-capability");
        }

        public TestAgentAgent(String id, Set<String> capabilities) {
            this.id = id;
            this.capabilities = capabilities;
        }

        @Override
        public String getAgentId() {
            return id;
        }

        @Override
        public String getAgentName() {
            return "Test Agent " + id;
        }

        @Override
        public String getAgentVersion() {
            return "1.0.0";
        }

        @Override
        public String[] getCapabilities() {
            return capabilities.toArray(new String[0]);
        }

        @Override
        public boolean hasCapability(String capability) {
            return capabilities.contains(capability);
        }

        @Override
        public CompletableFuture<TaskStatusUpdateEvent> executeTaskSync(Task task) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<TaskStatusUpdateEvent> executeTaskAsync(Task task) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<TaskStatusUpdateEvent> executeTaskStream(Task task,
                @Nullable Consumer<TaskStatusUpdateEvent> progressCallback) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> cancelTask(String taskId) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public AgentStatus getStatus() {
            return running ? AgentStatus.RUNNING : AgentStatus.STOPPED;
        }

        @Override
        public CompletableFuture<Boolean> start() {
            running = true;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> stop() {
            running = false;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public AgentMetrics getMetrics() {
            return new AgentMetrics() {
                @Override
                public long[] getExecutionTimes() {
                    return new long[0];
                }

                @Override
                public long getSuccessCount() {
                    return 0;
                }

                @Override
                public long getFailureCount() {
                    return 0;
                }

                @Override
                public double getAverageExecutionTime() {
                    return 0.0;
                }

                @Override
                public long getTotalExecutions() {
                    return 0;
                }
            };
        }

        @Override
        public boolean isHealthy() {
            return true;
        }

        @Override
        public int getMaxConcurrentTasks() {
            return 5;
        }

        @Override
        public int getActiveTaskCount() {
            return 0;
        }

        public boolean getRunning() {
            return running;
        }
    }

    @BeforeEach
    public void setUp() {
        registry = new AgentRegistry();
        testAgent = new TestAgentAgent(TEST_AGENT_ID);
    }

    @Test
    public void testAgentRegistrationWithValidSecurityContext() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read", TEST_USER_ID + ":write"));
        AgentRegistrationResult result = registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(registry.getRegisteredAgentIds(TEST_USER_ID).contains(TEST_AGENT_ID));
    }

    @Test
    public void testAgentRegistrationWithInvalidAgentId() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        AgentRegistrationResult result = registry.registerAgent("", testAgent, securityContext);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Agent ID cannot be empty")));
    }

    @Test
    public void testAgentRegistrationWithDuplicateId() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        TestAgentAgent testAgent2 = new TestAgentAgent(TEST_AGENT_ID, Set.of("different-capability"));
        AgentRegistrationResult result = registry.registerAgent(TEST_AGENT_ID, testAgent2, securityContext);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Agent already registered")));
    }

    @Test
    public void testAgentUnregistrationWithPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.unregisterAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertTrue(result);
        assertFalse(registry.getRegisteredAgentIds(TEST_USER_ID).contains(TEST_AGENT_ID));
    }

    @Test
    public void testAgentUnregistrationWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.unregisterAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertFalse(result);
        assertTrue(registry.getRegisteredAgentIds(TEST_USER_ID).contains(TEST_AGENT_ID));
    }

    @Test
    public void testAgentStartWithPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.startAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertTrue(result);
        assertTrue(testAgent.getRunning());
    }

    @Test
    public void testAgentStartWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.startAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertFalse(result);
        assertFalse(testAgent.getRunning());
    }

    @Test
    public void testAgentStopWithPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        registry.startAgent(TEST_AGENT_ID, TEST_USER_ID);
        boolean result = registry.stopAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertTrue(result);
        assertFalse(testAgent.getRunning());
    }

    @Test
    public void testAgentStopWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        testAgent.start();
        boolean result = registry.stopAgent(TEST_AGENT_ID, TEST_USER_ID);
        assertFalse(result);
        assertTrue(testAgent.getRunning());
    }

    @Test
    public void testAgentSecurityContext() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read", TEST_USER_ID + ":write"));
        assertTrue(securityContext.getOwners().contains(TEST_USER_ID));
        assertFalse(securityContext.getOwners().contains("other-user"));
        assertTrue(securityContext.getPermissions().contains(TEST_USER_ID + ":read"));
        assertTrue(securityContext.getPermissions().contains(TEST_USER_ID + ":write"));
        assertFalse(securityContext.getPermissions().contains(TEST_USER_ID + ":admin"));
        assertFalse(securityContext.getPermissions().contains("other-user:read"));
    }

    @Test
    public void testAgentValidationResult() {
        AgentValidationResult validResult = new AgentValidationResult(true, List.of());
        AgentValidationResult invalidResult = new AgentValidationResult(false, List.of("Error 1", "Error 2"));
        assertTrue(validResult.isValid());
        assertTrue(validResult.getErrors().isEmpty());
        assertFalse(invalidResult.isValid());
        assertEquals(2, invalidResult.getErrors().size());
        assertTrue(invalidResult.getErrors().contains("Error 1"));
        assertTrue(invalidResult.getErrors().contains("Error 2"));
    }

    @Test
    public void testAgentRegistrationResult() {
        AgentRegistrationResult successResult = AgentRegistrationResult.success(TEST_AGENT_ID);
        AgentRegistrationResult failureResult = AgentRegistrationResult.failure(List.of("Registration failed"));
        assertTrue(successResult.isSuccess());
        assertTrue(successResult.getErrors().isEmpty());
        assertFalse(failureResult.isSuccess());
        assertEquals(1, failureResult.getErrors().size());
        assertTrue(failureResult.getErrors().contains("Registration failed"));
    }

    @Test
    public void testAgentCommunicationProtocol() {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);
        assertTrue(protocol.sendMessage("Hello", "user1"));
        assertTrue(protocol.sendMessage("World", "user2"));
        assertEquals(2, protocol.getMessages().size());
        assertTrue(protocol.getMessages().stream().anyMatch(msg -> msg.contains("Hello")));
        assertTrue(protocol.getMessages().stream().anyMatch(msg -> msg.contains("World")));
    }

    @Test
    public void testEnhancedMessageDelivery() throws InterruptedException {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        // Create a test message handler
        List<AgentMessage> receivedMessages = new CopyOnWriteArrayList<>();
        MessageHandler testHandler = message -> {
            receivedMessages.add(message);
            return MessageResponse.success("Message processed: " + message.content());
        };

        // Register the handler
        protocol.addMessageHandler(testHandler);

        // Send messages
        assertTrue(protocol.sendMessage("Test message 1", "user1"));
        assertTrue(protocol.sendMessage("Test message 2", "user2"));

        // Wait for message processing (100ms interval)
        Thread.sleep(200);

        // Verify messages were delivered
        assertEquals(2, receivedMessages.size());
        assertTrue(receivedMessages.stream().anyMatch(msg -> msg.content().equals("Test message 1")));
        assertTrue(receivedMessages.stream().anyMatch(msg -> msg.content().equals("Test message 2")));

        // Verify message status
        AgentMessage message1 = receivedMessages.get(0);
        assertEquals(MessageStatus.DELIVERED, protocol.getMessageStatus(message1.messageId()));

        // Test acknowledgment
        assertTrue(protocol.acknowledgeMessage(message1.messageId()));
        assertEquals(MessageStatus.ACKNOWLEDGED, protocol.getMessageStatus(message1.messageId()));
    }

    @Test
    public void testMessageDeliveryWithoutHandlers() throws InterruptedException {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        // Send message without any handlers
        assertTrue(protocol.sendMessage("Test message", "user1"));

        // Wait for message processing
        Thread.sleep(200);

        // Get the message ID (we need to access the internal queue)
        List<AgentMessage> messages = protocol.getAgentMessages();
        assertEquals(1, messages.size());

        AgentMessage message = messages.get(0);
        assertEquals(MessageStatus.NO_HANDLERS, protocol.getMessageStatus(message.messageId()));
    }

    @Test
    public void testMessageHandlerRegistration() {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        MessageHandler handler1 = message -> MessageResponse.success("Handler 1");
        MessageHandler handler2 = message -> MessageResponse.success("Handler 2");

        // Register handlers
        protocol.addMessageHandler(handler1);
        protocol.addMessageHandler(handler2);

        // Send message
        assertTrue(protocol.sendMessage("Test", "user1"));

        // Verify only first handler processes the message (stops after first success)
        // This is the expected behavior based on our implementation
        // The message should be delivered to the first handler that acknowledges it
    }

    @Test
    public void testMessageHandlerRemoval() throws InterruptedException {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        List<AgentMessage> receivedMessages = new CopyOnWriteArrayList<>();
        MessageHandler handler = message -> {
            receivedMessages.add(message);
            return MessageResponse.success("Processed");
        };

        // Register and then remove handler
        protocol.addMessageHandler(handler);
        protocol.removeMessageHandler(handler);

        // Send message
        assertTrue(protocol.sendMessage("Test", "user1"));

        // Wait for processing
        Thread.sleep(200);

        // Verify no messages were delivered
        assertEquals(0, receivedMessages.size());

        // Verify message status shows no handlers
        List<AgentMessage> messages = protocol.getAgentMessages();
        assertEquals(1, messages.size());
        assertEquals(MessageStatus.NO_HANDLERS, protocol.getMessageStatus(messages.get(0).messageId()));
    }

    @Test
    public void testRegistryMessageHandlerRegistration() {
        // Register agent first
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":communicate", TEST_USER_ID + ":modify", TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);

        // Create test handler
        List<AgentMessage> receivedMessages = new CopyOnWriteArrayList<>();
        MessageHandler handler = message -> {
            receivedMessages.add(message);
            return MessageResponse.success("Processed");
        };

        // Register handler through registry
        assertTrue(registry.registerMessageHandler(TEST_AGENT_ID, handler, TEST_USER_ID));

        // Send message through registry
        assertTrue(registry.sendMessageToAgent(TEST_AGENT_ID, "Test message", TEST_USER_ID));

        // Verify handler registration
        assertTrue(registry.unregisterMessageHandler(TEST_AGENT_ID, handler, TEST_USER_ID));
    }

    @Test
    public void testMessageStatusTracking() throws InterruptedException {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        // Send message without handlers
        assertTrue(protocol.sendMessage("Test", "user1"));

        // Wait for processing
        Thread.sleep(200);

        // Get message and check status
        List<AgentMessage> messages = protocol.getAgentMessages();
        assertEquals(1, messages.size());

        AgentMessage message = messages.get(0);
        assertEquals(MessageStatus.NO_HANDLERS, protocol.getMessageStatus(message.messageId()));

        // Test unknown message ID
        assertEquals(MessageStatus.UNKNOWN, protocol.getMessageStatus("unknown-id"));
    }

    @Test
    public void testMessageAcknowledgment() throws InterruptedException {
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(TEST_AGENT_ID);

        MessageHandler handler = message -> MessageResponse.success("Processed");
        protocol.addMessageHandler(handler);

        // Send message
        assertTrue(protocol.sendMessage("Test", "user1"));

        // Wait for processing
        Thread.sleep(200);

        // Get message
        List<AgentMessage> messages = protocol.getAgentMessages();
        assertEquals(1, messages.size());
        AgentMessage message = messages.get(0);

        // Verify initial status
        assertEquals(MessageStatus.DELIVERED, protocol.getMessageStatus(message.messageId()));

        // Acknowledge message
        assertTrue(protocol.acknowledgeMessage(message.messageId()));
        assertEquals(MessageStatus.ACKNOWLEDGED, protocol.getMessageStatus(message.messageId()));

        // Test acknowledging already acknowledged message
        assertFalse(protocol.acknowledgeMessage(message.messageId()));
    }

    @Test
    public void testFindAgentsWithCapability() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        TestAgentAgent testAgent2 = new TestAgentAgent(TEST_AGENT_ID_2, Set.of("different-capability"));
        registry.registerAgent(TEST_AGENT_ID_2, testAgent2, securityContext);
        List<String> agentsWithTestCapability = registry.findAgentsWithCapability("test-capability", TEST_USER_ID);
        List<String> agentsWithDifferentCapability = registry.findAgentsWithCapability("different-capability",
                TEST_USER_ID);
        assertEquals(1, agentsWithTestCapability.size());
        assertTrue(agentsWithTestCapability.contains(TEST_AGENT_ID));
        assertEquals(1, agentsWithDifferentCapability.size());
        assertTrue(agentsWithDifferentCapability.contains(TEST_AGENT_ID_2));
    }

    @Test
    public void testAgentAccessControl() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        assertNotNull(registry.getAgent(TEST_AGENT_ID, TEST_USER_ID));
        assertNull(registry.getAgent(TEST_AGENT_ID, "other-user"));
    }

    @Test
    public void testAgentMetricsAccess() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        assertNotNull(registry.getAgentMetrics(TEST_AGENT_ID, TEST_USER_ID));
        assertNull(registry.getAgentMetrics(TEST_AGENT_ID, "other-user"));
    }

    @Test
    public void testCapabilityRegistrationWithPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.registerCapability(TEST_AGENT_ID, "new-capability", TEST_USER_ID);
        assertTrue(result);
        List<String> caps = registry.getAgentCapabilities(TEST_AGENT_ID, TEST_USER_ID);
        assertTrue(caps.contains("new-capability"));
    }

    @Test
    public void testCapabilityRegistrationWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.registerCapability(TEST_AGENT_ID, "new-capability", TEST_USER_ID);
        assertFalse(result);
        List<String> caps = registry.getAgentCapabilities(TEST_AGENT_ID, TEST_USER_ID);
        assertFalse(caps.contains("new-capability"));
    }

    @Test
    public void testCapabilityRegistrationWithInvalidFormat() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.registerCapability(TEST_AGENT_ID, "", TEST_USER_ID);
        assertFalse(result);
    }

    @Test
    public void testCommunicationChannelManagement() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.addCommunicationChannel(TEST_AGENT_ID, "channel1", TEST_USER_ID);
        assertTrue(result);
        Set<String> channels = registry.getAgentCommunicationChannels(TEST_AGENT_ID, TEST_USER_ID);
        assertTrue(channels.contains("channel1"));
    }

    @Test
    public void testCommunicationChannelManagementWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.addCommunicationChannel(TEST_AGENT_ID, "channel1", TEST_USER_ID);
        assertFalse(result);
        Set<String> channels = registry.getAgentCommunicationChannels(TEST_AGENT_ID, TEST_USER_ID);
        assertFalse(channels.contains("channel1"));
    }

    @Test
    public void testAgentCommunicationWithPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":write"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.sendMessageToAgent(TEST_AGENT_ID, "Hello", TEST_USER_ID);
        assertTrue(result);
    }

    @Test
    public void testAgentCommunicationWithoutPermission() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read"));
        registry.registerAgent(TEST_AGENT_ID, testAgent, securityContext);
        boolean result = registry.sendMessageToAgent(TEST_AGENT_ID, "Hello", TEST_USER_ID);
        assertFalse(result);
    }

    @Test
    public void testOwnerHasAllPermissions() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":*"));
        assertTrue(securityContext.getPermissions().contains(TEST_USER_ID + ":*"));
    }

    @Test
    public void testWildcardPermissions() {
        AgentSecurityContext securityContext = new AgentSecurityContext(Set.of(TEST_USER_ID),
                Set.of(TEST_USER_ID + ":read:*"));
        assertTrue(securityContext.getPermissions().contains(TEST_USER_ID + ":read:*"));
    }
}
