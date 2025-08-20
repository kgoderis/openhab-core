package org.openhab.core.ai.common.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified security level enumeration for openHAB AI components.
 * 
 * Defines the different security levels that can be used across
 * agent, reasoning, action, and other AI components.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum SecurityLevel {
    /**
     * Low security level.
     * Minimal security requirements.
     */
    LOW("low", "Low Security"),

    /**
     * Medium security level.
     * Standard security requirements.
     */
    MEDIUM("medium", "Medium Security"),

    /**
     * High security level.
     * Enhanced security requirements.
     */
    HIGH("high", "High Security"),

    /**
     * Critical security level.
     * Maximum security requirements.
     */
    CRITICAL("critical", "Critical Security"),

    /**
     * Maximum security level.
     * Highest possible security requirements.
     */
    MAXIMUM("maximum", "Maximum Security");

    private final String code;
    private final String description;

    /**
     * Create a new security level.
     *
     * @param code the security level code
     * @param description the security level description
     */
    SecurityLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the security level code.
     *
     * @return the security level code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the security level description.
     *
     * @return the security level description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get security level by code.
     *
     * @param code the security level code
     * @return the security level, or null if not found
     */
    public static SecurityLevel fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (SecurityLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Check if this security level is at least as high as the specified level.
     *
     * @param minimumLevel the minimum security level
     * @return true if this level is at least as high as the minimum
     */
    public boolean isAtLeast(SecurityLevel minimumLevel) {
        return this.ordinal() >= minimumLevel.ordinal();
    }

    /**
     * Check if this security level requires authentication.
     *
     * @return true if authentication is required
     */
    public boolean requiresAuthentication() {
        return this != LOW;
    }

    /**
     * Check if this security level requires authorization.
     *
     * @return true if authorization is required
     */
    public boolean requiresAuthorization() {
        return this == HIGH || this == CRITICAL || this == MAXIMUM;
    }

    /**
     * Check if this security level requires encryption.
     *
     * @return true if encryption is required
     */
    public boolean requiresEncryption() {
        return this == HIGH || this == CRITICAL || this == MAXIMUM;
    }

    @Override
    public String toString() {
        return code + " (" + description + ")";
    }
}
