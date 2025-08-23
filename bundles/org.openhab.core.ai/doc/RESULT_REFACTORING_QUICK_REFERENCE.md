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
