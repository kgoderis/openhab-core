package org.openhab.core.ai.common.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the unified adapter hierarchy.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AdapterTest {

    @Test
    void testBaseAdapterLifecycle() {
        TestBaseAdapter adapter = new TestBaseAdapter(1000L);

        assertTrue(adapter.isValid());
        assertEquals(0, adapter.getLastRefreshTime());
        assertFalse(adapter.needsRefresh());

        adapter.refresh("test", "context");
        assertTrue(adapter.getLastRefreshTime() > 0);

        adapter.markInvalid();
        assertFalse(adapter.isValid());

        adapter.cleanup();
        adapter.close();
    }

    @Test
    void testResourceAdapter() {
        TestResourceAdapter adapter = new TestResourceAdapter(1000L);

        assertNotNull(adapter.createEntity("test", "context"));
        assertEquals("test content", adapter.getContent("test", "context"));
        assertTrue(adapter.isWritable("test", "context"));
        assertTrue(adapter.writeContent("test", "new content", "context"));
        assertTrue(adapter.exists("test", "context"));

        Object result = adapter.execute("test", "get", Map.of(), "context");
        assertNotNull(result);

        adapter.cleanup();
    }

    @Test
    void testCompletionAdapter() {
        TestCompletionAdapter adapter = new TestCompletionAdapter(1000L);

        assertNotNull(adapter.createEntity("test", "context"));
        assertEquals("test content", adapter.getContent("test", "context"));
        assertFalse(adapter.isWritable("test", "context"));
        assertFalse(adapter.writeContent("test", "new content", "context"));
        assertTrue(adapter.exists("test", "context"));

        List<String> suggestions = adapter.getSuggestions("test", "context");
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());

        Object result = adapter.execute("test", "suggest", Map.of(), "context");
        assertNotNull(result);

        adapter.cleanup();
    }

    @Test
    void testPromptAdapter() {
        TestPromptAdapter adapter = new TestPromptAdapter(1000L);

        assertNotNull(adapter.createEntity("test", "context"));
        assertEquals("test content", adapter.getContent("test", "context"));
        assertTrue(adapter.isWritable("test", "context"));
        assertTrue(adapter.writeContent("test", "new content", "context"));
        assertTrue(adapter.exists("test", "context"));

        String prompt = adapter.generatePrompt("test", "context");
        assertEquals("Generated prompt for test", prompt);

        Object result = adapter.execute("test", "generate", Map.of(), "context");
        assertNotNull(result);

        adapter.cleanup();
    }

    // Test implementations
    private static class TestBaseAdapter extends BaseAdapter<String, String, String> {
        public TestBaseAdapter(long refreshIntervalMs) {
            super(refreshIntervalMs);
        }

        @Override
        public String adapt(String source, String context) {
            return source + " adapted with " + context;
        }

        @Override
        public boolean canAdapt(String source) {
            return source != null && !source.isEmpty();
        }

        @Override
        public Class<String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<String> getResultType() {
            return String.class;
        }

        @Override
        public String getAdapterType() {
            return "test";
        }

        @Override
        public String getUriPattern() {
            return "test://{id}";
        }

        @Override
        protected void doRefresh(String identifier, String context) {
            // Test implementation
        }

        @Override
        protected void doCleanup() {
            // Test implementation
        }
    }

    private static class TestResourceAdapter extends ResourceAdapter<String, String, Object> {
        public TestResourceAdapter(long refreshIntervalMs) {
            super(refreshIntervalMs);
        }

        @Override
        public String adapt(String source, String context) {
            return source + " adapted with " + context;
        }

        @Override
        public boolean canAdapt(String source) {
            return source != null && !source.isEmpty();
        }

        @Override
        public Class<String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<Object> getResultType() {
            return Object.class;
        }

        @Override
        public String getAdapterType() {
            return "test-resource";
        }

        @Override
        public String getUriPattern() {
            return "test://resource/{id}";
        }

        @Override
        public String createEntity(String identifier, String context) {
            return "entity:" + identifier;
        }

        @Override
        public String getContent(String identifier, String context) {
            return "test content";
        }

        @Override
        public boolean isWritable(String identifier, String context) {
            return true;
        }

        @Override
        public boolean writeContent(String identifier, String content, String context) {
            return true;
        }

        @Override
        public boolean exists(String identifier, String context) {
            return true;
        }

        @Override
        public Object execute(String identifier, String operation, Map<String, Object> parameters, String context) {
            return Map.of("success", true, "operation", operation);
        }

        @Override
        protected void doRefresh(String identifier, String context) {
            // Test implementation
        }
    }

    private static class TestCompletionAdapter extends CompletionAdapter<String, String, Object> {
        public TestCompletionAdapter(long refreshIntervalMs) {
            super(refreshIntervalMs);
        }

        @Override
        public String adapt(String source, String context) {
            return source + " adapted with " + context;
        }

        @Override
        public boolean canAdapt(String source) {
            return source != null && !source.isEmpty();
        }

        @Override
        public Class<String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<Object> getResultType() {
            return Object.class;
        }

        @Override
        public String getAdapterType() {
            return "test-completion";
        }

        @Override
        public String getUriPattern() {
            return "test://completion/{id}";
        }

        @Override
        public String createEntity(String identifier, String context) {
            return "completion:" + identifier;
        }

        @Override
        public String getContent(String identifier, String context) {
            return "test content";
        }

        @Override
        public boolean isWritable(String identifier, String context) {
            return false;
        }

        @Override
        public boolean writeContent(String identifier, String content, String context) {
            return false;
        }

        @Override
        public boolean exists(String identifier, String context) {
            return true;
        }

        @Override
        public Object execute(String identifier, String operation, Map<String, Object> parameters, String context) {
            return Map.of("success", true, "operation", operation);
        }

        @Override
        public List<String> getSuggestions(String identifier, String context) {
            return List.of("suggestion1", "suggestion2");
        }

        @Override
        protected void doRefresh(String identifier, String context) {
            // Test implementation
        }
    }

    private static class TestPromptAdapter extends PromptAdapter<String, String, Object> {
        public TestPromptAdapter(long refreshIntervalMs) {
            super(refreshIntervalMs);
        }

        @Override
        public String adapt(String source, String context) {
            return source + " adapted with " + context;
        }

        @Override
        public boolean canAdapt(String source) {
            return source != null && !source.isEmpty();
        }

        @Override
        public Class<String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<Object> getResultType() {
            return Object.class;
        }

        @Override
        public String getAdapterType() {
            return "test-prompt";
        }

        @Override
        public String getUriPattern() {
            return "test://prompt/{id}";
        }

        @Override
        public String createEntity(String identifier, String context) {
            return "prompt:" + identifier;
        }

        @Override
        public String getContent(String identifier, String context) {
            return "test content";
        }

        @Override
        public boolean isWritable(String identifier, String context) {
            return true;
        }

        @Override
        public boolean writeContent(String identifier, String content, String context) {
            return true;
        }

        @Override
        public boolean exists(String identifier, String context) {
            return true;
        }

        @Override
        public Object execute(String identifier, String operation, Map<String, Object> parameters, String context) {
            return Map.of("success", true, "operation", operation);
        }

        @Override
        public String generatePrompt(String identifier, String context) {
            return "Generated prompt for " + identifier;
        }

        @Override
        protected void doRefresh(String identifier, String context) {
            // Test implementation
        }
    }
}
