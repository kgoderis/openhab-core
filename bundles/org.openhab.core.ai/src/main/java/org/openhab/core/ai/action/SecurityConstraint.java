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
package org.openhab.core.ai.action;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted security constraint for `ActionSecurityPolicy` Phase 2 item.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class SecurityConstraint {
    private final Set<String> forbiddenParameters;
    private final Set<String> restrictedRoles;

    public SecurityConstraint(Set<String> forbiddenParameters, Set<String> restrictedRoles) {
        this.forbiddenParameters = Set.copyOf(forbiddenParameters);
        this.restrictedRoles = Set.copyOf(restrictedRoles);
    }

    public boolean isParameterForbidden(String name) {
        return forbiddenParameters.contains(name);
    }

    public boolean isRoleRestricted(String role) {
        return restrictedRoles.contains(role);
    }
}
