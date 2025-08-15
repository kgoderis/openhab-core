package org.openhab.core.ai.tool.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Cached resource content with expiration control.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CachedResourceContent {
    private final Object content;
    private final long expirationTime;

    public CachedResourceContent(Object content, long expirationTime) {
        this.content = content;
        this.expirationTime = expirationTime;
    }

    public Object getContent() {
        return content;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
