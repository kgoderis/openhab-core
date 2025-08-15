package org.openhab.core.ai.tool.prompts;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt Registration Service for MCP Prompts
 *
 * This service manages programmatic registration of Prompt objects
 * into the PromptRegistry. It can be extended to auto-discover prompt
 * providers via OSGi if/when such services are introduced.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class PromptRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(PromptRegistrationService.class);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable PromptRegistry promptRegistry;

    @Activate
    protected void activate() {
        logger.debug("Activating PromptRegistrationService");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating PromptRegistrationService");
    }

    /** Register a prompt in the registry. */
    public boolean registerPrompt(Prompt prompt) {
        PromptRegistry registry = promptRegistry;
        if (registry == null) {
            logger.warn("PromptRegistry not available, cannot register prompt: {}", prompt.getName());
            return false;
        }
        registry.registerPrompt(prompt);
        logger.debug("Registered prompt: {}", prompt.getName());
        return true;
    }

    /** Unregister a prompt by name. */
    public boolean unregisterPrompt(String name) {
        PromptRegistry registry = promptRegistry;
        if (registry == null) {
            logger.warn("PromptRegistry not available, cannot unregister prompt: {}", name);
            return false;
        }
        registry.unregisterPrompt(name);
        logger.debug("Unregistered prompt: {}", name);
        return true;
    }

    /** Get all registered prompts. */
    public Map<String, Prompt> getAllPrompts() {
        PromptRegistry registry = promptRegistry;
        return registry != null ? registry.getAllPrompts() : java.util.Map.of();
    }
}
