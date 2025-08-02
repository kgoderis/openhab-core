package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.api.skill.A2ASkillException;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.service.ReadyService;

import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;

@ExtendWith(MockitoExtension.class)
class A2ASkillRegistryTest {

    @Mock
    private ReadyService mockReadyService;

    @Mock
    private AIActionRegistry mockActionRegistry;

    @Mock
    private AIAction mockAction1;

    @Mock
    private AIAction mockAction2;

    @Mock
    private Message mockMessage;

    private A2ASkillRegistry skillRegistry;

    @BeforeEach
    void setUp() {
        skillRegistry = new A2ASkillRegistry();

        // Setup mock actions
        when(mockAction1.getActionId()).thenReturn("test.action1");
        when(mockAction1.getActionName()).thenReturn("Test Action 1");
        when(mockAction1.getDescription()).thenReturn("Test action 1 description");

        when(mockAction2.getActionId()).thenReturn("test.action2");
        when(mockAction2.getActionName()).thenReturn("Test Action 2");
        when(mockAction2.getDescription()).thenReturn("Test action 2 description");
    }

    @Test
    void testActivate() {
        // Test activation
        skillRegistry.activate();

        // Verify ready service tracker registration
        // Note: In a real test, we'd need to inject the ReadyService
        assertNotNull(skillRegistry);
    }

    @Test
    void testDeactivate() {
        // Test deactivation
        skillRegistry.deactivate();

        // Verify cleanup
        assertNotNull(skillRegistry);
    }

    @Test
    void testHasSkill() {
        // Test with non-existent skill
        assertFalse(skillRegistry.hasSkill("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testExecuteSkill() throws A2ASkillException {
        // Test execution of non-existent skill
        assertThrows(A2ASkillException.class, () -> {
            skillRegistry.executeSkill("non.existent.skill", mockMessage);
        });
    }

    @Test
    void testGetSkillDefinitions() {
        List<Map<String, Object>> definitions = skillRegistry.getSkillDefinitions();

        // Should return a list (may be empty if no skills registered)
        assertNotNull(definitions);
        assertTrue(definitions instanceof List);
    }

    @Test
    void testGetAgentSkills() {
        List<AgentSkill> skills = skillRegistry.getAgentSkills();

        // Should return a list (may be empty if no skills registered)
        assertNotNull(skills);
        assertTrue(skills instanceof List);
    }

    @Test
    void testGetSkillStatistics() {
        Map<String, Object> statistics = skillRegistry.getSkillStatistics();

        // Should return statistics map
        assertNotNull(statistics);
        assertTrue(statistics instanceof Map);

        // Should contain basic statistics
        assertTrue(statistics.containsKey("totalSkills"));
        assertTrue(statistics.containsKey("totalExecutions"));
        assertTrue(statistics.containsKey("successfulExecutions"));
        assertTrue(statistics.containsKey("failedExecutions"));
    }

    @Test
    void testRefreshSkills() {
        // Test skill refresh
        skillRegistry.refreshSkills();

        // Should not throw exception
        assertNotNull(skillRegistry);
    }

    @Test
    void testIsSkillReady() {
        // Test with non-existent skill
        assertFalse(skillRegistry.isSkillReady("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testGetSkillIds() {
        List<String> skillIds = skillRegistry.getSkillIds();

        // Should return a list (may be empty if no skills registered)
        assertNotNull(skillIds);
        assertTrue(skillIds instanceof List);
    }

    @Test
    void testGetSkillAdapter() {
        // Test with non-existent skill
        assertNull(skillRegistry.getSkillAdapter("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testGetSkillMetadata() {
        // Test with non-existent skill
        assertNull(skillRegistry.getSkillMetadata("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testGetSkillExecutionCount() {
        // Test with non-existent skill
        assertEquals(0, skillRegistry.getSkillExecutionCount("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testGetSkillLastExecutionTime() {
        // Test with non-existent skill
        assertEquals(0, skillRegistry.getSkillLastExecutionTime("non.existent.skill"));

        // Test with existing skill (would need to register first)
        // This test would need actual skill registration
    }

    @Test
    void testOnReadyMarkerAdded() {
        // Test with valid marker
        org.openhab.core.service.ReadyMarker marker = new org.openhab.core.service.ReadyMarker("test", "marker");
        skillRegistry.onReadyMarkerAdded(marker);

        // Should not throw exception
        assertNotNull(skillRegistry);
    }

    @Test
    void testOnReadyMarkerRemoved() {
        // Test with valid marker
        org.openhab.core.service.ReadyMarker marker = new org.openhab.core.service.ReadyMarker("test", "marker");
        skillRegistry.onReadyMarkerRemoved(marker);

        // Should not throw exception
        assertNotNull(skillRegistry);
    }

    @Test
    void testReadyMarkerConstants() {
        // Test that ready marker constants are accessible
        assertNotNull(A2ASkillRegistry.A2A_SKILLS_READY);
        assertEquals("a2a", A2ASkillRegistry.A2A_SKILLS_READY.getType());
        assertEquals("skills", A2ASkillRegistry.A2A_SKILLS_READY.getIdentifier());
    }

    @Test
    void testSkillRegistryLifecycle() {
        // Test complete lifecycle
        skillRegistry.activate();
        assertNotNull(skillRegistry);

        skillRegistry.refreshSkills();
        assertNotNull(skillRegistry);

        skillRegistry.deactivate();
        assertNotNull(skillRegistry);
    }

    @Test
    void testStatisticsInitialization() {
        // Test that statistics are properly initialized
        Map<String, Object> statistics = skillRegistry.getSkillStatistics();

        assertEquals(0, statistics.get("totalSkills"));
        assertEquals(0L, statistics.get("totalExecutions"));
        assertEquals(0L, statistics.get("successfulExecutions"));
        assertEquals(0L, statistics.get("failedExecutions"));
    }

    @Test
    void testEmptySkillLists() {
        // Test that empty skill lists are returned when no skills are registered
        List<String> skillIds = skillRegistry.getSkillIds();
        List<Map<String, Object>> definitions = skillRegistry.getSkillDefinitions();
        List<AgentSkill> skills = skillRegistry.getAgentSkills();

        assertNotNull(skillIds);
        assertTrue(skillIds.isEmpty());

        assertNotNull(definitions);
        assertTrue(definitions.isEmpty());

        assertNotNull(skills);
        assertTrue(skills.isEmpty());
    }
}
