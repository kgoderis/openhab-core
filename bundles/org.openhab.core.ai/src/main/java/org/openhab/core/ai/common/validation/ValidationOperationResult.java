package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable validation result following the result classes best practices.
 * 
 * <p>
 * This class provides a unified validation result that follows the established
 * patterns for result classes in the openHAB AI system.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ValidationOperationResult {

    public enum Status {
        SUCCESS,
        FAILURE,
        PARTIAL
    }

    public enum ErrorCode {
        NONE,
        VALIDATION,
        PARAMETER_MISSING,
        INVALID_FORMAT,
        CONSTRAINT_VIOLATION,
        INTERNAL
    }

    private final Status status;
    private final @Nullable Map<String, Object> details;
    private final ErrorCode errorCode;
    private final String message;
    private final List<String> warnings;
    private final String correlationId;
    private final Instant timestamp;
    private final long durationMs;

    private ValidationOperationResult(Status status, @Nullable Map<String, Object> details, ErrorCode errorCode,
            String message, List<String> warnings, String correlationId, Instant timestamp, long durationMs) {
        this.status = Objects.requireNonNull(status);
        this.details = details != null ? Map.copyOf(details) : null;
        this.errorCode = Objects.requireNonNull(errorCode);
        this.message = Objects.requireNonNull(message);
        this.warnings = List.copyOf(warnings);
        this.correlationId = Objects.requireNonNull(correlationId);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.durationMs = durationMs;
    }

    public static ValidationOperationResult success(String correlationId, long durationMs) {
        return new ValidationOperationResult(Status.SUCCESS, null, ErrorCode.NONE, "", List.of(), correlationId,
                Instant.now(), durationMs);
    }

    public static ValidationOperationResult success(Map<String, Object> details, String correlationId,
            long durationMs) {
        return new ValidationOperationResult(Status.SUCCESS, details, ErrorCode.NONE, "", List.of(), correlationId,
                Instant.now(), durationMs);
    }

    public static ValidationOperationResult failure(ErrorCode code, String message, String correlationId,
            long durationMs) {
        return new ValidationOperationResult(Status.FAILURE, null, code, message, List.of(), correlationId,
                Instant.now(), durationMs);
    }

    public static ValidationOperationResult failure(ErrorCode code, String message, List<String> warnings,
            String correlationId, long durationMs) {
        return new ValidationOperationResult(Status.FAILURE, null, code, message, warnings, correlationId,
                Instant.now(), durationMs);
    }

    public static ValidationOperationResult partial(Map<String, Object> details, List<String> warnings,
            String correlationId, long durationMs) {
        return new ValidationOperationResult(Status.PARTIAL, details, ErrorCode.NONE, "", warnings, correlationId,
                Instant.now(), durationMs);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isPartial() {
        return status == Status.PARTIAL;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public Status status() {
        return status;
    }

    public @Nullable Map<String, Object> details() {
        return details;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String message() {
        return message;
    }

    public List<String> warnings() {
        return warnings;
    }

    public String correlationId() {
        return correlationId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public long durationMs() {
        return durationMs;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ValidationOperationResult other = (ValidationOperationResult) obj;
        return status == other.status && Objects.equals(details, other.details) && errorCode == other.errorCode
                && Objects.equals(message, other.message) && Objects.equals(warnings, other.warnings)
                && Objects.equals(correlationId, other.correlationId) && Objects.equals(timestamp, other.timestamp)
                && durationMs == other.durationMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, details, errorCode, message, warnings, correlationId, timestamp, durationMs);
    }

    @Override
    public String toString() {
        return String.format(
                "ValidationOperationResult{status=%s, errorCode=%s, message='%s', warnings=%s, correlationId='%s'}",
                status, errorCode, message, warnings, correlationId);
    }
}
