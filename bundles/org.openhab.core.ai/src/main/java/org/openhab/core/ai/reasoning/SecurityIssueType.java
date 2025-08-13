package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Types of security issues detected during validation.
 */
@NonNullByDefault
public enum SecurityIssueType {
    AUTHENTICATION_FAILED,
    AUTHORIZATION_FAILED,
    CONTENT_SAFETY_VIOLATION,
    ACCESS_CONTROL_VIOLATION,
    RATE_LIMIT_EXCEEDED,
    SYSTEM_ERROR
}


