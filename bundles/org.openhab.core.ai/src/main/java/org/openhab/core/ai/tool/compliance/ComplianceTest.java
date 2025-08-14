package org.openhab.core.ai.tool.compliance;

import java.util.List;
import java.util.Map;

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
    // Nested AbstractComplianceTest extracted to top-level class org.openhab.core.ai.tool.compliance.AbstractComplianceTest
}
