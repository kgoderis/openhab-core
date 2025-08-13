package org.openhab.core.ai.events;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

    public enum FilterType {
        /** Priority-based filtering */
        PRIORITY,
        /** Pattern-based filtering */
        PATTERN,
        /** Sampling-based filtering */
        SAMPLING,
        /** Time-based filtering */
        TIME_BASED,
        /** Source-based filtering */
        SOURCE_BASED,
        /** Type-based filtering */
        TYPE_BASED,
        /** Custom filtering */
        CUSTOM
    }