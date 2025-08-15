/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.tool.roots.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

/**
 * Unit tests for DefaultRootDiscoveryService.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class DefaultRootDiscoveryServiceTest {

    private DefaultRootDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultRootDiscoveryService();
    }

    @Test
    void testGetServiceId() {
        String serviceId = service.getServiceId();
        assertNotNull(serviceId);
        assertTrue(serviceId.startsWith("default-root-discovery-"));
    }

    @Test
    void testGetServiceName() {
        String serviceName = service.getServiceName();
        assertEquals("Default Root Discovery Service", serviceName);
    }

    @Test
    void testGetServiceDescription() {
        String description = service.getServiceDescription();
        assertEquals("Default implementation for MCP root discovery", description);
    }

    @Test
    void testDiscoverRoots() {
        List<ResourceSpecification> roots = service.discoverRoots();
        assertNotNull(roots);
        assertFalse(roots.isEmpty());

        // Should discover system roots
        boolean hasFilesystemRoot = roots.stream().anyMatch(root -> "filesystem".equals(root.getName()));
        assertTrue(hasFilesystemRoot, "Should discover filesystem root");

        // Should discover openHAB roots
        boolean hasOpenHABConfigRoot = roots.stream().anyMatch(root -> "openhab-config".equals(root.getName()));
        assertTrue(hasOpenHABConfigRoot, "Should discover openHAB config root");
    }

    @Test
    void testDiscoverRootsWithCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", "filesystem");

        List<ResourceSpecification> roots = service.discoverRoots(criteria);
        assertNotNull(roots);
        assertFalse(roots.isEmpty());

        // Should only return filesystem root
        assertEquals(1, roots.size());
        assertEquals("filesystem", roots.get(0).getName());
    }

    @Test
    void testDiscoverRootsWithTypeCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("type", "root");

        List<ResourceSpecification> roots = service.discoverRoots(criteria);
        assertNotNull(roots);
        assertFalse(roots.isEmpty());

        // All returned roots should have type "root" in metadata
        for (ResourceSpecification root : roots) {
            Map<String, Object> metadata = root.getMetadata();
            assertNotNull(metadata);
            assertEquals("root", metadata.get("type"));
        }
    }

    @Test
    void testDiscoverRootsWithAccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("access", "read-write");

        List<ResourceSpecification> roots = service.discoverRoots(criteria);
        assertNotNull(roots);
        assertFalse(roots.isEmpty());

        // All returned roots should have read-write access
        for (ResourceSpecification root : roots) {
            Map<String, Object> metadata = root.getMetadata();
            assertNotNull(metadata);
            assertEquals("read-write", metadata.get("access"));
        }
    }

    @Test
    void testGetRoot() {
        ResourceSpecification root = service.getRoot("filesystem");
        assertNotNull(root);
        assertEquals("filesystem", root.getName());
        assertEquals("Filesystem root", root.getDescription());
    }

    @Test
    void testGetRootNotFound() {
        ResourceSpecification root = service.getRoot("nonexistent-root");
        assertNull(root);
    }

    @Test
    void testRegisterRoot() {
        // Create a test root
        ResourceSpecification testRoot = createTestRoot("test-root", "/test", "Test root", "read-write");

        // Register the root
        service.registerRoot(testRoot);

        // Verify it can be retrieved
        ResourceSpecification retrieved = service.getRoot("test-root");
        assertNotNull(retrieved);
        assertEquals("test-root", retrieved.getName());
    }

    @Test
    void testRegisterRootWithNullName() {
        ResourceSpecification testRoot = createTestRoot(null, "/test", "Test root", "read-write");

        assertThrows(RuntimeException.class, () -> {
            service.registerRoot(testRoot);
        });
    }

    @Test
    void testRegisterRootWithEmptyName() {
        ResourceSpecification testRoot = createTestRoot("", "/test", "Test root", "read-write");

        assertThrows(RuntimeException.class, () -> {
            service.registerRoot(testRoot);
        });
    }

    @Test
    void testUnregisterRoot() {
        // Create and register a test root
        ResourceSpecification testRoot = createTestRoot("test-root", "/test", "Test root", "read-write");
        service.registerRoot(testRoot);

        // Verify it exists
        assertNotNull(service.getRoot("test-root"));

        // Unregister it
        service.unregisterRoot("test-root");

        // Verify it no longer exists
        assertNull(service.getRoot("test-root"));
    }

    @Test
    void testUnregisterNonexistentRoot() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            service.unregisterRoot("nonexistent-root");
        });
    }

    @Test
    void testGetConfiguration() {
        Map<String, Object> config = service.getConfiguration();
        assertNotNull(config);
        assertTrue(config.containsKey("discoveryEnabled"));
        assertTrue(config.containsKey("cacheTimeout"));
        assertTrue(config.containsKey("maxRoots"));
        assertEquals(true, config.get("discoveryEnabled"));
        assertEquals(300000, config.get("cacheTimeout"));
        assertEquals(100, config.get("maxRoots"));
    }

    @Test
    void testUpdateConfiguration() {
        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("discoveryEnabled", false);
        newConfig.put("cacheTimeout", 600000);
        newConfig.put("maxRoots", 200);

        service.updateConfiguration(newConfig);

        Map<String, Object> updatedConfig = service.getConfiguration();
        assertEquals(false, updatedConfig.get("discoveryEnabled"));
        assertEquals(600000, updatedConfig.get("cacheTimeout"));
        assertEquals(200, updatedConfig.get("maxRoots"));
    }

    @Test
    void testRootSpecificationProperties() {
        List<ResourceSpecification> roots = service.discoverRoots();
        assertFalse(roots.isEmpty());

        ResourceSpecification root = roots.get(0);
        assertNotNull(root.getUriPattern());
        assertNotNull(root.getMimeType());
        assertNotNull(root.getResourceMetadata());
        assertNotNull(root.getMetadata());

        // Test validation
        Map<String, Object> parameters = new HashMap<>();
        var validationResult = root.validateParameters(parameters);
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());

        // Test execution
        var context = new org.openhab.core.ai.tool.api.ResourceContext();
        var result = root.execute(parameters, context);
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    private ResourceSpecification createTestRoot(String name, String path, String description, String access) {
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
            public org.openhab.core.ai.tool.resources.api.validation.ResourceMetadata getResourceMetadata() {
                return new org.openhab.core.ai.tool.resources.api.validation.ResourceMetadata("1.0.0", // version
                        "openHAB AI", // author
                        Map.of("path", path, "access", access, "type", "root") // properties
                );
            }

            @Override
            public org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult validateParameters(
                    Map<String, Object> parameters) {
                return org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult.success();
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
}
