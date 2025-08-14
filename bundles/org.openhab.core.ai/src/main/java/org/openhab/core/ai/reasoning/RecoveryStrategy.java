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
import org.openhab.core.ai.reasoning.api.ErrorContext;
import org.openhab.core.ai.reasoning.api.ErrorRecoveryResult;

/**
 * Strategy wrapper that selects appropriate recovery path.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class RecoveryStrategy {

    private final ErrorRecovery errorRecovery = new ErrorRecovery();
    private final FaultTolerance faultTolerance = new FaultTolerance();

    public ErrorRecoveryResult recover(Throwable error, ErrorContext context) {
        if (faultTolerance.shouldFallback(error)) {
            return new ErrorRecoveryResult(true, "FALLBACK", "Fallback strategy suggested",
                    System.currentTimeMillis());
        }
        return errorRecovery.recover(error, context);
    }
}


