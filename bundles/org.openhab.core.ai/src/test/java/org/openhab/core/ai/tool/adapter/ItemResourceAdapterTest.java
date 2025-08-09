package org.openhab.core.ai.tool.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.resource.Resource;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;

/**
 * Unit tests for ItemResourceAdapter.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ItemResourceAdapterTest {

    @Mock
    private ItemRegistry itemRegistry;

    private ItemResourceAdapter adapter;
    private Item testItem;

    @BeforeEach
    void setUp() {
        adapter = new ItemResourceAdapter(itemRegistry);

        // Create a test item
        testItem = new SwitchItem("testSwitch");
        // Note: In a real test, you would set up the item with proper metadata
        // For this test, we'll use the default item properties
    }

    @Test
    void testCreateItemResource_Success() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        Resource resource = adapter.createItemResource("testSwitch");

        // Then
        assertNotNull(resource);
        assertEquals("openhab://items/testSwitch", resource.getUri());
        assertEquals("Item: testSwitch", resource.getName());
        assertEquals("Resource adapter for openHAB item: testSwitch", resource.getDescription());
        assertEquals("application/json", resource.getMimeType());

        var metadata = resource.getMetadata();
        assertNotNull(metadata);
        assertEquals("openhab-item", metadata.get("type"));
        assertEquals("testSwitch", metadata.get("itemName"));
        assertEquals("Switch", metadata.get("itemType"));
        // Note: Label, category, and tags would be set in a real item configuration
        // For this test, we just verify the metadata structure exists
        assertNotNull(metadata.get("label"));
        assertNotNull(metadata.get("category"));
        assertNotNull(metadata.get("tags"));
    }

    @Test
    void testCreateItemResource_ItemNotFound() {
        // Given
        when(itemRegistry.get("nonexistent")).thenReturn(null);

        // When
        Resource resource = adapter.createItemResource("nonexistent");

        // Then
        assertNull(resource);
    }

    @Test
    void testGetItemContent_Success() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        String content = adapter.getItemContent("testSwitch");

        // Then
        assertNotNull(content);
        assertTrue(content.contains("\"name\":\"testSwitch\""));
        assertTrue(content.contains("\"type\":\"Switch\""));
        assertTrue(content.contains("\"state\":\"OFF\""));
        // Note: Label, category, and tags would be set in a real item configuration
        // For this test, we just verify the content structure exists
        assertTrue(content.contains("\"label\""));
        assertTrue(content.contains("\"category\""));
        assertTrue(content.contains("\"tags\""));
    }

    @Test
    void testGetItemContent_ItemNotFound() {
        // Given
        when(itemRegistry.get("nonexistent")).thenReturn(null);

        // When
        String content = adapter.getItemContent("nonexistent");

        // Then
        assertNull(content);
    }

    @Test
    void testIsItemWritable_True() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        boolean writable = adapter.isItemWritable("testSwitch");

        // Then
        assertTrue(writable);
    }

    @Test
    void testIsItemWritable_False() {
        // Given
        when(itemRegistry.get("nonexistent")).thenReturn(null);

        // When
        boolean writable = adapter.isItemWritable("nonexistent");

        // Then
        assertFalse(writable);
    }

    @Test
    void testWriteItemContent_Success() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);
        String content = "{\"state\":\"ON\"}";

        // When
        boolean result = adapter.writeItemContent("testSwitch", content);

        // Then
        assertTrue(result);
    }

    @Test
    void testWriteItemContent_InvalidFormat() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);
        String content = "{\"invalid\":\"format\"}";

        // When
        boolean result = adapter.writeItemContent("testSwitch", content);

        // Then
        assertFalse(result);
    }

    @Test
    void testWriteItemContent_NullContent() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        boolean result = adapter.writeItemContent("testSwitch", null);

        // Then
        assertFalse(result);
    }

    @Test
    void testWriteItemContent_EmptyContent() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        boolean result = adapter.writeItemContent("testSwitch", "");

        // Then
        assertFalse(result);
    }

    @Test
    void testWriteItemContent_ItemNotWritable() {
        // Given
        when(itemRegistry.get("nonexistent")).thenReturn(null);
        String content = "{\"state\":\"ON\"}";

        // When
        boolean result = adapter.writeItemContent("nonexistent", content);

        // Then
        assertFalse(result);
    }

    @Test
    void testItemExists_True() {
        // Given
        when(itemRegistry.get("testSwitch")).thenReturn(testItem);

        // When
        boolean exists = adapter.itemExists("testSwitch");

        // Then
        assertTrue(exists);
    }

    @Test
    void testItemExists_False() {
        // Given
        when(itemRegistry.get("nonexistent")).thenReturn(null);

        // When
        boolean exists = adapter.itemExists("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void testExtractStateFromJson_ValidState() {
        // Given
        String content = "{\"state\":\"ON\",\"other\":\"value\"}";

        // When
        String state = adapter.extractStateFromJson(content);

        // Then
        assertEquals("ON", state);
    }

    @Test
    void testExtractStateFromJson_NoState() {
        // Given
        String content = "{\"other\":\"value\"}";

        // When
        String state = adapter.extractStateFromJson(content);

        // Then
        assertNull(state);
    }

    @Test
    void testExtractStateFromJson_InvalidJson() {
        // Given
        String content = "invalid json";

        // When
        String state = adapter.extractStateFromJson(content);

        // Then
        assertNull(state);
    }
}
