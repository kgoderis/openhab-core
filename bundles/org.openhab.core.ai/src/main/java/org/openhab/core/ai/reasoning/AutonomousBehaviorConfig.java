package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
        return ConfigurationPerformanceMetrics.builder().totalConfigurations(totalConfigurations.get())
                .totalPolicyUpdates(totalPolicyUpdates.get()).totalPreferenceUpdates(totalPreferenceUpdates.get())
                .totalConstraintUpdates(totalConstraintUpdates.get())
                .agentConfigurationCount(agentConfigurations.size()).behaviorPolicyCount(behaviorPolicies.size())
                .userPreferenceCount(userPreferences.size()).constraintDefinitionCount(constraintDefinitions.size())
                .safetyPolicyCount(safetyPolicies.size()).build();
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
    public static class ConfigurationResult {
        private final boolean success;
        private final String message;
        private final AgentConfiguration configuration;

        private ConfigurationResult(boolean success, String message, AgentConfiguration configuration) {
            this.success = success;
            this.message = message;
            this.configuration = configuration;
        }

        public static ConfigurationResult success(AgentConfiguration configuration) {
            return new ConfigurationResult(true, "Configuration successful", configuration);
        }

        public static ConfigurationResult invalid(String reason) {
            return new ConfigurationResult(false, "Configuration invalid: " + reason, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public AgentConfiguration getConfiguration() {
            return configuration;
        }
    }

    public static class PolicyResult {
        private final boolean success;
        private final String message;
        private final BehaviorPolicy policy;

        private PolicyResult(boolean success, String message, BehaviorPolicy policy) {
            this.success = success;
            this.message = message;
            this.policy = policy;
        }

        public static PolicyResult success(BehaviorPolicy policy) {
            return new PolicyResult(true, "Policy operation successful", policy);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public BehaviorPolicy getPolicy() {
            return policy;
        }
    }

    public static class PreferenceResult {
        private final boolean success;
        private final String message;
        private final UserPreferenceConfig preferences;

        private PreferenceResult(boolean success, String message, UserPreferenceConfig preferences) {
            this.success = success;
            this.message = message;
            this.preferences = preferences;
        }

        public static PreferenceResult success(UserPreferenceConfig preferences) {
            return new PreferenceResult(true, "Preference configuration successful", preferences);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public UserPreferenceConfig getPreferences() {
            return preferences;
        }
    }

    public static class ConstraintResult {
        private final boolean success;
        private final String message;
        private final ConstraintDefinition constraint;

        private ConstraintResult(boolean success, String message, ConstraintDefinition constraint) {
            this.success = success;
            this.message = message;
            this.constraint = constraint;
        }

        public static ConstraintResult success(ConstraintDefinition constraint) {
            return new ConstraintResult(true, "Constraint definition successful", constraint);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public ConstraintDefinition getConstraint() {
            return constraint;
        }
    }

    public static class SafetyResult {
        private final boolean success;
        private final String message;
        private final SafetyPolicyConfig policy;

        private SafetyResult(boolean success, String message, SafetyPolicyConfig policy) {
            this.success = success;
            this.message = message;
            this.policy = policy;
        }

        public static SafetyResult success(SafetyPolicyConfig policy) {
            return new SafetyResult(true, "Safety policy configuration successful", policy);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public SafetyPolicyConfig getPolicy() {
            return policy;
        }
    }

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

    public static class ConfigurationPerformanceMetrics {
        private final long totalConfigurations;
        private final long totalPolicyUpdates;
        private final long totalPreferenceUpdates;
        private final long totalConstraintUpdates;
        private final int agentConfigurationCount;
        private final int behaviorPolicyCount;
        private final int userPreferenceCount;
        private final int constraintDefinitionCount;
        private final int safetyPolicyCount;

        private ConfigurationPerformanceMetrics(Builder builder) {
            this.totalConfigurations = builder.totalConfigurations;
            this.totalPolicyUpdates = builder.totalPolicyUpdates;
            this.totalPreferenceUpdates = builder.totalPreferenceUpdates;
            this.totalConstraintUpdates = builder.totalConstraintUpdates;
            this.agentConfigurationCount = builder.agentConfigurationCount;
            this.behaviorPolicyCount = builder.behaviorPolicyCount;
            this.userPreferenceCount = builder.userPreferenceCount;
            this.constraintDefinitionCount = builder.constraintDefinitionCount;
            this.safetyPolicyCount = builder.safetyPolicyCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public long getTotalConfigurations() {
            return totalConfigurations;
        }

        public long getTotalPolicyUpdates() {
            return totalPolicyUpdates;
        }

        public long getTotalPreferenceUpdates() {
            return totalPreferenceUpdates;
        }

        public long getTotalConstraintUpdates() {
            return totalConstraintUpdates;
        }

        public int getAgentConfigurationCount() {
            return agentConfigurationCount;
        }

        public int getBehaviorPolicyCount() {
            return behaviorPolicyCount;
        }

        public int getUserPreferenceCount() {
            return userPreferenceCount;
        }

        public int getConstraintDefinitionCount() {
            return constraintDefinitionCount;
        }

        public int getSafetyPolicyCount() {
            return safetyPolicyCount;
        }

        public static class Builder {
            private long totalConfigurations;
            private long totalPolicyUpdates;
            private long totalPreferenceUpdates;
            private long totalConstraintUpdates;
            private int agentConfigurationCount;
            private int behaviorPolicyCount;
            private int userPreferenceCount;
            private int constraintDefinitionCount;
            private int safetyPolicyCount;

            public Builder totalConfigurations(long totalConfigurations) {
                this.totalConfigurations = totalConfigurations;
                return this;
            }

            public Builder totalPolicyUpdates(long totalPolicyUpdates) {
                this.totalPolicyUpdates = totalPolicyUpdates;
                return this;
            }

            public Builder totalPreferenceUpdates(long totalPreferenceUpdates) {
                this.totalPreferenceUpdates = totalPreferenceUpdates;
                return this;
            }

            public Builder totalConstraintUpdates(long totalConstraintUpdates) {
                this.totalConstraintUpdates = totalConstraintUpdates;
                return this;
            }

            public Builder agentConfigurationCount(int agentConfigurationCount) {
                this.agentConfigurationCount = agentConfigurationCount;
                return this;
            }

            public Builder behaviorPolicyCount(int behaviorPolicyCount) {
                this.behaviorPolicyCount = behaviorPolicyCount;
                return this;
            }

            public Builder userPreferenceCount(int userPreferenceCount) {
                this.userPreferenceCount = userPreferenceCount;
                return this;
            }

            public Builder constraintDefinitionCount(int constraintDefinitionCount) {
                this.constraintDefinitionCount = constraintDefinitionCount;
                return this;
            }

            public Builder safetyPolicyCount(int safetyPolicyCount) {
                this.safetyPolicyCount = safetyPolicyCount;
                return this;
            }

            public ConfigurationPerformanceMetrics build() {
                return new ConfigurationPerformanceMetrics(this);
            }
        }
    }

    // Configuration data classes
    public static class AgentConfiguration {
        private final String agentId;
        private final boolean autonomousModeEnabled;
        private final boolean behaviorLearningEnabled;
        private final boolean safetyConstraintsEnabled;
        private final double confidenceThreshold;
        private final Duration timeout;
        private final int maxConcurrentActions;
        private final List<String> behaviorPolicies;
        private final List<String> constraints;
        private final List<String> safetyPolicies;
        private final Map<String, Object> customSettings;

        private AgentConfiguration(Builder builder) {
            this.agentId = builder.agentId;
            this.autonomousModeEnabled = builder.autonomousModeEnabled;
            this.behaviorLearningEnabled = builder.behaviorLearningEnabled;
            this.safetyConstraintsEnabled = builder.safetyConstraintsEnabled;
            this.confidenceThreshold = builder.confidenceThreshold;
            this.timeout = builder.timeout;
            this.maxConcurrentActions = builder.maxConcurrentActions;
            this.behaviorPolicies = builder.behaviorPolicies;
            this.constraints = builder.constraints;
            this.safetyPolicies = builder.safetyPolicies;
            this.customSettings = builder.customSettings;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getAgentId() {
            return agentId;
        }

        public boolean isAutonomousModeEnabled() {
            return autonomousModeEnabled;
        }

        public boolean isBehaviorLearningEnabled() {
            return behaviorLearningEnabled;
        }

        public boolean isSafetyConstraintsEnabled() {
            return safetyConstraintsEnabled;
        }

        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public int getMaxConcurrentActions() {
            return maxConcurrentActions;
        }

        public List<String> getBehaviorPolicies() {
            return behaviorPolicies;
        }

        public List<String> getConstraints() {
            return constraints;
        }

        public List<String> getSafetyPolicies() {
            return safetyPolicies;
        }

        public Map<String, Object> getCustomSettings() {
            return customSettings;
        }

        public static class Builder {
            private String agentId;
            private boolean autonomousModeEnabled = true;
            private boolean behaviorLearningEnabled = true;
            private boolean safetyConstraintsEnabled = true;
            private double confidenceThreshold = 0.7;
            private Duration timeout = Duration.ofMinutes(5);
            private int maxConcurrentActions = 10;
            private List<String> behaviorPolicies = new ArrayList<>();
            private List<String> constraints = new ArrayList<>();
            private List<String> safetyPolicies = new ArrayList<>();
            private Map<String, Object> customSettings = new ConcurrentHashMap<>();

            public Builder agentId(String agentId) {
                this.agentId = agentId;
                return this;
            }

            public Builder autonomousModeEnabled(boolean autonomousModeEnabled) {
                this.autonomousModeEnabled = autonomousModeEnabled;
                return this;
            }

            public Builder behaviorLearningEnabled(boolean behaviorLearningEnabled) {
                this.behaviorLearningEnabled = behaviorLearningEnabled;
                return this;
            }

            public Builder safetyConstraintsEnabled(boolean safetyConstraintsEnabled) {
                this.safetyConstraintsEnabled = safetyConstraintsEnabled;
                return this;
            }

            public Builder confidenceThreshold(double confidenceThreshold) {
                this.confidenceThreshold = confidenceThreshold;
                return this;
            }

            public Builder timeout(Duration timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder maxConcurrentActions(int maxConcurrentActions) {
                this.maxConcurrentActions = maxConcurrentActions;
                return this;
            }

            public Builder behaviorPolicies(List<String> behaviorPolicies) {
                this.behaviorPolicies = behaviorPolicies;
                return this;
            }

            public Builder constraints(List<String> constraints) {
                this.constraints = constraints;
                return this;
            }

            public Builder safetyPolicies(List<String> safetyPolicies) {
                this.safetyPolicies = safetyPolicies;
                return this;
            }

            public Builder customSettings(Map<String, Object> customSettings) {
                this.customSettings = customSettings;
                return this;
            }

            public AgentConfiguration build() {
                return new AgentConfiguration(this);
            }
        }
    }

    public static class BehaviorPolicy {
        private final String policyId;
        private final String name;
        private final String description;
        private final String actionType;
        private final Map<String, Object> parameters;
        private final boolean enabled;
        private final int priority;

        public BehaviorPolicy(String policyId, String name, String description, String actionType,
                Map<String, Object> parameters, boolean enabled, int priority) {
            this.policyId = policyId;
            this.name = name;
            this.description = description;
            this.actionType = actionType;
            this.parameters = parameters;
            this.enabled = enabled;
            this.priority = priority;
        }

        public String getPolicyId() {
            return policyId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getActionType() {
            return actionType;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getPriority() {
            return priority;
        }
    }

    public static class UserPreferenceConfig {
        private final String userId;
        private final Map<String, Object> preferences;
        private final boolean learningEnabled;
        private final double learningRate;

        public UserPreferenceConfig(String userId, Map<String, Object> preferences, boolean learningEnabled,
                double learningRate) {
            this.userId = userId;
            this.preferences = preferences;
            this.learningEnabled = learningEnabled;
            this.learningRate = learningRate;
        }

        public String getUserId() {
            return userId;
        }

        public Map<String, Object> getPreferences() {
            return preferences;
        }

        public boolean isLearningEnabled() {
            return learningEnabled;
        }

        public double getLearningRate() {
            return learningRate;
        }
    }

    public static class ConstraintDefinition {
        private final String constraintId;
        private final String name;
        private final String description;
        private final String constraintType;
        private final Map<String, Object> parameters;
        private final boolean enabled;
        private final int severity;

        public ConstraintDefinition(String constraintId, String name, String description, String constraintType,
                Map<String, Object> parameters, boolean enabled, int severity) {
            this.constraintId = constraintId;
            this.name = name;
            this.description = description;
            this.constraintType = constraintType;
            this.parameters = parameters;
            this.enabled = enabled;
            this.severity = severity;
        }

        public String getConstraintId() {
            return constraintId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getConstraintType() {
            return constraintType;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getSeverity() {
            return severity;
        }
    }

    public static class SafetyPolicyConfig {
        private final String policyId;
        private final String name;
        private final String description;
        private final String policyType;
        private final Map<String, Object> parameters;
        private final boolean enabled;
        private final double safetyThreshold;

        public SafetyPolicyConfig(String policyId, String name, String description, String policyType,
                Map<String, Object> parameters, boolean enabled, double safetyThreshold) {
            this.policyId = policyId;
            this.name = name;
            this.description = description;
            this.policyType = policyType;
            this.parameters = parameters;
            this.enabled = enabled;
            this.safetyThreshold = safetyThreshold;
        }

        public String getPolicyId() {
            return policyId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getPolicyType() {
            return policyType;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public double getSafetyThreshold() {
            return safetyThreshold;
        }
    }

    public static class AgentFullConfiguration {
        private final AgentConfiguration agentConfiguration;
        private final List<BehaviorPolicy> behaviorPolicies;
        private final List<ConstraintDefinition> constraints;
        private final List<SafetyPolicyConfig> safetyPolicies;

        public AgentFullConfiguration(AgentConfiguration agentConfiguration, List<BehaviorPolicy> behaviorPolicies,
                List<ConstraintDefinition> constraints, List<SafetyPolicyConfig> safetyPolicies) {
            this.agentConfiguration = agentConfiguration;
            this.behaviorPolicies = behaviorPolicies;
            this.constraints = constraints;
            this.safetyPolicies = safetyPolicies;
        }

        public AgentConfiguration getAgentConfiguration() {
            return agentConfiguration;
        }

        public List<BehaviorPolicy> getBehaviorPolicies() {
            return behaviorPolicies;
        }

        public List<ConstraintDefinition> getConstraints() {
            return constraints;
        }

        public List<SafetyPolicyConfig> getSafetyPolicies() {
            return safetyPolicies;
        }
    }
}
