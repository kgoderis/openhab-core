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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common Security Policy Interface
 * 
 * <p>
 * This interface defines the core security policy contract that is common across
 * all security policy implementations (agent, action, model, tool). It provides
 * a unified contract for basic security policy functionality while allowing
 * domain-specific implementations to extend with additional features.
 * </p>
 * 
 * <p>
 * The interface focuses on the essential security policy aspects:
 * - Policy identification and metadata
 * - Security level and enabled status
 * - Context validation capabilities
 * - Policy type classification
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface SecurityPolicy {

    /**
     * Get the unique policy identifier.
     * 
     * @return the policy ID
     */
    String getPolicyId();

    /**
     * Get the human-readable policy name.
     * 
     * @return the policy name
     */
    String getPolicyName();

    /**
     * Check if this policy is enabled and active.
     * 
     * @return true if the policy is enabled
     */
    boolean isEnabled();

    /**
     * Get the security level for this policy.
     * 
     * @return the security level
     */
    SecurityLevel getSecurityLevel();

    /**
     * Get the policy type for classification purposes.
     * 
     * @return the policy type
     */
    SecurityPolicyType getPolicyType();

    /**
     * Validate a security context against this policy.
     * 
     * <p>
     * This method validates whether a given security context complies with
     * this policy's security requirements. The specific validation logic
     * is implementation-dependent.
     * </p>
     * 
     * @param context the security context to validate
     * @return true if the context is valid according to this policy
     */
    boolean validateContext(SecurityContext context);

    /**
     * Get policy metadata as a map for serialization or inspection.
     * 
     * @return map containing policy metadata
     */
    Map<String, Object> getPolicyMetadata();
}
