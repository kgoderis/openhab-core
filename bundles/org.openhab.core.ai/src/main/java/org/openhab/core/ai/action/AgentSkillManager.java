package org.openhab.core.ai.action;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent Skill Manager - Defines the contract for managing agent skills
 * 
 * <p>
 * This service provides:
 * - Skill discovery and registration
 * - Skill validation and testing
 * - Skill mapping and routing
 * - Skill performance monitoring
 * - Skill security and access controls
 * - Skill documentation and examples
 * - Skill testing and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentSkillManager {

    /**
     * Register a skill for an agent
     * 
     * @param agentId the agent ID
     * @param skill the skill to register
     * @return true if registration was successful
     */
    boolean registerSkill(String agentId, String skill);

    /**
     * Unregister a skill from an agent
     * 
     * @param agentId the agent ID
     * @param skill the skill to unregister
     * @return true if unregistration was successful
     */
    boolean unregisterSkill(String agentId, String skill);

    /**
     * Get all skills for an agent
     * 
     * @param agentId the agent ID
     * @return list of skills
     */
    List<String> getAgentSkills(String agentId);

    /**
     * Find agents with a specific skill
     * 
     * @param skill the skill to search for
     * @return list of agent IDs with the skill
     */
    List<String> findAgentsWithSkill(String skill);

    /**
     * Validate a skill
     * 
     * @param skill the skill to validate
     * @return validation result
     */
    SkillValidationResult validateSkill(String skill);

    /**
     * Test a skill
     * 
     * @param agentId the agent ID
     * @param skill the skill to test
     * @return test result
     */
    SkillTestResult testSkill(String agentId, String skill);

    /**
     * Get skill performance metrics
     * 
     * @param skill the skill
     * @return performance metrics
     */
    SkillPerformanceMetrics getSkillPerformance(String skill);

    /**
     * Get skill documentation
     * 
     * @param skill the skill
     * @return documentation
     */
    SkillDocumentation getSkillDocumentation(String skill);

    /**
     * Get skill examples
     * 
     * @param skill the skill
     * @return list of examples
     */
    List<SkillExample> getSkillExamples(String skill);

    /**
     * Check if agent has permission for skill
     * 
     * @param agentId the agent ID
     * @param skill the skill
     * @return true if agent has permission
     */
    boolean hasSkillPermission(String agentId, String skill);

    /**
     * Get service status
     * 
     * @return true if the service is available and ready
     */
    boolean isAvailable();

    /**
     * Get the number of registered skills
     * 
     * @return the number of skills
     */
    int getSkillCount();

    /**
     * Skill validation result
     */
    interface SkillValidationResult {
        boolean isValid();

        String getMessage();

        List<String> getErrors();
    }

    /**
     * Skill test result
     */
    interface SkillTestResult {
        boolean isSuccessful();

        String getMessage();

        long getExecutionTime();

        Map<String, Object> getTestData();
    }

    /**
     * Skill performance metrics
     */
    interface SkillPerformanceMetrics {
        long getTotalExecutions();

        long getSuccessfulExecutions();

        long getFailedExecutions();

        double getAverageExecutionTime();

        double getSuccessRate();
    }

    /**
     * Skill documentation
     */
    interface SkillDocumentation {
        String getDescription();

        String getUsage();

        List<String> getParameters();

        List<String> getExamples();

        String getVersion();
    }

    /**
     * Skill example
     */
    interface SkillExample {
        String getName();

        String getDescription();

        Map<String, Object> getParameters();

        Object getExpectedResult();
    }
}
