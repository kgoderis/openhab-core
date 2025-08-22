/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.config.repo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parser for agent configuration YAML files.
 * 
 * Handles parsing of agent configuration YAML files into structured data for the
 * AgentConfigurationRepository. Supports validation and error handling for malformed
 * or invalid configuration files.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentConfigYamlParser extends BaseYamlParser {

    /**
     * Parse agent configuration from YAML input stream
     *
     * @param inputStream the YAML input stream
     * @param agentName the name of the agent
     * @return the parsed agent configuration
     * @throws AgentConfigurationRepositoryException if parsing fails
     */
    public AgentConfigurationRepository.AgentConfiguration parseAgentConfiguration(InputStream inputStream,
            String agentName) throws AgentConfigurationRepositoryException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(agentName, "Agent name cannot be null");

        try {
            // Parse YAML content
            Map<String, Object> yamlData = yaml.load(inputStream);
            if (yamlData == null || yamlData.isEmpty()) {
                throw new AgentConfigurationRepositoryException("Empty or invalid YAML content");
            }

            // Validate the structure
            List<String> validationErrors = validateAgentConfiguration(yamlData);
            if (!validationErrors.isEmpty()) {
                throw new AgentConfigurationRepositoryException(
                        "Agent configuration validation failed: " + validationErrors);
            }

            // Create agent configuration
            return createAgentConfiguration(yamlData, agentName);

        } catch (Exception e) {
            logger.error("Failed to parse agent configuration YAML for agent '{}': {}", agentName, e.getMessage());
            throw new AgentConfigurationRepositoryException("Failed to parse agent configuration: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Parse raw YAML content without creating agent configuration object
     *
     * @param inputStream the YAML input stream
     * @return the parsed YAML data as Map
     * @throws IOException if reading fails
     */
    public Map<String, Object> parse(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");

        try {
            Map<String, Object> yamlData = yaml.load(inputStream);
            if (yamlData == null) {
                throw new IOException("Empty or invalid YAML content");
            }
            return yamlData;
        } catch (Exception e) {
            logger.error("Failed to parse YAML content: {}", e.getMessage());
            throw new IOException("Failed to parse YAML: " + e.getMessage(), e);
        }
    }

    /**
     * Validate agent configuration structure
     *
     * @param yamlData the parsed YAML data
     * @return list of validation errors (empty if valid)
     */
    private List<String> validateAgentConfiguration(Map<String, Object> yamlData) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (!yamlData.containsKey("agent")) {
            errors.add("Missing required 'agent' section");
            return errors; // Cannot proceed without agent section
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> agentData = (Map<String, Object>) yamlData.get("agent");

        // Validate agent metadata
        if (!agentData.containsKey("metadata")) {
            errors.add("Missing required 'metadata' section in agent configuration");
        } else {
            errors.addAll(validateAgentMetadata((Map<String, Object>) agentData.get("metadata")));
        }

        // Validate agent specification
        if (!agentData.containsKey("specification")) {
            errors.add("Missing required 'specification' section in agent configuration");
        } else {
            errors.addAll(validateAgentSpecification((Map<String, Object>) agentData.get("specification")));
        }

        return errors;
    }

    /**
     * Validate agent metadata section
     */
    @SuppressWarnings("unchecked")
    private List<String> validateAgentMetadata(Map<String, Object> metadata) {
        List<String> errors = new ArrayList<>();

        // Required fields
        YamlValidationUtils.validateRequiredField(metadata, "name", errors);
        YamlValidationUtils.validateStringField(metadata, "name", errors);

        YamlValidationUtils.validateRequiredField(metadata, "version", errors);
        YamlValidationUtils.validateStringField(metadata, "version", errors);

        YamlValidationUtils.validateRequiredField(metadata, "description", errors);
        YamlValidationUtils.validateStringField(metadata, "description", errors);

        // Validate capabilities if present
        if (metadata.containsKey("capabilities")) {
            YamlValidationUtils.validateListField(metadata, "capabilities", errors);
        }

        return errors;
    }

    /**
     * Validate agent specification section
     */
    @SuppressWarnings("unchecked")
    private List<String> validateAgentSpecification(Map<String, Object> specification) {
        List<String> errors = new ArrayList<>();

        // Validate reasoning configuration if present
        if (specification.containsKey("reasoning")) {
            Map<String, Object> reasoning = (Map<String, Object>) specification.get("reasoning");
            errors.addAll(validateReasoningConfiguration(reasoning));
        }

        // Validate skill configuration if present
        if (specification.containsKey("skills")) {
            Map<String, Object> skills = (Map<String, Object>) specification.get("skills");
            errors.addAll(validateSkillConfiguration(skills));
        }

        // Validate communication configuration if present
        if (specification.containsKey("communication")) {
            Map<String, Object> communication = (Map<String, Object>) specification.get("communication");
            errors.addAll(validateCommunicationConfiguration(communication));
        }

        return errors;
    }

    /**
     * Validate reasoning configuration
     */
    private List<String> validateReasoningConfiguration(Map<String, Object> reasoning) {
        List<String> errors = new ArrayList<>();

        if (reasoning.containsKey("maxSteps")) {
            Object maxSteps = reasoning.get("maxSteps");
            if (!(maxSteps instanceof Integer) || (Integer) maxSteps <= 0) {
                errors.add("Invalid maxSteps value in reasoning configuration");
            }
        }

        if (reasoning.containsKey("timeout")) {
            Object timeout = reasoning.get("timeout");
            if (!(timeout instanceof Integer) || (Integer) timeout <= 0) {
                errors.add("Invalid timeout value in reasoning configuration");
            }
        }

        return errors;
    }

    /**
     * Validate skill configuration
     */
    @SuppressWarnings("unchecked")
    private List<String> validateSkillConfiguration(Map<String, Object> skills) {
        List<String> errors = new ArrayList<>();

        if (skills.containsKey("enabled")) {
            YamlValidationUtils.validateListField(skills, "enabled", errors);
        }

        return errors;
    }

    /**
     * Validate communication configuration
     */
    private List<String> validateCommunicationConfiguration(Map<String, Object> communication) {
        List<String> errors = new ArrayList<>();

        if (communication.containsKey("maxConcurrentRequests")) {
            Object maxRequests = communication.get("maxConcurrentRequests");
            if (!(maxRequests instanceof Integer) || (Integer) maxRequests <= 0) {
                errors.add("Invalid maxConcurrentRequests value in communication configuration");
            }
        }

        if (communication.containsKey("requestTimeout")) {
            Object timeout = communication.get("requestTimeout");
            if (!(timeout instanceof Integer) || (Integer) timeout <= 0) {
                errors.add("Invalid requestTimeout value in communication configuration");
            }
        }

        return errors;
    }

    /**
     * Create agent configuration from validated YAML data
     */
    @SuppressWarnings("unchecked")
    private AgentConfigurationRepository.AgentConfiguration createAgentConfiguration(Map<String, Object> yamlData,
            String agentName) {
        Map<String, Object> agentData = (Map<String, Object>) yamlData.get("agent");
        Map<String, Object> metadata = (Map<String, Object>) agentData.get("metadata");
        Map<String, Object> specification = (Map<String, Object>) agentData.get("specification");

        return new AgentConfigurationRepository.AgentConfiguration() {
            @Override
            public String getName() {
                return agentName;
            }

            @Override
            public String getDescription() {
                return (String) metadata.get("description");
            }

            @Override
            public String getVersion() {
                return (String) metadata.get("version");
            }

            @Override
            public String getType() {
                return (String) agentData.getOrDefault("type", "generic");
            }

            @Override
            public List<AgentConfigurationRepository.AgentSkill> getSkills() {
                Object skills = specification.get("skills");
                if (skills instanceof Map) {
                    Map<String, Object> skillsMap = (Map<String, Object>) skills;
                    Object enabled = skillsMap.get("enabled");
                    if (enabled instanceof List) {
                        List<String> enabledSkills = (List<String>) enabled;
                        return enabledSkills.stream().map(skillName -> new SimpleAgentSkill(skillName))
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
                return List.of();
            }

            @Override
            public AgentConfigurationRepository.AgentBehavior getBehavior() {
                Object behavior = specification.get("behavior");
                if (behavior instanceof Map) {
                    return new SimpleAgentBehavior((Map<String, Object>) behavior);
                }
                return new SimpleAgentBehavior(Map.of());
            }

            @Override
            public Map<String, Object> getMetadata() {
                return Map.copyOf(metadata);
            }

            @Override
            public Map<String, Object> getParameters() {
                Object params = agentData.get("parameters");
                if (params instanceof Map) {
                    return Map.copyOf((Map<String, Object>) params);
                }
                return Map.of();
            }
        };
    }

    @Override
    protected List<String> validateStructure(Map<String, Object> yamlData) {
        return validateAgentConfiguration(yamlData);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T createDomainObject(Map<String, Object> yamlData, String name) throws Exception {
        @SuppressWarnings("unchecked")
        T result = (T) createAgentConfiguration(yamlData, name);
        return result;
    }

    @Override
    protected Exception createParseException(String message, Throwable cause) {
        return new AgentConfigurationRepositoryException(message, cause);
    }

    /**
     * Generate YAML content from agent configuration
     *
     * @param agentConfig the agent configuration
     * @return the YAML content as string
     */
    public String generateYaml(AgentConfigurationRepository.AgentConfiguration agentConfig) {
        Objects.requireNonNull(agentConfig, "Agent configuration cannot be null");

        StringBuilder yaml = new StringBuilder();
        yaml.append("# Agent Configuration\n");
        yaml.append("# Generated agent configuration YAML\n\n");

        yaml.append("agent:\n");
        yaml.append("  type: ").append(agentConfig.getType()).append("\n");
        yaml.append("  metadata:\n");
        yaml.append("    name: ").append(agentConfig.getName()).append("\n");
        yaml.append("    version: ").append(agentConfig.getVersion()).append("\n");
        if (agentConfig.getDescription() != null) {
            yaml.append("    description: ").append(agentConfig.getDescription()).append("\n");
        }

        yaml.append("\n  specification:\n");

        // Add skills
        if (!agentConfig.getSkills().isEmpty()) {
            yaml.append("    skills:\n");
            yaml.append("      enabled:\n");
            for (AgentConfigurationRepository.AgentSkill skill : agentConfig.getSkills()) {
                yaml.append("        - ").append(skill.getName()).append("\n");
            }
        }

        // Add behavior configuration
        AgentConfigurationRepository.AgentBehavior behavior = agentConfig.getBehavior();
        yaml.append("    behavior:\n");
        yaml.append("      maxParallelTasks: ").append(behavior.getMaxParallelTasks()).append("\n");
        yaml.append("      taskTimeoutSeconds: ").append(behavior.getTaskTimeoutSeconds()).append("\n");
        yaml.append("      maxRetryAttempts: ").append(behavior.getMaxRetryAttempts()).append("\n");
        yaml.append("      retryDelayMs: ").append(behavior.getRetryDelayMs()).append("\n");
        yaml.append("      deadlockPreventionEnabled: ").append(behavior.isDeadlockPreventionEnabled()).append("\n");
        yaml.append("      monitoringEnabled: ").append(behavior.isMonitoringEnabled()).append("\n");
        yaml.append("      monitoringCheckIntervalMs: ").append(behavior.getMonitoringCheckIntervalMs()).append("\n");

        // Add parameters if not empty
        Map<String, Object> parameters = agentConfig.getParameters();
        if (!parameters.isEmpty()) {
            yaml.append("\n  parameters:\n");
            parameters.forEach((key, value) -> yaml.append("    ").append(key).append(": ").append(value).append("\n"));
        }

        return yaml.toString();
    }

    /**
     * Simple implementation of AgentSkill interface
     */
    private static class SimpleAgentSkill implements AgentConfigurationRepository.AgentSkill {
        private final String name;
        private final String description;
        private final String version;
        private final Map<String, Object> parameters;
        private final boolean enabled;

        public SimpleAgentSkill(String name) {
            this.name = name;
            this.description = null;
            this.version = "1.0.0";
            this.parameters = Map.of();
            this.enabled = true;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public Map<String, Object> getParameters() {
            return parameters;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * Simple implementation of AgentBehavior interface
     */
    private static class SimpleAgentBehavior implements AgentConfigurationRepository.AgentBehavior {
        private final Map<String, Object> behaviorData;

        public SimpleAgentBehavior(Map<String, Object> behaviorData) {
            this.behaviorData = behaviorData;
        }

        @Override
        public int getMaxParallelTasks() {
            Object value = behaviorData.get("maxParallelTasks");
            return value instanceof Integer ? (Integer) value : 5;
        }

        @Override
        public int getTaskTimeoutSeconds() {
            Object value = behaviorData.get("taskTimeoutSeconds");
            return value instanceof Integer ? (Integer) value : 300;
        }

        @Override
        public int getMaxRetryAttempts() {
            Object value = behaviorData.get("maxRetryAttempts");
            return value instanceof Integer ? (Integer) value : 3;
        }

        @Override
        public long getRetryDelayMs() {
            Object value = behaviorData.get("retryDelayMs");
            return value instanceof Number ? ((Number) value).longValue() : 1000L;
        }

        @Override
        public boolean isDeadlockPreventionEnabled() {
            Object value = behaviorData.get("deadlockPreventionEnabled");
            return value instanceof Boolean ? (Boolean) value : true;
        }

        @Override
        public boolean isMonitoringEnabled() {
            Object value = behaviorData.get("monitoringEnabled");
            return value instanceof Boolean ? (Boolean) value : true;
        }

        @Override
        public long getMonitoringCheckIntervalMs() {
            Object value = behaviorData.get("monitoringCheckIntervalMs");
            return value instanceof Number ? ((Number) value).longValue() : 5000L;
        }
    }
}
