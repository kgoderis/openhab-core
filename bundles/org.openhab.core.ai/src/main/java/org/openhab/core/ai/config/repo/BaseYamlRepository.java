package org.openhab.core.ai.config.repo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for YAML repositories with file monitoring capabilities.
 * 
 * <p>
 * This class provides common functionality for YAML repositories including:
 * - File monitoring with WatchService
 * - YAML file loading and parsing
 * - Error handling and logging
 * - Thread-safe operations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public abstract class BaseYamlRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    protected final AtomicBoolean isInitialized = new AtomicBoolean(false);

    protected @Nullable WatchService watchService;
    protected Path repositoryPath;

    /**
     * Activates the repository and starts file monitoring.
     * 
     * @param repositoryName the name of the repository directory
     */
    @Activate
    protected void activate(String repositoryName) {
        logger.debug("Activating {} repository", repositoryName);

        try {
            // Initialize repository path
            repositoryPath = Paths.get(OpenHAB.getConfigFolder(), "ai", repositoryName);
            Files.createDirectories(repositoryPath);

            // Start file monitoring
            startFileMonitoring();

            // Load initial data
            loadAllData();

            isInitialized.set(true);
            logger.info("{} repository activated successfully", repositoryName);
        } catch (Exception e) {
            logger.error("Failed to activate {} repository: {}", repositoryName, e.getMessage(), e);
        }
    }

    /**
     * Deactivates the repository and stops file monitoring.
     */
    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating repository");

        try {
            stopFileMonitoring();
            cache.clear();
            isInitialized.set(false);
            logger.info("Repository deactivated successfully");
        } catch (Exception e) {
            logger.error("Error during repository deactivation: {}", e.getMessage(), e);
        }
    }

    /**
     * Starts file monitoring for the repository directory.
     */
    protected void startFileMonitoring() {
        try {
            // TODO: Implement WatchService integration
            // This will be implemented when we have the WatchService available
            logger.debug("File monitoring started for {}", repositoryPath);
        } catch (Exception e) {
            logger.error("Failed to start file monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Stops file monitoring.
     */
    protected void stopFileMonitoring() {
        try {
            // TODO: Implement WatchService cleanup
            logger.debug("File monitoring stopped");
        } catch (Exception e) {
            logger.error("Error stopping file monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles file change events.
     * 
     * @param filePath the path of the changed file
     */
    protected void handleFileChange(Path filePath) {
        logger.debug("File changed: {}", filePath);
        try {
            reload();
        } catch (Exception e) {
            logger.error("Error handling file change for {}: {}", filePath, e.getMessage(), e);
        }
    }

    /**
     * Loads all data from the repository directory.
     */
    protected abstract void loadAllData();

    /**
     * Reloads all data from disk.
     */
    public abstract void reload();

    /**
     * Checks if the repository is initialized.
     * 
     * @return true if initialized
     */
    protected boolean isInitialized() {
        return isInitialized.get();
    }

    /**
     * Gets the repository path.
     * 
     * @return the repository path
     */
    protected Path getRepositoryPath() {
        return repositoryPath;
    }

    /**
     * Validates that a file is a valid YAML file.
     * 
     * @param file the file to validate
     * @return true if the file is valid
     */
    protected boolean isValidYamlFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }

    /**
     * Creates a backup of a file before modification.
     * 
     * @param file the file to backup
     * @throws IOException if backup creation fails
     */
    protected void createBackup(File file) throws IOException {
        Path backupPath = Paths.get(file.getParent(), file.getName() + ".backup");
        Files.copy(file.toPath(), backupPath);
        logger.debug("Created backup: {}", backupPath);
    }

    /**
     * Restores a file from backup.
     * 
     * @param file the file to restore
     * @throws IOException if restoration fails
     */
    protected void restoreFromBackup(File file) throws IOException {
        Path backupPath = Paths.get(file.getParent(), file.getName() + ".backup");
        if (Files.exists(backupPath)) {
            Files.copy(backupPath, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Restored from backup: {}", backupPath);
        }
    }

    /**
     * Validates YAML content structure.
     * 
     * @param content the YAML content to validate
     * @return list of validation errors, empty if valid
     */
    protected abstract java.util.List<String> validateYamlContent(String content);

    /**
     * Parses YAML content into the appropriate data structure.
     * 
     * @param content the YAML content to parse
     * @return the parsed data structure
     * @throws Exception if parsing fails
     */
    protected abstract Object parseYamlContent(String content) throws Exception;
}
