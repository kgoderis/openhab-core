package org.openhab.core.ai.tool.resources.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Rule;

/**
 * Cached rule payload with refresh tracking.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class RuleCachedData {
    private volatile @Nullable Rule rule;
    private volatile @Nullable String content;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

    RuleCachedData(Rule rule) { this.rule = rule; }

    @Nullable Rule getRule() { return rule; }
    void setRule(@Nullable Rule rule) { this.rule = rule; }

    @Nullable String getContent() { return content; }
    void setContent(@Nullable String content) { this.content = content; }

    boolean needsRefresh() { return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs; }
    void updateRefreshTime() { lastRefreshTime = System.currentTimeMillis(); }
}


