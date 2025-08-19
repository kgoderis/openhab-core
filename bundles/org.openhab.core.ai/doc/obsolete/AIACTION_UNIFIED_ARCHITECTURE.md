# Action Unified Architecture Proposal

## Overview

This document proposes a unified `Action` interface that serves as the common foundation for both MCP tools and A2A skills, enabling code reuse and consistent behavior across AI protocols in openHAB.

## Table of Contents

1. [Current State Analysis](#current-state-analysis)
2. [Commonalities and Differences](#commonalities-and-differences)
3. [Action Interface Design](#Action-interface-design)
4. [Adapter Architecture](#adapter-architecture)
5. [Implementation Strategy](#implementation-strategy)
6. [Benefits and Trade-offs](#benefits-and-trade-offs)
7. [Migration Path](#migration-path)

---

## Current State Analysis

### MCP Tool Interface
```java
public interface MCPTool {
    String getToolId();
    String getToolName();
    String getDescription();
    Map<String, Object> getSchema();
    MCPToolValidationResult validateParameters(Map<String, Object> parameters);
    MCPToolResult execute(Map<String, Object> parameters, MCPToolContext context) throws MCPToolException;
    MCPToolMetadata getMetadata();
}
```

### A2A Agent Interface
```java
public interface A2AAgent {
    String getAgentId();
    String getAgentName();
    Map<String, Object> getCapabilities();
    void receiveMessage(A2AMessage message) throws Exception;
    boolean isActive();
    long getLastActivity();
}
```

### Key Observations

1. **MCP Tools**: Focus on stateless, synchronous tool execution with parameter validation
2. **A2A Agents**: Focus on stateful, message-based communication with capabilities discovery
3. **Common Patterns**: Both need identification, description, and execution capabilities
4. **Different Contexts**: MCP uses tool context, A2A uses message-based communication

---

## Commonalities and Differences

### 🔄 **COMMON ELEMENTS**

| **Aspect** | **MCP Tool** | **A2A Skill** | **Common Pattern** |
|------------|--------------|---------------|-------------------|
| **Identification** | `getToolId()` | `getSkillId()` | Unique identifier |
| **Naming** | `getToolName()` | `getSkillName()` | Human-readable name |
| **Description** | `getDescription()` | `getDescription()` | Detailed description |
| **Schema** | `getSchema()` | `getSchema()` | Parameter specification |
| **Validation** | `validateParameters()` | `validateParameters()` | Input validation |
| **Execution** | `execute()` | `execute()` | Core functionality |
| **Metadata** | `getMetadata()` | `getMetadata()` | Version, author, etc. |
| **Context** | `MCPToolContext` | `A2ASkillContext` | Execution context |

### 🔀 **DIFFERENCES**

| **Aspect** | **MCP Tool** | **A2A Skill** | **Rationale** |
|------------|--------------|---------------|---------------|
| **Execution Model** | Synchronous | Async-capable | MCP: Simple, A2A: Complex |
| **State Management** | Stateless | Stateful | MCP: Request/response, A2A: Session-based |
| **Communication** | Direct call | Message-based | MCP: Function call, A2A: Protocol messages |
| **Priority** | None | Priority levels | MCP: Simple, A2A: Multi-agent coordination |
| **Correlation** | None | Correlation IDs | MCP: Direct, A2A: Async tracking |
| **Broadcasting** | None | Broadcast support | MCP: Single client, A2A: Multi-agent |
| **Discovery** | Registry-based | Capability-based | MCP: Tool registry, A2A: Agent capabilities |

---

## Action Interface Design

### Core Action Interface

```java
package org.openhab.core.ai.api.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Unified interface for AI actions that can be executed by both MCP and A2A protocols.
 * This serves as the common foundation for all AI capabilities in openHAB.
 */
public interface Action {
    
    // ===== CORE IDENTIFICATION =====
    
    /**
     * Get the unique identifier for this action.
     * Format: "protocol.category.action" (e.g., "openhab.items.list", "openhab.security.arm")
     */
    String getActionId();
    
    /**
     * Get the human-readable name for this action.
     */
    String getActionName();
    
    /**
     * Get a detailed description of what this action does.
     */
    String getDescription();
    
    /**
     * Get the action category (e.g., "items", "things", "security", "automation").
     */
    String getCategory();
    
    /**
     * Get the action version.
     */
    String getVersion();
    
    // ===== SCHEMA AND VALIDATION =====
    
    /**
     * Get the JSON schema for action parameters.
     */
    Map<String, Object> getParameterSchema();
    
    /**
     * Validate action parameters before execution.
     */
    ActionValidationResult validateParameters(Map<String, Object> parameters);
    
    /**
     * Get the return schema for action results.
     */
    Map<String, Object> getReturnSchema();
    
    // ===== EXECUTION =====
    
    /**
     * Execute the action synchronously.
     * This is the primary execution method for MCP tools.
     */
    ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException;
    
    /**
     * Execute the action asynchronously.
     * This is the primary execution method for A2A skills.
     */
    CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context);
    
    // ===== METADATA =====
    
    /**
     * Get comprehensive metadata about this action.
     */
    ActionMetadata getMetadata();
    
    /**
     * Get action capabilities and features.
     */
    Map<String, Object> getCapabilities();
    
    // ===== LIFECYCLE =====
    
    /**
     * Initialize the action with context.
     */
    void initialize(ActionContext context);
    
    /**
     * Clean up resources when the action is no longer needed.
     */
    void cleanup();
    
    /**
     * Check if the action is ready for execution.
     */
    boolean isReady();
}
```

### Supporting Classes

#### ActionContext
```java
package org.openhab.core.ai.api.action;

import java.util.Map;
import java.util.Optional;

/**
 * Context for AI action execution, providing access to openHAB services and protocol-specific information.
 */
public class ActionContext {
    
    // Protocol identification
    private final String protocol; // "mcp" or "a2a"
    private final String clientId;
    private final String sessionId;
    
    // Authentication and authorization
    private final AIAuthenticationContext authContext;
    
    // Protocol-specific context
    private final Map<String, Object> protocolContext;
    
    // Execution metadata
    private final long executionStartTime;
    private final String correlationId;
    private final A2APriority priority; // For A2A protocol
    
    // Constructor and getters...
    
    public String getProtocol() { return protocol; }
    public String getClientId() { return clientId; }
    public AIAuthenticationContext getAuthContext() { return authContext; }
    public Optional<A2APriority> getPriority() { return Optional.ofNullable(priority); }
    public String getCorrelationId() { return correlationId; }
}
```

#### ActionResult
```java
package org.openhab.core.ai.api.action;

import java.time.Instant;
import java.util.Map;

/**
 * Result of AI action execution.
 */
public class ActionResult {
    
    private final boolean success;
    private final Object data;
    private final String message;
    private final ActionError error;
    private final long executionTimeMs;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    
    // Constructors for success and error cases
    public static ActionResult success(Object data, long executionTimeMs) {
        return new ActionResult(true, data, "Success", null, executionTimeMs, Instant.now(), Map.of());
    }
    
    public static ActionResult error(String message, ActionError error, long executionTimeMs) {
        return new ActionResult(false, null, message, error, executionTimeMs, Instant.now(), Map.of());
    }
    
    // Getters...
}
```

#### ActionValidationResult
```java
package org.openhab.core.ai.api.action;

import java.util.List;
import java.util.Map;

/**
 * Result of parameter validation.
 */
public class ActionValidationResult {
    
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> sanitizedParameters;
    
    // Constructors and methods...
}
```

#### ActionMetadata
```java
package org.openhab.core.ai.api.action;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive metadata about an AI action.
 */
public class ActionMetadata {
    
    private final String version;
    private final String author;
    private final String description;
    private final List<String> tags;
    private final Map<String, Object> properties;
    private final Instant created;
    private final Instant lastModified;
    private final String documentation;
    private final List<String> examples;
    private final Map<String, Object> requirements;
    
    // Builder pattern for easy construction
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        // Builder implementation...
    }
}
```

---

## Adapter Architecture

### MCP Tool Adapter

```java
package org.openhab.core.ai.mcp.internal;

import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.mcp.api.tool.MCPTool;

/**
 * Adapter that converts Action to MCPTool interface.
 */
public class MCPToolAdapter implements MCPTool {
    
    private final Action action;
    private final String toolId;
    
    public MCPToolAdapter(Action action) {
        this.action = action;
        this.toolId = "mcp." + action.getActionId();
    }
    
    @Override
    public String getToolId() {
        return toolId;
    }
    
    @Override
    public String getToolName() {
        return action.getActionName();
    }
    
    @Override
    public String getDescription() {
        return action.getDescription();
    }
    
    @Override
    public Map<String, Object> getSchema() {
        return action.getParameterSchema();
    }
    
    @Override
    public MCPToolValidationResult validateParameters(Map<String, Object> parameters) {
        ActionValidationResult validation = action.validateParameters(parameters);
        
        return MCPToolValidationResult.builder()
            .valid(validation.isValid())
            .message(validation.getErrors().isEmpty() ? "Valid" : String.join("; ", validation.getErrors()))
            .build();
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> parameters, MCPToolContext context) throws MCPToolException {
        try {
            // Convert MCP context to Action context
            ActionContext aiContext = convertToActionContext(context);
            
            // Execute action synchronously
            ActionResult result = action.execute(parameters, aiContext);
            
            // Convert result to MCP format
            return convertToMCPToolResult(result);
            
        } catch (ActionException e) {
            throw new MCPToolException(toolId, e.getMessage(), e, MCPToolException.MCPToolErrorCode.EXECUTION_ERROR);
        }
    }
    
    @Override
    public MCPToolMetadata getMetadata() {
        ActionMetadata metadata = action.getMetadata();
        
        return MCPToolMetadata.builder()
            .withVersion(metadata.getVersion())
            .withAuthor(metadata.getAuthor())
            .withDescription(metadata.getDescription())
            .build();
    }
    
    private ActionContext convertToActionContext(MCPToolContext mcpContext) {
        return ActionContext.builder()
            .protocol("mcp")
            .clientId(mcpContext.getClientId())
            .authContext(mcpContext.getAuthContext())
            .serviceProvider(mcpContext.getServiceProvider())
            .build();
    }
    
    private MCPToolResult convertToMCPToolResult(ActionResult aiResult) {
        if (aiResult.isSuccess()) {
            return MCPToolResult.successJson(toolId, aiResult.getData(), aiResult.getExecutionTimeMs());
        } else {
            return MCPToolResult.error(toolId, aiResult.getMessage(), aiResult.getExecutionTimeMs());
        }
    }
}
```

### A2A Skill Adapter

```java
package org.openhab.core.ai.a2a.internal;

import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.a2a.api.A2AMessage;
import org.openhab.core.ai.a2a.api.A2APriority;

/**
 * Adapter that converts Action to A2A skill interface.
 */
public class A2ASkillAdapter implements A2ASkill {
    
    private final Action action;
    private final String skillId;
    
    public A2ASkillAdapter(Action action) {
        this.action = action;
        this.skillId = "a2a." + action.getActionId();
    }
    
    @Override
    public String getSkillId() {
        return skillId;
    }
    
    @Override
    public String getSkillName() {
        return action.getActionName();
    }
    
    @Override
    public String getDescription() {
        return action.getDescription();
    }
    
    @Override
    public Map<String, Object> getSchema() {
        return action.getParameterSchema();
    }
    
    @Override
    public A2ASkillValidationResult validateParameters(Map<String, Object> parameters) {
        ActionValidationResult validation = action.validateParameters(parameters);
        
        return A2ASkillValidationResult.builder()
            .valid(validation.isValid())
            .errors(validation.getErrors())
            .warnings(validation.getWarnings())
            .build();
    }
    
    @Override
    public CompletableFuture<A2ASkillResult> executeAsync(A2AMessage message) {
        try {
            // Extract parameters from A2A message
            Map<String, Object> parameters = extractParametersFromMessage(message);
            
            // Convert A2A context to Action context
            ActionContext aiContext = convertToActionContext(message);
            
            // Execute action asynchronously
            return action.executeAsync(parameters, aiContext)
                .thenApply(this::convertToA2ASkillResult)
                .exceptionally(this::handleExecutionError);
                
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                A2ASkillResult.error(skillId, e.getMessage())
            );
        }
    }
    
    @Override
    public A2ASkillResult execute(A2AMessage message) {
        try {
            // Extract parameters from A2A message
            Map<String, Object> parameters = extractParametersFromMessage(message);
            
            // Convert A2A context to Action context
            ActionContext aiContext = convertToActionContext(message);
            
            // Execute action synchronously
            ActionResult result = action.execute(parameters, aiContext);
            
            // Convert result to A2A format
            return convertToA2ASkillResult(result);
            
        } catch (ActionException e) {
            return A2ASkillResult.error(skillId, e.getMessage());
        }
    }
    
    @Override
    public A2ASkillMetadata getMetadata() {
        ActionMetadata metadata = action.getMetadata();
        
        return A2ASkillMetadata.builder()
            .withVersion(metadata.getVersion())
            .withAuthor(metadata.getAuthor())
            .withDescription(metadata.getDescription())
            .capabilities(action.getCapabilities())
            .build();
    }
    
    private ActionContext convertToActionContext(A2AMessage message) {
        return ActionContext.builder()
            .protocol("a2a")
            .clientId(message.getSenderId())
            .sessionId(message.getSessionId())
            .correlationId(message.getCorrelationId())
            .priority(message.getPriority())
            .authContext(extractAuthContext(message))
            .serviceProvider(getServiceProvider())
            .build();
    }
    
    private A2ASkillResult convertToA2ASkillResult(ActionResult aiResult) {
        if (aiResult.isSuccess()) {
            return A2ASkillResult.success(skillId, aiResult.getData(), aiResult.getExecutionTimeMs());
        } else {
            return A2ASkillResult.error(skillId, aiResult.getMessage());
        }
    }
    
    private A2ASkillResult handleExecutionError(Throwable error) {
        return A2ASkillResult.error(skillId, "Execution failed: " + error.getMessage());
    }
}
```

---

## Implementation Strategy

### Phase 1: Core Action Interface

1. **Create Action Interface**
   ```java
   // In org.openhab.core.ai.common
   public interface Action {
       // Core interface methods
   }
   ```

2. **Implement Supporting Classes**
   ```java
   // ActionContext, ActionResult, ActionValidationResult, ActionMetadata
   ```

3. **Create Base Implementation**
   ```java
   public abstract class AbstractAction implements Action {
       // Common implementation for shared functionality
   }
   ```

### Phase 2: Protocol Adapters

1. **MCP Tool Adapter**
   ```java
   // In org.openhab.core.ai.mcp
   public class MCPToolAdapter implements MCPTool {
       // Adapt Action to MCPTool
   }
   ```

2. **A2A Skill Adapter**
   ```java
   // In org.openhab.core.ai.a2a
   public class A2ASkillAdapter implements A2ASkill {
       // Adapt Action to A2ASkill
   }
   ```

### Phase 3: Action Implementations

1. **Migrate Existing Tools**
   ```java
   // Convert existing MCP tools to Action implementations
   public class ListItemsAction extends AbstractAction {
       // Implementation using Action interface
   }
   ```

2. **Create New Actions**
   ```java
   // Create new actions that work with both protocols
   public class SceneCoordinationAction extends AbstractAction {
       // Multi-agent coordination action
   }
   ```

### Phase 4: Registry Integration

1. **Action Registry**
   ```java
   public class ActionRegistry {
       private final Map<String, Action> actions = new ConcurrentHashMap<>();
       
       public void registerAction(Action action) {
           actions.put(action.getActionId(), action);
       }
       
       public Action getAction(String actionId) {
           return actions.get(actionId);
       }
   }
   ```

2. **Protocol-Specific Registries**
   ```java
   // MCP Tool Registry using Action Registry
   public class MCPToolRegistry {
       private final ActionRegistry actionRegistry;
       
       public MCPTool getTool(String toolId) {
           Action action = actionRegistry.getAction(extractActionId(toolId));
           return new MCPToolAdapter(action);
       }
   }
   ```

---

## Benefits and Trade-offs

### ✅ **Benefits**

1. **🔄 Code Reuse**
   - Single implementation for both protocols
   - Reduced maintenance overhead
   - Consistent behavior across protocols

2. **🧪 Unified Testing**
   - Single test suite for core functionality
   - Protocol-specific testing for adapters only
   - Easier to test edge cases

3. **📦 Simplified Development**
   - Developers write one action, get two protocols
   - Clear separation of concerns
   - Easier to understand and maintain

4. **🔧 Protocol Independence**
   - Actions are protocol-agnostic
   - Easy to add new protocols (e.g., REST API)
   - Future-proof architecture

5. **📈 Performance**
   - No double execution overhead
   - Shared caching and optimization
   - Better resource utilization

### ⚠️ **Trade-offs**

1. **🔗 Interface Complexity**
   - Action interface must support both protocols
   - Some methods may not be relevant for all protocols
   - Potential for interface bloat

2. **🔄 Adapter Overhead**
   - Small performance overhead from adapters
   - Additional complexity in adapter logic
   - Potential for adapter bugs

3. **📚 Learning Curve**
   - Developers need to understand Action interface
   - Protocol-specific knowledge still required
   - More complex debugging

4. **🔧 Migration Effort**
   - Existing tools need to be migrated
   - Potential breaking changes
   - Testing effort for all protocols

---

## Migration Path

### Step 1: Create Action Interface (Week 1-2)
```java
// Create core interface and supporting classes
// Implement basic action implementations
// Create unit tests for Action interface
```

### Step 2: Implement Adapters (Week 3-4)
```java
// Create MCPToolAdapter and A2ASkillAdapter
// Test adapters with simple actions
// Ensure protocol compatibility
```

### Step 3: Migrate Core Tools (Week 5-8)
```java
// Migrate high-priority tools (items, things, rules)
// Test both protocols with migrated tools
// Update documentation and examples
```

### Step 4: Migrate Advanced Tools (Week 9-12)
```java
// Migrate remaining tools
// Create new A2A-specific actions
// Performance testing and optimization
```

### Step 5: Production Deployment (Week 13-16)
```java
// Full integration testing
// Performance validation
// Documentation updates
// Community feedback and iteration
```

---

## Example Implementation

### List Items Action

```java
package org.openhab.core.ai.action.library.items;

import org.openhab.core.ai.api.action.AbstractAction;
import org.openhab.core.ai.api.action.ActionContext;
import org.openhab.core.ai.api.action.ActionResult;
import org.openhab.core.items.ItemRegistry;

@Component(service = Action.class)
public class ListItemsAction extends AbstractAction {
    
    private static final String ACTION_ID = "openhab.items.list";
    private static final String ACTION_NAME = "List Items";
    private static final String CATEGORY = "items";
    
    @Reference
    private ItemRegistry itemRegistry;
    
    @Override
    public String getActionId() {
        return ACTION_ID;
    }
    
    @Override
    public String getActionName() {
        return ACTION_NAME;
    }
    
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    
    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "filter", Map.of("type", "string", "description", "Filter items by name pattern"),
                "type", Map.of("type", "string", "description", "Filter by item type"),
                "tags", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Filter by tags")
            )
        );
    }
    
    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Extract parameters
            String filter = (String) parameters.get("filter");
            String type = (String) parameters.get("type");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) parameters.get("tags");
            
            // Execute action
            List<Item> items = listItems(filter, type, tags);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(items, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.error("Failed to list items: " + e.getMessage(), 
                new ActionError("EXECUTION_ERROR", e.getMessage()), executionTime);
        }
    }
    
    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder()
            .withVersion("1.0.0")
            .
            .withDescription("List openHAB items with optional filtering")
            .withTags(List.of("items", "list", "filter"))
            .withExamples(List.of(
                "List all items: {}",
                "Filter by name: {\"filter\": \"light*\"}",
                "Filter by type: {\"type\": \"Switch\"}"
            ))
            .build();
    }
    
    private List<Item> listItems(String filter, String type, List<String> tags) {
        // Implementation using ItemRegistry
        return itemRegistry.getAll().stream()
            .filter(item -> filter == null || item.getName().matches(filter))
            .filter(item -> type == null || item.getType().equals(type))
            .filter(item -> tags == null || tags.isEmpty() || 
                tags.stream().anyMatch(tag -> item.getTags().contains(tag)))
            .collect(Collectors.toList());
    }
}
```

### Usage in Both Protocols

```java
// MCP Usage
MCPTool tool = new MCPToolAdapter(new ListItemsAction());
MCPToolResult result = tool.execute(Map.of("filter", "light*"), context);

// A2A Usage
A2ASkill skill = new A2ASkillAdapter(new ListItemsAction());
A2ASkillResult result = skill.execute(createMessage(Map.of("filter", "light*")));
```

---

## Conclusion

The `Action` unified architecture provides a powerful foundation for sharing AI capabilities between MCP and A2A protocols while maintaining protocol-specific features and optimizations. The adapter pattern ensures clean separation of concerns while enabling maximum code reuse.

**Key Benefits:**
- ✅ Single implementation for both protocols
- ✅ Consistent behavior and testing
- ✅ Easy to add new protocols
- ✅ Protocol-specific optimizations via adapters
- ✅ Future-proof architecture

**Recommended Approach:**
1. Start with core Action interface
2. Implement adapters for existing protocols
3. Migrate high-priority tools first
4. Create new A2A-specific actions
5. Iterate based on feedback and performance

This architecture positions openHAB as a leader in multi-protocol AI integration while maintaining the flexibility and power that both MCP and A2A protocols provide.

---

## Document Information

- **Created**: Based on analysis of MCP tools and A2A skills interfaces
- **Purpose**: Propose unified Action architecture for protocol interoperability
- **Scope**: Interface design, adapter patterns, implementation strategy
- **Status**: Architecture proposal complete, ready for implementation planning 