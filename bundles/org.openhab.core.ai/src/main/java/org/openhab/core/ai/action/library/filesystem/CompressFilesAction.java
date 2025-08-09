package org.openhab.core.ai.action.library.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to compress files and directories into a ZIP archive.
 * Only operates within the openHAB root folder for security.
 */
@NonNullByDefault
public class CompressFilesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(CompressFilesAction.class);

    private static final String ACTION_ID = "filesystem.compress_files";
    private static final String ACTION_NAME = "Compress Files";
    private static final String DESCRIPTION = "Compress files and directories into a ZIP archive";
    private static final String VERSION = "1.0.0";

    // Maximum file size for compression (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // Maximum total size for compression (1GB)
    private static final long MAX_TOTAL_SIZE = 1024 * 1024 * 1024;

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return "filesystem";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> sourcePathsParam = new HashMap<>();
        sourcePathsParam.put("type", "array");
        sourcePathsParam.put("description", "List of file or directory paths to compress");
        sourcePathsParam.put("items", Map.of("type", "string"));
        sourcePathsParam.put("required", true);
        schema.put("sourcePaths", sourcePathsParam);

        Map<String, Object> outputPathParam = new HashMap<>();
        outputPathParam.put("type", "string");
        outputPathParam.put("description", "Output path for the ZIP file");
        outputPathParam.put("required", true);
        schema.put("outputPath", outputPathParam);

        Map<String, Object> includeHiddenParam = new HashMap<>();
        includeHiddenParam.put("type", "boolean");
        includeHiddenParam.put("description", "Include hidden files and directories");
        includeHiddenParam.put("required", false);
        includeHiddenParam.put("default", false);
        schema.put("includeHidden", includeHiddenParam);

        Map<String, Object> compressionLevelParam = new HashMap<>();
        compressionLevelParam.put("type", "integer");
        compressionLevelParam.put("description", "Compression level (0-9, 0=no compression, 9=maximum)");
        compressionLevelParam.put("required", false);
        compressionLevelParam.put("default", 6);
        compressionLevelParam.put("minimum", 0);
        compressionLevelParam.put("maximum", 9);
        schema.put("compressionLevel", compressionLevelParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> compressionResult = new HashMap<>();
        compressionResult.put("type", "object");
        compressionResult.put("description", "Compression result");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> successProp = new HashMap<>();
        successProp.put("type", "boolean");
        successProp.put("description", "Whether the compression was successful");
        properties.put("success", successProp);

        Map<String, Object> outputPathProp = new HashMap<>();
        outputPathProp.put("type", "string");
        outputPathProp.put("description", "Path to the created ZIP file");
        properties.put("outputPath", outputPathProp);

        Map<String, Object> originalSizeProp = new HashMap<>();
        originalSizeProp.put("type", "integer");
        originalSizeProp.put("description", "Total size of original files in bytes");
        properties.put("originalSize", originalSizeProp);

        Map<String, Object> compressedSizeProp = new HashMap<>();
        compressedSizeProp.put("type", "integer");
        compressedSizeProp.put("description", "Size of the ZIP file in bytes");
        properties.put("compressedSize", compressedSizeProp);

        Map<String, Object> compressionRatioProp = new HashMap<>();
        compressionRatioProp.put("type", "number");
        compressionRatioProp.put("description", "Compression ratio (0.0-1.0)");
        properties.put("compressionRatio", compressionRatioProp);

        Map<String, Object> filesProcessedProp = new HashMap<>();
        filesProcessedProp.put("type", "integer");
        filesProcessedProp.put("description", "Number of files processed");
        properties.put("filesProcessed", filesProcessedProp);

        Map<String, Object> messageProp = new HashMap<>();
        messageProp.put("type", "string");
        messageProp.put("description", "Result message");
        properties.put("message", messageProp);

        compressionResult.put("properties", properties);
        schema.put("compressionResult", compressionResult);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.read", true);
        capabilities.put("filesystem.write", true);
        capabilities.put("compression", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(getVersion()).build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate required parameters
        if (!parameters.containsKey("sourcePaths")) {
            errors.add("Missing required parameter: sourcePaths");
        } else {
            Object sourcePathsObj = parameters.get("sourcePaths");
            if (!(sourcePathsObj instanceof List)) {
                errors.add("sourcePaths must be a list");
            } else {
                @SuppressWarnings("unchecked")
                List<String> sourcePaths = (List<String>) sourcePathsObj;
                if (sourcePaths.isEmpty()) {
                    errors.add("sourcePaths cannot be empty");
                } else {
                    for (String path : sourcePaths) {
                        if (path == null || path.trim().isEmpty()) {
                            errors.add("Source path cannot be null or empty");
                        } else {
                            try {
                                if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                                    errors.add("Source path is not allowed: " + path);
                                }
                            } catch (Exception e) {
                                errors.add("Invalid source path format: " + path);
                            }
                        }
                    }
                }
            }
        }

        if (!parameters.containsKey("outputPath")) {
            errors.add("Missing required parameter: outputPath");
        } else {
            String outputPath = (String) parameters.get("outputPath");
            if (outputPath == null || outputPath.trim().isEmpty()) {
                errors.add("Output path cannot be null or empty");
            } else {
                try {
                    if (!FileSystemSecurityUtils.isPathAllowed(outputPath)) {
                        errors.add("Output path is not allowed: " + outputPath);
                    }
                } catch (Exception e) {
                    errors.add("Invalid output path format: " + outputPath);
                }
            }
        }

        // Validate optional parameters
        if (parameters.containsKey("compressionLevel")) {
            Object compressionLevel = parameters.get("compressionLevel");
            if (!(compressionLevel instanceof Integer) || (Integer) compressionLevel < 0
                    || (Integer) compressionLevel > 9) {
                errors.add("compressionLevel must be an integer between 0 and 9");
            }
        }

        if (parameters.containsKey("includeHidden")) {
            Object includeHidden = parameters.get("includeHidden");
            if (!(includeHidden instanceof Boolean)) {
                errors.add("Parameter 'includeHidden' must be a boolean");
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            // Validate parameters
            ActionValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                throw new ActionException(ACTION_ID,
                        "Parameter validation failed: " + String.join(", ", validation.getErrors()));
            }

            @SuppressWarnings("unchecked")
            List<String> sourcePaths = (List<String>) parameters.get("sourcePaths");
            String outputPath = (String) parameters.get("outputPath");
            boolean includeHidden = parameters.containsKey("includeHidden") ? (Boolean) parameters.get("includeHidden")
                    : false;
            int compressionLevel = parameters.containsKey("compressionLevel")
                    ? (Integer) parameters.get("compressionLevel")
                    : 6;

            // Security checks
            for (String path : sourcePaths) {
                if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                    throw new ActionException(ACTION_ID, "Security violation: Source path not allowed: " + path);
                }
            }

            if (!FileSystemSecurityUtils.isPathAllowed(outputPath)) {
                throw new ActionException(ACTION_ID, "Security violation: Output path not allowed: " + outputPath);
            }

            Path outputFile = Paths.get(outputPath);

            // Ensure output directory exists
            Files.createDirectories(outputFile.getParent());

            // Validate source files and calculate total size
            long totalSize = 0;
            List<Path> validPaths = new ArrayList<>();

            for (String sourcePath : sourcePaths) {
                Path path = Paths.get(sourcePath);
                if (!Files.exists(path)) {
                    throw new ActionException(ACTION_ID, "Source path does not exist: " + sourcePath);
                }

                if (Files.isRegularFile(path)) {
                    long size = Files.size(path);
                    if (size > MAX_FILE_SIZE) {
                        throw new ActionException(ACTION_ID,
                                "File too large for compression: " + sourcePath + " (" + size + " bytes)");
                    }
                    totalSize += size;
                    validPaths.add(path);
                } else if (Files.isDirectory(path)) {
                    // Calculate directory size
                    long dirSize = calculateDirectorySize(path, includeHidden);
                    totalSize += dirSize;
                    validPaths.add(path);
                }
            }

            if (totalSize > MAX_TOTAL_SIZE) {
                throw new ActionException(ACTION_ID,
                        "Total size too large for compression: " + totalSize + " bytes (max: " + MAX_TOTAL_SIZE + ")");
            }

            // Perform compression
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> compressionResult = new HashMap<>();

            int filesProcessed = 0;
            long compressedSize = 0;

            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(outputFile))) {
                zipOut.setLevel(compressionLevel);

                for (Path sourcePath : validPaths) {
                    if (Files.isRegularFile(sourcePath)) {
                        filesProcessed += addFileToZip(sourcePath, sourcePath.getFileName().toString(), zipOut);
                    } else if (Files.isDirectory(sourcePath)) {
                        filesProcessed += addDirectoryToZip(sourcePath, sourcePath.getFileName().toString(), zipOut,
                                includeHidden);
                    }
                }
            }

            // Get compressed file size
            compressedSize = Files.size(outputFile);

            // Calculate compression ratio
            double compressionRatio = totalSize > 0 ? (double) compressedSize / totalSize : 0.0;

            compressionResult.put("success", true);
            compressionResult.put("outputPath", outputPath);
            compressionResult.put("originalSize", totalSize);
            compressionResult.put("compressedSize", compressedSize);
            compressionResult.put("compressionRatio", Math.round(compressionRatio * 100.0) / 100.0);
            compressionResult.put("filesProcessed", filesProcessed);
            compressionResult.put("message", "Compression completed successfully");

            resultData.put("compressionResult", compressionResult);

            logger.debug("Compression completed. Processed {} files, {} -> {} bytes", filesProcessed, totalSize,
                    compressedSize);
            return ActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during compression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during compression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "IO error during compression: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during compression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Unexpected error during compression: " + e.getMessage());
        }
    }

    private long calculateDirectorySize(Path directory, boolean includeHidden) throws IOException {
        long totalSize = 0;
        try (var stream = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                if (Files.isRegularFile(path)) {
                    if (includeHidden || !isHidden(path)) {
                        totalSize += Files.size(path);
                    }
                }
            }
        }
        return totalSize;
    }

    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path) || path.getFileName().toString().startsWith(".");
        } catch (IOException e) {
            return path.getFileName().toString().startsWith(".");
        }
    }

    private int addFileToZip(Path file, String entryName, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);

        Files.copy(file, zipOut);
        zipOut.closeEntry();

        return 1;
    }

    private int addDirectoryToZip(Path directory, String entryName, ZipOutputStream zipOut, boolean includeHidden)
            throws IOException {
        int filesProcessed = 0;

        try (var stream = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                if (Files.isRegularFile(path)) {
                    if (includeHidden || !isHidden(path)) {
                        String relativePath = directory.relativize(path).toString();
                        String zipEntryName = entryName + "/" + relativePath;

                        ZipEntry zipEntry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(zipEntry);

                        Files.copy(path, zipOut);
                        zipOut.closeEntry();

                        filesProcessed++;
                    }
                }
            }
        }

        return filesProcessed;
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
    public void initialize(ActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
