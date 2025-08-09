package org.openhab.core.ai.action.library.addons;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for retrieving basic openHAB addon information from OSGi bundles.
 * 
 * This action provides fundamental addon information including metadata, state,
 * and basic properties using real OSGi bundle analysis.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class GetAddonAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetAddonAction.class);
    private static final String ACTION_ID = "openhab.addons.get";
    private static final String ACTION_NAME = "Get Addon";
    private static final String DESCRIPTION = "Retrieves basic openHAB addon information from OSGi bundles";
    private static final String CATEGORY = "addons";
    private static final String VERSION = "1.0.0";

    @Reference
    private @Nullable BundleContext bundleContext;

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("addonId",
                Map.of("type", "string", "description", "Addon ID (bundle symbolic name or bundle ID)"));
        properties.put("includeBasicInfo",
                Map.of("type", "boolean", "description", "Include basic addon information", "default", true));
        properties.put("includeState",
                Map.of("type", "boolean", "description", "Include addon state information", "default", true));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include addon metadata", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("addonId"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            if (!parameters.containsKey("addonId")) {
                return ActionValidationResult.invalid(List.of("Missing required parameter: addonId"));
            }

            String addonId = (String) parameters.get("addonId");
            if (addonId == null || addonId.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("addonId cannot be null or empty"));
            }

            return ActionValidationResult.valid(parameters);
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("addonId", Map.of("type", "string", "description", "The addon ID that was queried"));
        properties.put("found", Map.of("type", "boolean", "description", "Whether the addon was found"));
        properties.put("addonInfo", Map.of("type", "object", "description", "Basic addon information"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            logger.debug("Executing GetAddonAction with parameters: {}", parameters);

            long startTime = System.currentTimeMillis();
            Map<String, Object> result = getAddon(parameters);
            long executionTime = System.currentTimeMillis() - startTime;

            return ActionResult.success(result, executionTime);
        } catch (Exception e) {
            logger.error("Error executing GetAddonAction", e);
            throw new ActionException(ACTION_ID, "Failed to get addon information: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(VERSION).description(DESCRIPTION).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("maxConcurrentExecutions", 10);
        capabilities.put("timeout", 30000);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing GetAddonAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetAddonAction");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> getAddon(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", Instant.now().toString());

        String addonId = (String) parameters.get("addonId");
        boolean includeBasicInfo = (Boolean) parameters.getOrDefault("includeBasicInfo", true);
        boolean includeState = (Boolean) parameters.getOrDefault("includeState", true);
        boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", false);

        result.put("addonId", addonId);

        if (bundleContext == null) {
            result.put("error", "BundleContext not available");
            return result;
        }

        try {
            Bundle bundle = findBundle(addonId);

            if (bundle == null) {
                result.put("found", false);
                result.put("error", "Addon not found: " + addonId);
                return result;
            }

            result.put("found", true);
            result.put("success", true);

            Map<String, Object> addonInfo = new HashMap<>();

            if (includeBasicInfo) {
                addonInfo.put("bundleId", bundle.getBundleId());
                addonInfo.put("symbolicName", bundle.getSymbolicName());
                addonInfo.put("version", bundle.getVersion().toString());
                addonInfo.put("location", bundle.getLocation());
                addonInfo.put("lastModified", bundle.getLastModified());
                addonInfo.put("addonType", determineAddonType(bundle.getSymbolicName()));
            }

            if (includeState) {
                addonInfo.put("state", getBundleStateString(bundle.getState()));
                addonInfo.put("stateCode", bundle.getState());
            }

            if (includeMetadata) {
                addonInfo.put("vendor", getBundleHeader(bundle, Constants.BUNDLE_VENDOR));
                addonInfo.put("description", getBundleHeader(bundle, Constants.BUNDLE_DESCRIPTION));
                addonInfo.put("copyright", getBundleHeader(bundle, Constants.BUNDLE_COPYRIGHT));
                addonInfo.put("docURL", getBundleHeader(bundle, Constants.BUNDLE_DOCURL));
                addonInfo.put("contactAddress", getBundleHeader(bundle, Constants.BUNDLE_CONTACTADDRESS));
            }

            result.put("addonInfo", addonInfo);

        } catch (Exception e) {
            logger.error("Error getting addon information for: {}", addonId, e);
            result.put("error", "Failed to get addon information: " + e.getMessage());
        }

        return result;
    }

    private @Nullable Bundle findBundle(String addonId) {
        if (bundleContext == null) {
            return null;
        }

        Bundle[] bundles = bundleContext.getBundles();

        // Try to find by bundle ID first (if addonId is numeric)
        try {
            long bundleId = Long.parseLong(addonId);
            for (Bundle bundle : bundles) {
                if (bundle.getBundleId() == bundleId) {
                    return bundle;
                }
            }
        } catch (NumberFormatException e) {
            // Not a numeric ID, continue with symbolic name search
        }

        // Try to find by symbolic name
        for (Bundle bundle : bundles) {
            if (addonId.equals(bundle.getSymbolicName())) {
                return bundle;
            }
        }

        return null;
    }

    private String determineAddonType(String symbolicName) {
        if (symbolicName == null) {
            return "unknown";
        }

        String lowerName = symbolicName.toLowerCase();

        if (lowerName.contains("binding")) {
            return "binding";
        } else if (lowerName.contains("transformation")) {
            return "transformation";
        } else if (lowerName.contains("persistence")) {
            return "persistence";
        } else if (lowerName.contains("voice")) {
            return "voice";
        } else if (lowerName.contains("ui")) {
            return "ui";
        } else if (lowerName.contains("core")) {
            return "core";
        } else {
            return "other";
        }
    }

    private String getBundleStateString(int state) {
        switch (state) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }

    private String getBundleHeader(Bundle bundle, String headerName) {
        try {
            String value = bundle.getHeaders().get(headerName);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }
}
