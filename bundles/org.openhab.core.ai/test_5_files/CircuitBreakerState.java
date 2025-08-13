package org.openhab.core.ai.tool.error;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    public enum CircuitBreakerState {
        CLOSED, // Normal operation
        OPEN, // Circuit breaker is open, no requests allowed
        HALF_OPEN // Testing if service is back to normal
    }