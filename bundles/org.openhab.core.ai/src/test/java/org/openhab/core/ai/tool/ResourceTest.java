package org.openhab.core.ai.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.resource.Resource;

/**
 * Unit tests for the Resource class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceTest {

    @Test
    public void testResourceCreation() {
        // Given
        String uri = "test://resource";
        String name = "Test Resource";
        String description = "A test resource";
        String mimeType = "application/json";
        Map<String, Object> metadata = Map.of("key", "value");

        // When
        Resource resource = new Resource(uri, name, description, mimeType, metadata);

        // Then
        assertEquals(uri, resource.getUri());
        assertEquals(name, resource.getName());
        assertEquals(description, resource.getDescription());
        assertEquals(mimeType, resource.getMimeType());
        assertEquals(metadata, resource.getMetadata());
    }

    @Test
    public void testResourceCreationWithNullMetadata() {
        // Given
        String uri = "test://resource";
        String name = "Test Resource";
        String description = "A test resource";
        String mimeType = "application/json";

        // When
        Resource resource = new Resource(uri, name, description, mimeType, null);

        // Then
        assertEquals(uri, resource.getUri());
        assertEquals(name, resource.getName());
        assertEquals(description, resource.getDescription());
        assertEquals(mimeType, resource.getMimeType());
        assertNull(resource.getMetadata());
    }

    @Test
    public void testResourceToString() {
        // Given
        String uri = "test://resource";
        String name = "Test Resource";
        String description = "A test resource";
        String mimeType = "application/json";

        // When
        Resource resource = new Resource(uri, name, description, mimeType, null);
        String result = resource.toString();

        // Then
        assertTrue(result.contains("uri='" + uri + "'"));
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("description='" + description + "'"));
        assertTrue(result.contains("mimeType='" + mimeType + "'"));
    }
}
