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

    public static class CompositionResult {
        private final String compositionId;
        private final boolean success;
        private final @Nullable List<AgentSkillResult> results;
        private final @Nullable String message;
        private final long executionTime;

        private CompositionResult(String compositionId, boolean success, @Nullable List<AgentSkillResult> results,
                @Nullable String message, long executionTime) {
            this.compositionId = compositionId;
            this.success = success;
            this.results = results;
            this.message = message;
            this.executionTime = executionTime;
        }

        public static CompositionResult success(String compositionId, List<AgentSkillResult> results,
                long executionTime) {
            return new CompositionResult(compositionId, true, results, null, executionTime);
        }

        public static CompositionResult error(String compositionId, String message, long executionTime) {
            return new CompositionResult(compositionId, false, null, message, executionTime);
        }

        public String getCompositionId() {
            return compositionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public @Nullable List<AgentSkillResult> getResults() {
            return results;
        }

        public @Nullable String getMessage() {
            return message;
        }

        public long getExecutionTime() {
            return executionTime;
        }
    }