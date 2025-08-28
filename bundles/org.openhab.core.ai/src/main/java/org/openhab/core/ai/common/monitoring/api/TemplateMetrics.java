package org.openhab.core.ai.common.monitoring.api;

/**
 * Capability interface for template-specific metrics.
 * 
 * This interface provides functionality for calculating template-related metrics
 * such as template usage, completion rates, and parameter validation statistics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
public interface TemplateMetrics {

    /**
     * Get the total number of templates available
     * 
     * @return Total template count
     */
    long totalTemplates();

    /**
     * Get the number of templates that have been used
     * 
     * @return Used template count
     */
    long usedTemplates();

    /**
     * Get the template usage rate (used / total)
     * 
     * @return Usage rate as a percentage (0.0 to 1.0)
     */
    default double templateUsageRate() {
        long total = totalTemplates();
        return total > 0 ? (double) usedTemplates() / total : 0.0;
    }

    /**
     * Get the number of parameter completions generated
     * 
     * @return Parameter completion count
     */
    long parameterCompletions();

    /**
     * Get the number of parameter validations performed
     * 
     * @return Parameter validation count
     */
    long parameterValidations();

    /**
     * Get the parameter validation success rate
     * 
     * @return Validation success rate as a percentage (0.0 to 1.0)
     */
    default double validationSuccessRate() {
        long validations = parameterValidations();
        return validations > 0 ? (double) parameterCompletions() / validations : 0.0;
    }
}
