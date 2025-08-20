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
package org.openhab.core.ai.reasoning.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access controller for model security.
 * 
 * This class manages access control for model operations based on security policies.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AccessController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessController.class);

    private final Map<String, ModelSecurityPolicy> policies = new ConcurrentHashMap<>();

    /**
     * Check if access is allowed for the given model and operation.
     * 
     * @param modelId the model ID
     * @param operation the operation to perform
     * @param userId the user ID requesting access
     * @return true if access is allowed, false otherwise
     */
    public boolean isAccessAllowed(String modelId, String operation, @Nullable String userId) {
        ModelSecurityPolicy policy = policies.get(modelId);
        if (policy == null) {
            LOGGER.warn("No security policy found for model: {}", modelId);
            return false;
        }

        if (!policy.isEnabled()) {
            LOGGER.debug("Security policy is disabled for model: {}", modelId);
            return false;
        }

        if (!policy.isModelAllowed(modelId)) {
            LOGGER.warn("Model {} is not allowed by security policy", modelId);
            return false;
        }

        if (!policy.isOperationAllowed(operation)) {
            LOGGER.warn("Operation {} is not allowed for model {} by security policy", operation, modelId);
            return false;
        }

        LOGGER.debug("Access allowed for model: {}, operation: {}, user: {}", modelId, operation, userId);
        return true;
    }

    /**
     * Add a security policy for a model.
     * 
     * @param modelId the model ID
     * @param policy the security policy
     */
    public void addPolicy(String modelId, ModelSecurityPolicy policy) {
        policies.put(modelId, policy);
        LOGGER.debug("Added security policy for model: {}", modelId);
    }

    /**
     * Remove a security policy for a model.
     * 
     * @param modelId the model ID
     */
    public void removePolicy(String modelId) {
        policies.remove(modelId);
        LOGGER.debug("Removed security policy for model: {}", modelId);
    }

    /**
     * Get the security policy for a model.
     * 
     * @param modelId the model ID
     * @return the security policy, or null if not found
     */
    @Nullable
    public ModelSecurityPolicy getPolicy(String modelId) {
        return policies.get(modelId);
    }

    /**
     * Check if a policy exists for the given model.
     * 
     * @param modelId the model ID
     * @return true if a policy exists, false otherwise
     */
    public boolean hasPolicy(String modelId) {
        return policies.containsKey(modelId);
    }
}
