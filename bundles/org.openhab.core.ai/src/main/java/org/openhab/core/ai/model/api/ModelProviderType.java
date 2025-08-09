package org.openhab.core.ai.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of supported LLM provider types.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public enum ModelProviderType {

    /** OpenAI provider (GPT models) */
    OPENAI("OpenAI", "openai"),

    /** Anthropic provider (Claude models) */
    ANTHROPIC("Anthropic", "anthropic"),

    /** Google GenAI provider (Gemini models) */
    GOOGLE("Google GenAI", "google"),

    /** Azure OpenAI provider */
    AZURE("Azure OpenAI", "azure"),

    /** Ollama local provider */
    OLLAMA("Ollama", "ollama"),

    /** LocalAI provider */
    LOCALAI("LocalAI", "localai"),

    /** vLLM provider */
    VLLM("vLLM", "vllm"),

    /** LM Studio provider */
    LMSTUDIO("LM Studio", "lmstudio");

    private final String displayName;
    private final String configKey;

    ModelProviderType(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    /**
     * Gets the display name for this provider type.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the configuration key for this provider type.
     * 
     * @return The configuration key
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Checks if this is a cloud-based provider.
     * 
     * @return true if cloud-based
     */
    public boolean isCloudProvider() {
        return this == OPENAI || this == ANTHROPIC || this == GOOGLE || this == AZURE;
    }

    /**
     * Checks if this is a local provider.
     * 
     * @return true if local
     */
    public boolean isLocalProvider() {
        return this == OLLAMA || this == LOCALAI || this == VLLM || this == LMSTUDIO;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
