package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Backup snapshot for a context.
 *
 * Stores the context, its versions and permissions at a point in time.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextBackup {

    private final String backupId;
    private final String contextId;
    private final Instant backupTime;
    private final Map<String, Object> backupData;

    public ContextBackup(String backupId, String contextId, Instant backupTime, Map<String, Object> backupData) {
        this.backupId = backupId;
        this.contextId = contextId;
        this.backupTime = backupTime;
        this.backupData = new ConcurrentHashMap<>(backupData);
    }

    public String getBackupId() {
        return backupId;
    }

    public String getContextId() {
        return contextId;
    }

    public Instant getBackupTime() {
        return backupTime;
    }

    public Map<String, Object> getBackupData() {
        return Map.copyOf(backupData);
    }

    public SharedContext getContext() {
        return (SharedContext) backupData.get("context");
    }

    @SuppressWarnings("unchecked")
    public List<ContextVersion> getVersions() {
        return (List<ContextVersion>) backupData.get("versions");
    }

    @SuppressWarnings("unchecked")
    public Map<String, ContextPermission> getPermissions() {
        return (Map<String, ContextPermission>) backupData.get("permissions");
    }

    @Override
    public String toString() {
        return String.format("ContextBackup{backupId='%s', contextId='%s', backupTime=%s, dataSize=%d}", backupId,
                contextId, backupTime, backupData.size());
    }
}


