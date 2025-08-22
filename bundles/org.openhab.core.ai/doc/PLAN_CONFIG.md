# openHAB AI Bundle Configuration Management Plan

## Overview

This plan outlines the configuration management strategy for the openHAB AI bundle, following openHAB standards and patterns. The goal is to provide a comprehensive configuration system that combines the reliability of OSGi Config Admin (.cfg files) with the flexibility of YAML for complex structured data.

## Current State Analysis

### ✅ What's Already Implemented (Following openHAB Standards)

- **OSGi DS Integration**: Proper `@Component` with `configurationPid`
- **Lifecycle Management**: `@Activate`, `@Modified`, `@Deactivate` methods
- **File-Based Configuration**: `.cfg` files in `OH-INF/config/`
- **Hot-Reload**: WatchService integration for configuration changes
- **Configuration Precedence**: Environment variables > Config Admin > File-backed
- **Basic Structure**: Separate config files for different domains (a2a.cfg, mcp.cfg, ai-common.cfg)

### ❌ What Needs Enhancement

- **Limited Validation**: No comprehensive configuration validation
- **No YAML Support**: Complex structured data still uses properties format
- **No Typed Repositories**: Prompts, policies, and model presets lack structured storage
- **Basic Error Handling**: Limited error recovery and user feedback
- **No Configuration UI**: No integration with openHAB Paper UI or Main UI

## Configuration Strategy

### 1. Configuration Naming Convention

All configuration keys follow the consistent pattern: `ai.{domain}.{setting}=value`

**Domain Structure**:
- `ai.common.*` - Core AI bundle settings (ai-common.cfg)
- `ai.model.*` - Model provider settings (model.cfg)
- `ai.agent.*` - Agent protocol settings (agent.cfg)
- `ai.tool.*` - Tool protocol settings (tool.cfg)

**Benefits**:
- **Namespace Separation**: Prevents conflicts between different configuration domains
- **Consistency**: All keys follow the same pattern
- **Clarity**: Easy to identify which domain a setting belongs to
- **Maintainability**: Clear structure for adding new configuration domains
- **openHAB Integration**: Aligns with openHAB's configuration patterns

### 2. Configuration File Structure

```
src/main/resources/OH-INF/config/
├── ai-common.cfg          # Core AI settings (OSGi Config Admin)
├── agent.cfg              # Agent protocol settings (OSGi Config Admin)
├── tool.cfg               # Tool protocol settings (OSGi Config Admin)
└── model.cfg              # Model provider settings (OSGi Config Admin)

conf/ai/
├── prompts/              # YAML prompt templates
│   ├── agents/
│   │   ├── energy-agent.yaml
│   │   ├── security-agent.yaml
│   │   └── comfort-agent.yaml
│   └── tools/
│       ├── discovery.yaml
│       └── automation.yaml
├── policies/             # YAML policy definitions
│   ├── safety-policies.yaml
│   ├── privacy-policies.yaml
│   └── compliance-policies.yaml
├── models/               # YAML model presets
│   ├── openai-presets.yaml
│   ├── anthropic-presets.yaml
│   └── ollama-presets.yaml
└── agents/               # YAML agent configurations
    ├── energy-agent.yaml
    ├── security-agent.yaml
    └── comfort-agent.yaml
```

### 3. Configuration Separation Strategy

#### **OSGi Config Admin (.cfg files) - Simple Key-Value Settings**

**Purpose**: Core system settings, provider credentials, basic flags, and performance parameters that need OSGi integration.

**Characteristics**:
- Simple key-value pairs
- Environment variable overrides
- Hot-reload via OSGi Config Admin
- UI integration through Paper UI/Main UI
- Manual validation through configuration validators

**Examples**:
```properties
# ai-common.cfg
ai.enabled=true
ai.debug.mode=false
ai.default.timeout=30000
ai.max.concurrent.requests=10
ai.security.enabled=true

# model.cfg
ai.model.primary.provider=ollama
ai.model.fallback.provider=openai
ai.model.default.temperature=0.3
ai.model.default.max.tokens=1000
ai.model.openai.api.key=${OPENAI_API_KEY}
ai.model.anthropic.api.key=${ANTHROPIC_API_KEY}
ai.model.ollama.base.url=http://localhost:11434

# agent.cfg
ai.agent.server.enabled=true
ai.agent.server.port=8080
ai.agent.persistence.enabled=true
ai.agent.max.agents=50
ai.agent.execution.timeout=300s

# tool.cfg
ai.tool.server.enabled=true
ai.tool.server.port=8081
ai.tool.authentication.enabled=true
ai.tool.max.tools=100
```

#### **YAML Configuration - Complex Structured Data**

**Purpose**: Complex configurations that benefit from structured data, hierarchical organization, and rich content.

**Characteristics**:
- Hierarchical structure
- Rich content (prompts, policies, model configurations)
- File-backed with WatchService monitoring
- No direct OSGi integration
- Validation through schema or programmatic validation

**Examples**:

**Prompt Templates (conf/ai/prompts/agents/energy-agent.yaml)**:
```yaml
agent:
  name: "energy-agent"
  version: "1.0.0"
  description: "Energy management and optimization agent"
  
prompts:
  system:
    role: "You are an energy management AI agent for openHAB"
    capabilities:
      - "Monitor energy consumption"
      - "Optimize device schedules"
      - "Provide energy-saving recommendations"
    
  tasks:
    energy_analysis:
      instruction: |
        Analyze the current energy consumption patterns and identify optimization opportunities.
        Consider:
        - Peak usage times
        - Device efficiency
        - Renewable energy availability
        - Cost optimization
      examples:
        - "Device X is consuming 20% more energy during peak hours"
        - "Solar panels are producing excess energy that could be stored"
    
    schedule_optimization:
      instruction: |
        Create an optimized schedule for energy-consuming devices based on:
        - Energy prices
        - Renewable energy availability
        - User preferences
        - Device capabilities
      constraints:
        - "Never compromise user comfort"
        - "Respect device operational limits"
        - "Consider battery storage capacity"
```

**Policy Definitions (conf/ai/policies/safety-policies.yaml)**:
```yaml
policies:
  safety:
    version: "1.0.0"
    description: "Safety policies for AI agent operations"
    
    constraints:
      device_control:
        max_power_change: "10%"
        max_temperature_change: "5°C"
        require_confirmation: true
        emergency_override: true
        
      system_access:
        require_authentication: true
        audit_all_changes: true
        max_concurrent_operations: 5
        
      data_privacy:
        anonymize_user_data: true
        encrypt_sensitive_data: true
        retention_period: "30 days"
        
    rules:
      - name: "temperature_safety"
        condition: "temperature_change > 5°C"
        action: "require_confirmation"
        priority: "high"
        
      - name: "power_safety"
        condition: "power_change > 10%"
        action: "require_confirmation"
        priority: "high"
        
      - name: "emergency_override"
        condition: "emergency_detected"
        action: "allow_override"
        priority: "critical"
```

**Model Presets (conf/ai/models/openai-presets.yaml)**:
```yaml
models:
  openai:
    version: "1.0.0"
    description: "OpenAI model configurations"
    
    presets:
      gpt4_analysis:
        model: "gpt-4"
        temperature: 0.1
        max_tokens: 2000
        top_p: 0.9
        frequency_penalty: 0.0
        presence_penalty: 0.0
        system_prompt: "You are an analytical AI assistant"
        
      gpt4_creative:
        model: "gpt-4"
        temperature: 0.8
        max_tokens: 1500
        top_p: 0.95
        frequency_penalty: 0.1
        presence_penalty: 0.1
        system_prompt: "You are a creative AI assistant"
        
      gpt4o_mini_fast:
        model: "gpt-4o-mini"
        temperature: 0.3
        max_tokens: 1000
        top_p: 0.9
        frequency_penalty: 0.0
        presence_penalty: 0.0
        system_prompt: "You are a fast, efficient AI assistant"
```

## Implementation Plan

### Phase 1: Enhanced OSGi Configuration (Week 1-2)

#### 1.1 Enhance Configuration Services
- [ ] **Update DefaultConfigurationService** with improved validation and error handling
- [ ] **Update DefaultModelConfigurationService** with enhanced provider validation and `ai.model.*` key support
- [ ] **Update AgentConfigurationManager** with better configuration parsing and `ai.agent.*` key support
- [ ] **Create ToolConfigurationService** following openHAB patterns with `ai.tool.*` key support

#### 1.2 Implement Configuration Validation
- [ ] **Create ConfigurationValidator** for manual validation of configuration maps
- [ ] **Create ModelConfigurationValidator** for provider-specific validation with `ai.model.*` keys
- [ ] **Create AgentConfigurationValidator** for agent-specific validation with `ai.agent.*` keys
- [ ] **Create ToolConfigurationValidator** for tool-specific validation with `ai.tool.*` keys
- [ ] **Add validation to all @Activate and @Modified methods**
- [ ] **Implement configuration error recovery and logging**

### Phase 2: YAML Configuration Infrastructure (Week 2-3)

#### 2.1 Create YAML Configuration Services
- [ ] **Create PromptRepository** for YAML prompt templates
- [ ] **Create PolicyRepository** for YAML policy definitions
- [ ] **Create ModelPresetRepository** for YAML model configurations
- [ ] **Create AgentConfigurationRepository** for YAML agent configs

#### 2.2 Implement File Monitoring
- [ ] **Extend WatchService integration** for YAML files
- [ ] **Add YAML file change detection** and reload
- [ ] **Implement YAML validation** with error reporting
- [ ] **Add YAML schema validation** for complex structures

#### 2.3 Create YAML Parsers
- [ ] **Create PromptYamlParser** with template support
- [ ] **Create PolicyYamlParser** with rule validation
- [ ] **Create ModelPresetYamlParser** with provider mapping
- [ ] **Create AgentConfigYamlParser** with dependency resolution

### Phase 3: Configuration Integration (Week 3-4)

#### 3.1 Unified Configuration Access
- [ ] **Create ConfigurationManager** as unified access point
- [ ] **Implement configuration precedence** (env > cfg > yaml > defaults)
- [ ] **Add configuration caching** for performance
- [ ] **Create configuration change events**

#### 3.2 openHAB UI Integration
- [ ] **Add configuration to Paper UI** (for .cfg settings)
- [ ] **Create Main UI configuration pages** for YAML files
- [ ] **Implement configuration validation UI**
- [ ] **Add configuration backup/restore functionality**

#### 3.3 Configuration Documentation
- [ ] **Create configuration schema documentation**
- [ ] **Add configuration examples** for all YAML files
- [ ] **Create configuration troubleshooting guide**
- [ ] **Add configuration migration guide**

### Phase 4: Testing and Validation (Week 4-5)

#### 4.1 Configuration Testing
- [ ] **Create unit tests** for all configuration services
- [ ] **Create integration tests** for configuration loading
- [ ] **Create YAML validation tests**
- [ ] **Create configuration precedence tests**

#### 4.2 Error Handling Testing
- [ ] **Test configuration error recovery**
- [ ] **Test YAML parsing error handling**
- [ ] **Test configuration validation errors**
- [ ] **Test configuration change events**

## Configuration Examples

### OSGi Config Admin Examples

**Enhanced Configuration Service Example**:
```java
@Component(service = ModelConfigurationService.class, configurationPid = "org.openhab.ai.model")
public class DefaultModelConfigurationService implements ModelConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(DefaultModelConfigurationService.class);
    private final ConfigurationValidator validator = new ConfigurationValidator();

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("Activating Model Configuration Service");
        
        try {
            // Validate configuration before loading
            validator.validateModelConfiguration(config);
            
            // Load and validate provider configurations
            loadProviderConfigurations(config);
            
            // Initialize configuration file and start watching
            initializeConfigurationFile();
            
            logger.info("Model Configuration Service activated successfully");
        } catch (ConfigurationException e) {
            logger.error("Failed to activate Model Configuration Service: {}", e.getMessage());
            // Load default configuration as fallback
            loadDefaultConfiguration();
        }
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying Model Configuration Service");
        
        try {
            // Validate new configuration
            validator.validateModelConfiguration(config);
            
            // Reload configuration
            loadProviderConfigurations(config);
            
            logger.info("Model Configuration Service modified successfully");
        } catch (ConfigurationException e) {
            logger.error("Failed to modify Model Configuration Service: {}", e.getMessage());
            // Keep existing configuration
        }
    }

    private void loadProviderConfigurations(Map<String, Object> config) {
        // Enhanced configuration loading with validation
        primaryProvider = validator.validateProvider(getStringConfig(config, "ai.model.primary.provider", "ollama"));
        fallbackProvider = validator.validateProvider(getStringConfig(config, "ai.model.fallback.provider", "openai"));
        defaultTemperature = validator.validateTemperature(getDoubleConfig(config, "ai.model.default.temperature", 0.3));
        defaultMaxTokens = validator.validateMaxTokens(getIntConfig(config, "ai.model.default.maxTokens", 1000));
        
        // Load and validate provider-specific configurations
        loadProviderConfigs(config);
    }
}
```

**Configuration Validator Example**:
```java
public class ConfigurationValidator {
    
    private final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    public void validateModelConfiguration(Map<String, Object> config) throws ConfigurationException {
        // Validate required fields
        validateRequiredField(config, "ai.model.primary.provider");
        validateRequiredField(config, "ai.model.fallback.provider");
        
        // Validate numeric ranges
        validateTemperatureRange(getDoubleConfig(config, "ai.model.default.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "ai.model.default.maxTokens", 1000));
        
        // Validate provider configurations
        validateProviderConfigurations(config);
    }
    
    private void validateProviderConfigurations(Map<String, Object> config) throws ConfigurationException {
        // Validate OpenAI configuration if enabled
        if (getBooleanConfig(config, "ai.model.openai.enabled", false)) {
            validateOpenAIConfig(config);
        }
        
        // Validate Anthropic configuration if enabled
        if (getBooleanConfig(config, "ai.model.anthropic.enabled", false)) {
            validateAnthropicConfig(config);
        }
        
        // Validate Ollama configuration if enabled
        if (getBooleanConfig(config, "ai.model.ollama.enabled", true)) {
            validateOllamaConfig(config);
        }
    }
    
    private void validateOpenAIConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "ai.model.openai.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("OpenAI API key is required when OpenAI is enabled");
        }
        
        String baseUrl = getStringConfig(config, "ai.model.openai.base.url", "https://api.openai.com/v1");
        if (!isValidUrl(baseUrl)) {
            throw new ConfigurationException("Invalid OpenAI base URL: " + baseUrl);
        }
    }
    
    private void validateTemperatureRange(double temperature) throws ConfigurationException {
        if (temperature < 0.0 || temperature > 2.0) {
            throw new ConfigurationException("Temperature must be between 0.0 and 2.0, got: " + temperature);
        }
    }
    
    private void validateMaxTokensRange(int maxTokens) throws ConfigurationException {
        if (maxTokens < 1 || maxTokens > 8192) {
            throw new ConfigurationException("Max tokens must be between 1 and 8192, got: " + maxTokens);
        }
    }
}
```

### YAML Configuration Examples

**Agent Configuration (conf/ai/agents/energy-agent.yaml)**:
```yaml
agent:
  id: "energy-agent"
  name: "Energy Management Agent"
  version: "1.0.0"
  description: "AI agent for energy optimization and management"
  
  capabilities:
    - "energy_monitoring"
    - "schedule_optimization"
    - "cost_analysis"
    - "device_control"
    
  autonomy:
    level: "high"
    confidence_threshold: 0.8
    max_actions_per_hour: 10
    require_confirmation: false
    
  learning:
    enabled: true
    adaptation_rate: 0.1
    memory_retention_days: 30
    feedback_integration: true
    
  safety:
    max_power_change_percent: 15
    max_temperature_change_celsius: 3
    emergency_override_enabled: true
    audit_all_actions: true
    
  communication:
    protocols: ["a2a", "mcp"]
    push_notifications_enabled: true
    event_streaming_enabled: true
    
  persistence:
    enabled: true
    storage_key: "energy-agent-data"
    backup_enabled: true
    retention_days: 90
```

**Policy Configuration (conf/ai/policies/energy-policies.yaml)**:
```yaml
policies:
  energy_management:
    version: "1.0.0"
    description: "Energy management specific policies"
    
    constraints:
      power_management:
        max_instantaneous_power: "5000W"
        max_daily_energy: "50kWh"
        peak_shaving_enabled: true
        load_balancing_enabled: true
        
      device_safety:
        min_operating_temperature: "5°C"
        max_operating_temperature: "35°C"
        voltage_protection: true
        current_limiting: true
        
      user_comfort:
        min_indoor_temperature: "18°C"
        max_indoor_temperature: "26°C"
        humidity_range: "30-70%"
        lighting_minimum: "100 lux"
        
    rules:
      - name: "peak_shaving"
        condition: "grid_demand > threshold"
        action: "reduce_non_essential_loads"
        priority: "high"
        
      - name: "comfort_violation"
        condition: "temperature < 18°C OR temperature > 26°C"
        action: "override_energy_savings"
        priority: "critical"
        
      - name: "renewable_optimization"
        condition: "solar_production > 0"
        action: "maximize_renewable_usage"
        priority: "medium"
```

## Success Criteria

### Phase 1: OSGi Configuration
- [ ] All configuration services have proper validation and error handling
- [ ] Configuration validation works for all settings with `ai.{domain}.*` key patterns
- [ ] Configuration changes are properly handled via @Modified
- [ ] Configuration errors are logged and recovered from

### Phase 2: YAML Configuration
- [ ] YAML files are properly loaded and parsed
- [ ] YAML validation catches configuration errors
- [ ] YAML file changes trigger automatic reload
- [ ] YAML configuration is accessible through services

### Phase 3: Integration
- [ ] Configuration precedence works correctly
- [ ] UI integration provides user-friendly configuration
- [ ] Configuration changes trigger appropriate events
- [ ] Configuration backup/restore works

### Phase 4: Testing
- [ ] All configuration scenarios are tested
- [ ] Error handling is thoroughly tested
- [ ] Performance is acceptable under load
- [ ] Documentation is complete and accurate

## Risk Mitigation

### Configuration Complexity
- **Risk**: YAML configuration becomes too complex
- **Mitigation**: Provide clear examples and documentation
- **Mitigation**: Implement schema validation for YAML files

### Performance Impact
- **Risk**: Configuration loading impacts startup time
- **Mitigation**: Implement lazy loading for YAML files
- **Mitigation**: Add configuration caching

### User Experience
- **Risk**: Configuration is too complex for users
- **Mitigation**: Provide UI integration for common settings
- **Mitigation**: Create configuration wizards for complex setups

### Backward Compatibility
- **Risk**: Changes break existing configurations
- **Mitigation**: Maintain backward compatibility for .cfg files
- **Mitigation**: Provide migration tools for configuration updates

## Timeline and Dependencies

### Week 1-2: OSGi Configuration Enhancement
- **Dependencies**: None
- **Deliverables**: Enhanced validation, error handling, updated services

### Week 2-3: YAML Infrastructure
- **Dependencies**: Phase 1 completion
- **Deliverables**: YAML repositories, parsers, file monitoring

### Week 3-4: Integration
- **Dependencies**: Phase 2 completion
- **Deliverables**: Unified configuration, UI integration, documentation

### Week 4-5: Testing
- **Dependencies**: Phase 3 completion
- **Deliverables**: Comprehensive testing, error handling, performance validation

## Conclusion

This plan provides a comprehensive configuration management strategy that:

1. **Follows openHAB standards** for OSGi DS and file-based configuration
2. **Separates concerns** between simple settings (.cfg) and complex data (YAML)
3. **Provides flexibility** for different types of configuration needs
4. **Ensures reliability** through proper validation and error handling
5. **Maintains usability** through UI integration and documentation

The combination of OSGi Config Admin for core settings and YAML for complex structured data provides the best of both worlds: reliability and flexibility.