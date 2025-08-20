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
package org.openhab.core.ai.common.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Quick security check result.
 * 
 * <p>
 * This class represents the result of a quick security check operation.
 * It provides information about whether an operation is allowed and the
 * reason for the decision.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class QuickSecurityResult {
    private final boolean allowed;
    private final String reason;
    private final long checkTime;

    /**
     * Create a new quick security result.
     * 
     * @param allowed whether the operation is allowed
     * @param reason the reason for the decision
     * @param checkTime the time when the check was performed
     */
    public QuickSecurityResult(boolean allowed, String reason, long checkTime) {
        this.allowed = allowed;
        this.reason = reason;
        this.checkTime = checkTime;
    }

    /**
     * Create a quick security result for an allowed operation.
     * 
     * @param reason the reason for allowing the operation
     * @return a new QuickSecurityResult instance
     */
    public static QuickSecurityResult granted(String reason) {
        return new QuickSecurityResult(true, reason, System.currentTimeMillis());
    }

    /**
     * Create a quick security result for a denied operation.
     * 
     * @param reason the reason for denying the operation
     * @return a new QuickSecurityResult instance
     */
    public static QuickSecurityResult denied(String reason) {
        return new QuickSecurityResult(false, reason, System.currentTimeMillis());
    }

    /**
     * Check if the operation is allowed.
     * 
     * @return true if the operation is allowed
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Get the reason for the security decision.
     * 
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Get the time when the security check was performed.
     * 
     * @return the check time
     */
    public long getCheckTime() {
        return checkTime;
    }

    @Override
    public String toString() {
        return String.format("QuickSecurityResult{allowed=%s, reason='%s', checkTime=%d}", allowed, reason, checkTime);
    }
}
