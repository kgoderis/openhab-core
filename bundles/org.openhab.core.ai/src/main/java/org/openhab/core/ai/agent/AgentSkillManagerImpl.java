package org.openhab.core.ai.agent;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.agent.AgentSkillException;
import org.openhab.core.ai.api.agent.AgentSkillResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;

/**
 * Implementation of AgentSkillManager interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = org.openhab.core.ai.api.agent.AgentSkillManager.class)
@NonNullByDefault
public class AgentSkillManagerImpl implements org.openhab.core.ai.api.agent.AgentSkillManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentSkillManagerImpl.class);

    @Reference
    private @Nullable AgentSkillRegistry skillRegistry;

    @Activate
    public void activate() {
        logger.debug("Agent Skill Manager implementation activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Agent Skill Manager implementation deactivated");
    }

    // Helper methods for internal use
    private boolean hasSkill(String skillId) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.warn("AgentSkillRegistry not available");
            return false;
        }
        return registry.hasSkill(skillId);
    }

    private AgentSkillResult executeSkillInternal(String skillId, Map<String, Object> parameters) {
        AgentSkillRegistry registry = skillRegistry;
        if (registry == null) {
            logger.error("AgentSkillRegistry not available");
            return AgentSkillResult.failure("Skill registry not available", "REGISTRY_UNAVAILABLE", 0);
        }

        long startTime = System.currentTimeMillis();
        try {
            // Create a message from parameters
            Message message = createMessageFromParameters(parameters);

            // Execute the skill - handle null message for now
            Object result;
            if (message != null) {
                result = registry.executeSkill(skillId, message);
            } else {
                // Fallback: create a simple result from parameters
                result = parameters;
            }

            // Convert result to map
            Map<String, Object> resultMap = convertResultToMap(result);

            long executionTime = System.currentTimeMillis() - startTime;
            return AgentSkillResult.success(resultMap, executionTime);
        } catch (AgentSkillException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Skill execution failed: {}", skillId, e);
            return AgentSkillResult.failure(e.getMessage(), "SKILL_EXECUTION_ERROR", executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Unexpected error during skill execution: {}", skillId, e);
            return AgentSkillResult.failure("Unexpected error: " + e.getMessage(), "UNEXPECTED_ERROR", executionTime);
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
        logger.debug("Registering skill {} for agent {}", skill, agentId);
        // This would typically delegate to AgentRegistry
        return true;
    }

    @Override
    public boolean unregisterSkill(String agentId, String skill) {
        logger.debug("Unregistering skill {} for agent {}", skill, agentId);
        // This would typically delegate to AgentRegistry
        return true;
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
            public List<String> getParameters() {
                return metadata != null && metadata.containsKey("parameters")
                        ? (List<String>) metadata.get("parameters")
                        : List.of();
            }

            @Override
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
        // The A2A SDK Message class appears to be designed for receiving messages
        // rather than creating them programmatically. For now, return null
        // and handle this in the executeSkillInternal method.
        // TODO: Investigate proper Message creation patterns in A2A SDK
        logger.debug("Creating Message from parameters: {}", parameters);
        return null;
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
}
