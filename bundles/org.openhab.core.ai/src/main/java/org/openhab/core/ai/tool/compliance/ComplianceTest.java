package org.openhab.core.ai.tool.compliance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Test for MCP compliance validation.
 * 
 * This interface defines the contract for compliance tests that can validate
 * whether tool implementations meet MCP specification requirements.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ComplianceTest {

    /**
     * Get the test ID.
     * 
     * @return the test ID
     */
    String getTestId();

    /**
     * Get the test name.
     * 
     * @return the test name
     */
    String getTestName();

    /**
     * Get the test description.
     * 
     * @return the test description
     */
    String getTestDescription();

    /**
     * Get the test category.
     * 
     * @return the test category
     */
    String getCategory();

    /**
     * Get the test priority.
     * 
     * @return the test priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the test is enabled.
     * 
     * @return true if the test is enabled
     */
    boolean isEnabled();

    /**
     * Run the compliance test.
     * 
     * @return test result
     */
    ComplianceTestResult runTest();

    /**
     * Run the test with the given parameters.
     * 
     * @param parameters the test parameters
     * @return test result
     */
    ComplianceTestResult runTest(Map<String, Object> parameters);

    /**
     * Get the test configuration.
     * 
     * @return the test configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the test configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Get test dependencies.
     * 
     * @return list of test IDs that this test depends on
     */
    List<String> getDependencies();

    /**
     * Check if all dependencies are satisfied.
     * 
     * @param completedTests list of completed test IDs
     * @return true if all dependencies are satisfied
     */
    boolean areDependenciesSatisfied(List<String> completedTests);

    /**
     * Get test performance metrics.
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Get test version.
     * 
     * @return test version
     */
    String getVersion();

    /**
     * Check if test version is compatible.
     * 
     * @param requiredVersion required version
     * @return true if compatible
     */
    boolean isVersionCompatible(String requiredVersion);

    /**
     * Abstract base implementation of ComplianceTest.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    abstract class AbstractComplianceTest implements ComplianceTest {

        protected final String testId;
        protected final String testName;
        protected final String testDescription;
        protected final String category;
        protected final int priority;
        protected final String version;
        protected final List<String> dependencies;
        protected final Map<String, Object> configuration;
        protected final AtomicLong executionCount = new AtomicLong(0);
        protected final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
        protected final AtomicLong lastExecutionTimeMs = new AtomicLong(0);
        protected final AtomicLong successCount = new AtomicLong(0);
        protected final AtomicLong failureCount = new AtomicLong(0);
        protected boolean enabled = true;

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
            executionCount.incrementAndGet();

            try {
                // Check dependencies before execution
                if (!areDependenciesSatisfied(new ArrayList<>())) {
                    return ComplianceTestResult.failed("Dependencies not satisfied",
                            List.of("Dependencies not satisfied"));
                }

                // Execute the actual test logic
                ComplianceTestResult result = executeTest(parameters);
                long executionTime = System.currentTimeMillis() - startTime;
                lastExecutionTimeMs.set(executionTime);
                totalExecutionTimeMs.addAndGet(executionTime);

                if (result.isPassed()) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

                return result;
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                lastExecutionTimeMs.set(executionTime);
                totalExecutionTimeMs.addAndGet(executionTime);
                failureCount.incrementAndGet();
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
            long totalExecutions = executionCount.get();

            metrics.put("executionCount", totalExecutions);
            metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get());
            metrics.put("lastExecutionTimeMs", lastExecutionTimeMs.get());
            metrics.put("successCount", successCount.get());
            metrics.put("failureCount", failureCount.get());

            if (totalExecutions > 0) {
                metrics.put("averageExecutionTimeMs", totalExecutionTimeMs.get() / totalExecutions);
                metrics.put("successRate", (double) successCount.get() / totalExecutions);
            } else {
                metrics.put("averageExecutionTimeMs", 0L);
                metrics.put("successRate", 0.0);
            }

            return metrics;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public boolean isVersionCompatible(String requiredVersion) {
            // Simple version compatibility check
            // In a real implementation, you would use proper semantic versioning
            return version.equals(requiredVersion) || version.startsWith(requiredVersion);
        }

        /**
         * Add a dependency to this test.
         * 
         * @param dependencyTestId the test ID this test depends on
         */
        public void addDependency(String dependencyTestId) {
            if (!dependencies.contains(dependencyTestId)) {
                dependencies.add(dependencyTestId);
            }
        }

        /**
         * Remove a dependency from this test.
         * 
         * @param dependencyTestId the test ID to remove as dependency
         */
        public void removeDependency(String dependencyTestId) {
            dependencies.remove(dependencyTestId);
        }

        /**
         * Set the enabled state of this test.
         * 
         * @param enabled whether the test is enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Execute the actual test logic.
         * 
         * @param parameters the test parameters
         * @return test result
         */
        protected abstract ComplianceTestResult executeTest(Map<String, Object> parameters);
    }
}
