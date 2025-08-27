package org.openhab.core.ai.tool.compliance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Abstract base implementation of {@link ComplianceTest}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractComplianceTest implements ComplianceTest {

    protected final String testId;
    protected final String testName;
    protected final String testDescription;
    protected final String category;
    protected final int priority;
    protected final String version;
    protected final List<String> dependencies;
    protected final Map<String, Object> configuration;
    // Performance monitoring - migrated to MetricsService
    // protected final AtomicLong executionCount = new AtomicLong(0);
    // protected final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
    // protected final AtomicLong lastExecutionTimeMs = new AtomicLong(0);
    // protected final AtomicLong successCount = new AtomicLong(0);
    // protected final AtomicLong failureCount = new AtomicLong(0);
    protected boolean enabled = true;

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    protected AbstractComplianceTest(String testId, String testName, String testDescription, String category,
            int priority, String version) {
        this.testId = testId;
        this.testName = testName;
        this.testDescription = testDescription;
        this.category = category;
        this.priority = priority;
        this.version = version;
        this.dependencies = new ArrayList<>();
        this.configuration = new HashMap<>();
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public String getTestName() {
        return testName;
    }

    @Override
    public String getTestDescription() {
        return testDescription;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ComplianceTestResult runTest() {
        return runTest(new HashMap<>());
    }

    @Override
    public ComplianceTestResult runTest(Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        // executionCount.incrementAndGet(); // Removed
        try {
            if (!areDependenciesSatisfied(new ArrayList<>())) {
                return ComplianceTestResult.failed("Dependencies not satisfied", List.of("Dependencies not satisfied"));
            }
            ComplianceTestResult result = executeTest(parameters);
            long executionTime = System.currentTimeMillis() - startTime;
            // lastExecutionTimeMs.set(executionTime); // Removed
            // totalExecutionTimeMs.addAndGet(executionTime); // Removed
            if (result.isPassed()) {
                // successCount.incrementAndGet(); // Removed
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("compliance_test", "success", true, Duration.ofMillis(executionTime));
                    } catch (Exception e) {
                        // Fallback to local logging if MetricsService fails
                        System.err.println("Failed to record compliance test success metrics: " + e.getMessage());
                    }
                }
            } else {
                // failureCount.incrementAndGet(); // Removed
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("compliance_test", "failure", false, Duration.ofMillis(executionTime));
                    } catch (Exception e) {
                        // Fallback to local logging if MetricsService fails
                        System.err.println("Failed to record compliance test failure metrics: " + e.getMessage());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // lastExecutionTimeMs.set(executionTime); // Removed
            // totalExecutionTimeMs.addAndGet(executionTime); // Removed
            // failureCount.incrementAndGet(); // Removed
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("compliance_test", "failure", false, Duration.ofMillis(executionTime));
                } catch (Exception e2) {
                    // Fallback to local logging if MetricsService fails
                    System.err.println("Failed to record compliance test failure metrics: " + e2.getMessage());
                }
            }
            return ComplianceTestResult.failed("Test execution failed: " + e.getMessage(), List.of(e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    @Override
    public boolean areDependenciesSatisfied(List<String> completedTests) {
        return completedTests.containsAll(dependencies);
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        // long totalExecutions = executionCount.get(); // Removed
        // metrics.put("executionCount", totalExecutions); // Removed
        // metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get()); // Removed
        // metrics.put("lastExecutionTimeMs", lastExecutionTimeMs.get()); // Removed
        // metrics.put("successCount", successCount.get()); // Removed
        // metrics.put("failureCount", failureCount.get()); // Removed
        // if (totalExecutions > 0) { // Removed
        //     metrics.put("averageExecutionTimeMs", totalExecutionTimeMs.get() / totalExecutions); // Removed
        //     metrics.put("successRate", (double) successCount.get() / totalExecutions); // Removed
        // } else { // Removed
        //     metrics.put("averageExecutionTimeMs", 0L); // Removed
        //     metrics.put("successRate", 0.0); // Removed
        // } // Removed
        return metrics;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isVersionCompatible(String requiredVersion) {
        return version.equals(requiredVersion) || version.startsWith(requiredVersion);
    }

    public void addDependency(String dependencyTestId) {
        if (!dependencies.contains(dependencyTestId)) {
            dependencies.add(dependencyTestId);
        }
    }

    public void removeDependency(String dependencyTestId) {
        dependencies.remove(dependencyTestId);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected abstract ComplianceTestResult executeTest(Map<String, Object> parameters);
}
