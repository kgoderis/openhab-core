package org.openhab.core.ai.tool.resources.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;

/**
 * Cached configuration data for performance optimization.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CachedConfigurationData {
    private volatile @Nullable Configuration configuration;
    private volatile @Nullable String content;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

    public CachedConfigurationData(Configuration configuration) {
        this.configuration = configuration;
    }

    public @Nullable Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(@Nullable Configuration configuration) {
        this.configuration = configuration;
    }

    public @Nullable String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    public boolean needsRefresh() {
        return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
    }

    public void updateRefreshTime() {
        lastRefreshTime = System.currentTimeMillis();
    }
}
