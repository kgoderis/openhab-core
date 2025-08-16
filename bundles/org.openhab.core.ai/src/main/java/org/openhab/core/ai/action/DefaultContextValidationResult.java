package org.openhab.core.ai.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default implementation of ContextValidationResult.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultContextValidationResult implements ContextValidationResult {
    private boolean valid = true;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> issues = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();
    private String message = "";

    public DefaultContextValidationResult() {
    }

    public DefaultContextValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public void addIssue(String issue) {
        issues.add(issue);
        valid = false;
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    @Override
    public boolean isValid() {
        return valid && errors.isEmpty();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    @Override
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<String> getIssues() {
        return new ArrayList<>(issues);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }
}
