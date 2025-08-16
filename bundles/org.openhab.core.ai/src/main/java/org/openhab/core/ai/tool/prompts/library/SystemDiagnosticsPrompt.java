package org.openhab.core.ai.tool.prompts.library;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.registry.PromptExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System Diagnostics Prompt Implementation for openHAB.
 * 
 * This class provides parameterized prompt templates with argument validation
 * for openHAB system diagnostics operations, as specified in section 16.2.14.2.4 of BRAIN_PLAN.md.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemDiagnosticsPrompt {

    private static final Logger logger = LoggerFactory.getLogger(SystemDiagnosticsPrompt.class);

    public static final String PROMPT_NAME = "system_diagnostics";
    public static final String PROMPT_DESCRIPTION = "Perform system diagnostics and health checks";

    // Performance monitoring
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);

    public SystemDiagnosticsPrompt() {
        // Constructor for system diagnostics prompt
    }

    /**
     * Execute the system diagnostics prompt with the given arguments.
     *
     * @param arguments the prompt arguments
     * @return the execution result
     */
    public PromptExecutionResult execute(Map<String, Object> arguments) {
        totalExecutions.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing system diagnostics prompt with arguments: {}", arguments);

            // Validate arguments
            String diagnosticType = validateAndGetString(arguments, "diagnosticType");
            String scope = getOptionalString(arguments, "scope");
            String includeDetails = getOptionalString(arguments, "includeDetails");

            // Validate diagnostic type
            if (!isValidDiagnosticType(diagnosticType)) {
                String errorMessage = "Invalid diagnostic type: " + diagnosticType
                        + ". Valid types are: SYSTEM_HEALTH, PERFORMANCE, MEMORY, NETWORK, STORAGE, SECURITY";
                logger.warn(errorMessage);
                failedExecutions.incrementAndGet();
                return new PromptExecutionResult(false, errorMessage, null);
            }

            // Execute the diagnostic
            String result = executeSystemDiagnostic(diagnosticType, scope, includeDetails);

            successfulExecutions.incrementAndGet();
            logger.debug("System diagnostics prompt executed successfully: {} {} {}", diagnosticType, scope,
                    includeDetails);

            return new PromptExecutionResult(true, null, result);

        } catch (Exception e) {
            String errorMessage = "Error executing system diagnostics prompt: " + e.getMessage();
            logger.error(errorMessage, e);
            failedExecutions.incrementAndGet();
            return new PromptExecutionResult(false, errorMessage, null);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            totalExecutionTimeMs.addAndGet(executionTime);
        }
    }

    private String validateAndGetString(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required argument missing: " + key);
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Argument must be a string: " + key);
        }
        return (String) value;
    }

    private @Nullable String getOptionalString(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        return value instanceof String ? (String) value : null;
    }

    private boolean isValidDiagnosticType(String diagnosticType) {
        return "SYSTEM_HEALTH".equals(diagnosticType) || "PERFORMANCE".equals(diagnosticType)
                || "MEMORY".equals(diagnosticType) || "NETWORK".equals(diagnosticType)
                || "STORAGE".equals(diagnosticType) || "SECURITY".equals(diagnosticType);
    }

    private String executeSystemDiagnostic(String diagnosticType, @Nullable String scope,
            @Nullable String includeDetails) {
        // TODO: Implement actual system diagnostic execution
        // This would typically involve collecting system metrics and health information
        // For now, return a simulation result

        String result = String.format("System diagnostic '%s' would be performed", diagnosticType);
        if (scope != null) {
            result += String.format(" with scope '%s'", scope);
        }
        if (includeDetails != null && "true".equalsIgnoreCase(includeDetails)) {
            result += " including detailed information";
        }

        logger.info("Simulated system diagnostic: {} {} {}", diagnosticType, scope, includeDetails);
        return result;
    }

    /**
     * Get performance metrics for this prompt.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalExecutions", totalExecutions.get());
        metrics.put("successfulExecutions", successfulExecutions.get());
        metrics.put("failedExecutions", failedExecutions.get());
        metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get());
        metrics.put("averageExecutionTimeMs",
                totalExecutions.get() > 0 ? totalExecutionTimeMs.get() / totalExecutions.get() : 0);
        metrics.put("successRate",
                totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0);
        return metrics;
    }

    /**
     * Get the prompt template with placeholders.
     *
     * @return the prompt template
     */
    public String getPromptTemplate() {
        return "Perform system diagnostic '{diagnosticType}'"
                + ". This will analyze the specified aspect of the system.";
    }

    /**
     * Get usage examples for this prompt.
     *
     * @return usage examples
     */
    public String[] getUsageExamples() {
        return new String[] { "System health check: diagnosticType='SYSTEM_HEALTH'",
                "Performance analysis: diagnosticType='PERFORMANCE', scope='FULL'",
                "Memory usage: diagnosticType='MEMORY', includeDetails='true'",
                "Network diagnostics: diagnosticType='NETWORK', scope='QUICK'",
                "Security audit: diagnosticType='SECURITY', includeDetails='true'" };
    }

    /**
     * Get the prompt argument schema.
     *
     * @return the argument schema
     */
    public Map<String, Object> getArgumentSchema() {
        Map<String, Object> schema = new HashMap<>();

        // diagnosticType argument
        Map<String, Object> diagnosticTypeSchema = new HashMap<>();
        diagnosticTypeSchema.put("type", "string");
        diagnosticTypeSchema.put("description",
                "Type of diagnostic to perform (SYSTEM_HEALTH, PERFORMANCE, MEMORY, NETWORK, STORAGE, SECURITY)");
        diagnosticTypeSchema.put("required", true);
        diagnosticTypeSchema.put("enum",
                new String[] { "SYSTEM_HEALTH", "PERFORMANCE", "MEMORY", "NETWORK", "STORAGE", "SECURITY" });
        schema.put("diagnosticType", diagnosticTypeSchema);

        // scope argument
        Map<String, Object> scopeSchema = new HashMap<>();
        scopeSchema.put("type", "string");
        scopeSchema.put("description", "Scope of the diagnostic (FULL, QUICK, TARGETED)");
        scopeSchema.put("required", false);
        scopeSchema.put("enum", new String[] { "FULL", "QUICK", "TARGETED" });
        schema.put("scope", scopeSchema);

        // includeDetails argument
        Map<String, Object> includeDetailsSchema = new HashMap<>();
        includeDetailsSchema.put("type", "string");
        includeDetailsSchema.put("description", "Include detailed diagnostic information");
        includeDetailsSchema.put("required", false);
        includeDetailsSchema.put("enum", new String[] { "true", "false" });
        schema.put("includeDetails", includeDetailsSchema);

        return schema;
    }

    // PromptExecutionResult unified to org.openhab.core.ai.tool.registry.PromptExecutionResult
}
