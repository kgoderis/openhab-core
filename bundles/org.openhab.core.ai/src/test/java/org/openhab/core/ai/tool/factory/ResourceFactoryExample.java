package org.openhab.core.ai.tool.factory;

import java.util.Map;

import org.openhab.core.ai.tool.resource.AbstractResource;
import org.openhab.core.ai.tool.resource.Resource;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;

/**
 * Example demonstrating bidirectional conversion between Resource DTOs and AbstractResource objects.
 *
 * This example shows how to:
 * 1. Convert from Resource DTO to AbstractResource (fromResource)
 * 2. Convert from AbstractResource to Resource DTO (toResource)
 * 3. Use the encapsulated behavior of AbstractResource
 * 4. Perform round-trip conversions
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public class ResourceFactoryExample {

    /**
     * Example demonstrating the complete bidirectional conversion workflow.
     */
    public static void demonstrateBidirectionalConversion(ItemRegistry itemRegistry, ThingRegistry thingRegistry,
            RuleRegistry ruleRegistry) {

        ResourceFactory factory = new ResourceFactory();

        // Create context with required registries
        Map<String, Object> context = Map.of("itemRegistry", itemRegistry, "thingRegistry", thingRegistry,
                "ruleRegistry", ruleRegistry);

        System.out.println("=== Resource Factory Bidirectional Conversion Example ===\n");

        // Example 1: Item Resource
        demonstrateItemResourceConversion(factory, context);

        // Example 2: Thing Resource
        demonstrateThingResourceConversion(factory, context);

        // Example 3: Rule Resource
        demonstrateRuleResourceConversion(factory, context);

        // Example 4: Round-trip conversion
        demonstrateRoundTripConversion(factory, context);

        // Example 5: Error handling
        demonstrateErrorHandling(factory, context);

        System.out.println("=== Example Complete ===");
    }

    private static void demonstrateItemResourceConversion(ResourceFactory factory, Map<String, Object> context) {
        System.out.println("1. Item Resource Conversion:");

        // Create a Resource DTO
        Resource itemDto = new Resource("openhab://items/living_room_light", "Living Room Light",
                "Main living room lighting control", "text/plain", Map.of("type", "item", "category", "lighting"));

        System.out.println("   Created Resource DTO: " + itemDto.getUri());

        // Convert DTO to AbstractResource
        AbstractResource itemResource = factory.fromResource(itemDto, context);

        if (itemResource != null) {
            System.out.println("   ✓ Successfully converted to AbstractResource");
            System.out.println("   - URI: " + itemResource.getUri());
            System.out.println("   - Name: " + itemResource.getName());
            System.out.println("   - Valid: " + itemResource.isValid());
            System.out.println("   - Writable: " + itemResource.isWritable());
            System.out.println("   - Exists: " + itemResource.exists());

            // Use encapsulated behavior
            String content = itemResource.getContent();
            System.out.println("   - Content: "
                    + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null"));

            // Convert back to DTO
            Resource convertedDto = itemResource.toResource();
            System.out.println("   ✓ Successfully converted back to Resource DTO");
            System.out.println("   - URI matches: " + itemDto.getUri().equals(convertedDto.getUri()));
        } else {
            System.out.println("   ✗ Failed to convert Resource DTO to AbstractResource");
        }

        System.out.println();
    }

    private static void demonstrateThingResourceConversion(ResourceFactory factory, Map<String, Object> context) {
        System.out.println("2. Thing Resource Conversion:");

        // Create a Resource DTO for a thing
        Resource thingDto = new Resource("openhab://things/hue:bridge:001", "Philips Hue Bridge",
                "Philips Hue Bridge for smart lighting control", "text/plain",
                Map.of("type", "thing", "vendor", "Philips"));

        System.out.println("   Created Resource DTO: " + thingDto.getUri());

        // Convert DTO to AbstractResource with custom refresh interval
        AbstractResource thingResource = factory.fromResource(thingDto, context, 30000); // 30 seconds

        if (thingResource != null) {
            System.out.println("   ✓ Successfully converted to AbstractResource");
            System.out.println("   - URI: " + thingResource.getUri());
            System.out.println("   - Name: " + thingResource.getName());
            System.out.println("   - Valid: " + thingResource.isValid());
            System.out.println("   - Needs refresh: " + thingResource.needsRefresh());

            // Demonstrate refresh behavior
            thingResource.refresh();
            System.out.println("   - Refreshed resource");
        } else {
            System.out.println("   ✗ Failed to convert Resource DTO to AbstractResource");
        }

        System.out.println();
    }

    private static void demonstrateRuleResourceConversion(ResourceFactory factory, Map<String, Object> context) {
        System.out.println("3. Rule Resource Conversion:");

        // Create a Resource DTO for a rule
        Resource ruleDto = new Resource("openhab://rules/automation_001", "Light Automation Rule",
                "Automated lighting control based on time and presence", "text/plain",
                Map.of("type", "rule", "tags", "automation,lighting"));

        System.out.println("   Created Resource DTO: " + ruleDto.getUri());

        // Convert DTO to AbstractResource
        AbstractResource ruleResource = factory.fromResource(ruleDto, context);

        if (ruleResource != null) {
            System.out.println("   ✓ Successfully converted to AbstractResource");
            System.out.println("   - URI: " + ruleResource.getUri());
            System.out.println("   - Name: " + ruleResource.getName());
            System.out.println("   - Valid: " + ruleResource.isValid());

            // Demonstrate lifecycle management
            System.out.println("   - Last refresh time: " + ruleResource.getLastRefreshTime());
            ruleResource.refresh();
            System.out.println("   - Refreshed, new time: " + ruleResource.getLastRefreshTime());
        } else {
            System.out.println("   ✗ Failed to convert Resource DTO to AbstractResource");
        }

        System.out.println();
    }

    private static void demonstrateRoundTripConversion(ResourceFactory factory, Map<String, Object> context) {
        System.out.println("4. Round-trip Conversion Test:");

        // Original DTO with metadata
        Map<String, Object> metadata = Map.of("type", "item", "category", "lighting", "location", "living_room", "tags",
                "smart,automated");

        Resource originalDto = new Resource("openhab://items/smart_light", "Smart Light",
                "Intelligent lighting with automation", "text/plain", metadata);

        System.out.println("   Original DTO: " + originalDto.getUri());
        System.out.println("   Metadata keys: " + originalDto.getMetadata().keySet());

        // Round-trip conversion
        AbstractResource abstractResource = factory.fromResource(originalDto, context);
        Resource convertedDto = abstractResource != null ? abstractResource.toResource() : null;

        if (convertedDto != null) {
            System.out.println("   ✓ Round-trip conversion successful");
            System.out.println("   - URI preserved: " + originalDto.getUri().equals(convertedDto.getUri()));
            System.out.println("   - Name preserved: " + originalDto.getName().equals(convertedDto.getName()));
            System.out.println("   - Description preserved: "
                    + originalDto.getDescription().equals(convertedDto.getDescription()));
            System.out.println(
                    "   - MIME type preserved: " + originalDto.getMimeType().equals(convertedDto.getMimeType()));

            // Check metadata preservation
            boolean metadataPreserved = originalDto.getMetadata() != null && convertedDto.getMetadata() != null
                    && originalDto.getMetadata().equals(convertedDto.getMetadata());
            System.out.println("   - Metadata preserved: " + metadataPreserved);
        } else {
            System.out.println("   ✗ Round-trip conversion failed");
        }

        System.out.println();
    }

    private static void demonstrateErrorHandling(ResourceFactory factory, Map<String, Object> context) {
        System.out.println("5. Error Handling Examples:");

        // Test 1: Unknown URI scheme
        Resource unknownDto = new Resource("unknown://scheme/resource", "Unknown Resource",
                "Resource with unknown URI scheme", "text/plain", null);

        AbstractResource unknownResource = factory.fromResource(unknownDto, context);
        System.out.println(
                "   Unknown URI scheme: " + (unknownResource == null ? "✓ Handled gracefully" : "✗ Should be null"));

        // Test 2: Missing registry
        Resource itemDto = new Resource("openhab://items/light1", "Light1", "Test light", "text/plain", null);

        Map<String, Object> emptyContext = Map.of(); // No registries
        AbstractResource missingRegistryResource = factory.fromResource(itemDto, emptyContext);
        System.out.println("   Missing registry: "
                + (missingRegistryResource == null ? "✓ Handled gracefully" : "✗ Should be null"));

        // Test 3: Invalid URI format
        Resource invalidDto = new Resource("openhab://items/", // Missing item name
                "Invalid", "Invalid URI format", "text/plain", null);

        AbstractResource invalidResource = factory.fromResource(invalidDto, context);
        System.out.println(
                "   Invalid URI format: " + (invalidResource == null ? "✓ Handled gracefully" : "✗ Should be null"));

        System.out.println();
    }
}
