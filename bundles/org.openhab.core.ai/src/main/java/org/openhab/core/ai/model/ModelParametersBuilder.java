package org.openhab.core.ai.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for {@link ModelParameters}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ModelParametersBuilder {
    double temperature = 0.7;
    int maxTokens = 1000;
    @Nullable String model;
    Map<String, Object> additionalParams = Map.of();
    boolean stream = false;
    @Nullable String systemPrompt;
    int timeoutMs = 30000;

    public ModelParametersBuilder temperature(double temperature) { this.temperature = temperature; return this; }
    public ModelParametersBuilder maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
    public ModelParametersBuilder model(@Nullable String model) { this.model = model; return this; }
    public ModelParametersBuilder additionalParams(Map<String, Object> additionalParams) { this.additionalParams = additionalParams; return this; }
    public ModelParametersBuilder stream(boolean stream) { this.stream = stream; return this; }
    public ModelParametersBuilder systemPrompt(@Nullable String systemPrompt) { this.systemPrompt = systemPrompt; return this; }
    public ModelParametersBuilder timeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }

    public ModelParameters build() { return new ModelParameters(this); }
}


