package org.openhab.core.ai.reasoning.constraints;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.auth.AuditLogger;
import org.openhab.core.ai.reasoning.policies.PolicyResult;
import org.openhab.core.ai.reasoning.policies.SafetyPolicy;
import org.openhab.core.ai.reasoning.policies.SafetyValidationResult;
import org.openhab.core.ai.reasoning.results.IncidentResult;
import org.openhab.core.ai.reasoning.results.OverrideResult;
import org.openhab.core.ai.reasoning.security.SafetyIncident;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Safety and Constraint Management System - Focuses on action safety validation and user-defined constraints.
 * 
 * This class provides safety management for AI actions, focusing on:
 * - Action safety validation and risk assessment
 * - User-defined constraint enforcement
 * - Safety policy management and violation detection
 * - Integration with openHAB audit logging system
 * 
 * Note: Authentication and authorization are handled by AgentModelSecurityManager
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = SafetyConstraintManager.class)
public class SafetyConstraintManager {

    private final Logger logger = LoggerFactory.getLogger(SafetyConstraintManager.class);

    // Auth system integration
    @Reference
    private AuditLogger auditLogger;

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
                    auditLogger.logSecurityViolation(userId, "SAFETY_POLICY_VIOLATION",
                            "Safety policy violation for action: " + actionType, "ai-safety", Instant.now());
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
                    auditLogger.logSecurityViolation(userId, "USER_CONSTRAINT_VIOLATION",
                            "User constraint violation for action: " + actionType, "ai-safety", Instant.now());
                    return constraintResult;
                }
            }

            // Check global safety rules
            SafetyValidationResult globalResult = validateGlobalSafetyRules(actionType, actionParameters);
            if (!globalResult.isValid()) {
                recordConstraintViolation(agentId, actionType, actionParameters, userId,
                        "Global safety rule violation: " + globalResult.getReason());
                auditLogger.logSecurityViolation(userId, "GLOBAL_SAFETY_VIOLATION",
                        "Global safety rule violation for action: " + actionType, "ai-safety", Instant.now());
                return globalResult;
            }

            logger.debug("Action safety validation passed for agent: {} action: {}", agentId, actionType);
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
        long totalValidations = totalSafetyValidations.get();
        long successfulValidations = totalValidations - totalConstraintViolations.get();
        long failedValidations = totalConstraintViolations.get();
        long totalTime = totalValidations * 10; // Estimate average time per validation
        double avgTime = totalValidations > 0 ? (double) totalTime / totalValidations : 0.0;

        return new SafetyPerformanceMetrics(totalValidations, successfulValidations, failedValidations, totalTime,
                avgTime);
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

    // Result and data classes extracted to top-level in org.openhab.core.ai.reasoning
}
