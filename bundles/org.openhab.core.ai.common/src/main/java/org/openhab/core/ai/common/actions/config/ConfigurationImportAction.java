package org.openhab.core.ai.common.actions.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for importing openHAB configuration files.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class ConfigurationImportAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationImportAction.class);

    private static final String ACTION_ID = "openhab.config.import";
    private static final String ACTION_NAME = "Configuration Import";
    private static final String DESCRIPTION = "Imports openHAB configuration files from various formats including ZIP archives, JSON exports, or individual file imports";
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
                Map.of("source",
                        Map.of("type", "string", "enum", List.of("file", "data", "url"), "description",
                                "Import source type", "default", "data"),
                        "format",
                        Map.of("type", "string", "enum", List.of("zip", "json", "raw"), "description", "Import format",
                                "default", "zip"),
                        "sourcePath", Map.of("type", "string", "description", "File path for file-based imports"),
                        "sourceData",
                        Map.of("type", "string", "description", "Base64 encoded data for data-based imports"),
                        "sourceUrl", Map.of("type", "string", "description", "URL for URL-based imports"), "mergeMode",
                        Map.of("type", "string", "enum", List.of("overwrite", "merge", "skip"), "description",
                                "How to handle existing files", "default", "merge"),
                        "createBackup",
                        Map.of("type", "boolean", "description", "Create backup before importing", "default", true),
                        "validateFiles",
                        Map.of("type", "boolean", "description", "Validate imported configuration files", "default",
                                true),
                        "targetTypes", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                                "Specific configuration types to import (if not specified, imports all)")));
        schema.put("required", List.of("source", "format"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("importId", Map.of("type", "string"), "processedFiles", Map.of("type", "integer"),
                        "createdFiles", Map.of("type", "integer"), "updatedFiles", Map.of("type", "integer"),
                        "skippedFiles", Map.of("type", "integer"), "errorCount", Map.of("type", "integer"),
                        "importTimestamp", Map.of("type", "string"), "backupPath", Map.of("type", "string"),
                        "validationErrors", Map.of("type", "array"), "importDetails", Map.of("type", "array")));
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        String source = (String) parameters.get("source");
        if (source == null) {
            return AIActionValidationResult.invalid(List.of("source is required"));
        }

        List<String> validSources = List.of("file", "data", "url");
        if (!validSources.contains(source)) {
            return AIActionValidationResult.invalid(
                    List.of("Invalid source: " + source + ". Must be one of: " + String.join(", ", validSources)));
        }

        String format = (String) parameters.get("format");
        if (format == null) {
            return AIActionValidationResult.invalid(List.of("format is required"));
        }

        List<String> validFormats = List.of("zip", "json", "raw");
        if (!validFormats.contains(format)) {
            return AIActionValidationResult.invalid(
                    List.of("Invalid format: " + format + ". Must be one of: " + String.join(", ", validFormats)));
        }

        // Validate source-specific parameters
        switch (source) {
            case "file":
                if (parameters.get("sourcePath") == null) {
                    return AIActionValidationResult.invalid(List.of("sourcePath is required when source is 'file'"));
                }
                break;
            case "data":
                if (parameters.get("sourceData") == null) {
                    return AIActionValidationResult.invalid(List.of("sourceData is required when source is 'data'"));
                }
                break;
            case "url":
                if (parameters.get("sourceUrl") == null) {
                    return AIActionValidationResult.invalid(List.of("sourceUrl is required when source is 'url'"));
                }
                break;
        }

        String mergeMode = (String) parameters.getOrDefault("mergeMode", "merge");
        List<String> validMergeModes = List.of("overwrite", "merge", "skip");
        if (!validMergeModes.contains(mergeMode)) {
            return AIActionValidationResult.invalid(List
                    .of("Invalid mergeMode: " + mergeMode + ". Must be one of: " + String.join(", ", validMergeModes)));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing configuration import action with parameters: {}", parameters);

            String source = (String) parameters.get("source");
            String format = (String) parameters.get("format");
            String sourcePath = (String) parameters.get("sourcePath");
            String sourceData = (String) parameters.get("sourceData");
            String sourceUrl = (String) parameters.get("sourceUrl");
            String mergeMode = (String) parameters.getOrDefault("mergeMode", "merge");
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", true);
            boolean validateFiles = (Boolean) parameters.getOrDefault("validateFiles", true);
            @SuppressWarnings("unchecked")
            List<String> targetTypes = (List<String>) parameters.get("targetTypes");

            Map<String, Object> importResult = importConfiguration(source, format, sourcePath, sourceData, sourceUrl,
                    mergeMode, createBackup, validateFiles, targetTypes);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration import action completed in {}ms", executionTime);

            return AIActionResult.success(importResult, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Configuration import action failed", e);
            throw new AIActionException(ACTION_ID, "Failed to import configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Provides comprehensive configuration import capabilities for openHAB")
                .tags(List.of("import", "configuration", "restore", "migration"))
                .documentation(
                        "Imports openHAB configuration files from various formats with validation and backup options")
                .examples(List.of(
                        "Import from ZIP file: {\"source\": \"file\", \"format\": \"zip\", \"sourcePath\": \"/backups/config.zip\"}",
                        "Import from data: {\"source\": \"data\", \"format\": \"json\", \"sourceData\": \"base64data...\"}",
                        "Import with backup: {\"source\": \"file\", \"format\": \"zip\", \"sourcePath\": \"/backups/config.zip\", \"createBackup\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("import", true, "validation", true, "backup", true, "multiple_formats", true, "merge_modes", true,
                "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ConfigurationImportAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationImportAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> importConfiguration(String source, String format, String sourcePath, String sourceData,
            String sourceUrl, String mergeMode, boolean createBackup, boolean validateFiles, List<String> targetTypes)
            throws IOException, AIActionException {
        Map<String, Object> result = new HashMap<>();

        // Generate import ID
        String importId = "openhab-import-"
                + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // Get import data
        byte[] importData = getImportData(source, sourcePath, sourceData, sourceUrl);

        // Parse import data
        List<ImportFile> files = parseImportData(importData, format);

        if (files.isEmpty()) {
            throw new AIActionException(ACTION_ID, "No files found in import data", "NO_FILES_FOUND");
        }

        // Filter by target types if specified
        if (targetTypes != null && !targetTypes.isEmpty()) {
            files = files.stream().filter(file -> targetTypes.contains(file.type)).toList();
            if (files.isEmpty()) {
                throw new AIActionException(ACTION_ID, "No files match the specified target types",
                        "NO_MATCHING_FILES");
            }
        }

        // Create backup if requested
        String backupPath = null;
        Path confPath = Paths.get(OpenHAB.getConfigFolder());
        if (createBackup) {
            backupPath = createBackupBeforeImport(confPath);
        }

        // Import files
        ImportStats stats = importFiles(confPath, files, mergeMode, validateFiles);

        // Build result
        result.put("importId", importId);
        result.put("processedFiles", stats.processed);
        result.put("createdFiles", stats.created);
        result.put("updatedFiles", stats.updated);
        result.put("skippedFiles", stats.skipped);
        result.put("errorCount", stats.errors);
        result.put("importTimestamp", Instant.now().toString());
        result.put("validationErrors", stats.validationErrors);
        result.put("importDetails", stats.details);

        if (backupPath != null) {
            result.put("backupPath", backupPath);
        }

        return result;
    }

    private byte[] getImportData(String source, String sourcePath, String sourceData, String sourceUrl)
            throws IOException, AIActionException {
        switch (source) {
            case "file":
                if (sourcePath == null) {
                    throw new AIActionException(ACTION_ID, "sourcePath is required for file-based imports",
                            "MISSING_PARAMETER");
                }
                Path path = Paths.get(sourcePath);
                if (!Files.exists(path)) {
                    throw new AIActionException(ACTION_ID, "Import file not found: " + sourcePath, "FILE_NOT_FOUND");
                }
                return Files.readAllBytes(path);

            case "data":
                if (sourceData == null) {
                    throw new AIActionException(ACTION_ID, "sourceData is required for data-based imports",
                            "MISSING_PARAMETER");
                }
                try {
                    return Base64.getDecoder().decode(sourceData);
                } catch (IllegalArgumentException e) {
                    throw new AIActionException(ACTION_ID, "Invalid base64 data", "INVALID_DATA");
                }

            case "url":
                if (sourceUrl == null) {
                    throw new AIActionException(ACTION_ID, "sourceUrl is required for URL-based imports",
                            "MISSING_PARAMETER");
                }
                // For now, we'll throw an exception as URL downloading requires additional dependencies
                throw new AIActionException(ACTION_ID, "URL-based imports are not yet supported", "NOT_IMPLEMENTED");

            default:
                throw new AIActionException(ACTION_ID, "Unsupported source type: " + source, "INVALID_SOURCE");
        }
    }

    private List<ImportFile> parseImportData(byte[] data, String format) throws IOException, AIActionException {
        switch (format) {
            case "zip":
                return parseZipData(data);
            case "json":
                return parseJsonData(data);
            case "raw":
                return parseRawData(data);
            default:
                throw new AIActionException(ACTION_ID, "Unsupported format: " + format, "INVALID_FORMAT");
        }
    }

    private List<ImportFile> parseZipData(byte[] data) throws IOException {
        List<ImportFile> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String path = entry.getName();
                    if (path.contains("/")) {
                        String[] parts = path.split("/", 2);
                        String type = parts[0];
                        String name = parts[1];

                        // Read content
                        byte[] contentBytes = zis.readAllBytes();
                        String content = new String(contentBytes);

                        ImportFile file = new ImportFile();
                        file.path = path;
                        file.name = name;
                        file.type = type;
                        file.content = content;
                        file.lastModified = Instant.ofEpochMilli(entry.getLastModifiedTime().toMillis());

                        files.add(file);
                    }
                }
                zis.closeEntry();
            }
        }

        return files;
    }

    private List<ImportFile> parseJsonData(byte[] data) throws AIActionException {
        try {
            String jsonString = new String(data);
            Map<String, Object> json = new com.google.gson.Gson().fromJson(jsonString, Map.class);

            List<ImportFile> files = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> filesData = (Map<String, Object>) json.get("files");

            if (filesData != null) {
                for (Map.Entry<String, Object> entry : filesData.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fileData = (Map<String, Object>) entry.getValue();

                    ImportFile file = new ImportFile();
                    file.path = entry.getKey();
                    file.name = (String) fileData.get("name");
                    file.type = (String) fileData.get("type");
                    file.content = (String) fileData.get("content");

                    String lastModifiedStr = (String) fileData.get("lastModified");
                    if (lastModifiedStr != null) {
                        file.lastModified = Instant.parse(lastModifiedStr);
                    }

                    files.add(file);
                }
            }

            return files;
        } catch (Exception e) {
            throw new AIActionException(ACTION_ID, "Failed to parse JSON data: " + e.getMessage(), "PARSE_ERROR");
        }
    }

    private List<ImportFile> parseRawData(byte[] data) throws AIActionException {
        try {
            String jsonString = new String(data);
            List<Map<String, Object>> rawData = new com.google.gson.Gson().fromJson(jsonString, List.class);

            List<ImportFile> files = new ArrayList<>();
            for (Map<String, Object> fileData : rawData) {
                ImportFile file = new ImportFile();
                file.path = (String) fileData.get("path");
                file.name = (String) fileData.get("name");
                file.type = (String) fileData.get("type");
                file.content = (String) fileData.get("content");

                String lastModifiedStr = (String) fileData.get("lastModified");
                if (lastModifiedStr != null) {
                    file.lastModified = Instant.parse(lastModifiedStr);
                }

                files.add(file);
            }

            return files;
        } catch (Exception e) {
            throw new AIActionException(ACTION_ID, "Failed to parse raw data: " + e.getMessage(), "PARSE_ERROR");
        }
    }

    private String createBackupBeforeImport(Path confPath) throws IOException {
        String timestamp = Instant.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupPath = confPath.resolve("backups").resolve("pre-import-backup-" + timestamp);

        if (!Files.exists(backupPath.getParent())) {
            Files.createDirectories(backupPath.getParent());
        }

        copyDirectory(confPath, backupPath);
        return backupPath.toString();
    }

    private void copyDirectory(Path source, Path destination) throws IOException {
        if (Files.isDirectory(source)) {
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }

            try (var stream = Files.list(source)) {
                for (Path child : stream.toList()) {
                    if (!child.getFileName().toString().equals("backups")) {
                        copyDirectory(child, destination.resolve(child.getFileName()));
                    }
                }
            }
        } else {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ImportStats importFiles(Path confPath, List<ImportFile> files, String mergeMode, boolean validateFiles)
            throws IOException {
        ImportStats stats = new ImportStats();

        for (ImportFile file : files) {
            try {
                boolean success = importSingleFile(confPath, file, mergeMode, validateFiles, stats);
                if (success) {
                    stats.processed++;
                } else {
                    stats.skipped++;
                }
            } catch (Exception e) {
                logger.error("Failed to import file: {}", file.path, e);
                stats.errors++;
                String errorMessage = e.getMessage();
                stats.details.add(Map.of("file", file.path, "status", "error", "error",
                        errorMessage != null ? errorMessage : "Unknown error"));
            }
        }

        return stats;
    }

    private boolean importSingleFile(Path confPath, ImportFile file, String mergeMode, boolean validateFiles,
            ImportStats stats) throws IOException {
        Path targetPath = confPath.resolve(file.path);
        Path targetDir = targetPath.getParent();

        // Create directory if it doesn't exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Check if file exists
        boolean fileExists = Files.exists(targetPath);

        // Handle merge mode
        if (fileExists) {
            switch (mergeMode) {
                case "skip":
                    stats.details.add(Map.of("file", file.path, "status", "skipped", "reason",
                            "File exists and merge mode is skip"));
                    return false;

                case "merge":
                    // For now, we'll overwrite. In a real implementation, you might want to merge content
                    break;

                case "overwrite":
                    // Continue with overwrite
                    break;
            }
        }

        // Validate file content if requested
        if (validateFiles) {
            List<String> validationErrors = validateFileContent(file);
            if (!validationErrors.isEmpty()) {
                stats.validationErrors.addAll(validationErrors);
                stats.details.add(Map.of("file", file.path, "status", "validation_error", "errors", validationErrors));
                return false;
            }
        }

        // Write file
        Files.write(targetPath, file.content.getBytes());

        // Update stats
        if (fileExists) {
            stats.updated++;
            stats.details.add(Map.of("file", file.path, "status", "updated"));
        } else {
            stats.created++;
            stats.details.add(Map.of("file", file.path, "status", "created"));
        }

        return true;
    }

    private List<String> validateFileContent(ImportFile file) {
        List<String> errors = new ArrayList<>();

        // Basic validation based on file type
        switch (file.type) {
            case "items":
                if (!file.content.contains(" ") && !file.content.startsWith("Group")) {
                    errors.add("Invalid item syntax: missing type");
                }
                break;

            case "things":
                if (!file.content.contains("Thing") && !file.content.contains("Bridge")) {
                    errors.add("Invalid thing syntax: missing Thing/Bridge keyword");
                }
                break;

            case "rules":
                if (!file.content.contains("rule") || !file.content.contains("when")) {
                    errors.add("Invalid rule syntax: missing rule or when clause");
                }
                break;
        }

        return errors;
    }

    private static class ImportFile {
        String path = "";
        String name = "";
        String type = "";
        String content = "";
        Instant lastModified;
    }

    private static class ImportStats {
        int processed = 0;
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int errors = 0;
        List<String> validationErrors = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();
    }
}
