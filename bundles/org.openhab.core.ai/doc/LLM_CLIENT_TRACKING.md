# LLM Client Tracking System

## Overview

The LLM Client Tracking System provides comprehensive monitoring and analytics for Large Language Model (LLM) usage within the openHAB AI framework. It automatically tracks which agents are using which LLM clients, monitors performance metrics, estimates costs, and provides real-time insights through REST APIs.

## Architecture

The system consists of three main components:

1. **LLMClientTrackingService** - Core service that tracks usage and provides analytics
2. **LLMProviderFactory** - Factory that records client usage when creating clients for agents
3. **LLMClientTrackingResource** - REST API for accessing tracking data

## Direct Integration Approach

The system uses **direct integration** rather than wrapping, providing better performance and cleaner architecture:

### **Factory-Level Integration**

The **LLMProviderFactory** records client usage when agents request clients:

```java
@Component
public class MyAgent {
    @Reference
    private LLMProviderFactory llmProviderFactory;

    public void performReasoning() {
        // Automatic tracking - usage is recorded when client is obtained
        ModelClient client = llmProviderFactory.getOrCreateProviderForAgent(
            ModelProviderType.OPENAI, "my-agent-id");
        
        // All calls are automatically tracked by the reasoning engine
        ModelResponse response = client.complete("Hello world", params);
    }
}
```

### **Reasoning Engine Integration**

The **SharedModelReasoningEngine** automatically records request completions:

```java
// Automatic tracking in reasoning requests
ModelClient client = getOrCreateModelClientForAgent(providerId, agentId);

// Request completion is automatically recorded after each call
ModelResponse response = client.complete(prompt, params).get();
recordRequestCompletion(agentId, client.getProviderType(), client.getModelName(), 
    requestId, response, responseTime, true);
```

## Features

### 1. Automatic Usage Tracking
- **Agent-Client Mapping**: Track which agents are using which LLM clients
- **Session Tracking**: Optional session-level tracking for complex workflows
- **Real-time Monitoring**: Live tracking of active sessions and usage

### 2. Performance Metrics
- **Response Times**: Track request/response latency
- **Success Rates**: Monitor success vs failure rates
- **Token Usage**: Estimate tokens used per request
- **Cost Estimation**: Calculate estimated costs based on provider pricing

### 3. Analytics and Reporting
- **Provider Statistics**: Usage statistics per LLM provider
- **Agent Statistics**: Usage patterns per agent
- **System-wide Metrics**: Overall system performance and usage
- **Historical Data**: Track usage over time

### 4. REST API Access
- **Real-time Data**: Access current usage statistics
- **Agent-specific Views**: Get usage data for specific agents
- **Provider-specific Views**: Get usage data for specific providers
- **System Health**: Monitor overall system health

## Usage Examples

### Basic Agent Usage

```java
@Component
public class MyAgent {
    @Reference
    private LLMProviderFactory llmProviderFactory;

    public void performTask() {
        // Get client with automatic tracking
        ModelClient client = llmProviderFactory.getOrCreateProviderForAgent(
            ModelProviderType.OPENAI, "my-agent-id");
        
        // Use normally - all calls are tracked by the reasoning engine
        ModelResponse response = client.complete("Analyze this data", params);
    }
}
```

### Session-Based Tracking

```java
// Get client with session tracking
ModelClient client = llmProviderFactory.getOrCreateProviderForAgent(
    ModelProviderType.OPENAI, agentId, sessionId);
```

### Using Primary/Fallback Providers

```java
@Component
public class ReliableAgent {
    @Reference
    private LLMProviderFactory llmProviderFactory;

    public void performReliableTask() {
        // Try primary provider first
        ModelClient client = llmProviderFactory.getPrimaryProviderForAgent("my-agent-id");
        
        if (client == null || !client.isAvailable()) {
            // Fallback to secondary provider
            client = llmProviderFactory.getFallbackProviderForAgent("my-agent-id");
        }
        
        // All calls are tracked regardless of which provider is used
        ModelResponse response = client.complete("Important task", params);
    }
}
```

## REST API Endpoints

The system provides comprehensive REST API access:

### Client Information
- `GET /rest/ai/llm-tracking/clients` - All clients overview
- `GET /rest/ai/llm-tracking/clients/{providerType}/{modelName}` - Specific client details
- `GET /rest/ai/llm-tracking/clients/{providerType}/{modelName}/agents` - Agents using a client

### Agent Information
- `GET /rest/ai/llm-tracking/agents` - All agents overview
- `GET /rest/ai/llm-tracking/agents/{agentId}` - Specific agent details

### Provider Information
- `GET /rest/ai/llm-tracking/providers` - All providers overview
- `GET /rest/ai/llm-tracking/providers/{providerType}` - Specific provider details

### System Information
- `GET /rest/ai/llm-tracking/system/stats` - System statistics
- `GET /rest/ai/llm-tracking/system/health` - System health

## Data Structures

### ClientUsageInfo
Tracks usage information for a specific client (provider + model combination):
- Provider type and model name
- Active agents using this client
- Total requests, tokens, and costs
- Performance metrics (response times, success rates)

### AgentClientSession
Tracks an agent's session with a specific client:
- Agent ID and session ID
- Provider type and model name
- Session duration and last usage time
- Active status

### ProviderUsageStats
Aggregated statistics for a provider:
- Total usage across all models
- Model-specific usage breakdown
- Agent-specific usage breakdown
- Performance and cost metrics

### SystemUsageStats
System-wide aggregated statistics:
- Total requests, tokens, and costs
- Average response times
- Error rates
- Last request timestamp

## Integration with Existing Services

### SharedModelReasoningEngine
The reasoning engine automatically records request completions:

```java
// Automatic tracking in reasoning requests
ModelClient client = getOrCreateModelClientForAgent(providerId, agentId);

// Request completion is automatically recorded
recordRequestCompletion(agentId, client.getProviderType(), client.getModelName(), 
    requestId, response, responseTime, true);
```

### Agent Classes
Agent classes can use the factory methods directly:

```java
// In any agent implementation
ModelClient client = llmProviderFactory.getOrCreateProviderForAgent(
    ModelProviderType.OPENAI, getAgentId());
```

## Configuration

### Tracking Service Configuration
The tracking service can be configured through OSGi configuration:

```properties
# Enable/disable tracking
org.openhab.core.ai.model.tracking.enabled=true

# Tracking retention settings
org.openhab.core.ai.model.tracking.retention.hours=24
org.openhab.core.ai.model.tracking.max.sessions=1000
```

### Provider Factory Configuration
The provider factory automatically integrates tracking when available:

```properties
# Primary and fallback providers
org.openhab.core.ai.model.primary.provider=openai
org.openhab.core.ai.model.fallback.provider=anthropic
```

## Monitoring and Troubleshooting

### Health Checks
Monitor system health through REST API:
```bash
curl http://localhost:8080/rest/ai/llm-tracking/system/health
```

### Performance Monitoring
Track performance metrics:
```bash
curl http://localhost:8080/rest/ai/llm-tracking/system/stats
```

### Agent-Specific Monitoring
Monitor specific agent usage:
```bash
curl http://localhost:8080/rest/ai/llm-tracking/agents/my-agent-id
```

### Debug Logging
Enable debug logging for troubleshooting:
```properties
org.openhab.core.ai.model.tracking.log.level=DEBUG
```

## Benefits of Direct Integration

1. **Better Performance** - No wrapper object creation or delegation overhead
2. **Cleaner Architecture** - Tracking is integrated at the service level, not as decoration
3. **Better Encapsulation** - Each service manages its own tracking responsibilities
4. **Easier Testing** - No need to test wrapper behavior
5. **More Maintainable** - Tracking logic is co-located with the actual service logic
6. **Better Error Handling** - Can handle tracking errors within the service context
7. **Zero Code Changes** - Existing agent code continues to work unchanged

## Comparison: Direct Integration vs Wrapping

### **Direct Integration (Current Approach)**
```java
// Factory records usage when client is obtained
ModelClient client = llmProviderFactory.getOrCreateProviderForAgent(providerType, agentId);

// Reasoning engine records completions
ModelResponse response = client.complete(prompt, params).get();
recordRequestCompletion(agentId, providerType, modelName, requestId, response, responseTime, true);
```

**Advantages:**
- No performance overhead from wrapper objects
- Cleaner architecture with clear separation of concerns
- Better error handling and debugging
- Easier to test and maintain

### **Wrapping Approach (Alternative)**
```java
// Wrapper object is created
TrackingModelClient client = new TrackingModelClient(originalClient, agentId);

// All calls go through wrapper
ModelResponse response = client.complete(prompt, params).get();
```

**Disadvantages:**
- Performance overhead from wrapper object creation
- More complex architecture with delegation
- Harder to debug and test
- Potential for wrapper-related bugs

## Future Enhancements

- **Cost Alerts**: Set up alerts for high usage/costs
- **Usage Quotas**: Implement usage limits per agent
- **Advanced Analytics**: Machine learning-based usage optimization
- **Integration with Monitoring**: Connect with external monitoring systems
- **Historical Analysis**: Long-term usage trend analysis

---

## 📋 **Document Analysis and Relevance Assessment**

### **Current Status: HIGHLY RELEVANT - LLM TRACKING SYSTEM GUIDE**

This document provides a **comprehensive guide for the LLM Client Tracking System** that is **actively relevant** for implementing monitoring and analytics for LLM usage. It contains detailed architecture, implementation guidance, and best practices for tracking LLM client usage.

### **Key Findings:**

#### ✅ **Comprehensive Tracking System**
- **Automatic Usage Tracking**: Agent-client mapping and session tracking
- **Performance Metrics**: Response times, success rates, and token usage
- **Analytics and Reporting**: Provider statistics, agent statistics, and system-wide metrics
- **REST API Access**: Real-time data access and system health monitoring

#### ✅ **Practical Implementation Guidance**
- **Direct Integration Approach**: Better performance and cleaner architecture than wrapping
- **Code Examples**: Comprehensive code examples for all tracking scenarios
- **Configuration Examples**: Detailed configuration examples for different scenarios
- **Monitoring and Troubleshooting**: Health checks, performance monitoring, and debug logging

#### ✅ **Production-Ready Features**
- **Zero Code Changes**: Existing agent code continues to work unchanged
- **Better Performance**: No wrapper object creation or delegation overhead
- **Cleaner Architecture**: Tracking integrated at service level
- **Better Error Handling**: Can handle tracking errors within service context

### **Recommended Actions:**

#### ✅ **Keep and Implement**
- **Tracking System**: Implement the LLM client tracking system for monitoring and analytics
- **Direct Integration**: Use the direct integration approach for better performance
- **REST API**: Deploy the REST API for accessing tracking data
- **Monitoring**: Implement the monitoring and troubleshooting capabilities

#### ✅ **Update Based on Current Implementation**
- **Current Status**: Verify current implementation status against documented features
- **Configuration Updates**: Update configuration examples for current system
- **API Integration**: Ensure REST API integration with current system

#### ✅ **Integration with Other Documents**
- **Agent Model Integration**: Coordinate with AGENT_MODEL_INTEGRATION.md
- **Performance Optimization**: Align with AGENT_MODEL_PERFORMANCE_OPTIMIZATION.md
- **Monitoring**: Coordinate with monitoring/README.md

### **Current Relevance Score: 9/10**

This document is **highly relevant** and should be **actively implemented** for monitoring and analytics of LLM usage in the openHAB AI system.
