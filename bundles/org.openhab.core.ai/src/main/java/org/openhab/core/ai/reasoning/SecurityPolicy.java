package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security policy for model access.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityPolicy {
    private final String policyId;
    private final String name;
    private final SecurityLevel level;
    private final Map<String, Object> rules;
    private final boolean enabled;

    public SecurityPolicy(String policyId, String name, SecurityLevel level, Map<String, Object> rules,
            boolean enabled) {
        this.policyId = policyId;
        this.name = name;
        this.level = level;
        this.rules = new ConcurrentHashMap<>(rules);
        this.enabled = enabled;
    }

    public String getPolicyId() {
        return policyId;
    }

    public String getName() {
        return name;
    }

    public SecurityLevel getLevel() {
        return level;
    }

    public Map<String, Object> getRules() {
        return new ConcurrentHashMap<>(rules);
    }

    public boolean isEnabled() {
        return enabled;
    }
}


