package org.openhab.core.ai.common.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener interface for authentication events in AI protocols.
 * 
 * This interface allows components to be notified of authentication
 * events such as successful logins, failures, and logouts.
 * 
 * 
 */
@NonNullByDefault
public interface AIAuthenticationListener {

    /**
     * Called when authentication succeeds.
     * 
     * @param context Authentication context
     * @param protocol Protocol name
     * @param clientId Client identifier
     */
    void onAuthenticationSuccess(AIAuthenticationContext context, String protocol, String clientId);

    /**
     * Called when authentication fails.
     * 
     * @param clientId Client identifier
     * @param protocol Protocol name
     * @param reason Failure reason
     */
    void onAuthenticationFailure(String clientId, String protocol, String reason);

    /**
     * Called when a user logs out.
     * 
     * @param context Authentication context that was logged out
     */
    void onLogout(AIAuthenticationContext context);

    /**
     * Get the listener name for identification.
     * 
     * @return Listener name
     */
    default String getListenerName() {
        return this.getClass().getSimpleName();
    }
}
