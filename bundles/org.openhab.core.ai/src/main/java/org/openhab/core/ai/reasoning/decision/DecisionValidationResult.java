package org.openhab.core.ai.reasoning.decision;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validation result for a model decision.
 *
 * <p>
 * Contains validation issues, warnings, and recommendations.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DecisionValidationResult {
    private final List<String> validationIssues = new ArrayList<>();
    private final List<String> validationWarnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();

    public void addValidationIssue(String issue) {
        validationIssues.add(issue);
    }

    public void addValidationWarning(String warning) {
        validationWarnings.add(warning);
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public List<String> getValidationIssues() {
        return new ArrayList<>(validationIssues);
    }

    public List<String> getValidationWarnings() {
        return new ArrayList<>(validationWarnings);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public boolean isValid() {
        return validationIssues.isEmpty();
    }

    public boolean hasWarnings() {
        return !validationWarnings.isEmpty();
    }

    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    public int getIssueCount() {
        return validationIssues.size();
    }

    public int getWarningCount() {
        return validationWarnings.size();
    }

    public int getRecommendationCount() {
        return recommendations.size();
    }
}
