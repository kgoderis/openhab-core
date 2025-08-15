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
package org.openhab.core.ai.reasoning.engine;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Health status information for the reasoning engine.
 *
 * Provides an immutable snapshot of the engine's health, including the number
 * of active sessions, queued requests, and executor state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningEngineHealthStatus {
    private final boolean healthy;
    private final int activeSessions;
    private final int queuedRequests;
    private final boolean executorShutdown;

    public ReasoningEngineHealthStatus(boolean healthy, int activeSessions, int queuedRequests,
            boolean executorShutdown) {
        this.healthy = healthy;
        this.activeSessions = activeSessions;
        this.queuedRequests = queuedRequests;
        this.executorShutdown = executorShutdown;
    }

    public boolean isHealthy() {
        return healthy && !executorShutdown;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public int getQueuedRequests() {
        return queuedRequests;
    }

    public boolean isExecutorShutdown() {
        return executorShutdown;
    }
}
