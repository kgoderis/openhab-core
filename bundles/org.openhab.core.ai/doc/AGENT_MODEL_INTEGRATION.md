# Agent-Model Integration Documentation

## Overview

The Agent-Model Integration framework provides a comprehensive solution for integrating AI models with autonomous agents in the openHAB AI bundle. This framework enables agents to perform intelligent reasoning, decision-making, and natural language processing using shared AI model resources.

## Architecture

### Core Components

#### 1. SharedModelReasoningEngine
The central component that orchestrates model reasoning for multiple agents. It provides:
- Shared model brain architecture for resource optimization
- Agent-specific model access and context management
- Concurrent request handling and resource management
- Model session pooling and optimization
- Comprehensive error handling and fallback mechanisms

#### 2. AgentModelContext
Represents the context and configuration for an agent's interaction with AI models:
- Agent specialization and domain information
- Capabilities and constraints
- Prompt templates and preferences
- Model parameter optimization

#### 3. AgentModelProvider
Provides agent-specific model access with:
- Agent-specific prompt templates and context builders
- Agent-specific model selection logic
- Agent-specific model parameter optimization
- Agent-specific model response processing and validation

### Key Features

- **Shared Resource Management**: Multiple agents can efficiently share AI model resources
- **Agent Specialization**: Each agent can have specialized contexts and capabilities
- **Context-Aware Reasoning**: Models receive agent-specific context for better responses
- **Performance Optimization**: Built-in caching, session management, and resource optimization
- **Error Handling**: Comprehensive error handling and fallback mechanisms
- **Security**: Built-in security controls and access management

## Getting Started

### Basic Setup

1. **Register an Agent**:
```java
String agentId = "my-agent";
AgentModelContext context = AgentModelContext.builder()
    .agentId(agentId)
    .specialization("system-monitoring")
    .domain("home-automation")
    .capabilities(Map.of("reasoning", true, "analysis", true))
    .constraints(Map.of("maxTokens", 1000, "timeout", 30))
    .build();

SharedModelReasoningEngine engine = new SharedModelReasoningEngine();
boolean registered = engine.registerAgent(agentId, context);
```

2. **Perform Reasoning**:
```java
String prompt = "Analyze the current system status";
Map<String, Object> contextData = new HashMap<>();
contextData.put("systemStatus", "operational");
contextData.put("load", "medium");

ModelParameters parameters = ModelParameters.builder()
    .temperature(0.7)
    .maxTokens(500)
    .build();

CompletableFuture<ModelResponse> result = engine.reasonAsync(agentId, prompt, contextData, parameters);
ModelResponse response = result.get(5, TimeUnit.SECONDS);
```

### Advanced Configuration

#### Agent Context Configuration

```java
AgentModelContext context = AgentModelContext.builder()
    .agentId("specialized-agent")
    .specialization("performance-optimization")
    .domain("system-performance")
    .capabilities(Map.of(
        "reasoning", true,
        "optimization", true,
        "performance_analysis", true
    ))
    .constraints(Map.of(
        "maxTokens", 1500,
        "timeout", 45,
        "maxRequestsPerMinute", 60
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a performance optimization specialist.",
        "analysis", "Analyze the performance data: {{data}}"
    ))
    .preferences(Map.of(
        "temperature", 0.4,
        "maxTokens", 1500,
        "responseFormat", "text"
    ))
    .build();
```

#### Model Parameters Optimization

```java
ModelParameters parameters = ModelParameters.builder()
    .temperature(0.5)        // Controls response creativity (0.0-1.0)
    .maxTokens(1000)         // Maximum tokens in response
    .topP(0.9)              // Nucleus sampling parameter
    .frequencyPenalty(0.1)   // Reduces repetition
    .presencePenalty(0.1)    // Encourages new topics
    .build();
```

## Usage Examples

### Basic Reasoning

```java
// Simple reasoning with basic context
String prompt = "What is the current system status?";
Map<String, Object> contextData = new HashMap<>();
contextData.put("systemStatus", "operational");
contextData.put("timestamp", Instant.now().toString());

ModelResponse response = engine.reasonAsync(agentId, prompt, contextData, parameters).get();
System.out.println("Response: " + response.getContent());
```

### Complex Decision Making

```java
// Complex decision with multiple factors
String prompt = "Should we implement the new caching strategy?";
Map<String, Object> contextData = new HashMap<>();
contextData.put("currentPerformance", 85.5);
contextData.put("expectedImprovement", 15.2);
contextData.put("implementationCost", 5000);
contextData.put("maintenanceOverhead", "medium");
contextData.put("compatibilityRisk", "low");

ModelResponse response = engine.reasonAsync(agentId, prompt, contextData, parameters).get();
```

### Optimization-Based Reasoning

```java
// Reasoning with optimization hints
String prompt = "Optimize the system configuration for better performance";
Map<String, Object> contextData = new HashMap<>();
contextData.put("currentConfig", "default");
contextData.put("performanceTarget", "high");

Map<String, Object> optimizationHints = new HashMap<>();
optimizationHints.put("focus", "memory_optimization");
optimizationHints.put("priority", "high");
optimizationHints.put("constraints", "budget_limited");

ModelResponse response = engine.reasonWithOptimizationAsync(agentId, prompt, contextData, optimizationHints, parameters).get();
```

## Configuration

### Model Configuration

The framework supports various model providers and configurations:

```java
// Configure model parameters
ModelParameters parameters = ModelParameters.builder()
    .temperature(0.7)        // Default temperature
    .maxTokens(1000)         // Default max tokens
    .build();

// Agent-specific preferences override defaults
Map<String, Object> preferences = new HashMap<>();
preferences.put("temperature", 0.4);
preferences.put("maxTokens", 1500);
```

### Performance Configuration

```java
// Configure performance settings
Map<String, Object> constraints = new HashMap<>();
constraints.put("maxTokens", 1000);
constraints.put("timeout", 30);
constraints.put("maxRequestsPerMinute", 60);
constraints.put("concurrentRequests", 5);
```

## Monitoring and Statistics

### Agent Statistics

```java
// Get agent-specific statistics
AgentModelStatistics stats = engine.getAgentStatistics(agentId);
System.out.println("Total requests: " + stats.getTotalRequests());
System.out.println("Successful requests: " + stats.getSuccessfulRequests());
System.out.println("Failed requests: " + stats.getFailedRequests());
System.out.println("Success rate: " + stats.getSuccessRate());
```

### Overall Statistics

```java
// Get overall system statistics
ModelIntegrationStatistics overallStats = engine.getOverallStatistics();
System.out.println("Total agents: " + overallStats.getTotalAgents());
System.out.println("Active agents: " + overallStats.getActiveAgents());
System.out.println("Active agent rate: " + overallStats.getActiveAgentRate());
```

### Health Monitoring

```java
// Check model health status
ModelHealthStatus healthStatus = engine.getModelHealthStatus();
System.out.println("Healthy: " + healthStatus.isHealthy());
System.out.println("Primary model available: " + healthStatus.isPrimaryModelAvailable());
System.out.println("Fallback model available: " + healthStatus.isFallbackModelAvailable());
System.out.println("Error rate: " + healthStatus.getErrorRate());
```

## Error Handling

### Exception Handling

```java
try {
    ModelResponse response = engine.reasonAsync(agentId, prompt, contextData, parameters).get();
} catch (RuntimeException e) {
    // Handle agent not registered
    System.err.println("Agent not registered: " + e.getMessage());
} catch (TimeoutException e) {
    // Handle timeout
    System.err.println("Request timed out: " + e.getMessage());
} catch (Exception e) {
    // Handle other errors
    System.err.println("Unexpected error: " + e.getMessage());
}
```

### Fallback Mechanisms

```java
// Force model fallback
String fallbackModel = "gpt-3.5-turbo";
boolean fallbackResult = engine.forceModelFallback(agentId, fallbackModel);

// Reset fallback
boolean resetResult = engine.resetModelFallback(agentId);
```

## Security

### Access Control

The framework includes built-in security controls:

- **Agent Registration**: Only registered agents can access models
- **Context Validation**: Agent contexts are validated for security
- **Request Limits**: Configurable rate limiting and request limits
- **Audit Logging**: All requests are logged for audit purposes

### Security Best Practices

1. **Validate Agent Contexts**: Always validate agent contexts before registration
2. **Use Secure Prompts**: Avoid including sensitive information in prompts
3. **Monitor Usage**: Regularly monitor agent statistics and usage patterns
4. **Implement Rate Limiting**: Configure appropriate rate limits for agents
5. **Audit Logs**: Review audit logs regularly for suspicious activity

## Performance Optimization

### Caching

```java
// Clear agent cache
boolean clearResult = engine.clearAgentCache(agentId);

// Clear all caches
boolean clearAllResult = engine.clearAllCaches();
```

### Resource Management

```java
// Update agent context for optimization
AgentModelContext updatedContext = context.toBuilder()
    .preferences(Map.of("temperature", 0.3, "maxTokens", 800))
    .build();

boolean updateResult = engine.updateAgentContext(agentId, updatedContext);
```

### Concurrent Processing

```java
// Submit multiple concurrent requests
CompletableFuture<ModelResponse>[] results = new CompletableFuture[5];
for (int i = 0; i < 5; i++) {
    String prompt = "Process request " + i;
    results[i] = engine.reasonAsync(agentId, prompt, contextData, parameters);
}

// Wait for all results
for (CompletableFuture<ModelResponse> result : results) {
    ModelResponse response = result.get(5, TimeUnit.SECONDS);
    // Process response
}
```

## Testing

### Unit Testing

```java
@Test
void testAgentRegistration() {
    String agentId = "test-agent";
    AgentModelContext context = createTestAgentContext(agentId);
    
    boolean result = engine.registerAgent(agentId, context);
    assertTrue(result);
    assertTrue(engine.isAgentRegistered(agentId));
}
```

### Integration Testing

```java
@Test
void testCompleteWorkflow() throws Exception {
    // Register agent
    String agentId = "integration-test-agent";
    AgentModelContext context = createTestAgentContext(agentId);
    engine.registerAgent(agentId, context);
    
    // Perform reasoning
    String prompt = "Analyze the current system state";
    Map<String, Object> contextData = new HashMap<>();
    contextData.put("systemState", "operational");
    
    ModelResponse response = engine.reasonAsync(agentId, prompt, contextData, parameters).get();
    assertNotNull(response);
    assertNotNull(response.getContent());
}
```

## Troubleshooting

### Common Issues

1. **Agent Not Registered**
   - Ensure the agent is registered before making requests
   - Check that the agent ID is correct

2. **Request Timeouts**
   - Increase timeout values in constraints
   - Check model provider availability
   - Monitor system resources

3. **High Error Rates**
   - Check model provider health
   - Review agent context configuration
   - Monitor system performance

4. **Memory Issues**
   - Clear agent caches regularly
   - Monitor cache sizes
   - Adjust max token limits

### Debugging

```java
// Enable debug logging
// Add to logback.xml:
// <logger name="org.openhab.core.ai.reasoning" level="DEBUG"/>

// Check agent registration
boolean registered = engine.isAgentRegistered(agentId);
System.out.println("Agent registered: " + registered);

// Get registered agent IDs
Set<String> agentIds = engine.getRegisteredAgentIds();
System.out.println("Registered agents: " + agentIds);
```

## Best Practices

### Agent Design

1. **Specialized Contexts**: Create specialized contexts for different agent types
2. **Clear Capabilities**: Define clear capabilities and constraints
3. **Optimized Prompts**: Use optimized prompt templates for better responses
4. **Resource Management**: Monitor and manage resource usage

### Performance

1. **Caching**: Use caching for frequently requested information
2. **Concurrent Processing**: Use concurrent processing for multiple requests
3. **Resource Limits**: Set appropriate resource limits
4. **Monitoring**: Monitor performance metrics regularly

### Security

1. **Validation**: Validate all inputs and contexts
2. **Access Control**: Implement proper access controls
3. **Audit Logging**: Enable comprehensive audit logging
4. **Rate Limiting**: Implement appropriate rate limiting

## API Reference

### SharedModelReasoningEngine

#### Core Methods

- `registerAgent(String agentId, AgentModelContext context)`: Register an agent
- `unregisterAgent(String agentId)`: Unregister an agent
- `isAgentRegistered(String agentId)`: Check if agent is registered
- `getRegisteredAgentIds()`: Get all registered agent IDs

#### Reasoning Methods

- `reasonAsync(String agentId, String prompt, Map<String, Object> context, ModelParameters parameters)`: Basic reasoning
- `reasonWithOptimizationAsync(String agentId, String prompt, Map<String, Object> context, Map<String, Object> optimizationHints, ModelParameters parameters)`: Optimization-based reasoning

#### Management Methods

- `updateAgentContext(String agentId, AgentModelContext context)`: Update agent context
- `getAgentContext(String agentId)`: Get agent context
- `getAgentModelProvider(String agentId)`: Get agent model provider

#### Statistics Methods

- `getAgentStatistics(String agentId)`: Get agent statistics
- `getOverallStatistics()`: Get overall statistics
- `getModelHealthStatus()`: Get model health status

#### Cache Management

- `clearAgentCache(String agentId)`: Clear agent cache
- `clearAllCaches()`: Clear all caches

#### Fallback Management

- `forceModelFallback(String agentId, String fallbackModel)`: Force model fallback
- `resetModelFallback(String agentId)`: Reset model fallback

### AgentModelContext

#### Builder Methods

- `agentId(String agentId)`: Set agent ID
- `specialization(String specialization)`: Set specialization
- `domain(String domain)`: Set domain
- `capabilities(Map<String, Object> capabilities)`: Set capabilities
- `constraints(Map<String, Object> constraints)`: Set constraints
- `promptTemplates(Map<String, String> promptTemplates)`: Set prompt templates
- `preferences(Map<String, Object> preferences)`: Set preferences
- `createdAt(Instant createdAt)`: Set creation time
- `lastUpdated(Instant lastUpdated)`: Set last update time

## Conclusion

The Agent-Model Integration framework provides a powerful and flexible solution for integrating AI models with autonomous agents. By following the guidelines and best practices outlined in this documentation, you can create robust, secure, and high-performance agent-model integrations.

For more information, refer to the integration tests and examples in the test suite.
