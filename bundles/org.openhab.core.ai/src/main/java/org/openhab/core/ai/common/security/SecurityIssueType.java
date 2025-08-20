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
 * Types of security issues that can be detected.
 * 
 * <p>
 * This enum defines the different types of security issues that can be
 * detected during security validation operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public enum SecurityIssueType {
    /**
     * Authentication has failed.
     */
    AUTHENTICATION_FAILED,

    /**
     * Authorization has failed.
     */
    AUTHORIZATION_FAILED,

    /**
     * Content safety violation detected.
     */
    CONTENT_SAFETY_VIOLATION,

    /**
     * Access control violation detected.
     */
    ACCESS_CONTROL_VIOLATION,

    /**
     * Rate limit has been exceeded.
     */
    RATE_LIMIT_EXCEEDED,

    /**
     * System error has occurred.
     */
    SYSTEM_ERROR
}
