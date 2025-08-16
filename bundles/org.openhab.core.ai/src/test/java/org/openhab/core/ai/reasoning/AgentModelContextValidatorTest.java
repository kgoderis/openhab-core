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
package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AgentModelContextValidator.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AgentModelContextValidatorTest {

    private AgentModelContextValidator validator;
    private AgentModelContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        validator = new AgentModelContextValidator();
        contextBuilder = new AgentModelContextBuilder();
    }

    @Test
    void testValidateValidContext() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createValidContext();

        // Act
        ContextValidationResult result = validator.validate(context);

        // Assert
        assertTrue(result.isValid());
        assertEquals(0, result.getIssueCount());
        assertEquals(0, result.getWarningCount());
    }

    @Test
    void testValidateMissingRequiredFields() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createContextWithMissingFields();

        // Act
        ContextValidationResult result = validator.validate(context);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getIssueCount() > 0);
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("agentId")));
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("agentType")));
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("domain")));
    }

    @Test
    void testValidateDataQualityIssues() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createContextWithDataQualityIssues();

        // Act
        ContextValidationResult result = validator.validate(context);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getIssueCount() > 0);
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("Null value")));
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("Empty string")));
    }

    @Test
    void testValidateConsistencyIssues() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createContextWithConsistencyIssues();

        // Act
        ContextValidationResult result = validator.validate(context);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getIssueCount() > 0);
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("should have")));
    }

    @Test
    void testValidateCompletenessIssues() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createContextWithCompletenessIssues();

        // Act
        ContextValidationResult result = validator.validate(context);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getIssueCount() > 0);
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("capabilities")));
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("current state")));
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("user preferences")));
    }

    @Test
    void testOptimizeValidContext() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createValidContext();

        // Act
        ContextOptimizationResult result = validator.optimize(context);

        // Assert
        assertTrue(result.hasRecommendations());
        assertTrue(result.getRecommendationCount() > 0);
    }

    @Test
    void testOptimizeLargeContext() {
        // Arrange
        AgentModelContextBuilder.AgentModelContext context = createLargeContext();

        // Act
        ContextOptimizationResult result = validator.optimize(context);

        // Assert
        assertTrue(result.hasRecommendations());
        assertTrue(result.getRecommendations().stream().anyMatch(rec -> rec.contains("reducing context data size")));
    }

    @Test
    void testAddCustomValidationRule() {
        // Arrange
        AgentModelContextValidator.ValidationRule rule = context -> {
            List<String> issues = new ArrayList<>();
            if (context.getContextData().containsKey("testField")) {
                issues.add("Custom validation failed");
            }
            return issues;
        };

        // Act
        validator.addValidationRule("testRule", rule);

        // Assert
        AgentModelContextBuilder.AgentModelContext context = createValidContext();
        context.getContextData().put("testField", "testValue");
        ContextValidationResult result = validator.validate(context);
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("testRule")));
    }

    @Test
    void testAddCustomOptimizationRule() {
        // Arrange
        AgentModelContextValidator.OptimizationRule rule = context -> {
            List<String> recommendations = new ArrayList<>();
            if (context.getContextData().size() > 10) {
                recommendations.add("Consider reducing context size");
            }
            return recommendations;
        };

        // Act
        validator.addOptimizationRule("testRule", rule);

        // Assert
        AgentModelContextBuilder.AgentModelContext context = createLargeContext();
        ContextOptimizationResult result = validator.optimize(context);
        assertTrue(result.getRecommendations().stream().anyMatch(rec -> rec.contains("testRule")));
    }

    @Test
    void testValidationResultMethods() {
        // Arrange
        ContextValidationResult result = new ContextValidationResult();

        // Act
        result.addIssue("Test issue");
        result.addWarning("Test warning");

        // Assert
        assertEquals(1, result.getIssueCount());
        assertEquals(1, result.getWarningCount());
        assertFalse(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals("Test issue", result.getIssues().get(0));
        assertEquals("Test warning", result.getWarnings().get(0));
    }

    @Test
    void testOptimizationResultMethods() {
        // Arrange
        ContextOptimizationResult result = new ContextOptimizationResult();

        // Act
        result.addRecommendation("Test recommendation");
        result.addOptimization("Test optimization");

        // Assert
        assertEquals(1, result.getRecommendationCount());
        assertEquals(1, result.getOptimizationCount());
        assertTrue(result.hasRecommendations());
        assertTrue(result.hasOptimizations());
        assertEquals("Test recommendation", result.getRecommendations().get(0));
        assertEquals("Test optimization", result.getOptimizations().get(0));
    }

    private AgentModelContextBuilder.AgentModelContext createValidContext() {
        Map<String, Object> capabilities = Map.of("reasoning", true, "planning", true);
        Map<String, Object> userPreferences = Map.of("efficiency", "high", "comfort", "medium");

        return contextBuilder.create().withAgentId("test-agent-1").withAgentType("energy")
                .withDomain("energy_management").withCapabilities(Map.of("reasoning", "true", "planning", "true"))
                .withCurrentState(Map.of("status", "operational")).withUserPreferences(userPreferences)
                .withMetadata("priority", "high").withMetadata("timestamp", System.currentTimeMillis())
                .withMetadata("source", "test").build();
    }

    private AgentModelContextBuilder.AgentModelContext createContextWithMissingFields() {
        return contextBuilder.create().withCapabilities(Map.of("reasoning", "true")).build();
    }

    private AgentModelContextBuilder.AgentModelContext createContextWithDataQualityIssues() {
        return contextBuilder.create().withAgentId("test-agent-1").withAgentType("energy")
                .withDomain("energy_management").withContextData("nullField", null).withContextData("emptyField", "")
                .build();
    }

    private AgentModelContextBuilder.AgentModelContext createContextWithConsistencyIssues() {
        return contextBuilder.create().withAgentId("test-agent-1").withAgentType("energy")
                .withDomain("security_management") // Inconsistent with energy agent type
                .build();
    }

    private AgentModelContextBuilder.AgentModelContext createContextWithCompletenessIssues() {
        return contextBuilder.create().withAgentId("test-agent-1").withAgentType("energy")
                .withDomain("energy_management")
                // Missing capabilities, currentState, and userPreferences
                .build();
    }

    private AgentModelContextBuilder.AgentModelContext createLargeContext() {
        AgentModelContextBuilder builder = contextBuilder.create().withAgentId("test-agent-1").withAgentType("energy")
                .withDomain("energy_management");

        // Add many fields to make it large
        for (int i = 0; i < 60; i++) {
            builder.withContextData("field" + i, "value" + i);
        }

        return builder.build();
    }
}
