# Metrics Refactoring Plan: Centralized MetricsService with Builder Pattern

## Executive Summary

This document outlines the comprehensive refactoring of the metrics and monitoring system in the openHAB AI bundle to implement a **centralized MetricsService** with a **builder pattern for flexible data recording**. The refactoring eliminates duplicate/wrapper methods and uses **snapshots with capability interfaces** for clean, type-safe metrics management.

## Current State Analysis

### Problems with Current Implementation

1. **Redundant AbstractMetrics Classes**: 25+ classes extending `AbstractMetrics` with duplicate method implementations
2. **Wrapper Methods**: Confusing patterns like `success()` calling `getSuccessfulOperations()`
3. **Scattered Collection**: Metrics collection scattered across multiple services
4. **Inflexible Data Recording**: No support for operation-specific data types
5. **Complex Inheritance**: Deep inheritance hierarchies instead of composition

### Current Patterns to Eliminate

```java
// ❌ REDUNDANT: These classes will be removed
public class AgentModelPerformanceMetrics extends AbstractMetrics 
    implements CountsMetrics, LatencyMetrics {
    
    @Override
    public long success() { return getSuccessfulOperations(); }  // Wrapper method
    @Override
    public long total() { return getTotalOperations(); }         // Wrapper method
    // ... 200+ lines of duplicate code
}
```

## Target Architecture

### Core Design Principles

1. **Single Centralized Service**: One `MetricsService` for all metrics collection
2. **Builder Pattern**: Flexible data recording for different operation types
3. **Capability Interfaces**: Snapshot classes implement relevant interfaces
4. **No Duplicate Methods**: Direct access to metrics data
5. **Operation-Specific Snapshots**: Different snapshot types for different domains
6. **Statistics Integration**: Computed insights derived from metrics data

### Metrics vs Statistics

| **Metrics** | **Statistics** |
|-------------|----------------|
| Real-time counts, latencies | Aggregated averages, trends |
| Individual operation data | Historical summaries |
| Current state snapshots | Computed insights |
| Raw performance data | Business intelligence |

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Centralized MetricsService               │
├─────────────────────────────────────────────────────────────┤
│  recordOperation(domain, operation)                         │
│  recordModelOperation(modelId, operation)                   │
│  recordToolOperation(toolId, operation)                     │
│  recordAgentOperation(agentId, operation)                   │
│                                                             │
│  getSnapshot(domain, operation, snapshotType)               │
│  getModelSnapshot(modelId, operation)                       │
│  getToolSnapshot(toolId, operation)                         │
│  getAgentSnapshot(agentId, operation)                       │
│                                                             │
│  getStatistics(domain, operation, timeRange)                │
│  getModelStatistics(modelId, timeRange)                     │
│  getToolStatistics(toolId, timeRange)                       │
│  getAgentStatistics(agentId, timeRange)                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 Builder Pattern for Data                    │
├─────────────────────────────────────────────────────────────┤
│  recordOperation("model", "gpt4:completion")                │
│    .withSuccess(true)                                       │
│    .withDuration(50000000L)                                 │
│    .withData("tokens", 150)                                 │
│    .withData("cost", 0.002)                                 │
│    .record()                                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Operation-Specific Snapshots                   │
├─────────────────────────────────────────────────────────────┤
│  ModelCompletionSnapshot implements CountsMetrics,          │
│    LatencyMetrics, ModelMetrics                             │
│                                                             │
│  ToolFileReadSnapshot implements CountsMetrics,             │
│    LatencyMetrics, ToolMetrics                              │
│                                                             │
│  AgentTaskSnapshot implements CountsMetrics,                │
│    LatencyMetrics, AgentMetrics                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Statistics and Analytics                       │
├─────────────────────────────────────────────────────────────┤
│  ModelCompletionStatistics implements TrendMetrics,         │
│    PercentileMetrics, BusinessMetrics                       │
│                                                             │
│  ToolPerformanceStatistics implements TrendMetrics,         │
│    PercentileMetrics, EfficiencyMetrics                     │
│                                                             │
│  AgentBehaviorStatistics implements TrendMetrics,           │
│    PercentileMetrics, IntelligenceMetrics                   │
└─────────────────────────────────────────────────────────────┘
```

## Implementation Plan

### Phase 1: Core Infrastructure (Week 1-2)

#### 1.1 Create Centralized MetricsService

**Tasks**:
- [ ] Create `MetricsService` interface
- [ ] Implement `MetricsServiceImpl` with OSGi `@Component`
- [ ] Add `@Reference MonitoringRegistry` for underlying collection
- [ ] Implement basic operation recording methods
- [ ] Add proper null safety and error handling

**Deliverables**:
```java
@Component(service = MetricsService.class)
@NonNullByDefault
public interface MetricsService {
    
    /**
     * Record any operation with basic metrics
     */
    void recordOperation(String domain, String operation, boolean success, long durationNanos);
    
    /**
     * Get snapshot for any domain/operation
     */
    <T extends MetricsSnapshot> T getSnapshot(String domain, String operation, Class<T> snapshotType);
    
    /**
     * Domain-specific convenience methods
     */
    void recordModelOperation(String modelId, String operation, boolean success, long durationNanos);
    void recordToolOperation(String toolId, String operation, boolean success, long durationNanos);
    void recordAgentOperation(String agentId, String operation, boolean success, long durationNanos);
    
    /**
     * Domain-specific snapshot retrieval
     */
    ModelCompletionSnapshot getModelSnapshot(String modelId, String operation);
    ToolFileReadSnapshot getToolSnapshot(String toolId, String operation);
    AgentTaskSnapshot getAgentSnapshot(String agentId, String operation);
    
    /**
     * Statistics methods - computed insights from metrics data
     */
    <T extends StatisticsSnapshot> T getStatistics(String domain, String operation, 
        Duration timeRange, Class<T> statisticsType);
    
    /**
     * Domain-specific statistics retrieval
     */
    ModelCompletionStatistics getModelStatistics(String modelId, String operation, Duration timeRange);
    ToolPerformanceStatistics getToolStatistics(String toolId, String operation, Duration timeRange);
    AgentBehaviorStatistics getAgentStatistics(String agentId, String operation, Duration timeRange);
}
```

#### 1.2 Implement Builder Pattern for Flexible Data

**Tasks**:
- [ ] Create `OperationRecorder` builder class
- [ ] Implement fluent API for data recording
- [ ] Support arbitrary key-value data storage
- [ ] Add validation for data types and values
- [ ] Create unit tests for builder pattern

**Deliverables**:
```java
public static class OperationRecorder {
    private final MetricsService service;
    private final String domain;
    private final String operation;
    private final Map<String, Object> data = new HashMap<>();
    private boolean success;
    private long durationNanos;
    
    public OperationRecorder(MetricsService service, String domain, String operation) {
        this.service = service;
        this.domain = domain;
        this.operation = operation;
    }
    
    public OperationRecorder withSuccess(boolean success) {
        this.success = success;
        return this;
    }
    
    public OperationRecorder withDuration(long durationNanos) {
        this.durationNanos = durationNanos;
        return this;
    }
    
    public OperationRecorder withData(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    public void record() {
        service.recordOperationWithData(domain, operation, success, durationNanos, data);
    }
}
```

**Usage Examples**:
```java
// Model operations with flexible data
metricsService.recordOperation("model", "gpt4:completion")
    .withSuccess(true)
    .withDuration(50000000L)
    .withData("tokens", 150)
    .withData("cost", 0.002)
    .withData("temperature", 0.7)
    .withData("maxTokens", 2048)
    .record();

// Tool operations with flexible data
metricsService.recordOperation("tool", "file:read")
    .withSuccess(true)
    .withDuration(1000000L)
    .withData("fileSize", 1024L)
    .withData("bytesRead", 1024L)
    .withData("fileType", "json")
    .record();

// Agent operations with flexible data
metricsService.recordOperation("agent", "task:execution")
    .withSuccess(true)
    .withDuration(2000000L)
    .withData("taskType", "classification")
    .withData("priority", 5)
    .withData("skill", "image_analysis")
    .record();
```

### Phase 2: Snapshot Classes with Capability Interfaces (Week 2-3)

#### 2.1 Define Capability Interfaces

**Tasks**:
- [ ] Create `CountsMetrics` interface for basic counting
- [ ] Create `LatencyMetrics` interface for timing
- [ ] Create `ModelMetrics` interface for model-specific metrics
- [ ] Create `ToolMetrics` interface for tool-specific metrics
- [ ] Create `AgentMetrics` interface for agent-specific metrics
- [ ] Add unit tests for interface contracts

**Deliverables**:
```java
public interface CountsMetrics {
    long totalCount();
    long successCount();
    long failureCount();
    double successRate();
}

public interface LatencyMetrics {
    long totalDurationNanos();
    double averageMs();
    double operationsPerSecond();
}

public interface ModelMetrics {
    double tokensPerSecond();
    double costPerRequest();
    double averageTokensPerRequest();
}

public interface ToolMetrics {
    double throughputPerSecond();
    double averageResourceUsage();
    int maxConcurrentExecutions();
}

public interface AgentMetrics {
    double decisionAccuracy();
    double learningRate();
    double successRate();
}

// Statistics capability interfaces
public interface TrendMetrics {
    double trendPercentage();
    String trendDirection(); // "increasing", "decreasing", "stable"
    double changeRate();
}

public interface PercentileMetrics {
    double percentile50(); // median
    double percentile90();
    double percentile95();
    double percentile99();
}

public interface BusinessMetrics {
    double costEfficiency();
    double resourceUtilization();
    double throughputEfficiency();
}

public interface EfficiencyMetrics {
    double resourceEfficiency();
    double timeEfficiency();
    double energyEfficiency();
}

public interface IntelligenceMetrics {
    double decisionQuality();
    double learningEfficiency();
    double adaptationRate();
}
```

#### 2.2 Implement Operation-Specific Snapshots

**Tasks**:
- [ ] Create `ModelCompletionSnapshot` implementing relevant interfaces
- [ ] Create `ToolFileReadSnapshot` implementing relevant interfaces
- [ ] Create `AgentTaskSnapshot` implementing relevant interfaces
- [ ] Implement computed metrics in snapshots
- [ ] Add edge case handling for zero values
- [ ] Create comprehensive unit tests

**Deliverables**:
```java
public record ModelCompletionSnapshot(Counts counts, Timing timing, long timestampMs,
        long totalTokens, double totalCost) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ModelMetrics {
    
    // CountsMetrics implementation
    public long totalCount() { return counts.total(); }
    public long successCount() { return counts.success(); }
    public long failureCount() { return counts.failure(); }
    public double successRate() {
        if (totalCount() == 0) return 0.0;
        return (successCount() * 100.0) / totalCount();
    }
    
    // LatencyMetrics implementation
    public long totalDurationNanos() { return timing.totalDurationNanos(); }
    public double averageMs() { return timing.averageMs(totalCount()); }
    public double operationsPerSecond() {
        if (timing.totalDurationNanos() == 0) return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return totalCount() / durationSeconds;
    }
    
    // ModelMetrics implementation
    public double tokensPerSecond() {
        if (totalCount() == 0 || timing.totalDurationNanos() == 0) return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return totalTokens / durationSeconds;
    }
    
    public double costPerRequest() {
        if (totalCount() == 0) return 0.0;
        return totalCost / totalCount();
    }
    
    public double averageTokensPerRequest() {
        if (totalCount() == 0) return 0.0;
        return (double) totalTokens / totalCount();
    }
}
```

#### 2.3 Implement Statistics Classes

**Tasks**:
- [ ] Create `StatisticsSnapshot` marker interface
- [ ] Create `ModelCompletionStatistics` implementing relevant interfaces
- [ ] Create `ToolPerformanceStatistics` implementing relevant interfaces
- [ ] Create `AgentBehaviorStatistics` implementing relevant interfaces
- [ ] Implement computed statistics from metrics data
- [ ] Add time-range based calculations
- [ ] Create comprehensive unit tests

**Deliverables**:
```java
public record ModelCompletionStatistics(
        List<ModelCompletionSnapshot> snapshots,
        Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, BusinessMetrics {
    
    // TrendMetrics implementation
    public double trendPercentage() {
        if (snapshots.size() < 2) return 0.0;
        
        // Calculate trend based on success rate over time
        double firstHalf = calculateAverageSuccessRate(0, snapshots.size() / 2);
        double secondHalf = calculateAverageSuccessRate(snapshots.size() / 2, snapshots.size());
        
        if (firstHalf == 0) return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }
    
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) return "increasing";
        if (trend < -1.0) return "decreasing";
        return "stable";
    }
    
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }
    
    // PercentileMetrics implementation
    public double percentile50() {
        return calculatePercentile(0.5);
    }
    
    public double percentile90() {
        return calculatePercentile(0.9);
    }
    
    public double percentile95() {
        return calculatePercentile(0.95);
    }
    
    public double percentile99() {
        return calculatePercentile(0.99);
    }
    
    // BusinessMetrics implementation
    public double costEfficiency() {
        if (snapshots.isEmpty()) return 0.0;
        
        double totalCost = snapshots.stream()
            .mapToDouble(s -> s.totalCost())
            .sum();
        long totalOperations = snapshots.stream()
            .mapToLong(s -> s.totalCount())
            .sum();
        
        if (totalOperations == 0) return 0.0;
        return totalCost / totalOperations;
    }
    
    public double resourceUtilization() {
        if (snapshots.isEmpty()) return 0.0;
        
        long totalDuration = snapshots.stream()
            .mapToLong(s -> s.totalDurationNanos())
            .sum();
        long totalOperations = snapshots.stream()
            .mapToLong(s -> s.totalCount())
            .sum();
        
        if (totalOperations == 0) return 0.0;
        return (double) totalDuration / (totalOperations * timeRange.toNanos());
    }
    
    public double throughputEfficiency() {
        if (snapshots.isEmpty()) return 0.0;
        
        long totalOperations = snapshots.stream()
            .mapToLong(s -> s.totalCount())
            .sum();
        
        return (double) totalOperations / timeRange.toHours();
    }
    
    // Helper methods
    private double calculateAverageSuccessRate(int start, int end) {
        return snapshots.subList(start, end).stream()
            .mapToDouble(s -> s.successRate())
            .average()
            .orElse(0.0);
    }
    
    private double calculatePercentile(double percentile) {
        List<Double> latencies = snapshots.stream()
            .flatMap(s -> Stream.generate(() -> s.averageMs()).limit(s.totalCount()))
            .sorted()
            .collect(Collectors.toList());
        
        if (latencies.isEmpty()) return 0.0;
        
        int index = (int) Math.ceil(percentile * latencies.size()) - 1;
        return latencies.get(Math.max(0, index));
    }
}
```

### Phase 3: Migration from Existing Metrics (Week 3-4)

#### 3.1 Remove Redundant AbstractMetrics Classes

**Tasks**:
- [ ] Identify all 25+ AbstractMetrics classes
- [ ] Remove redundant wrapper methods
- [ ] Delete duplicate implementations
- [ ] Update all consumers to use MetricsService
- [ ] Create migration guide for each class

**Classes to Remove**:
- `AgentModelPerformanceMetrics`
- `ToolPerformanceMetrics`
- `ActionPerformanceMetrics`
- `MemoryPerformanceMetrics`
- `InputPerformanceMetrics`
- `OrchestrationPerformanceMetrics`
- `AutonomousPerformanceMetrics`
- `SafetyPerformanceMetrics`
- `LearningPerformanceMetrics`
- `ConfigurationPerformanceMetrics`
- And 15+ more similar classes

#### 3.2 Update Domain Services

**Tasks**:
- [ ] Update all domain services to use `@Reference MetricsService`
- [ ] Replace direct metric recording with MetricsService calls
- [ ] Update metric recording patterns to use builder pattern
- [ ] Add proper error handling for metric recording
- [ ] Create unit tests for updated services

**Migration Examples**:
```java
// BEFORE: Direct metric recording
private final AtomicLong totalOperations = new AtomicLong(0);
private final AtomicLong successfulOperations = new AtomicLong(0);

public void executeOperation() {
    totalOperations.increment();
    // ... do work ...
    successfulOperations.increment();
}

// AFTER: Centralized MetricsService
@Reference
private @Nullable MetricsService metricsService;

public void executeOperation() {
    long startTime = System.nanoTime();
    boolean success = false;
    
    try {
        // ... do work ...
        success = true;
    } finally {
        if (metricsService != null) {
            metricsService.recordOperation("my-domain", "my-operation", success, 
                System.nanoTime() - startTime);
        }
    }
}

// WITH BUILDER PATTERN:
if (metricsService != null) {
    metricsService.recordOperation("model", "gpt4:completion")
        .withSuccess(success)
        .withDuration(System.nanoTime() - startTime)
        .withData("tokens", tokens)
        .withData("cost", cost)
        .withData("temperature", temperature)
        .record();
}
```

#### 3.3 Migrate Statistics Classes

**Tasks**:
- [ ] Analyze all existing statistics classes (25+ classes identified)
- [ ] Remove redundant AbstractStatistics-based classes
- [ ] Migrate remaining classes to new capability interface pattern
- [ ] Update all consumers to use MetricsService for statistics
- [ ] Create migration guide for each statistics class
- [ ] Add time-range support to statistics generation

**Current Statistics Classes Analysis**:

**Classes to Remove (redundant with new plan)**:
- `AgentStatistics` → replace with `AgentBehaviorStatistics`
- `ToolStatistics` → replace with `ToolPerformanceStatistics`
- `ProviderStatistics` → replace with `ProviderPerformanceStatistics`
- `EventProcessingStatistics` → replace with `EventProcessingStatistics`
- `ContextPerformanceMetrics` → replace with `ContextPerformanceStatistics`
- `ModelIntegrationStatistics` → replace with `ModelCompletionStatistics`
- `SystemAggregatedStatistics` → replace with `DomainAggregatedSnapshot`
- `ReasoningStepStorageStatistics` → replace with `ReasoningPerformanceStatistics`
- `ReasoningAnalysisStatistics` → replace with `ReasoningPerformanceStatistics`

**Classes to Migrate (update to new pattern)**:
- `StubStatistics` → migrate to use MetricsService
- `ResourceUsageStatistics` → migrate to use MetricsService
- `TransportStatistics` → migrate to use MetricsService
- `AgentServerStatistics` → migrate to use MetricsService
- `ConflictResolutionStatistics` → migrate to use MetricsService
- `ConversationStatistics` → migrate to use MetricsService
- `MessagingStatistics` → migrate to use MetricsService
- `EventBusStatistics` → migrate to use MetricsService

**Classes to Keep (already migrated)**:
- `StubServiceStatistics` - already uses MonitoringRegistry

**Migration Examples**:
```java
// BEFORE: AbstractStatistics-based class with wrapper methods
public class AgentStatistics extends AbstractStatistics implements CountsMetrics, LatencyMetrics {
    private final String agentId;
    private final AgentState agentState;
    private final long totalDurationNanos;
    
    @Override
    public long total() { return getTotalCount(); }  // Wrapper method
    @Override
    public long success() { return getSuccessCount(); }  // Wrapper method
    @Override
    public long failure() { return getFailureCount(); }  // Wrapper method
    @Override
    public long totalDurationNanos() { return totalDurationNanos; }
    @Override
    public double averageMs() { return getAverageResponseTime(); }
}

// AFTER: New capability interface pattern with time-range support
public record AgentBehaviorStatistics(
    List<AgentTaskSnapshot> snapshots,
    Duration timeRange,
    long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, IntelligenceMetrics {
    
    // CountsMetrics implementation
    public long totalCount() { 
        return snapshots.stream().mapToLong(s -> s.totalCount()).sum(); 
    }
    public long successCount() { 
        return snapshots.stream().mapToLong(s -> s.successCount()).sum(); 
    }
    public long failureCount() { 
        return snapshots.stream().mapToLong(s -> s.failureCount()).sum(); 
    }
    public double successRate() {
        if (totalCount() == 0) return 0.0;
        return (successCount() * 100.0) / totalCount();
    }
    
    // LatencyMetrics implementation
    public long totalDurationNanos() { 
        return snapshots.stream().mapToLong(s -> s.totalDurationNanos()).sum(); 
    }
    public double averageMs() { 
        if (totalCount() == 0) return 0.0;
        return totalDurationNanos() / (totalCount() * 1_000_000.0);
    }
    public double operationsPerSecond() {
        if (timeRange.toNanos() == 0) return 0.0;
        return totalCount() / (timeRange.toNanos() / 1_000_000_000.0);
    }
    
    // TrendMetrics implementation
    public double trendPercentage() {
        if (snapshots.size() < 2) return 0.0;
        
        // Calculate trend based on success rate over time
        double firstHalf = calculateAverageSuccessRate(0, snapshots.size() / 2);
        double secondHalf = calculateAverageSuccessRate(snapshots.size() / 2, snapshots.size());
        
        if (firstHalf == 0) return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }
    
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) return "increasing";
        if (trend < -1.0) return "decreasing";
        return "stable";
    }
    
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }
    
    // PercentileMetrics implementation
    public double percentile50() { return calculatePercentile(0.5); }
    public double percentile90() { return calculatePercentile(0.9); }
    public double percentile95() { return calculatePercentile(0.95); }
    public double percentile99() { return calculatePercentile(0.99); }
    
    // IntelligenceMetrics implementation
    public double decisionQuality() {
        if (snapshots.isEmpty()) return 0.0;
        return snapshots.stream()
            .mapToDouble(s -> s.decisionAccuracy())
            .average()
            .orElse(0.0);
    }
    
    public double learningEfficiency() {
        if (snapshots.isEmpty()) return 0.0;
        return snapshots.stream()
            .mapToDouble(s -> s.learningRate())
            .average()
            .orElse(0.0);
    }
    
    public double adaptationRate() {
        if (snapshots.size() < 2) return 0.0;
        
        // Calculate adaptation rate based on learning efficiency trend
        double firstHalf = snapshots.subList(0, snapshots.size() / 2).stream()
            .mapToDouble(s -> s.learningRate())
            .average()
            .orElse(0.0);
        double secondHalf = snapshots.subList(snapshots.size() / 2, snapshots.size()).stream()
            .mapToDouble(s -> s.learningRate())
            .average()
            .orElse(0.0);
        
        if (firstHalf == 0) return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }
    
    // Helper methods
    private double calculateAverageSuccessRate(int start, int end) {
        return snapshots.subList(start, end).stream()
            .mapToDouble(s -> s.successRate())
            .average()
            .orElse(0.0);
    }
    
    private double calculatePercentile(double percentile) {
        List<Double> latencies = snapshots.stream()
            .flatMap(s -> Stream.generate(() -> s.averageMs()).limit(s.totalCount()))
            .sorted()
            .collect(Collectors.toList());
        
        if (latencies.isEmpty()) return 0.0;
        
        int index = (int) Math.ceil(percentile * latencies.size()) - 1;
        return latencies.get(Math.max(0, index));
    }
}
```

**Usage Migration**:
```java
// BEFORE: Direct statistics access
@Reference
private @Nullable AgentStatistics agentStatistics;

public void getAgentStats() {
    if (agentStatistics != null) {
        long total = agentStatistics.total();  // Wrapper method
        double successRate = agentStatistics.getSuccessRate();
    }
}

// AFTER: Centralized MetricsService with time-range support
@Reference
private @Nullable MetricsService metricsService;

public void getAgentStats() {
    if (metricsService != null) {
        // Get current snapshot (metrics)
        AgentTaskSnapshot snapshot = metricsService.getAgentSnapshot("agent1", "task:execution");
        long total = snapshot.totalCount();  // Direct access
        double successRate = snapshot.successRate();
        
        // Get historical statistics (computed insights)
        AgentBehaviorStatistics stats = metricsService.getAgentStatistics(
            "agent1", "task:execution", Duration.ofDays(30)
        );
        double trend = stats.trendPercentage();
        double p95 = stats.percentile95();
        double decisionQuality = stats.decisionQuality();
    }
}
```

**Deliverables**:
- [ ] All 25+ statistics classes migrated to new pattern
- [ ] Time-range support implemented for all statistics
- [ ] New capability interfaces implemented (TrendMetrics, PercentileMetrics, etc.)
- [ ] All consumers updated to use MetricsService
- [ ] Comprehensive unit tests for new statistics classes
- [ ] Migration guide documenting all changes

### Phase 3.4: Comprehensive Action Points

**This section provides detailed checkboxes for all classes that need refactoring, including both the classes themselves and the classes that need to upgrade their metrics recording.**

#### 3.4.1 Metrics Classes to Remove (AbstractMetrics-based)

**Classes extending AbstractMetrics with duplicate implementations:**

- [ ] `AgentModelPerformanceMetrics` - Remove class, replace with `ModelCompletionSnapshot`
- [ ] `ToolPerformanceMetrics` - Remove class, replace with `ToolFileReadSnapshot`
- [ ] `ActionPerformanceMetrics` - Remove class, replace with `ActionExecutionSnapshot`
- [ ] `MemoryPerformanceMetrics` - Remove class, replace with `MemoryUsageSnapshot`
- [ ] `InputPerformanceMetrics` - Remove class, replace with `InputProcessingSnapshot`
- [ ] `OrchestrationPerformanceMetrics` - Remove class, replace with `OrchestrationSnapshot`
- [ ] `AutonomousPerformanceMetrics` - Remove class, replace with `AutonomousBehaviorSnapshot`
- [ ] `SafetyPerformanceMetrics` - Remove class, replace with `SafetyMonitoringSnapshot`
- [ ] `LearningPerformanceMetrics` - Remove class, replace with `LearningProgressSnapshot`
- [ ] `ConfigurationPerformanceMetrics` - Remove class, replace with `ConfigurationSnapshot`
- [ ] `ContextPerformanceMetrics` - Remove class, replace with `ContextProcessingSnapshot`
- [ ] `EventProcessingPerformanceMetrics` - Remove class, replace with `EventProcessingSnapshot`
- [ ] `ReasoningPerformanceMetrics` - Remove class, replace with `ReasoningExecutionSnapshot`
- [ ] `CommunicationPerformanceMetrics` - Remove class, replace with `CommunicationSnapshot`
- [ ] `SecurityPerformanceMetrics` - Remove class, replace with `SecurityMonitoringSnapshot`
- [ ] `TransportPerformanceMetrics` - Remove class, replace with `TransportSnapshot`
- [ ] `PersistencePerformanceMetrics` - Remove class, replace with `PersistenceSnapshot`
- [ ] `ValidationPerformanceMetrics` - Remove class, replace with `ValidationSnapshot`
- [ ] `IntegrationPerformanceMetrics` - Remove class, replace with `IntegrationSnapshot`
- [ ] `OptimizationPerformanceMetrics` - Remove class, replace with `OptimizationSnapshot`
- [ ] `CollaborationPerformanceMetrics` - Remove class, replace with `CollaborationSnapshot`
- [ ] `CoordinationPerformanceMetrics` - Remove class, replace with `CoordinationSnapshot`
- [ ] `ConflictResolutionPerformanceMetrics` - Remove class, replace with `ConflictResolutionSnapshot`
- [ ] `ConversationPerformanceMetrics` - Remove class, replace with `ConversationSnapshot`
- [ ] `MessagingPerformanceMetrics` - Remove class, replace with `MessagingSnapshot`

#### 3.4.2 Statistics Classes to Remove (AbstractStatistics-based)

**Classes extending AbstractStatistics with wrapper methods:**

- [ ] `AgentStatistics` - Remove class, replace with `AgentBehaviorStatistics`
- [ ] `ToolStatistics` - Remove class, replace with `ToolPerformanceStatistics`
- [ ] `ProviderStatistics` - Remove class, replace with `ProviderPerformanceStatistics`
- [ ] `EventProcessingStatistics` - Remove class, replace with `EventProcessingStatistics`
- [ ] `ContextPerformanceMetrics` - Remove class, replace with `ContextPerformanceStatistics`
- [ ] `ModelIntegrationStatistics` - Remove class, replace with `ModelCompletionStatistics`
- [ ] `SystemAggregatedStatistics` - Remove class, replace with `DomainAggregatedSnapshot`
- [ ] `ReasoningStepStorageStatistics` - Remove class, replace with `ReasoningPerformanceStatistics`
- [ ] `ReasoningAnalysisStatistics` - Remove class, replace with `ReasoningPerformanceStatistics`

#### 3.4.3 Statistics Classes to Migrate (Update to use MetricsService)

**Standalone statistics classes that need MetricsService integration:**

- [ ] `StubStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ResourceUsageStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `TransportStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `AgentServerStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ConflictResolutionStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ConversationStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `MessagingStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `EventBusStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `LifecycleStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ServerStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `FilterStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `AgentModelContextCacheStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ContextManagerStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `CoordinationStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ExecutionStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `SchemaStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `AgentModelOptimizationStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `AgentModelStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `MessageSecurityStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ToolSecurityStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `AgentSecurityStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `ErrorRecoveryStatistics` - Migrate to use MetricsService, update all consumers
- [ ] `MonitoringStatistics` - Migrate to use MetricsService, update all consumers

#### 3.4.4 Classes That Need Metrics Recording Upgrades

**Classes that currently use direct metric recording and need to upgrade to MetricsService:**

**Agent Domain:**
- [ ] `AgentModelRegistry` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentModelCostEvaluation` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentModelOptimization` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentModelEvaluation` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentModelContextCache` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentModelRegistryMetrics` - Already migrated, verify implementation
- [ ] `AgentModelPerformanceMetrics` - Remove class (see 3.4.1)
- [ ] `AgentModelOptimizationStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `AgentModelStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `AgentModelStatisticsAggregatorService` - Update to use MetricsService for aggregation
- [ ] `AgentPersistenceManager` - Already migrated, verify implementation
- [ ] `AgentCommunicationPerformanceMonitor` - Already migrated, verify implementation
- [ ] `AgentCoordinationManager` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentSharedContextManager` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentSkillExecutor` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentServlet` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `AgentServerStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `AgentStatistics` - Remove class (see 3.4.2)
- [ ] `AgentSecurityStatistics` - Migrate to use MetricsService (see 3.4.3)

**Tool Domain:**
- [ ] `ToolExecutionService` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `ToolMetrics` - Already migrated, verify implementation
- [ ] `ProviderMetrics` - Already migrated, verify implementation
- [ ] `ToolLoggingManager` - Already migrated, verify implementation
- [ ] `ToolMetricsEndpoint` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `ToolServer` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `DefaultToolServer` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `ToolStatistics` - Remove class (see 3.4.2)
- [ ] `ProviderStatistics` - Remove class (see 3.4.2)
- [ ] `ServerStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `TransportStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `FilterStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `ToolSecurityStatistics` - Migrate to use MetricsService (see 3.4.3)

**Events Domain:**
- [ ] `EventProcessingAnalytics` - Already migrated, verify implementation
- [ ] `EventProcessingStatistics` - Remove class (see 3.4.2)
- [ ] `EventSystemIntegration` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `EventFilter` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `EventBusStatistics` - Migrate to use MetricsService (see 3.4.3)

**Reasoning Domain:**
- [ ] `ReasoningStepStorageStatistics` - Remove class (see 3.4.2)
- [ ] `ReasoningAnalysisStatistics` - Remove class (see 3.4.2)
- [ ] `ReasoningEngine` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `ReasoningStepStorage` - Add `@Reference MetricsService`, replace direct metric recording

**Action Domain:**
- [ ] `ActionExecutionService` - Add `@Reference MetricsService`, replace direct metric recording
- [ ] `ActionPerformanceMetrics` - Remove class (see 3.4.1)
- [ ] `ContextPerformanceMetrics` - Remove class (see 3.4.1)
- [ ] `GetLogStatisticsAction` - Update to use MetricsService for statistics generation
- [ ] `GetItemStatisticsAction` - Update to use MetricsService for statistics generation
- [ ] `GetPersistenceStatisticsAction` - Update to use MetricsService for statistics generation
- [ ] `GetRuleStatisticsAction` - Update to use MetricsService for statistics generation
- [ ] `GetNetworkStatisticsAction` - Update to use MetricsService for statistics generation
- [ ] `SystemDiagnosticsAction` - Update to use MetricsService for statistics generation

**Common/Security Domain:**
- [ ] `ErrorRecoveryStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `MessageSecurityStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `AgentSecurityStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `SecurityStatisticsTest` - Update test to use MetricsService
- [ ] `MonitoringStatistics` - Migrate to use MetricsService (see 3.4.3)

**Stub Domain:**
- [ ] `StubServiceStatistics` - Already migrated, verify implementation
- [ ] `StubStatistics` - Migrate to use MetricsService (see 3.4.3)

**Transport Domain:**
- [ ] `TransportStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `LifecycleStatistics` - Migrate to use MetricsService (see 3.4.3)

**Communication Domain:**
- [ ] `ConversationStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `MessagingStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `CoordinationStatistics` - Migrate to use MetricsService (see 3.4.3)
- [ ] `ConflictResolutionStatistics` - Migrate to use MetricsService (see 3.4.3)

#### 3.4.5 New Classes to Create

**New snapshot and statistics classes to implement:**

**Snapshot Classes:**
- [ ] `ModelCompletionSnapshot` - Implement with CountsMetrics, LatencyMetrics, ModelMetrics
- [ ] `ToolFileReadSnapshot` - Implement with CountsMetrics, LatencyMetrics, ToolMetrics
- [ ] `AgentTaskSnapshot` - Implement with CountsMetrics, LatencyMetrics, AgentMetrics
- [ ] `ActionExecutionSnapshot` - Implement with CountsMetrics, LatencyMetrics, ActionMetrics
- [ ] `MemoryUsageSnapshot` - Implement with CountsMetrics, LatencyMetrics, MemoryMetrics
- [ ] `InputProcessingSnapshot` - Implement with CountsMetrics, LatencyMetrics, InputMetrics
- [ ] `OrchestrationSnapshot` - Implement with CountsMetrics, LatencyMetrics, OrchestrationMetrics
- [ ] `AutonomousBehaviorSnapshot` - Implement with CountsMetrics, LatencyMetrics, AutonomousMetrics
- [ ] `SafetyMonitoringSnapshot` - Implement with CountsMetrics, LatencyMetrics, SafetyMetrics
- [ ] `LearningProgressSnapshot` - Implement with CountsMetrics, LatencyMetrics, LearningMetrics
- [ ] `ConfigurationSnapshot` - Implement with CountsMetrics, LatencyMetrics, ConfigurationMetrics
- [ ] `ContextProcessingSnapshot` - Implement with CountsMetrics, LatencyMetrics, ContextMetrics
- [ ] `EventProcessingSnapshot` - Implement with CountsMetrics, LatencyMetrics, EventMetrics
- [ ] `ReasoningExecutionSnapshot` - Implement with CountsMetrics, LatencyMetrics, ReasoningMetrics
- [ ] `CommunicationSnapshot` - Implement with CountsMetrics, LatencyMetrics, CommunicationMetrics
- [ ] `SecurityMonitoringSnapshot` - Implement with CountsMetrics, LatencyMetrics, SecurityMetrics
- [ ] `TransportSnapshot` - Implement with CountsMetrics, LatencyMetrics, TransportMetrics
- [ ] `PersistenceSnapshot` - Implement with CountsMetrics, LatencyMetrics, PersistenceMetrics
- [ ] `ValidationSnapshot` - Implement with CountsMetrics, LatencyMetrics, ValidationMetrics
- [ ] `IntegrationSnapshot` - Implement with CountsMetrics, LatencyMetrics, IntegrationMetrics
- [ ] `OptimizationSnapshot` - Implement with CountsMetrics, LatencyMetrics, OptimizationMetrics
- [ ] `CollaborationSnapshot` - Implement with CountsMetrics, LatencyMetrics, CollaborationMetrics
- [ ] `CoordinationSnapshot` - Implement with CountsMetrics, LatencyMetrics, CoordinationMetrics
- [ ] `ConflictResolutionSnapshot` - Implement with CountsMetrics, LatencyMetrics, ConflictMetrics
- [ ] `ConversationSnapshot` - Implement with CountsMetrics, LatencyMetrics, ConversationMetrics
- [ ] `MessagingSnapshot` - Implement with CountsMetrics, LatencyMetrics, MessagingMetrics

**Statistics Classes:**
- [ ] `ModelCompletionStatistics` - Implement with TrendMetrics, PercentileMetrics, BusinessMetrics
- [ ] `ToolPerformanceStatistics` - Implement with TrendMetrics, PercentileMetrics, EfficiencyMetrics
- [ ] `AgentBehaviorStatistics` - Implement with TrendMetrics, PercentileMetrics, IntelligenceMetrics
- [ ] `ActionExecutionStatistics` - Implement with TrendMetrics, PercentileMetrics, EfficiencyMetrics
- [ ] `MemoryUsageStatistics` - Implement with TrendMetrics, PercentileMetrics, ResourceMetrics
- [ ] `InputProcessingStatistics` - Implement with TrendMetrics, PercentileMetrics, ProcessingMetrics
- [ ] `OrchestrationStatistics` - Implement with TrendMetrics, PercentileMetrics, CoordinationMetrics
- [ ] `AutonomousBehaviorStatistics` - Implement with TrendMetrics, PercentileMetrics, IntelligenceMetrics
- [ ] `SafetyMonitoringStatistics` - Implement with TrendMetrics, PercentileMetrics, SafetyMetrics
- [ ] `LearningProgressStatistics` - Implement with TrendMetrics, PercentileMetrics, LearningMetrics
- [ ] `ConfigurationStatistics` - Implement with TrendMetrics, PercentileMetrics, ConfigurationMetrics
- [ ] `ContextProcessingStatistics` - Implement with TrendMetrics, PercentileMetrics, ContextMetrics
- [ ] `EventProcessingStatistics` - Implement with TrendMetrics, PercentileMetrics, EventMetrics
- [ ] `ReasoningExecutionStatistics` - Implement with TrendMetrics, PercentileMetrics, ReasoningMetrics
- [ ] `CommunicationStatistics` - Implement with TrendMetrics, PercentileMetrics, CommunicationMetrics
- [ ] `SecurityMonitoringStatistics` - Implement with TrendMetrics, PercentileMetrics, SecurityMetrics
- [ ] `TransportStatistics` - Implement with TrendMetrics, PercentileMetrics, TransportMetrics
- [ ] `PersistenceStatistics` - Implement with TrendMetrics, PercentileMetrics, PersistenceMetrics
- [ ] `ValidationStatistics` - Implement with TrendMetrics, PercentileMetrics, ValidationMetrics
- [ ] `IntegrationStatistics` - Implement with TrendMetrics, PercentileMetrics, IntegrationMetrics
- [ ] `OptimizationStatistics` - Implement with TrendMetrics, PercentileMetrics, OptimizationMetrics
- [ ] `CollaborationStatistics` - Implement with TrendMetrics, PercentileMetrics, CollaborationMetrics
- [ ] `CoordinationStatistics` - Implement with TrendMetrics, PercentileMetrics, CoordinationMetrics
- [ ] `ConflictResolutionStatistics` - Implement with TrendMetrics, PercentileMetrics, ConflictMetrics
- [ ] `ConversationStatistics` - Implement with TrendMetrics, PercentileMetrics, ConversationMetrics
- [ ] `MessagingStatistics` - Implement with TrendMetrics, PercentileMetrics, MessagingMetrics

**Core Infrastructure Classes:**
- [ ] `MetricsService` - Interface definition
- [ ] `MetricsServiceImpl` - OSGi service implementation
- [ ] `OperationRecorder` - Builder pattern implementation
- [ ] `StatisticsSnapshot` - Marker interface
- [ ] `DomainAggregatedSnapshot` - Aggregated statistics across domains

#### 3.4.6 Testing Requirements

**Test classes to create/update:**

**Unit Tests:**
- [ ] `MetricsServiceTest` - Test MetricsService functionality
- [ ] `OperationRecorderTest` - Test builder pattern
- [ ] `ModelCompletionSnapshotTest` - Test snapshot implementation
- [ ] `AgentBehaviorStatisticsTest` - Test statistics implementation
- [ ] `CapabilityInterfacesTest` - Test all capability interfaces
- [ ] `MetricsExporterTest` - Test export functionality

**Integration Tests:**
- [ ] `MetricsServiceIntegrationTest` - Test OSGi integration
- [ ] `MetricsMigrationTest` - Test migration from old to new system
- [ ] `StatisticsComputationTest` - Test statistics computation from metrics
- [ ] `TimeRangeStatisticsTest` - Test time-range based statistics

**Performance Tests:**
- [ ] `MetricsServicePerformanceTest` - Benchmark performance
- [ ] `BuilderPatternPerformanceTest` - Test builder overhead
- [ ] `SnapshotCreationPerformanceTest` - Test snapshot creation speed
- [ ] `StatisticsComputationPerformanceTest` - Test statistics computation speed

**Migration Tests:**
- [ ] `AbstractMetricsMigrationTest` - Test migration from AbstractMetrics classes
- [ ] `AbstractStatisticsMigrationTest` - Test migration from AbstractStatistics classes
- [ ] `DirectMetricsRecordingMigrationTest` - Test migration from direct recording

#### 3.4.7 Documentation Requirements

**Documentation to create/update:**

- [ ] `METRICS_MIGRATION_GUIDE.md` - Step-by-step migration guide
- [ ] `METRICS_USAGE_GUIDE.md` - How to use the new MetricsService
- [ ] `STATISTICS_USAGE_GUIDE.md` - How to use statistics features
- [ ] `CAPABILITY_INTERFACES_GUIDE.md` - Guide to capability interfaces
- [ ] `BUILDER_PATTERN_GUIDE.md` - Guide to builder pattern usage
- [ ] `METRICS_EXPORT_GUIDE.md` - Guide to metrics export features
- [ ] Update all existing class documentation to reflect new patterns
- [ ] Update API documentation for all new interfaces and classes

### Phase 4: Advanced Features (Week 4-5)

#### 4.1 Implement Aggregation and Filtering

**Tasks**:
- [ ] Add aggregation methods to MetricsService
- [ ] Implement time-based filtering
- [ ] Add domain-specific aggregation
- [ ] Create aggregation snapshots
- [ ] Add unit tests for aggregation

**Deliverables**:
```java
public interface MetricsService {
    // ... existing methods ...
    
    /**
     * Get aggregated metrics across multiple operations
     */
    <T extends MetricsSnapshot> List<T> getAggregatedSnapshots(String domain, Class<T> snapshotType);
    
    /**
     * Get metrics within time range
     */
    <T extends MetricsSnapshot> List<T> getSnapshotsInRange(String domain, String operation, 
        Class<T> snapshotType, Instant start, Instant end);
    
    /**
     * Get domain-wide aggregated metrics
     */
    DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain);
}
```

#### 4.2 Implement Metrics Export

**Tasks**:
- [ ] Create Prometheus exporter for snapshots
- [ ] Create JSON exporter for REST APIs
- [ ] Create JMX exporter for monitoring
- [ ] Add filtering and formatting options
- [ ] Create unit tests for exporters

**Deliverables**:
```java
@Component(service = MetricsExporter.class)
@NonNullByDefault
public interface MetricsExporter {
    
    /**
     * Export metrics to Prometheus format
     */
    String exportPrometheus(MetricsService metricsService);
    
    /**
     * Export metrics to JSON format
     */
    String exportJSON(MetricsService metricsService);
    
    /**
     * Export metrics to JMX
     */
    void exportJMX(MetricsService metricsService);
}
```

### Phase 5: Testing and Validation (Week 5-6)

#### 5.1 Comprehensive Testing

**Tasks**:
- [ ] Create unit tests for MetricsService
- [ ] Test builder pattern functionality
- [ ] Test snapshot capability interfaces
- [ ] Test operation-specific data recording
- [ ] Test aggregation and filtering
- [ ] Test exporters

**Deliverables**:
```java
@Test
public void testMetricsServiceBuilderPattern() {
    // Test builder pattern
    metricsService.recordOperation("model", "gpt4:completion")
        .withSuccess(true)
        .withDuration(50000000L)
        .withData("tokens", 150)
        .withData("cost", 0.002)
        .record();
    
    // Test snapshot retrieval
    ModelCompletionSnapshot snapshot = metricsService.getModelSnapshot("gpt4", "completion");
    
    assertThat(snapshot.totalCount()).isEqualTo(1);
    assertThat(snapshot.successCount()).isEqualTo(1);
    assertThat(snapshot.tokensPerSecond()).isGreaterThan(0);
    assertThat(snapshot.costPerRequest()).isEqualTo(0.002);
}

@Test
public void testCapabilityInterfaces() {
    ModelCompletionSnapshot snapshot = metricsService.getModelSnapshot("gpt4", "completion");
    
    // Test CountsMetrics
    assertThat(snapshot.totalCount()).isEqualTo(1);
    assertThat(snapshot.successRate()).isEqualTo(100.0);
    
    // Test LatencyMetrics
    assertThat(snapshot.averageMs()).isGreaterThan(0);
    assertThat(snapshot.operationsPerSecond()).isGreaterThan(0);
    
    // Test ModelMetrics
    assertThat(snapshot.tokensPerSecond()).isGreaterThan(0);
    assertThat(snapshot.costPerRequest()).isGreaterThan(0);
}
```

#### 5.2 Performance Testing

**Tasks**:
- [ ] Benchmark MetricsService performance
- [ ] Test builder pattern overhead
- [ ] Test snapshot creation performance
- [ ] Test concurrent access patterns
- [ ] Validate memory usage

**Deliverables**:
- Performance benchmarks
- Memory usage analysis
- Concurrency performance validation

## Success Criteria

### Functional Requirements

- [ ] Single centralized MetricsService operational
- [ ] Builder pattern supports flexible data recording
- [ ] Operation-specific snapshots with capability interfaces
- [ ] No duplicate or wrapper methods
- [ ] All existing metrics functionality preserved
- [ ] Aggregation and filtering operational
- [ ] Export functionality (Prometheus, JSON, JMX) working

### Performance Requirements

- [ ] No performance regression in metrics collection
- [ ] Builder pattern overhead < 1% of operation time
- [ ] Snapshot creation < 1ms per snapshot
- [ ] Memory usage optimized for high-frequency operations
- [ ] Thread-safe concurrent access

### Quality Requirements

- [ ] 100% test coverage for MetricsService
- [ ] All capability interfaces properly implemented
- [ ] No memory leaks in builder pattern
- [ ] Proper error handling throughout
- [ ] Comprehensive documentation

## Risk Mitigation

### Technical Risks

1. **Builder Pattern Complexity**
   - **Risk**: Builder pattern adds complexity
   - **Mitigation**: Keep builder simple, provide convenience methods

2. **Snapshot Type Safety**
   - **Risk**: Type casting issues with generic snapshots
   - **Mitigation**: Use type-specific methods, avoid generic casting

3. **Migration Complexity**
   - **Risk**: Difficult migration from existing metrics
   - **Mitigation**: Gradual migration, parallel implementation

### Integration Risks

1. **OSGi Service Dependencies**
   - **Risk**: MetricsService dependency issues
   - **Mitigation**: Proper OSGi testing, fallback mechanisms

2. **Data Storage**
   - **Risk**: Memory usage with operation-specific data
   - **Mitigation**: Implement data retention policies, cleanup mechanisms

## Maintenance and Evolution

### Ongoing Maintenance

- [ ] Monitor MetricsService performance
- [ ] Update capability interfaces as needed
- [ ] Maintain builder pattern simplicity
- [ ] Update documentation
- [ ] Collect user feedback

### Future Enhancements

- [ ] Add new operation types and snapshots
- [ ] Implement metrics retention policies
- [ ] Add advanced aggregation features
- [ ] Implement metrics compression
- [ ] Add custom capability interfaces

## Conclusion

This refactoring establishes a **clean, centralized metrics architecture** with:

- **Single MetricsService**: One place for all metrics collection
- **Builder Pattern**: Flexible data recording for any operation type
- **Capability Interfaces**: Type-safe snapshot access without wrapper methods
- **Operation-Specific Snapshots**: Domain-relevant metrics and methods
- **No Duplication**: Eliminates redundant classes and methods

The architecture provides a **scalable, maintainable, and extensible** metrics system that supports the diverse needs of the openHAB AI bundle while maintaining simplicity and performance.
