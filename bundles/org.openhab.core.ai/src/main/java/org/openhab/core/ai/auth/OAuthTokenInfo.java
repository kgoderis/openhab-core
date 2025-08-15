package org.openhab.core.ai.auth;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
class OAuthTokenInfo {
    private final String subject;
    private final String clientId;
    private final String issuer;
    private final Instant expiresAt;
    private final Set<String> scopes;

    OAuthTokenInfo(String subject, String clientId, String issuer, Instant expiresAt, Set<String> scopes) {
        this.subject = subject;
        this.clientId = clientId;
        this.issuer = issuer;
        this.expiresAt = expiresAt;
        this.scopes = scopes;
    }

    String getSubject() {
        return subject;
    }

    String getClientId() {
        return clientId;
    }

    String getIssuer() {
        return issuer;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }

    Set<String> getScopes() {
        return scopes;
    }
}
