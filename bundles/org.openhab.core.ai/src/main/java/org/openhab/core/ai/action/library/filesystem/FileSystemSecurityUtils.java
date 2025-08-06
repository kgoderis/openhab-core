package org.openhab.core.ai.action.library.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security utilities for file system operations in openHAB AI actions.
 * 
 * This class provides security validation to ensure file operations only work
 * within the openHAB root folder and its allowed subdirectories.
 * 
 * 
 */
@NonNullByDefault
public class FileSystemSecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemSecurityUtils.class);

    // Allowed base directories for file operations
    private static final List<String> ALLOWED_BASE_PATHS = createAllowedPaths();

    /**
     * Create the list of allowed base paths for file operations.
     * 
     * @return List of allowed base paths
     */
    private static List<String> createAllowedPaths() {
        String configFolder = OpenHAB.getConfigFolder();
        String userDataFolder = OpenHAB.getUserDataFolder();
        String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");

        return List.of(configFolder != null ? configFolder : "conf",
                userDataFolder != null ? userDataFolder : "userdata", tmpDir);
    }

    /**
     * Check if a path is allowed for file operations.
     * 
     * @param path The path to check
     * @return true if the path is allowed, false otherwise
     */
    public static boolean isPathAllowed(String path) {
        if (path == null) {
            logger.warn("Path validation failed: path is null");
            return false;
        }

        try {
            Path normalizedPath = Paths.get(path).toAbsolutePath().normalize();

            boolean allowed = ALLOWED_BASE_PATHS.stream().anyMatch(allowedPath -> {
                try {
                    Path allowedNormalizedPath = Paths.get(allowedPath).toAbsolutePath().normalize();
                    boolean isAllowed = normalizedPath.startsWith(allowedNormalizedPath);

                    if (isAllowed) {
                        logger.debug("Path allowed: {} (within allowed base: {})", normalizedPath,
                                allowedNormalizedPath);
                    } else {
                        logger.debug("Path not allowed: {} (not within allowed base: {})", normalizedPath,
                                allowedNormalizedPath);
                    }

                    return isAllowed;
                } catch (Exception e) {
                    logger.warn("Error checking path against allowed base {}: {}", allowedPath, e.getMessage());
                    return false;
                }
            });

            if (!allowed) {
                logger.warn("Access denied to path: {} (not within any allowed base paths)", path);
            }

            return allowed;
        } catch (Exception e) {
            logger.error("Error validating path {}: {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Validate a path and throw an exception if it's not allowed.
     * 
     * @param path The path to validate
     * @param operation The operation being performed (for error message)
     * @throws SecurityException if the path is not allowed
     */
    public static void validatePath(String path, String operation) throws SecurityException {
        if (!isPathAllowed(path)) {
            String errorMessage = String.format("Access denied for %s operation: %s (not within openHAB root folder)",
                    operation, path);
            logger.error(errorMessage);
            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Get the list of allowed base paths.
     * 
     * @return List of allowed base paths
     */
    public static List<String> getAllowedBasePaths() {
        return List.copyOf(ALLOWED_BASE_PATHS);
    }

    /**
     * Check if a path exists and is accessible.
     * 
     * @param path The path to check
     * @return true if the path exists and is accessible, false otherwise
     */
    public static boolean isPathAccessible(String path) {
        if (!isPathAllowed(path)) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            return java.nio.file.Files.exists(filePath)
                    && (java.nio.file.Files.isReadable(filePath) || java.nio.file.Files.isDirectory(filePath));
        } catch (Exception e) {
            logger.warn("Error checking path accessibility for {}: {}", path, e.getMessage());
            return false;
        }
    }
}
