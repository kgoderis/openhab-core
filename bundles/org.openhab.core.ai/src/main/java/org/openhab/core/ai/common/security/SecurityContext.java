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

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.BaseContext;

/**
 * Security context for policy validation.
 * 
 * <p>
 * This class extends BaseContext to provide security-specific context functionality
 * for validating security policies. It includes security-specific fields and methods
 * while maintaining compatibility with the existing context hierarchy.
 * </p>
 * 
 * <p>
 * A security context contains all the information needed to validate
 * whether a particular operation or access request complies with
 * a security policy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class SecurityContext extends BaseContext {

    // Security-specific context keys
    public static final String PRINCIPAL_ID_KEY = "principalId";
    public static final String ACTION_KEY = "action";
    public static final String RESOURCE_ID_KEY = "resourceId";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String TIMESTAMP_KEY = "timestamp";

    /**
     * Create a new security context.
     * 
     * @param contextId the unique context identifier
     * @param principalId the principal identifier (user, agent, etc.)
     * @param action the action or operation being performed
     * @param resourceId the resource identifier being accessed
     * @param parameters additional context parameters
     */
    public SecurityContext(String contextId, String principalId, String action, String resourceId,
            @Nullable Map<String, Object> parameters) {
        super(contextId, "security", "1.0", buildSecurityValues(principalId, action, resourceId, parameters), null);
    }

    /**
     * Create a new security context with custom timestamps.
     * 
     * @param contextId the unique context identifier
     * @param principalId the principal identifier (user, agent, etc.)
     * @param action the action or operation being performed
     * @param resourceId the resource identifier being accessed
     * @param parameters additional context parameters
     * @param createdAt the creation timestamp
     * @param lastModifiedAt the last modification timestamp
     */
    public SecurityContext(String contextId, String principalId, String action, String resourceId,
            @Nullable Map<String, Object> parameters, Instant createdAt, Instant lastModifiedAt) {
        super(contextId, "security", "1.0", buildSecurityValues(principalId, action, resourceId, parameters), null,
                createdAt, lastModifiedAt);
    }

    /**
     * Get the principal identifier (user, agent, etc.).
     * 
     * @return the principal ID
     */
    public String getPrincipalId() {
        return getValue(PRINCIPAL_ID_KEY, String.class);
    }

    /**
     * Get the action or operation being performed.
     * 
     * @return the action/operation
     */
    public String getAction() {
        return getValue(ACTION_KEY, String.class);
    }

    /**
     * Get the resource identifier being accessed.
     * 
     * @return the resource ID
     */
    public String getResourceId() {
        return getValue(RESOURCE_ID_KEY, String.class);
    }

    /**
     * Get additional context parameters.
     * 
     * @return map of context parameters
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameters() {
        Map<String, Object> params = getValue(PARAMETERS_KEY, Map.class);
        return params != null ? params : Map.of();
    }

    /**
     * Get the timestamp when this context was created.
     * 
     * @return the timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        Long timestamp = getValue(TIMESTAMP_KEY, Long.class);
        return timestamp != null ? timestamp : getCreatedAt().toEpochMilli();
    }

    /**
     * Create a copy of this security context with new values and metadata.
     * 
     * @param newValues the new values map
     * @param newMetadata the new metadata map
     * @return a new security context instance
     */
    @Override
    protected SecurityContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new SecurityContext(getContextId(), (String) newValues.get(PRINCIPAL_ID_KEY),
                (String) newValues.get(ACTION_KEY), (String) newValues.get(RESOURCE_ID_KEY),
                (Map<String, Object>) newValues.get(PARAMETERS_KEY), getCreatedAt(), Instant.now());
    }

    /**
     * Build security-specific values map.
     * 
     * @param principalId the principal ID
     * @param action the action
     * @param resourceId the resource ID
     * @param parameters the parameters
     * @return the values map
     */
    private static Map<String, Object> buildSecurityValues(String principalId, String action, String resourceId,
            @Nullable Map<String, Object> parameters) {
        return Map.of(PRINCIPAL_ID_KEY, principalId, ACTION_KEY, action, RESOURCE_ID_KEY, resourceId, PARAMETERS_KEY,
                parameters != null ? parameters : Map.of(), TIMESTAMP_KEY, Instant.now().toEpochMilli());
    }

    @Override
    public String toString() {
        return String.format("SecurityContext{id='%s', principalId='%s', action='%s', resourceId='%s'}", getContextId(),
                getPrincipalId(), getAction(), getResourceId());
    }
}
