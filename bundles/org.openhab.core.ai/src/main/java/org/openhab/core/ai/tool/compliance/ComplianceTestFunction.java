package org.openhab.core.ai.tool.compliance;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted from ComplianceValidator.
 */
@NonNullByDefault
@FunctionalInterface
public interface ComplianceTestFunction {
    boolean run() throws Exception;
}
