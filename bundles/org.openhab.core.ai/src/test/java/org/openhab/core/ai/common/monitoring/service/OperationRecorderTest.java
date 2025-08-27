package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Unit tests for OperationRecorder builder pattern.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OperationRecorderTest {

    @Mock
    private MetricsService metricsService;

    private OperationRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new OperationRecorder(metricsService, "test-domain", "test-operation");
    }

    @Test
    void testConstructorWithValidParameters() {
        assertNotNull(recorder);
    }

    @Test
    void testConstructorWithNullService() {
        assertThrows(NullPointerException.class, () -> {
            new OperationRecorder(null, "domain", "operation");
        });
    }

    @Test
    void testConstructorWithNullDomain() {
        assertThrows(NullPointerException.class, () -> {
            new OperationRecorder(metricsService, null, "operation");
        });
    }

    @Test
    void testConstructorWithNullOperation() {
        assertThrows(NullPointerException.class, () -> {
            new OperationRecorder(metricsService, "domain", null);
        });
    }

    @Test
    void testWithSuccess() {
        OperationRecorder result = recorder.withSuccess(true);
        assertSame(recorder, result);
    }

    @Test
    void testWithDuration() {
        OperationRecorder result = recorder.withDuration(1000000L);
        assertSame(recorder, result);
    }

    @Test
    void testWithDurationNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withDuration(-1L);
        });
    }

    @Test
    void testWithData() {
        OperationRecorder result = recorder.withData("key", "value");
        assertSame(recorder, result);
    }

    @Test
    void testWithDataNullKey() {
        assertThrows(NullPointerException.class, () -> {
            recorder.withData(null, "value");
        });
    }

    @Test
    void testWithDataBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withData("", "value");
        });
    }

    @Test
    void testWithDataWhitespaceKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withData("   ", "value");
        });
    }

    @Test
    void testWithDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("key1", "value1");
        dataMap.put("key2", 42);

        OperationRecorder result = recorder.withData(dataMap);
        assertSame(recorder, result);
    }

    @Test
    void testWithDataMapNull() {
        assertThrows(NullPointerException.class, () -> {
            recorder.withData((Map<String, Object>) null);
        });
    }

    @Test
    void testRecordWithBasicData() {
        // Given
        recorder.withSuccess(true).withDuration(1000000L);

        // When
        recorder.record();

        // Then
        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(true),
                eq(Duration.ofNanos(1000000L)), eq(new HashMap<>()));
    }

    @Test
    void testRecordWithAdditionalData() {
        // Given
        recorder.withSuccess(false).withDuration(2000000L).withData("tokens", 150).withData("cost", 0.002)
                .withData("temperature", 0.7);

        // When
        recorder.record();

        // Then
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("tokens", 150);
        expectedData.put("cost", 0.002);
        expectedData.put("temperature", 0.7);

        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(false),
                eq(Duration.ofNanos(2000000L)), eq(expectedData));
    }

    @Test
    void testRecordWithDataMap() {
        // Given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("fileSize", 1024L);
        dataMap.put("bytesRead", 1024L);
        dataMap.put("fileType", "json");

        recorder.withSuccess(true).withDuration(500000L).withData(dataMap);

        // When
        recorder.record();

        // Then
        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(true),
                eq(Duration.ofNanos(500000L)), eq(dataMap));
    }

    @Test
    void testRecordWithMixedData() {
        // Given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("taskType", "classification");
        dataMap.put("priority", 5);

        recorder.withSuccess(true).withDuration(3000000L).withData("skill", "image_analysis").withData(dataMap)
                .withData("confidence", 0.95);

        // When
        recorder.record();

        // Then
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("skill", "image_analysis");
        expectedData.put("taskType", "classification");
        expectedData.put("priority", 5);
        expectedData.put("confidence", 0.95);

        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(true),
                eq(Duration.ofNanos(3000000L)), eq(expectedData));
    }

    @Test
    void testFluentApiChaining() {
        // Given & When
        OperationRecorder result = recorder.withSuccess(true).withDuration(1000000L).withData("key1", "value1")
                .withData("key2", "value2");

        // Then
        assertSame(recorder, result);
    }

    @Test
    void testRecordWithoutSettingValues() {
        // Given - recorder with default values
        // When
        recorder.record();

        // Then - should record with default values
        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(false), // default
                                                                                                           // success
                                                                                                           // value
                eq(Duration.ofNanos(0L)), // default duration value
                eq(new HashMap<>()) // empty data map
        );
    }

    @Test
    void testDataOverwrite() {
        // Given
        recorder.withData("key", "value1").withData("key", "value2"); // overwrite

        // When
        recorder.record();

        // Then
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("key", "value2"); // should be the last value

        verify(metricsService).recordOperationWithData(eq("test-domain"), eq("test-operation"), eq(false),
                eq(Duration.ofNanos(0L)), eq(expectedData));
    }
}
