package org.openhab.core.ai.agent.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface OwnershipTestResult {
    boolean isSuccessful();
    String getMessage();
    long getResolutionTime();
    Map<String, Object> getTestData();
}


