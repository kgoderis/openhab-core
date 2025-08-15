package org.openhab.core.ai.agent.collaboration.context;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the shared context manager.
 *
 * Contains tunables for caching, retention and backup intervals.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextManagerConfiguration {

    private Duration cacheExpiry = Duration.ofMinutes(30);
    private Duration versionRetentionPeriod = Duration.ofDays(7);
    private int maxCacheSize = 1000;
    private int maxVersionsPerContext = 100;
    private boolean enableBackup = true;
    private Duration backupInterval = Duration.ofHours(1);

    public Duration getCacheExpiry() {
        return cacheExpiry;
    }

    public void setCacheExpiry(Duration cacheExpiry) {
        this.cacheExpiry = cacheExpiry;
    }

    public Duration getVersionRetentionPeriod() {
        return versionRetentionPeriod;
    }

    public void setVersionRetentionPeriod(Duration versionRetentionPeriod) {
        this.versionRetentionPeriod = versionRetentionPeriod;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public int getMaxVersionsPerContext() {
        return maxVersionsPerContext;
    }

    public void setMaxVersionsPerContext(int maxVersionsPerContext) {
        this.maxVersionsPerContext = maxVersionsPerContext;
    }

    public boolean isEnableBackup() {
        return enableBackup;
    }

    public void setEnableBackup(boolean enableBackup) {
        this.enableBackup = enableBackup;
    }

    public Duration getBackupInterval() {
        return backupInterval;
    }

    public void setBackupInterval(Duration backupInterval) {
        this.backupInterval = backupInterval;
    }
}
