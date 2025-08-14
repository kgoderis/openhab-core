package org.openhab.core.ai.events;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;

@NonNullByDefault
public interface FilterRule {
    String getId();
    String getName();
    String getDescription();
    boolean isEnabled();
    int getPriority();
    FilterType getType();
    Map<String, Object> getConfiguration();
    boolean apply(Event event);
}


