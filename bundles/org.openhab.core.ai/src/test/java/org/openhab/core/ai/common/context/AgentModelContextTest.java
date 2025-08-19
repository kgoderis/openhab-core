package org.openhab.core.ai.common.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentModelContext.
 * 
 * @author Karel Goderis - Initial Contribution
 */
class AgentModelContextTest {

    private AgentModelContext.AgentModelContextBuilder builder;

    @BeforeEach
    void setUp() {
        builder = AgentModelContext.builder();
    }

    @Test
    void testBasicBuilder() {
        AgentModelContext context = builder.withAgentId("test-agent").withAgentType("energy")
                .withDomain("home_automation").build();

        assertEquals("test-agent", context.getAgentId());
        assertEquals("energy", context.getAgentType());
        assertEquals("home_automation", context.getDomain());
        assertEquals("agent_model", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertFalse(context.isEmpty());
        assertEquals(3, context.size());
    }

    @Test
    void testCapabilitiesAndSkills() {
        Map<String, String> capabilities = new HashMap<>();
        capabilities.put("energy_management", "Monitor and control energy usage");
        capabilities.put("automation", "Automate home systems");

        Map<String, String> skills = new HashMap<>();
        skills.put("optimization", "Optimize energy consumption");
        skills.put("prediction", "Predict energy needs");

        AgentModelContext context = builder.withAgentId("test-agent").withCapabilities(capabilities).withSkills(skills)
                .build();

        Map<String, String> resultCapabilities = context.getCapabilities();
        assertEquals(2, resultCapabilities.size());
        assertEquals("Monitor and control energy usage", resultCapabilities.get("energy_management"));
        assertEquals("Automate home systems", resultCapabilities.get("automation"));

        Map<String, String> resultSkills = context.getSkills();
        assertEquals(2, resultSkills.size());
        assertEquals("Optimize energy consumption", resultSkills.get("optimization"));
        assertEquals("Predict energy needs", resultSkills.get("prediction"));
    }

    @Test
    void testCurrentStateAndHistory() {
        Map<String, Object> state = new HashMap<>();
        state.put("temperature", 22.5);
        state.put("humidity", 45.0);
        state.put("active", true);

        Map<String, Object> history = new HashMap<>();
        history.put("last_24h", "energy_usage_data");

        AgentModelContext context = builder.withAgentId("test-agent").withCurrentState(state).withHistory(history)
                .build();

        Map<String, Object> resultState = context.getCurrentState();
        assertEquals(3, resultState.size());
        assertEquals(22.5, resultState.get("temperature"));
        assertEquals(45.0, resultState.get("humidity"));
        assertEquals(true, resultState.get("active"));

        assertEquals(history, context.getHistory());
    }

    @Test
    void testUserPreferencesAndEnvironment() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("comfort_temp", 21.0);
        preferences.put("energy_saving", true);

        Map<String, Object> environment = new HashMap<>();
        environment.put("weather", "sunny");
        environment.put("time_of_day", "afternoon");

        AgentModelContext context = builder.withAgentId("test-agent").withUserPreferences(preferences)
                .withEnvironment(environment).build();

        Map<String, Object> resultPreferences = context.getUserPreferences();
        assertEquals(2, resultPreferences.size());
        assertEquals(21.0, resultPreferences.get("comfort_temp"));
        assertEquals(true, resultPreferences.get("energy_saving"));

        Map<String, Object> resultEnvironment = context.getEnvironment();
        assertEquals(2, resultEnvironment.size());
        assertEquals("sunny", resultEnvironment.get("weather"));
        assertEquals("afternoon", resultEnvironment.get("time_of_day"));
    }

    @Test
    void testReasoningAndSpecialization() {
        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("confidence", 0.95);
        reasoning.put("reasoning_type", "optimization");

        AgentModelContext context = builder.withAgentId("test-agent").withReasoning(reasoning)
                .withSpecialization("energy_optimization").build();

        Map<String, Object> resultReasoning = context.getReasoning();
        assertEquals(2, resultReasoning.size());
        assertEquals(0.95, resultReasoning.get("confidence"));
        assertEquals("optimization", resultReasoning.get("reasoning_type"));

        assertEquals("energy_optimization", context.getSpecialization());
    }

    @Test
    void testMetadata() {
        AgentModelContext context = builder.withAgentId("test-agent").withPriority("HIGH")
                .withExpirationTime(System.currentTimeMillis() + 3600000) // 1 hour from now
                .withSource("user_request").build();

        assertEquals("HIGH", context.getPriority());
        assertNotNull(context.getExpirationTime());
        assertEquals("user_request", context.getSource());
        assertFalse(context.isExpired());
    }

    @Test
    void testExpiration() {
        AgentModelContext context = builder.withAgentId("test-agent")
                .withExpirationTime(System.currentTimeMillis() - 1000) // 1 second ago
                .build();

        assertTrue(context.isExpired());
    }

    @Test
    void testToBuilder() {
        AgentModelContext original = builder.withAgentId("test-agent").withAgentType("energy")
                .withDomain("home_automation").build();

        AgentModelContext copy = original.toBuilder().withAgentType("security").build();

        assertEquals("test-agent", copy.getAgentId());
        assertEquals("security", copy.getAgentType()); // Changed
        assertEquals("home_automation", copy.getDomain());
        assertNotEquals(original.getContextId(), copy.getContextId()); // New ID
    }

    @Test
    void testCustomValues() {
        AgentModelContext context = builder.withAgentId("test-agent").withValue("custom_field", "custom_value")
                .withMetadata("custom_metadata", "metadata_value").build();

        assertEquals("custom_value", context.getValue("custom_field"));
        assertEquals("metadata_value", context.getMetadata("custom_metadata"));
        assertTrue(context.hasValue("custom_field"));
        assertTrue(context.hasMetadata("custom_metadata"));
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withContextId("").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            builder.withAgentId(null).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            builder.withAgentType(null).build();
        });
    }

    @Test
    void testToString() {
        AgentModelContext context = builder.withAgentId("test-agent").withAgentType("energy")
                .withDomain("home_automation").build();

        String result = context.toString();
        assertTrue(result.contains("AgentModelContext"));
        assertTrue(result.contains("test-agent"));
        assertTrue(result.contains("energy"));
        assertTrue(result.contains("home_automation"));
    }
}
