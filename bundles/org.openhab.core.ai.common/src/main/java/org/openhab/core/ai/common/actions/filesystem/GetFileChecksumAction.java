package org.openhab.core.ai.common.actions.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting file checksums in openHAB.
 * 
 * This action provides functionality to calculate
 * checksums for files using various algorithms.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetFileChecksumAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetFileChecksumAction.class);

    private static final String ACTION_ID = "filesystem.get_checksum";
    private static final String ACTION_NAME = "Get File Checksum";
    private static final String DESCRIPTION = "Calculate file checksums using various algorithms";
    private static final String VERSION = "1.0.0";

    // Maximum file size for checksum calculation (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // Supported algorithms
    private static final List<String> SUPPORTED_ALGORITHMS = List.of("MD5", "SHA-1", "SHA-256", "SHA-512");

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

        Map<String, Object> filePathParam = new HashMap<>();
        filePathParam.put("type", "string");
        filePathParam.put("description", "Path to the file to calculate checksum for");
        filePathParam.put("required", true);
        schema.put("filePath", filePathParam);

        Map<String, Object> algorithmParam = new HashMap<>();
        algorithmParam.put("type", "string");
        algorithmParam.put("enum", SUPPORTED_ALGORITHMS);
        algorithmParam.put("description", "Checksum algorithm to use");
        algorithmParam.put("required", false);
        algorithmParam.put("default", "SHA-256");
        schema.put("algorithm", algorithmParam);

        Map<String, Object> formatParam = new HashMap<>();
        formatParam.put("type", "string");
        formatParam.put("enum", List.of("hex", "base64"));
        formatParam.put("description", "Output format for the checksum");
        formatParam.put("required", false);
        formatParam.put("default", "hex");
        schema.put("format", formatParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> checksumResult = new HashMap<>();
        checksumResult.put("type", "object");
        checksumResult.put("description", "Checksum calculation result");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> filePathProp = new HashMap<>();
        filePathProp.put("type", "string");
        filePathProp.put("description", "Path to the file");
        properties.put("filePath", filePathProp);

        Map<String, Object> algorithmProp = new HashMap<>();
        algorithmProp.put("type", "string");
        algorithmProp.put("description", "Algorithm used for checksum calculation");
        properties.put("algorithm", algorithmProp);

        Map<String, Object> checksumProp = new HashMap<>();
        checksumProp.put("type", "string");
        checksumProp.put("description", "Calculated checksum");
        properties.put("checksum", checksumProp);

        Map<String, Object> formatProp = new HashMap<>();
        formatProp.put("type", "string");
        formatProp.put("description", "Format of the checksum");
        properties.put("format", formatProp);

        Map<String, Object> fileSizeProp = new HashMap<>();
        fileSizeProp.put("type", "integer");
        fileSizeProp.put("description", "Size of the file in bytes");
        properties.put("fileSize", fileSizeProp);

        Map<String, Object> calculationTimeProp = new HashMap<>();
        calculationTimeProp.put("type", "number");
        calculationTimeProp.put("description", "Time taken to calculate checksum in milliseconds");
        properties.put("calculationTime", calculationTimeProp);

        checksumResult.put("properties", properties);
        schema.put("checksumResult", checksumResult);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.read", true);
        capabilities.put("checksum", true);
        capabilities.put("cryptography", true);
        return capabilities;
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().description(DESCRIPTION).version(getVersion()).build();
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate required parameters
        if (!parameters.containsKey("filePath")) {
            errors.add("Missing required parameter: filePath");
        } else {
            String filePath = (String) parameters.get("filePath");
            if (filePath == null || filePath.trim().isEmpty()) {
                errors.add("File path cannot be null or empty");
            } else {
                try {
                    if (!FileSystemSecurityUtils.isPathAllowed(filePath)) {
                        errors.add("File path is not allowed: " + filePath);
                    }
                } catch (Exception e) {
                    errors.add("Invalid file path format: " + filePath);
                }
            }
        }

        // Validate optional parameters
        if (parameters.containsKey("algorithm")) {
            String algorithm = (String) parameters.get("algorithm");
            if (algorithm == null || !SUPPORTED_ALGORITHMS.contains(algorithm)) {
                errors.add("Algorithm must be one of: " + String.join(", ", SUPPORTED_ALGORITHMS));
            }
        }

        if (parameters.containsKey("format")) {
            String format = (String) parameters.get("format");
            if (format == null || !List.of("hex", "base64").contains(format)) {
                errors.add("Format must be one of: hex, base64");
            }
        }

        if (errors.isEmpty()) {
            return AIActionValidationResult.valid(parameters);
        } else {
            return AIActionValidationResult.invalid(errors);
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            // Validate parameters
            AIActionValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                throw new AIActionException(ACTION_ID,
                        "Parameter validation failed: " + String.join(", ", validation.getErrors()));
            }

            String filePath = (String) parameters.get("filePath");
            String algorithm = parameters.containsKey("algorithm") ? (String) parameters.get("algorithm") : "SHA-256";
            String format = parameters.containsKey("format") ? (String) parameters.get("format") : "hex";

            // Security check
            if (!FileSystemSecurityUtils.isPathAllowed(filePath)) {
                throw new AIActionException(ACTION_ID, "Security violation: File path not allowed: " + filePath);
            }

            Path file = Paths.get(filePath);

            // Check if file exists
            if (!Files.exists(file)) {
                throw new AIActionException(ACTION_ID, "File does not exist: " + filePath);
            }

            if (!Files.isRegularFile(file)) {
                throw new AIActionException(ACTION_ID, "Path is not a regular file: " + filePath);
            }

            // Check file size
            long fileSize = Files.size(file);
            if (fileSize > MAX_FILE_SIZE) {
                throw new AIActionException(ACTION_ID,
                        "File too large for checksum calculation: " + fileSize + " bytes (max: " + MAX_FILE_SIZE + ")");
            }

            // Calculate checksum
            long calculationStartTime = System.currentTimeMillis();
            String checksum = calculateChecksum(file, algorithm, format);
            long calculationTime = System.currentTimeMillis() - calculationStartTime;

            // Build result
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> checksumResult = new HashMap<>();

            checksumResult.put("filePath", filePath);
            checksumResult.put("algorithm", algorithm);
            checksumResult.put("checksum", checksum);
            checksumResult.put("format", format);
            checksumResult.put("fileSize", fileSize);
            checksumResult.put("calculationTime", calculationTime);

            resultData.put("checksumResult", checksumResult);

            logger.debug("Checksum calculated for {} using {}: {} ({} bytes, {}ms)", filePath, algorithm, checksum,
                    fileSize, calculationTime);
            return AIActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during checksum calculation: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during checksum calculation: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "IO error during checksum calculation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during checksum calculation: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Unexpected error during checksum calculation: " + e.getMessage());
        }
    }

    private String calculateChecksum(Path file, String algorithm, String format)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hash = digest.digest();

        if ("hex".equals(format)) {
            return bytesToHex(hash);
        } else if ("base64".equals(format)) {
            return java.util.Base64.getEncoder().encodeToString(hash);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
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
    public void initialize(AIActionContext context) {
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
