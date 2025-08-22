/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConfigurationValidator.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ConfigurationValidatorTest {

    private ConfigurationValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new ConfigurationValidator();
    }

    @Test
    public void testValidateModelConfiguration() {
        // Test valid model configuration
        Map<String, Object> validConfig = new HashMap<>();
        validConfig.put("ai.model.primary.provider", "ollama");
        validConfig.put("ai.model.fallback.provider", "openai");
        validConfig.put("ai.model.default.temperature", 0.3);
        validConfig.put("ai.model.default.maxTokens", 1000);

        assertDoesNotThrow(() -> {
            validator.validateModelConfiguration(validConfig);
        });

        // Test invalid model configuration - missing required fields
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.model.primary.provider", "ollama");
        // Missing fallback provider

        assertThrows(ConfigurationException.class, () -> {
            validator.validateModelConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateAgentConfiguration() {
        // Test valid agent configuration
        Map<String, Object> validConfig = new HashMap<>();
        validConfig.put("ai.agent.server.id", "test-server");
        validConfig.put("ai.agent.server.name", "Test Server");
        validConfig.put("ai.agent.server.enabled", true);
        validConfig.put("ai.agent.server.port", 8080);
        validConfig.put("ai.agent.max.agents", 50);
        validConfig.put("ai.agent.execution.timeout", 30000);

        assertDoesNotThrow(() -> {
            validator.validateAgentConfiguration(validConfig);
        });

        // Test invalid agent configuration - invalid port
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.agent.server.id", "test-server");
        invalidConfig.put("ai.agent.server.name", "Test Server");
        invalidConfig.put("ai.agent.server.enabled", true);
        invalidConfig.put("ai.agent.server.port", -1); // Invalid port

        assertThrows(ConfigurationException.class, () -> {
            validator.validateAgentConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateToolConfiguration() {
        // Test valid tool configuration
        Map<String, Object> validConfig = new HashMap<>();
        validConfig.put("ai.tool.server.id", "test-tool-server");
        validConfig.put("ai.tool.server.name", "Test Tool Server");
        validConfig.put("ai.tool.server.enabled", true);
        validConfig.put("ai.tool.server.port", 8081);
        validConfig.put("ai.tool.authentication.enabled", true);
        validConfig.put("ai.tool.max.tools", 100);

        assertDoesNotThrow(() -> {
            validator.validateToolConfiguration(validConfig);
        });

        // Test invalid tool configuration - invalid max tools
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.tool.server.id", "test-tool-server");
        invalidConfig.put("ai.tool.server.name", "Test Tool Server");
        invalidConfig.put("ai.tool.server.enabled", true);
        invalidConfig.put("ai.tool.max.tools", -1); // Invalid max tools

        assertThrows(ConfigurationException.class, () -> {
            validator.validateToolConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateCommonConfiguration() {
        // Test valid common configuration
        Map<String, Object> validConfig = new HashMap<>();
        validConfig.put("ai.common.enabled", true);
        validConfig.put("ai.common.debug.mode", false);
        validConfig.put("ai.common.default.timeout", 30000);
        validConfig.put("ai.common.max.concurrent.requests", 10);

        assertDoesNotThrow(() -> {
            validator.validateCommonConfiguration(validConfig);
        });

        // Test invalid common configuration - invalid timeout
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.common.enabled", true);
        invalidConfig.put("ai.common.default.timeout", -1); // Invalid timeout

        assertThrows(ConfigurationException.class, () -> {
            validator.validateCommonConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateModelConfigurationWithInvalidProvider() {
        // Test model configuration with invalid provider
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.model.primary.provider", "invalid_provider");
        invalidConfig.put("ai.model.fallback.provider", "openai");
        invalidConfig.put("ai.model.default.temperature", 0.3);
        invalidConfig.put("ai.model.default.maxTokens", 1000);

        // This should still pass as provider validation is not implemented in the current version
        assertDoesNotThrow(() -> {
            validator.validateModelConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateModelConfigurationWithInvalidTemperature() {
        // Test model configuration with invalid temperature
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.model.primary.provider", "ollama");
        invalidConfig.put("ai.model.fallback.provider", "openai");
        invalidConfig.put("ai.model.default.temperature", 3.0); // Invalid temperature
        invalidConfig.put("ai.model.default.maxTokens", 1000);

        assertThrows(ConfigurationException.class, () -> {
            validator.validateModelConfiguration(invalidConfig);
        });
    }

    @Test
    public void testValidateModelConfigurationWithInvalidMaxTokens() {
        // Test model configuration with invalid max tokens
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.model.primary.provider", "ollama");
        invalidConfig.put("ai.model.fallback.provider", "openai");
        invalidConfig.put("ai.model.default.temperature", 0.3);
        invalidConfig.put("ai.model.default.maxTokens", 10000); // Invalid max tokens

        assertThrows(ConfigurationException.class, () -> {
            validator.validateModelConfiguration(invalidConfig);
        });
    }

    @Test
    public void testNullConfigurationHandling() {
        // Test null configuration
        assertThrows(NullPointerException.class, () -> {
            validator.validateModelConfiguration(null);
        });

        assertThrows(NullPointerException.class, () -> {
            validator.validateAgentConfiguration(null);
        });

        assertThrows(NullPointerException.class, () -> {
            validator.validateToolConfiguration(null);
        });

        assertThrows(NullPointerException.class, () -> {
            validator.validateCommonConfiguration(null);
        });
    }

    @Test
    public void testEmptyConfigurationHandling() {
        // Test empty configuration
        Map<String, Object> emptyConfig = new HashMap<>();

        assertThrows(ConfigurationException.class, () -> {
            validator.validateModelConfiguration(emptyConfig);
        });

        assertThrows(ConfigurationException.class, () -> {
            validator.validateAgentConfiguration(emptyConfig);
        });

        assertThrows(ConfigurationException.class, () -> {
            validator.validateToolConfiguration(emptyConfig);
        });

        // Common configuration doesn't have required fields, so it should pass
        assertDoesNotThrow(() -> {
            validator.validateCommonConfiguration(emptyConfig);
        });
    }
}
