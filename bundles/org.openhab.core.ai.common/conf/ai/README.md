# OpenHAB AI - LLM Configuration

This directory contains configuration files for the OpenHAB AI system's LLM (Large Language Model) providers.

## Configuration Files

### `llm.cfg` - Main LLM Configuration
This is the primary configuration file for all LLM providers. It follows openHAB's standard configuration format and supports auto-reload.

## File-Based Configuration System

The LLM configuration system integrates with openHAB's file-based configuration management using the standard openHAB WatchService:

- **Auto-Reload**: Changes to `llm.cfg` are automatically detected and applied
- **Hot-Reload**: No restart required when modifying configuration
- **Standard WatchService**: Uses openHAB's centralized file watching infrastructure
- **Default Creation**: If `llm.cfg` doesn't exist, a default configuration is created automatically
- **Proper OSGi Integration**: Follows openHAB's standard service patterns

## Configuration Format

The configuration uses the standard openHAB format:
```
provider.setting=value
```

### Global Settings
```
# Primary and fallback providers
primary.provider=ollama
fallback.provider=openai

# System-wide settings
hybrid.enabled=true
load.balancing.enabled=false
default.temperature=0.3
default.maxTokens=1000
default.timeoutMs=30000
default.retryAttempts=3
```

### Provider-Specific Settings
```
# Enable/disable providers
openai.enabled=true
anthropic.enabled=false
ollama.enabled=true

# Provider configuration
openai.apiKey=your_api_key_here
openai.defaultModel=gpt-4o-mini
openai.timeoutMs=30000

ollama.baseUrl=http://localhost:11434
ollama.defaultModel=llama3.1:8b
ollama.timeoutMs=60000
```

## Supported Providers

### Cloud Providers
- **OpenAI**: GPT-4, GPT-3.5, and other OpenAI models
- **Anthropic**: Claude 3.5 Sonnet and other Claude models
- **Google GenAI**: Gemini 1.5 Pro and other Google models
- **Azure OpenAI**: OpenAI models hosted on Azure

### Local Providers
- **Ollama**: Local LLM models (llama3.1, mistral, etc.)
- **LocalAI**: OpenAI-compatible local models
- **vLLM**: High-performance local inference
- **LM Studio**: Local model management and inference

## Quick Start

1. **Edit the configuration**:
   ```bash
   nano conf/ai/llm.cfg
   ```

2. **Enable your preferred providers**:
   ```ini
   # For local development with Ollama
   ollama.enabled=true
   ollama.baseUrl=http://localhost:11434
   ollama.defaultModel=llama3.1:8b
   
   # For cloud providers (add your API keys)
   openai.enabled=true
   openai.apiKey=your_openai_api_key_here
   openai.defaultModel=gpt-4o-mini
   ```

3. **Set primary and fallback providers**:
   ```ini
   primary.provider=ollama
   fallback.provider=openai
   ```

4. **Save the file** - changes are applied automatically

## Configuration Examples

### Local Development Setup
```ini
# Use Ollama as primary, OpenAI as fallback
primary.provider=ollama
fallback.provider=openai

# Enable hybrid mode for testing
hybrid.enabled=true
load.balancing.enabled=false

# Ollama configuration
ollama.enabled=true
ollama.baseUrl=http://localhost:11434
ollama.defaultModel=llama3.1:8b
ollama.timeoutMs=60000

# OpenAI fallback (disabled for local development)
openai.enabled=false
```

### Production Cloud Setup
```ini
# Use OpenAI as primary, Anthropic as fallback
primary.provider=openai
fallback.provider=anthropic

# Enable load balancing
hybrid.enabled=true
load.balancing.enabled=true

# OpenAI configuration
openai.enabled=true
openai.apiKey=sk-your-openai-key-here
openai.defaultModel=gpt-4o-mini
openai.timeoutMs=30000

# Anthropic configuration
anthropic.enabled=true
anthropic.apiKey=sk-ant-your-anthropic-key-here
anthropic.defaultModel=claude-3-5-sonnet-20241022
anthropic.timeoutMs=30000

# Disable local providers in production
ollama.enabled=false
localai.enabled=false
vllm.enabled=false
lmstudio.enabled=false
```

### Multi-Provider Setup
```ini
# Enable multiple providers for different use cases
primary.provider=openai
fallback.provider=anthropic

# Enable hybrid mode
hybrid.enabled=true
load.balancing.enabled=true

# OpenAI for general tasks
openai.enabled=true
openai.apiKey=sk-your-openai-key-here
openai.defaultModel=gpt-4o-mini

# Anthropic for reasoning tasks
anthropic.enabled=true
anthropic.apiKey=sk-ant-your-anthropic-key-here
anthropic.defaultModel=claude-3-5-sonnet-20241022

# Google for creative tasks
google.enabled=true
google.apiKey=your-google-api-key-here
google.defaultModel=gemini-1.5-pro

# Ollama for local development
ollama.enabled=true
ollama.baseUrl=http://localhost:11434
ollama.defaultModel=llama3.1:8b
```

## Advanced Configuration

### Performance Tuning
```ini
# Increase timeouts for local models
ollama.timeoutMs=120000
localai.timeoutMs=120000
vllm.timeoutMs=120000

# Reduce retry attempts for faster failure detection
ollama.retryAttempts=1
localai.retryAttempts=1

# Increase timeouts for cloud providers
openai.timeoutMs=45000
anthropic.timeoutMs=45000
```

### Cost Optimization
```ini
# Use cheaper models for non-critical tasks
openai.defaultModel=gpt-3.5-turbo
anthropic.defaultModel=claude-3-haiku-20240307

# Enable cost tracking
cost.tracking.enabled=true
```

### Security Configuration
```ini
# Use environment variables for API keys
openai.apiKey=${OPENAI_API_KEY}
anthropic.apiKey=${ANTHROPIC_API_KEY}
google.apiKey=${GOOGLE_API_KEY}

# Disable debug logging in production
debug.enabled=false
```

## Environment Variables

For security, you can use environment variables for sensitive configuration:

```bash
# Set environment variables
export OPENAI_API_KEY="sk-your-openai-key-here"
export ANTHROPIC_API_KEY="sk-ant-your-anthropic-key-here"
export GOOGLE_API_KEY="your-google-api-key-here"

# Reference in configuration
openai.apiKey=${OPENAI_API_KEY}
anthropic.apiKey=${ANTHROPIC_API_KEY}
google.apiKey=${GOOGLE_API_KEY}
```

## Troubleshooting

### Configuration Not Loading
1. Check file permissions: `ls -la conf/ai/llm.cfg`
2. Verify syntax: No spaces around `=` signs
3. Check logs for configuration errors

### Auto-Reload Not Working
1. Ensure the `conf/ai/` directory exists
2. Check that the file watcher service is running
3. Verify file modification timestamps

### Provider Connection Issues
1. Check API keys are correct
2. Verify network connectivity
3. Check provider-specific logs
4. Test with curl or other tools

### Performance Issues
1. Increase timeout values for slow providers
2. Reduce retry attempts for faster failure detection
3. Enable debug logging to identify bottlenecks

## Logging

The LLM configuration system provides detailed logging:

- **Configuration loading**: `DEBUG` level
- **File watching**: `INFO` level
- **Provider creation**: `DEBUG` level
- **Errors**: `WARN` or `ERROR` level

Enable debug logging to see detailed configuration processing:
```ini
debug.enabled=true
```

## Integration with openHAB

The LLM configuration system integrates seamlessly with openHAB:

- **OSGi Service**: Available as `LLMConfigurationService`
- **File Watching**: Automatic reload on configuration changes
- **Provider Management**: Dynamic provider creation and management
- **Health Monitoring**: Built-in health checks and monitoring

## Next Steps

1. **Configure your preferred providers** in `llm.cfg`
2. **Test the configuration** by restarting the AI bundle
3. **Monitor the logs** for any configuration issues
4. **Implement LLM clients** for your chosen providers

For more information, see the main project documentation and the `BRAIN.md` architectural overview. 