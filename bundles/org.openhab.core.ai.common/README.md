# openHAB AI Common Bundle

## Overview

The `org.openhab.core.ai.common` bundle provides the foundational AI action framework for openHAB, enabling AI assistants to interact with openHAB systems through standardized AIAction interfaces.

## Features

### ✅ **Comprehensive AIAction Framework**
- **Standardized AIAction Interface**: Common interface for all AI actions
- **Parameter Validation**: Built-in parameter validation and sanitization
- **Async Support**: Both synchronous and asynchronous execution
- **Error Handling**: Comprehensive error handling and recovery
- **Metadata Support**: Rich metadata and capability information

### ✅ **Extensive Action Categories**
- **Persistence Actions**: Data persistence operations
- **Items Actions**: Item management and listing
- **Configuration Actions**: Configuration retrieval and management
- **Event Actions**: Event sending and subscription management
- **Automation Actions**: Advanced automation workflows
- **System Actions**: System monitoring and management
- **Security Actions**: Authentication and access control
- **File System Actions**: File operations and management
- **Script Actions**: Script execution and management
- **Analytics Actions**: Data analysis and reporting

### ✅ **Production-Ready Features**
- **OSGi Integration**: Full OSGi bundle lifecycle management
- **Configuration Management**: Comprehensive configuration options
- **Logging**: Detailed logging for debugging and monitoring
- **Testing Framework**: Comprehensive unit and integration testing
- **Documentation**: Complete API documentation and examples

## Architecture

### Core Components

```
org.openhab.core.ai.common/
├── api/                    # Public API interfaces
│   └── action/            # AIAction interfaces and types
├── actions/               # AIAction implementations
│   ├── persistence/       # Persistence actions
│   ├── items/            # Item management actions
│   ├── config/           # Configuration actions
│   ├── events/           # Event management actions
│   ├── automation/       # Automation actions
│   ├── system/           # System management actions
│   ├── security/         # Security actions
│   ├── filesystem/       # File system actions
│   ├── scripts/          # Script management actions
│   ├── analytics/        # Analytics actions
│   └── ...               # Additional action categories
├── auth/                 # Authentication and authorization
├── config/               # Configuration management
├── integration/          # Integration utilities
├── internal/             # Internal implementation
├── stub/                 # Stub implementations for testing
└── util/                 # Utility classes
```

### AIAction Interface

The core AIAction interface provides a standardized way to implement AI actions:

```java
public interface AIAction {
    String getActionId();
    String getActionName();
    String getDescription();
    String getCategory();
    String getVersion();
    
    Map<String, Object> getParameterSchema();
    Map<String, Object> getReturnSchema();
    
    AIActionValidationResult validateParameters(Map<String, Object> parameters);
    AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException;
    CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context);
    
    AIActionMetadata getMetadata();
    Map<String, Object> getCapabilities();
    
    void initialize(AIActionContext context);
    void cleanup();
    boolean isReady();
}
```

## Action Categories

### Persistence Actions
- **PersistenceAction**: Data persistence operations and service availability

### Items Actions
- **ListItemsAction**: Comprehensive item listing with filtering, pagination, and metadata

### Configuration Actions
- **ConfigurationGetAction**: Retrieve configuration values from files
- **ConfigurationSetAction**: Set configuration values
- **ConfigurationListAction**: List available configurations
- **ConfigurationBackupAction**: Backup configurations
- **ConfigurationExportAction**: Export configurations
- **ConfigurationImportAction**: Import configurations
- **ConfigurationValidationAction**: Validate configurations

### Event Actions
- **SendEventAction**: Send custom events to the EventBus
- **SubscribeEventsAction**: Subscribe to events with filtering
- **UnsubscribeEventsAction**: Unsubscribe from events
- **ListSubscriptionsAction**: List active subscriptions

### Automation Actions
- **AdvancedAutomationAction**: Complex automation workflows

### System Actions
- **HealthCheckAction**: System health monitoring
- **SystemDiagnosticsAction**: System diagnostics
- **MemoryManagementAction**: Memory management
- **LoggingMonitoringAction**: Logging and monitoring

### Security Actions
- **SecurityManagementAction**: Authentication and access control

### File System Actions
- **FileSystemManagementAction**: File operations and management

### Script Actions
- **ScriptExecutionAction**: Script execution
- **ScriptLibraryAction**: Script library management
- **ListScriptsAction**: List available scripts

### Analytics Actions
- **DataAnalysisAction**: Data analysis and reporting

## Usage

### Basic AIAction Implementation

```java
@Component(service = AIAction.class, immediate = true)
public class MyCustomAction implements AIAction {
    
    @Override
    public String getActionId() {
        return "openhab.custom.myaction";
    }
    
    @Override
    public String getActionName() {
        return "My Custom Action";
    }
    
    @Override
    public String getDescription() {
        return "A custom AI action for openHAB";
    }
    
    @Override
    public String getCategory() {
        return "custom";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        // Define parameter schema
        return schema;
    }
    
    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        // Define return schema
        return schema;
    }
    
    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        // Implement parameter validation
        return AIActionValidationResult.valid(parameters);
    }
    
    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        // Implement action execution
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        return AIActionResult.success(result);
    }
    
    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder()
            .version(getVersion())
            .description(getDescription())
            .tags(List.of("custom", "example"))
            .build();
    }
    
    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        return capabilities;
    }
    
    @Override
    public void initialize(AIActionContext context) {
        // Initialize action
    }
    
    @Override
    public void cleanup() {
        // Cleanup resources
    }
    
    @Override
    public boolean isReady() {
        return true;
    }
}
```

### Parameter Validation

```java
@Override
public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
    List<String> errors = new ArrayList<>();
    
    // Validate required parameters
    if (!parameters.containsKey("requiredParam")) {
        errors.add("Missing required parameter: requiredParam");
    }
    
    // Validate parameter types
    Object value = parameters.get("stringParam");
    if (value != null && !(value instanceof String)) {
        errors.add("Parameter 'stringParam' must be a string");
    }
    
    // Validate parameter values
    if (parameters.containsKey("enumParam")) {
        String enumValue = (String) parameters.get("enumParam");
        if (!Arrays.asList("value1", "value2", "value3").contains(enumValue)) {
            errors.add("Parameter 'enumParam' must be one of: value1, value2, value3");
        }
    }
    
    if (!errors.isEmpty()) {
        return AIActionValidationResult.invalid(errors);
    }
    
    return AIActionValidationResult.valid(parameters);
}
```

### Error Handling

```java
@Override
public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
    try {
        // Action implementation
        Map<String, Object> result = performAction(parameters, context);
        return AIActionResult.success(result);
        
    } catch (IllegalArgumentException e) {
        throw new AIActionException("Invalid parameters: " + e.getMessage(), e);
    } catch (Exception e) {
        throw new AIActionException("Action execution failed: " + e.getMessage(), e);
    }
}
```

## Testing

The bundle includes comprehensive testing infrastructure:

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class MyCustomActionTest {
    
    @Mock
    private AIActionContext mockContext;
    
    private MyCustomAction action;
    
    @BeforeEach
    void setUp() {
        action = new MyCustomAction();
        action.initialize(mockContext);
    }
    
    @Test
    void testGetActionId() {
        assertEquals("openhab.custom.myaction", action.getActionId());
    }
    
    @Test
    void testValidateParametersWithValidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("requiredParam", "value");
        
        AIActionValidationResult result = action.validateParameters(parameters);
        
        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }
    
    @Test
    void testExecuteWithValidParameters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("requiredParam", "value");
        
        AIActionResult result = action.execute(parameters, mockContext);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }
}
```

### Integration Testing

The bundle provides utilities for integration testing:

```java
@ExtendWith(MockitoExtension.class)
class AIActionIntegrationTest {
    
    @Test
    void testActionLifecycle() {
        MyCustomAction action = new MyCustomAction();
        AIActionContext context = createMockContext();
        
        // Test initialization
        action.initialize(context);
        assertTrue(action.isReady());
        
        // Test execution
        Map<String, Object> parameters = createValidParameters();
        AIActionResult result = action.execute(parameters, context);
        assertTrue(result.isSuccess());
        
        // Test cleanup
        action.cleanup();
    }
}
```

## Building

```bash
# Build the common bundle
cd bundles/org.openhab.core.ai.common
mvn clean install

# Build with tests
mvn clean verify

# Build with integration tests
mvn clean verify -P integration-test
```

## Dependencies

### Core Dependencies
- **openHAB Core**: Core openHAB functionality
- **openHAB Thing**: Thing management
- **openHAB Config Core**: Configuration management
- **openHAB Automation**: Automation framework
- **openHAB Transform**: Transformation utilities
- **openHAB Persistence**: Persistence framework

### Testing Dependencies
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **openHAB Test**: openHAB testing framework

## Contributing

This bundle follows openHAB development guidelines:

1. **Code Style**: Follow openHAB coding standards
2. **Testing**: Include comprehensive tests for new actions
3. **Documentation**: Update documentation for changes
4. **Review Process**: Submit pull requests for review

## License

This project is licensed under the Eclipse Public License 2.0 - see the openHAB project for details.

## Support

For issues and questions:
- **GitHub Issues**: Report bugs and feature requests
- **openHAB Community**: Community support and discussion
- **Documentation**: Check this README and inline documentation