package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of validating an agent model context.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextValidationResult {
    private final List<String> issues = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();

    public void addIssue(String issue) { issues.add(issue); }
    public void addWarning(String warning) { warnings.add(warning); }
    public void addRecommendation(String recommendation) { recommendations.add(recommendation); }

    public List<String> getIssues() { return new ArrayList<>(issues); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
}


