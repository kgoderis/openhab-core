package org.openhab.core.ai.tool.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;

/**
 * Test class for ResourceFactory fromResource functionality.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ResourceFactoryTest {

    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    private ThingRegistry thingRegistry;

    @Mock
    private RuleRegistry ruleRegistry;

    private ResourceFactory resourceFactory;

    @BeforeEach
    void setUp() {
        resourceFactory = new ResourceFactory();
    }

    @Test
    void testFromResource_ItemResource() {
        // Given
        Resource resourceDto = new Resource("openhab://items/light1", "Light1", "Living room light", "text/plain",
                null);
        Map<String, Object> context = Map.of("itemRegistry", itemRegistry);

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNotNull(result);
        assertEquals("openhab://items/light1", result.getUri());
        assertEquals("Light1", result.getName());
        assertEquals("Living room light", result.getDescription());
        assertTrue(result.isValid());
    }

    @Test
    void testFromResource_ThingResource() {
        // Given
        Resource resourceDto = new Resource("openhab://things/hue:bridge:001", "Hue Bridge", "Philips Hue Bridge",
                "text/plain", null);
        Map<String, Object> context = Map.of("thingRegistry", thingRegistry);

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNotNull(result);
        assertEquals("openhab://things/hue:bridge:001", result.getUri());
        assertEquals("Hue Bridge", result.getName());
        assertEquals("Philips Hue Bridge", result.getDescription());
        assertTrue(result.isValid());
    }

    @Test
    void testFromResource_RuleResource() {
        // Given
        Resource resourceDto = new Resource("openhab://rules/automation_001", "Automation Rule",
                "Light automation rule", "text/plain", null);
        Map<String, Object> context = Map.of("ruleRegistry", ruleRegistry);

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNotNull(result);
        assertEquals("openhab://rules/automation_001", result.getUri());
        assertEquals("Automation Rule", result.getName());
        assertEquals("Light automation rule", result.getDescription());
        assertTrue(result.isValid());
    }

    @Test
    void testFromResource_UnknownUriScheme() {
        // Given
        Resource resourceDto = new Resource("unknown://scheme/resource", "Unknown", "Unknown resource", "text/plain",
                null);
        Map<String, Object> context = Map.of();

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNull(result);
    }

    @Test
    void testFromResource_MissingRegistry() {
        // Given
        Resource resourceDto = new Resource("openhab://items/light1", "Light1", "Living room light", "text/plain",
                null);
        Map<String, Object> context = Map.of(); // No itemRegistry provided

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNull(result);
    }

    @Test
    void testFromResource_WithCustomRefreshInterval() {
        // Given
        Resource resourceDto = new Resource("openhab://items/light1", "Light1", "Living room light", "text/plain",
                null);
        Map<String, Object> context = Map.of("itemRegistry", itemRegistry);
        long customInterval = 10000; // 10 seconds

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context, customInterval);

        // Then
        assertNotNull(result);
        assertEquals("openhab://items/light1", result.getUri());
        assertTrue(result.isValid());
    }

    @Test
    void testFromResource_RoundTripConversion() {
        // Given
        Resource originalDto = new Resource("openhab://items/light1", "Light1", "Living room light", "text/plain",
                null);
        Map<String, Object> context = Map.of("itemRegistry", itemRegistry);

        // When
        AbstractResource abstractResource = resourceFactory.fromResource(originalDto, context);
        Resource convertedDto = abstractResource.toResource();

        // Then
        assertNotNull(abstractResource);
        assertNotNull(convertedDto);
        assertEquals(originalDto.getUri(), convertedDto.getUri());
        assertEquals(originalDto.getName(), convertedDto.getName());
        assertEquals(originalDto.getDescription(), convertedDto.getDescription());
        assertEquals(originalDto.getMimeType(), convertedDto.getMimeType());
    }

    @Test
    void testFromResource_WithMetadata() {
        // Given
        Map<String, Object> metadata = Map.of("type", "item", "category", "lighting");
        Resource resourceDto = new Resource("openhab://items/light1", "Light1", "Living room light", "text/plain",
                metadata);
        Map<String, Object> context = Map.of("itemRegistry", itemRegistry);

        // When
        AbstractResource result = resourceFactory.fromResource(resourceDto, context);

        // Then
        assertNotNull(result);
        assertEquals("openhab://items/light1", result.getUri());
        assertTrue(result.getMetadata().containsKey("type"));
        assertEquals("item", result.getMetadata().get("type"));
        assertEquals("lighting", result.getMetadata().get("category"));
    }
}
