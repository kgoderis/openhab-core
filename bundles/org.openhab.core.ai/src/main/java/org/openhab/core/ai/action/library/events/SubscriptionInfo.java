package org.openhab.core.ai.action.library.events;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class SubscriptionInfo {
    private final String subscriptionId;
    private final String clientId;
    private final Set<String> eventTypes;
    private final @Nullable Map<String, String> filters;
    private final long createdAt;

    public SubscriptionInfo(String subscriptionId, String clientId, Set<String> eventTypes,
            @Nullable Map<String, String> filters) {
        this.subscriptionId = subscriptionId;
        this.clientId = clientId;
        this.eventTypes = eventTypes;
        this.filters = filters;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getClientId() {
        return clientId;
    }

    public Set<String> getEventTypes() {
        return eventTypes;
    }

    public @Nullable Map<String, String> getFilters() {
        return filters;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSseUrl() {
        return "/mcp/events/" + subscriptionId;
    }
}
