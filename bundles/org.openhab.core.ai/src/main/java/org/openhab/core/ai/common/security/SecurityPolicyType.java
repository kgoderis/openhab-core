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
 * Policy types for classification and organization of security policies.
 * 
 * <p>
 * This enum defines the different types of security policies that can be
 * implemented in the openHAB AI system. Each type represents a specific
 * domain or area of security concern.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public enum SecurityPolicyType {

    /**
     * Agent security policies - control agent behavior and permissions
     */
    AGENT_SECURITY,

    /**
     * Action security policies - control action execution permissions
     */
    ACTION_SECURITY,

    /**
     * Model security policies - control model access and usage
     */
    MODEL_SECURITY,

    /**
     * Tool security policies - control tool access and operations
     */
    TOOL_SECURITY,

    /**
     * General security policies - generic security controls
     */
    GENERAL_SECURITY
}
