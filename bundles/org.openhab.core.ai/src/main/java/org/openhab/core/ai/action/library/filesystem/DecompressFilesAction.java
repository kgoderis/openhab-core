package org.openhab.core.ai.action.library.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for decompressing files in openHAB.
 * 
 * This action provides functionality to decompress
 * archive files and extract their contents.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DecompressFilesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DecompressFilesAction.class);

    private static final String ACTION_ID = "filesystem.decompress_files";
    private static final String ACTION_NAME = "Decompress Files";
    private static final String DESCRIPTION = "Decompress files from a ZIP archive";
    private static final String VERSION = "1.0.0";

    // Maximum file size for decompression (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // Maximum total size for decompression (1GB)
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

        Map<String, Object> zipPathParam = new HashMap<>();
        zipPathParam.put("type", "string");
        zipPathParam.put("description", "Path to the ZIP file to decompress");
        zipPathParam.put("required", true);
        schema.put("zipPath", zipPathParam);

        Map<String, Object> outputPathParam = new HashMap<>();
        outputPathParam.put("type", "string");
        outputPathParam.put("description", "Output directory for decompressed files");
        outputPathParam.put("required", true);
        schema.put("outputPath", outputPathParam);

        Map<String, Object> overwriteParam = new HashMap<>();
        overwriteParam.put("type", "boolean");
        overwriteParam.put("description", "Overwrite existing files");
        overwriteParam.put("required", false);
        overwriteParam.put("default", false);
        schema.put("overwrite", overwriteParam);

        Map<String, Object> extractSpecificParam = new HashMap<>();
        extractSpecificParam.put("type", "array");
        extractSpecificParam.put("description",
                "List of specific files to extract (optional, extracts all if not specified)");
        extractSpecificParam.put("items", Map.of("type", "string"));
        extractSpecificParam.put("required", false);
        schema.put("extractSpecific", extractSpecificParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> decompressionResult = new HashMap<>();
        decompressionResult.put("type", "object");
        decompressionResult.put("description", "Decompression result");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> successProp = new HashMap<>();
        successProp.put("type", "boolean");
        successProp.put("description", "Whether the decompression was successful");
        properties.put("success", successProp);

        Map<String, Object> outputPathProp = new HashMap<>();
        outputPathProp.put("type", "string");
        outputPathProp.put("description", "Path to the output directory");
        properties.put("outputPath", outputPathProp);

        Map<String, Object> filesExtractedProp = new HashMap<>();
        filesExtractedProp.put("type", "integer");
        filesExtractedProp.put("description", "Number of files extracted");
        properties.put("filesExtracted", filesExtractedProp);

        Map<String, Object> totalSizeProp = new HashMap<>();
        totalSizeProp.put("type", "integer");
        totalSizeProp.put("description", "Total size of extracted files in bytes");
        properties.put("totalSize", totalSizeProp);

        Map<String, Object> extractedFilesProp = new HashMap<>();
        extractedFilesProp.put("type", "array");
        extractedFilesProp.put("description", "List of extracted files");
        properties.put("extractedFiles", extractedFilesProp);

        Map<String, Object> messageProp = new HashMap<>();
        messageProp.put("type", "string");
        messageProp.put("description", "Result message");
        properties.put("message", messageProp);

        decompressionResult.put("properties", properties);
        schema.put("decompressionResult", decompressionResult);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.read", true);
        capabilities.put("filesystem.write", true);
        capabilities.put("decompression", true);
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
        if (!parameters.containsKey("zipPath")) {
            errors.add("Missing required parameter: zipPath");
        } else {
            String zipPath = (String) parameters.get("zipPath");
            if (zipPath == null || zipPath.trim().isEmpty()) {
                errors.add("ZIP path cannot be null or empty");
            } else {
                try {
                    if (!FileSystemSecurityUtils.isPathAllowed(zipPath)) {
                        errors.add("ZIP path is not allowed: " + zipPath);
                    }
                } catch (Exception e) {
                    errors.add("Invalid ZIP path format: " + zipPath);
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
        if (parameters.containsKey("overwrite")) {
            Object overwrite = parameters.get("overwrite");
            if (!(overwrite instanceof Boolean)) {
                errors.add("Parameter 'overwrite' must be a boolean");
            }
        }

        if (parameters.containsKey("extractSpecific")) {
            Object extractSpecificObj = parameters.get("extractSpecific");
            if (!(extractSpecificObj instanceof List)) {
                errors.add("extractSpecific must be a list");
            } else {
                @SuppressWarnings("unchecked")
                List<String> extractSpecific = (List<String>) extractSpecificObj;
                for (String file : extractSpecific) {
                    if (file == null || file.trim().isEmpty()) {
                        errors.add("Extract specific file cannot be null or empty");
                    }
                }
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

            String zipPath = (String) parameters.get("zipPath");
            String outputPath = (String) parameters.get("outputPath");
            boolean overwrite = parameters.containsKey("overwrite") ? (Boolean) parameters.get("overwrite") : false;
            @SuppressWarnings("unchecked")
            List<String> extractSpecific = parameters.containsKey("extractSpecific")
                    ? (List<String>) parameters.get("extractSpecific")
                    : null;

            // Security checks
            if (!FileSystemSecurityUtils.isPathAllowed(zipPath)) {
                throw new ActionException(ACTION_ID, "Security violation: ZIP path not allowed: " + zipPath);
            }

            if (!FileSystemSecurityUtils.isPathAllowed(outputPath)) {
                throw new ActionException(ACTION_ID, "Security violation: Output path not allowed: " + outputPath);
            }

            Path zipFile = Paths.get(zipPath);
            Path outputDir = Paths.get(outputPath);

            // Check if ZIP file exists
            if (!Files.exists(zipFile)) {
                throw new ActionException(ACTION_ID, "ZIP file does not exist: " + zipPath);
            }

            if (!Files.isRegularFile(zipFile)) {
                throw new ActionException(ACTION_ID, "ZIP path is not a file: " + zipPath);
            }

            // Check ZIP file size
            long zipSize = Files.size(zipFile);
            if (zipSize > MAX_FILE_SIZE) {
                throw new ActionException(ACTION_ID,
                        "ZIP file too large for decompression: " + zipSize + " bytes (max: " + MAX_FILE_SIZE + ")");
            }

            // Create output directory
            Files.createDirectories(outputDir);

            // Perform decompression
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> decompressionResult = new HashMap<>();

            List<String> extractedFiles = new ArrayList<>();
            int filesExtracted = 0;
            long totalSize = 0;

            try (InputStream fis = Files.newInputStream(zipFile); ZipInputStream zis = new ZipInputStream(fis)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    // Skip if we're extracting specific files and this one isn't in the list
                    if (extractSpecific != null && !extractSpecific.contains(entryName)) {
                        continue;
                    }

                    // Skip directory entries (they'll be created when files are extracted)
                    if (entry.isDirectory()) {
                        continue;
                    }

                    // Check for path traversal attacks
                    if (entryName.contains("..") || entryName.startsWith("/")) {
                        logger.warn("Skipping potentially malicious entry: {}", entryName);
                        continue;
                    }

                    // Create the output file path
                    Path outputFile = outputDir.resolve(entryName).normalize();

                    // Security check: ensure the output file is within the allowed directory
                    if (!outputFile.startsWith(outputDir)) {
                        logger.warn("Skipping entry that would extract outside target directory: {}", entryName);
                        continue;
                    }

                    // Check if file already exists
                    if (Files.exists(outputFile) && !overwrite) {
                        logger.debug("Skipping existing file: {}", entryName);
                        continue;
                    }

                    // Create parent directories
                    Files.createDirectories(outputFile.getParent());

                    // Extract the file
                    try {
                        long extractedSize = Files.copy(zis, outputFile,
                                overwrite ? StandardCopyOption.REPLACE_EXISTING : StandardCopyOption.COPY_ATTRIBUTES);

                        totalSize += extractedSize;
                        filesExtracted++;
                        extractedFiles.add(outputFile.toString());

                        logger.debug("Extracted: {} ({} bytes)", entryName, extractedSize);

                    } catch (IOException e) {
                        logger.warn("Failed to extract {}: {}", entryName, e.getMessage());
                    }

                    zis.closeEntry();
                }
            }

            decompressionResult.put("success", true);
            decompressionResult.put("outputPath", outputPath);
            decompressionResult.put("filesExtracted", filesExtracted);
            decompressionResult.put("totalSize", totalSize);
            decompressionResult.put("extractedFiles", extractedFiles);
            decompressionResult.put("message", "Decompression completed successfully");

            resultData.put("decompressionResult", decompressionResult);

            logger.debug("Decompression completed. Extracted {} files, total size: {} bytes", filesExtracted,
                    totalSize);
            return ActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during decompression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during decompression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "IO error during decompression: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during decompression: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Unexpected error during decompression: " + e.getMessage());
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
