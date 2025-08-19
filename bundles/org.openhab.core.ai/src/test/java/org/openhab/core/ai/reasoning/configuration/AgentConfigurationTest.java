package org.openhab.core.ai.reasoning.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AgentConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentConfigurationTest {

    @Test
    void testBasicConfiguration() {
        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").autonomousModeEnabled(true)
                .behaviorLearningEnabled(true).safetyConstraintsEnabled(true).confidenceThreshold(0.8)
                .timeout(Duration.ofMinutes(10)).maxConcurrentActions(5).build();

        assertEquals("test-agent", config.getId());
        assertEquals("test-agent", config.getAgentId());
        assertEquals("Agent Configuration", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertTrue(config.isAutonomousModeEnabled());
        assertTrue(config.isBehaviorLearningEnabled());
        assertTrue(config.isSafetyConstraintsEnabled());
        assertEquals(0.8, config.getConfidenceThreshold());
        assertEquals(Duration.ofMinutes(10), config.getTimeout());
        assertEquals(5, config.getMaxConcurrentActions());
    }

    @Test
    void testBehaviorPolicies() {
        List<String> policies = List.of("energy-efficiency", "safety-first", "user-preference");

        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").behaviorPolicies(policies)
                .build();

        assertEquals(policies, config.getBehaviorPolicies());
    }

    @Test
    void testConstraints() {
        List<String> constraints = List.of("max-power-usage", "min-temperature", "max-brightness");

        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").constraints(constraints).build();

        assertEquals(constraints, config.getConstraints());
    }

    @Test
    void testSafetyPolicies() {
        List<String> safetyPolicies = List.of("emergency-shutdown", "overload-protection", "user-safety");

        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").safetyPolicies(safetyPolicies)
                .build();

        assertEquals(safetyPolicies, config.getSafetyPolicies());
    }

    @Test
    void testCustomSettings() {
        Map<String, Object> customSettings = Map.of("learningRate", 0.01, "maxIterations", 1000, "enableLogging", true);

        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").customSettings(customSettings)
                .build();

        assertEquals(customSettings, config.getCustomSettings());
        assertEquals(customSettings, config.getCustomOptions());
        assertTrue(config.hasCustomOption("learningRate"));
        assertTrue(config.hasCustomOption("maxIterations"));
        assertTrue(config.hasCustomOption("enableLogging"));
        assertEquals(0.01, config.getCustomOption("learningRate"));
        assertEquals(1000, config.getCustomOption("maxIterations"));
        assertEquals(true, config.getCustomOption("enableLogging"));
    }

    @Test
    void testValidation() {
        AgentConfigurationBuilder builder = AgentConfiguration.builder();

        // Test invalid configuration (missing agentId)
        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());

        // Test valid configuration
        builder.agentId("test-agent");
        assertTrue(builder.isValid());

        // Test invalid confidence threshold
        builder.confidenceThreshold(1.5); // > 1.0
        assertFalse(builder.isValid());

        // Test invalid max concurrent actions
        builder.confidenceThreshold(0.8).maxConcurrentActions(0);
        assertFalse(builder.isValid());
    }

    @Test
    void testEquality() {
        AgentConfiguration config1 = AgentConfiguration.builder().agentId("test-agent").autonomousModeEnabled(true)
                .confidenceThreshold(0.8).build();

        AgentConfiguration config2 = AgentConfiguration.builder().agentId("test-agent").autonomousModeEnabled(true)
                .confidenceThreshold(0.8).build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        AgentConfiguration config = AgentConfiguration.builder().agentId("test-agent").autonomousModeEnabled(true)
                .build();

        String toString = config.toString();
        assertTrue(toString.contains("test-agent"));
        assertTrue(toString.contains("Agent Configuration"));
        assertTrue(toString.contains("1.0.0"));
    }

    @Test
    void testBuilderReset() {
        AgentConfigurationBuilder builder = AgentConfiguration.builder().agentId("test-agent")
                .autonomousModeEnabled(false).confidenceThreshold(0.9);

        // Build first configuration
        AgentConfiguration config1 = builder.build();
        assertEquals("test-agent", config1.getAgentId());
        assertFalse(config1.isAutonomousModeEnabled());
        assertEquals(0.9, config1.getConfidenceThreshold());

        // Reset and build second configuration
        builder.reset();
        AgentConfiguration config2 = builder.agentId("new-agent").build();
        assertEquals("new-agent", config2.getAgentId());
        assertTrue(config2.isAutonomousModeEnabled()); // Default value
        assertEquals(0.7, config2.getConfidenceThreshold()); // Default value
    }
}
