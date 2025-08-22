package org.openhab.core.ai.reasoning.engine.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Types of reasoning steps in the reasoning process.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ReasoningStepType {
    /**
     * Initial analysis of the problem or situation
     */
    ANALYSIS,

    /**
     * Planning and strategy development
     */
    PLANNING,

    /**
     * Execution of planned actions or tool calls
     */
    EXECUTION,

    /**
     * Evaluation of results and outcomes
     */
    EVALUATION,

    /**
     * Backtracking to previous steps when needed
     */
    BACKTRACKING,

    /**
     * Synthesis and conclusion formation
     */
    SYNTHESIS,

    /**
     * Validation and verification of results
     */
    VALIDATION,

    /**
     * Decision making step
     */
    DECISION,

    /**
     * Information gathering step
     */
    INFORMATION_GATHERING,

    /**
     * Problem decomposition step
     */
    PROBLEM_DECOMPOSITION,

    /**
     * Solution integration step
     */
    SOLUTION_INTEGRATION,

    /**
     * Unknown or unspecified step type
     */
    UNKNOWN
}
