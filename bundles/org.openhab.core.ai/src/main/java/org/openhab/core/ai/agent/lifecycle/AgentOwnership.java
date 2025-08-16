package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents ownership information for an agent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentOwnership {
    private final String ownerId;
    private final String ownerType;
    private final long ownershipStartTime;
    private final @Nullable String ownershipReason;

    public AgentOwnership(String ownerId, String ownerType, long ownershipStartTime, @Nullable String ownershipReason) {
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.ownershipStartTime = ownershipStartTime;
        this.ownershipReason = ownershipReason;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public long getOwnershipStartTime() {
        return ownershipStartTime;
    }

    public @Nullable String getOwnershipReason() {
        return ownershipReason;
    }
}
