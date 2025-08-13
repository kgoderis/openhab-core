package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.a2a.spec.Task;

    public static class SchemaCompatibilityResult {
        private final boolean compatible;
        private final String message;

        public SchemaCompatibilityResult(boolean compatible, String message) {
            this.compatible = compatible;
            this.message = message;
        }

        // Getters
        public boolean isCompatible() {
            return compatible;
        }

        public String getMessage() {
            return message;
        }
    }