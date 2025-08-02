package org.openhab.core.ai.common.api.llm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown when an LLM operation fails.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LLMException extends Exception {

    private static final long serialVersionUID = 1L;

    private final LLMProviderType providerType;
    private final String modelName;
    private final @Nullable String errorCode;

    public LLMException(String message, LLMProviderType providerType, String modelName) {
        super(message);
        this.providerType = providerType;
        this.modelName = modelName;
        this.errorCode = null;
    }

    public LLMException(String message, LLMProviderType providerType, String modelName, String errorCode) {
        super(message);
        this.providerType = providerType;
        this.modelName = modelName;
        this.errorCode = errorCode;
    }

    public LLMException(String message, LLMProviderType providerType, String modelName, Throwable cause) {
        super(message, cause);
        this.providerType = providerType;
        this.modelName = modelName;
        this.errorCode = null;
    }

    public LLMException(String message, LLMProviderType providerType, String modelName, String errorCode,
            Throwable cause) {
        super(message, cause);
        this.providerType = providerType;
        this.modelName = modelName;
        this.errorCode = errorCode;
    }

    /**
     * Gets the provider type that caused the error.
     * 
     * @return The provider type
     */
    public LLMProviderType getProviderType() {
        return providerType;
    }

    /**
     * Gets the model name that was being used.
     * 
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the error code if available.
     * 
     * @return The error code, or null if not available
     */
    public @Nullable String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "LLMException{providerType=" + providerType + ", modelName='" + modelName + "', errorCode='" + errorCode
                + "', message='" + getMessage() + "'}";
    }
}
