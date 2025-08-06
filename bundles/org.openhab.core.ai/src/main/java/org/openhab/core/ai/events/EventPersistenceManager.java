package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;

/**
 * Event Persistence Manager - Event persistence interface
 * 
 * This interface provides event persistence capabilities to store and
 * retrieve events for later analysis and replay.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventPersistenceManager {

    /**
     * Persist an event for later retrieval
     * 
     * @param event The event to persist
     */
    void persistEvent(Event event);

    /**
     * Get events within a specific time range
     * 
     * @param startTime The start time (inclusive)
     * @param endTime The end time (inclusive)
     * @return List of events in the time range
     */
    List<Event> getEventsInTimeRange(Instant startTime, Instant endTime);

    /**
     * Get events by type within a time range
     * 
     * @param eventType The event type to filter by
     * @param startTime The start time (inclusive)
     * @param endTime The end time (inclusive)
     * @return List of events matching the criteria
     */
    List<Event> getEventsByType(String eventType, Instant startTime, Instant endTime);

    /**
     * Clean up old events based on retention policy
     * 
     * @param retentionPeriod The retention period in seconds
     */
    void cleanupOldEvents(long retentionPeriod);
}
