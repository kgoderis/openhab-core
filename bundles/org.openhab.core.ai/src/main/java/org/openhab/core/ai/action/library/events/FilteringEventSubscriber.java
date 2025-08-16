package org.openhab.core.ai.action.library.events;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.events.ItemEvent;

/**
 * Filtering event subscriber that forwards events to SSE endpoints.
 *
 * Extracted from {@link EventSubscriptionRegistry}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilteringEventSubscriber implements EventSubscriber {

    private final String clientId;
    private final String subscriptionId;
    private final Set<String> eventTypes;
    private final @Nullable Map<String, String> filters;

    public FilteringEventSubscriber(String clientId, String subscriptionId, Set<String> eventTypes,
            @Nullable Map<String, String> filters) {
        this.clientId = clientId;
        this.subscriptionId = subscriptionId;
        this.eventTypes = eventTypes;
        this.filters = filters;
    }

    @Override
    public void receive(Event event) {
        if (!eventTypes.contains(event.getType())) {
            return;
        }
        if (filters != null && !passesFilters(event, filters)) {
            return;
        }
        EventSSEManager.getInstance().forwardEvent(subscriptionId, event);
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return eventTypes;
    }

    private boolean passesFilters(Event event, @Nullable Map<String, String> filters) {
        if (filters == null) {
            return true;
        }
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String key = filter.getKey();
            String value = filter.getValue();
            switch (key) {
                case "topic":
                    if (!event.getTopic().contains(value)) {
                        return false;
                    }
                    break;
                case "source":
                    if (!event.getSource().contains(value)) {
                        return false;
                    }
                    break;
                case "itemName":
                    if (event instanceof ItemEvent itemEvent) {
                        if (!itemEvent.getItemName().contains(value)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    break;
                case "thingUID":
                    if (!event.getTopic().contains(value)) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }
}
