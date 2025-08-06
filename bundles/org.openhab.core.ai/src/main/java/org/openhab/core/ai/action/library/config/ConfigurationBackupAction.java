package org.openhab.core.ai.action.library.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for creating backups of openHAB configuration files.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ConfigurationBackupAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBackupAction.class);

    private static final String ACTION_ID = "openhab.config.backup";
    private static final String ACTION_NAME = "Configuration Backup";
    private static final String DESCRIPTION = "Creates comprehensive backups of openHAB configuration files with automatic timestamping and compression";
    private static final String CATEGORY = "config";
    private static final String VERSION = "1.0.0";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("configType", Map.of("type", "string", "enum",
                        List.of("items", "things", "rules", "scripts", "sitemaps", "persistence", "transforms",
                                "services", "all"),
                        "description", "Type of configuration to backup", "default", "all"), "backupFormat",
                        Map.of("type", "string", "enum", List.of("zip", "directory"), "description", "Backup format",
                                "default", "zip"),
                        "backupLocation",
                        Map.of("type", "string", "enum", List.of("default", "custom"), "description",
                                "Backup location type", "default", "default"),
                        "customPath",
                        Map.of("type", "string", "description", "Custom backup path (when backupLocation is custom)"),
                        "includeMetadata",
                        Map.of("type", "boolean", "description", "Include detailed backup metadata", "default", true),
                        "compressionLevel",
                        Map.of("type", "integer", "minimum", 0, "maximum", 9, "description",
                                "ZIP compression level (0-9)", "default", 6),
                        "retainCount",
                        Map.of("type", "integer", "minimum", 1, "maximum", 100, "description",
                                "Number of backup copies to retain", "default", 10),
                        "description", Map.of("type", "string", "description", "Optional backup description")));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("backupId", Map.of("type", "string"), "backupPath", Map.of("type", "string"), "backupSize",
                        Map.of("type", "integer"), "fileCount", Map.of("type", "integer"), "backupTimestamp",
                        Map.of("type", "string"), "filesByType", Map.of("type", "object"), "compressionRatio",
                        Map.of("type", "number"), "cleanedUpCount", Map.of("type", "integer")));
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        String configType = (String) parameters.getOrDefault("configType", "all");
        List<String> validTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                "transforms", "services", "all");

        if (!validTypes.contains(configType)) {
            return ActionValidationResult.invalid(List
                    .of("Invalid configType: " + configType + ". Must be one of: " + String.join(", ", validTypes)));
        }

        String backupFormat = (String) parameters.getOrDefault("backupFormat", "zip");
        List<String> validFormats = List.of("zip", "directory");
        if (!validFormats.contains(backupFormat)) {
            return ActionValidationResult.invalid(List.of(
                    "Invalid backupFormat: " + backupFormat + ". Must be one of: " + String.join(", ", validFormats)));
        }

        String backupLocation = (String) parameters.getOrDefault("backupLocation", "default");
        List<String> validLocations = List.of("default", "custom");
        if (!validLocations.contains(backupLocation)) {
            return ActionValidationResult.invalid(List.of("Invalid backupLocation: " + backupLocation
                    + ". Must be one of: " + String.join(", ", validLocations)));
        }

        if ("custom".equals(backupLocation)
                && (parameters.get("customPath") == null || ((String) parameters.get("customPath")).trim().isEmpty())) {
            return ActionValidationResult.invalid(List.of("customPath is required when backupLocation is 'custom'"));
        }

        Integer compressionLevel = (Integer) parameters.getOrDefault("compressionLevel", 6);
        if (compressionLevel < 0 || compressionLevel > 9) {
            return ActionValidationResult.invalid(List.of("compressionLevel must be between 0 and 9"));
        }

        Integer retainCount = (Integer) parameters.getOrDefault("retainCount", 10);
        if (retainCount < 1 || retainCount > 100) {
            return ActionValidationResult.invalid(List.of("retainCount must be between 1 and 100"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing configuration backup action with parameters: {}", parameters);

            String configType = (String) parameters.getOrDefault("configType", "all");
            String backupFormat = (String) parameters.getOrDefault("backupFormat", "zip");
            String backupLocation = (String) parameters.getOrDefault("backupLocation", "default");
            String customPath = (String) parameters.get("customPath");
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            int compressionLevel = (Integer) parameters.getOrDefault("compressionLevel", 6);
            int retainCount = (Integer) parameters.getOrDefault("retainCount", 10);
            String description = (String) parameters.get("description");

            Map<String, Object> backupResult = createBackup(configType, backupFormat, backupLocation, customPath,
                    includeMetadata, compressionLevel, retainCount, description);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration backup action completed in {}ms", executionTime);

            return ActionResult.success(backupResult, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Configuration backup action failed", e);
            throw new ActionException(ACTION_ID, "Failed to create backup: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Provides comprehensive configuration backup capabilities for openHAB")
                .tags(List.of("backup", "configuration", "compression", "archiving"))
                .documentation(
                        "Creates timestamped backups of openHAB configuration files with optional compression and metadata")
                .examples(List.of("Backup all configurations: {\"configType\": \"all\", \"backupFormat\": \"zip\"}",
                        "Backup specific type: {\"configType\": \"items\", \"backupFormat\": \"directory\"}",
                        "Custom location backup: {\"backupLocation\": \"custom\", \"customPath\": \"/backups\", \"description\": \"Monthly backup\"}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("backup", true, "compression", true, "metadata", true, "retention", true, "custom_locations",
                true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ConfigurationBackupAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationBackupAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> createBackup(String configType, String backupFormat, String backupLocation,
            String customPath, boolean includeMetadata, int compressionLevel, int retainCount, String description)
            throws IOException, ActionException {
        Map<String, Object> result = new HashMap<>();

        // Generate backup ID
        String backupId = "openhab-backup-"
                + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        if (description != null && !description.trim().isEmpty()) {
            backupId += "-" + description.replaceAll("[^a-zA-Z0-9_-]", "_");
        }

        // Get backup destination
        Path backupDestination = getBackupDestination(backupLocation, customPath, backupId, backupFormat);

        // Collect files to backup
        Path confPath = Paths.get(OpenHAB.getConfigFolder());
        List<BackupFile> filesToBackup = collectFilesToBackup(confPath, configType);

        if (filesToBackup.isEmpty()) {
            throw new ActionException(ACTION_ID, "No configuration files found to backup", "NO_FILES_FOUND");
        }

        // Create backup
        long originalSize = filesToBackup.stream().mapToLong(f -> f.size).sum();
        long backupSize = 0;

        if ("zip".equals(backupFormat)) {
            byte[] zipData = createZipBackup(filesToBackup, includeMetadata, compressionLevel, description);
            Files.write(backupDestination, zipData);
            backupSize = zipData.length;
        } else {
            createDirectoryBackup(confPath, backupDestination, filesToBackup, includeMetadata, description);
            backupSize = calculateDirectorySize(backupDestination);
        }

        // Cleanup old backups
        int cleanedUpCount = cleanupOldBackups(backupDestination.getParent(), backupFormat, retainCount);

        // Calculate compression ratio
        double compressionRatio = originalSize > 0 ? (double) backupSize / originalSize : 1.0;

        // Build result
        result.put("backupId", backupId);
        result.put("backupPath", backupDestination.toString());
        result.put("backupSize", backupSize);
        result.put("fileCount", filesToBackup.size());
        result.put("backupTimestamp", Instant.now().toString());
        result.put("filesByType", getFilesByType(filesToBackup));
        result.put("compressionRatio", compressionRatio);
        result.put("cleanedUpCount", cleanedUpCount);

        return result;
    }

    private Path getBackupDestination(String backupLocation, String customPath, String backupId, String backupFormat)
            throws IOException {
        Path backupDir;
        String extension = "zip".equals(backupFormat) ? ".zip" : "";

        if ("custom".equals(backupLocation)) {
            backupDir = Paths.get(customPath);
        } else {
            // Default backup location
            backupDir = Paths.get(OpenHAB.getConfigFolder()).resolve("backups");
        }

        // Create backup directory if it doesn't exist
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        return backupDir.resolve(backupId + extension);
    }

    private List<BackupFile> collectFilesToBackup(Path confPath, String configType) throws IOException {
        List<BackupFile> files = new ArrayList<>();

        if ("all".equals(configType)) {
            // Collect all configuration types
            List<String> allTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                    "transforms", "services");
            for (String type : allTypes) {
                files.addAll(collectFilesOfType(confPath, type));
            }
        } else {
            files.addAll(collectFilesOfType(confPath, configType));
        }

        return files;
    }

    private List<BackupFile> collectFilesOfType(Path confPath, String type) throws IOException {
        List<BackupFile> files = new ArrayList<>();
        Path typePath = confPath.resolve(type);

        if (!Files.exists(typePath)) {
            return files;
        }

        try (Stream<Path> paths = Files.walk(typePath, 1)) {
            paths.filter(Files::isRegularFile).filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(path -> {
                        try {
                            BackupFile backupFile = new BackupFile();
                            backupFile.path = type + "/" + path.getFileName().toString();
                            backupFile.absolutePath = path;
                            backupFile.name = path.getFileName().toString();
                            backupFile.type = type;
                            backupFile.size = Files.size(path);
                            backupFile.lastModified = Files.getLastModifiedTime(path).toInstant();
                            files.add(backupFile);
                        } catch (IOException e) {
                            logger.warn("Failed to collect file for backup: {}", path, e);
                        }
                    });
        }

        return files;
    }

    private byte[] createZipBackup(List<BackupFile> files, boolean includeMetadata, int compressionLevel,
            String description) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.setLevel(compressionLevel);

            // Add metadata file if requested
            if (includeMetadata) {
                ZipEntry metadataEntry = new ZipEntry("backup-metadata.json");
                zos.putNextEntry(metadataEntry);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("backupTimestamp", Instant.now().toString());
                metadata.put("description", description);
                metadata.put("fileCount", files.size());
                metadata.put("totalSize", files.stream().mapToLong(f -> f.size).sum());
                metadata.put("filesByType", getFilesByType(files));

                String metadataJson = new com.google.gson.Gson().toJson(metadata);
                zos.write(metadataJson.getBytes());
                zos.closeEntry();
            }

            // Add configuration files
            for (BackupFile file : files) {
                ZipEntry entry = new ZipEntry(file.path);
                zos.putNextEntry(entry);
                Files.copy(file.absolutePath, zos);
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }

    private void createDirectoryBackup(Path confPath, Path backupDestination, List<BackupFile> files,
            boolean includeMetadata, String description) throws IOException {
        // Create backup directory
        Files.createDirectories(backupDestination);

        // Copy files
        for (BackupFile file : files) {
            Path targetPath = backupDestination.resolve(file.path);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.absolutePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Add metadata file if requested
        if (includeMetadata) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("backupTimestamp", Instant.now().toString());
            metadata.put("description", description);
            metadata.put("fileCount", files.size());
            metadata.put("totalSize", files.stream().mapToLong(f -> f.size).sum());
            metadata.put("filesByType", getFilesByType(files));

            String metadataJson = new com.google.gson.Gson().toJson(metadata);
            Files.write(backupDestination.resolve("backup-metadata.json"), metadataJson.getBytes());
        }
    }

    private long calculateDirectorySize(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        }
    }

    private int cleanupOldBackups(Path backupDir, String backupFormat, int retainCount) throws IOException {
        if (!Files.exists(backupDir)) {
            return 0;
        }

        String extension = "zip".equals(backupFormat) ? ".zip" : "";
        List<Path> backupFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.list(backupDir)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(extension))
                    .filter(path -> path.getFileName().toString().startsWith("openhab-backup-")).sorted()
                    .forEach(backupFiles::add);
        }

        int filesToDelete = Math.max(0, backupFiles.size() - retainCount);
        int deletedCount = 0;

        for (int i = 0; i < filesToDelete; i++) {
            try {
                Files.delete(backupFiles.get(i));
                deletedCount++;
            } catch (IOException e) {
                logger.warn("Failed to delete old backup: {}", backupFiles.get(i), e);
            }
        }

        return deletedCount;
    }

    private Map<String, Integer> getFilesByType(List<BackupFile> files) {
        Map<String, Integer> filesByType = new HashMap<>();
        for (BackupFile file : files) {
            filesByType.merge(file.type, 1, Integer::sum);
        }
        return filesByType;
    }

    private static class BackupFile {
        String path = "";
        Path absolutePath;
        String name = "";
        String type = "";
        long size = 0;
        Instant lastModified;
    }
}
