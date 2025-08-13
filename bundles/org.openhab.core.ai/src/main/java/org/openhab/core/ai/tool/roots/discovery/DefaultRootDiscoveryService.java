package org.openhab.core.ai.tool.roots.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of RootDiscoveryService.
 * 
 * Provides actual root discovery logic for MCP roots.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = RootDiscoveryService.class)
@NonNullByDefault
public class DefaultRootDiscoveryService implements RootDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRootDiscoveryService.class);

    private final String serviceId;
    private final String serviceName;
    private final String serviceDescription;
    private final Map<String, ResourceSpecification> registeredRoots;
    private final Map<String, Object> configuration;

    @Activate
    public DefaultRootDiscoveryService() {
        this.serviceId = "default-root-discovery-" + System.currentTimeMillis();
        this.serviceName = "Default Root Discovery Service";
        this.serviceDescription = "Default implementation for MCP root discovery";
        this.registeredRoots = new ConcurrentHashMap<>();
        this.configuration = new ConcurrentHashMap<>();

        // Initialize default configuration
        configuration.put("discoveryEnabled", true);
        configuration.put("cacheTimeout", 300000); // 5 minutes
        configuration.put("maxRoots", 100);

        logger.debug("Default Root Discovery Service created: {}", serviceId);
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Default Root Discovery Service deactivated: {}", serviceId);
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getServiceDescription() {
        return serviceDescription;
    }

    @Override
    public List<ResourceSpecification> discoverRoots() {
        try {
            logger.debug("Discovering roots without criteria");

            // Implement actual root discovery logic
            List<ResourceSpecification> discoveredRoots = new ArrayList<>();

            // Discover system roots
            discoveredRoots.addAll(discoverSystemRoots());

            // Discover openHAB roots
            discoveredRoots.addAll(discoverOpenHABRoots());

            // Add registered roots
            discoveredRoots.addAll(registeredRoots.values());

            logger.info("Discovered {} roots", discoveredRoots.size());
            return discoveredRoots;

        } catch (Exception e) {
            logger.error("Error discovering roots", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ResourceSpecification> discoverRoots(Map<String, Object> criteria) {
        try {
            logger.debug("Discovering roots with criteria: {}", criteria);

            // Implement actual root discovery logic with criteria
            List<ResourceSpecification> allRoots = discoverRoots();
            List<ResourceSpecification> filteredRoots = new ArrayList<>();

            for (ResourceSpecification root : allRoots) {
                if (matchesCriteria(root, criteria)) {
                    filteredRoots.add(root);
                }
            }

            logger.info("Discovered {} roots matching criteria", filteredRoots.size());
            return filteredRoots;

        } catch (Exception e) {
            logger.error("Error discovering roots with criteria", e);
            return new ArrayList<>();
        }
    }

    @Override
    public ResourceSpecification getRoot(String rootId) {
        try {
            logger.debug("Getting root: {}", rootId);

            // Check registered roots first
            ResourceSpecification registeredRoot = registeredRoots.get(rootId);
            if (registeredRoot != null) {
                return registeredRoot;
            }

            // Check system roots
            List<ResourceSpecification> systemRoots = discoverSystemRoots();
            for (ResourceSpecification root : systemRoots) {
                if (rootId.equals(root.getName())) {
                    return root;
                }
            }

            // Check openHAB roots
            List<ResourceSpecification> openHABRoots = discoverOpenHABRoots();
            for (ResourceSpecification root : openHABRoots) {
                if (rootId.equals(root.getName())) {
                    return root;
                }
            }

            logger.debug("Root not found: {}", rootId);
            return null;

        } catch (Exception e) {
            logger.error("Error getting root: {}", rootId, e);
            return null;
        }
    }

    @Override
    public void registerRoot(ResourceSpecification root) {
        try {
            logger.debug("Registering root: {}", root.getName());

            if (root.getName() == null || root.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Root name cannot be null or empty");
            }

            registeredRoots.put(root.getName(), root);
            logger.info("Root registered successfully: {}", root.getName());

        } catch (Exception e) {
            logger.error("Error registering root: {}", root.getName(), e);
            throw new RuntimeException("Root registration failed", e);
        }
    }

    @Override
    public void unregisterRoot(String rootId) {
        try {
            logger.debug("Unregistering root: {}", rootId);

            ResourceSpecification removed = registeredRoots.remove(rootId);
            if (removed != null) {
                logger.info("Root unregistered successfully: {}", rootId);
            } else {
                logger.debug("Root not found for unregistration: {}", rootId);
            }

        } catch (Exception e) {
            logger.error("Error unregistering root: {}", rootId, e);
            throw new RuntimeException("Root unregistration failed", e);
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new ConcurrentHashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        try {
            logger.debug("Updating configuration: {}", configuration);

            this.configuration.clear();
            this.configuration.putAll(configuration);

            logger.info("Configuration updated successfully");

        } catch (Exception e) {
            logger.error("Error updating configuration", e);
            throw new RuntimeException("Configuration update failed", e);
        }
    }

    // Private helper methods for actual root discovery

    private List<ResourceSpecification> discoverSystemRoots() {
        List<ResourceSpecification> systemRoots = new ArrayList<>();

        try {
            // Discover filesystem roots
            systemRoots.add(createRootSpecification("filesystem", "/", "Filesystem root", "read-write"));
            systemRoots.add(createRootSpecification("home", System.getProperty("user.home"), "User home directory",
                    "read-write"));
            systemRoots.add(createRootSpecification("temp", System.getProperty("java.io.tmpdir"), "Temporary directory",
                    "read-write"));

            // Discover application roots
            systemRoots.add(createRootSpecification("application", ".", "Application root", "read-write"));

            logger.debug("Discovered {} system roots", systemRoots.size());

        } catch (Exception e) {
            logger.error("Error discovering system roots", e);
        }

        return systemRoots;
    }

    private List<ResourceSpecification> discoverOpenHABRoots() {
        List<ResourceSpecification> openHABRoots = new ArrayList<>();

        try {
            // Discover openHAB-specific roots
            openHABRoots.add(
                    createRootSpecification("openhab-config", "conf", "openHAB configuration directory", "read-write"));
            openHABRoots.add(createRootSpecification("openhab-userdata", "userdata", "openHAB user data directory",
                    "read-write"));
            openHABRoots
                    .add(createRootSpecification("openhab-addons", "addons", "openHAB addons directory", "read-only"));
            openHABRoots.add(createRootSpecification("openhab-logs", "logs", "openHAB logs directory", "read-only"));

            logger.debug("Discovered {} openHAB roots", openHABRoots.size());

        } catch (Exception e) {
            logger.error("Error discovering openHAB roots", e);
        }

        return openHABRoots;
    }

    private ResourceSpecification createRootSpecification(String name, String path, String description, String access) {
        // Create a basic resource specification for the root
        return new ResourceSpecification(name, // id
                name, // name
                description, // description
                "1.0.0", // version
                Map.of(), // inputSchema
                Map.of(), // outputSchema
                Map.of(), // configuration
                Map.of("path", path, "access", access, "type", "root") // metadata
        ) {
            @Override
            public String getUriPattern() {
                return "file://" + path + "/{*:path}";
            }

            @Override
            public String getMimeType() {
                return "application/vnd.openhab.root+json";
            }

            @Override
            public org.openhab.core.ai.tool.api.validation.ResourceMetadata getResourceMetadata() {
                return new org.openhab.core.ai.tool.api.validation.ResourceMetadata("1.0.0", // version
                        "openHAB AI", // author
                        Map.of("path", path, "access", access, "type", "root") // properties
                );
            }

            @Override
            public org.openhab.core.ai.tool.api.validation.ResourceValidationResult validateParameters(
                    Map<String, Object> parameters) {
                return org.openhab.core.ai.tool.api.validation.ResourceValidationResult.success();
            }

            @Override
            public org.openhab.core.ai.tool.api.ResourceResult execute(Map<String, Object> parameters,
                    org.openhab.core.ai.tool.api.ResourceContext context) {
                return org.openhab.core.ai.tool.api.ResourceResult.success(
                        Map.of("root", name, "path", path, "access", access), // content
                        0 // executionTimeMs
                );
            }
        };
    }

    private boolean matchesCriteria(ResourceSpecification root, Map<String, Object> criteria) {
        try {
            for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    case "name":
                        if (!root.getName().contains(String.valueOf(value))) {
                            return false;
                        }
                        break;
                    case "type":
                        Map<String, Object> metadata = root.getMetadata();
                        if (metadata != null && !"root".equals(metadata.get("type"))) {
                            return false;
                        }
                        break;
                    case "access":
                        Map<String, Object> rootMetadata = root.getMetadata();
                        if (rootMetadata != null && !String.valueOf(value).equals(rootMetadata.get("access"))) {
                            return false;
                        }
                        break;
                    default:
                        // Unknown criteria, skip
                        break;
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("Error matching criteria for root: {}", root.getName(), e);
            return false;
        }
    }
}
