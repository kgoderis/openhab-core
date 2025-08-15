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
package org.openhab.core.ai.reasoning.error;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.error.api.ErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized error logger for reasoning components.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class ErrorLogger {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogger.class);

    public void log(Throwable error, ErrorContext context) {
        logger.warn("[AI-Reasoning] error in component={}, operation={}, agent={} - {}", context.getComponentId(),
                context.getOperation(), context.getAgentId(), error.toString());
    }
}
