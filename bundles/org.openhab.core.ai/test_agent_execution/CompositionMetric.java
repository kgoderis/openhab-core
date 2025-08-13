package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentSkillManager;
import org.openhab.core.ai.agent.api.AgentSkillResult;
import org.openhab.core.ai.agents.SkillExecutionRequest;
import org.openhab.core.ai.events.EventProcessingAnalytics;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    public static class CompositionMetric {
        private final String compositionId;
        private final boolean success;
        private final Duration duration;
        private final @Nullable String message;
        private final Instant timestamp;

        public CompositionMetric(String compositionId, boolean success, Duration duration, @Nullable String message,
                Instant timestamp) {
            this.compositionId = compositionId;
            this.success = success;
            this.duration = duration;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getCompositionId() {
            return compositionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public Duration getDuration() {
            return duration;
        }

        public @Nullable String getMessage() {
            return message;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }