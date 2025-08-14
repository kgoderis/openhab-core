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
import org.openhab.core.ai.reasoning.api.SecurityRequest;
import org.openhab.core.ai.reasoning.api.ThreatLevel;

/**
 * Simple threat detector for reasoning requests.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class ThreatDetector {

    public ThreatLevel detect(SecurityRequest request) {
        // Minimal heuristic: critical if authentication missing
        if (request.getAuthenticationToken().isBlank()) {
            return ThreatLevel.CRITICAL;
        }
        return ThreatLevel.LOW;
    }
}


