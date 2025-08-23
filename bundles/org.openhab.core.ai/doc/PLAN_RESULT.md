# Result Classes Refactoring Action Plan

## Overview

This document outlines a comprehensive plan for refactoring all Result classes in the openHAB AI bundle to use the improved `BaseResult` class. The refactoring will standardize Result class patterns, reduce code duplication, and improve maintainability.

## Current State Analysis

### Result Class Categories

Based on the codebase analysis, Result classes fall into the following categories:

#### 1. **Validation Results** (Already Well-Structured)
- **Hierarchy**: `ValidationResult` interface → `BaseValidationResult` → Specific validation classes
- **Classes**: `ToolValidationResult`, `InputValidationResult`, `ReasoningValidationResult`, etc.
- **Status**: ✅ Already well-structured, minimal changes needed

#### 2. **Tool-Related Results** (High Priority for Refactoring)
- **Classes**: `ToolResult`, `PromptResult`, `CompletionResult`, `ResourceResult`, `ElicitationResult`
- **Pattern**: Standalone classes with common fields (success, message, executionTime, etc.)
- **Priority**: 🔴 **HIGH** - These are core MCP functionality

#### 3. **Reasoning Results** (High Priority for Refactoring)
- **Classes**: `MultiStepReasoningResult`, `ReasoningSessionResult`, `ContextValidationResult`, etc.
- **Pattern**: Complex results with reasoning-specific fields
- **Priority**: 🔴 **HIGH** - Core reasoning functionality

#### 4. **Security & Health Results** (Medium Priority)
- **Classes**: `SecurityResult`, `SystemCheckResult`, `HealthCheckResult`, `AuthenticationResult`
- **Pattern**: Domain-specific success indicators (authenticated, healthy, etc.)
- **Priority**: 🟡 **MEDIUM** - Important but not core

#### 5. **Memory & Learning Results** (Medium Priority)
- **Classes**: `MemorySearchResult`, `MemoryStoreResult`, `LearningResult`, `FeedbackIntegrationResult`
- **Pattern**: Memory and learning-specific operations
- **Priority**: 🟡 **MEDIUM** - Important for AI functionality

#### 6. **Configuration & Policy Results** (Low Priority)
- **Classes**: `ConfigurationResult`, `PolicyResult`, `ConfigurationBackupResult`
- **Pattern**: Configuration and policy management
- **Priority**: 🟢 **LOW** - Administrative functionality

#### 7. **Monitoring & Metrics Results** (Special Case)
- **Classes**: `AgentModelPerformanceMetrics`, `ActionPerformanceMetrics`
- **Pattern**: Extend `AbstractMetrics` (different hierarchy)
- **Status**: ⚠️ **SPECIAL** - Keep existing hierarchy, no BaseResult refactoring

## Refactoring Strategy

### Phase 1: Foundation (Week 1)
**Goal**: Establish BaseResult as the foundation and create migration utilities

#### 1.1 BaseResult Enhancement
- [x] ✅ **COMPLETED**: Enhanced BaseResult with static factory methods and builder pattern
- [ ] Create migration utilities and documentation
- [ ] Add comprehensive unit tests for BaseResult
- [ ] Create example implementations for each category

#### 1.2 Migration Utilities
- [ ] Create `ResultMigrationHelper` utility class
- [ ] Add validation tools for migration consistency
- [ ] Create migration templates for each Result category

### Phase 2: Tool Results Refactoring (Week 2)
**Goal**: Refactor core MCP tool Result classes

#### 2.1 ToolResult Refactoring
```java
// Current: Standalone class
public class ToolResult {
    private final String toolId;
    private final boolean success;
    private final @Nullable Object content;
    // ...
}

// Target: Extends BaseResult
public final class ToolResult extends BaseResult {
    private final String toolId;
    private final @Nullable Object content;
    
    // Constructor calls super() with common fields
    // Add domain-specific fields and methods
}
```

**Files to Refactor**:
- [ ] `src/main/java/org/openhab/core/ai/tool/api/ToolResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/prompts/api/PromptResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/completions/api/CompletionResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/resources/api/ResourceResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/elicitation/input/ElicitationResult.java`

#### 2.2 Update Tool Adapters
- [ ] Update all tool adapters to use new ToolResult constructors
- [ ] Update tests to use new factory methods
- [ ] Verify MCP protocol compatibility

### Phase 3: Reasoning Results Refactoring (Week 3)
**Goal**: Refactor reasoning-related Result classes

#### 3.1 MultiStepReasoningResult Refactoring
```java
// Current: Complex standalone class with builder
public class MultiStepReasoningResult {
    private final String reasoningId;
    private final boolean successful;
    // ... many fields
}

// Target: Extends BaseResult with domain-specific fields
public final class MultiStepReasoningResult extends BaseResult {
    private final String reasoningId;
    private final String sessionId;
    private final boolean completed;
    private final String finalAnswer;
    // ... domain-specific fields only
}
```

**Files to Refactor**:
- [ ] `src/main/java/org/openhab/core/ai/reasoning/api/MultiStepReasoningResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/session/ReasoningSessionResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/validation/ContextValidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/memory/MemorySearchResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/memory/MemoryStoreResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/events/EventProcessingResult.java`

#### 3.2 Update Reasoning Services
- [ ] Update reasoning engine to use new Result constructors
- [ ] Update memory services
- [ ] Update event processing services

### Phase 4: Security & Health Results (Week 4)
**Goal**: Refactor security and health-related Result classes

#### 4.1 SecurityResult Refactoring
```java
// Current: Domain-specific success indicator
public class SecurityResult {
    private final boolean authenticated;
    private final String userId;
    // ...
}

// Target: Extends BaseResult with domain-specific fields
public final class SecurityResult extends BaseResult {
    private final boolean authenticated; // Keep domain-specific indicator
    private final String userId;
    private final String username;
    // Remove common fields (success, message, timestamp) - use BaseResult
}
```

**Files to Refactor**:
- [ ] `src/main/java/org/openhab/core/ai/tool/security/filters/SecurityResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/monitoring/health/SystemCheckResult.java`
- [ ] `src/main/java/org/openhab/core/ai/tool/monitoring/HealthCheckResult.java`
- [ ] `src/main/java/org/openhab/core/ai/agent/infrastructure/security/AuthenticationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/agent/infrastructure/security/AuthorizationResult.java`

### Phase 5: Memory & Learning Results (Week 5)
**Goal**: Refactor memory and learning-related Result classes

**Files to Refactor**:
- [ ] `src/main/java/org/openhab/core/ai/reasoning/memory/AgentMemoryStoreResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/memory/AgentMemoryConsolidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/memory/MemoryConsolidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/learning/FeedbackIntegrationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/results/LearningResult.java`

### Phase 6: Configuration & Policy Results (Week 6)
**Goal**: Refactor configuration and policy-related Result classes

**Files to Refactor**:
- [ ] `src/main/java/org/openhab/core/ai/reasoning/config/ConfigurationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/config/api/ConfigurationBackupResult.java`
- [ ] `src/main/java/org/openhab/core/ai/reasoning/policies/PolicyResult.java`
- [ ] `src/main/java/org/openhab/core/ai/agent/lifecycle/AgentRegistrationResult.java`

### Phase 7: Validation Results Enhancement (Week 7)
**Goal**: Enhance existing validation results to use BaseResult patterns

#### 7.1 Validation Results Enhancement
- [ ] Review existing `BaseValidationResult` for BaseResult compatibility
- [ ] Add static factory methods to validation results
- [ ] Enhance validation result builders
- [ ] Ensure consistency with BaseResult patterns

**Files to Enhance**:
- [ ] `src/main/java/org/openhab/core/ai/common/validation/BaseValidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/common/validation/ToolValidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/common/validation/InputValidationResult.java`
- [ ] `src/main/java/org/openhab/core/ai/common/validation/ReasoningValidationResult.java`

### Phase 8: Testing & Validation (Week 8)
**Goal**: Comprehensive testing and validation of refactored Result classes

#### 8.1 Unit Testing
- [ ] Create unit tests for all refactored Result classes
- [ ] Test BaseResult inheritance and functionality
- [ ] Test static factory methods
- [ ] Test builder patterns
- [ ] Test serialization/deserialization

#### 8.2 Integration Testing
- [ ] Test Result classes in MCP protocol context
- [ ] Test Result classes in A2A protocol context
- [ ] Test Result classes in reasoning engine
- [ ] Test Result classes in memory systems

#### 8.3 Performance Testing
- [ ] Benchmark Result class creation performance
- [ ] Test memory usage of refactored classes
- [ ] Verify no performance regressions

## Detailed Refactoring Guidelines

### 1. Common Refactoring Pattern

For each Result class, follow this pattern:

```java
// BEFORE: Standalone class
public class ExampleResult {
    private final String resultId;
    private final boolean success;
    private final String message;
    private final long executionTimeMs;
    private final Instant timestamp;
    // ... domain-specific fields
    
    // Constructor, getters, static factories
}

// AFTER: Extends BaseResult
public final class ExampleResult extends BaseResult {
    // Only domain-specific fields (remove common fields)
    private final String domainSpecificField;
    private final @Nullable Object domainData;
    
    // Constructor calls super() with common fields
    private ExampleResult(String resultId, boolean success, String message, 
                         @Nullable String errorMessage, long executionTimeMs, 
                         Instant timestamp, @Nullable Map<String, Object> metadata,
                         String domainSpecificField, @Nullable Object domainData) {
        super(resultId, "example", success, message, errorMessage, 
              executionTimeMs, timestamp, metadata);
        this.domainSpecificField = domainSpecificField;
        this.domainData = domainData;
    }
    
    // Static factory methods using BaseResult.success()/error()
    public static ExampleResult success(String message, String domainField, 
                                       @Nullable Object data, long executionTimeMs) {
        return new Builder()
            .success(true)
            .message(message)
            .executionTimeMs(executionTimeMs)
            .domainSpecificField(domainField)
            .domainData(data)
            .build();
    }
    
    // Domain-specific getters only
    public String getDomainSpecificField() { return domainSpecificField; }
    public @Nullable Object getDomainData() { return domainData; }
    
    // Builder class
    public static final class Builder extends BaseResult.Builder {
        private String domainSpecificField = "";
        private @Nullable Object domainData = null;
        
        // Domain-specific builder methods
        public Builder domainSpecificField(String field) {
            this.domainSpecificField = field;
            return this;
        }
        
        public Builder domainData(@Nullable Object data) {
            this.domainData = data;
            return this;
        }
        
        @Override
        public ExampleResult build() {
            return new ExampleResult(resultId, success, message, errorMessage,
                                   executionTimeMs, timestamp, metadata,
                                   domainSpecificField, domainData);
        }
    }
}
```

### 2. Field Mapping Guidelines

#### Common Fields (Move to BaseResult)
- `resultId` → `BaseResult.resultId`
- `success` → `BaseResult.success`
- `message` → `BaseResult.message`
- `errorMessage` → `BaseResult.errorMessage`
- `executionTimeMs` → `BaseResult.executionTimeMs`
- `timestamp` → `BaseResult.timestamp`
- `metadata` → `BaseResult.metadata`

#### Domain-Specific Fields (Keep in subclass)
- `toolId` (ToolResult)
- `content` (ToolResult)
- `reasoningId` (MultiStepReasoningResult)
- `sessionId` (MultiStepReasoningResult)
- `authenticated` (SecurityResult)
- `userId` (SecurityResult)
- `healthy` (SystemCheckResult)

### 3. Method Mapping Guidelines

#### Common Methods (Use BaseResult)
- `isSuccess()` → `BaseResult.isSuccess()`
- `getMessage()` → `BaseResult.getMessage()`
- `getExecutionTimeMs()` → `BaseResult.getExecutionTimeMs()`
- `getTimestamp()` → `BaseResult.getTimestamp()`
- `getMetadata()` → `BaseResult.getMetadata()`

#### Domain-Specific Methods (Keep in subclass)
- `getToolId()` (ToolResult)
- `getContent()` (ToolResult)
- `isAuthenticated()` (SecurityResult)
- `isHealthy()` (SystemCheckResult)
- `getReasoningId()` (MultiStepReasoningResult)

### 4. Static Factory Method Guidelines

#### Standard Factory Methods
```java
// Success with execution time
public static ExampleResult success(String message, long executionTimeMs) {
    return new Builder()
        .success(true)
        .message(message)
        .executionTimeMs(executionTimeMs)
        .build();
}

// Error with execution time
public static ExampleResult error(String message, long executionTimeMs) {
    return new Builder()
        .success(false)
        .message(message)
        .errorMessage(message)
        .executionTimeMs(executionTimeMs)
        .build();
}

// Domain-specific success
public static ExampleResult successWithData(String message, Object data, long executionTimeMs) {
    return new Builder()
        .success(true)
        .message(message)
        .executionTimeMs(executionTimeMs)
        .domainData(data)
        .build();
}
```

## Migration Checklist

### For Each Result Class

#### Pre-Refactoring
- [ ] Analyze current fields and methods
- [ ] Identify common vs domain-specific fields
- [ ] Document current usage patterns
- [ ] Create unit tests for current behavior

#### During Refactoring
- [ ] Extend BaseResult
- [ ] Move common fields to BaseResult constructor
- [ ] Keep domain-specific fields in subclass
- [ ] Update constructors to call super()
- [ ] Add static factory methods
- [ ] Add builder class
- [ ] Update getters to use BaseResult methods where appropriate
- [ ] Maintain backward compatibility where possible

#### Post-Refactoring
- [ ] Update all usages of the Result class
- [ ] Update tests to use new constructors/factories
- [ ] Verify serialization/deserialization works
- [ ] Test performance impact
- [ ] Update documentation

## Risk Mitigation

### 1. Backward Compatibility
- **Risk**: Breaking changes to existing APIs
- **Mitigation**: 
  - Maintain existing constructors where possible
  - Add deprecation annotations for old methods
  - Provide migration guides

### 2. Performance Impact
- **Risk**: Performance degradation from inheritance
- **Mitigation**:
  - Benchmark before and after refactoring
  - Use final classes to enable JVM optimizations
  - Minimize object creation overhead

### 3. Testing Coverage
- **Risk**: Incomplete testing leading to regressions
- **Mitigation**:
  - Comprehensive unit test coverage
  - Integration tests for all Result class usages
  - Performance regression tests

### 4. Protocol Compatibility
- **Risk**: Breaking MCP/A2A protocol compatibility
- **Mitigation**:
  - Test Result serialization/deserialization
  - Verify protocol message formats
  - Maintain existing field names where possible

## Success Criteria

### Quantitative Metrics
- [ ] 100% of eligible Result classes extend BaseResult
- [ ] 0% increase in memory usage
- [ ] <5% performance degradation
- [ ] 100% test coverage for refactored classes
- [ ] 0 breaking changes to public APIs

### Qualitative Metrics
- [ ] Consistent Result class patterns across codebase
- [ ] Reduced code duplication
- [ ] Improved developer experience
- [ ] Better maintainability
- [ ] Clear documentation and examples

## Timeline Summary

| Week | Phase | Focus | Deliverables |
|------|-------|-------|--------------|
| 1 | Foundation | BaseResult enhancement, utilities | Enhanced BaseResult, migration tools |
| 2 | Tool Results | MCP tool Result classes | Refactored ToolResult, PromptResult, etc. |
| 3 | Reasoning Results | Reasoning engine Result classes | Refactored MultiStepReasoningResult, etc. |
| 4 | Security & Health | Security and health Result classes | Refactored SecurityResult, SystemCheckResult |
| 5 | Memory & Learning | Memory and learning Result classes | Refactored MemorySearchResult, LearningResult |
| 6 | Configuration | Configuration and policy Result classes | Refactored ConfigurationResult, PolicyResult |
| 7 | Validation Enhancement | Enhance existing validation results | Enhanced BaseValidationResult |
| 8 | Testing & Validation | Comprehensive testing | Test suite, performance validation |

## Conclusion

This refactoring plan will significantly improve the consistency, maintainability, and developer experience of Result classes in the openHAB AI bundle. By following a phased approach with careful testing and validation, we can achieve these improvements while minimizing risk and maintaining backward compatibility.

The refactored Result classes will provide a solid foundation for future development and make the codebase more accessible to new contributors.


# Result Class Refactoring Quick Reference

## Quick Start Guide

This guide provides a step-by-step process for refactoring any Result class to extend `BaseResult`.

## Step-by-Step Refactoring Process

### Step 1: Analyze Current Class
```java
// BEFORE: Analyze current fields
public class MyResult {
    private final String resultId;        // → Move to BaseResult
    private final boolean success;        // → Move to BaseResult
    private final String message;         // → Move to BaseResult
    private final long executionTimeMs;   // → Move to BaseResult
    private final Instant timestamp;      // → Move to BaseResult
    private final Map<String, Object> metadata; // → Move to BaseResult
    
    // Domain-specific fields (keep these)
    private final String domainField;     // → Keep in subclass
    private final @Nullable Object data;  // → Keep in subclass
}
```

### Step 2: Create New Class Structure
```java
// AFTER: Extend BaseResult
public final class MyResult extends BaseResult {
    // Only domain-specific fields
    private final String domainField;
    private final @Nullable Object data;
    
    // Constructor calls super()
    private MyResult(String resultId, boolean success, String message,
                    @Nullable String errorMessage, long executionTimeMs,
                    Instant timestamp, @Nullable Map<String, Object> metadata,
                    String domainField, @Nullable Object data) {
        super(resultId, "my-result", success, message, errorMessage,
              executionTimeMs, timestamp, metadata);
        this.domainField = domainField;
        this.data = data;
    }
}
```

### Step 3: Add Static Factory Methods
```java
// Standard factory methods
public static MyResult success(String message, String domainField, 
                              @Nullable Object data, long executionTimeMs) {
    return new Builder()
        .success(true)
        .message(message)
        .executionTimeMs(executionTimeMs)
        .domainField(domainField)
        .data(data)
        .build();
}

public static MyResult error(String message, long executionTimeMs) {
    return new Builder()
        .success(false)
        .message(message)
        .errorMessage(message)
        .executionTimeMs(executionTimeMs)
        .build();
}
```

### Step 4: Add Builder Class
```java
public static final class Builder extends BaseResult.Builder {
    private String domainField = "";
    private @Nullable Object data = null;
    
    // Domain-specific builder methods
    public Builder domainField(String field) {
        this.domainField = field;
        return this;
    }
    
    public Builder data(@Nullable Object data) {
        this.data = data;
        return this;
    }
    
    @Override
    public MyResult build() {
        return new MyResult(resultId, success, message, errorMessage,
                           executionTimeMs, timestamp, metadata,
                           domainField, data);
    }
}
```

### Step 5: Update Getters
```java
// Domain-specific getters only
public String getDomainField() { return domainField; }
public @Nullable Object getData() { return data; }

// Common getters use BaseResult
// isSuccess() → BaseResult.isSuccess()
// getMessage() → BaseResult.getMessage()
// getExecutionTimeMs() → BaseResult.getExecutionTimeMs()
// getTimestamp() → BaseResult.getTimestamp()
// getMetadata() → BaseResult.getMetadata()
```

## Field Mapping Reference

### Move to BaseResult (Common Fields)
| Current Field | BaseResult Field | Notes |
|---------------|------------------|-------|
| `resultId` | `BaseResult.resultId` | Unique identifier |
| `success` | `BaseResult.success` | Success indicator |
| `message` | `BaseResult.message` | Human-readable message |
| `errorMessage` | `BaseResult.errorMessage` | Error details |
| `executionTimeMs` | `BaseResult.executionTimeMs` | Execution time |
| `timestamp` | `BaseResult.timestamp` | Result timestamp |
| `metadata` | `BaseResult.metadata` | Additional data |

### Keep in Subclass (Domain-Specific Fields)
| Field Type | Examples | Notes |
|------------|----------|-------|
| **Identifiers** | `toolId`, `reasoningId`, `sessionId` | Domain-specific IDs |
| **Content** | `content`, `data`, `response` | Result payload |
| **Status** | `authenticated`, `healthy`, `completed` | Domain-specific status |
| **Domain Data** | `userId`, `username`, `finalAnswer` | Domain-specific information |

## Method Mapping Reference

### Use BaseResult Methods
| Current Method | BaseResult Method | Notes |
|----------------|-------------------|-------|
| `isSuccess()` | `BaseResult.isSuccess()` | Success check |
| `getMessage()` | `BaseResult.getMessage()` | Get message |
| `getExecutionTimeMs()` | `BaseResult.getExecutionTimeMs()` | Get execution time |
| `getTimestamp()` | `BaseResult.getTimestamp()` | Get timestamp |
| `getMetadata()` | `BaseResult.getMetadata()` | Get metadata |
| `hasError()` | `BaseResult.hasError()` | Check for errors |
| `getSummary()` | `BaseResult.getSummary()` | Get summary |

### Keep in Subclass (Domain-Specific Methods)
| Method Type | Examples | Notes |
|-------------|----------|-------|
| **Getters** | `getToolId()`, `getContent()` | Domain-specific data |
| **Status Checks** | `isAuthenticated()`, `isHealthy()` | Domain-specific status |
| **Domain Logic** | `getReasoningId()`, `getSessionId()` | Domain-specific logic |

## Common Patterns

### 1. Simple Result Class
```java
public final class SimpleResult extends BaseResult {
    private final @Nullable Object data;
    
    private SimpleResult(String resultId, boolean success, String message,
                        @Nullable String errorMessage, long executionTimeMs,
                        Instant timestamp, @Nullable Map<String, Object> metadata,
                        @Nullable Object data) {
        super(resultId, "simple", success, message, errorMessage,
              executionTimeMs, timestamp, metadata);
        this.data = data;
    }
    
    public @Nullable Object getData() { return data; }
    
    public static SimpleResult success(String message, @Nullable Object data, long executionTimeMs) {
        return new Builder()
            .success(true)
            .message(message)
            .executionTimeMs(executionTimeMs)
            .data(data)
            .build();
    }
    
    public static final class Builder extends BaseResult.Builder {
        private @Nullable Object data = null;
        
        public Builder data(@Nullable Object data) {
            this.data = data;
            return this;
        }
        
        @Override
        public SimpleResult build() {
            return new SimpleResult(resultId, success, message, errorMessage,
                                  executionTimeMs, timestamp, metadata, data);
        }
    }
}
```

### 2. Result with Domain-Specific Status
```java
public final class HealthResult extends BaseResult {
    private final boolean healthy;  // Keep domain-specific status
    private final String status;    // Keep domain-specific status
    
    // Constructor...
    
    // Domain-specific status methods
    public boolean isHealthy() { return healthy; }
    public String getStatus() { return status; }
    
    // Static factories
    public static HealthResult healthy(String message) {
        return new Builder()
            .success(true)
            .message(message)
            .healthy(true)
            .status("HEALTHY")
            .build();
    }
    
    public static HealthResult unhealthy(String message) {
        return new Builder()
            .success(false)
            .message(message)
            .healthy(false)
            .status("UNHEALTHY")
            .build();
    }
}
```

### 3. Complex Result with Multiple Domain Fields
```java
public final class ComplexResult extends BaseResult {
    private final String operationId;
    private final String operationType;
    private final @Nullable Object resultData;
    private final Map<String, Object> operationDetails;
    
    // Constructor...
    
    // Domain-specific getters
    public String getOperationId() { return operationId; }
    public String getOperationType() { return operationType; }
    public @Nullable Object getResultData() { return resultData; }
    public Map<String, Object> getOperationDetails() { return operationDetails; }
    
    // Static factories for common cases
    public static ComplexResult success(String message, String operationId,
                                      String operationType, @Nullable Object data,
                                      long executionTimeMs) {
        return new Builder()
            .success(true)
            .message(message)
            .executionTimeMs(executionTimeMs)
            .operationId(operationId)
            .operationType(operationType)
            .resultData(data)
            .build();
    }
}
```

## Testing Checklist

### Unit Tests
- [ ] Test static factory methods
- [ ] Test builder pattern
- [ ] Test BaseResult inheritance
- [ ] Test domain-specific methods
- [ ] Test serialization/deserialization
- [ ] Test equals/hashCode/toString

### Integration Tests
- [ ] Test in actual usage context
- [ ] Test with existing services
- [ ] Test protocol compatibility (MCP/A2A)
- [ ] Test performance impact

## Common Pitfalls

### ❌ Don't Do This
```java
// Don't duplicate BaseResult fields
public final class BadResult extends BaseResult {
    private final boolean success;  // ❌ Duplicate of BaseResult.success
    private final String message;   // ❌ Duplicate of BaseResult.message
}

// Don't forget to call super()
public final class BadResult extends BaseResult {
    public BadResult() {
        // ❌ Missing super() call
    }
}

// Don't expose mutable data
public final class BadResult extends BaseResult {
    public Map<String, Object> getMetadata() {
        return metadata; // ❌ Exposes mutable map
    }
}
```

### ✅ Do This Instead
```java
// Only domain-specific fields
public final class GoodResult extends BaseResult {
    private final String domainField; // ✅ Only domain-specific
}

// Always call super()
public final class GoodResult extends BaseResult {
    public GoodResult(...) {
        super(resultId, "good", success, message, errorMessage,
              executionTimeMs, timestamp, metadata); // ✅ Call super()
    }
}

// Return defensive copies
public final class GoodResult extends BaseResult {
    public Map<String, Object> getMetadata() {
        return Map.copyOf(metadata); // ✅ Defensive copy
    }
}
```

## Migration Tools

### ResultMigrationHelper
```java
// Utility for migration validation
public class ResultMigrationHelper {
    public static boolean validateMigration(Class<?> resultClass) {
        // Check if class extends BaseResult
        // Validate field mapping
        // Check for common patterns
        return true;
    }
}
```

## Support

For questions or issues during refactoring:
1. Check this quick reference guide
2. Review the detailed action plan in `RESULT_CLASSES_REFACTORING_PLAN.md`
3. Look at existing examples in the codebase
4. Create unit tests to validate your refactoring

