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

/**
 * Extracted configuration holder for negotiation templates.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class NegotiationTemplateConfig {
    private final String templateId;
    private final Map<String, Object> parameters;

    public NegotiationTemplateConfig(String templateId, Map<String, Object> parameters) {
        this.templateId = templateId;
        this.parameters = Map.copyOf(parameters);
    }

    public String getTemplateId() { return templateId; }
    public Map<String, Object> getParameters() { return parameters; }
}


