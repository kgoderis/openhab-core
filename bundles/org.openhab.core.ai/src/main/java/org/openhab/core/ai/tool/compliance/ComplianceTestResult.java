package org.openhab.core.ai.tool.compliance;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of compliance test operations.
 * 
 * This class encapsulates the result of compliance test operations, including
 * success status, test details, and compliance information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ComplianceTestResult {

    private final String testId;
    private final String category;
    private final String description;
    private final boolean passed;
    private final String status;
    private final String message;
    private final List<String> failures;
    private final List<String> warnings;
    private final Map<String, Object> details;
    private final long timestamp;
    private final long durationMs;
    private String errorMessage;

    /**
     * Create a new compliance test result.
     * 
     * @param passed whether the test passed
     * @param status the test status
     * @param message the test message
     * @param failures list of test failures
     * @param warnings list of test warnings
     * @param details additional test details
     * @param timestamp the timestamp of the test
     */
    public ComplianceTestResult(boolean passed, String status, String message, List<String> failures,
            List<String> warnings, Map<String, Object> details, long timestamp) {
        this.testId = "";
        this.category = "";
        this.description = "";
        this.passed = passed;
        this.status = status;
        this.message = message;
        this.failures = failures;
        this.warnings = warnings;
        this.details = details;
        this.timestamp = timestamp;
        this.durationMs = 0L;
        this.errorMessage = "";
    }

    /**
     * Create a new compliance test result with full context expected by the validator.
     */
    public ComplianceTestResult(String testId, String category, String description, boolean passed, long durationMs,
            long timestamp) {
        this.testId = testId;
        this.category = category;
        this.description = description;
        this.passed = passed;
        this.status = passed ? "PASSED" : "FAILED";
        this.message = description;
        this.failures = List.of();
        this.warnings = List.of();
        this.details = Map.of();
        this.timestamp = timestamp;
        this.durationMs = durationMs;
        this.errorMessage = "";
    }

    /**
     * Create a passed compliance test result.
     * 
     * @param message the test message
     * @return passed compliance test result
     */
    public static ComplianceTestResult passed(String message) {
        return new ComplianceTestResult(true, "PASSED", message, List.of(), List.of(), Map.of(),
                System.currentTimeMillis());
    }

    /**
     * Create a failed compliance test result.
     * 
     * @param message the test message
     * @param failures list of test failures
     * @return failed compliance test result
     */
    public static ComplianceTestResult failed(String message, List<String> failures) {
        return new ComplianceTestResult(false, "FAILED", message, failures, List.of(), Map.of(),
                System.currentTimeMillis());
    }

    /**
     * Check if the test passed.
     * 
     * @return true if passed, false otherwise
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Get the test category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the test status.
     * 
     * @return the test status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the test message.
     * 
     * @return the test message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the list of test failures.
     * 
     * @return list of test failures
     */
    public List<String> getFailures() {
        return failures;
    }

    /**
     * Get the list of test warnings.
     * 
     * @return list of test warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Get additional test details.
     * 
     * @return test details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the timestamp of the test.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the test duration in milliseconds.
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Set an error message for the test.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get the error message if any.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // TODO: Implement compliance test result caching
    // TODO: Add support for compliance test result serialization
    // TODO: Implement compliance test result comparison
    // TODO: Add support for compliance test result metrics
}
