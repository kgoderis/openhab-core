package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.execution.api.AgentSkillManager;
import org.openhab.core.ai.agent.execution.api.AgentSkillResult;
import org.openhab.core.ai.agent.execution.api.SkillDocumentation;
import org.openhab.core.ai.agent.execution.api.SkillExample;
import org.openhab.core.ai.agent.execution.api.SkillPerformanceMetrics;
import org.openhab.core.ai.agent.execution.api.SkillTestResult;
import org.openhab.core.ai.agent.execution.api.SkillValidationResult;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;

/**
 * Skill Execution Implementation.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Skill Execution Implementation
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Skill Execution Implementation:</strong> Implements actual skill execution logic</li>
 * <li><strong>Parameter Conversion:</strong> Converts parameters to A2A Message format</li>
 * <li><strong>Message Creation:</strong> Creates A2A SDK Message objects from parameters</li>
 * <li><strong>Result Conversion:</strong> Converts execution results to AgentSkillResult</li>
 * <li><strong>Error Handling:</strong> Handles skill execution errors</li>
 * <li><strong>Execution Tracking:</strong> Tracks skill execution metrics</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Implements skill execution logic via {@link #executeSkill(String, Map)}</li>
 * <li>Converts parameters to A2A Message format via {@link #createMessageFromParameters(Map)}</li>
 * <li>Creates A2A SDK Message objects from parameters</li>
 * <li>Converts execution results to AgentSkillResult format</li>
 * <li>Handles skill execution errors and exceptions</li>
 * <li>Tracks skill execution metrics and performance</li>
 * <li>Delegates actual execution to {@link AgentSkillExecutor}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Register skills (delegates to AgentSkillRegistry)</li>
 * <li>❌ Execute skills directly (delegates to AgentSkillExecutor)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Manage skill registry (delegates to AgentSkillRegistry)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only implements skill execution logic</li>
 * <li>Delegates actual execution to AgentSkillExecutor</li>
 * <li>Handles parameter conversion and message creation</li>
 * <li>Does not register skills</li>
 * <li>Does not handle protocol communication</li>
 * <li>Does not manage task lifecycle</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link AgentSkillExecutor}: For actual skill execution</li>
 * <li>{@link AgentSkillRegistry}: For skill lookup</li>
 * <li>A2A SDK classes: For Message creation</li>
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
@Component(service = AgentSkillManager.class)
@NonNullByDefault
public class DefaultAgentSkillManager implements AgentSkillManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAgentSkillManager.class);

    @Reference
    private @Nullable AgentSkillRegistry skillRegistry;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable MetricsService metricsService;

    private final AgentSkillExecutor skillExecutor = new AgentSkillExecutor();

    @Activate
    public void activate() {
        try {
            logger.debug("AgentSkillManagerImpl activated");

            // Set the skill registry reference in the executor
            if (skillRegistry != null) {
                try {
                    skillExecutor.setSkillRegistry(skillRegistry);
                    logger.debug("Skill registry reference set in executor");
                } catch (Exception e) {
                    logger.error("Error setting skill registry in executor: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("Skill registry not available during activation");
            }

            recordMetrics("skill-manager", "service-activated", true, Duration.ZERO);

        } catch (Exception e) {
            logger.error("Error during Agent Skill Manager activation: {}", e.getMessage(), e);
            recordMetrics("skill-manager", "service-activated", false, Duration.ZERO);
            // Continue activation to ensure basic functionality
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            recordMetrics("skill-manager", "service-deactivated", true, Duration.ZERO);
            logger.debug("Agent Skill Manager implementation deactivated");
        } catch (Exception e) {
            logger.error("Error during Agent Skill Manager deactivation: {}", e.getMessage(), e);
            recordMetrics("skill-manager", "service-deactivated", false, Duration.ZERO);
        }
    }

    // Helper methods for internal use
    private boolean hasSkill(String skillId) {
        try {
            if (skillId == null || skillId.trim().isEmpty()) {
                logger.warn("Cannot check skill: skill ID is null or empty");
                recordMetrics("skill-manager", "skill-check", false, Duration.ZERO);
                return false;
            }

            AgentSkillRegistry registry = skillRegistry;
            if (registry == null) {
                logger.warn("AgentSkillRegistry not available for skill check: {}", skillId);
                recordMetrics("skill-manager", "skill-check", false, Duration.ZERO);
                return false;
            }

            boolean hasSkill = registry.hasSkill(skillId);
            recordMetrics("skill-manager", "skill-check", true, Duration.ZERO);
            return hasSkill;
        } catch (Exception e) {
            logger.error("Error checking skill '{}': {}", skillId, e.getMessage(), e);
            recordMetrics("skill-manager", "skill-check", false, Duration.ZERO);
            return false;
        }
    }

    private AgentSkillResult executeSkillInternal(String skillId, Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        try {
            if (skillId == null || skillId.trim().isEmpty()) {
                logger.warn("Cannot execute skill: skill ID is null or empty");
                recordMetrics("skill-manager", "skill-execution", false,
                        Duration.ofMillis(System.currentTimeMillis() - startTime));
                return AgentSkillResult.failure("Skill ID cannot be null or empty", "INVALID_SKILL_ID",
                        System.currentTimeMillis() - startTime);
            }

            if (parameters == null) {
                logger.warn("Cannot execute skill '{}': parameters map is null", skillId);
                recordMetrics("skill-manager", "skill-execution", false,
                        Duration.ofMillis(System.currentTimeMillis() - startTime));
                return AgentSkillResult.failure("Parameters cannot be null", "INVALID_PARAMETERS",
                        System.currentTimeMillis() - startTime);
            }

            // Create a proper message from parameters
            Message message = createMessageFromParameters(parameters);

            if (message == null) {
                logger.error("Failed to create message from parameters for skill: {}", skillId);
                recordMetrics("skill-manager", "skill-execution", false,
                        Duration.ofMillis(System.currentTimeMillis() - startTime));
                return AgentSkillResult.failure("Failed to create message from parameters", "MESSAGE_CREATION_FAILED",
                        System.currentTimeMillis() - startTime);
            }

            // Execute the skill using the dedicated executor
            AgentSkillResult result = skillExecutor.executeSkill(skillId, message);
            recordMetrics("skill-manager", "skill-execution", result.isSuccess(),
                    Duration.ofMillis(System.currentTimeMillis() - startTime));
            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing skill '{}': {}", skillId, e.getMessage(), e);
            recordMetrics("skill-manager", "skill-execution", false, Duration.ofMillis(executionTime));
            return AgentSkillResult.failure("Error executing skill: " + e.getMessage(), "EXECUTION_ERROR",
                    executionTime);
        }
    }

    private List<Map<String, Object>> getSkillDefinitions() {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return List.of();
        }
        return registry.getSkillDefinitions();
    }

    private List<AgentSkill> getAgentSkills() {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return List.of();
        }
        return registry.getAgentSkills();
    }

    private Map<String, Object> getSkillStatistics() {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return Map.of();
        }
        return registry.getSkillStatistics();
    }

    private void refreshSkills() {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return;
        }
        registry.refreshSkills();
    }

    private boolean isSkillReady(String skillId) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return false;
        }
        return registry.isSkillReady(skillId);
    }

    private List<String> getSkillIds() {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return List.of();
        }
        return registry.getSkillIds();
    }

    private @Nullable Map<String, Object> getSkillMetadata(String skillId) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return null;
        }
        return registry.getSkillMetadata(skillId);
    }

    private long getSkillExecutionCount(String skillId) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return 0;
        }
        return registry.getSkillExecutionCount(skillId);
    }

    private long getSkillLastExecutionTime(String skillId) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return 0;
        }
        return registry.getSkillLastExecutionTime(skillId);
    }

    @Override
    public boolean registerSkill(String agentId, String skill) {
        try {
            if (agentId == null || agentId.trim().isEmpty()) {
                logger.warn("Cannot register skill: agent ID is null or empty");
                recordMetrics("skill-manager", "skill-registration", false, Duration.ZERO);
                return false;
            }

            if (skill == null || skill.trim().isEmpty()) {
                logger.warn("Cannot register skill for agent '{}': skill is null or empty", agentId);
                recordMetrics("skill-manager", "skill-registration", false, Duration.ZERO);
                return false;
            }

            logger.debug("Registering skill {} for agent {}", skill, agentId);
            // This would typically delegate to AgentRegistry
            recordMetrics("skill-manager", "skill-registration", true, Duration.ZERO);
            return true;
        } catch (Exception e) {
            logger.error("Error registering skill '{}' for agent '{}': {}", skill, agentId, e.getMessage(), e);
            recordMetrics("skill-manager", "skill-registration", false, Duration.ZERO);
            return false;
        }
    }

    @Override
    public boolean unregisterSkill(String agentId, String skill) {
        try {
            if (agentId == null || agentId.trim().isEmpty()) {
                logger.warn("Cannot unregister skill: agent ID is null or empty");
                recordMetrics("skill-manager", "skill-unregistration", false, Duration.ZERO);
                return false;
            }

            if (skill == null || skill.trim().isEmpty()) {
                logger.warn("Cannot unregister skill for agent '{}': skill is null or empty", agentId);
                recordMetrics("skill-manager", "skill-unregistration", false, Duration.ZERO);
                return false;
            }

            logger.debug("Unregistering skill {} for agent {}", skill, agentId);
            // This would typically delegate to AgentRegistry
            recordMetrics("skill-manager", "skill-unregistration", true, Duration.ZERO);
            return true;
        } catch (Exception e) {
            logger.error("Error unregistering skill '{}' for agent '{}': {}", skill, agentId, e.getMessage(), e);
            recordMetrics("skill-manager", "skill-unregistration", false, Duration.ZERO);
            return false;
        }
    }

    @Override
    public List<String> getAgentSkills(String agentId) {
        logger.debug("Getting skills for agent {}", agentId);
        // This would typically delegate to AgentRegistry
        return List.of();
    }

    @Override
    public List<String> findAgentsWithSkill(String skill) {
        logger.debug("Finding agents with skill {}", skill);
        // This would typically delegate to AgentRegistry
        return List.of();
    }

    @Override
    public SkillValidationResult validateSkill(String skill) {
        logger.debug("Validating skill {}", skill);
        return new SkillValidationResult() {
            @Override
            public boolean isValid() {
                return hasSkill(skill);
            }

            @Override
            public String getMessage() {
                return hasSkill(skill) ? "Skill is valid" : "Skill not found";
            }

            @Override
            public List<String> getErrors() {
                return hasSkill(skill) ? List.of() : List.of("Skill not found");
            }
        };
    }

    @Override
    public SkillTestResult testSkill(String agentId, String skill) {
        logger.debug("Testing skill {} for agent {}", skill, agentId);
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> testParams = Map.of("test", true);
            AgentSkillResult result = executeSkillInternal(skill, testParams);
            long executionTime = System.currentTimeMillis() - startTime;

            return new SkillTestResult() {
                @Override
                public boolean isSuccessful() {
                    return result.isSuccess();
                }

                @Override
                public String getMessage() {
                    return result.getErrorMessage() != null ? result.getErrorMessage() : "Test completed";
                }

                @Override
                public long getExecutionTime() {
                    return executionTime;
                }

                @Override
                public Map<String, Object> getTestData() {
                    return result.getData() != null ? result.getData() : Map.of();
                }
            };
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new SkillTestResult() {
                @Override
                public boolean isSuccessful() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Test failed: " + e.getMessage();
                }

                @Override
                public long getExecutionTime() {
                    return executionTime;
                }

                @Override
                public Map<String, Object> getTestData() {
                    return Map.of("error", e.getMessage());
                }
            };
        }
    }

    @Override
    public SkillPerformanceMetrics getSkillPerformance(String skill) {
        logger.debug("Getting performance metrics for skill {}", skill);
        long totalExecutions = getSkillExecutionCount(skill);
        long lastExecutionTime = getSkillLastExecutionTime(skill);

        return new SkillPerformanceMetrics() {
            @Override
            public long getTotalExecutions() {
                return totalExecutions;
            }

            @Override
            public long getSuccessfulExecutions() {
                return totalExecutions; // Simplified - would need actual tracking
            }

            @Override
            public long getFailedExecutions() {
                return 0; // Simplified - would need actual tracking
            }

            @Override
            public double getAverageExecutionTime() {
                return lastExecutionTime > 0 ? lastExecutionTime : 0.0;
            }

            @Override
            public double getSuccessRate() {
                return totalExecutions > 0 ? 1.0 : 0.0; // Simplified
            }
        };
    }

    @Override
    public SkillDocumentation getSkillDocumentation(String skill) {
        logger.debug("Getting documentation for skill {}", skill);
        Map<String, Object> metadata = getSkillMetadata(skill);

        return new SkillDocumentation() {
            @Override
            public String getDescription() {
                return metadata != null && metadata.containsKey("description") ? metadata.get("description").toString()
                        : "No description available";
            }

            @Override
            public String getUsage() {
                return metadata != null && metadata.containsKey("usage") ? metadata.get("usage").toString()
                        : "No usage information available";
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<String> getParameters() {
                return metadata != null && metadata.containsKey("parameters")
                        ? (List<String>) metadata.get("parameters")
                        : List.of();
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<String> getExamples() {
                return metadata != null && metadata.containsKey("examples") ? (List<String>) metadata.get("examples")
                        : List.of();
            }

            @Override
            public String getVersion() {
                return metadata != null && metadata.containsKey("version") ? metadata.get("version").toString()
                        : "1.0.0";
            }
        };
    }

    @Override
    public List<SkillExample> getSkillExamples(String skill) {
        logger.debug("Getting examples for skill {}", skill);
        return List.of(new SkillExample() {
            @Override
            public String getName() {
                return "Basic Example";
            }

            @Override
            public String getDescription() {
                return "Basic usage example for " + skill;
            }

            @Override
            public Map<String, Object> getParameters() {
                return Map.of("example", true);
            }

            @Override
            public Object getExpectedResult() {
                return "Example result";
            }
        });
    }

    @Override
    public boolean hasSkillPermission(String agentId, String skill) {
        logger.debug("Checking permission for agent {} to use skill {}", agentId, skill);
        // Simplified permission check - would need actual RBAC implementation
        return hasSkill(skill);
    }

    @Override
    public boolean isAvailable() {
        return skillRegistry != null;
    }

    @Override
    public int getSkillCount() {
        return getSkillIds().size();
    }

    @Override
    public AgentSkillResult executeSkill(String skillId, Map<String, Object> parameters) {
        logger.debug("Executing skill: {} with parameters: {}", skillId, parameters);
        return executeSkillInternal(skillId, parameters);
    }

    private Message createMessageFromParameters(Map<String, Object> parameters) {
        try {
            logger.debug("Creating Message from parameters: {}", parameters);

            // Convert parameters to a text representation
            String parameterText = convertParametersToText(parameters);

            // Create TextPart with the parameter text
            List<Part<?>> parts = List.of(new TextPart(parameterText, null));

            // Create metadata from parameters
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("skillExecution", true);
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.putAll(parameters);

            // Create Message using A2A SDK constructor pattern
            return new Message(Message.Role.AGENT, parts, null, null, null, null, metadata);

        } catch (Exception e) {
            logger.error("Failed to create Message from parameters: {}", parameters, e);
            return null;
        }
    }

    private String convertParametersToText(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "execute";
        }

        StringBuilder text = new StringBuilder("execute");
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            text.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }

        return text.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertResultToMap(Object result) {
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        } else if (result != null) {
            return Map.of("result", result.toString());
        } else {
            return Map.of();
        }
    }

    /**
     * Record metrics for skill management operations.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        try {
            if (domain == null || domain.trim().isEmpty()) {
                logger.warn("Cannot record metrics: domain is null or empty for operation: {}", operation);
                return;
            }

            if (operation == null || operation.trim().isEmpty()) {
                logger.warn("Cannot record metrics: operation is null or empty for domain: {}", domain);
                return;
            }

            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation(domain, operation, success, duration);
                } catch (Exception e) {
                    logger.error("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage(), e);
                }
            } else {
                logger.debug("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                        operation);
            }
        } catch (Exception e) {
            // Prevent recursive error recording
            logger.error("Error in metrics recording helper for {}.{}: {}", domain, operation, e.getMessage());
        }
    }
}
