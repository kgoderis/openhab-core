package org.openhab.core.ai.reasoning.actions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.config.AgentConfiguration;
import org.openhab.core.ai.reasoning.config.AgentFullConfiguration;
import org.openhab.core.ai.reasoning.config.ConfigurationResult;
import org.openhab.core.ai.reasoning.constraints.ConstraintDefinition;
import org.openhab.core.ai.reasoning.constraints.ConstraintResult;
import org.openhab.core.ai.reasoning.monitoring.ConfigurationPerformanceMetrics;
import org.openhab.core.ai.reasoning.policies.BehaviorPolicy;
import org.openhab.core.ai.reasoning.policies.PolicyResult;
import org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig;
import org.openhab.core.ai.reasoning.policies.UserPreferenceConfig;
import org.openhab.core.ai.reasoning.results.PreferenceResult;
import org.openhab.core.ai.reasoning.results.SafetyResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Autonomous Behavior Configuration System for AI agents
 * 
 * Implements autonomous mode configuration, behavior policy management,
 * user preference configuration, and constraint definition and management.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = AutonomousBehaviorConfig.class)
public class AutonomousBehaviorConfig {

    private final Logger logger = LoggerFactory.getLogger(AutonomousBehaviorConfig.class);

    // Data storage
    private final Map<String, AgentConfiguration> agentConfigurations = new ConcurrentHashMap<>();
    private final Map<String, BehaviorPolicy> behaviorPolicies = new ConcurrentHashMap<>();
    private final Map<String, UserPreferenceConfig> userPreferences = new ConcurrentHashMap<>();
    private final Map<String, ConstraintDefinition> constraintDefinitions = new ConcurrentHashMap<>();
    private final Map<String, SafetyPolicyConfig> safetyPolicies = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalConfigurations = new AtomicLong(0);
    private final AtomicLong totalPolicyUpdates = new AtomicLong(0);
    private final AtomicLong totalPreferenceUpdates = new AtomicLong(0);
    private final AtomicLong totalConstraintUpdates = new AtomicLong(0);

    // Thread safety
    private final ReadWriteLock configLock = new ReentrantReadWriteLock();
    private final ReadWriteLock policyLock = new ReentrantReadWriteLock();
    private final ReadWriteLock preferenceLock = new ReentrantReadWriteLock();
    private final ReadWriteLock constraintLock = new ReentrantReadWriteLock();
    private final ReadWriteLock safetyLock = new ReentrantReadWriteLock();

    // Global configuration
    private boolean enableAutonomousMode = true;
    private boolean enableBehaviorLearning = true;
    private boolean enableSafetyConstraints = true;
    private double defaultConfidenceThreshold = 0.7;
    private Duration defaultTimeout = Duration.ofMinutes(5);
    private int maxConcurrentActions = 10;

    @Activate
    public void activate() {
        logger.info("Autonomous Behavior Configuration System activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Autonomous Behavior Configuration System deactivated");
    }

    /**
     * Configure autonomous behavior for an agent
     */
    public ConfigurationResult configureAgent(String agentId, AgentConfiguration configuration) {
        try {
            configLock.writeLock().lock();

            // Validate configuration
            ConfigurationValidationResult validation = validateConfiguration(configuration);
            if (!validation.isValid()) {
                return ConfigurationResult.invalid(validation.getReason());
            }

            agentConfigurations.put(agentId, configuration);
            totalConfigurations.incrementAndGet();
            logger.debug("Configured autonomous behavior for agent: {}", agentId);

            return ConfigurationResult.success(configuration);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Get agent configuration
     */
    public AgentConfiguration getAgentConfiguration(String agentId) {
        try {
            configLock.readLock().lock();
            return agentConfigurations.get(agentId);
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Add behavior policy
     */
    public PolicyResult addBehaviorPolicy(String policyId, BehaviorPolicy policy) {
        try {
            policyLock.writeLock().lock();

            behaviorPolicies.put(policyId, policy);
            totalPolicyUpdates.incrementAndGet();
            logger.debug("Added behavior policy: {}", policyId);

            return PolicyResult.success(policy);
        } finally {
            policyLock.writeLock().unlock();
        }
    }

    /**
     * Get behavior policy
     */
    public BehaviorPolicy getBehaviorPolicy(String policyId) {
        try {
            policyLock.readLock().lock();
            return behaviorPolicies.get(policyId);
        } finally {
            policyLock.readLock().unlock();
        }
    }

    /**
     * Configure user preferences
     */
    public PreferenceResult configureUserPreferences(String userId, UserPreferenceConfig preferences) {
        try {
            preferenceLock.writeLock().lock();

            userPreferences.put(userId, preferences);
            totalPreferenceUpdates.incrementAndGet();
            logger.debug("Configured user preferences for user: {}", userId);

            return PreferenceResult.success(preferences);
        } finally {
            preferenceLock.writeLock().unlock();
        }
    }

    /**
     * Get user preferences
     */
    public UserPreferenceConfig getUserPreferences(String userId) {
        try {
            preferenceLock.readLock().lock();
            return userPreferences.get(userId);
        } finally {
            preferenceLock.readLock().unlock();
        }
    }

    /**
     * Define constraint
     */
    public ConstraintResult defineConstraint(String constraintId, ConstraintDefinition constraint) {
        try {
            constraintLock.writeLock().lock();

            constraintDefinitions.put(constraintId, constraint);
            totalConstraintUpdates.incrementAndGet();
            logger.debug("Defined constraint: {}", constraintId);

            return ConstraintResult.success(constraint);
        } finally {
            constraintLock.writeLock().unlock();
        }
    }

    /**
     * Get constraint definition
     */
    public ConstraintDefinition getConstraintDefinition(String constraintId) {
        try {
            constraintLock.readLock().lock();
            return constraintDefinitions.get(constraintId);
        } finally {
            constraintLock.readLock().unlock();
        }
    }

    /**
     * Configure safety policy
     */
    public SafetyResult configureSafetyPolicy(String policyId, SafetyPolicyConfig policy) {
        try {
            safetyLock.writeLock().lock();

            safetyPolicies.put(policyId, policy);
            logger.debug("Configured safety policy: {}", policyId);

            return SafetyResult.success(policy);
        } finally {
            safetyLock.writeLock().unlock();
        }
    }

    /**
     * Get safety policy
     */
    public SafetyPolicyConfig getSafetyPolicy(String policyId) {
        try {
            safetyLock.readLock().lock();
            return safetyPolicies.get(policyId);
        } finally {
            safetyLock.readLock().unlock();
        }
    }

    /**
     * Get all configurations for an agent
     */
    public AgentFullConfiguration getFullConfiguration(String agentId) {
        try {
            configLock.readLock().lock();
            policyLock.readLock().lock();
            preferenceLock.readLock().lock();
            constraintLock.readLock().lock();
            safetyLock.readLock().lock();

            AgentConfiguration agentConfig = agentConfigurations.get(agentId);
            if (agentConfig == null) {
                return null;
            }

            List<BehaviorPolicy> policies = new ArrayList<>();
            if (agentConfig.getBehaviorPolicies() != null) {
                for (String policyId : agentConfig.getBehaviorPolicies()) {
                    BehaviorPolicy policy = behaviorPolicies.get(policyId);
                    if (policy != null) {
                        policies.add(policy);
                    }
                }
            }

            List<ConstraintDefinition> constraints = new ArrayList<>();
            if (agentConfig.getConstraints() != null) {
                for (String constraintId : agentConfig.getConstraints()) {
                    ConstraintDefinition constraint = constraintDefinitions.get(constraintId);
                    if (constraint != null) {
                        constraints.add(constraint);
                    }
                }
            }

            List<SafetyPolicyConfig> safetyPoliciesList = new ArrayList<>();
            if (agentConfig.getSafetyPolicies() != null) {
                for (String policyId : agentConfig.getSafetyPolicies()) {
                    SafetyPolicyConfig policy = safetyPolicies.get(policyId);
                    if (policy != null) {
                        safetyPoliciesList.add(policy);
                    }
                }
            }

            return new AgentFullConfiguration(agentConfig, policies, constraints, safetyPoliciesList);
        } finally {
            configLock.readLock().unlock();
            policyLock.readLock().unlock();
            preferenceLock.readLock().unlock();
            constraintLock.readLock().unlock();
            safetyLock.readLock().unlock();
        }
    }

    /**
     * Get configuration performance metrics
     */
    public ConfigurationPerformanceMetrics getPerformanceMetrics() {
        return new ConfigurationPerformanceMetrics("autonomous-behavior-config", 0, // totalOperations - not tracked yet
                0, // successfulOperations - not tracked yet
                0, // failedOperations - not tracked yet
                0, // totalProcessingTime - not tracked yet
                0.0, // averageResponseTime - not tracked yet
                totalPolicyUpdates.get(), // totalPolicyUpdates
                totalPreferenceUpdates.get(), // totalPreferenceUpdates
                totalConstraintUpdates.get(), // totalConstraintUpdates
                agentConfigurations.size(), // agentConfigurationCount
                behaviorPolicies.size(), // behaviorPolicyCount
                userPreferences.size(), // userPreferenceCount
                constraintDefinitions.size(), // constraintDefinitionCount
                safetyPolicies.size() // safetyPolicyCount
        );
    }

    // Configuration methods
    public void setEnableAutonomousMode(boolean enableAutonomousMode) {
        this.enableAutonomousMode = enableAutonomousMode;
    }

    public void setEnableBehaviorLearning(boolean enableBehaviorLearning) {
        this.enableBehaviorLearning = enableBehaviorLearning;
    }

    public void setEnableSafetyConstraints(boolean enableSafetyConstraints) {
        this.enableSafetyConstraints = enableSafetyConstraints;
    }

    public void setDefaultConfidenceThreshold(double defaultConfidenceThreshold) {
        this.defaultConfidenceThreshold = Math.max(0.0, Math.min(1.0, defaultConfidenceThreshold));
    }

    public void setDefaultTimeout(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void setMaxConcurrentActions(int maxConcurrentActions) {
        this.maxConcurrentActions = Math.max(1, maxConcurrentActions);
    }

    // Private helper methods
    private ConfigurationValidationResult validateConfiguration(AgentConfiguration configuration) {
        if (configuration == null) {
            return ConfigurationValidationResult.invalid("Configuration cannot be null");
        }

        if (configuration.getAgentId() == null || configuration.getAgentId().trim().isEmpty()) {
            return ConfigurationValidationResult.invalid("Agent ID cannot be null or empty");
        }

        if (configuration.getConfidenceThreshold() < 0.0 || configuration.getConfidenceThreshold() > 1.0) {
            return ConfigurationValidationResult.invalid("Confidence threshold must be between 0.0 and 1.0");
        }

        if (configuration.getTimeout() != null && configuration.getTimeout().isNegative()) {
            return ConfigurationValidationResult.invalid("Timeout cannot be negative");
        }

        return ConfigurationValidationResult.valid();
    }

    // Result classes
    /* Extracted: org.openhab.core.ai.reasoning.ConfigurationResult */

    /* Extracted: org.openhab.core.ai.reasoning.PolicyResult */

    /* Extracted: org.openhab.core.ai.reasoning.PreferenceResult */

    /* Extracted: org.openhab.core.ai.reasoning.ConstraintResult */

    /* Extracted: org.openhab.core.ai.reasoning.SafetyResult */

    public static class ConfigurationValidationResult {
        private final boolean valid;
        private final String reason;

        private ConfigurationValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ConfigurationValidationResult valid() {
            return new ConfigurationValidationResult(true, "Configuration is valid");
        }

        public static ConfigurationValidationResult invalid(String reason) {
            return new ConfigurationValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }

    /* Extracted: org.openhab.core.ai.reasoning.ConfigurationPerformanceMetrics */

    // Configuration data classes extracted to top-level types in org.openhab.core.ai.reasoning

    // BehaviorPolicy extracted to org.openhab.core.ai.reasoning.BehaviorPolicy

    // UserPreferenceConfig extracted to org.openhab.core.ai.reasoning.UserPreferenceConfig

    // ConstraintDefinition extracted to org.openhab.core.ai.reasoning.ConstraintDefinition

    // SafetyPolicyConfig extracted to org.openhab.core.ai.reasoning.SafetyPolicyConfig

    // AgentFullConfiguration extracted to org.openhab.core.ai.reasoning.AgentFullConfiguration
}
