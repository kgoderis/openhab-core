package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of a context retrieval operation.
 */
@NonNullByDefault
public interface ContextRetrievalResult {
    boolean isSuccess();

    String getMessage();

    @Nullable
    SharedContext getContext();

    @Nullable
    ContextVersion getVersion();

    static ContextRetrievalResult success(SharedContext context, ContextVersion version) {
        return new ContextRetrievalResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Context retrieved successfully";
            }

            @Override
            public SharedContext getContext() {
                return context;
            }

            @Override
            public ContextVersion getVersion() {
                return version;
            }
        };
    }

    static ContextRetrievalResult failure(String message) {
        return new ContextRetrievalResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public SharedContext getContext() {
                return null;
            }

            @Override
            public ContextVersion getVersion() {
                return null;
            }
        };
    }

    static ContextRetrievalResult permissionDenied(String message) {
        return new ContextRetrievalResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Permission denied: " + message;
            }

            @Override
            public SharedContext getContext() {
                return null;
            }

            @Override
            public ContextVersion getVersion() {
                return null;
            }
        };
    }

    static ContextRetrievalResult notFound(String message) {
        return new ContextRetrievalResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Not found: " + message;
            }

            @Override
            public SharedContext getContext() {
                return null;
            }

            @Override
            public ContextVersion getVersion() {
                return null;
            }
        };
    }
}
