package org.openhab.core.ai.common.llm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.api.llm.LLMProviderType;

/**
 * Information about an LLM provider and its capabilities.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LLMProviderInfo {

    private final LLMProviderType providerType;
    private final String modelName;
    private final boolean supportsFunctionCalling;
    private final boolean supportsStreaming;
    private final boolean supportsMultimodal;
    private final int maxTokens;
    private final double costPer1kTokens;

    public LLMProviderInfo(LLMProviderType providerType, String modelName, boolean supportsFunctionCalling,
            boolean supportsStreaming, boolean supportsMultimodal, int maxTokens, double costPer1kTokens) {
        this.providerType = providerType;
        this.modelName = modelName;
        this.supportsFunctionCalling = supportsFunctionCalling;
        this.supportsStreaming = supportsStreaming;
        this.supportsMultimodal = supportsMultimodal;
        this.maxTokens = maxTokens;
        this.costPer1kTokens = costPer1kTokens;
    }

    /**
     * Gets the provider type.
     * 
     * @return The provider type
     */
    public LLMProviderType getProviderType() {
        return providerType;
    }

    /**
     * Gets the model name.
     * 
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Checks if function calling is supported.
     * 
     * @return true if function calling is supported
     */
    public boolean supportsFunctionCalling() {
        return supportsFunctionCalling;
    }

    /**
     * Checks if streaming is supported.
     * 
     * @return true if streaming is supported
     */
    public boolean supportsStreaming() {
        return supportsStreaming;
    }

    /**
     * Checks if multimodal input is supported.
     * 
     * @return true if multimodal input is supported
     */
    public boolean supportsMultimodal() {
        return supportsMultimodal;
    }

    /**
     * Gets the maximum number of tokens supported.
     * 
     * @return Maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Gets the cost per 1K tokens.
     * 
     * @return Cost per 1K tokens
     */
    public double getCostPer1kTokens() {
        return costPer1kTokens;
    }

    @Override
    public String toString() {
        return "LLMProviderInfo{providerType=" + providerType + ", modelName='" + modelName
                + "', supportsFunctionCalling=" + supportsFunctionCalling + ", supportsStreaming=" + supportsStreaming
                + ", supportsMultimodal=" + supportsMultimodal + ", maxTokens=" + maxTokens + ", costPer1kTokens="
                + costPer1kTokens + "}";
    }
}
