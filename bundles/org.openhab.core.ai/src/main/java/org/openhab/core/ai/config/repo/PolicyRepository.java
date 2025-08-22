package org.openhab.core.ai.config.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Repository for managing YAML policy definitions.
 * 
 * <p>
 * This interface provides methods for loading, storing, and managing policy definitions
 * from YAML files in the /conf/ai/policies/ directory. It supports policy rules,
 * conditions, and actions for controlling AI behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface PolicyRepository {

    /**
     * Loads all policy definitions from the configured directory.
     * 
     * @return map of policy names to their definitions
     * @throws PolicyRepositoryException if loading fails
     */
    Map<String, PolicyDefinition> loadAllPolicies() throws PolicyRepositoryException;

    /**
     * Loads a specific policy definition by name.
     * 
     * @param policyName the name of the policy to load
     * @return the policy definition if found
     * @throws PolicyRepositoryException if loading fails
     */
    Optional<PolicyDefinition> loadPolicy(String policyName) throws PolicyRepositoryException;

    /**
     * Loads policies for a specific agent.
     * 
     * @param agentName the name of the agent
     * @return map of policy names to their definitions for the agent
     * @throws PolicyRepositoryException if loading fails
     */
    Map<String, PolicyDefinition> loadAgentPolicies(String agentName) throws PolicyRepositoryException;

    /**
     * Saves a policy definition to the repository.
     * 
     * @param policyName the name of the policy
     * @param policy the policy definition
     * @throws PolicyRepositoryException if saving fails
     */
    void savePolicy(String policyName, PolicyDefinition policy) throws PolicyRepositoryException;

    /**
     * Deletes a policy definition from the repository.
     * 
     * @param policyName the name of the policy to delete
     * @throws PolicyRepositoryException if deletion fails
     */
    void deletePolicy(String policyName) throws PolicyRepositoryException;

    /**
     * Validates a policy definition structure.
     * 
     * @param policy the policy to validate
     * @return list of validation errors, empty if valid
     */
    List<String> validatePolicy(PolicyDefinition policy);

    /**
     * Gets the list of available policy names.
     * 
     * @return list of policy names
     * @throws PolicyRepositoryException if listing fails
     */
    List<String> getPolicyNames() throws PolicyRepositoryException;

    /**
     * Checks if a policy exists.
     * 
     * @param policyName the name of the policy
     * @return true if the policy exists
     */
    boolean policyExists(String policyName);

    /**
     * Reloads all policies from disk.
     * 
     * @throws PolicyRepositoryException if reloading fails
     */
    void reload() throws PolicyRepositoryException;

    /**
     * Represents a policy definition with rules and metadata.
     */
    interface PolicyDefinition {
        /**
         * Gets the policy name.
         * 
         * @return the policy name
         */
        String getName();

        /**
         * Gets the policy description.
         * 
         * @return the policy description
         */
        @Nullable
        String getDescription();

        /**
         * Gets the policy version.
         * 
         * @return the policy version
         */
        String getVersion();

        /**
         * Gets the policy rules.
         * 
         * @return list of policy rules
         */
        List<PolicyRule> getRules();

        /**
         * Gets the policy metadata.
         * 
         * @return map of metadata key-value pairs
         */
        Map<String, Object> getMetadata();

        /**
         * Gets the agent this policy is associated with.
         * 
         * @return the agent name, or null if not associated
         */
        @Nullable
        String getAgent();

        /**
         * Gets the priority level of this policy.
         * 
         * @return the priority level
         */
        PolicyPriority getPriority();
    }

    /**
     * Represents a policy rule with condition and action.
     */
    interface PolicyRule {
        /**
         * Gets the rule name.
         * 
         * @return the rule name
         */
        String getName();

        /**
         * Gets the rule condition.
         * 
         * @return the rule condition
         */
        String getCondition();

        /**
         * Gets the rule action.
         * 
         * @return the rule action
         */
        String getAction();

        /**
         * Gets the rule priority.
         * 
         * @return the rule priority
         */
        PolicyPriority getPriority();

        /**
         * Gets the rule metadata.
         * 
         * @return map of metadata key-value pairs
         */
        Map<String, Object> getMetadata();
    }

    /**
     * Represents policy priority levels.
     */
    enum PolicyPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
