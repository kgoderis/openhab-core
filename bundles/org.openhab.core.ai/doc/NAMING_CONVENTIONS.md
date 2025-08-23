# openHAB AI Bundle Naming Conventions

This document defines the standardized naming conventions for the openHAB AI bundle to ensure consistency and maintainability across the codebase.

## Table of Contents

1. [Class Naming](#class-naming)
2. [Package Naming](#package-naming)
3. [Method Naming](#method-naming)
4. [Variable Naming](#variable-naming)
5. [Constant Naming](#constant-naming)
6. [Interface Naming](#interface-naming)
7. [Enum Naming](#enum-naming)
8. [File Naming](#file-naming)
9. [Domain-Specific Patterns](#domain-specific-patterns)

## Class Naming

### Core Classes
- **Base Classes**: Use `Base` prefix for abstract base classes
  - `BaseConfiguration`
  - `BaseContext`
  - `AbstractBuilder`

### Domain-Specific Classes
- **Tool Classes**: Use `Tool` prefix for MCP tool-related classes
  - `ToolContext`
  - `ToolSecurityManager`
  - `ToolExecutionService`

- **Agent Classes**: Use `Agent` prefix for A2A agent-related classes
  - `AgentContext`
  - `AgentSecurityManager`
  - `AgentTaskManager`

- **AI Classes**: Use `AI` prefix for AI action framework classes
  - `AIActionContext`
  - `AISecurityManager`
  - `AIExecutionService`

### Implementation Classes
- **Default Implementations**: Use `Default` prefix for default implementations
  - `DefaultSecurityManager`
  - `DefaultConfigurationService`
  - `DefaultActionExecutionService`

- **Manager Classes**: Use `Manager` suffix for management classes
  - `SecurityManager`
  - `ConfigurationManager`
  - `TaskManager`

- **Service Classes**: Use `Service` suffix for service classes
  - `ExecutionService`
  - `ValidationService`
  - `NotificationService`

### Builder Classes
- **Builder Classes**: Use `Builder` suffix for builder pattern classes
  - `ServerConfigurationBuilder`
  - `ContextBuilder`
  - `ResponseBuilder`

## Package Naming

### Core Packages
- `org.openhab.core.ai.common.*` - Common utilities and base classes
- `org.openhab.core.ai.common.builder` - Builder pattern base classes
- `org.openhab.core.ai.common.configuration` - Configuration base classes
- `org.openhab.core.ai.common.context` - Context base classes
- `org.openhab.core.ai.common.response` - Response base classes

### Domain-Specific Packages
- `org.openhab.core.ai.tool.*` - MCP tool-related classes
- `org.openhab.core.ai.agent.*` - A2A agent-related classes
- `org.openhab.core.ai.action.*` - AI action framework classes
- `org.openhab.core.ai.reasoning.*` - Reasoning and decision-making classes
- `org.openhab.core.ai.model.*` - Model and provider classes
- `org.openhab.core.ai.security.*` - Security-related classes
- `org.openhab.core.ai.auth.*` - Authentication-related classes

### API Packages
- `org.openhab.core.ai.*.api` - Public APIs for each domain
- `org.openhab.core.ai.*.internal` - Internal implementation classes

## Method Naming

### Getter Methods
- Use `get` prefix for simple getters
  - `getId()`
  - `getName()`
  - `getVersion()`

- Use descriptive names for complex getters
  - `getActiveOperationCount()`
  - `getPerformanceMetrics()`
  - `getValidationErrors()`

### Setter Methods (Builder Pattern)
- Use `with` prefix for builder setters
  - `withId(String id)`
  - `withName(String name)`
  - `withEnabled(boolean enabled)`

### Action Methods
- Use verb-based names for actions
  - `beginOperation()`
  - `reportProgress()`
  - `endOperation()`
  - `validate()`
  - `reset()`

### Boolean Methods
- Use `is` prefix for boolean getters
  - `isEnabled()`
  - `isValid()`
  - `isAuthenticated()`

- Use `has` prefix for existence checks
  - `hasCustomOption()`
  - `hasValidationErrors()`
  - `hasActiveOperations()`

## Variable Naming

### Instance Variables
- Use camelCase for instance variables
  - `operationId`
  - `maxConnections`
  - `enableAuthentication`

### Constants
- Use UPPER_SNAKE_CASE for constants
  - `DEFAULT_PORT`
  - `MAX_CONNECTIONS`
  - `DEFAULT_TIMEOUT`

### Collections
- Use descriptive plural names for collections
  - `activeOperations`
  - `validationErrors`
  - `customOptions`

## Constant Naming

### Configuration Constants
- Use descriptive names with `DEFAULT_` prefix for defaults
  - `DEFAULT_PORT = 8080`
  - `DEFAULT_TIMEOUT = 30000`
  - `DEFAULT_MAX_CONNECTIONS = 100`

### Status Constants
- Use descriptive names for status values
  - `STATUS_ACTIVE`
  - `STATUS_INACTIVE`
  - `STATUS_ERROR`

## Interface Naming

### Core Interfaces
- Use descriptive names without prefixes
  - `Configuration`
  - `Context`
  - `Response`
  - `Builder`

### Domain Interfaces
- Use domain-specific prefixes
  - `SecurityManager`
  - `ExecutionService`
  - `ValidationService`

## Enum Naming

### Status Enums
- Use descriptive names with `Status` suffix
  - `ProgressStatus`
  - `ValidationStatus`
  - `SecurityStatus`

### Type Enums
- Use descriptive names with `Type` suffix
  - `AuthType`
  - `ProtocolType`
  - `ContextType`

## File Naming

### Java Files
- Use PascalCase for class names
  - `ServerConfiguration.java`
  - `DefaultSecurityManager.java`
  - `AbstractBuilder.java`

### Test Files
- Use same name as class with `Test` suffix
  - `ServerConfigurationTest.java`
  - `DefaultSecurityManagerTest.java`
  - `AbstractBuilderTest.java`

### Documentation Files
- Use descriptive names with appropriate extensions
  - `NAMING_CONVENTIONS.md`
  - `PLAN_DUPLICATE.md`
  - `README.md`

## Domain-Specific Patterns

### MCP Tool Domain
- **Classes**: `Tool*` prefix
- **Packages**: `org.openhab.core.ai.tool.*`
- **Interfaces**: `Tool*` prefix
- **Examples**:
  - `ToolContext`
  - `ToolSecurityManager`
  - `ToolExecutionService`

### A2A Agent Domain
- **Classes**: `Agent*` prefix
- **Packages**: `org.openhab.core.ai.agent.*`
- **Interfaces**: `Agent*` prefix
- **Examples**:
  - `AgentContext`
  - `AgentSecurityManager`
  - `AgentTaskManager`

### AI Action Domain
- **Classes**: `AI*` prefix
- **Packages**: `org.openhab.core.ai.action.*`
- **Interfaces**: `AI*` prefix
- **Examples**:
  - `AIActionContext`
  - `AISecurityManager`
  - `AIExecutionService`

### Security Domain
- **Classes**: `Security*` prefix or `*Security*` pattern
- **Packages**: `org.openhab.core.ai.security.*`
- **Interfaces**: `Security*` prefix
- **Examples**:
  - `SecurityManager`
  - `SecurityContext`
  - `SecurityValidationResult`

### Configuration Domain
- **Classes**: `*Configuration` suffix
- **Packages**: `org.openhab.core.ai.*.configuration`
- **Interfaces**: `Configuration` or `*Configuration`
- **Examples**:
  - `ServerConfiguration`
  - `SecurityConfiguration`
  - `AgentConfiguration`

## Best Practices

### Consistency
- Always use the same naming pattern within a domain
- Maintain consistency across related classes
- Follow established patterns in the codebase

### Clarity
- Use descriptive names that clearly indicate purpose
- Avoid abbreviations unless they are widely understood
- Prefer longer, clear names over short, ambiguous ones

### Maintainability
- Choose names that will remain meaningful over time
- Consider future extensions when naming
- Document any non-obvious naming decisions

### Compatibility
- Follow Java naming conventions
- Ensure compatibility with openHAB standards
- Consider integration with external systems

## Examples

### Good Examples
```java
// Clear, descriptive class names
public class ServerConfiguration extends BaseConfiguration
public class DefaultSecurityManager implements SecurityManager
public class ToolExecutionService implements ExecutionService

// Consistent method naming
public String getId()
public boolean isEnabled()
public void withName(String name)
public int getActiveOperationCount()

// Clear variable names
private final Map<String, Operation> activeOperations;
private final List<String> validationErrors;
private final int maxConnections;
```

### Avoid
```java
// Unclear abbreviations
public class SvrConfig
public class SecMgr
public class ToolExecSvc

// Inconsistent naming
public String getID()  // Should be getId()
public boolean enabled()  // Should be isEnabled()
public void setName(String name)  // Should be withName() for builders
```

## Enforcement

### Code Reviews
- All code reviews should check naming convention compliance
- Inconsistent naming should be flagged for correction
- New patterns should be documented and shared

### Automated Checks
- Consider implementing automated naming convention checks
- Use static analysis tools to enforce patterns
- Include naming checks in CI/CD pipelines

### Documentation
- Keep this document updated as patterns evolve
- Document any exceptions or special cases
- Provide examples for common patterns

---

## 📋 **Document Analysis and Relevance Assessment**

### **Current Status: HIGHLY RELEVANT - CODING STANDARDS**

This document provides **essential naming conventions and coding standards** that are **actively relevant** for maintaining code consistency and quality. It contains valuable guidelines for class, package, method, and variable naming that should be followed throughout the project.

### **Key Findings:**

#### ✅ **Comprehensive Naming Standards**
- **Class Naming**: Clear conventions for different types of classes (base, domain-specific, implementation)
- **Package Naming**: Well-defined package structure and organization
- **Method Naming**: Consistent patterns for getters, setters, and other methods
- **Variable Naming**: Clear guidelines for variable and constant naming

#### ✅ **Domain-Specific Patterns**
- **Tool Classes**: Clear conventions for MCP tool-related classes
- **Agent Classes**: Well-defined patterns for A2A agent-related classes
- **AI Classes**: Consistent naming for AI action framework classes
- **Security Classes**: Proper naming for security-related components

#### ✅ **Best Practices and Enforcement**
- **Consistency Guidelines**: Clear rules for maintaining consistency across the codebase
- **Clarity Standards**: Emphasis on descriptive and meaningful names
- **Maintainability**: Guidelines for choosing names that remain meaningful over time
- **Enforcement Strategy**: Code review and automated check recommendations

### **Recommended Actions:**

#### **KEEP AND MAINTAIN** - This document should be:
1. **Enforced in Development**: Use as the authoritative source for naming conventions
2. **Updated Regularly**: Update patterns as new domains or patterns emerge
3. **Referenced in Code Reviews**: Use as a checklist during code reviews
4. **Linked to CI/CD**: Integrate automated checks into development pipelines

#### **Immediate Updates Needed:**
1. **Verify Current Compliance**: Check if current codebase follows these conventions
2. **Add Missing Patterns**: Include any naming patterns that have emerged since creation
3. **Update Examples**: Ensure examples reflect current implementation
4. **Add Enforcement Tools**: Implement automated checks for naming convention compliance

### **Work Remaining:**
- **Code Review Integration**: Ensure naming conventions are checked in all code reviews
- **Automated Enforcement**: Implement automated tools to check naming convention compliance
- **Documentation Updates**: Keep examples and patterns current with implementation
- **Training**: Ensure all developers are familiar with these conventions

### **Conclusion:**
This document is **essential for code quality and consistency** and should be actively enforced and maintained. It provides clear guidelines that are crucial for maintaining a clean, consistent, and maintainable codebase.
