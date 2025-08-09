package org.openhab.core.ai.action.library.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.service.component.annotations.Component;

/**
 * AI Action for listing openHAB configuration files and their metadata.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ConfigurationListAction implements Action {

    private static final String ACTION_ID = "openhab.config.list";
    private static final String ACTION_NAME = "Configuration List";
    private static final String DESCRIPTION = "Lists all openHAB configuration files with metadata including size, modification dates, and file counts by type";
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
                        "description", "Type of configuration to list", "default", "all"), "includeEmpty",
                        Map.of("type", "boolean", "description", "Include directories even if they are empty",
                                "default", false),
                        "sortBy",
                        Map.of("type", "string", "enum", List.of("name", "size", "modified", "type"), "description",
                                "Sort files by specified criteria", "default", "name"),
                        "sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                                "Sort order", "default", "asc")));
        schema.put("additionalProperties", false);
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

        String sortBy = (String) parameters.getOrDefault("sortBy", "name");
        List<String> validSortBy = List.of("name", "size", "modified", "type");
        if (!validSortBy.contains(sortBy)) {
            return ActionValidationResult.invalid(
                    List.of("Invalid sortBy: " + sortBy + ". Must be one of: " + String.join(", ", validSortBy)));
        }

        String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            return ActionValidationResult
                    .invalid(List.of("Invalid sortOrder: " + sortOrder + ". Must be 'asc' or 'desc'"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));
        properties.put("configType", Map.of("type", "string", "description", "Type of configuration listed"));
        properties.put("configurations", Map.of("type", "array", "description", "List of configuration files"));
        properties.put("totalFiles", Map.of("type", "integer", "description", "Total number of files"));
        properties.put("confDirectory", Map.of("type", "string", "description", "Configuration directory path"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String configType = (String) parameters.getOrDefault("configType", "all");
            boolean includeEmpty = (Boolean) parameters.getOrDefault("includeEmpty", false);
            String sortBy = (String) parameters.getOrDefault("sortBy", "name");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");

            Map<String, Object> result = listConfigurations(configType, includeEmpty, sortBy, sortOrder);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to list configurations: " + e.getMessage(), e);
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
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB")
                .tags(List.of("configuration", "files", "metadata")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("configuration_listing", true);
        capabilities.put("file_metadata", true);
        capabilities.put("directory_scanning", true);
        capabilities.put("file_sorting", true);
        capabilities.put("size_formatting", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return true; // Always ready as this only reads file system
    }

    private Map<String, Object> listConfigurations(String configType, boolean includeEmpty, String sortBy,
            String sortOrder) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("configType", configType);

        // Use the proper openHAB configuration directory
        String confDir = OpenHAB.getConfigFolder();
        Path confPath = Paths.get(confDir);

        if (!Files.exists(confPath)) {
            result.put("error", "Configuration directory not found: " + confDir);
            result.put("configurations", List.of());
            result.put("summary", Map.of());
            return result;
        }

        List<Map<String, Object>> allFiles = new ArrayList<>();
        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Long> typeSizes = new HashMap<>();

        if ("all".equals(configType)) {
            String[] types = { "items", "things", "rules", "scripts", "sitemaps", "persistence", "transform",
                    "services" };
            for (String type : types) {
                List<Map<String, Object>> typeFiles = listFilesOfType(confPath, type, includeEmpty);
                allFiles.addAll(typeFiles);

                int count = typeFiles.size();
                long totalSize = typeFiles.stream().mapToLong(f -> (Long) f.getOrDefault("size", 0L)).sum();

                typeCounts.put(type, count);
                typeSizes.put(type, totalSize);
            }
        } else {
            String dirName = "transforms".equals(configType) ? "transform" : configType;
            List<Map<String, Object>> typeFiles = listFilesOfType(confPath, dirName, includeEmpty);
            allFiles.addAll(typeFiles);

            int count = typeFiles.size();
            long totalSize = typeFiles.stream().mapToLong(f -> (Long) f.getOrDefault("size", 0L)).sum();

            typeCounts.put(configType, count);
            typeSizes.put(configType, totalSize);
        }

        // Sort the results
        sortFiles(allFiles, sortBy, sortOrder);

        result.put("configurations", allFiles);
        result.put("totalFiles", allFiles.size());
        result.put("confDirectory", confDir);

        // Create summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalFiles", allFiles.size());
        summary.put("totalSize",
                formatBytes(allFiles.stream().mapToLong(f -> (Long) f.getOrDefault("size", 0L)).sum()));
        summary.put("typeBreakdown", createTypeBreakdown(typeCounts, typeSizes));
        result.put("summary", summary);

        return result;
    }

    private List<Map<String, Object>> listFilesOfType(Path confPath, String type, boolean includeEmpty)
            throws IOException {
        List<Map<String, Object>> files = new ArrayList<>();
        Path typeDir = confPath.resolve(type);

        if (!Files.exists(typeDir)) {
            if (includeEmpty) {
                Map<String, Object> emptyDir = new HashMap<>();
                emptyDir.put("type", type);
                emptyDir.put("name", "[Directory not found]");
                emptyDir.put("path", typeDir.toString());
                emptyDir.put("exists", false);
                files.add(emptyDir);
            }
            return files;
        }

        if (!Files.isDirectory(typeDir)) {
            return files;
        }

        try (Stream<Path> fileStream = Files.list(typeDir)) {
            boolean hasFiles = false;

            for (Path file : fileStream.filter(Files::isRegularFile).toList()) {
                hasFiles = true;
                try {
                    Map<String, Object> fileInfo = new HashMap<>();
                    String fileName = file.getFileName().toString();

                    fileInfo.put("type", type);
                    fileInfo.put("name", fileName);
                    fileInfo.put("path", file.toString());
                    fileInfo.put("size", Files.size(file));
                    fileInfo.put("sizeFormatted", formatBytes(Files.size(file)));
                    fileInfo.put("lastModified", Files.getLastModifiedTime(file).toInstant().toString());
                    fileInfo.put("readable", Files.isReadable(file));
                    fileInfo.put("writable", Files.isWritable(file));
                    fileInfo.put("extension", getFileExtension(fileName));
                    fileInfo.put("exists", true);

                    files.add(fileInfo);
                } catch (IOException e) {
                    Map<String, Object> errorFile = new HashMap<>();
                    errorFile.put("type", type);
                    errorFile.put("name", file.getFileName().toString());
                    errorFile.put("path", file.toString());
                    errorFile.put("error", "Failed to read file metadata: " + e.getMessage());
                    errorFile.put("exists", true);
                    files.add(errorFile);
                }
            }

            if (!hasFiles && includeEmpty) {
                Map<String, Object> emptyDir = new HashMap<>();
                emptyDir.put("type", type);
                emptyDir.put("name", "[Directory empty]");
                emptyDir.put("path", typeDir.toString());
                emptyDir.put("fileCount", 0);
                emptyDir.put("exists", true);
                files.add(emptyDir);
            }
        }

        return files;
    }

    private void sortFiles(List<Map<String, Object>> files, String sortBy, String sortOrder) {
        boolean ascending = "asc".equals(sortOrder);

        files.sort((f1, f2) -> {
            int comparison = switch (sortBy) {
                case "name" -> {
                    String name1 = (String) f1.getOrDefault("name", "");
                    String name2 = (String) f2.getOrDefault("name", "");
                    yield name1.compareToIgnoreCase(name2);
                }
                case "size" -> {
                    Long size1 = (Long) f1.getOrDefault("size", 0L);
                    Long size2 = (Long) f2.getOrDefault("size", 0L);
                    yield size1.compareTo(size2);
                }
                case "modified" -> {
                    String mod1 = (String) f1.getOrDefault("lastModified", "");
                    String mod2 = (String) f2.getOrDefault("lastModified", "");
                    yield mod1.compareTo(mod2);
                }
                case "type" -> {
                    String type1 = (String) f1.getOrDefault("type", "");
                    String type2 = (String) f2.getOrDefault("type", "");
                    yield type1.compareToIgnoreCase(type2);
                }
                default -> 0;
            };

            return ascending ? comparison : -comparison;
        });
    }

    private Map<String, Object> createTypeBreakdown(Map<String, Integer> typeCounts, Map<String, Long> typeSizes) {
        Map<String, Object> breakdown = new HashMap<>();

        for (String type : typeCounts.keySet()) {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("fileCount", typeCounts.get(type));
            typeInfo.put("totalSize", typeSizes.get(type));
            typeInfo.put("totalSizeFormatted", formatBytes(typeSizes.get(type)));
            breakdown.put(type, typeInfo);
        }

        return breakdown;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
