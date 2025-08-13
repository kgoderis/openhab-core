package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link SafetyPerformanceMetrics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPerformanceMetricsBuilder {
	private long totalSafetyValidations;
	private long totalConstraintViolations;
	private long totalSafetyIncidents;
	private long totalSafetyOverrides;
	private int safetyPolicyCount;
	private int userConstraintCount;
	private int constraintViolationCount;
	private int safetyIncidentCount;

	public SafetyPerformanceMetricsBuilder totalSafetyValidations(long v) { this.totalSafetyValidations = v; return this; }
	public SafetyPerformanceMetricsBuilder totalConstraintViolations(long v) { this.totalConstraintViolations = v; return this; }
	public SafetyPerformanceMetricsBuilder totalSafetyIncidents(long v) { this.totalSafetyIncidents = v; return this; }
	public SafetyPerformanceMetricsBuilder totalSafetyOverrides(long v) { this.totalSafetyOverrides = v; return this; }
	public SafetyPerformanceMetricsBuilder safetyPolicyCount(int v) { this.safetyPolicyCount = v; return this; }
	public SafetyPerformanceMetricsBuilder userConstraintCount(int v) { this.userConstraintCount = v; return this; }
	public SafetyPerformanceMetricsBuilder constraintViolationCount(int v) { this.constraintViolationCount = v; return this; }
	public SafetyPerformanceMetricsBuilder safetyIncidentCount(int v) { this.safetyIncidentCount = v; return this; }

	public SafetyPerformanceMetrics build() {
		return new SafetyPerformanceMetrics(totalSafetyValidations, totalConstraintViolations, totalSafetyIncidents,
			totalSafetyOverrides, safetyPolicyCount, userConstraintCount, constraintViolationCount, safetyIncidentCount);
	}
}
