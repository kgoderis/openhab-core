package org.openhab.core.ai.reasoning.config.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of configuration backup operation.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ConfigurationBackupResult {
    private final boolean success;
    private final String backupId;
    private final String error;

    public ConfigurationBackupResult(boolean success, String backupId, String error) {
        this.success = success;
        this.backupId = backupId;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getBackupId() {
        return backupId;
    }

    public String getError() {
        return error;
    }
}
