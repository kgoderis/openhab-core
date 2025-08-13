package org.openhab.core.ai.agent.execution;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;

    public enum ExecutionStrategyType {
        SKILL, // Execute as skill via AgentSkillManager
        ACTION, // Execute as action directly
        COMPOSED // Execute as composed action/skill
    }