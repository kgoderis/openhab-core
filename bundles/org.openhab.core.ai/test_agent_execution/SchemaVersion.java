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

    public static class SchemaVersion {
        private final String actionId;
        private final String version;
        private final TaskSchema schema;
        private final long createdAt;

        public SchemaVersion(String actionId, String version, TaskSchema schema, long createdAt) {
            this.actionId = actionId;
            this.version = version;
            this.schema = schema;
            this.createdAt = createdAt;
        }

        // Getters
        public String getActionId() {
            return actionId;
        }

        public String getVersion() {
            return version;
        }

        public TaskSchema getSchema() {
            return schema;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }