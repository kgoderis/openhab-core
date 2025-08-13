package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ReasoningSessionResult {
    private final boolean success;
    private final @Nullable MemoryReasoningSession session;
    private final @Nullable String error;

    private ReasoningSessionResult(boolean success, @Nullable MemoryReasoningSession session, @Nullable String error) {
        this.success = success;
        this.session = session;
        this.error = error;
    }

    public static ReasoningSessionResult success(MemoryReasoningSession session) { return new ReasoningSessionResult(true, session, null); }
    public static ReasoningSessionResult error(String error) { return new ReasoningSessionResult(false, null, error); }
    public boolean isSuccess() { return success; }
    public @Nullable MemoryReasoningSession getSession() { return session; }
    public @Nullable String getError() { return error; }
}


