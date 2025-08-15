package org.openhab.core.ai.agent.execution.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Skill Execution Orchestration Interface.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Skill Execution Orchestration Interface
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Skill Execution Interface:</strong> Provides unified interface for skill execution</li>
 * <li><strong>Skill Management:</strong> Manages skill execution lifecycle</li>
 * <li><strong>Parameter Handling:</strong> Handles skill parameter conversion</li>
 * <li><strong>Result Processing:</strong> Processes skill execution results</li>
 * <li><strong>Error Handling:</strong> Handles skill execution errors</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this interface DOES:</strong>
 * <ul>
 * <li>Provides unified interface for skill execution via {@link #executeSkill(String, Map)}</li>
 * <li>Manages skill execution lifecycle</li>
 * <li>Handles skill parameter conversion and processing</li>
 * <li>Processes skill execution results</li>
 * <li>Handles skill execution errors</li>
 * <li>Provides access to skill manager and registry references</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this interface DOES NOT do:</strong>
 * <ul>
 * <li>❌ Execute skills directly (delegates to AgentSkillManagerImpl)</li>
 * <li>❌ Register skills (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Contain business logic (delegates to execution layers)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only provides interface for skill execution</li>
 * <li>Delegates implementation to AgentSkillManagerImpl</li>
 * <li>Does not contain business logic</li>
 * <li>Does not handle protocol communication</li>
 * <li>Does not manage skill registration</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>DefaultAgentSkillManager: For actual implementation</li>
 * <li>AgentSkillRegistry: For skill lookup</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> Skill Management Layer
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
     * Execute a skill with the given parameters.
     * 
     * @param skillId the skill ID to execute
     * @param parameters the parameters for the skill execution
     * @return the skill execution result
     */
    AgentSkillResult executeSkill(String skillId, Map<String, Object> parameters);

    // Extracted: SkillValidationResult

    // Extracted: SkillTestResult

    // Extracted: SkillPerformanceMetrics

    // Extracted: SkillDocumentation

    // Extracted: SkillExample
}
