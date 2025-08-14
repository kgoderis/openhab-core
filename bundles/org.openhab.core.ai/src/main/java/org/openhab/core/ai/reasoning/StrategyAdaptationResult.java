package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class StrategyAdaptationResult {
    private final boolean success;
    private final String message;
    private final boolean adapted;
    private final int adaptationCount;

    private StrategyAdaptationResult(boolean success, String message, boolean adapted, int adaptationCount) {
        this.success = success;
        this.message = message;
        this.adapted = adapted;
        this.adaptationCount = adaptationCount;
    }

    public static StrategyAdaptationResult success(boolean adapted, int adaptationCount) {
        return new StrategyAdaptationResult(true, "Strategy adaptation successful", adapted, adaptationCount);
    }

    public static StrategyAdaptationResult disabled(String reason) {
        return new StrategyAdaptationResult(false, "Strategy adaptation disabled: " + reason, false, 0);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isAdapted() { return adapted; }
    public int getAdaptationCount() { return adaptationCount; }
}


