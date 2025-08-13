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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelContext;
import org.openhab.core.ai.model.ModelParameters;

/**
 * Represents a reasoning request in the shared engine.
 *
 * Immutable carrier for all data required to process a reasoning request.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningRequest {
    final String requestId;
    final String agentId;
    final AgentModelContext context;
    final String prompt;
    final @Nullable ModelParameters parameters;
    final long timestamp;

    public ReasoningRequest(String requestId, String agentId, AgentModelContext context, String prompt,
            @Nullable ModelParameters parameters) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.context = context;
        this.prompt = prompt;
        this.parameters = parameters;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAgentId() {
        return agentId;
    }

    public AgentModelContext getContext() {
        return context;
    }

    public String getPrompt() {
        return prompt;
    }

    public @Nullable ModelParameters getParameters() {
        return parameters;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


