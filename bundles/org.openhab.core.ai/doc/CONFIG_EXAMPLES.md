# Complete Configuration Examples for openHAB AI Bundle

## Overview

This document provides complete examples for both .cfg files (OSGi Config Admin) and YAML files (complex structured data) based on the actual implementation in the openHAB AI bundle.

## 1. OSGi Config Admin (.cfg files)

### 1.1 Complete ai-common.cfg

```properties
# =============================================================================
# openHAB AI Common Bundle Configuration
# =============================================================================

# Bundle Identity
ai.common.bundle.name=openHAB AI Common
ai.common.bundle.version=1.0.0
ai.common.bundle.description=Shared foundation for AI protocol implementations

# Protocol Support
ai.common.protocols.mcp.enabled=true
ai.common.protocols.a2a.enabled=true
ai.common.protocols.future.enabled=false

# =============================================================================
# Logging Configuration
# =============================================================================

# Global Logging Settings
ai.common.logging.level=INFO
ai.common.logging.structured=true
ai.common.logging.include.metadata=true
ai.common.logging.performance.tracking=true
ai.common.logging.audit.enabled=true

# Logging Categories
ai.common.logging.category.common=INFO
ai.common.logging.category.auth=INFO
ai.common.logging.category.config=INFO
ai.common.logging.category.integration=INFO
ai.common.logging.category.stub=DEBUG
ai.common.logging.category.util=DEBUG

# Logging Format
ai.common.logging.format=json
ai.common.logging.timestamp.format=ISO-8601
ai.common.logging.include.thread.id=true
ai.common.logging.include.correlation.id=true

# Logging Retention
ai.common.logging.retention.days=30
ai.common.logging.rotation.enabled=true
ai.common.logging.rotation.max.size=100MB
ai.common.logging.rotation.max.files=10

# =============================================================================
# Security Configuration
# =============================================================================

# Authentication Framework
ai.common.security.auth.enabled=true
ai.common.security.auth.providers=openhab_users,oauth2.1,api_key,jwt
ai.common.security.auth.audit.logging=true
ai.common.security.auth.session.timeout=3600s

# Authorization Framework
ai.common.security.authorization.enabled=true
ai.common.security.role.based.access=true
ai.common.security.permission.levels=read,write,admin
ai.common.security.permission.inheritance=true

# Security Features
ai.common.security.rate.limiting.enabled=true
ai.common.security.request.validation.enabled=true
ai.common.security.input.sanitization.enabled=true
ai.common.security.output.encoding.enabled=true

# Security Headers
ai.common.security.headers.enabled=true
ai.common.security.headers.cors.enabled=true
ai.common.security.headers.csrf.enabled=true
ai.common.security.headers.content.security.policy.enabled=true

# =============================================================================
# Integration Configuration
# =============================================================================

# openHAB Integration
ai.common.openhab.integration.enabled=true
ai.common.openhab.service.discovery.enabled=true
ai.common.openhab.service.fallback.enabled=true
ai.common.openhab.service.timeout=30s

# Service Integration
ai.common.integration.ready.service.enabled=true
ai.common.integration.storage.service.enabled=true
ai.common.integration.persistence.service.enabled=true
ai.common.integration.item.registry.enabled=true
ai.common.integration.thing.registry.enabled=true

# Integration Timeouts
ai.common.integration.timeout.default=30s
ai.common.integration.timeout.storage=10s
ai.common.integration.timeout.persistence=15s
ai.common.integration.timeout.registry=5s

# =============================================================================
# Storage Configuration
# =============================================================================

# Storage Service Integration
ai.common.storage.service.default=mapdb
ai.common.storage.service.fallback=json
ai.common.storage.service.timeout=10s
ai.common.storage.service.retry.attempts=3

# Storage Configuration
ai.common.storage.data.directory=${OPENHAB_USERDATA}/ai
ai.common.storage.config.directory=${OPENHAB_CONFIG}/ai
ai.common.storage.backup.enabled=true
ai.common.storage.backup.interval=24h

# Storage Retention
ai.common.storage.retention.default=30d
ai.common.storage.retention.config=1y
ai.common.storage.retention.logs=90d
ai.common.storage.retention.temp=7d

# =============================================================================
# Configuration Management
# =============================================================================

# Configuration Sources
ai.common.config.sources=file,environment,service
ai.common.config.file.enabled=true
ai.common.config.environment.enabled=true
ai.common.config.service.enabled=true

# Configuration Validation
ai.common.config.validation.enabled=true
ai.common.config.validation.strict=false
ai.common.config.validation.schema.enabled=true

# Configuration Reload
ai.common.config.reload.enabled=true
ai.common.config.reload.interval=60s
ai.common.config.reload.hot.enabled=false

# =============================================================================
# Stub Framework Configuration
# =============================================================================

# Stub Framework Enablement
ai.common.stub.framework.enabled=true
ai.common.stub.framework.auto.start=false
ai.common.stub.framework.auto.stop=true

# Stub Services
ai.common.stub.services.http.enabled=true
ai.common.stub.services.websocket.enabled=true
ai.common.stub.services.mqtt.enabled=true
ai.common.stub.services.coap.enabled=false

# Stub Service Ports
ai.common.stub.ports.http=8080
ai.common.stub.ports.websocket=8081
ai.common.stub.ports.mqtt=1883
ai.common.stub.ports.coap=5683

# Stub Service Configuration
ai.common.stub.services.timeout=30s
ai.common.stub.services.retry.attempts=3
ai.common.stub.services.fallback.enabled=true

# =============================================================================
# Utility Configuration
# =============================================================================

# Utility Features
ai.common.util.json.processing.enabled=true
ai.common.util.xml.processing.enabled=true
ai.common.util.yaml.processing.enabled=true
ai.common.util.encryption.enabled=true

# Utility Performance
ai.common.util.cache.enabled=true
ai.common.util.cache.size=1000
ai.common.util.cache.ttl=3600s
ai.common.util.thread.pool.size=10

# =============================================================================
# Performance Configuration
# =============================================================================

# Performance Monitoring
ai.common.performance.monitoring.enabled=true
ai.common.performance.metrics.enabled=true
ai.common.performance.profiling.enabled=false
ai.common.performance.health.checks.enabled=true

# Performance Limits
ai.common.performance.max.memory.usage=512MB
ai.common.performance.max.thread.count=50
ai.common.performance.max.concurrent.operations=100

# Performance Optimization
ai.common.performance.caching.enabled=true
ai.common.performance.compression.enabled=true
ai.common.performance.connection.pooling.enabled=true

# =============================================================================
# Development Configuration
# =============================================================================

# Development Mode
ai.common.dev.mode.enabled=false
ai.common.dev.debug.enabled=false
ai.common.dev.hot.reload.enabled=false
ai.common.dev.metrics.dashboard.enabled=false

# Development Features
ai.common.dev.features.configuration.reload=true
ai.common.dev.features.service.discovery=true
ai.common.dev.features.stub.framework=true
ai.common.dev.features.testing.framework=true

# Development Logging
ai.common.dev.logging.verbose=false
ai.common.dev.logging.requests=true
ai.common.dev.logging.responses=false
ai.common.dev.logging.performance=true

# =============================================================================
# Testing Configuration
# =============================================================================

# Testing Framework
ai.common.testing.framework.enabled=true
ai.common.testing.stub.services.enabled=true
ai.common.testing.mock.services.enabled=true
ai.common.testing.integration.tests.enabled=true

# Testing Settings
ai.common.testing.timeout=60s
ai.common.testing.retry.attempts=3
ai.common.testing.cleanup.enabled=true
ai.common.testing.isolation.enabled=true

# =============================================================================
# Error Handling Configuration
# =============================================================================

# Error Handling
ai.common.error.handling.enabled=true
ai.common.error.recovery.enabled=true
ai.common.error.logging.enabled=true
ai.common.error.notification.enabled=false

# Error Settings
ai.common.error.max.retries=3
ai.common.error.backoff.delay=1s
ai.common.error.timeout=30s
ai.common.error.fallback.enabled=true
```

### 1.2 Complete a2a.cfg

```properties
# =============================================================================
# openHAB A2A Bundle Configuration
# =============================================================================

# Server Identity
a2a.server.id=openhab-a2a-server
a2a.server.name=openHAB A2A Server
a2a.server.version=1.0.0
a2a.server.description=openHAB A2A Server for Multi-Agent Coordination

# Server Features
a2a.server.enable.skills=true
a2a.server.enable.tasks=true
a2a.server.enable.push.notifications=true
a2a.server.enable.persistence=true

# =============================================================================
# Persistence Configuration
# =============================================================================

# Persistence Enablement
a2a.persistence.enabled=true
a2a.persistence.service=mapdb
a2a.persistence.backup.enabled=true
a2a.persistence.backup.interval=24h
a2a.persistence.backup.retention=7d

# Data Retention Settings
a2a.persistence.retention.tasks=30d
a2a.persistence.retention.executions=90d
a2a.persistence.retention.statistics=1y
a2a.persistence.retention.logs=30d
a2a.persistence.retention.metadata=1y

# Storage Configuration
a2a.storage.tasks.key=a2a-tasks
a2a.storage.metadata.key=a2a-metadata
a2a.storage.statistics.key=a2a-statistics
a2a.storage.config.key=a2a-config
a2a.storage.recovery.key=a2a-recovery
a2a.storage.push.notifications.key=a2a-push-notifications

# =============================================================================
# Task Execution Configuration
# =============================================================================

# Execution Limits
a2a.execution.max.concurrent.tasks=10
a2a.execution.max.queue.size=100
a2a.execution.timeout=300s
a2a.execution.cleanup.interval=60s

# Retry Policy
a2a.execution.retry.max.attempts=3
a2a.execution.retry.backoff.multiplier=2.0
a2a.execution.retry.initial.delay=1s
a2a.execution.retry.max.delay=60s

# Task Lifecycle
a2a.execution.task.states=CREATED,VALIDATED,QUEUED,EXECUTING,COMPLETED,FAILED,CANCELLED,TIMEOUT
a2a.execution.task.timeout=300s
a2a.execution.task.cancellation.enabled=true

# =============================================================================
# Push Notifications Configuration
# =============================================================================

# Push Notification Enablement
a2a.push.notifications.enabled=true
a2a.push.notifications.persistence.enabled=true
a2a.push.notifications.validation.enabled=true

# Push Notification Settings
a2a.push.notifications.max.retries=3
a2a.push.notifications.retry.delay=5s
a2a.push.notifications.timeout=30s
a2a.push.notifications.batch.size=10

# Push Notification Types
a2a.push.notifications.types=task.completed,task.failed,task.timeout,system.alert,agent.message
a2a.push.notifications.priority.levels=low,normal,high,critical

# =============================================================================
# Agent Management Configuration
# =============================================================================

# Agent Coordination
a2a.agents.max.count=50
a2a.agents.coordination.enabled=true
a2a.agents.communication.enabled=true
a2a.agents.negotiation.enabled=true

# Agent Security
a2a.agents.security.enabled=true
a2a.agents.authentication.required=true
a2a.agents.authorization.enabled=true
a2a.agents.audit.logging.enabled=true

# Agent Lifecycle
a2a.agents.registration.enabled=true
a2a.agents.heartbeat.interval=30s
a2a.agents.timeout=120s
a2a.agents.cleanup.interval=300s

# =============================================================================
# Skills Configuration
# =============================================================================

# Skills Enablement
a2a.skills.enabled=true
a2a.skills.auto.discovery=true
a2a.skills.validation.enabled=true
a2a.skills.caching.enabled=true

# Skills Categories
a2a.skills.categories=items,things,channels,rules,config,system,security,filesystem,scripts,persistence,events,bindings,discovery,monitoring,analytics,automation

# Skills Performance
a2a.skills.max.concurrent.executions=5
a2a.skills.execution.timeout=60s
a2a.skills.cache.size=1000
a2a.skills.cache.ttl=3600s

# =============================================================================
# Security Configuration
# =============================================================================

# Authentication
a2a.security.auth.enabled=true
a2a.security.auth.methods=oauth2.1,openhab_users,api_key,jwt
a2a.security.auth.primary.method=oauth2.1
a2a.security.auth.fallback.method=openhab_users

# Protocol-Specific Authentication Settings
a2a.security.auth.protocol.name=a2a
a2a.security.auth.protocol.permissions=a2a:read,a2a:send,a2a:receive,a2a:task,a2a:admin
a2a.security.auth.protocol.session.timeout=3600
a2a.security.auth.protocol.session.refresh.enabled=true
a2a.security.auth.protocol.session.refresh.threshold=300

# Permission Mapping Configuration
a2a.security.auth.permissions.read.operations=get_agent_card,get_health,get_status,get_task_status
a2a.security.auth.permissions.send.operations=send_message,create_task,notify_agent
a2a.security.auth.permissions.receive.operations=receive_message,receive_task,receive_notification
a2a.security.auth.permissions.task.operations=get_task,cancel_task,update_task,execute_task
a2a.security.auth.permissions.admin.operations=manage_agents,configure_server,manage_users,view_analytics

# Session Management Configuration
a2a.security.auth.session.timeout.seconds=3600
a2a.security.auth.session.max.concurrent=10
a2a.security.auth.session.cleanup.interval=300
a2a.security.auth.session.invalidation.enabled=true

# Authorization
a2a.security.authorization.enabled=true
a2a.security.role.based.access=true
a2a.security.permission.levels=read,write,admin

# Rate Limiting
a2a.security.rate.limit.enabled=true
a2a.security.rate.limit.requests.per.minute=1000
a2a.security.rate.limit.max.connections=100

# =============================================================================
# Security Policy Configuration
# =============================================================================

# Rate Limiting Configuration per Authentication Scheme
a2a.security.rate.limit.oauth.requests.per.minute=1000
a2a.security.rate.limit.openhab.users.requests.per.minute=500
a2a.security.rate.limit.api.key.requests.per.minute=2000
a2a.security.rate.limit.jwt.requests.per.minute=1000

# Access Control Configuration per Protocol
a2a.security.access.control.a2a.read.enabled=true
a2a.security.access.control.a2a.send.enabled=true
a2a.security.access.control.a2a.receive.enabled=true
a2a.security.access.control.a2a.task.enabled=true
a2a.security.access.control.a2a.admin.enabled=false

# Audit Logging Configuration
a2a.security.audit.logging.enabled=true
a2a.security.audit.logging.level=INFO
a2a.security.audit.logging.events=authentication,authorization,session,permission_check,message_send,message_receive,task_execution
a2a.security.audit.logging.retention.days=90

# Security Monitoring Configuration
a2a.security.monitoring.enabled=true
a2a.security.monitoring.incident.detection.enabled=true
a2a.security.incident.response.auto.block.enabled=false
a2a.security.incident.response.block.duration=3600
a2a.security.incident.response.notification.enabled=true

# =============================================================================
# Logging Configuration
# =============================================================================

# Logging Levels
a2a.logging.level=INFO
a2a.logging.structured=true
a2a.logging.include.metadata=true
a2a.logging.performance.tracking=true

# Logging Categories
a2a.logging.category.server=INFO
a2a.logging.category.tasks=DEBUG
a2a.logging.category.skills=INFO
a2a.logging.category.persistence=INFO
a2a.logging.category.security=WARN
a2a.logging.category.agents=INFO

# Logging Retention
a2a.logging.retention.days=30
a2a.logging.rotation.enabled=true
a2a.logging.rotation.max.size=100MB
a2a.logging.rotation.max.files=10

# =============================================================================
# Performance Configuration
# =============================================================================

# Memory Management
a2a.performance.max.memory.usage=1GB
a2a.performance.gc.optimization=true
a2a.performance.thread.pool.size=20

# Caching
a2a.performance.cache.enabled=true
a2a.performance.cache.size=1000
a2a.performance.cache.ttl=3600s

# Monitoring
a2a.performance.metrics.enabled=true
a2a.performance.metrics.collection.interval=60s
a2a.performance.health.checks.enabled=true

# =============================================================================
# Recovery Configuration
# =============================================================================

# Recovery Enablement
a2a.recovery.enabled=true
a2a.recovery.auto.restart=true
a2a.recovery.state.persistence=true

# Recovery Settings
a2a.recovery.max.attempts=3
a2a.recovery.backoff.delay=5s
a2a.recovery.timeout=60s
a2a.recovery.cleanup.enabled=true

# =============================================================================
# Development and Debugging
# =============================================================================

# Debug Mode
a2a.debug.enabled=false
a2a.debug.log.task.executions=true
a2a.debug.log.skill.calls=true
a2a.debug.log.persistence.operations=true

# Development Features
a2a.dev.enable.hot.reload=false
a2a.dev.enable.skill.discovery=true
a2a.dev.enable.configuration.reload=true
a2a.dev.enable.metrics.dashboard=true
```

### 1.3 Complete mcp.cfg

```properties
# =============================================================================
# openHAB MCP Bundle Configuration
# =============================================================================

# Server Identity
mcp.server.id=openhab-mcp-server
mcp.server.name=openHAB MCP Server
mcp.server.version=1.0.0
mcp.server.description=openHAB MCP Server for AI Integration

# Transport Configuration
mcp.transport.type=STDIO
mcp.transport.base.url=http://localhost:8080
mcp.transport.message.endpoint=/mcp/message
mcp.transport.sse.endpoint=/mcp/events
mcp.transport.enable.sse=true

# Feature Enablement
mcp.features.enable.tools=true
mcp.features.enable.resources=true
mcp.features.enable.prompts=true
mcp.features.enable.logging=true

# Async Server Configuration
mcp.async.enable.server=false
mcp.async.enable.tools=false
mcp.async.thread.pool.size=10
mcp.async.queue.capacity=1000
mcp.async.enable.completions=false

# =============================================================================
# Authentication Configuration
# =============================================================================

# Authentication Enablement
mcp.auth.enabled=true
mcp.auth.enable.request.validation=true

# Authentication Method Selection
mcp.auth.primary.method=oauth2.1
mcp.auth.fallback.method=openhab_users
mcp.auth.enable.fallback=true

# Protocol-Specific Authentication Settings
mcp.auth.protocol.name=mcp
mcp.auth.protocol.permissions=mcp:read,mcp:write,mcp:execute,mcp:admin
mcp.auth.protocol.session.timeout=3600
mcp.auth.protocol.session.refresh.enabled=true
mcp.auth.protocol.session.refresh.threshold=300

# Permission Mapping Configuration
mcp.auth.permissions.read.operations=list_tools,list_resources,list_prompts,get_tool,get_resource,get_prompt
mcp.auth.permissions.write.operations=create_tool,create_resource,create_prompt,update_tool,update_resource,update_prompt
mcp.auth.permissions.execute.operations=call_tool,execute_tool,run_tool
mcp.auth.permissions.admin.operations=delete_tool,delete_resource,delete_prompt,configure_server,manage_users

# Session Management Configuration
mcp.auth.session.timeout.seconds=3600
mcp.auth.session.max.concurrent=10
mcp.auth.session.cleanup.interval=300
mcp.auth.session.invalidation.enabled=true

# OAuth 2.1 Configuration
mcp.oauth.issuer.url=
mcp.oauth.client.id=
mcp.oauth.client.secret=
mcp.oauth.redirect.uri=http://localhost:8080/callback
mcp.oauth.pkce.enabled=true
mcp.oauth.scope=mcp:read mcp:write mcp:execute mcp:admin
mcp.oauth.token.validation.enabled=true
mcp.oauth.token.refresh.enabled=true

# openHAB Users Authentication
mcp.openhab.users.file=
mcp.openhab.users.enabled=true
mcp.openhab.users.permission.mapping.enabled=true
mcp.openhab.users.default.permissions=mcp:read

# API Key Authentication
mcp.api.key.header=X-API-Key
mcp.api.key.value=
mcp.api.key.enabled=false
mcp.api.key.permission.mapping.enabled=true
mcp.api.key.default.permissions=mcp:read,mcp:execute

# JWT Authentication
mcp.jwt.secret=
mcp.jwt.issuer=openhab-mcp
mcp.jwt.expiration.minutes=60
mcp.jwt.enabled=false
mcp.jwt.permission.mapping.enabled=true
mcp.jwt.default.permissions=mcp:read,mcp:execute

# =============================================================================
# Security Policy Configuration
# =============================================================================

# Rate Limiting Configuration per Authentication Scheme
mcp.security.rate.limit.oauth.requests.per.minute=1000
mcp.security.rate.limit.openhab.users.requests.per.minute=500
mcp.security.rate.limit.api.key.requests.per.minute=2000
mcp.security.rate.limit.jwt.requests.per.minute=1000

# Access Control Configuration per Protocol
mcp.security.access.control.mcp.read.enabled=true
mcp.security.access.control.mcp.write.enabled=true
mcp.security.access.control.mcp.execute.enabled=true
mcp.security.access.control.mcp.admin.enabled=false

# Audit Logging Configuration
mcp.security.audit.logging.enabled=true
mcp.security.audit.logging.level=INFO
mcp.security.audit.logging.events=authentication,authorization,session,permission_check
mcp.security.audit.logging.retention.days=90

# Security Monitoring Configuration
mcp.security.monitoring.enabled=true
mcp.security.monitoring.incident.detection.enabled=true
mcp.security.monitoring.alert.threshold=10
mcp.security.monitoring.alert.window=300

# Incident Response Configuration
mcp.security.incident.response.enabled=true
mcp.security.incident.response.auto.block.enabled=false
mcp.security.incident.response.block.duration=3600
mcp.security.incident.response.notification.enabled=true

# =============================================================================
# Security and Rate Limiting
# =============================================================================

# Connection Limits
mcp.connections.max=100
mcp.rate.limit.per.minute=1000

# Security Settings
mcp.security.enable.metrics=true
mcp.security.enable.audit.logging=true
mcp.security.enable.request.validation=true

# =============================================================================
# Logging Configuration
# =============================================================================

# Logging Levels
mcp.logging.level=INFO
mcp.logging.structured=true
mcp.logging.include.metadata=true
mcp.logging.performance.tracking=true

# Logging Categories
mcp.logging.category.server=INFO
mcp.logging.category.tools=DEBUG
mcp.logging.category.transport=INFO
mcp.logging.category.security=WARN
mcp.logging.category.auth=INFO

# =============================================================================
# Performance Configuration
# =============================================================================

# Timeout Settings
mcp.timeout.connection=30000
mcp.timeout.request=60000
mcp.timeout.tool.execution=300000

# Resource Limits
mcp.resources.max.memory.usage=512MB
mcp.resources.max.concurrent.requests=50
mcp.resources.max.tool.executions=10

# =============================================================================
# Health Monitoring
# =============================================================================

# Health Check Configuration
mcp.health.enabled=true
mcp.health.endpoint=/health
mcp.health.check.interval=30s
mcp.health.timeout=10s

# Metrics Configuration
mcp.metrics.enabled=true
mcp.metrics.endpoint=/metrics
mcp.metrics.collection.interval=60s
mcp.metrics.retention.days=7

# =============================================================================
# Development and Debugging
# =============================================================================

# Debug Mode
mcp.debug.enabled=false
mcp.debug.log.requests=true
mcp.debug.log.responses=true
mcp.debug.log.tool.executions=true

# Development Features
mcp.dev.enable.hot.reload=false
mcp.dev.enable.tool.discovery=true
mcp.dev.enable.configuration.reload=true
```

### 1.4 Complete model.cfg

```properties
# =============================================================================
# OpenHAB AI - Model Configuration
# =============================================================================

# =============================================================================
# GLOBAL MODEL SETTINGS
# =============================================================================

# Primary model provider (used for most requests)
# Options: openai, anthropic, google, azure, ollama, localai, vllm, lmstudio
model.primary.provider=ollama

# Fallback model provider (used when primary fails)
# Options: openai, anthropic, google, azure, ollama, localai, vllm, lmstudio
model.fallback.provider=openai

# Enable hybrid mode (use multiple providers for different tasks)
model.hybrid.enabled=true

# Enable load balancing between providers
model.load.balancing.enabled=false

# Default parameters for model requests
model.default.temperature=0.3
model.default.maxTokens=1000
model.default.timeoutMs=30000
model.default.retryAttempts=3

# =============================================================================
# OPENAI CONFIGURATION
# =============================================================================

# OpenAI API Key (required for OpenAI provider)
# Get from: https://platform.openai.com/api-keys
openai.apiKey=your_openai_api_key_here

# OpenAI Organization ID (optional)
# openai.organizationId=org-your_org_id_here

# OpenAI Base URL (optional, defaults to https://api.openai.com/v1)
# openai.baseUrl=https://api.openai.com/v1

# Default OpenAI model
openai.defaultModel=gpt-4o-mini

# OpenAI timeout in milliseconds
openai.timeoutMs=30000

# OpenAI retry attempts
openai.retryAttempts=3

# Enable OpenAI provider
openai.enabled=true

# =============================================================================
# ANTHROPIC CONFIGURATION
# =============================================================================

# Anthropic API Key (required for Anthropic provider)
# Get from: https://console.anthropic.com/
anthropic.apiKey=your_anthropic_api_key_here

# Anthropic Base URL (optional, defaults to https://api.anthropic.com)
# anthropic.baseUrl=https://api.anthropic.com

# Default Anthropic model
anthropic.defaultModel=claude-3-5-sonnet-20241022

# Anthropic timeout in milliseconds
anthropic.timeoutMs=30000

# Anthropic retry attempts
anthropic.retryAttempts=3

# Enable Anthropic provider
anthropic.enabled=false

# =============================================================================
# GOOGLE GENAI CONFIGURATION
# =============================================================================

# Google API Key (required for Google GenAI provider)
# Get from: https://makersuite.google.com/app/apikey
google.apiKey=your_google_api_key_here

# Google Base URL (optional, defaults to https://generativelanguage.googleapis.com)
# google.baseUrl=https://generativelanguage.googleapis.com

# Default Google model
google.defaultModel=gemini-1.5-pro

# Google timeout in milliseconds
google.timeoutMs=30000

# Google retry attempts
google.retryAttempts=3

# Enable Google provider
google.enabled=false

# =============================================================================
# AZURE OPENAI CONFIGURATION
# =============================================================================

# Azure OpenAI API Key (required for Azure provider)
# Get from: Azure Portal > OpenAI Service > Keys and Endpoint
azure.apiKey=your_azure_api_key_here

# Azure OpenAI Endpoint (required for Azure provider)
# Format: https://your-resource-name.openai.azure.com/
azure.endpoint=https://your-resource-name.openai.azure.com/

# Azure OpenAI Deployment Name (required for Azure provider)
azure.deploymentName=your-deployment-name

# Azure OpenAI API Version (optional, defaults to 2024-02-15-preview)
azure.apiVersion=2024-02-15-preview

# Azure timeout in milliseconds
azure.timeoutMs=30000

# Azure retry attempts
azure.retryAttempts=3

# Enable Azure provider
azure.enabled=false

# =============================================================================
# OLLAMA CONFIGURATION (LOCAL)
# =============================================================================

# Ollama Base URL (optional, defaults to http://localhost:11434)
ollama.baseUrl=http://localhost:11434

# Default Ollama model
ollama.defaultModel=llama3.1:8b

# Ollama timeout in milliseconds
ollama.timeoutMs=60000

# Ollama retry attempts
ollama.retryAttempts=2

# Enable Ollama provider
ollama.enabled=true

# =============================================================================
# LOCALAI CONFIGURATION (LOCAL)
# =============================================================================

# LocalAI Base URL (optional, defaults to http://localhost:8080)
localai.baseUrl=http://localhost:8080

# Default LocalAI model
localai.defaultModel=gpt-3.5-turbo

# LocalAI timeout in milliseconds
localai.timeoutMs=60000

# LocalAI retry attempts
localai.retryAttempts=2

# Enable LocalAI provider
localai.enabled=false

# =============================================================================
# VLLM CONFIGURATION (LOCAL)
# =============================================================================

# vLLM Base URL (optional, defaults to http://localhost:8000)
vllm.baseUrl=http://localhost:8000

# Default vLLM model
vllm.defaultModel=meta-llama/Llama-2-7b-chat-hf

# vLLM timeout in milliseconds
vllm.timeoutMs=60000

# vLLM retry attempts
vllm.retryAttempts=2

# Enable vLLM provider
vllm.enabled=false

# =============================================================================
# LM STUDIO CONFIGURATION (LOCAL)
# =============================================================================

# LM Studio Base URL (optional, defaults to http://localhost:1234)
lmstudio.baseUrl=http://localhost:1234

# Default LM Studio model
lmstudio.defaultModel=local-model

# LM Studio timeout in milliseconds
lmstudio.timeoutMs=60000

# LM Studio retry attempts
lmstudio.retryAttempts=2

# Enable LM Studio provider
lmstudio.enabled=false

# =============================================================================
# ADVANCED SETTINGS
# =============================================================================

# Enable debug logging for LLM operations
debug.enabled=false

# Enable cost tracking (for cloud providers)
cost.tracking.enabled=true

# Enable performance monitoring
performance.monitoring.enabled=true

# Maximum concurrent requests per provider
max.concurrent.requests=10

# Request queue size
request.queue.size=100

# Health check interval in seconds
health.check.interval=30

# Circuit breaker failure threshold
circuit.breaker.failure.threshold=5

# Circuit breaker recovery timeout in seconds
circuit.breaker.recovery.timeout=60
```

## 2. YAML Configuration Files

**Note**: These YAML files are located in the `conf/ai/` directory structure and are separate from the OSGi Config Admin (.cfg) files which are in `src/main/resources/OH-INF/config/`.

### 2.1 Agent Configuration (conf/ai/agents/energy-agent.yaml)

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

### 2.2 Policy Configuration (conf/ai/policies/energy-policies.yaml)

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

### 2.3 Prompt Templates (conf/ai/prompts/agents/energy-agent.yaml)

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

### 2.4 Model Presets (conf/ai/models/openai-presets.yaml)

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

## 3. Configuration Parsing Implementation

### 3.1 YAML Parser Example

```java
@Component(service = PromptRepository.class)
public class PromptYamlParser {
    
    private final Logger logger = LoggerFactory.getLogger(PromptYamlParser.class);
    private final ObjectMapper yamlMapper;
    
    public PromptYamlParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    public PromptTemplate parsePromptTemplate(Path filePath) throws IOException {
        try {
            PromptYamlConfig config = yamlMapper.readValue(filePath.toFile(), PromptYamlConfig.class);
            return buildPromptTemplate(config);
        } catch (Exception e) {
            logger.error("Failed to parse prompt template from {}: {}", filePath, e.getMessage());
            throw new IOException("Failed to parse prompt template", e);
        }
    }
    
    private PromptTemplate buildPromptTemplate(PromptYamlConfig config) {
        return PromptTemplate.builder()
            .agentName(config.getAgent().getName())
            .version(config.getAgent().getVersion())
            .systemRole(config.getPrompts().getSystem().getRole())
            .capabilities(config.getPrompts().getSystem().getCapabilities())
            .tasks(buildTasks(config.getPrompts().getTasks()))
            .build();
    }
    
    private List<TaskPrompt> buildTasks(Map<String, TaskConfig> taskConfigs) {
        return taskConfigs.entrySet().stream()
            .map(entry -> TaskPrompt.builder()
                .name(entry.getKey())
                .instruction(entry.getValue().getInstruction())
                .examples(entry.getValue().getExamples())
                .constraints(entry.getValue().getConstraints())
                .build())
            .collect(Collectors.toList());
    }
}

// YAML Configuration Classes
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptYamlConfig {
    private AgentConfig agent;
    private PromptsConfig prompts;
    
    // getters and setters
}

public class AgentConfig {
    private String name;
    private String version;
    private String description;
    
    // getters and setters
}

public class PromptsConfig {
    private SystemConfig system;
    private Map<String, TaskConfig> tasks;
    
    // getters and setters
}

public class SystemConfig {
    private String role;
    private List<String> capabilities;
    
    // getters and setters
}

public class TaskConfig {
    private String instruction;
    private List<String> examples;
    private List<String> constraints;
    
    // getters and setters
}
```

### 3.2 Configuration Validation Example

```java
public class ConfigurationValidator {
    
    private final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    public void validateLLMConfiguration(Map<String, Object> config) throws ConfigurationException {
        // Validate required fields
        validateRequiredField(config, "primary.provider");
        validateRequiredField(config, "fallback.provider");
        
        // Validate numeric ranges
        validateTemperatureRange(getDoubleConfig(config, "default.temperature", 0.3));
        validateMaxTokensRange(getIntConfig(config, "default.maxTokens", 1000));
        
        // Validate provider configurations
        validateProviderConfigurations(config);
    }
    
    private void validateProviderConfigurations(Map<String, Object> config) throws ConfigurationException {
        // Validate OpenAI configuration if enabled
        if (getBooleanConfig(config, "openai.enabled", false)) {
            validateOpenAIConfig(config);
        }
        
        // Validate Anthropic configuration if enabled
        if (getBooleanConfig(config, "anthropic.enabled", false)) {
            validateAnthropicConfig(config);
        }
        
        // Validate Ollama configuration if enabled
        if (getBooleanConfig(config, "ollama.enabled", true)) {
            validateOllamaConfig(config);
        }
    }
    
    private void validateOpenAIConfig(Map<String, Object> config) throws ConfigurationException {
        String apiKey = getStringConfig(config, "openai.api.key", "");
        if (apiKey.isEmpty()) {
            throw new ConfigurationException("OpenAI API key is required when OpenAI is enabled");
        }
        
        String baseUrl = getStringConfig(config, "openai.base.url", "https://api.openai.com/v1");
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

## 4. File Structure Summary

### **Configuration File Locations:**

**OSGi Config Admin (.cfg files)** - In `src/main/resources/OH-INF/config/`:
- `ai-common.cfg` - Common AI bundle configuration
- `a2a.cfg` - A2A protocol configuration  
- `mcp.cfg` - MCP protocol configuration

**File-based Configuration** - In `conf/ai/`:
- `model.cfg` - Model provider configuration (existing)
- `README.md` - Configuration documentation (existing)

**YAML Configuration Files** - In `conf/ai/` subdirectories:
- `conf/ai/agents/energy-agent.yaml` - Agent configuration
- `conf/ai/policies/energy-policies.yaml` - Policy configuration
- `conf/ai/prompts/agents/energy-agent.yaml` - Prompt templates
- `conf/ai/models/openai-presets.yaml` - Model presets

### **Directory Structure:**
```
conf/ai/
├── model.cfg                  # Model provider configuration
├── README.md                 # Configuration documentation
├── agents/
│   └── energy-agent.yaml     # Agent configuration
├── policies/
│   └── energy-policies.yaml  # Policy configuration
├── prompts/
│   └── agents/
│       └── energy-agent.yaml # Prompt templates
└── models/
    └── openai-presets.yaml   # Model presets
```

## 5. Summary

This document provides complete, parseable examples for:

1. **OSGi Config Admin (.cfg files)** - All configuration elements from the actual implementation
2. **YAML Configuration Files** - Complex structured data for agents, policies, prompts, and models
3. **Configuration Parsing Implementation** - Java classes and validation logic

The examples are based on the actual codebase analysis and ensure that:
- All configuration elements are covered
- YAML structures are parseable by standard YAML libraries
- Configuration validation is comprehensive
- The implementation follows openHAB patterns
- File locations match the actual project structure

---

## 📋 **Document Analysis and Relevance Assessment**

### **Current Status: HIGHLY RELEVANT - CONFIGURATION REFERENCE**

This document provides **comprehensive configuration examples** that are **actively relevant** for understanding and implementing the configuration system. It contains valuable examples and patterns for both OSGi Config Admin and YAML-based configuration.

### **Key Findings:**

#### ✅ **Comprehensive Configuration Coverage**
- **OSGi Config Admin**: Complete examples for .cfg files with all configuration options
- **YAML Configuration**: Structured examples for agents, policies, prompts, and models
- **Validation Logic**: Java code examples for configuration validation
- **File Structure**: Clear documentation of configuration file locations and organization

#### ✅ **Practical Implementation Examples**
- **Configuration Classes**: Concrete Java examples for configuration parsing
- **Validation Patterns**: Comprehensive validation logic for different configuration types
- **Error Handling**: Proper error handling and validation approaches
- **Best Practices**: Follows openHAB configuration patterns and conventions

#### ✅ **Current Architecture Alignment**
- **Bundle Structure**: Examples reflect the current consolidated bundle structure
- **Package Organization**: Configuration examples match current package organization
- **File Locations**: Directory structure matches current project layout
- **Implementation Patterns**: Examples align with current implementation approaches

### **Recommended Actions:**

#### **KEEP AND MAINTAIN** - This document should be:
1. **Updated Regularly**: Update examples as configuration options evolve
2. **Referenced in Development**: Use as the primary reference for configuration implementation
3. **Enhanced with Examples**: Add more examples for complex configuration scenarios
4. **Linked to Implementation**: Connect examples to actual configuration classes

#### **Immediate Updates Needed:**
1. **Verify Current Examples**: Ensure all examples reflect current configuration options
2. **Add Missing Examples**: Include examples for any new configuration options
3. **Update File Paths**: Ensure file paths match current project structure
4. **Add Validation Examples**: Include more comprehensive validation examples

### **Work Remaining:**
- **Configuration Documentation**: Ensure all configuration options are documented
- **Validation Examples**: Add more comprehensive validation examples
- **Error Handling**: Document error handling patterns for configuration issues
- **Migration Guide**: Add configuration migration examples for future changes

### **Conclusion:**
This document is **essential for configuration management** and should be actively used and maintained. It provides comprehensive examples and patterns that are crucial for understanding and implementing the configuration system correctly.