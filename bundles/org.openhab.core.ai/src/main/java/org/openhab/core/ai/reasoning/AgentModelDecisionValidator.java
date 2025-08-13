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
package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Decision Validator for decision validation and safety checks.
 * 
 * This class provides comprehensive decision validation capabilities,
 * including safety checks, risk assessment, and compliance validation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelDecisionValidator.class)
@NonNullByDefault
public class AgentModelDecisionValidator {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelDecisionValidator.class);

    // Safety thresholds
    private static final double MIN_CONFIDENCE_THRESHOLD = 0.7;
    private static final double HIGH_RISK_CONFIDENCE_THRESHOLD = 0.9;
    private static final double MAX_RISK_SCORE = 0.8;

    private final Map<String, ValidationRule> validationRules = new ConcurrentHashMap<>();

    /**
     * Validate a decision for safety and compliance.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @return Validation result with safety issues and recommendations
     */
    public DecisionValidationResult validate(DecisionResult decision, DecisionContext context) {
        logger.debug("Validating decision: {}", decision.getDecisionId());

        DecisionValidationResult result = new DecisionValidationResult();

        // Check confidence level
        validateConfidence(decision, context, result);

        // Check risk assessment
        validateRiskAssessment(decision, context, result);

        // Check safety constraints
        validateSafetyConstraints(decision, context, result);

        // Check compliance
        validateCompliance(decision, context, result);

        // Check reasoning quality
        validateReasoningQuality(decision, context, result);

        // Apply custom validation rules
        applyCustomValidationRules(decision, context, result);

        logger.debug("Decision validation completed with {} issues", result.getValidationIssues().size());
        return result;
    }

    /**
     * Assess the risk level of a decision.
     * 
     * @param decision The decision to assess
     * @param context The decision context
     * @return Risk assessment result
     */
    public RiskAssessmentResult assessRisk(DecisionResult decision, DecisionContext context) {
        logger.debug("Assessing risk for decision: {}", decision.getDecisionId());

        RiskAssessmentResult result = new RiskAssessmentResult();

        // Calculate risk score
        double riskScore = calculateRiskScore(decision, context);
        result.setRiskScore(riskScore);

        // Determine risk level
        RiskLevel riskLevel = determineRiskLevel(riskScore, context.getRiskLevel());
        result.setRiskLevel(riskLevel);

        // Identify risk factors
        List<String> riskFactors = identifyRiskFactors(decision, context);
        result.setRiskFactors(riskFactors);

        // Generate risk mitigation recommendations
        List<String> mitigationStrategies = generateMitigationStrategies(decision, context, riskLevel);
        result.setMitigationStrategies(mitigationStrategies);

        logger.debug("Risk assessment completed: score={}, level={}", riskScore, riskLevel);
        return result;
    }

    /**
     * Check if a decision is safe to execute.
     * 
     * @param decision The decision to check
     * @param context The decision context
     * @return True if the decision is safe, false otherwise
     */
    public boolean isSafeToExecute(DecisionResult decision, DecisionContext context) {
        DecisionValidationResult validationResult = validate(decision, context);
        RiskAssessmentResult riskResult = assessRisk(decision, context);

        // Decision is safe if no critical validation issues and risk is acceptable
        boolean noCriticalIssues = validationResult.getValidationIssues().stream()
                .noneMatch(issue -> issue.toLowerCase().contains("critical") || issue.toLowerCase().contains("safety"));

        boolean acceptableRisk = riskResult.getRiskLevel() != RiskLevel.CRITICAL
                && riskResult.getRiskLevel() != RiskLevel.HIGH;

        boolean sufficientConfidence = decision.getConfidence() >= MIN_CONFIDENCE_THRESHOLD;

        return noCriticalIssues && acceptableRisk && sufficientConfidence;
    }

    /**
     * Add a custom validation rule.
     * 
     * @param ruleName The name of the rule
     * @param rule The validation rule
     */
    public void addValidationRule(String ruleName, ValidationRule rule) {
        validationRules.put(ruleName, rule);
        logger.debug("Added validation rule: {}", ruleName);
    }

    /**
     * Validate confidence level.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void validateConfidence(DecisionResult decision, DecisionContext context, DecisionValidationResult result) {
        double confidence = decision.getConfidence();

        if (confidence < MIN_CONFIDENCE_THRESHOLD) {
            result.addValidationIssue(
                    "Decision confidence too low: " + confidence + " (min: " + MIN_CONFIDENCE_THRESHOLD + ")");
        }

        if (confidence < 0.5) {
            result.addValidationIssue("Critical: Very low decision confidence: " + confidence);
        }

        // For high-risk decisions, require higher confidence
        if (context.getRiskLevel() == RiskLevel.HIGH
                && confidence < HIGH_RISK_CONFIDENCE_THRESHOLD) {
            result.addValidationIssue("High-risk decision requires higher confidence: " + confidence + " (min: "
                    + HIGH_RISK_CONFIDENCE_THRESHOLD + ")");
        }

        if (confidence > 0.99) {
            result.addValidationWarning(
                    "Unusually high confidence level: " + confidence + " - may indicate overconfidence");
        }
    }

    /**
     * Validate risk assessment.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void validateRiskAssessment(DecisionResult decision, DecisionContext context,
            DecisionValidationResult result) {
        RiskAssessmentResult riskResult = assessRisk(decision, context);

        if (riskResult.getRiskScore() > MAX_RISK_SCORE) {
            result.addValidationIssue(
                    "Risk score too high: " + riskResult.getRiskScore() + " (max: " + MAX_RISK_SCORE + ")");
        }

        if (riskResult.getRiskLevel() == RiskLevel.CRITICAL) {
            result.addValidationIssue("Critical: Decision has critical risk level");
        }

        if (riskResult.getRiskLevel() == RiskLevel.HIGH) {
            result.addValidationWarning("High-risk decision detected - requires careful review");
        }

        if (!riskResult.getRiskFactors().isEmpty()) {
            result.addValidationWarning("Risk factors identified: " + String.join(", ", riskResult.getRiskFactors()));
        }
    }

    /**
     * Validate safety constraints.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void validateSafetyConstraints(DecisionResult decision, DecisionContext context,
            DecisionValidationResult result) {
        String decisionText = decision.getDecision().toLowerCase();

        // Check for potentially dangerous actions
        if (decisionText.contains("delete") || decisionText.contains("remove")) {
            result.addValidationWarning("Decision involves deletion/removal - verify safety");
        }

        if (decisionText.contains("shutdown") || decisionText.contains("stop")) {
            result.addValidationWarning("Decision involves system shutdown - verify necessity");
        }

        if (decisionText.contains("override") || decisionText.contains("bypass")) {
            result.addValidationIssue("Decision involves override/bypass - potential safety concern");
        }

        // Check for financial implications
        if (decisionText.contains("cost") || decisionText.contains("money") || decisionText.contains("payment")) {
            result.addValidationWarning("Decision has financial implications - verify authorization");
        }

        // Check for security implications
        if (decisionText.contains("security") || decisionText.contains("access")
                || decisionText.contains("permission")) {
            result.addValidationWarning("Decision has security implications - verify permissions");
        }
    }

    /**
     * Validate compliance.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void validateCompliance(DecisionResult decision, DecisionContext context,
            DecisionValidationResult result) {
        // Check for regulatory compliance
        String agentType = context.getAgentType();
        String domain = context.getDomain();

        if ("energy".equals(agentType) && domain.contains("energy")) {
            // Energy-specific compliance checks
            if (decision.getDecision().toLowerCase().contains("peak")) {
                result.addValidationWarning("Energy peak management decision - verify compliance with regulations");
            }
        }

        if ("security".equals(agentType) && domain.contains("security")) {
            // Security-specific compliance checks
            if (decision.getDecision().toLowerCase().contains("access")) {
                result.addValidationWarning("Security access decision - verify compliance with access policies");
            }
        }

        // Check for audit trail
        if (decision.getReasoning() == null || decision.getReasoning().trim().isEmpty()) {
            result.addValidationIssue("Missing reasoning - required for audit compliance");
        }
    }

    /**
     * Validate reasoning quality.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void validateReasoningQuality(DecisionResult decision, DecisionContext context,
            DecisionValidationResult result) {
        String reasoning = decision.getReasoning();

        if (reasoning == null || reasoning.trim().isEmpty()) {
            result.addValidationIssue("Missing reasoning explanation");
            return;
        }

        // Check reasoning length
        if (reasoning.length() < 10) {
            result.addValidationIssue("Reasoning too brief - insufficient explanation");
        }

        if (reasoning.length() > 1000) {
            result.addValidationWarning("Reasoning very long - may indicate overthinking");
        }

        // Check for logical structure
        if (!reasoning.contains("because") && !reasoning.contains("since") && !reasoning.contains("therefore")) {
            result.addValidationWarning("Reasoning lacks clear logical structure");
        }

        // Check for consideration of alternatives
        if (!reasoning.toLowerCase().contains("alternative") && !reasoning.toLowerCase().contains("option")) {
            result.addValidationWarning("Reasoning may not consider alternatives");
        }
    }

    /**
     * Apply custom validation rules.
     * 
     * @param decision The decision to validate
     * @param context The decision context
     * @param result The validation result to update
     */
    private void applyCustomValidationRules(DecisionResult decision, DecisionContext context,
            DecisionValidationResult result) {
        for (Map.Entry<String, ValidationRule> entry : validationRules.entrySet()) {
            try {
                ValidationRule rule = entry.getValue();
                List<String> ruleIssues = rule.validate(decision, context);
                for (String issue : ruleIssues) {
                    result.addValidationIssue("Rule '" + entry.getKey() + "': " + issue);
                }
            } catch (Exception e) {
                logger.error("Error applying validation rule: {}", entry.getKey(), e);
                result.addValidationIssue("Error applying validation rule: " + entry.getKey());
            }
        }
    }

    /**
     * Calculate risk score for a decision.
     * 
     * @param decision The decision to assess
     * @param context The decision context
     * @return Risk score between 0.0 and 1.0
     */
    private double calculateRiskScore(DecisionResult decision, DecisionContext context) {
        double riskScore = 0.0;

        // Base risk from context
        switch (context.getRiskLevel()) {
            case LOW:
                riskScore += 0.1;
                break;
            case MEDIUM:
                riskScore += 0.3;
                break;
            case HIGH:
                riskScore += 0.6;
                break;
            case CRITICAL:
                riskScore += 0.8;
                break;
        }

        // Risk from confidence (inverse relationship)
        double confidenceRisk = 1.0 - decision.getConfidence();
        riskScore += confidenceRisk * 0.3;

        // Risk from decision content
        String decisionText = decision.getDecision().toLowerCase();
        if (decisionText.contains("delete") || decisionText.contains("remove")) {
            riskScore += 0.2;
        }
        if (decisionText.contains("override") || decisionText.contains("bypass")) {
            riskScore += 0.3;
        }
        if (decisionText.contains("shutdown") || decisionText.contains("stop")) {
            riskScore += 0.2;
        }

        // Risk from agent type
        if ("security".equals(context.getAgentType())) {
            riskScore += 0.1; // Security decisions have higher inherent risk
        }

        return Math.min(riskScore, 1.0);
    }

    /**
     * Determine risk level based on score and context.
     * 
     * @param riskScore The calculated risk score
     * @param contextRiskLevel The context risk level
     * @return The determined risk level
     */
    private RiskLevel determineRiskLevel(double riskScore, RiskLevel contextRiskLevel) {
        if (riskScore >= 0.8 || contextRiskLevel == RiskLevel.CRITICAL) {
            return RiskLevel.CRITICAL;
        } else if (riskScore >= 0.6 || contextRiskLevel == RiskLevel.HIGH) {
            return RiskLevel.HIGH;
        } else if (riskScore >= 0.4 || contextRiskLevel == RiskLevel.MEDIUM) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * Identify risk factors for a decision.
     * 
     * @param decision The decision to analyze
     * @param context The decision context
     * @return List of identified risk factors
     */
    private List<String> identifyRiskFactors(DecisionResult decision, DecisionContext context) {
        List<String> riskFactors = new ArrayList<>();

        // Low confidence
        if (decision.getConfidence() < MIN_CONFIDENCE_THRESHOLD) {
            riskFactors.add("Low confidence level");
        }

        // High context risk
        if (context.getRiskLevel() == RiskLevel.HIGH || context.getRiskLevel() == RiskLevel.CRITICAL) {
            riskFactors.add("High context risk level");
        }

        // Decision content risks
        String decisionText = decision.getDecision().toLowerCase();
        if (decisionText.contains("delete") || decisionText.contains("remove")) {
            riskFactors.add("Destructive action");
        }
        if (decisionText.contains("override") || decisionText.contains("bypass")) {
            riskFactors.add("System override");
        }
        if (decisionText.contains("shutdown") || decisionText.contains("stop")) {
            riskFactors.add("System shutdown");
        }

        // Agent type risks
        if ("security".equals(context.getAgentType())) {
            riskFactors.add("Security-sensitive decision");
        }

        return riskFactors;
    }

    /**
     * Generate risk mitigation strategies.
     * 
     * @param decision The decision to analyze
     * @param context The decision context
     * @param riskLevel The determined risk level
     * @return List of mitigation strategies
     */
    private List<String> generateMitigationStrategies(DecisionResult decision, DecisionContext context,
            RiskLevel riskLevel) {
        List<String> strategies = new ArrayList<>();

        switch (riskLevel) {
            case CRITICAL:
                strategies.add("Require manual approval before execution");
                strategies.add("Implement additional safety checks");
                strategies.add("Create detailed audit trail");
                strategies.add("Consider alternative approaches");
                break;
            case HIGH:
                strategies.add("Review decision with supervisor");
                strategies.add("Implement safety safeguards");
                strategies.add("Monitor execution closely");
                break;
            case MEDIUM:
                strategies.add("Verify decision parameters");
                strategies.add("Implement basic safety checks");
                break;
            case LOW:
                strategies.add("Standard monitoring");
                break;
        }

        // Specific strategies based on decision content
        String decisionText = decision.getDecision().toLowerCase();
        if (decisionText.contains("delete") || decisionText.contains("remove")) {
            strategies.add("Create backup before execution");
            strategies.add("Verify deletion scope");
        }

        if (decisionText.contains("override") || decisionText.contains("bypass")) {
            strategies.add("Verify override authorization");
            strategies.add("Document override reason");
        }

        return strategies;
    }

    /**
     * Decision Validation Result class.
     */
    // DecisionValidationResult extracted to org.openhab.core.ai.reasoning.DecisionValidationResult

    /**
     * Risk Assessment Result class.
     */
    // RiskAssessmentResult extracted to org.openhab.core.ai.reasoning.RiskAssessmentResult

    /**
     * Risk Level enum.
     */
    // RiskLevel extracted to org.openhab.core.ai.reasoning.RiskLevel

    /**
     * Validation Rule interface.
     */
    @FunctionalInterface
    public interface ValidationRule {
        List<String> validate(DecisionResult decision, DecisionContext context);
    }
}
