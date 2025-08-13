package org.openhab.core.ai.agent.infrastructure.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record ConfigurationValidationResult(boolean isValid, List<String> errors, List<String> warnings) {}


