package org.openhab.core.ai.config.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Repository for managing YAML agent configurations.
 * 
 * <p>
 * This interface provides methods for loading, storing, and managing agent configurations
 * from YAML files in the /conf/ai/agents/ directory. It supports agent-specific settings,
 * skills, and behavior configurations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface AgentConfigurationRepository {

    /**
     * Loads all agent configurations from the configured directory.
     * 
     * @return map of agent names to their configurations
     * @throws AgentConfigurationRepositoryException if loading fails
     */
    Map<String, AgentConfiguration> loadAllConfigurations() throws AgentConfigurationRepositoryException;

    /**
     * Loads a specific agent configuration by name.
     * 
     * @param agentName the name of the agent to load
     * @return the agent configuration if found
     * @throws AgentConfigurationRepositoryException if loading fails
     */
    Optional<AgentConfiguration> loadConfiguration(String agentName) throws AgentConfigurationRepositoryException;

    /**
     * Saves an agent configuration to the repository.
     * 
     * @param agentName the name of the agent
     * @param configuration the agent configuration
     * @throws AgentConfigurationRepositoryException if saving fails
     */
    void saveConfiguration(String agentName, AgentConfiguration configuration)
            throws AgentConfigurationRepositoryException;

    /**
     * Deletes an agent configuration from the repository.
     * 
     * @param agentName the name of the agent to delete
     * @throws AgentConfigurationRepositoryException if deletion fails
     */
    void deleteConfiguration(String agentName) throws AgentConfigurationRepositoryException;

    /**
     * Validates an agent configuration structure.
     * 
     * @param configuration the configuration to validate
     * @return list of validation errors, empty if valid
     */
    List<String> validateConfiguration(AgentConfiguration configuration);

    /**
     * Gets the list of available agent names.
     * 
     * @return list of agent names
     * @throws AgentConfigurationRepositoryException if listing fails
     */
    List<String> getAgentNames() throws AgentConfigurationRepositoryException;

    /**
     * Checks if an agent configuration exists.
     * 
     * @param agentName the name of the agent
     * @return true if the agent configuration exists
     */
    boolean configurationExists(String agentName);

    /**
     * Reloads all configurations from disk.
     * 
     * @throws AgentConfigurationRepositoryException if reloading fails
     */
    void reload() throws AgentConfigurationRepositoryException;

    /**
     * Represents an agent configuration with settings and metadata.
     */
    interface AgentConfiguration {
        /**
         * Gets the agent name.
         * 
         * @return the agent name
         */
        String getName();

        /**
         * Gets the agent description.
         * 
         * @return the agent description
         */
        @Nullable
        String getDescription();

        /**
         * Gets the agent version.
         * 
         * @return the agent version
         */
        String getVersion();

        /**
         * Gets the agent type.
         * 
         * @return the agent type
         */
        String getType();

        /**
         * Gets the agent skills.
         * 
         * @return list of agent skills
         */
        List<AgentSkill> getSkills();

        /**
         * Gets the agent behavior settings.
         * 
         * @return the behavior settings
         */
        AgentBehavior getBehavior();

        /**
         * Gets the agent metadata.
         * 
         * @return map of metadata key-value pairs
         */
        Map<String, Object> getMetadata();

        /**
         * Gets the agent configuration parameters.
         * 
         * @return map of configuration parameters
         */
        Map<String, Object> getParameters();
    }

    /**
     * Represents an agent skill configuration.
     */
    interface AgentSkill {
        /**
         * Gets the skill name.
         * 
         * @return the skill name
         */
        String getName();

        /**
         * Gets the skill description.
         * 
         * @return the skill description
         */
        @Nullable
        String getDescription();

        /**
         * Gets the skill version.
         * 
         * @return the skill version
         */
        String getVersion();

        /**
         * Gets the skill parameters.
         * 
         * @return map of skill parameters
         */
        Map<String, Object> getParameters();

        /**
         * Gets whether the skill is enabled.
         * 
         * @return true if the skill is enabled
         */
        boolean isEnabled();
    }

    /**
     * Represents agent behavior settings.
     */
    interface AgentBehavior {
        /**
         * Gets the maximum parallel tasks.
         * 
         * @return the maximum parallel tasks
         */
        int getMaxParallelTasks();

        /**
         * Gets the task timeout in seconds.
         * 
         * @return the task timeout
         */
        int getTaskTimeoutSeconds();

        /**
         * Gets the maximum retry attempts.
         * 
         * @return the maximum retry attempts
         */
        int getMaxRetryAttempts();

        /**
         * Gets the retry delay in milliseconds.
         * 
         * @return the retry delay
         */
        long getRetryDelayMs();

        /**
         * Gets whether deadlock prevention is enabled.
         * 
         * @return true if deadlock prevention is enabled
         */
        boolean isDeadlockPreventionEnabled();

        /**
         * Gets whether monitoring is enabled.
         * 
         * @return true if monitoring is enabled
         */
        boolean isMonitoringEnabled();

        /**
         * Gets the monitoring check interval in milliseconds.
         * 
         * @return the monitoring check interval
         */
        long getMonitoringCheckIntervalMs();
    }
}
