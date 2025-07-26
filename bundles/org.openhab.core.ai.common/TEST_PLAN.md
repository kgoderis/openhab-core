# openHAB AI Bundles Test Plan

## Overview

This document outlines a comprehensive testing strategy for the openHAB AI bundles (MCP and A2A), including unit tests for AIActions and integration testing approaches.

## Test Architecture

### 1. Unit Testing Strategy

#### 1.1 AIAction Unit Tests

**Location**: `org.openhab.core.ai.common/src/test/java/org/openhab/core/ai/common/actions/`

**Test Structure**:
```java
@ExtendWith(MockitoExtension.class)
class [ActionName]ActionTest {
    
    @Mock
    private AIActionContext mockContext;
    
    @Mock
    private [ServiceName] mockService; // e.g., ItemRegistry, ThingRegistry
    
    private [ActionName]Action action;
    
    @BeforeEach
    void setUp() {
        action = new [ActionName]Action();
        // Setup mocks and inject dependencies
    }
    
    @Test
    void testExecuteSuccess() throws AIActionException {
        // Test successful execution
    }
    
    @Test
    void testExecuteWithInvalidParameters() {
        // Test parameter validation
    }
    
    @Test
    void testExecuteAsync() {
        // Test async execution
    }
    
    @Test
    void testValidateParameters() {
        // Test parameter validation
    }
    
    @Test
    void testGetSchemas() {
        // Test schema generation
    }
}
```

**Key Testing Areas for AIActions**:

1. **Parameter Validation**:
   - Valid parameters should pass validation
   - Invalid parameters should return validation errors
   - Missing required parameters should be detected
   - Type validation for parameters

2. **Execution Logic**:
   - Successful execution with valid parameters
   - Error handling for invalid parameters
   - Exception handling for service failures
   - Async execution completion

3. **Schema Generation**:
   - Parameter schema accuracy
   - Return schema accuracy
   - Schema consistency with implementation

4. **Service Integration**:
   - Mock openHAB services (ItemRegistry, ThingRegistry, etc.)
   - Verify service method calls
   - Test service availability checks

#### 1.2 MCP Bundle Unit Tests

**Current Test Coverage**:
- ✅ `MCPServerManagerTest` - Server lifecycle management
- ✅ `MCPToolAdapterTest` - AIAction to MCP tool conversion
- ✅ `MCPToolRegistryTest` - Tool registration and discovery
- ✅ `MCPServerInstanceTest` - Server instance management
- ✅ `MCPServerConfigurationTest` - Configuration handling
- ✅ `MCPErrorRecoveryManagerTest` - Error recovery mechanisms
- ✅ `MCPSecurityManagerTest` - Security and authentication
- ✅ `MCPTransportTypeTest` - Transport type handling

**Additional Tests Needed**:

1. **Transport Layer Tests**:
   ```java
   class MCPTransportTest {
       @Test
       void testStdioTransport() { /* Test STDIO transport */ }
       @Test
       void testSseTransport() { /* Test SSE transport */ }
       @Test
       void testWebSocketTransport() { /* Test WebSocket transport */ }
   }
   ```

2. **Protocol Handler Tests**:
   ```java
   class MCPProtocolHandlerTest {
       @Test
       void testToolCallHandling() { /* Test tool call processing */ }
       @Test
       void testListToolsResponse() { /* Test tool listing */ }
       @Test
       void testErrorResponseFormat() { /* Test error handling */ }
   }
   ```

#### 1.3 A2A Bundle Unit Tests

**Current Test Coverage**:
- ✅ `A2ASkillAdapterTest` - AIAction to A2A skill conversion
- ✅ `A2ASkillRegistryTest` - Skill registration and management
- ✅ `A2ASecurityManagerTest` - Security and permissions
- ✅ `A2AServerManagerTest` - Server lifecycle management
- ✅ `A2AAgentExecutorTest` - Task execution
- ✅ `A2ARestEndpointTest` - REST API endpoints

**Additional Tests Needed**:

1. **Message Handling Tests**:
   ```java
   class A2AMessageHandlerTest {
       @Test
       void testMessageParsing() { /* Test message parsing */ }
       @Test
       void testMessageValidation() { /* Test message validation */ }
       @Test
       void testMessageRouting() { /* Test message routing */ }
   }
   ```

2. **Task Management Tests**:
   ```java
   class A2ATaskManagerTest {
       @Test
       void testTaskCreation() { /* Test task creation */ }
       @Test
       void testTaskExecution() { /* Test task execution */ }
       @Test
       void testTaskCancellation() { /* Test task cancellation */ }
   }
   ```

### 2. Integration Testing Strategy

#### 2.1 AIAction Integration Tests

**Location**: `org.openhab.core.ai.common/src/test/java/org/openhab/core/ai/common/integration/`

**Test Structure**:
```java
@ExtendWith(OSGiExtension.class)
class [ActionName]ActionIntegrationTest {
    
    @Inject
    private [ActionName]Action action;
    
    @Inject
    private [ServiceName] service; // Real openHAB service
    
    @Test
    void testIntegrationWithRealServices() {
        // Test with actual openHAB services
    }
    
    @Test
    void testEndToEndWorkflow() {
        // Test complete workflows
    }
}
```

**Integration Test Categories**:

1. **Service Integration Tests**:
   - Test AIActions with real openHAB services
   - Verify data consistency between actions and services
   - Test service lifecycle integration

2. **Workflow Integration Tests**:
   - Test multi-action workflows
   - Verify action chaining and dependencies
   - Test error propagation across actions

3. **Performance Integration Tests**:
   - Test action performance under load
   - Verify memory usage and cleanup
   - Test concurrent execution

#### 2.2 MCP Integration Tests

**Location**: `org.openhab.core.ai.mcp/src/test/java/org/openhab/core/ai/mcp/integration/`

**Test Structure**:
```java
@ExtendWith(OSGiExtension.class)
class MCPIntegrationTest {
    
    @Inject
    private MCPServerManager serverManager;
    
    @Inject
    private MCPToolRegistry toolRegistry;
    
    @Test
    void testMCPClientConnection() {
        // Test MCP client connection and communication
    }
    
    @Test
    void testToolExecutionViaMCP() {
        // Test tool execution through MCP protocol
    }
    
    @Test
    void testTransportIntegration() {
        // Test transport layer integration
    }
}
```

**Integration Test Scenarios**:

1. **MCP Client Integration**:
   - Test with real MCP clients (Claude, GPT-4, etc.)
   - Verify protocol compliance
   - Test authentication and authorization

2. **Transport Integration**:
   - Test STDIO transport with real processes
   - Test SSE transport with HTTP clients
   - Test WebSocket transport with WebSocket clients

3. **Tool Execution Integration**:
   - Test tool execution end-to-end
   - Verify result formatting and delivery
   - Test error handling and recovery

#### 2.3 A2A Integration Tests

**Location**: `org.openhab.core.ai.a2a/src/test/java/org/openhab/core/ai/a2a/integration/`

**Test Structure**:
```java
@ExtendWith(OSGiExtension.class)
class A2AIntegrationTest {
    
    @Inject
    private A2AServerManager serverManager;
    
    @Inject
    private A2ASkillRegistry skillRegistry;
    
    @Test
    void testExternalAgentCommunication() {
        // Test communication with external A2A agents
    }
    
    @Test
    void testSkillExecutionViaA2A() {
        // Test skill execution through A2A protocol
    }
    
    @Test
    void testTaskManagementIntegration() {
        // Test task management and execution
    }
}
```

**Integration Test Scenarios**:

1. **External Agent Integration**:
   - Test with real A2A agents
   - Verify protocol compliance
   - Test agent discovery and registration

2. **Skill Execution Integration**:
   - Test skill execution end-to-end
   - Verify result formatting and delivery
   - Test error handling and recovery

3. **Task Management Integration**:
   - Test task creation, execution, and monitoring
   - Verify task state management
   - Test task cancellation and cleanup

### 3. Test Infrastructure

#### 3.1 Test Dependencies

**Common Test Dependencies**:
```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- OSGi Testing -->
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.test.junit5</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ for fluent assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### 3.2 Test Utilities

**Common Test Utilities**:
```java
public class TestUtils {
    
    public static AIActionContext createMockContext() {
        // Create mock AIActionContext
    }
    
    public static Map<String, Object> createValidParameters() {
        // Create valid parameters for testing
    }
    
    public static Map<String, Object> createInvalidParameters() {
        // Create invalid parameters for testing
    }
    
    public static void assertActionResult(AIActionResult result, boolean expectedSuccess) {
        // Assert action result properties
    }
}
```

#### 3.3 Test Data Management

**Test Data Strategy**:
1. **Fixtures**: Pre-defined test data for common scenarios
2. **Factories**: Dynamic test data generation
3. **Cleanup**: Automatic test data cleanup after tests
4. **Isolation**: Each test should be independent

### 4. Testing Best Practices

#### 4.1 Unit Testing Best Practices

1. **Test Naming**: Use descriptive test names that explain the scenario
2. **Arrange-Act-Assert**: Follow the AAA pattern for test structure
3. **Mocking**: Mock external dependencies, not the unit under test
4. **Coverage**: Aim for 80%+ code coverage
5. **Isolation**: Each test should be independent and not affect others

#### 4.2 Integration Testing Best Practices

1. **Real Services**: Use real openHAB services when possible
2. **Test Data**: Use realistic test data that mirrors production
3. **Cleanup**: Ensure proper cleanup of test data and resources
4. **Performance**: Monitor test performance and optimize slow tests
5. **Reliability**: Make tests reliable and not flaky

#### 4.3 Test Organization

**Package Structure**:
```
src/test/java/org/openhab/core/ai/[bundle]/
├── unit/                    # Unit tests
│   ├── actions/            # AIAction unit tests
│   ├── internal/           # Internal component tests
│   └── utils/              # Utility tests
├── integration/            # Integration tests
│   ├── services/           # Service integration tests
│   ├── protocols/          # Protocol integration tests
│   └── workflows/          # Workflow integration tests
└── utils/                  # Test utilities
    ├── TestDataFactory.java
    ├── MockUtils.java
    └── AssertionUtils.java
```

### 5. Test Execution Strategy

#### 5.1 Test Execution Order

1. **Unit Tests**: Fast, isolated tests that run first
2. **Integration Tests**: Slower tests that require more setup
3. **End-to-End Tests**: Full system tests that run last

#### 5.2 Test Categories

1. **Fast Tests** (< 1 second): Unit tests, simple mocks
2. **Medium Tests** (1-10 seconds): Integration tests, service tests
3. **Slow Tests** (> 10 seconds): End-to-end tests, performance tests

#### 5.3 CI/CD Integration

**Maven Test Execution**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <excludes>
            <exclude>**/*IntegrationTest.java</exclude>
        </excludes>
    </configuration>
</plugin>
```

### 6. Implementation Plan

#### Phase 1: AIAction Unit Tests (Week 1-2)
1. Create test infrastructure and utilities
2. Implement unit tests for core AIActions
3. Establish testing patterns and conventions

#### Phase 2: MCP/A2A Unit Tests (Week 3-4)
1. Complete existing unit test coverage
2. Add missing unit tests for internal components
3. Fix any test failures and improve test quality

#### Phase 3: Integration Tests (Week 5-6)
1. Implement AIAction integration tests
2. Implement MCP integration tests
3. Implement A2A integration tests

#### Phase 4: Test Optimization (Week 7-8)
1. Optimize test performance
2. Improve test reliability
3. Add comprehensive test documentation

### 7. Success Metrics

#### 7.1 Code Coverage Targets
- **Unit Tests**: 80%+ line coverage
- **Integration Tests**: 60%+ line coverage
- **Overall Coverage**: 75%+ line coverage

#### 7.2 Test Quality Metrics
- **Test Execution Time**: < 5 minutes for full test suite
- **Test Reliability**: < 1% flaky tests
- **Test Maintenance**: Clear, maintainable test code

#### 7.3 Continuous Improvement
- Regular test code reviews
- Test performance monitoring
- Test coverage trend analysis
- Test failure root cause analysis

## Conclusion

This test plan provides a comprehensive strategy for testing the openHAB AI bundles. The approach balances thoroughness with practicality, ensuring high-quality code while maintaining reasonable test execution times. The phased implementation allows for iterative improvement and validation of the testing strategy. 