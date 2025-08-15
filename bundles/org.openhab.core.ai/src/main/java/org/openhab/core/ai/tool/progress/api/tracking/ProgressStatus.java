package org.openhab.core.ai.tool.progress.api.tracking;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Progress status enumeration for MCP Utilities.
 * 
 * Defines the different statuses that a progress operation can have.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public enum ProgressStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED,
    PAUSED
}
