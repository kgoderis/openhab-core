# Agent-Model Configuration Examples

This document provides comprehensive configuration examples for different model providers and agent scenarios in the openHAB AI bundle.

## Model Provider Configurations

### OpenAI Configuration

```java
// OpenAI GPT-4 Configuration
ModelParameters openAiGpt4Params = ModelParameters.builder()
    .model("gpt-4")
    .temperature(0.7)
    .maxTokens(2000)
    .topP(0.9)
    .frequencyPenalty(0.1)
    .presencePenalty(0.1)
    .build();

// OpenAI GPT-3.5 Configuration (for faster responses)
ModelParameters openAiGpt35Params = ModelParameters.builder()
    .model("gpt-3.5-turbo")
    .temperature(0.5)
    .maxTokens(1000)
    .topP(0.9)
    .build();
```

### Anthropic Claude Configuration

```java
// Claude 3 Opus Configuration (most capable)
ModelParameters claudeOpusParams = ModelParameters.builder()
    .model("claude-3-opus-20240229")
    .temperature(0.7)
    .maxTokens(4000)
    .topP(0.9)
    .build();

// Claude 3 Sonnet Configuration (balanced)
ModelParameters claudeSonnetParams = ModelParameters.builder()
    .model("claude-3-sonnet-20240229")
    .temperature(0.6)
    .maxTokens(2000)
    .topP(0.9)
    .build();

// Claude 3 Haiku Configuration (fastest)
ModelParameters claudeHaikuParams = ModelParameters.builder()
    .model("claude-3-haiku-20240307")
    .temperature(0.4)
    .maxTokens(1000)
    .topP(0.9)
    .build();
```

### Local Model Configuration (Ollama)

```java
// Local Llama 2 Configuration
ModelParameters llama2Params = ModelParameters.builder()
    .model("llama2")
    .temperature(0.7)
    .maxTokens(2048)
    .topP(0.9)
    .build();

// Local Mistral Configuration
ModelParameters mistralParams = ModelParameters.builder()
    .model("mistral")
    .temperature(0.6)
    .maxTokens(2048)
    .topP(0.9)
    .build();

// Local Code Llama Configuration (for code generation)
ModelParameters codeLlamaParams = ModelParameters.builder()
    .model("codellama")
    .temperature(0.3)
    .maxTokens(2048)
    .topP(0.9)
    .build();
```

## Agent Specialization Configurations

### System Monitoring Agent

```java
AgentModelContext systemMonitoringContext = AgentModelContext.builder()
    .agentId("system-monitor")
    .specialization("system-monitoring")
    .domain("home-automation")
    .capabilities(Map.of(
        "monitoring", true,
        "alerting", true,
        "performance_analysis", true,
        "anomaly_detection", true
    ))
    .constraints(Map.of(
        "maxTokens", 1000,
        "timeout", 30,
        "maxRequestsPerMinute", 60,
        "priority", "high"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a system monitoring specialist for home automation.",
        "alert", "Analyze the alert: {{alert}} and provide recommendations.",
        "performance", "Analyze system performance: {{metrics}} and suggest optimizations.",
        "anomaly", "Detect anomalies in: {{data}} and report findings."
    ))
    .preferences(Map.of(
        "temperature", 0.3,
        "maxTokens", 1000,
        "responseFormat", "structured",
        "urgency", "high"
    ))
    .build();
```

### Security Agent

```java
AgentModelContext securityAgentContext = AgentModelContext.builder()
    .agentId("security-agent")
    .specialization("security-monitoring")
    .domain("cybersecurity")
    .capabilities(Map.of(
        "threat_detection", true,
        "vulnerability_assessment", true,
        "incident_response", true,
        "compliance_monitoring", true
    ))
    .constraints(Map.of(
        "maxTokens", 1500,
        "timeout", 45,
        "maxRequestsPerMinute", 30,
        "priority", "critical"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a cybersecurity specialist for home automation systems.",
        "threat", "Analyze potential threat: {{threat}} and provide mitigation steps.",
        "vulnerability", "Assess vulnerability: {{vulnerability}} and recommend fixes.",
        "incident", "Respond to security incident: {{incident}} with immediate actions."
    ))
    .preferences(Map.of(
        "temperature", 0.2,
        "maxTokens", 1500,
        "responseFormat", "actionable",
        "urgency", "critical"
    ))
    .build();
```

### Performance Optimization Agent

```java
AgentModelContext performanceAgentContext = AgentModelContext.builder()
    .agentId("performance-optimizer")
    .specialization("performance-optimization")
    .domain("system-performance")
    .capabilities(Map.of(
        "performance_analysis", true,
        "optimization", true,
        "resource_management", true,
        "bottleneck_detection", true
    ))
    .constraints(Map.of(
        "maxTokens", 2000,
        "timeout", 60,
        "maxRequestsPerMinute", 20,
        "priority", "medium"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a performance optimization specialist.",
        "analysis", "Analyze performance data: {{metrics}} and identify bottlenecks.",
        "optimization", "Optimize system configuration: {{config}} for better performance.",
        "resource", "Manage resource allocation: {{resources}} for optimal performance."
    ))
    .preferences(Map.of(
        "temperature", 0.4,
        "maxTokens", 2000,
        "responseFormat", "detailed",
        "urgency", "medium"
    ))
    .build();
```

### User Experience Agent

```java
AgentModelContext userExperienceContext = AgentModelContext.builder()
    .agentId("ux-agent")
    .specialization("user-experience")
    .domain("user-interface")
    .capabilities(Map.of(
        "ui_analysis", true,
        "usability_testing", true,
        "accessibility", true,
        "user_feedback", true
    ))
    .constraints(Map.of(
        "maxTokens", 1200,
        "timeout", 40,
        "maxRequestsPerMinute", 40,
        "priority", "normal"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a user experience specialist for home automation interfaces.",
        "ui_analysis", "Analyze user interface: {{ui}} and suggest improvements.",
        "usability", "Evaluate usability of: {{feature}} and provide recommendations.",
        "accessibility", "Check accessibility of: {{component}} and suggest enhancements."
    ))
    .preferences(Map.of(
        "temperature", 0.6,
        "maxTokens", 1200,
        "responseFormat", "user_friendly",
        "urgency", "normal"
    ))
    .build();
```

### Data Analysis Agent

```java
AgentModelContext dataAnalysisContext = AgentModelContext.builder()
    .agentId("data-analyst")
    .specialization("data-analysis")
    .domain("analytics")
    .capabilities(Map.of(
        "data_analysis", true,
        "trend_detection", true,
        "pattern_recognition", true,
        "reporting", true
    ))
    .constraints(Map.of(
        "maxTokens", 3000,
        "timeout", 90,
        "maxRequestsPerMinute", 15,
        "priority", "low"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a data analysis specialist for home automation systems.",
        "analysis", "Analyze data: {{data}} and identify patterns and trends.",
        "trend", "Detect trends in: {{dataset}} and provide insights.",
        "report", "Generate report for: {{metrics}} with actionable insights."
    ))
    .preferences(Map.of(
        "temperature", 0.5,
        "maxTokens", 3000,
        "responseFormat", "analytical",
        "urgency", "low"
    ))
    .build();
```

## Scenario-Based Configurations

### Emergency Response Scenario

```java
// Emergency Response Agent Configuration
AgentModelContext emergencyContext = AgentModelContext.builder()
    .agentId("emergency-responder")
    .specialization("emergency-response")
    .domain("safety")
    .capabilities(Map.of(
        "emergency_detection", true,
        "response_coordination", true,
        "safety_assessment", true,
        "alert_management", true
    ))
    .constraints(Map.of(
        "maxTokens", 500,
        "timeout", 10,
        "maxRequestsPerMinute", 120,
        "priority", "critical"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, an emergency response specialist. Respond quickly and decisively.",
        "emergency", "Emergency detected: {{emergency}}. Provide immediate response actions.",
        "coordination", "Coordinate response for: {{situation}} with available resources.",
        "assessment", "Assess safety of: {{condition}} and provide recommendations."
    ))
    .preferences(Map.of(
        "temperature", 0.1,
        "maxTokens", 500,
        "responseFormat", "immediate_action",
        "urgency", "critical"
    ))
    .build();

// Emergency Model Parameters (fast, deterministic)
ModelParameters emergencyParams = ModelParameters.builder()
    .temperature(0.1)
    .maxTokens(500)
    .topP(0.9)
    .build();
```

### Energy Management Scenario

```java
// Energy Management Agent Configuration
AgentModelContext energyContext = AgentModelContext.builder()
    .agentId("energy-manager")
    .specialization("energy-management")
    .domain("energy-optimization")
    .capabilities(Map.of(
        "energy_monitoring", true,
        "consumption_optimization", true,
        "cost_analysis", true,
        "sustainability", true
    ))
    .constraints(Map.of(
        "maxTokens", 1500,
        "timeout", 60,
        "maxRequestsPerMinute", 30,
        "priority", "high"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, an energy management specialist for home automation.",
        "monitoring", "Monitor energy consumption: {{consumption}} and identify optimization opportunities.",
        "optimization", "Optimize energy usage: {{usage}} for cost and sustainability.",
        "analysis", "Analyze energy costs: {{costs}} and provide savings recommendations."
    ))
    .preferences(Map.of(
        "temperature", 0.4,
        "maxTokens", 1500,
        "responseFormat", "cost_effective",
        "urgency", "high"
    ))
    .build();

// Energy Management Model Parameters (balanced)
ModelParameters energyParams = ModelParameters.builder()
    .temperature(0.4)
    .maxTokens(1500)
    .topP(0.9)
    .build();
```

### Learning and Adaptation Scenario

```java
// Learning Agent Configuration
AgentModelContext learningContext = AgentModelContext.builder()
    .agentId("learning-agent")
    .specialization("learning-adaptation")
    .domain("machine-learning")
    .capabilities(Map.of(
        "pattern_learning", true,
        "behavior_adaptation", true,
        "prediction", true,
        "optimization", true
    ))
    .constraints(Map.of(
        "maxTokens", 2000,
        "timeout", 120,
        "maxRequestsPerMinute", 10,
        "priority", "low"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a learning and adaptation specialist.",
        "learning", "Learn from pattern: {{pattern}} and adapt behavior accordingly.",
        "prediction", "Predict future behavior based on: {{data}} and historical patterns.",
        "optimization", "Optimize learning parameters: {{parameters}} for better performance."
    ))
    .preferences(Map.of(
        "temperature", 0.7,
        "maxTokens", 2000,
        "responseFormat", "adaptive",
        "urgency", "low"
    ))
    .build();

// Learning Model Parameters (creative, exploratory)
ModelParameters learningParams = ModelParameters.builder()
    .temperature(0.7)
    .maxTokens(2000)
    .topP(0.9)
    .frequencyPenalty(0.2)
    .presencePenalty(0.2)
    .build();
```

## Multi-Agent Coordination Configurations

### Coordinated Response Team

```java
// Coordinator Agent
AgentModelContext coordinatorContext = AgentModelContext.builder()
    .agentId("coordinator")
    .specialization("coordination")
    .domain("multi-agent-coordination")
    .capabilities(Map.of(
        "coordination", true,
        "task_allocation", true,
        "conflict_resolution", true,
        "communication", true
    ))
    .constraints(Map.of(
        "maxTokens", 1000,
        "timeout", 30,
        "maxRequestsPerMinute", 60,
        "priority", "high"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a multi-agent coordination specialist.",
        "coordination", "Coordinate agents: {{agents}} for task: {{task}}.",
        "allocation", "Allocate tasks: {{tasks}} among agents: {{agents}}.",
        "resolution", "Resolve conflict: {{conflict}} between agents: {{agents}}."
    ))
    .preferences(Map.of(
        "temperature", 0.3,
        "maxTokens", 1000,
        "responseFormat", "coordinated",
        "urgency", "high"
    ))
    .build();

// Specialized Team Members
AgentModelContext monitorContext = AgentModelContext.builder()
    .agentId("monitor")
    .specialization("monitoring")
    .domain("system-monitoring")
    .capabilities(Map.of("monitoring", true))
    .constraints(Map.of("maxTokens", 500, "timeout", 15))
    .build();

AgentModelContext responderContext = AgentModelContext.builder()
    .agentId("responder")
    .specialization("response")
    .domain("action-execution")
    .capabilities(Map.of("response", true))
    .constraints(Map.of("maxTokens", 500, "timeout", 15))
    .build();

AgentModelContext analyzerContext = AgentModelContext.builder()
    .agentId("analyzer")
    .specialization("analysis")
    .domain("data-analysis")
    .capabilities(Map.of("analysis", true))
    .constraints(Map.of("maxTokens", 1000, "timeout", 45))
    .build();
```

## Environment-Specific Configurations

### Development Environment

```java
// Development Agent Configuration
AgentModelContext devContext = AgentModelContext.builder()
    .agentId("dev-agent")
    .specialization("development-support")
    .domain("software-development")
    .capabilities(Map.of(
        "code_analysis", true,
        "debugging", true,
        "testing", true,
        "documentation", true
    ))
    .constraints(Map.of(
        "maxTokens", 3000,
        "timeout", 120,
        "maxRequestsPerMinute", 20,
        "priority", "normal"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a development support specialist.",
        "code_analysis", "Analyze code: {{code}} and provide improvements.",
        "debugging", "Debug issue: {{issue}} and provide solutions.",
        "testing", "Generate tests for: {{component}} with comprehensive coverage."
    ))
    .preferences(Map.of(
        "temperature", 0.3,
        "maxTokens", 3000,
        "responseFormat", "technical",
        "urgency", "normal"
    ))
    .build();

// Development Model Parameters (precise, technical)
ModelParameters devParams = ModelParameters.builder()
    .temperature(0.3)
    .maxTokens(3000)
    .topP(0.9)
    .build();
```

### Production Environment

```java
// Production Agent Configuration
AgentModelContext prodContext = AgentModelContext.builder()
    .agentId("prod-agent")
    .specialization("production-support")
    .domain("production-operations")
    .capabilities(Map.of(
        "monitoring", true,
        "alerting", true,
        "maintenance", true,
        "optimization", true
    ))
    .constraints(Map.of(
        "maxTokens", 1000,
        "timeout", 30,
        "maxRequestsPerMinute", 60,
        "priority", "high"
    ))
    .promptTemplates(Map.of(
        "default", "You are {{agentId}}, a production support specialist.",
        "monitoring", "Monitor production system: {{system}} and report status.",
        "alerting", "Handle alert: {{alert}} and provide immediate actions.",
        "maintenance", "Schedule maintenance for: {{component}} with minimal disruption."
    ))
    .preferences(Map.of(
        "temperature", 0.2,
        "maxTokens", 1000,
        "responseFormat", "operational",
        "urgency", "high"
    ))
    .build();

// Production Model Parameters (reliable, fast)
ModelParameters prodParams = ModelParameters.builder()
    .temperature(0.2)
    .maxTokens(1000)
    .topP(0.9)
    .build();
```

## Configuration Best Practices

### 1. Temperature Settings

- **Low (0.1-0.3)**: For deterministic, factual responses (monitoring, alerts)
- **Medium (0.4-0.6)**: For balanced responses (analysis, optimization)
- **High (0.7-0.9)**: For creative, exploratory responses (learning, innovation)

### 2. Token Limits

- **Short (500-1000)**: For quick responses (alerts, status updates)
- **Medium (1000-2000)**: For detailed analysis (optimization, reporting)
- **Long (2000-4000)**: For comprehensive responses (documentation, complex analysis)

### 3. Timeout Settings

- **Fast (10-30s)**: For critical operations (emergency response, alerts)
- **Normal (30-60s)**: For standard operations (monitoring, analysis)
- **Extended (60-120s)**: For complex operations (learning, optimization)

### 4. Priority Levels

- **Critical**: Emergency response, security alerts
- **High**: System monitoring, performance optimization
- **Normal**: Regular operations, user interactions
- **Low**: Background tasks, learning, analysis

### 5. Rate Limiting

- **High Frequency (60-120/min)**: Real-time monitoring, alerts
- **Medium Frequency (20-60/min)**: Regular operations, analysis
- **Low Frequency (5-20/min)**: Complex operations, learning

## Configuration Validation

```java
// Configuration validation utility
public class ConfigurationValidator {
    
    public static boolean validateAgentContext(AgentModelContext context) {
        // Validate required fields
        if (context.getAgentId() == null || context.getAgentId().isEmpty()) {
            return false;
        }
        
        if (context.getSpecialization() == null || context.getSpecialization().isEmpty()) {
            return false;
        }
        
        // Validate constraints
        Map<String, Object> constraints = context.getConstraints();
        if (constraints != null) {
            Object maxTokens = constraints.get("maxTokens");
            if (maxTokens != null && (Integer) maxTokens <= 0) {
                return false;
            }
            
            Object timeout = constraints.get("timeout");
            if (timeout != null && (Integer) timeout <= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean validateModelParameters(ModelParameters parameters) {
        // Validate temperature
        if (parameters.getTemperature() < 0.0 || parameters.getTemperature() > 1.0) {
            return false;
        }
        
        // Validate max tokens
        if (parameters.getMaxTokens() <= 0) {
            return false;
        }
        
        // Validate top P
        if (parameters.getTopP() < 0.0 || parameters.getTopP() > 1.0) {
            return false;
        }
        
        return true;
    }
}
```

## Conclusion

These configuration examples provide a comprehensive starting point for different agent scenarios and model providers. Remember to:

1. **Start Simple**: Begin with basic configurations and refine based on performance
2. **Monitor Performance**: Track response times, success rates, and resource usage
3. **Iterate and Optimize**: Adjust parameters based on actual usage patterns
4. **Validate Configurations**: Use validation utilities to ensure proper configuration
5. **Document Changes**: Keep track of configuration changes and their impact

For more specific configurations or custom scenarios, refer to the integration tests and examples in the test suite.

---

## 📋 **Document Analysis and Relevance Assessment**

### **Current Status: HIGHLY RELEVANT - CONFIGURATION REFERENCE**

This document provides **comprehensive configuration examples** for agent-model integration that are **actively relevant** for implementing and configuring the AI system. It contains valuable examples and best practices for different model providers and agent scenarios.

### **Key Findings:**

#### ✅ **Comprehensive Configuration Coverage**
- **Multiple Model Providers**: Covers OpenAI, Anthropic Claude, and local Ollama models
- **Agent Specializations**: Provides examples for different agent types (system monitoring, energy optimization, etc.)
- **Best Practices**: Includes temperature, token limits, timeout, and rate limiting guidelines
- **Validation Utilities**: Provides configuration validation examples

#### ✅ **Practical Implementation Examples**
- **Model Parameters**: Detailed examples for different model configurations
- **Agent Contexts**: Comprehensive agent context configurations
- **Specialization Examples**: Specific examples for different agent types
- **Integration Patterns**: Shows how to integrate models with agents

#### ✅ **Best Practices and Guidelines**
- **Temperature Settings**: Clear guidelines for different response types
- **Token Limits**: Appropriate limits for different use cases
- **Timeout Settings**: Proper timeout configurations for different operations
- **Priority Levels**: Clear priority definitions for different operations

### **Recommended Actions:**

#### ✅ **Keep and Maintain**
- **Configuration Reference**: This document should be actively used as a configuration reference
- **Best Practices**: The best practices section is valuable for implementation
- **Validation Examples**: The validation utilities are useful for implementation

#### ✅ **Update Based on Current Implementation**
- **Current Model Support**: Verify which model providers are currently supported
- **Agent Types**: Update examples based on actual implemented agent types
- **Configuration System**: Align with current configuration system implementation

#### ✅ **Integration with Other Documents**
- **Model Integration**: Coordinate with AGENT_MODEL_INTEGRATION.md
- **Performance Optimization**: Align with AGENT_MODEL_PERFORMANCE_OPTIMIZATION.md
- **Troubleshooting**: Coordinate with AGENT_MODEL_TROUBLESHOOTING.md

### **Current Relevance Score: 9/10**

This document is **highly relevant** and should be **actively maintained** as a configuration reference for implementing and configuring the AI system.
