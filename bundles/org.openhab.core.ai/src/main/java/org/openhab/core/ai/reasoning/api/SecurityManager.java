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

    /**
     * Security request.
     */
    class SecurityRequest {
        private final String requestId;
        private final String agentId;
        private final String modelId;
        private final String taskType;
        private final String prompt;
        private final String authenticationToken;
        private final long timestamp;

        public SecurityRequest(String requestId, String agentId, String modelId, String taskType, String prompt,
                String authenticationToken, long timestamp) {
            this.requestId = requestId;
            this.agentId = agentId;
            this.modelId = modelId;
            this.taskType = taskType;
            this.prompt = prompt;
            this.authenticationToken = authenticationToken;
            this.timestamp = timestamp;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getModelId() {
            return modelId;
        }

        public String getTaskType() {
            return taskType;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getAuthenticationToken() {
            return authenticationToken;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Security validation result.
     */
    class SecurityValidationResult {
        private final String requestId;
        private final boolean valid;
        private final SecurityIssue[] issues;
        private final long validationTime;

        public SecurityValidationResult(String requestId, boolean valid, SecurityIssue[] issues, long validationTime) {
            this.requestId = requestId;
            this.valid = valid;
            this.issues = issues;
            this.validationTime = validationTime;
        }

        public String getRequestId() {
            return requestId;
        }

        public boolean isValid() {
            return valid;
        }

        public SecurityIssue[] getIssues() {
            return issues;
        }

        public long getValidationTime() {
            return validationTime;
        }
    }

    /**
     * Quick security result.
     */
    class QuickSecurityResult {
        private final boolean allowed;
        private final String reason;
        private final long checkTime;

        public QuickSecurityResult(boolean allowed, String reason, long checkTime) {
            this.allowed = allowed;
            this.reason = reason;
            this.checkTime = checkTime;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public long getCheckTime() {
            return checkTime;
        }
    }

    /**
     * Security issue.
     */
    class SecurityIssue {
        private final SecurityIssueType type;
        private final String description;
        private final SecurityLevel severity;

        public SecurityIssue(SecurityIssueType type, String description, SecurityLevel severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }

        public SecurityIssueType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public SecurityLevel getSeverity() {
            return severity;
        }
    }

    /**
     * Security issue types.
     */
    enum SecurityIssueType {
        AUTHENTICATION_FAILED,
        AUTHORIZATION_FAILED,
        CONTENT_SAFETY_VIOLATION,
        ACCESS_CONTROL_VIOLATION,
        RATE_LIMIT_EXCEEDED,
        SYSTEM_ERROR
    }

    /**
     * Security levels.
     */
    enum SecurityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
