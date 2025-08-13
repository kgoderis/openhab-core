package org.openhab.core.ai.agent.infrastructure.config;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record ConfigurationVersion(String configId, String version, Instant timestamp, String description,
        CommunicationConfig config) {}


