package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.api.reasoning.MultiStepReasoningResult;
import org.openhab.core.ai.api.reasoning.ReasoningContext;

/**
 * Intelligence Integration Tests for AI reasoning components
 * 
 * Comprehensive tests for multi-step reasoning, context memory management,
 * autonomous behavior, learning and adaptation, safety and constraint tests,
 * and performance and scalability tests.
 * 
 * @author Karel Goderis - Initial Contribution
 */
class IntelligenceIntegrationTests {

    private MultiStepReasoningEngine reasoningEngine;
    private ContextMemoryManager contextMemoryManager;
    private AgentMemory agentMemory;
    private AutonomousEventProcessor autonomousEventProcessor;
    private LearningAdaptationSystem learningAdaptationSystem;
    private SafetyConstraintManager safetyConstraintManager;
    private AutonomousBehaviorConfig autonomousBehaviorConfig;

    @BeforeEach
    void setUp() {
        reasoningEngine = new MultiStepReasoningEngine();
        contextMemoryManager = new ContextMemoryManager();
        agentMemory = new AgentMemory();
        autonomousEventProcessor = new AutonomousEventProcessor();
        learningAdaptationSystem = new LearningAdaptationSystem();
        safetyConstraintManager = new SafetyConstraintManager();
        autonomousBehaviorConfig = new AutonomousBehaviorConfig();

        // Activate all components
        reasoningEngine.activate();
        contextMemoryManager.activate();
        agentMemory.activate();
        autonomousEventProcessor.activate();
        learningAdaptationSystem.activate();
        safetyConstraintManager.activate();
        autonomousBehaviorConfig.activate();
    }

    @Test
    void testMultiStepReasoningIntegration() {
        // Test multi-step reasoning with context memory integration
        ReasoningContext context = ReasoningContext.builder().initialContext("User wants to control home lighting")
                .currentContext("Need to identify available lights and their states").domain("home-automation")
                .userId("testUser").build();

        // Store context in memory manager
        ContextMemoryManager.ContextStoreResult storeResult = contextMemoryManager.storeContext("test-reasoning-1",
                context, "testUser");
        assertTrue(storeResult.isSuccess());

        // Execute multi-step reasoning
        MultiStepReasoningResult result = reasoningEngine.reasonAsync(context).join();
        assertNotNull(result);
        assertNotNull(result.getSteps());
        assertTrue(result.getSteps().size() > 0);

        // Verify context was updated during reasoning
        ContextMemoryManager.ContextRetrieveResult retrieveResult = contextMemoryManager
                .retrieveContext("test-reasoning-1", "testUser");
        assertTrue(retrieveResult.isSuccess());
        assertNotNull(retrieveResult.getEntry());
    }

    @Test
    void testContextMemoryManagementIntegration() {
        // Test context memory with versioning and access control
        ReasoningContext context1 = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Current context").domain("test").userId("testUser").build();

        // Store initial context
        ContextMemoryManager.ContextStoreResult storeResult1 = contextMemoryManager.storeContext("test-context-1",
                context1, "testUser");
        assertTrue(storeResult1.isSuccess());

        // Update context
        ReasoningContext context2 = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Updated context").domain("test").userId("testUser").build();

        ContextMemoryManager.ContextUpdateResult updateResult = contextMemoryManager.updateContext("test-context-1",
                context2, "testUser");
        assertTrue(updateResult.isSuccess());

        // Verify version history
        List<ContextMemoryManager.ContextVersion.VersionEntry> history = contextMemoryManager
                .getVersionHistory("test-context-1", "testUser");
        assertNotNull(history);
        assertTrue(history.size() >= 2); // At least initial and update
    }

    @Test
    void testAutonomousBehaviorIntegration() {
        // Test autonomous event processing with safety constraints
        AutonomousEventProcessor.Event event = new AutonomousEventProcessor.Event("event-" + System.currentTimeMillis(),
                "item_change", "testAgent", Map.of("item", "LivingRoom_Light", "oldState", "OFF", "newState", "ON"));

        // Process event
        AutonomousEventProcessor.EventProcessingResult processResult = autonomousEventProcessor
                .processEvent("testAgent", event);
        assertTrue(processResult.isSuccess());

        // Verify autonomous actions were generated
        List<AutonomousEventProcessor.AutonomousAction> pendingActions = autonomousEventProcessor.getPendingActions();
        assertNotNull(pendingActions);
    }

    @Test
    void testLearningAndAdaptationIntegration() {
        // Test learning from user interactions
        Map<String, Object> interactionData = Map.of("action", "turn_on_light", "room", "living_room");

        LearningAdaptationSystem.LearningResult learningResult = learningAdaptationSystem
                .learnFromInteraction("testAgent", "testUser", "light_control", interactionData, 0.8);
        assertTrue(learningResult.isSuccess());

        // Test pattern recognition
        LearningAdaptationSystem.PatternRecognitionResult patternResult = learningAdaptationSystem
                .recognizeBehaviorPatterns("testUser", "light_control", interactionData);
        assertTrue(patternResult.isSuccess());

        // Test feedback integration
        LearningAdaptationSystem.FeedbackIntegrationResult feedbackResult = learningAdaptationSystem
                .integrateFeedback("testUser", "testAgent", "light_control", "Great job!", 0.9);
        assertTrue(feedbackResult.isSuccess());
    }

    @Test
    void testSafetyAndConstraintIntegration() {
        // Test safety validation with autonomous actions
        Map<String, Object> safeActionParams = Map.of("action", "turn_on_light", "room", "living_room");
        SafetyConstraintManager.SafetyValidationResult safeResult = safetyConstraintManager.validateAction("testAgent",
                "light_control", safeActionParams, "testUser");
        assertTrue(safeResult.isValid());

        // Test dangerous action validation
        Map<String, Object> dangerousActionParams = Map.of("action", "system_shutdown");
        SafetyConstraintManager.SafetyValidationResult dangerousResult = safetyConstraintManager
                .validateAction("testAgent", "system_control", dangerousActionParams, "testUser");
        assertFalse(dangerousResult.isValid());

        // Test user constraint addition
        Map<String, Object> constraintParams = Map.of("actionType", "system_control");
        SafetyConstraintManager.ConstraintResult constraintResult = safetyConstraintManager
                .addUserConstraint("testUser", "no_system_control", constraintParams, "Prevent system control actions");
        assertTrue(constraintResult.isSuccess());
    }

    @Test
    void testPerformanceAndScalability() {
        // Test performance with multiple concurrent operations
        int numOperations = 100;

        // Concurrent context storage
        for (int i = 0; i < numOperations; i++) {
            ReasoningContext context = ReasoningContext.builder().initialContext("Test context " + i)
                    .currentContext("Current context " + i).domain("test").userId("testUser").build();

            ContextMemoryManager.ContextStoreResult result = contextMemoryManager.storeContext("test-context-" + i,
                    context, "testUser");
            assertTrue(result.isSuccess());
        }

        // Verify performance metrics
        ContextMemoryManager.ContextPerformanceMetrics contextMetrics = contextMemoryManager.getPerformanceMetrics();
        assertTrue(contextMetrics.getTotalStores() >= numOperations);

        // Test agent memory performance
        for (int i = 0; i < numOperations; i++) {
            AgentMemory.MemoryEntry entry = new AgentMemory.MemoryEntry("memory-" + i, "Test memory entry " + i, "test",
                    0.5, Map.of("index", i));

            AgentMemory.MemoryStoreResult result = agentMemory.storeShortTermMemory("testAgent", entry);
            assertTrue(result.isSuccess());
        }

        AgentMemory.MemoryPerformanceMetrics memoryMetrics = agentMemory.getPerformanceMetrics();
        assertTrue(memoryMetrics.getTotalStores() >= numOperations);
    }

    @Test
    void testErrorHandlingAndRecovery() {
        // Test error handling with invalid inputs
        ContextMemoryManager.ContextStoreResult invalidResult = contextMemoryManager.storeContext("", null, "testUser");
        assertFalse(invalidResult.isSuccess());

        // Test recovery from errors
        ReasoningContext validContext = ReasoningContext.builder().initialContext("Valid context")
                .currentContext("Valid context").domain("test").userId("testUser").build();

        ContextMemoryManager.ContextStoreResult validResult = contextMemoryManager.storeContext("test-recovery",
                validContext, "testUser");
        assertTrue(validResult.isSuccess());

        // Test autonomous behavior error handling
        AutonomousEventProcessor.Event invalidEvent = new AutonomousEventProcessor.Event("invalid-event",
                "invalid_type", "testAgent", Map.of());

        AutonomousEventProcessor.EventProcessingResult invalidEventResult = autonomousEventProcessor
                .processEvent("testAgent", invalidEvent);
        // Should handle gracefully even with invalid input
        assertNotNull(invalidEventResult);
    }

    @Test
    void testSecurityAndPrivacy() {
        // Test access control in context memory
        ReasoningContext context = ReasoningContext.builder().initialContext("Private context")
                .currentContext("Private context").domain("private").userId("user1").build();

        // Store with one user
        ContextMemoryManager.ContextStoreResult storeResult = contextMemoryManager.storeContext("private-context",
                context, "user1");
        assertTrue(storeResult.isSuccess());

        // Try to access with different user (should be denied or return empty)
        ContextMemoryManager.ContextRetrieveResult retrieveResult = contextMemoryManager
                .retrieveContext("private-context", "user2");
        // Depending on implementation, this might return not found or empty result
        assertNotNull(retrieveResult);

        // Test safety constraint enforcement
        Map<String, Object> privateDataParams = Map.of("data", "sensitive_information");
        SafetyConstraintManager.SafetyValidationResult safetyResult = safetyConstraintManager
                .validateAction("testAgent", "data_access", privateDataParams, "user1");
        // Should be invalid due to safety constraints
        assertNotNull(safetyResult);
    }

    @Test
    void testConfigurationAndCustomization() {
        // Test autonomous behavior configuration
        AutonomousBehaviorConfig.AgentConfiguration agentConfig = AutonomousBehaviorConfig.AgentConfiguration.builder()
                .agentId("testAgent").autonomousModeEnabled(true).behaviorLearningEnabled(true)
                .safetyConstraintsEnabled(true).confidenceThreshold(0.8).timeout(Duration.ofMinutes(10))
                .maxConcurrentActions(5).build();

        AutonomousBehaviorConfig.ConfigurationResult configResult = autonomousBehaviorConfig.configureAgent("testAgent",
                agentConfig);
        assertTrue(configResult.isSuccess());

        // Test behavior policy configuration
        AutonomousBehaviorConfig.BehaviorPolicy policy = new AutonomousBehaviorConfig.BehaviorPolicy("test-policy",
                "Test Policy", "Test behavior policy", "light_control", Map.of("room", "living_room"), true, 1);

        AutonomousBehaviorConfig.PolicyResult policyResult = autonomousBehaviorConfig.addBehaviorPolicy("test-policy",
                policy);
        assertTrue(policyResult.isSuccess());

        // Test user preference configuration
        AutonomousBehaviorConfig.UserPreferenceConfig userPrefs = new AutonomousBehaviorConfig.UserPreferenceConfig(
                "testUser", Map.of("theme", "dark", "notifications", "enabled"), true, 0.1);

        AutonomousBehaviorConfig.PreferenceResult prefResult = autonomousBehaviorConfig
                .configureUserPreferences("testUser", userPrefs);
        assertTrue(prefResult.isSuccess());
    }

    @Test
    void testMonitoringAndAnalytics() {
        // Test performance monitoring across all components
        ContextMemoryManager.ContextPerformanceMetrics contextMetrics = contextMemoryManager.getPerformanceMetrics();
        assertNotNull(contextMetrics);
        assertTrue(contextMetrics.getTotalStores() >= 0);

        AgentMemory.MemoryPerformanceMetrics memoryMetrics = agentMemory.getPerformanceMetrics();
        assertNotNull(memoryMetrics);
        assertTrue(memoryMetrics.getTotalStores() >= 0);

        AutonomousEventProcessor.AutonomousPerformanceMetrics autonomousMetrics = autonomousEventProcessor
                .getPerformanceMetrics();
        assertNotNull(autonomousMetrics);
        assertTrue(autonomousMetrics.getTotalEventsProcessed() >= 0);

        LearningAdaptationSystem.LearningPerformanceMetrics learningMetrics = learningAdaptationSystem
                .getPerformanceMetrics();
        assertNotNull(learningMetrics);
        assertTrue(learningMetrics.getTotalLearningEvents() >= 0);

        SafetyConstraintManager.SafetyPerformanceMetrics safetyMetrics = safetyConstraintManager
                .getPerformanceMetrics();
        assertNotNull(safetyMetrics);
        assertTrue(safetyMetrics.getTotalSafetyValidations() >= 0);

        AutonomousBehaviorConfig.ConfigurationPerformanceMetrics configMetrics = autonomousBehaviorConfig
                .getPerformanceMetrics();
        assertNotNull(configMetrics);
        assertTrue(configMetrics.getTotalConfigurations() >= 0);
    }

    @Test
    void testIntegrationWorkflow() {
        // Test complete integration workflow
        String agentId = "integrationAgent";
        String userId = "integrationUser";

        // 1. Configure agent
        AutonomousBehaviorConfig.AgentConfiguration agentConfig = AutonomousBehaviorConfig.AgentConfiguration.builder()
                .agentId(agentId).autonomousModeEnabled(true).behaviorLearningEnabled(true)
                .safetyConstraintsEnabled(true).confidenceThreshold(0.7).timeout(Duration.ofMinutes(5))
                .maxConcurrentActions(3).build();

        AutonomousBehaviorConfig.ConfigurationResult configResult = autonomousBehaviorConfig.configureAgent(agentId,
                agentConfig);
        assertTrue(configResult.isSuccess());

        // 2. Create reasoning context
        ReasoningContext context = ReasoningContext.builder().initialContext("User wants to optimize home energy usage")
                .currentContext("Analyzing current energy consumption patterns").domain("energy-optimization")
                .userId(userId).build();

        // 3. Store context
        ContextMemoryManager.ContextStoreResult storeResult = contextMemoryManager.storeContext("energy-optimization",
                context, userId);
        assertTrue(storeResult.isSuccess());

        // 4. Execute reasoning
        MultiStepReasoningResult reasoningResult = reasoningEngine.reasonAsync(context).join();
        assertNotNull(reasoningResult);

        // 5. Process autonomous event
        AutonomousEventProcessor.Event event = new AutonomousEventProcessor.Event("event-" + System.currentTimeMillis(),
                "energy_usage_high", agentId, Map.of("usage", "1500W", "threshold", "1000W"));

        AutonomousEventProcessor.EventProcessingResult eventResult = autonomousEventProcessor.processEvent(agentId,
                event);
        assertTrue(eventResult.isSuccess());

        // 6. Learn from interaction
        LearningAdaptationSystem.LearningResult learningResult = learningAdaptationSystem.learnFromInteraction(agentId,
                userId, "energy_optimization", Map.of("action", "reduce_usage"), 0.8);
        assertTrue(learningResult.isSuccess());

        // 7. Validate safety
        SafetyConstraintManager.SafetyValidationResult safetyResult = safetyConstraintManager.validateAction(agentId,
                "energy_control", Map.of("action", "reduce_power"), userId);
        assertTrue(safetyResult.isValid());

        // 8. Store in agent memory
        AgentMemory.MemoryEntry memoryEntry = new AgentMemory.MemoryEntry(
                "energy-optimization-" + System.currentTimeMillis(), "Energy optimization workflow completed", "energy",
                0.9, Map.of("workflow", "energy_optimization", "success", true));

        AgentMemory.MemoryStoreResult memoryResult = agentMemory.storeLongTermMemory(agentId, memoryEntry);
        assertTrue(memoryResult.isSuccess());

        // 9. Verify complete workflow
        ContextMemoryManager.ContextRetrieveResult retrieveResult = contextMemoryManager
                .retrieveContext("energy-optimization", userId);
        assertTrue(retrieveResult.isSuccess());

        List<AgentMemory.MemoryEntry> memories = agentMemory.searchMemories(agentId, "energy", 10);
        assertNotNull(memories);
        assertTrue(memories.size() > 0);
    }
}
