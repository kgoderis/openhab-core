package org.openhab.core.ai.agent.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentOwnershipResolverOwnershipValidationResult {
    boolean isValid();
    String getMessage();
    List<String> getErrors();
    List<String> getWarnings();
}


