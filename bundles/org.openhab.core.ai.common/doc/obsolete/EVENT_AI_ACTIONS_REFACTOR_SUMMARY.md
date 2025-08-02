# Event AI Actions Refactor Summary

## Overview

The Event AI Actions have been refactored to implement an **SSE-only architecture** for MCP clients, eliminating server-side event persistence and providing real-time event streaming via Server-Sent Events (SSE).

## Architecture Changes

### Before (Simulated/Stub Implementation)
- **Event Management**: Simulated event collection and storage
- **Event History**: Mock data and simulated persistence
- **Event Statistics**: Fake analytics and metrics
- **Event Subscription**: Simulated subscription management
- **Event Publishing**: Mock event publishing

### After (SSE-Only Architecture)
- **Real Event Subscription**: MCP clients subscribe to events via SSE endpoints
- **Real Event Publishing**: Events are published to the openHAB EventBus
- **No Server-Side Storage**: Events are not persisted server-side
- **Real-Time Streaming**: Events flow directly to subscribed clients
- **Client-Managed History**: Clients handle their own event history if needed

## New Components

### 1. EventSubscriptionRegistry
**Location**: `org.openhab.core.ai.common.events.EventSubscriptionRegistry`

**Purpose**: Manages event subscriptions for MCP clients with SSE support

**Key Features**:
- Client-based subscription management
- Event filtering by type and criteria
- SSE endpoint generation
- Subscription lifecycle management

**Key Methods**:
```java
public SubscriptionInfo subscribe(String clientId, Set<String> eventTypes, Map<String, String> filters)
public boolean unsubscribe(String subscriptionId)
public List<SubscriptionInfo> listSubscriptions(String clientId)
public boolean hasSubscription(String subscriptionId)
```

### 2. EventSSEManager
**Location**: `org.openhab.core.ai.common.events.EventSSEManager`

**Purpose**: Manages SSE connections and forwards events to clients

**Key Features**:
- SSE connection management
- Event forwarding to subscribed clients
- Connection health monitoring
- Automatic cleanup of closed connections

**Key Methods**:
```java
public void registerSink(String subscriptionId, SSESink sink)
public void unregisterSink(String subscriptionId, SSESink sink)
public void forwardEvent(String subscriptionId, Event event)
public int getActiveConnectionCount()
```

## Refactored Event AI Actions

### 1. SubscribeEventsAction
**Purpose**: Register MCP clients for event streaming via SSE

**Parameters**:
- `eventTypes`: List of event types to subscribe to
- `filters`: Optional filters for event filtering
- `clientId`: MCP client identifier

**Returns**:
- `subscriptionId`: Unique subscription identifier
- `sseUrl`: SSE endpoint URL for receiving events
- `eventTypes`: List of subscribed event types
- `filters`: Applied filters
- `status`: Subscription status

**Example Usage**:
```json
{
  "eventTypes": ["ItemStateEvent", "ItemCommandEvent"],
  "filters": {"itemName": "LivingRoom_Light"},
  "clientId": "mcp-client-001"
}
```

### 2. UnsubscribeEventsAction
**Purpose**: Remove event subscriptions for MCP clients

**Parameters**:
- `subscriptionId`: ID of the subscription to remove
- `clientId`: MCP client identifier (optional, for validation)

**Returns**:
- `subscriptionId`: ID of the removed subscription
- `status`: Status of the unsubscription
- `message`: Result message

**Example Usage**:
```json
{
  "subscriptionId": "sub-12345",
  "clientId": "mcp-client-001"
}
```

### 3. ListSubscriptionsAction
**Purpose**: List active event subscriptions for MCP clients

**Parameters**:
- `clientId`: MCP client identifier
- `includeDetails`: Include detailed subscription information (optional)

**Returns**:
- `clientId`: MCP client identifier
- `subscriptions`: List of active subscriptions
- `totalCount`: Total number of subscriptions
- `status`: Status of the operation

**Example Usage**:
```json
{
  "clientId": "mcp-client-001",
  "includeDetails": true
}
```

### 4. SendEventAction
**Purpose**: Publish custom events to the openHAB EventBus

**Parameters**:
- `topic`: Event topic (e.g., 'openhab/items/Light/command')
- `payload`: Event payload data
- `eventType`: Type of event to send (optional)
- `source`: Source of the event (optional)
- `priority`: Event priority (optional)

**Returns**:
- `eventId`: ID of the sent event
- `topic`: Event topic
- `status`: Status of the event sending
- `timestamp`: Timestamp of the request

**Example Usage**:
```json
{
  "topic": "openhab/items/LivingRoom_Light/command",
  "payload": {"command": "ON"},
  "eventType": "ItemCommandEvent",
  "source": "ai-action"
}
```

## SSE Endpoint Structure

### Endpoint URL
```
/mcp/events/{subscriptionId}
```

### Event Format
```json
{
  "type": "ItemStateEvent",
  "topic": "openhab/items/LivingRoom_Light/state",
  "source": "org.openhab.core.items",
  "payload": "{\"itemName\":\"LivingRoom_Light\",\"state\":\"ON\"}",
  "timestamp": "1640995200000"
}
```

## Benefits of SSE-Only Architecture

### 1. **Real-Time Performance**
- Events flow directly to clients without server-side buffering
- Minimal latency for event delivery
- No server-side event processing overhead

### 2. **Scalability**
- No server-side storage requirements
- Clients manage their own event history
- Horizontal scaling without data synchronization

### 3. **Simplicity**
- Eliminates complex server-side event persistence
- Reduces memory usage and storage requirements
- Simplifies event management logic

### 4. **MCP Protocol Alignment**
- Matches MCP's stateless, tool-based architecture
- Provides real-time event streaming capabilities
- Integrates seamlessly with MCP client workflows

## Migration Guide

### For MCP Clients

1. **Subscribe to Events**:
   ```javascript
   // Use SubscribeEventsAction to get SSE endpoint
   const response = await mcpClient.call('openhab.events.subscribe', {
     eventTypes: ['ItemStateEvent'],
     clientId: 'my-client-id'
   });
   
   // Connect to SSE endpoint
   const eventSource = new EventSource(response.sseUrl);
   eventSource.onmessage = (event) => {
     const eventData = JSON.parse(event.data);
     console.log('Received event:', eventData);
   };
   ```

2. **Send Events**:
   ```javascript
   // Use SendEventAction to publish events
   await mcpClient.call('openhab.events.send', {
     topic: 'openhab/items/Light/command',
     payload: { command: 'ON' }
   });
   ```

3. **Manage Subscriptions**:
   ```javascript
   // List active subscriptions
   const subscriptions = await mcpClient.call('openhab.events.list_subscriptions', {
     clientId: 'my-client-id'
   });
   
   // Unsubscribe from events
   await mcpClient.call('openhab.events.unsubscribe', {
     subscriptionId: 'sub-12345'
   });
   ```

## Future Enhancements

### 1. **Event Filtering**
- Advanced filter expressions
- Regex-based filtering
- Time-based filtering

### 2. **Event Replay**
- Optional client-side event buffering
- Event replay capabilities for missed events
- Historical event access via persistence services

### 3. **Event Analytics**
- Client-side event analytics
- Event pattern recognition
- Performance monitoring

### 4. **Security**
- Subscription authentication
- Event encryption
- Access control for event types

## Conclusion

The refactored Event AI Actions provide a robust, scalable, and real-time event streaming solution that aligns perfectly with the MCP protocol's architecture. By eliminating server-side event persistence and focusing on real-time SSE delivery, the system achieves better performance, scalability, and simplicity while maintaining full functionality for MCP clients. 