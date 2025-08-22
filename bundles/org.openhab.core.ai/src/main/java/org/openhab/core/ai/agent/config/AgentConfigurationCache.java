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
package org.openhab.core.ai.agent.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted cache for agent configuration entries.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class AgentConfigurationCache {
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public void put(String agentId, Map<String, Object> config) {
        cache.put(agentId, Map.copyOf(config));
    }

    public Map<String, Object> get(String agentId) {
        return cache.getOrDefault(agentId, Map.of());
    }

    public void remove(String agentId) {
        cache.remove(agentId);
    }

    public void clear() {
        cache.clear();
    }
}
