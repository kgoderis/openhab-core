package org.openhab.core.ai.action.library.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
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
 * AI Action for exporting openHAB configuration files.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ConfigurationExportAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationExportAction.class);

    private static final String ACTION_ID = "openhab.config.export";
    private static final String ACTION_NAME = "Configuration Export";
    private static final String DESCRIPTION = "Exports openHAB configuration files to various formats including ZIP archives, JSON, or individual file exports";
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
        schema.put("properties", Map.of("configType",
                Map.of("type", "string", "enum",
                        List.of("items", "things", "rules", "scripts", "sitemaps", "persistence", "transforms",
                                "services", "all"),
                        "description", "Type of configuration to export", "default", "all"),
                "format",
                Map.of("type", "string", "enum", List.of("zip", "json", "raw"), "description", "Export format",
                        "default", "zip"),
                "includeMetadata",
                Map.of("type", "boolean", "description", "Include file metadata in export", "default", true),
                "outputPath", Map.of("type", "string", "description", "Optional output file path for saved export"),
                "compressionLevel", Map.of("type", "integer", "minimum", 0, "maximum", 9, "description",
                        "ZIP compression level (0-9)", "default", 6)));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("exportId", Map.of("type", "string"), "format", Map.of("type", "string"), "fileCount",
                        Map.of("type", "integer"), "totalSize", Map.of("type", "integer"), "exportTimestamp",
                        Map.of("type", "string"), "filesByType", Map.of("type", "object"), "outputPath",
                        Map.of("type", "string"), "data", Map.of("type", "string")));
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

        String format = (String) parameters.getOrDefault("format", "zip");
        List<String> validFormats = List.of("zip", "json", "raw");
        if (!validFormats.contains(format)) {
            return ActionValidationResult.invalid(
                    List.of("Invalid format: " + format + ". Must be one of: " + String.join(", ", validFormats)));
        }

        Object compressionLevel = parameters.get("compressionLevel");
        if (compressionLevel != null) {
            if (!(compressionLevel instanceof Integer) || (Integer) compressionLevel < 0
                    || (Integer) compressionLevel > 9) {
                return ActionValidationResult.invalid(List.of("compressionLevel must be an integer between 0 and 9"));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing configuration export action with parameters: {}", parameters);

            String configType = (String) parameters.getOrDefault("configType", "all");
            String format = (String) parameters.getOrDefault("format", "zip");
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            String outputPath = (String) parameters.get("outputPath");
            int compressionLevel = (Integer) parameters.getOrDefault("compressionLevel", 6);

            Map<String, Object> exportResult = exportConfiguration(configType, format, includeMetadata, outputPath,
                    compressionLevel);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration export action completed in {}ms", executionTime);

            return ActionResult.success(exportResult, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Configuration export action failed", e);
            throw new ActionException(ACTION_ID, "Failed to export configuration: " + e.getMessage(), e);
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
                .description("Provides comprehensive configuration export capabilities for openHAB")
                .tags(List.of("export", "configuration", "compression", "archiving"))
                .documentation(
                        "Exports openHAB configuration files to various formats with optional compression and metadata")
                .examples(List.of("Export all configurations as ZIP: {\"configType\": \"all\", \"format\": \"zip\"}",
                        "Export specific type as JSON: {\"configType\": \"items\", \"format\": \"json\"}",
                        "Export with custom path: {\"format\": \"zip\", \"outputPath\": \"/exports/config.zip\"}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("export", true, "compression", true, "metadata", true, "multiple_formats", true, "custom_paths",
                true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ConfigurationExportAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationExportAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> exportConfiguration(String configType, String format, boolean includeMetadata,
            String outputPath, int compressionLevel) throws IOException, ActionException {
        Map<String, Object> result = new HashMap<>();

        // Generate export ID
        String exportId = "openhab-export-"
                + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // Collect files to export
        Path confPath = Paths.get(OpenHAB.getConfigFolder());
        List<ExportFile> filesToExport = collectFilesToExport(confPath, configType, includeMetadata);

        if (filesToExport.isEmpty()) {
            throw new ActionException(ACTION_ID, "No configuration files found to export", "NO_FILES_FOUND");
        }

        // Create export based on format
        Object exportData = null;
        String finalOutputPath = null;

        switch (format) {
            case "zip":
                byte[] zipData = createZipExport(filesToExport, compressionLevel);
                exportData = Base64.getEncoder().encodeToString(zipData);

                if (outputPath != null) {
                    Path exportPath = Paths.get(outputPath);
                    Files.createDirectories(exportPath.getParent());
                    Files.write(exportPath, zipData);
                    finalOutputPath = exportPath.toString();
                }
                break;

            case "json":
                Map<String, Object> jsonData = createJsonExport(filesToExport);
                exportData = new com.google.gson.Gson().toJson(jsonData);

                if (outputPath != null) {
                    Path exportPath = Paths.get(outputPath);
                    Files.createDirectories(exportPath.getParent());
                    Files.write(exportPath, exportData.toString().getBytes());
                    finalOutputPath = exportPath.toString();
                }
                break;

            case "raw":
                List<Map<String, Object>> rawData = createRawExport(filesToExport);
                exportData = new com.google.gson.Gson().toJson(rawData);

                if (outputPath != null) {
                    Path exportPath = Paths.get(outputPath);
                    Files.createDirectories(exportPath.getParent());
                    Files.write(exportPath, exportData.toString().getBytes());
                    finalOutputPath = exportPath.toString();
                }
                break;

            default:
                throw new ActionException(ACTION_ID, "Unsupported export format: " + format, "INVALID_FORMAT");
        }

        // Build result
        result.put("exportId", exportId);
        result.put("format", format);
        result.put("fileCount", filesToExport.size());
        result.put("totalSize", filesToExport.stream().mapToLong(f -> f.size != null ? f.size : 0).sum());
        result.put("exportTimestamp", Instant.now().toString());
        result.put("filesByType", getFilesByType(filesToExport));
        result.put("data", exportData);

        if (finalOutputPath != null) {
            result.put("outputPath", finalOutputPath);
        }

        return result;
    }

    private List<ExportFile> collectFilesToExport(Path confPath, String configType, boolean includeMetadata)
            throws IOException {
        List<ExportFile> files = new ArrayList<>();

        if ("all".equals(configType)) {
            // Collect all configuration types
            List<String> allTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                    "transforms", "services");
            for (String type : allTypes) {
                files.addAll(collectFilesOfType(confPath, type, includeMetadata));
            }
        } else {
            files.addAll(collectFilesOfType(confPath, configType, includeMetadata));
        }

        return files;
    }

    private List<ExportFile> collectFilesOfType(Path confPath, String type, boolean includeMetadata)
            throws IOException {
        List<ExportFile> files = new ArrayList<>();
        Path typePath = confPath.resolve(type);

        if (!Files.exists(typePath)) {
            return files;
        }

        try (Stream<Path> paths = Files.walk(typePath, 1)) {
            paths.filter(Files::isRegularFile).filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(path -> {
                        try {
                            ExportFile exportFile = new ExportFile();
                            exportFile.path = type + "/" + path.getFileName().toString();
                            exportFile.name = path.getFileName().toString();
                            exportFile.type = type;
                            exportFile.content = Files.readString(path);
                            exportFile.size = Files.size(path);
                            exportFile.lastModified = Files.getLastModifiedTime(path).toInstant();

                            if (includeMetadata) {
                                exportFile.readable = Files.isReadable(path);
                                exportFile.writable = Files.isWritable(path);
                            }

                            files.add(exportFile);
                        } catch (IOException e) {
                            logger.warn("Failed to collect file for export: {}", path, e);
                        }
                    });
        }

        return files;
    }

    private byte[] createZipExport(List<ExportFile> files, int compressionLevel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.setLevel(compressionLevel);

            for (ExportFile file : files) {
                ZipEntry entry = new ZipEntry(file.path);
                zos.putNextEntry(entry);
                zos.write(file.content.getBytes());
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }

    private Map<String, Object> createJsonExport(List<ExportFile> files) {
        Map<String, Object> export = new HashMap<>();
        export.put("exportTimestamp", Instant.now().toString());
        export.put("fileCount", files.size());
        export.put("totalSize", files.stream().mapToLong(f -> f.size != null ? f.size : 0).sum());
        export.put("filesByType", getFilesByType(files));

        Map<String, Object> filesData = new HashMap<>();
        for (ExportFile file : files) {
            filesData.put(file.path, file.toMap());
        }
        export.put("files", filesData);

        return export;
    }

    private List<Map<String, Object>> createRawExport(List<ExportFile> files) {
        List<Map<String, Object>> export = new ArrayList<>();
        for (ExportFile file : files) {
            export.add(file.toMap());
        }
        return export;
    }

    private Map<String, Integer> getFilesByType(List<ExportFile> files) {
        Map<String, Integer> filesByType = new HashMap<>();
        for (ExportFile file : files) {
            filesByType.merge(file.type, 1, Integer::sum);
        }
        return filesByType;
    }

    private static class ExportFile {
        String path = "";
        String name = "";
        String type = "";
        String content = "";
        Long size;
        Instant lastModified;
        Boolean readable;
        Boolean writable;

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("path", path);
            map.put("name", name);
            map.put("type", type);
            map.put("content", content);
            map.put("size", size);
            map.put("lastModified", lastModified != null ? lastModified.toString() : null);
            map.put("readable", readable);
            map.put("writable", writable);
            return map;
        }
    }
}
