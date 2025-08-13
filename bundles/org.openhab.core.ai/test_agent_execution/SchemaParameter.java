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

    public static class SchemaParameter {
        private final String name;
        private final String type;
        private final String description;
        private final boolean required;
        private final Object defaultValue;
        private final Map<String, Object> constraints;

        public SchemaParameter(String name, String type, String description, boolean required, Object defaultValue,
                Map<String, Object> constraints) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
            this.defaultValue = defaultValue;
            this.constraints = constraints;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public Map<String, Object> getConstraints() {
            return constraints;
        }
    }