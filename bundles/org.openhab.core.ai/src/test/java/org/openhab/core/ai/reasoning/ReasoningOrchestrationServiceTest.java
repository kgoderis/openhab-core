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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.model.api.ModelParameters;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.api.ReasoningPlanStep;

/**
 * Test class for ReasoningOrchestrationService
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ReasoningOrchestrationServiceTest {

    private ReasoningOrchestrationService orchestrationService;

    @Mock
    private IntelligentToolClient mockClient;

    @Mock
    private MultiStepReasoningEngine mockReasoningEngine;

    @Mock
    private ContextMemoryManager mockContextMemoryManager;

    @Mock
    private AgentMemory mockAgentMemory;

    @Mock
    private LearningAdaptationSystem mockLearningSystem;

    @Mock
    private SafetyConstraintManager mockSafetyManager;

    @BeforeEach
    void setUp() {
        orchestrationService = new ReasoningOrchestrationService();
        // Activate the service manually for testing
        orchestrationService.activate();
    }

    @Test
    void testServiceActivation() {
        assertNotNull(orchestrationService);
        // Service should be activated after setUp
        assertTrue(orchestrationService.getMetrics().getTotalSessions() >= 0);
    }

    @Test
    void testDefaultStrategiesRegistration() {
        // Verify that default strategies are registered
        assertNotNull(orchestrationService.getStrategy("sequential"));
        assertNotNull(orchestrationService.getStrategy("parallel"));
        assertNotNull(orchestrationService.getStrategy("adaptive"));
    }

    @Test
    void testStepValidation() {
        List<ReasoningPlanStep> validSteps = createValidSteps();
        ReasoningOrchestrationService.ValidationResult result = orchestrationService.validateSteps(validSteps);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testStepValidationWithCircularDependencies() {
        List<ReasoningPlanStep> invalidSteps = createStepsWithCircularDependencies();
        ReasoningOrchestrationService.ValidationResult result = orchestrationService.validateSteps(invalidSteps);

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Circular dependencies")));
    }

    @Test
    void testStepValidationWithMissingDependencies() {
        List<ReasoningPlanStep> invalidSteps = createStepsWithMissingDependencies();
        ReasoningOrchestrationService.ValidationResult result = orchestrationService.validateSteps(invalidSteps);

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("depends on non-existent step")));
    }

    @Test
    void testMetricsCollection() {
        ReasoningOrchestrationService.OrchestrationMetrics metrics = orchestrationService.getMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.getActiveSessions() >= 0);
        assertTrue(metrics.getTotalSessions() >= 0);
    }

    @Test
    void testStrategyRegistration() {
        ReasoningOrchestrationService.ReasoningStrategy customStrategy = mock(
                ReasoningOrchestrationService.ReasoningStrategy.class);

        orchestrationService.registerStrategy("custom", customStrategy);

        assertNotNull(orchestrationService.getStrategy("custom"));
        assertEquals(customStrategy, orchestrationService.getStrategy("custom"));
    }

    @Test
    void testServiceDeactivation() {
        orchestrationService.deactivate();

        // After deactivation, the service should not accept new requests
        ReasoningContext context = new ReasoningContext.Builder().build();
        ModelParameters params = ModelParameters.builder().build();

        CompletableFuture<MultiStepReasoningResult> future = orchestrationService.orchestrateReasoning(mockClient,
                context, params);

        assertTrue(future.isCompletedExceptionally());
    }

    private List<ReasoningPlanStep> createValidSteps() {
        List<ReasoningPlanStep> steps = new ArrayList<>();

        steps.add(new ReasoningPlanStep() {
            @Override
            public String getId() {
                return "step1";
            }

            @Override
            public String getPrompt() {
                return "First step";
            }

            @Override
            public List<String> getDependencies() {
                return new ArrayList<>();
            }
        });

        steps.add(new ReasoningPlanStep() {
            @Override
            public String getId() {
                return "step2";
            }

            @Override
            public String getPrompt() {
                return "Second step";
            }

            @Override
            public List<String> getDependencies() {
                List<String> deps = new ArrayList<>();
                deps.add("step1");
                return deps;
            }
        });

        return steps;
    }

    private List<ReasoningPlanStep> createStepsWithCircularDependencies() {
        List<ReasoningPlanStep> steps = new ArrayList<>();

        steps.add(new ReasoningPlanStep() {
            @Override
            public String getId() {
                return "step1";
            }

            @Override
            public String getPrompt() {
                return "First step";
            }

            @Override
            public List<String> getDependencies() {
                List<String> deps = new ArrayList<>();
                deps.add("step2");
                return deps;
            }
        });

        steps.add(new ReasoningPlanStep() {
            @Override
            public String getId() {
                return "step2";
            }

            @Override
            public String getPrompt() {
                return "Second step";
            }

            @Override
            public List<String> getDependencies() {
                List<String> deps = new ArrayList<>();
                deps.add("step1");
                return deps;
            }
        });

        return steps;
    }

    private List<ReasoningPlanStep> createStepsWithMissingDependencies() {
        List<ReasoningPlanStep> steps = new ArrayList<>();

        steps.add(new ReasoningPlanStep() {
            @Override
            public String getId() {
                return "step1";
            }

            @Override
            public String getPrompt() {
                return "First step";
            }

            @Override
            public List<String> getDependencies() {
                List<String> deps = new ArrayList<>();
                deps.add("nonexistent");
                return deps;
            }
        });

        return steps;
    }
}
