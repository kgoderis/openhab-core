package org.openhab.core.ai.tool.compliance;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Comprehensive compliance validation report for MCP specification.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ComplianceValidationReport {

    private final List<ComplianceTestResult> testResults;
    private final Map<String, Double> categoryCompliance;
    private final double overallCompliancePercentage;
    private final long totalDurationMs;
    private final long timestamp;

    public ComplianceValidationReport(List<ComplianceTestResult> testResults, Map<String, Double> categoryCompliance,
            double overallCompliancePercentage, long totalDurationMs, long timestamp) {
        this.testResults = testResults;
        this.categoryCompliance = categoryCompliance;
        this.overallCompliancePercentage = overallCompliancePercentage;
        this.totalDurationMs = totalDurationMs;
        this.timestamp = timestamp;
    }

    /**
     * Get all test results.
     * 
     * @return list of test results
     */
    public List<ComplianceTestResult> getTestResults() {
        return testResults;
    }

    /**
     * Get compliance percentages by category.
     * 
     * @return map of category compliance percentages
     */
    public Map<String, Double> getCategoryCompliance() {
        return categoryCompliance;
    }

    /**
     * Get the overall compliance percentage.
     * 
     * @return overall compliance percentage (0.0 to 100.0)
     */
    public double getOverallCompliancePercentage() {
        return overallCompliancePercentage;
    }

    /**
     * Get the total validation duration in milliseconds.
     * 
     * @return total duration in milliseconds
     */
    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /**
     * Get the report timestamp.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the number of passed tests.
     * 
     * @return number of passed tests
     */
    public long getPassedTestCount() {
        return testResults.stream().filter(ComplianceTestResult::isPassed).count();
    }

    /**
     * Get the number of failed tests.
     * 
     * @return number of failed tests
     */
    public long getFailedTestCount() {
        return testResults.stream().filter(result -> !result.isPassed()).count();
    }

    /**
     * Get the total number of tests.
     * 
     * @return total number of tests
     */
    public int getTotalTestCount() {
        return testResults.size();
    }

    /**
     * Check if all tests passed.
     * 
     * @return true if all tests passed
     */
    public boolean isFullyCompliant() {
        return overallCompliancePercentage >= 100.0;
    }

    /**
     * Check if the implementation is mostly compliant (>= 90%).
     * 
     * @return true if mostly compliant
     */
    public boolean isMostlyCompliant() {
        return overallCompliancePercentage >= 90.0;
    }

    @Override
    public String toString() {
        return "ComplianceValidationReport{overallCompliance=" + String.format("%.1f%%", overallCompliancePercentage)
                + ", passedTests=" + getPassedTestCount() + ", failedTests=" + getFailedTestCount() + ", totalTests="
                + getTotalTestCount() + ", duration=" + totalDurationMs + "ms}";
    }
}
