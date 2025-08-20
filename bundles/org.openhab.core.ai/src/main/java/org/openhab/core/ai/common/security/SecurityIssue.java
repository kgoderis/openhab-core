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
 * Security issue or incident.
 * 
 * <p>
 * This class represents a security issue or incident that has been detected
 * during security validation operations. It contains information about the
 * type of issue, description, and severity level.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SecurityIssue {
    private final SecurityIssueType type;
    private final String description;
    private final SecurityLevel severity;

    /**
     * Create a new security issue.
     * 
     * @param type the type of security issue
     * @param description the description of the issue
     * @param severity the severity level of the issue
     */
    public SecurityIssue(SecurityIssueType type, String description, SecurityLevel severity) {
        this.type = type;
        this.description = description;
        this.severity = severity;
    }

    /**
     * Get the type of security issue.
     * 
     * @return the issue type
     */
    public SecurityIssueType getType() {
        return type;
    }

    /**
     * Get the description of the security issue.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the severity level of the security issue.
     * 
     * @return the severity level
     */
    public SecurityLevel getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format("SecurityIssue{type=%s, description='%s', severity=%s}", type, description, severity);
    }
}
