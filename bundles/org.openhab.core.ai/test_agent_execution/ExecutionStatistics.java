package org.openhab.core.ai.agent.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentSkillException;
import org.openhab.core.ai.agent.api.AgentSkillResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.a2a.spec.Message;

    public static class ExecutionStatistics {
        private final long totalExecutions;
        private final long successfulExecutions;
        private final long failedExecutions;

        public ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
        }

        public long getTotalExecutions() {
            return totalExecutions;
        }

        public long getSuccessfulExecutions() {
            return successfulExecutions;
        }

        public long getFailedExecutions() {
            return failedExecutions;
        }

        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
        }
    }