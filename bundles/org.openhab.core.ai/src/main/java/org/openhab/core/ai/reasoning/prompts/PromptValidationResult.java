package org.openhab.core.ai.reasoning.prompts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class PromptValidationResult {
    private final List<String> issues = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();

    public void addIssue(String issue) {
        issues.add(issue);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public List<String> getIssues() {
        return new ArrayList<>(issues);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public boolean isValid() {
        return issues.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    public int getIssueCount() {
        return issues.size();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public int getRecommendationCount() {
        return recommendations.size();
    }
}
