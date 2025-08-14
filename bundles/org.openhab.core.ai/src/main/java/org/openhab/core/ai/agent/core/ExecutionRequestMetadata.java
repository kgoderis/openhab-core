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
package org.openhab.core.ai.agent.core;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted metadata holder for execution requests.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class ExecutionRequestMetadata {
    private final String requestId;
    private final @Nullable String correlationId;
    private final Map<String, Object> attributes;

    public ExecutionRequestMetadata(String requestId, @Nullable String correlationId, Map<String, Object> attributes) {
        this.requestId = requestId;
        this.correlationId = correlationId;
        this.attributes = Map.copyOf(attributes);
    }

    public String getRequestId() { return requestId; }
    public @Nullable String getCorrelationId() { return correlationId; }
    public Map<String, Object> getAttributes() { return attributes; }
}


