package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
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
 * Safety and Constraint Management System for AI agents
 * 
 * Implements action safety validation, user-defined constraint enforcement,
 * safety policy management, and constraint violation detection.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = SafetyConstraintManager.class)
public class SafetyConstraintManager {

    private final Logger logger = LoggerFactory.getLogger(SafetyConstraintManager.class);

    // Data storage
    private final Map<String, SafetyPolicy> safetyPolicies = new ConcurrentHashMap<>();
    private final Map<String, UserConstraint> userConstraints = new ConcurrentHashMap<>();
    private final Map<String, ConstraintViolation> constraintViolations = new ConcurrentHashMap<>();
    private final Map<String, SafetyIncident> safetyIncidents = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalSafetyValidations = new AtomicLong(0);
    private final AtomicLong totalConstraintViolations = new AtomicLong(0);
    private final AtomicLong totalSafetyIncidents = new AtomicLong(0);
    private final AtomicLong totalSafetyOverrides = new AtomicLong(0);

    // Thread safety
    private final ReadWriteLock policyLock = new ReentrantReadWriteLock();
    private final ReadWriteLock constraintLock = new ReentrantReadWriteLock();
    private final ReadWriteLock violationLock = new ReentrantReadWriteLock();
    private final ReadWriteLock incidentLock = new ReentrantReadWriteLock();

    // Configuration
    private boolean enableSafetyValidation = true;
    private boolean enableConstraintEnforcement = true;
    private boolean enableIncidentReporting = true;
    private double safetyThreshold = 0.8;
    private int maxViolationHistory = 1000;
    private int maxIncidentHistory = 500;
    private Duration violationRetentionPeriod = Duration.ofDays(30);

    @Activate
    public void activate() {
        logger.info("Safety and Constraint Management System activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Safety and Constraint Management System deactivated");
    }

    /**
     * Validate action safety
     */
    public SafetyValidationResult validateAction(String agentId, String actionType,
            Map<String, Object> actionParameters, String userId) {
        if (!enableSafetyValidation) {
            return SafetyValidationResult.disabled("Safety validation is disabled");
        }

        try {
            policyLock.readLock().lock();
            constraintLock.readLock().lock();

            totalSafetyValidations.incrementAndGet();

            // Check safety policies
            SafetyPolicy policy = safetyPolicies.get(agentId);
            if (policy != null) {
                SafetyValidationResult policyResult = policy.validateAction(actionType, actionParameters);
                if (!policyResult.isValid()) {
                    recordConstraintViolation(agentId, actionType, actionParameters, userId,
                            "Safety policy violation: " + policyResult.getReason());
                    return policyResult;
                }
            }

            // Check user constraints
            UserConstraint constraint = userConstraints.get(userId);
            if (constraint != null) {
                SafetyValidationResult constraintResult = constraint.validateAction(actionType, actionParameters);
                if (!constraintResult.isValid()) {
                    recordConstraintViolation(agentId, actionType, actionParameters, userId,
                            "User constraint violation: " + constraintResult.getReason());
                    return constraintResult;
                }
            }

            // Check global safety rules
            SafetyValidationResult globalResult = validateGlobalSafetyRules(actionType, actionParameters);
            if (!globalResult.isValid()) {
                recordConstraintViolation(agentId, actionType, actionParameters, userId,
                        "Global safety rule violation: " + globalResult.getReason());
                return globalResult;
            }

            logger.debug("Action validated successfully: {} for agent: {}", actionType, agentId);
            return SafetyValidationResult.valid();
        } finally {
            policyLock.readLock().unlock();
            constraintLock.readLock().unlock();
        }
    }

    /**
     * Add user-defined constraint
     */
    public ConstraintResult addUserConstraint(String userId, String constraintType,
            Map<String, Object> constraintParameters, String description) {
        if (!enableConstraintEnforcement) {
            return ConstraintResult.disabled("Constraint enforcement is disabled");
        }

        try {
            constraintLock.writeLock().lock();

            UserConstraint constraint = userConstraints.computeIfAbsent(userId, k -> new UserConstraint(userId));
            boolean added = constraint.addConstraint(constraintType, constraintParameters, description);

            if (added) {
                logger.debug("Added user constraint: {} for user: {}", constraintType, userId);
            }

            return ConstraintResult.success(added, constraint.getConstraintCount());
        } finally {
            constraintLock.writeLock().unlock();
        }
    }

    /**
     * Remove user-defined constraint
     */
    public ConstraintResult removeUserConstraint(String userId, String constraintType) {
        try {
            constraintLock.writeLock().lock();

            UserConstraint constraint = userConstraints.get(userId);
            if (constraint == null) {
                return ConstraintResult.notFound("No constraints found for user: " + userId);
            }

            boolean removed = constraint.removeConstraint(constraintType);
            if (removed) {
                logger.debug("Removed user constraint: {} for user: {}", constraintType, userId);
            }

            return ConstraintResult.success(removed, constraint.getConstraintCount());
        } finally {
            constraintLock.writeLock().unlock();
        }
    }

    /**
     * Add safety policy
     */
    public PolicyResult addSafetyPolicy(String agentId, String policyType, Map<String, Object> policyParameters,
            String description) {
        try {
            policyLock.writeLock().lock();

            SafetyPolicy policy = safetyPolicies.computeIfAbsent(agentId, k -> new SafetyPolicy(agentId));
            boolean added = policy.addPolicy(policyType, policyParameters, description);

            if (added) {
                logger.debug("Added safety policy: {} for agent: {}", policyType, agentId);
            }

            return PolicyResult.success(added, policy.getPolicyCount());
        } finally {
            policyLock.writeLock().unlock();
        }
    }

    /**
     * Override safety constraint
     */
    public OverrideResult overrideSafetyConstraint(String agentId, String actionType,
            Map<String, Object> actionParameters, String userId, String overrideReason) {
        try {
            violationLock.writeLock().lock();

            // Record the override
            ConstraintViolation violation = new ConstraintViolation(agentId, actionType, actionParameters, userId,
                    "OVERRIDE: " + overrideReason, Instant.now());
            constraintViolations.put(violation.getId(), violation);

            totalSafetyOverrides.incrementAndGet();
            logger.debug("Safety constraint overridden: {} for agent: {} by user: {}", actionType, agentId, userId);

            return OverrideResult.success(violation);
        } finally {
            violationLock.writeLock().unlock();
        }
    }

    /**
     * Report safety incident
     */
    public IncidentResult reportSafetyIncident(String agentId, String incidentType, String description,
            Map<String, Object> incidentData, String reporterId) {
        if (!enableIncidentReporting) {
            return IncidentResult.disabled("Incident reporting is disabled");
        }

        try {
            incidentLock.writeLock().lock();

            SafetyIncident incident = new SafetyIncident(agentId, incidentType, description, incidentData, reporterId,
                    Instant.now());
            safetyIncidents.put(incident.getId(), incident);

            totalSafetyIncidents.incrementAndGet();
            logger.warn("Safety incident reported: {} for agent: {} by: {}", incidentType, agentId, reporterId);

            return IncidentResult.success(incident);
        } finally {
            incidentLock.writeLock().unlock();
        }
    }

    /**
     * Get constraint violations
     */
    public List<ConstraintViolation> getConstraintViolations(String agentId, int limit) {
        try {
            violationLock.readLock().lock();

            return constraintViolations.values().stream().filter(v -> agentId.equals(v.getAgentId()))
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(limit).toList();
        } finally {
            violationLock.readLock().unlock();
        }
    }

    /**
     * Get safety incidents
     */
    public List<SafetyIncident> getSafetyIncidents(String agentId, int limit) {
        try {
            incidentLock.readLock().lock();

            return safetyIncidents.values().stream().filter(i -> agentId.equals(i.getAgentId()))
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(limit).toList();
        } finally {
            incidentLock.readLock().unlock();
        }
    }

    /**
     * Get safety performance metrics
     */
    public SafetyPerformanceMetrics getPerformanceMetrics() {
        return SafetyPerformanceMetrics.builder().totalSafetyValidations(totalSafetyValidations.get())
                .totalConstraintViolations(totalConstraintViolations.get())
                .totalSafetyIncidents(totalSafetyIncidents.get()).totalSafetyOverrides(totalSafetyOverrides.get())
                .safetyPolicyCount(safetyPolicies.size()).userConstraintCount(userConstraints.size())
                .constraintViolationCount(constraintViolations.size()).safetyIncidentCount(safetyIncidents.size())
                .build();
    }

    // Configuration methods
    public void setSafetyThreshold(double safetyThreshold) {
        this.safetyThreshold = Math.max(0.0, Math.min(1.0, safetyThreshold));
    }

    public void setEnableSafetyValidation(boolean enableSafetyValidation) {
        this.enableSafetyValidation = enableSafetyValidation;
    }

    public void setEnableConstraintEnforcement(boolean enableConstraintEnforcement) {
        this.enableConstraintEnforcement = enableConstraintEnforcement;
    }

    public void setEnableIncidentReporting(boolean enableIncidentReporting) {
        this.enableIncidentReporting = enableIncidentReporting;
    }

    // Private helper methods
    private SafetyValidationResult validateGlobalSafetyRules(String actionType, Map<String, Object> actionParameters) {
        // Global safety rules - can be enhanced with more sophisticated validation
        if ("system_shutdown".equals(actionType) || "delete_all_data".equals(actionType)) {
            return SafetyValidationResult.invalid("Dangerous action type: " + actionType);
        }

        // Check for suspicious parameters
        for (Map.Entry<String, Object> entry : actionParameters.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.contains("rm -rf") || value.contains("format")) {
                    return SafetyValidationResult.invalid("Dangerous parameter detected: " + value);
                }
            }
        }

        return SafetyValidationResult.valid();
    }

    private void recordConstraintViolation(String agentId, String actionType, Map<String, Object> actionParameters,
            String userId, String reason) {
        try {
            violationLock.writeLock().lock();

            ConstraintViolation violation = new ConstraintViolation(agentId, actionType, actionParameters, userId,
                    reason, Instant.now());
            constraintViolations.put(violation.getId(), violation);

            totalConstraintViolations.incrementAndGet();
            logger.warn("Constraint violation recorded: {} for agent: {} - {}", actionType, agentId, reason);
        } finally {
            violationLock.writeLock().unlock();
        }
    }

    // Result classes
    public static class SafetyValidationResult {
        private final boolean valid;
        private final String reason;
        private final double confidence;

        private SafetyValidationResult(boolean valid, String reason, double confidence) {
            this.valid = valid;
            this.reason = reason;
            this.confidence = confidence;
        }

        public static SafetyValidationResult valid() {
            return new SafetyValidationResult(true, "Action is safe", 1.0);
        }

        public static SafetyValidationResult invalid(String reason) {
            return new SafetyValidationResult(false, reason, 0.0);
        }

        public static SafetyValidationResult disabled(String reason) {
            return new SafetyValidationResult(false, "Safety validation disabled: " + reason, 0.0);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class ConstraintResult {
        private final boolean success;
        private final String message;
        private final boolean added;
        private final int constraintCount;

        private ConstraintResult(boolean success, String message, boolean added, int constraintCount) {
            this.success = success;
            this.message = message;
            this.added = added;
            this.constraintCount = constraintCount;
        }

        public static ConstraintResult success(boolean added, int constraintCount) {
            return new ConstraintResult(true, "Constraint operation successful", added, constraintCount);
        }

        public static ConstraintResult notFound(String reason) {
            return new ConstraintResult(false, reason, false, 0);
        }

        public static ConstraintResult disabled(String reason) {
            return new ConstraintResult(false, "Constraint enforcement disabled: " + reason, false, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isAdded() {
            return added;
        }

        public int getConstraintCount() {
            return constraintCount;
        }
    }

    public static class PolicyResult {
        private final boolean success;
        private final String message;
        private final boolean added;
        private final int policyCount;

        private PolicyResult(boolean success, String message, boolean added, int policyCount) {
            this.success = success;
            this.message = message;
            this.added = added;
            this.policyCount = policyCount;
        }

        public static PolicyResult success(boolean added, int policyCount) {
            return new PolicyResult(true, "Policy operation successful", added, policyCount);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isAdded() {
            return added;
        }

        public int getPolicyCount() {
            return policyCount;
        }
    }

    public static class OverrideResult {
        private final boolean success;
        private final String message;
        private final ConstraintViolation violation;

        private OverrideResult(boolean success, String message, ConstraintViolation violation) {
            this.success = success;
            this.message = message;
            this.violation = violation;
        }

        public static OverrideResult success(ConstraintViolation violation) {
            return new OverrideResult(true, "Safety constraint overridden successfully", violation);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public ConstraintViolation getViolation() {
            return violation;
        }
    }

    public static class IncidentResult {
        private final boolean success;
        private final String message;
        private final SafetyIncident incident;

        private IncidentResult(boolean success, String message, SafetyIncident incident) {
            this.success = success;
            this.message = message;
            this.incident = incident;
        }

        public static IncidentResult success(SafetyIncident incident) {
            return new IncidentResult(true, "Safety incident reported successfully", incident);
        }

        public static IncidentResult disabled(String reason) {
            return new IncidentResult(false, "Incident reporting disabled: " + reason, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public SafetyIncident getIncident() {
            return incident;
        }
    }

    public static class SafetyPerformanceMetrics {
        private final long totalSafetyValidations;
        private final long totalConstraintViolations;
        private final long totalSafetyIncidents;
        private final long totalSafetyOverrides;
        private final int safetyPolicyCount;
        private final int userConstraintCount;
        private final int constraintViolationCount;
        private final int safetyIncidentCount;

        private SafetyPerformanceMetrics(Builder builder) {
            this.totalSafetyValidations = builder.totalSafetyValidations;
            this.totalConstraintViolations = builder.totalConstraintViolations;
            this.totalSafetyIncidents = builder.totalSafetyIncidents;
            this.totalSafetyOverrides = builder.totalSafetyOverrides;
            this.safetyPolicyCount = builder.safetyPolicyCount;
            this.userConstraintCount = builder.userConstraintCount;
            this.constraintViolationCount = builder.constraintViolationCount;
            this.safetyIncidentCount = builder.safetyIncidentCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public long getTotalSafetyValidations() {
            return totalSafetyValidations;
        }

        public long getTotalConstraintViolations() {
            return totalConstraintViolations;
        }

        public long getTotalSafetyIncidents() {
            return totalSafetyIncidents;
        }

        public long getTotalSafetyOverrides() {
            return totalSafetyOverrides;
        }

        public int getSafetyPolicyCount() {
            return safetyPolicyCount;
        }

        public int getUserConstraintCount() {
            return userConstraintCount;
        }

        public int getConstraintViolationCount() {
            return constraintViolationCount;
        }

        public int getSafetyIncidentCount() {
            return safetyIncidentCount;
        }

        public static class Builder {
            private long totalSafetyValidations;
            private long totalConstraintViolations;
            private long totalSafetyIncidents;
            private long totalSafetyOverrides;
            private int safetyPolicyCount;
            private int userConstraintCount;
            private int constraintViolationCount;
            private int safetyIncidentCount;

            public Builder totalSafetyValidations(long totalSafetyValidations) {
                this.totalSafetyValidations = totalSafetyValidations;
                return this;
            }

            public Builder totalConstraintViolations(long totalConstraintViolations) {
                this.totalConstraintViolations = totalConstraintViolations;
                return this;
            }

            public Builder totalSafetyIncidents(long totalSafetyIncidents) {
                this.totalSafetyIncidents = totalSafetyIncidents;
                return this;
            }

            public Builder totalSafetyOverrides(long totalSafetyOverrides) {
                this.totalSafetyOverrides = totalSafetyOverrides;
                return this;
            }

            public Builder safetyPolicyCount(int safetyPolicyCount) {
                this.safetyPolicyCount = safetyPolicyCount;
                return this;
            }

            public Builder userConstraintCount(int userConstraintCount) {
                this.userConstraintCount = userConstraintCount;
                return this;
            }

            public Builder constraintViolationCount(int constraintViolationCount) {
                this.constraintViolationCount = constraintViolationCount;
                return this;
            }

            public Builder safetyIncidentCount(int safetyIncidentCount) {
                this.safetyIncidentCount = safetyIncidentCount;
                return this;
            }

            public SafetyPerformanceMetrics build() {
                return new SafetyPerformanceMetrics(this);
            }
        }
    }

    // Internal data classes
    public static class SafetyPolicy {
        private final String agentId;
        private final Map<String, PolicyEntry> policies = new ConcurrentHashMap<>();

        public SafetyPolicy(String agentId) {
            this.agentId = agentId;
        }

        public SafetyValidationResult validateAction(String actionType, Map<String, Object> actionParameters) {
            // Check if any policy applies to this action
            for (PolicyEntry policy : policies.values()) {
                if (policy.appliesToAction(actionType, actionParameters)) {
                    if (!policy.isAllowed()) {
                        return SafetyValidationResult.invalid("Policy violation: " + policy.getDescription());
                    }
                }
            }
            return SafetyValidationResult.valid();
        }

        public boolean addPolicy(String policyType, Map<String, Object> policyParameters, String description) {
            PolicyEntry policy = new PolicyEntry(policyType, policyParameters, description);
            policies.put(policyType, policy);
            return true;
        }

        public int getPolicyCount() {
            return policies.size();
        }

        public String getAgentId() {
            return agentId;
        }

        public static class PolicyEntry {
            private final String policyType;
            private final Map<String, Object> parameters;
            private final String description;
            private final boolean allowed;

            public PolicyEntry(String policyType, Map<String, Object> parameters, String description) {
                this.policyType = policyType;
                this.parameters = parameters;
                this.description = description;
                this.allowed = !"deny".equals(policyType);
            }

            public boolean appliesToAction(String actionType, Map<String, Object> actionParameters) {
                // Simple policy matching - can be enhanced with more sophisticated logic
                return actionType.equals(parameters.get("actionType"));
            }

            public boolean isAllowed() {
                return allowed;
            }

            public String getPolicyType() {
                return policyType;
            }

            public Map<String, Object> getParameters() {
                return parameters;
            }

            public String getDescription() {
                return description;
            }
        }
    }

    public static class UserConstraint {
        private final String userId;
        private final Map<String, ConstraintEntry> constraints = new ConcurrentHashMap<>();

        public UserConstraint(String userId) {
            this.userId = userId;
        }

        public SafetyValidationResult validateAction(String actionType, Map<String, Object> actionParameters) {
            // Check if any constraint applies to this action
            for (ConstraintEntry constraint : constraints.values()) {
                if (constraint.appliesToAction(actionType, actionParameters)) {
                    return SafetyValidationResult.invalid("User constraint violation: " + constraint.getDescription());
                }
            }
            return SafetyValidationResult.valid();
        }

        public boolean addConstraint(String constraintType, Map<String, Object> constraintParameters,
                String description) {
            ConstraintEntry constraint = new ConstraintEntry(constraintType, constraintParameters, description);
            constraints.put(constraintType, constraint);
            return true;
        }

        public boolean removeConstraint(String constraintType) {
            return constraints.remove(constraintType) != null;
        }

        public int getConstraintCount() {
            return constraints.size();
        }

        public String getUserId() {
            return userId;
        }

        public static class ConstraintEntry {
            private final String constraintType;
            private final Map<String, Object> parameters;
            private final String description;

            public ConstraintEntry(String constraintType, Map<String, Object> parameters, String description) {
                this.constraintType = constraintType;
                this.parameters = parameters;
                this.description = description;
            }

            public boolean appliesToAction(String actionType, Map<String, Object> actionParameters) {
                // Simple constraint matching - can be enhanced with more sophisticated logic
                return actionType.equals(parameters.get("actionType"));
            }

            public String getConstraintType() {
                return constraintType;
            }

            public Map<String, Object> getParameters() {
                return parameters;
            }

            public String getDescription() {
                return description;
            }
        }
    }

    public static class ConstraintViolation {
        private final String id;
        private final String agentId;
        private final String actionType;
        private final Map<String, Object> actionParameters;
        private final String userId;
        private final String reason;
        private final Instant timestamp;

        public ConstraintViolation(String agentId, String actionType, Map<String, Object> actionParameters,
                String userId, String reason, Instant timestamp) {
            this.id = "violation-" + timestamp.toEpochMilli() + "-" + agentId;
            this.agentId = agentId;
            this.actionType = actionType;
            this.actionParameters = actionParameters;
            this.userId = userId;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getActionType() {
            return actionType;
        }

        public Map<String, Object> getActionParameters() {
            return actionParameters;
        }

        public String getUserId() {
            return userId;
        }

        public String getReason() {
            return reason;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class SafetyIncident {
        private final String id;
        private final String agentId;
        private final String incidentType;
        private final String description;
        private final Map<String, Object> incidentData;
        private final String reporterId;
        private final Instant timestamp;

        public SafetyIncident(String agentId, String incidentType, String description, Map<String, Object> incidentData,
                String reporterId, Instant timestamp) {
            this.id = "incident-" + timestamp.toEpochMilli() + "-" + agentId;
            this.agentId = agentId;
            this.incidentType = incidentType;
            this.description = description;
            this.incidentData = incidentData;
            this.reporterId = reporterId;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getIncidentType() {
            return incidentType;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getIncidentData() {
            return incidentData;
        }

        public String getReporterId() {
            return reporterId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
