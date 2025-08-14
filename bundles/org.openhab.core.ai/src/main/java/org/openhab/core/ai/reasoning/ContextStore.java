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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thread-safe store for session context data tied to agents and sessions.
 *
 * <p>Provides a minimal API to read/write arbitrary context maps by agent and
 * session identifiers, used by reasoning and memory subsystems.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class ContextStore {

    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public void put(String agentId, String sessionId, Map<String, Object> contextData) {
        store.put(key(agentId, sessionId), Map.copyOf(contextData));
    }

    public Map<String, Object> get(String agentId, String sessionId) {
        return store.getOrDefault(key(agentId, sessionId), Map.of());
    }

    public void remove(String agentId, String sessionId) {
        store.remove(key(agentId, sessionId));
    }

    public void clearAgent(String agentId) {
        store.keySet().removeIf(k -> k.startsWith(agentId + ":"));
    }

    public void clearAll() {
        store.clear();
    }

    private String key(String agentId, String sessionId) {
        return agentId + ":" + sessionId;
    }
}


