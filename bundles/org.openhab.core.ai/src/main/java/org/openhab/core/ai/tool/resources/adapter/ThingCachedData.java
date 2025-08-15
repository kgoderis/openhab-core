package org.openhab.core.ai.tool.resources.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

/**
 * Cached thing payload with refresh tracking.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class ThingCachedData {
    private volatile @Nullable Thing thing;
    private volatile @Nullable String content;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

    ThingCachedData(Thing thing) {
        this.thing = thing;
    }

    @Nullable
    Thing getThing() {
        return thing;
    }

    void setThing(@Nullable Thing thing) {
        this.thing = thing;
    }

    @Nullable
    String getContent() {
        return content;
    }

    void setContent(@Nullable String content) {
        this.content = content;
    }

    boolean needsRefresh() {
        return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
    }

    void updateRefreshTime() {
        lastRefreshTime = System.currentTimeMillis();
    }
}
