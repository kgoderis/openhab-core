package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

/**
 * Event Enricher - Event enrichment interface
 * 
 * This interface provides event enrichment capabilities to add additional
 * context and metadata to events before processing.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventEnricher {

    /**
     * Enrich an event with additional context and metadata
     * 
     * @param event The original event
     * @return The enriched event, or null if no enrichment is needed
     */
    @Nullable
    Event enrich(Event event);
}
