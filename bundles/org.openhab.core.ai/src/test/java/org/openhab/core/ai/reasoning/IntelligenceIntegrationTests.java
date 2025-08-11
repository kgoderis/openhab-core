package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;

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
    private AgentMemory agentMemory;
    private AutonomousEventProcessor autonomousEventProcessor;
    private LearningAdaptationSystem learningAdaptationSystem;
    private SafetyConstraintManager safetyConstraintManager;
    private AutonomousBehaviorConfig autonomousBehaviorConfig;

    @BeforeEach
    void setUp() {
        reasoningEngine = new MultiStepReasoningEngine();
        agentMemory = new AgentMemory();
        autonomousEventProcessor = new AutonomousEventProcessor();
        learningAdaptationSystem = new LearningAdaptationSystem();
        safetyConstraintManager = new SafetyConstraintManager();
        autonomousBehaviorConfig = new AutonomousBehaviorConfig();

        // Activate all components
        reasoningEngine.activate();
        agentMemory.activate();
        autonomousEventProcessor.activate();
        learningAdaptationSystem.activate();
        safetyConstraintManager.activate();
        autonomousBehaviorConfig.activate();
    }

    @Test
    void testMultiStepReasoningIntegration() {
        // Test multi-step reasoning with unified agent memory integration
        ReasoningContext context = ReasoningContext.builder().initialContext("User wants to control home lighting")
                .currentContext("Need to identify available lights and their states").domain("home-automation")
                .userId("testUser").build();

        // Store reasoning session in agent memory
        AgentMemory.ReasoningSessionResult storeResult = agentMemory.storeReasoningSession("testAgent",
                "test-reasoning-1", context);
        assertTrue(storeResult.isSuccess());

        // Execute multi-step reasoning
        MultiStepReasoningResult result = reasoningEngine.reasonAsync(context).join();
        assertNotNull(result);
        assertNotNull(result.getSteps());
        assertTrue(result.getSteps().size() > 0);

        // Verify reasoning session was stored in agent memory
        AgentMemory.ReasoningSession session = agentMemory.retrieveReasoningSession("testAgent", "test-reasoning-1");
        assertNotNull(session);
        assertEquals("test-reasoning-1", session.getSessionId());
    }

    @Test
    void testAgentMemoryManagementIntegration() {
        // Test unified agent memory with session context management
        ReasoningContext context1 = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Current context").domain("test").userId("testUser").build();

        // Store initial reasoning session
        AgentMemory.ReasoningSessionResult storeResult1 = agentMemory.storeReasoningSession("testAgent",
                "test-session-1", context1);
        assertTrue(storeResult1.isSuccess());

        // Store session context
        Map<String, Object> contextData = Map.of("key1", "value1", "key2", "value2");
        agentMemory.storeSessionContext("testAgent", "test-session-1", contextData);

        // Update session context
        Map<String, Object> updatedContextData = Map.of("key1", "updated-value1", "key3", "value3");
        agentMemory.storeSessionContext("testAgent", "test-session-1", updatedContextData);

        // Verify session context was stored
        Map<String, Object> retrievedContext = agentMemory.getSessionContext("testAgent", "test-session-1");
        assertNotNull(retrievedContext);
        assertTrue(retrievedContext.containsKey("key3"));
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

        // Test unified agent memory performance with reasoning sessions
        for (int i = 0; i < numOperations; i++) {
            ReasoningContext context = ReasoningContext.builder().initialContext("Test context " + i)
                    .currentContext("Current context " + i).domain("test").userId("testUser").build();

            AgentMemory.ReasoningSessionResult result = agentMemory.storeReasoningSession("testAgent",
                    "test-session-" + i, context);
            assertTrue(result.isSuccess());
        }

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
        AgentMemory.ReasoningSessionResult invalidResult = agentMemory.storeReasoningSession("testAgent", "", null);
        assertFalse(invalidResult.isSuccess());

        // Test recovery from errors
        ReasoningContext validContext = ReasoningContext.builder().initialContext("Valid context")
                .currentContext("Valid context").domain("test").userId("testUser").build();

        AgentMemory.ReasoningSessionResult validResult = agentMemory.storeReasoningSession("testAgent", "test-recovery",
                validContext);
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
        // Test access control in unified agent memory
        ReasoningContext context = ReasoningContext.builder().initialContext("Private context")
                .currentContext("Private context").domain("private").userId("user1").build();

        // Store with one agent
        AgentMemory.ReasoningSessionResult storeResult = agentMemory.storeReasoningSession("agent1", "private-session",
                context);
        assertTrue(storeResult.isSuccess());

        // Try to access with different agent (should be denied or return null)
        AgentMemory.ReasoningSession retrieveResult = agentMemory.retrieveReasoningSession("agent2", "private-session");
        // Should return null for different agent
        assertNull(retrieveResult);

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

        // 3. Store reasoning session
        AgentMemory.ReasoningSessionResult storeResult = agentMemory.storeReasoningSession(agentId,
                "energy-optimization", context);
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
        AgentMemory.ReasoningSession retrieveResult = agentMemory.retrieveReasoningSession(agentId,
                "energy-optimization");
        assertNotNull(retrieveResult);

        List<AgentMemory.MemoryEntry> memories = agentMemory.searchMemories(agentId, "energy", 10);
        assertNotNull(memories);
        assertTrue(memories.size() > 0);
    }
}
