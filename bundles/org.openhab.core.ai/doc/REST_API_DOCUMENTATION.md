# OpenHAB AI Bundle REST API Documentation

## Overview

The OpenHAB AI Bundle provides a comprehensive REST API for managing AI agents, tasks, and system operations. The API follows openHAB's JAX-RS Whiteboard pattern and is organized into several functional areas.

## Base URL

All endpoints are available under the `/rest` base path in openHAB.

## Authentication

The API uses openHAB's standard authentication system. All endpoints require appropriate permissions.

## API Endpoints

### AI Extensions

#### GET /rest/ai/extensions
Returns basic information about the AI extensions.

**Response:**
```json
{
  "status": "ok",
  "message": "ai extensions ready"
}
```

**Headers:**
- `ETag`: W/"ai-extensions-v1"
- `Cache-Control`: public, max-age=300

### A2A Protocol Management

#### GET /rest/a2a/v1/tasks
List A2A tasks with optional filtering.

**Query Parameters:**
- `status` (optional): Filter by task status
- `agentId` (optional): Filter by agent ID
- `skillId` (optional): Filter by skill ID
- `createdAfter` (optional): Filter by creation date (ISO 8601)
- `createdBefore` (optional): Filter by creation date (ISO 8601)
- `limit` (optional): Maximum number of results (default: 50)
- `offset` (optional): Number of results to skip (default: 0)

**Response:**
```json
{
  "tasks": [
    {
      "id": "task-123",
      "status": "pending",
      "agentId": "agent-1",
      "skillId": "skill-1",
      "created": "2024-01-01T00:00:00Z"
    }
  ],
  "total": 1,
  "limit": 50,
  "offset": 0
}
```

#### GET /rest/a2a/v1/tasks/{id}
Get details of a specific A2A task.

**Response:**
```json
{
  "id": "task-123",
  "status": "pending",
  "agentId": "agent-1",
  "skillId": "skill-1",
  "created": "2024-01-01T00:00:00Z",
  "details": "Task description"
}
```

#### POST /rest/a2a/v1/tasks/{id}:cancel
Cancel an A2A task.

**Response:**
```json
{
  "status": "cancelled",
  "taskId": "task-123"
}
```

#### GET /rest/a2a/v1/card
Get A2A agent card information.

**Response:**
```json
{
  "type": "a2a-card",
  "version": "1.0",
  "agents": ["agent-1", "agent-2"],
  "transport": {
    "type": "http",
    "status": "active"
  }
}
```

### AI Management

#### GET /rest/ai/management/system/health
Get system health status.

**Response:**
```json
{
  "status": "healthy",
  "timestamp": 1704067200000,
  "version": "1.0",
  "components": {
    "ai-core": "active",
    "a2a-transport": "active",
    "mcp-transport": "active",
    "rest-api": "active"
  }
}
```

#### GET /rest/ai/management/system/performance
Get system performance metrics.

**Response:**
```json
{
  "timestamp": 1704067200000,
  "metrics": {
    "active_tasks": 0,
    "total_requests": 0,
    "avg_response_time": 0.0,
    "error_rate": 0.0,
    "memory_usage": 0.0
  }
}
```

#### GET /rest/ai/management/config
Get current configuration.

**Response:**
```json
{
  "ai": {
    "enabled": true,
    "default_model": "gpt-4",
    "max_tokens": 4096,
    "temperature": 0.7
  },
  "a2a": {
    "enabled": true,
    "transport": "http",
    "port": 8080
  },
  "mcp": {
    "enabled": true,
    "transport": "http+sse",
    "protocol_version": "2024-11-05"
  }
}
```

#### GET /rest/ai/management/tools
List available tools.

**Response:**
```json
[
  {
    "id": "file_read",
    "name": "File Read",
    "description": "Read file contents",
    "category": "file_operations",
    "status": "available"
  }
]
```

### AI User Information

#### GET /rest/ai/user/status
Get user information service status.

**Response:**
```json
{
  "service": "ai-user-info",
  "status": "available",
  "version": "1.0"
}
```

### Protocol Compliance

#### GET /rest/ai/compliance
Get protocol compliance status.

**Response:**
```json
{
  "status": "compliant",
  "protocols": {
    "a2a": "compliant",
    "mcp": "compliant"
  },
  "timestamp": 1704067200000
}
```

### MCP Protocol Integration

#### GET /rest/ai/mcp/status
Get MCP protocol status.

**Response:**
```json
{
  "protocol": "MCP",
  "version": "1.0",
  "transports": {
    "http": "enabled",
    "stdio": "enabled",
    "websocket": "disabled"
  },
  "servlet": {
    "path": "/mcp",
    "status": "active",
    "compliance": "verified"
  },
  "policy": "protocol-standard-operations-only-via-transports"
}
```

### AI Integration

#### GET /rest/ai/integration/status
Get openHAB integration status.

**Response:**
```json
{
  "integration": "openHAB AI",
  "version": "1.0",
  "services": {
    "items": "available",
    "things": "available",
    "rules": "available",
    "events": "available"
  },
  "endpoints": {
    "items": "/rest/ai/integration/items",
    "things": "/rest/ai/integration/things",
    "rules": "/rest/ai/integration/rules",
    "events": "/rest/ai/integration/events"
  }
}
```

## Error Responses

All endpoints return standard HTTP status codes:

- `200 OK`: Success
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error responses include a JSON body with error details:

```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "timestamp": 1704067200000
}
```

## Rate Limiting

The API implements rate limiting to prevent abuse. Limits are configurable per endpoint.

## Caching

Some endpoints support HTTP caching with ETags. Check the `ETag` header in responses and use `If-None-Match` for conditional requests.

## Versioning

The API version is included in the response headers and endpoint paths. Current version: `1.0`.

## Examples

### List A2A Tasks
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
     "http://localhost:8080/rest/a2a/v1/tasks?status=pending&limit=10"
```

### Get System Health
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
     "http://localhost:8080/rest/ai/management/system/health"
```

### Cancel a Task
```bash
curl -X POST -H "Authorization: Bearer YOUR_TOKEN" \
     "http://localhost:8080/rest/a2a/v1/tasks/task-123:cancel"
```

## Security Considerations

- All endpoints require authentication
- Sensitive operations require appropriate permissions
- API keys should be kept secure
- Use HTTPS in production environments
- Monitor API usage for suspicious activity

## Support

For API support and questions, please refer to the openHAB community forums or create an issue in the openHAB repository.
