package org.openhab.core.ai.tool.prompts.cache;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Cached prompt data for various entity types.
 * 
 * This class encapsulates cached data for prompts, including
 * entity ID, prompt type, and cached content for performance optimization.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CachedPromptData {
    final String entityId;
    final String promptType;
    volatile @Nullable String cachedContent;

    /**
     * Create a new cached prompt data instance.
     * 
     * @param entityId the entity ID (rule UID, item name, etc.)
     * @param promptType the prompt type
     */
    public CachedPromptData(String entityId, String promptType) {
        this.entityId = entityId;
        this.promptType = promptType;
    }

    /**
     * Get the entity ID.
     * 
     * @return the entity ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Get the prompt type.
     * 
     * @return the prompt type
     */
    public String getPromptType() {
        return promptType;
    }

    /**
     * Get the cached content.
     * 
     * @return the cached content, or null if not cached
     */
    public @Nullable String getCachedContent() {
        return cachedContent;
    }

    /**
     * Set the cached content.
     * 
     * @param cachedContent the content to cache
     */
    public void setCachedContent(@Nullable String cachedContent) {
        this.cachedContent = cachedContent;
    }
}
