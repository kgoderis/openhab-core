package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Quality validation result for a model prompt.
 *
 * <p>Contains quality issues, warnings, and recommendations.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptQualityResult {
    private final List<String> qualityIssues = new ArrayList<>();
    private final List<String> qualityWarnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();

    public void addQualityIssue(String issue) {
        qualityIssues.add(issue);
    }

    public void addQualityWarning(String warning) {
        qualityWarnings.add(warning);
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public List<String> getQualityIssues() {
        return new ArrayList<>(qualityIssues);
    }

    public List<String> getQualityWarnings() {
        return new ArrayList<>(qualityWarnings);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public boolean meetsStandards() {
        return qualityIssues.isEmpty();
    }

    public boolean hasWarnings() {
        return !qualityWarnings.isEmpty();
    }

    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    public int getIssueCount() {
        return qualityIssues.size();
    }

    public int getWarningCount() {
        return qualityWarnings.size();
    }

    public int getRecommendationCount() {
        return recommendations.size();
    }
}


