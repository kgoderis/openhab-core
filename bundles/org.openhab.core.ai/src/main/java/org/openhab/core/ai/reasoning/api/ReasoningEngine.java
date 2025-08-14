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
package org.openhab.core.ai.reasoning.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;

/**
 * Interface for reasoning engines in the AI system.
 * 
 * This interface provides a common abstraction for all reasoning engines,
 * ensuring consistent behavior and proper dependency management.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ReasoningEngine {

    /**
     * Execute reasoning with the given context and prompt.
     * 
     * @param agentId The agent identifier
     * @param context The reasoning context
     * @param prompt The reasoning prompt
     * @param parameters Optional model parameters
     * @return A CompletableFuture containing the reasoning result
     */
    CompletableFuture<ModelResponse> reasonAsync(String agentId, ReasoningContext context, String prompt,
            @Nullable ModelParameters parameters);

    /**
     * Check if the reasoning engine is healthy and ready to process requests.
     * 
     * @return True if the engine is healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Get the engine type identifier.
     * 
     * @return The engine type identifier
     */
    String getEngineType();

    /**
     * Get the engine's current status.
     * 
     * @return The engine status
     */
    ReasoningEngineStatus getStatus();

    /**
     * Shutdown the reasoning engine gracefully.
     */
    void shutdown();

    // enum extracted to top-level: org.openhab.core.ai.reasoning.api.ReasoningEngineStatus
}
