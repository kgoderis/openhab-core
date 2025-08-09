package org.openhab.core.ai.tool.compliance.tests;

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

    // TODO: Implement compliance test logic
    // TODO: Add support for test dependencies
    // TODO: Implement test performance monitoring
    // TODO: Add support for test versioning
}
