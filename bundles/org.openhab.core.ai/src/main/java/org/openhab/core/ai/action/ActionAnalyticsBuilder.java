package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ActionAnalytics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionAnalyticsBuilder {
    String actionId = "";
    long totalExecutions = 0;
    long successfulExecutions = 0;
    long failedExecutions = 0;
    Duration averageExecutionTime = Duration.ZERO;
    Duration minExecutionTime = Duration.ZERO;
    Duration maxExecutionTime = Duration.ZERO;
    Map<String, Long> executionByAgent = Map.of();
    Map<String, Long> executionByTimeOfDay = Map.of();
    Map<String, Long> executionByDayOfWeek = Map.of();
    List<String> commonErrorMessages = List.of();
    Map<String, Long> parameterUsage = Map.of();
    Instant firstExecution = Instant.now();
    Instant lastExecution = Instant.now();
    Duration totalExecutionTime = Duration.ZERO;

    public ActionAnalyticsBuilder actionId(String actionId) {
        this.actionId = actionId;
        return this;
    }

    public ActionAnalyticsBuilder totalExecutions(long v) {
        this.totalExecutions = v;
        return this;
    }

    public ActionAnalyticsBuilder successfulExecutions(long v) {
        this.successfulExecutions = v;
        return this;
    }

    public ActionAnalyticsBuilder failedExecutions(long v) {
        this.failedExecutions = v;
        return this;
    }

    public ActionAnalyticsBuilder averageExecutionTime(Duration v) {
        this.averageExecutionTime = v;
        return this;
    }

    public ActionAnalyticsBuilder minExecutionTime(Duration v) {
        this.minExecutionTime = v;
        return this;
    }

    public ActionAnalyticsBuilder maxExecutionTime(Duration v) {
        this.maxExecutionTime = v;
        return this;
    }

    public ActionAnalyticsBuilder executionByAgent(Map<String, Long> v) {
        this.executionByAgent = v;
        return this;
    }

    public ActionAnalyticsBuilder executionByTimeOfDay(Map<String, Long> v) {
        this.executionByTimeOfDay = v;
        return this;
    }

    public ActionAnalyticsBuilder executionByDayOfWeek(Map<String, Long> v) {
        this.executionByDayOfWeek = v;
        return this;
    }

    public ActionAnalyticsBuilder commonErrorMessages(List<String> v) {
        this.commonErrorMessages = v;
        return this;
    }

    public ActionAnalyticsBuilder parameterUsage(Map<String, Long> v) {
        this.parameterUsage = v;
        return this;
    }

    public ActionAnalyticsBuilder firstExecution(Instant v) {
        this.firstExecution = v;
        return this;
    }

    public ActionAnalyticsBuilder lastExecution(Instant v) {
        this.lastExecution = v;
        return this;
    }

    public ActionAnalyticsBuilder totalExecutionTime(Duration v) {
        this.totalExecutionTime = v;
        return this;
    }

    public ActionAnalytics build() {
        return new ActionAnalytics(this);
    }
}
