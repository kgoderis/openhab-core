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
package org.openhab.core.ai.agent.collaboration.negotiation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted context holder for negotiation sessions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class NegotiationSessionContext {
    private final String sessionId;
    private final String templateId;
    private final Map<String, Object> contextData;

    public NegotiationSessionContext(String sessionId, String templateId, Map<String, Object> contextData) {
        this.sessionId = sessionId;
        this.templateId = templateId;
        this.contextData = Map.copyOf(contextData);
    }

    public String getSessionId() { return sessionId; }
    public String getTemplateId() { return templateId; }
    public Map<String, Object> getContextData() { return contextData; }
    public @Nullable Object get(String key) { return contextData.get(key); }
}


