# openHAB AI Common Bundle

## Overview

The `org.openhab.core.ai.common` bundle provides shared foundation functionality for AI protocol implementations in openHAB, specifically for Model Context Protocol (MCP) and Agent-to-Agent (A2A) protocols.

## Architecture

This bundle serves as the foundation for:
- `org.openhab.core.ai.mcp` - MCP protocol implementation  
- `org.openhab.core.ai.a2a` - A2A protocol implementation

## Features

### Shared Components
- **Authentication & Authorization**: Common security framework for AI protocols
- **Configuration Management**: Unified configuration handling
- **Protocol Message Structures**: Shared message structures for both protocols
- **Integration Layer**: openHAB core service integration utilities
- **Stub Framework**: Comprehensive testing infrastructure
- **Utility Classes**: Common helper functions and utilities

### Official SDK Integration
- **MCP Java SDK**: Integration with [Model Context Protocol Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- **A2A Java SDK**: Integration with [Agent2Agent Java SDK](https://github.com/a2aproject/a2a-java)

## Package Structure

```
org.openhab.core.ai.common/
├── auth/           # Authentication and authorization
├── config/         # Configuration management
├── api/            # API interfaces and contracts
├── integration/    # openHAB service integration
├── util/           # Utility classes
├── internal/       # Internal implementation classes
└── stub/           # Testing stub framework
```

## Dependencies

### openHAB Core Dependencies
- org.openhab.core
- org.openhab.core.thing
- org.openhab.core.items
- org.openhab.core.automation
- org.openhab.core.events
- org.openhab.core.persistence
- org.openhab.core.config.core
- org.openhab.core.auth
- org.openhab.core.io.rest

### External Dependencies
- Official MCP Java SDK
- Official A2A Java SDK
- Jackson (JSON processing)
- SLF4J (Logging)
- OSGi Framework

## Testing Strategy

The bundle implements a hybrid testing approach:
- **80%** - Stub-based unit testing for immediate development
- **15%** - Hybrid integration testing with live openHAB services
- **5%** - Full live system validation

## Development Status

This bundle is part of the comprehensive openHAB AI implementation project with:
- 42 major tasks across 25 phases
- 1,687 detailed subtasks
- Foundation phase currently in progress

## Building

```bash
mvn clean install
```

## Testing

```bash
# Unit tests
mvn test

# Integration tests  
mvn verify -P integration-test

# All tests with coverage
mvn clean verify jacoco:report
```

## Contributing

This bundle follows openHAB development guidelines and coding standards.

## License

This project is licensed under the Eclipse Public License 2.0 - see the openHAB project for details. 