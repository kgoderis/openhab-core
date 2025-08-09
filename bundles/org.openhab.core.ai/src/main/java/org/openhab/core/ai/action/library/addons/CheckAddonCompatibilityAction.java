package org.openhab.core.ai.action.library.addons;

import java.time.Instant;
import java.util.ArrayList;
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
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for checking openHAB addon compatibility through real OSGi bundle compatibility analysis.
 * 
 * This action provides comprehensive compatibility checking including version compatibility,
 * dependency resolution, and bundle wiring analysis using real OSGi bundle infrastructure.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class CheckAddonCompatibilityAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(CheckAddonCompatibilityAction.class);
    private static final String ACTION_ID = "openhab.addons.compatibility";
    private static final String ACTION_NAME = "Check Addon Compatibility";
    private static final String DESCRIPTION = "Checks openHAB addon compatibility using real OSGi bundle compatibility analysis";
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
        properties.put("action",
                Map.of("type", "string", "enum",
                        List.of("check_compatibility", "check_version_compatibility", "check_dependency_compatibility",
                                "check_environment_compatibility"),
                        "description", "Action to perform for compatibility checking"));
        properties.put("addonId", Map.of("type", "string", "description", "Addon ID to check compatibility for"));
        properties.put("targetVersion",
                Map.of("type", "string", "description", "Target version to check compatibility against"));
        properties.put("includeDetailedAnalysis",
                Map.of("type", "boolean", "description", "Include detailed compatibility analysis", "default", true));
        properties.put("checkDependencies",
                Map.of("type", "boolean", "description", "Check dependency compatibility", "default", true));
        properties.put("checkConflicts",
                Map.of("type", "boolean", "description", "Check for potential conflicts", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            if (!parameters.containsKey("action")) {
                return ActionValidationResult.invalid(List.of("Missing required parameter: action"));
            }

            String action = (String) parameters.get("action");
            if (action == null || action.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("action cannot be null or empty"));
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
        properties.put("compatible", Map.of("type", "boolean", "description", "Whether the addon is compatible"));
        properties.put("compatibilityScore", Map.of("type", "number", "description", "Compatibility score (0-100)"));
        properties.put("issues", Map.of("type", "array", "description", "List of compatibility issues"));
        properties.put("warnings", Map.of("type", "array", "description", "List of compatibility warnings"));
        properties.put("recommendations", Map.of("type", "array", "description", "List of recommendations"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            logger.debug("Executing CheckAddonCompatibilityAction with parameters: {}", parameters);

            long startTime = System.currentTimeMillis();
            Map<String, Object> result = checkCompatibility(parameters);
            long executionTime = System.currentTimeMillis() - startTime;

            return ActionResult.success(result, executionTime);
        } catch (Exception e) {
            logger.error("Error executing CheckAddonCompatibilityAction", e);
            throw new ActionException(ACTION_ID, "Failed to check addon compatibility: " + e.getMessage(), e);
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
        capabilities.put("maxConcurrentExecutions", 5);
        capabilities.put("timeout", 60000);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing CheckAddonCompatibilityAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up CheckAddonCompatibilityAction");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> checkCompatibility(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", Instant.now().toString());

        String action = (String) parameters.get("action");
        String addonId = (String) parameters.get("addonId");
        String targetVersion = (String) parameters.get("targetVersion");
        boolean includeDetailedAnalysis = (Boolean) parameters.getOrDefault("includeDetailedAnalysis", true);
        boolean checkDependencies = (Boolean) parameters.getOrDefault("checkDependencies", true);
        boolean checkConflicts = (Boolean) parameters.getOrDefault("checkConflicts", true);

        if (bundleContext == null) {
            result.put("error", "BundleContext not available");
            return result;
        }

        try {
            switch (action) {
                case "check_compatibility":
                    result = performComprehensiveCompatibilityCheck(addonId, targetVersion, includeDetailedAnalysis,
                            checkDependencies, checkConflicts);
                    break;
                case "check_version_compatibility":
                    result = checkVersionCompatibility(addonId, targetVersion, includeDetailedAnalysis);
                    break;
                case "check_dependency_compatibility":
                    result = checkDependencyCompatibility(addonId, includeDetailedAnalysis);
                    break;
                case "check_environment_compatibility":
                    result = checkEnvironmentCompatibility(addonId, includeDetailedAnalysis);
                    break;
                default:
                    result.put("error", "Unknown action: " + action);
                    return result;
            }

        } catch (Exception e) {
            logger.error("Error checking compatibility for: {}", addonId, e);
            result.put("error", "Failed to check compatibility: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> performComprehensiveCompatibilityCheck(String addonId, String targetVersion,
            boolean includeDetailedAnalysis, boolean checkDependencies, boolean checkConflicts) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("timestamp", Instant.now().toString());

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int compatibilityScore = 100;

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("compatible", false);
            result.put("compatibilityScore", 0);
            result.put("issues", List.of("Addon not found: " + addonId));
            return result;
        }

        // Check version compatibility
        if (targetVersion != null && !targetVersion.trim().isEmpty()) {
            Map<String, Object> versionCheck = checkVersionCompatibility(addonId, targetVersion,
                    includeDetailedAnalysis);
            if (!(Boolean) versionCheck.get("compatible")) {
                issues.addAll((List<String>) versionCheck.get("issues"));
                compatibilityScore -= 30;
            }
            warnings.addAll((List<String>) versionCheck.get("warnings"));
        }

        // Check dependency compatibility
        if (checkDependencies) {
            Map<String, Object> dependencyCheck = checkDependencyCompatibility(addonId, includeDetailedAnalysis);
            if (!(Boolean) dependencyCheck.get("compatible")) {
                issues.addAll((List<String>) dependencyCheck.get("issues"));
                compatibilityScore -= 40;
            }
            warnings.addAll((List<String>) dependencyCheck.get("warnings"));
        }

        // Check for conflicts
        if (checkConflicts) {
            Map<String, Object> conflictCheck = checkForConflicts(bundle, includeDetailedAnalysis);
            if (!(Boolean) conflictCheck.get("compatible")) {
                issues.addAll((List<String>) conflictCheck.get("issues"));
                compatibilityScore -= 30;
            }
            warnings.addAll((List<String>) conflictCheck.get("warnings"));
        }

        // Generate recommendations
        if (compatibilityScore < 100) {
            if (compatibilityScore < 50) {
                recommendations.add("Consider using a different version or alternative addon");
            } else if (compatibilityScore < 80) {
                recommendations.add("Review compatibility issues before installation");
            }
        } else {
            recommendations.add("Addon appears to be fully compatible");
        }

        result.put("compatible", compatibilityScore >= 70);
        result.put("compatibilityScore", Math.max(0, compatibilityScore));
        result.put("issues", issues);
        result.put("warnings", warnings);
        result.put("recommendations", recommendations);

        return result;
    }

    private Map<String, Object> checkVersionCompatibility(String addonId, String targetVersion,
            boolean includeDetailedAnalysis) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("compatible", false);
            result.put("issues", List.of("Addon not found: " + addonId));
            return result;
        }

        try {
            Version currentVersion = bundle.getVersion();
            Version target = Version.parseVersion(targetVersion);

            if (includeDetailedAnalysis) {
                result.put("currentVersion", currentVersion.toString());
                result.put("targetVersion", target.toString());
            }

            // Basic version comparison
            int comparison = currentVersion.compareTo(target);
            if (comparison < 0) {
                issues.add("Current version " + currentVersion + " is older than target version " + target);
            } else if (comparison > 0) {
                warnings.add("Current version " + currentVersion + " is newer than target version " + target);
            }

            result.put("compatible", issues.isEmpty());
            result.put("issues", issues);
            result.put("warnings", warnings);

        } catch (Exception e) {
            issues.add("Failed to parse version: " + e.getMessage());
            result.put("compatible", false);
            result.put("issues", issues);
        }

        return result;
    }

    private Map<String, Object> checkDependencyCompatibility(String addonId, boolean includeDetailedAnalysis) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("compatible", false);
            result.put("issues", List.of("Addon not found: " + addonId));
            return result;
        }

        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring == null) {
            issues.add("Cannot analyze bundle wiring");
            result.put("compatible", false);
            result.put("issues", issues);
            return result;
        }

        // Check package imports
        for (BundleRequirement requirement : wiring.getRequirements(null)) {
            if ("osgi.wiring.package".equals(requirement.getNamespace())) {
                String packageName = (String) requirement.getAttributes().get("osgi.wiring.package");
                VersionRange versionRange = (VersionRange) requirement.getAttributes().get("version");

                if (includeDetailedAnalysis) {
                    // Check if package is available
                    boolean resolved = requirement.getResource().getBundle().getState() == Bundle.ACTIVE;
                    if (!resolved) {
                        issues.add("Package dependency not resolved: " + packageName);
                    }
                }
            }
        }

        result.put("compatible", issues.isEmpty());
        result.put("issues", issues);
        result.put("warnings", warnings);

        return result;
    }

    private Map<String, Object> checkEnvironmentCompatibility(String addonId, boolean includeDetailedAnalysis) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("compatible", false);
            result.put("issues", List.of("Addon not found: " + addonId));
            return result;
        }

        // Check bundle state
        int state = bundle.getState();
        if (state != Bundle.ACTIVE && state != Bundle.RESOLVED) {
            issues.add("Bundle is not in a compatible state: " + getBundleStateString(state));
        }

        // Check for required capabilities
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            for (BundleCapability capability : wiring.getCapabilities(null)) {
                if ("osgi.service".equals(capability.getNamespace())) {
                    String serviceName = (String) capability.getAttributes().get("objectClass");
                    if (includeDetailedAnalysis) {
                        warnings.add("Requires service: " + serviceName);
                    }
                }
            }
        }

        result.put("compatible", issues.isEmpty());
        result.put("issues", issues);
        result.put("warnings", warnings);

        return result;
    }

    private Map<String, Object> checkForConflicts(Bundle bundle, boolean includeDetailedAnalysis) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check for duplicate symbolic names
        String symbolicName = bundle.getSymbolicName();
        Bundle[] bundles = bundleContext.getBundles();

        int duplicateCount = 0;
        for (Bundle otherBundle : bundles) {
            if (otherBundle != bundle && symbolicName.equals(otherBundle.getSymbolicName())) {
                duplicateCount++;
                if (includeDetailedAnalysis) {
                    warnings.add("Duplicate bundle found: " + otherBundle.getBundleId() + " (version: "
                            + otherBundle.getVersion() + ")");
                }
            }
        }

        if (duplicateCount > 0) {
            issues.add("Found " + duplicateCount + " duplicate bundle(s) with same symbolic name");
        }

        result.put("compatible", issues.isEmpty());
        result.put("issues", issues);
        result.put("warnings", warnings);

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
}
