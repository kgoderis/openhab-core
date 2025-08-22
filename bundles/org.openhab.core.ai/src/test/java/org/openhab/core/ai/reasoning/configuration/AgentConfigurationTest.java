package org.openhab.core.ai.reasoning.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.reasoning.config.AgentConfiguration;

/**
 * Unit tests for {@link AgentConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentConfigurationTest {

    @Test
    void testBuilderCreation() {
        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent").build();

        assertEquals("test-agent", config.getAgentId());
        assertTrue(config.isEnabled());
        assertEquals("Agent Configuration", config.getName());
        assertEquals("1.0.0", config.getVersion());
    }

    @Test
    void testDefaultValues() {
        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent").build();

        assertTrue(config.isAutonomousModeEnabled());
        assertTrue(config.isBehaviorLearningEnabled());
        assertTrue(config.isSafetyConstraintsEnabled());
        assertEquals(0.7, config.getConfidenceThreshold());
        assertEquals(Duration.ofMinutes(5), config.getTimeout());
        assertEquals(10, config.getMaxConcurrentActions());
        assertTrue(config.getBehaviorPolicies().isEmpty());
        assertTrue(config.getConstraints().isEmpty());
        assertTrue(config.getSafetyPolicies().isEmpty());
    }

    @Test
    void testCustomValues() {
        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent")
                .withAutonomousModeEnabled(false).withBehaviorLearningEnabled(false).withSafetyConstraintsEnabled(false)
                .withConfidenceThreshold(0.9).withTimeout(Duration.ofMinutes(10)).withMaxConcurrentActions(5).build();

        assertFalse(config.isAutonomousModeEnabled());
        assertFalse(config.isBehaviorLearningEnabled());
        assertFalse(config.isSafetyConstraintsEnabled());
        assertEquals(0.9, config.getConfidenceThreshold());
        assertEquals(Duration.ofMinutes(10), config.getTimeout());
        assertEquals(5, config.getMaxConcurrentActions());
    }

    @Test
    void testBehaviorPolicies() {
        List<String> behaviorPolicies = List.of("cooperative", "aggressive", "conservative");

        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent")
                .withBehaviorPolicies(behaviorPolicies).build();

        assertEquals(behaviorPolicies, config.getBehaviorPolicies());
    }

    @Test
    void testConstraints() {
        List<String> constraints = List.of("max-power-usage", "min-temperature", "max-brightness");

        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent").withConstraints(constraints)
                .build();

        assertEquals(constraints, config.getConstraints());
    }

    @Test
    void testSafetyPolicies() {
        List<String> safetyPolicies = List.of("emergency-shutdown", "overload-protection", "user-safety");

        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent")
                .withSafetyPolicies(safetyPolicies).build();

        assertEquals(safetyPolicies, config.getSafetyPolicies());
    }

    @Test
    void testCustomSettings() {
        Map<String, Object> customSettings = Map.of("learningRate", 0.01, "maxIterations", 1000, "enableLogging", true);

        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent")
                .withCustomSettings(customSettings).build();

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
        // Test invalid configuration (missing agentId)
        assertThrows(IllegalArgumentException.class, () -> {
            AgentConfiguration.builder().build();
        });

        // Test invalid confidence threshold
        assertThrows(IllegalArgumentException.class, () -> {
            AgentConfiguration.builder().withAgentId("test-agent").withConfidenceThreshold(1.5).build();
        });

        // Test invalid max concurrent actions
        assertThrows(IllegalArgumentException.class, () -> {
            AgentConfiguration.builder().withAgentId("test-agent").withMaxConcurrentActions(0).build();
        });

        // Test invalid timeout
        assertThrows(IllegalArgumentException.class, () -> {
            AgentConfiguration.builder().withAgentId("test-agent").withTimeout(Duration.ofMinutes(-1)).build();
        });
    }

    @Test
    void testEquality() {
        AgentConfiguration config1 = AgentConfiguration.builder().withAgentId("test-agent")
                .withAutonomousModeEnabled(true).withConfidenceThreshold(0.8).build();

        AgentConfiguration config2 = AgentConfiguration.builder().withAgentId("test-agent")
                .withAutonomousModeEnabled(true).withConfidenceThreshold(0.8).build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        AgentConfiguration config = AgentConfiguration.builder().withAgentId("test-agent")
                .withAutonomousModeEnabled(true).build();

        String toString = config.toString();
        assertTrue(toString.contains("test-agent"));
        assertTrue(toString.contains("Agent Configuration"));
        assertTrue(toString.contains("1.0.0"));
    }

    @Test
    void testToBuilder() {
        AgentConfiguration config1 = AgentConfiguration.builder().withAgentId("test-agent")
                .withAutonomousModeEnabled(false).withConfidenceThreshold(0.9).build();

        // Use toBuilder to create a modified version
        AgentConfiguration config2 = config1.toBuilder().withAgentId("new-agent").build();

        assertEquals("new-agent", config2.getAgentId());
        assertFalse(config2.isAutonomousModeEnabled()); // Preserved from original
        assertEquals(0.9, config2.getConfidenceThreshold()); // Preserved from original
    }
}
