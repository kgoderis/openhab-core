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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.error.ErrorRecoveryResult;
import org.openhab.core.ai.reasoning.error.api.ErrorContext;

/**
 * Provides basic recovery actions for reasoning errors.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class ErrorRecovery {

    public ErrorRecoveryResult recover(Throwable error, ErrorContext context) {
        // Minimal recovery: categorize as retriable when transient
        boolean retriable = isTransient(error);
        String action = retriable ? "RETRY" : "ESCALATE";
        String msg = retriable ? "Retry suggested" : "Manual intervention required";
        return new ErrorRecoveryResult(retriable, retriable ? "RECOVERED" : "FAILED", action, msg, Map.of(),
                System.currentTimeMillis(), 0);
    }

    private boolean isTransient(Throwable error) {
        String msg = error.getMessage();
        return msg != null && (msg.contains("timeout") || msg.contains("temporarily") || msg.contains("retry"));
    }
}
