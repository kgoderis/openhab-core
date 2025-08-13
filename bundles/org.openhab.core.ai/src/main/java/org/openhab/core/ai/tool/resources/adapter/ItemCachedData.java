package org.openhab.core.ai.tool.resources.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;

/**
 * Cached item payload with refresh tracking.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class ItemCachedData {
    private volatile @Nullable Item item;
    private volatile @Nullable String content;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

    ItemCachedData(Item item) {
        this.item = item;
    }

    @Nullable Item getItem() { return item; }
    void setItem(@Nullable Item item) { this.item = item; }

    @Nullable String getContent() { return content; }
    void setContent(@Nullable String content) { this.content = content; }

    boolean needsRefresh() { return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs; }
    void updateRefreshTime() { lastRefreshTime = System.currentTimeMillis(); }
}


