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
package org.openhab.core.ai.reasoning.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for security management in the AI reasoning system.
 * 
 * This interface provides a common abstraction for security operations,
 * ensuring consistent behavior across all reasoning components.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface SecurityManager {

    /**
     * Validate security for a reasoning request.
     * 
     * @param request The security request
     * @return A CompletableFuture containing the security validation result
     */
    CompletableFuture<SecurityValidationResult> validateSecurity(SecurityRequest request);

    /**
     * Perform a quick security check.
     * 
     * @param agentId The agent identifier
     * @param action The action to validate
     * @return A CompletableFuture containing the quick security result
     */
    CompletableFuture<QuickSecurityResult> quickSecurityCheck(String agentId, String action);

    // Extracted: SecurityRequest

    // Extracted: SecurityValidationResult

    // Extracted: QuickSecurityResult

    // Extracted: SecurityIssue

    // Extracted: SecurityIssueType

    // Extracted: SecurityLevel
}
