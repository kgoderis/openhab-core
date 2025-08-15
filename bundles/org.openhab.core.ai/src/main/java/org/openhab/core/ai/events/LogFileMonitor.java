package org.openhab.core.ai.events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Log file monitor for real-time log ingestion.
 *
 * Extracted from {@link LogIngestionPipeline}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class LogFileMonitor {
    private final LogIngestionPipeline owner;
    private final String fileName;
    private final Path filePath;
    private final List<String> lineBuffer = new ArrayList<>();
    private volatile boolean isRunning = false;
    private @Nullable Thread monitorThread;
    private long lastPosition = 0;

    public LogFileMonitor(LogIngestionPipeline owner, String fileName, Path filePath) {
        this.owner = owner;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        monitorThread = new Thread(this::monitorFile, "LogMonitor-" + fileName);
        monitorThread.setDaemon(true);
        monitorThread.start();
        LogIngestionPipeline.logger.debug("Started monitoring log file: {}", fileName);
    }

    public void stop() {
        isRunning = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
        LogIngestionPipeline.logger.debug("Stopped monitoring log file: {}", fileName);
    }

    private void monitorFile() {
        try {
            lastPosition = Files.size(filePath);
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    long currentSize = Files.size(filePath);
                    if (currentSize > lastPosition) {
                        List<String> newLines = readNewLines();
                        if (!newLines.isEmpty()) {
                            owner.processLogLines(newLines);
                        }
                    }
                    Thread.sleep(owner.getPollInterval().toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    LogIngestionPipeline.logger.debug("Error reading log file {}: {}", fileName, e.getMessage());
                    Thread.sleep(owner.getPollInterval().toMillis());
                }
            }
        } catch (Exception e) {
            LogIngestionPipeline.logger.error("Error in log file monitor for {}", fileName, e);
        }
    }

    private List<String> readNewLines() throws IOException {
        List<String> newLines = new ArrayList<>();
        try (var reader = Files.newBufferedReader(filePath)) {
            reader.skip(lastPosition);
            String line;
            while ((line = reader.readLine()) != null) {
                newLines.add(line);
            }
            lastPosition = Files.size(filePath);
        }
        return newLines;
    }
}
