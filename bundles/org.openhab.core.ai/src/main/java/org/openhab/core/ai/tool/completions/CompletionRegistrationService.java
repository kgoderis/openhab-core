package org.openhab.core.ai.tool.completions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.api.CompletionRegistry;
import org.openhab.core.ai.tool.completions.api.dto.Completion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion Registration Service for MCP Completions
 *
 * This service manages programmatic registration of Completion objects
 * into the CompletionRegistry. It can be extended to auto-discover completion
 * providers via OSGi if/when such services are introduced.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class CompletionRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(CompletionRegistrationService.class);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable CompletionRegistry completionRegistry;

    @Activate
    protected void activate() {
        logger.debug("Activating CompletionRegistrationService");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating CompletionRegistrationService");
    }

    /** Register a completion in the registry. */
    public boolean registerCompletion(Completion completion) {
        CompletionRegistry registry = completionRegistry;
        if (registry == null) {
            logger.warn("CompletionRegistry not available, cannot register completion: {}",
                    completion.getPromptReference());
            return false;
        }
        registry.registerCompletion(completion);
        logger.debug("Registered completion: {}", completion.getPromptReference());
        return true;
    }

    /** Unregister a completion by prompt reference. */
    public boolean unregisterCompletion(String promptReference) {
        CompletionRegistry registry = completionRegistry;
        if (registry == null) {
            logger.warn("CompletionRegistry not available, cannot unregister completion: {}", promptReference);
            return false;
        }
        registry.unregisterCompletion(promptReference);
        logger.debug("Unregistered completion: {}", promptReference);
        return true;
    }

    /** Get all registered completions. */
    public Map<String, Completion> getAllCompletions() {
        CompletionRegistry registry = completionRegistry;
        return registry != null ? registry.getAllCompletions() : java.util.Map.of();
    }
}
