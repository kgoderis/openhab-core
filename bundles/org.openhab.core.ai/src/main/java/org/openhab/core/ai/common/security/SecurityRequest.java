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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Generalized security request for validation operations.
 * 
 * <p>
 * This class represents a basic security request that can be validated by security managers.
 * It contains only the common security fields that are relevant across all domains.
 * Domain-specific security requests should extend this class or create their own
 * specialized versions.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class SecurityRequest {
    private final String requestId;
    private final @Nullable String authenticationToken;
    private final long timestamp;
    private final Map<String, Object> metadata;

    /**
     * Create a new security request.
     * 
     * @param requestId the unique request identifier
     * @param authenticationToken the authentication token (may be null)
     * @param timestamp the request timestamp
     * @param metadata additional metadata for the request
     */
    public SecurityRequest(String requestId, @Nullable String authenticationToken, long timestamp,
            Map<String, Object> metadata) {
        this.requestId = requestId;
        this.authenticationToken = authenticationToken;
        this.timestamp = timestamp;
        this.metadata = Map.copyOf(metadata);
    }

    /**
     * Create a new security request with minimal metadata.
     * 
     * @param requestId the unique request identifier
     * @param authenticationToken the authentication token (may be null)
     * @param timestamp the request timestamp
     */
    public SecurityRequest(String requestId, @Nullable String authenticationToken, long timestamp) {
        this(requestId, authenticationToken, timestamp, Map.of());
    }

    /**
     * Get the request identifier.
     * 
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Get the authentication token.
     * 
     * @return the authentication token, or null if not provided
     */
    public @Nullable String getAuthenticationToken() {
        return authenticationToken;
    }

    /**
     * Get the request timestamp.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the metadata for this request.
     * 
     * @return immutable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Get a specific metadata value.
     * 
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Get the agent ID from metadata.
     * 
     * @return the agent ID, or null if not found
     */
    public @Nullable String getAgentId() {
        return getMetadata("agentId");
    }

    @Override
    public String toString() {
        return String.format("SecurityRequest{requestId='%s', timestamp=%d, metadataSize=%d}", requestId, timestamp,
                metadata.size());
    }
}
