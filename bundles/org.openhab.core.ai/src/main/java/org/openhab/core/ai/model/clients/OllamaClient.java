package org.openhab.core.ai.model.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.common.monitoring.api.Health.HealthStatus;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.configuration.OllamaConfiguration;
import org.openhab.core.ai.model.monitoring.ModelHealthMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.response.OllamaResult;

/**
 * Enhanced Ollama LLM Client implementation using the official ollama4j SDK.
 * 
 * This implementation uses the official ollama4j Java SDK for better reliability and features.
 * It also includes logic to automatically download, install, and start the Ollama server
 * on macOS and Linux systems.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class OllamaClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private final OllamaConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore requestSemaphore;

    // ollama4j SDK components
    private @Nullable OllamaAPI ollamaAPI;
    private @Nullable Process ollamaProcess;
    private final String ollamaHost;
    private final boolean autoStartOllama;
    private final boolean autoInstallOllama;

    public OllamaClient(OllamaConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.getTimeoutMs())).build();
        this.objectMapper = new ObjectMapper();
        this.requestSemaphore = new Semaphore(config.getConcurrentRequests());

        // Extract Ollama host from configuration
        this.ollamaHost = config.getBaseUrl();
        this.autoStartOllama = config.isAutoStartOllama();
        this.autoInstallOllama = config.isAutoInstallOllama();

        this.providerInfo = new ModelClientInfo(ModelProviderType.OLLAMA, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), 0.0); // Local provider, no cost

        // Initialize Ollama if auto-start is enabled
        if (autoStartOllama) {
            initializeOllama();
        }
    }

    /**
     * Initialize Ollama server - download, install, and start if needed
     */
    private void initializeOllama() {
        CompletableFuture.runAsync(() -> {
            try {
                // Check if Ollama is already running
                if (isOllamaRunning()) {
                    logger.info("Ollama server is already running at {}", ollamaHost);
                    initializeOllamaAPI();
                    return;
                }

                // Check if Ollama is installed
                if (!isOllamaInstalled()) {
                    if (autoInstallOllama) {
                        logger.info("Ollama not found, attempting to download and install...");
                        downloadAndInstallOllama();
                    } else {
                        logger.warn(
                                "Ollama not installed and auto-install is disabled. Please install Ollama manually.");
                        return;
                    }
                }

                // Start Ollama server
                logger.info("Starting Ollama server...");
                startOllamaServer();

                // Wait for server to be ready
                waitForOllamaServer();

                // Initialize API client
                initializeOllamaAPI();

                logger.info("Ollama server initialized successfully");

            } catch (Exception e) {
                logger.error("Failed to initialize Ollama server", e);
            }
        }, executorService);
    }

    /**
     * Check if Ollama server is already running
     */
    private boolean isOllamaRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaHost + "/api/tags"))
                    .timeout(Duration.ofSeconds(5)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Ollama is installed on the system
     */
    private boolean isOllamaInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ollama", "--version");
            Process process = pb.start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Download and install Ollama based on the operating system
     */
    private void downloadAndInstallOllama() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String downloadUrl;
        String fileName;

        if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-darwin.tgz";
                fileName = "ollama-darwin.tgz";
            } else {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-darwin.tgz";
                fileName = "ollama-darwin.tgz";
            }
        } else if (os.contains("linux")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-linux-arm64.tgz";
                fileName = "ollama-linux-arm64.tgz";
            } else {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-linux-amd64.tgz";
                fileName = "ollama-linux-amd64.tgz";
            }
        } else if (os.contains("windows")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-windows-arm64.zip";
                fileName = "ollama-windows-arm64.zip";
            } else {
                downloadUrl = "https://github.com/ollama/ollama/releases/latest/download/ollama-windows-amd64.zip";
                fileName = "ollama-windows-amd64.zip";
            }
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }

        // Create temporary directory for download
        Path tempDir = Files.createTempDirectory("ollama-install");
        Path downloadPath = tempDir.resolve(fileName);

        logger.info("Downloading Ollama from: {}", downloadUrl);

        // Download Ollama
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).timeout(Duration.ofMinutes(5))
                .build();

        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(downloadPath));

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download Ollama: HTTP " + response.statusCode());
        }

        // Extract and install
        installOllama(downloadPath, tempDir);

        // Clean up
        Files.deleteIfExists(downloadPath);
        Files.deleteIfExists(tempDir);
    }

    /**
     * Install Ollama from downloaded archive
     */
    private void installOllama(Path archivePath, Path tempDir) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            installOllamaMac(archivePath, tempDir);
        } else if (os.contains("linux")) {
            installOllamaLinux(archivePath, tempDir);
        } else if (os.contains("windows")) {
            installOllamaWindows(archivePath, tempDir);
        }
    }

    /**
     * Install Ollama on macOS
     */
    private void installOllamaMac(Path archivePath, Path tempDir) throws IOException, InterruptedException {
        // Extract tar.gz
        ProcessBuilder extractPb = new ProcessBuilder("tar", "-xzf", archivePath.toString(), "-C", tempDir.toString());
        Process extractProcess = extractPb.start();
        if (extractProcess.waitFor() != 0) {
            throw new IOException("Failed to extract Ollama archive");
        }

        // Move to /usr/local/bin (requires sudo)
        Path ollamaBinary = tempDir.resolve("ollama");
        Path targetPath = Paths.get("/usr/local/bin/ollama");

        // Try without sudo first
        try {
            Files.move(ollamaBinary, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // If that fails, try with sudo
            ProcessBuilder movePb = new ProcessBuilder("sudo", "mv", ollamaBinary.toString(), targetPath.toString());
            Process moveProcess = movePb.start();
            if (moveProcess.waitFor() != 0) {
                throw new IOException("Failed to install Ollama binary", e);
            }
        }

        // Make executable
        ProcessBuilder chmodPb = new ProcessBuilder("chmod", "+x", targetPath.toString());
        Process chmodProcess = chmodPb.start();
        if (chmodProcess.waitFor() != 0) {
            logger.warn("Failed to make Ollama binary executable");
        }
    }

    /**
     * Install Ollama on Linux
     */
    private void installOllamaLinux(Path archivePath, Path tempDir) throws IOException, InterruptedException {
        // Extract tar.gz
        ProcessBuilder extractPb = new ProcessBuilder("tar", "-xzf", archivePath.toString(), "-C", tempDir.toString());
        Process extractProcess = extractPb.start();
        if (extractProcess.waitFor() != 0) {
            throw new IOException("Failed to extract Ollama archive");
        }

        // Move to /usr/local/bin
        Path ollamaBinary = tempDir.resolve("ollama");
        Path targetPath = Paths.get("/usr/local/bin/ollama");

        // Try without sudo first
        try {
            Files.move(ollamaBinary, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // If that fails, try with sudo
            ProcessBuilder movePb = new ProcessBuilder("sudo", "mv", ollamaBinary.toString(), targetPath.toString());
            Process moveProcess = movePb.start();
            if (moveProcess.waitFor() != 0) {
                throw new IOException("Failed to install Ollama binary", e);
            }
        }

        // Make executable
        ProcessBuilder chmodPb = new ProcessBuilder("chmod", "+x", targetPath.toString());
        Process chmodProcess = chmodPb.start();
        if (chmodProcess.waitFor() != 0) {
            logger.warn("Failed to make Ollama binary executable");
        }
    }

    /**
     * Install Ollama on Windows
     */
    private void installOllamaWindows(Path archivePath, Path tempDir) throws IOException, InterruptedException {
        // Extract zip file
        ProcessBuilder extractPb = new ProcessBuilder("powershell", "-Command", "Expand-Archive -Path '"
                + archivePath.toString() + "' -DestinationPath '" + tempDir.toString() + "' -Force");
        Process extractProcess = extractPb.start();
        if (extractProcess.waitFor() != 0) {
            throw new IOException("Failed to extract Ollama archive");
        }

        // Find the ollama.exe file in the extracted directory
        Path ollamaBinary = findOllamaExe(tempDir);
        if (ollamaBinary == null) {
            throw new IOException("Could not find ollama.exe in extracted archive");
        }

        // Install to user's Programs directory (no admin required)
        String userHome = System.getProperty("user.home");
        Path targetDir = Paths.get(userHome, "AppData", "Local", "Programs", "Ollama");
        Path targetPath = targetDir.resolve("ollama.exe");

        // Create target directory if it doesn't exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Move the binary
        try {
            Files.move(ollamaBinary, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to install Ollama binary to " + targetPath, e);
        }

        // Add to PATH by updating user environment variable
        addToWindowsPath(targetDir.toString());

        logger.info("Ollama installed to: {}", targetPath);
    }

    /**
     * Find ollama.exe in the extracted directory
     */
    private @Nullable Path findOllamaExe(Path directory) throws IOException {
        return Files.walk(directory).filter(path -> path.getFileName().toString().equals("ollama.exe")).findFirst()
                .orElse(null);
    }

    /**
     * Add directory to Windows PATH environment variable
     */
    private void addToWindowsPath(String directory) {
        try {
            // Get current PATH
            String currentPath = System.getenv("PATH");
            if (currentPath != null && !currentPath.contains(directory)) {
                // Add to PATH using PowerShell
                String command = String.format(
                        "powershell -Command \"$env:PATH += ';%s'; [Environment]::SetEnvironmentVariable('PATH', $env:PATH, 'User')\"",
                        directory);

                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
                Process process = pb.start();
                if (process.waitFor() != 0) {
                    logger.warn("Failed to add Ollama to PATH. Please add {} to your PATH manually.", directory);
                } else {
                    logger.info("Added Ollama to PATH: {}", directory);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to update PATH environment variable: {}", e.getMessage());
        }
    }

    /**
     * Start the Ollama server process
     */
    private void startOllamaServer() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("ollama", "serve");
        pb.redirectErrorStream(true);

        ollamaProcess = pb.start();

        // Log server output
        executorService.submit(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = ollamaProcess.getInputStream().read(buffer)) != -1) {
                    String output = new String(buffer, 0, bytesRead);
                    logger.debug("Ollama server: {}", output.trim());
                }
            } catch (IOException e) {
                logger.debug("Ollama server output stream closed", e);
            }
        });
    }

    /**
     * Wait for Ollama server to be ready
     */
    private void waitForOllamaServer() throws InterruptedException {
        int maxAttempts = 30; // 30 seconds
        int attempt = 0;

        while (attempt < maxAttempts) {
            if (isOllamaRunning()) {
                return;
            }

            Thread.sleep(1000);
            attempt++;
        }

        throw new RuntimeException("Ollama server failed to start within 30 seconds");
    }

    /**
     * Initialize the ollama4j API client
     */
    private void initializeOllamaAPI() {
        try {
            ollamaAPI = new OllamaAPI(ollamaHost);
            ollamaAPI.setVerbose(false);
            ollamaAPI.setRequestTimeoutSeconds(config.getTimeoutMs() / 1000);
            logger.info("Ollama API client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Ollama API client", e);
        }
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Acquire semaphore for concurrent request limiting
                requestSemaphore.acquire();
                try {
                    // Use ollama4j SDK for better reliability
                    if (ollamaAPI != null) {
                        return completeWithSDK(prompt, params);
                    } else {
                        // Fallback to manual HTTP implementation
                        return completeWithHTTP(prompt, params);
                    }
                } finally {
                    requestSemaphore.release();
                }
            } catch (Exception e) {
                logger.error("Error completing prompt with Ollama", e);
                throw new RuntimeException("Failed to complete prompt", e);
            }
        }, executorService);
    }

    /**
     * Complete using the ollama4j SDK
     */
    private ModelResponse completeWithSDK(String prompt, ModelParameters params)
            throws OllamaBaseException, ToolInvocationException, IOException, InterruptedException {
        if (ollamaAPI == null) {
            throw new IllegalStateException("Ollama API not initialized");
        }

        // Create chat request using SDK
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(config.getModelName());

        // Add system prompt if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            builder.withMessage(OllamaChatMessageRole.SYSTEM, config.getSystemPrompt());
        }

        // Add user message
        OllamaChatRequest request = builder.withMessage(OllamaChatMessageRole.USER, prompt).build();

        // Execute request
        OllamaChatResult result = ollamaAPI.chat(request);

        // Convert to ModelResponse
        return ModelResponse.builder().withContent(result.getResponseModel().getMessage().getContent())
                .withModelName(config.getModelName()).withProviderType(ModelProviderType.OLLAMA.name())
                .withTotalTokens(result.getResponseModel().getMessage().getContent().length()) // Approximate
                .build();
    }

    /**
     * Complete using manual HTTP implementation (fallback)
     */
    private ModelResponse completeWithHTTP(String prompt, ModelParameters params) throws Exception {
        // Build request payload (OpenAI-compatible format)
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", config.getModelName());
        requestBody.put("max_tokens", params.getMaxTokens());
        requestBody.put("temperature", params.getTemperature());
        requestBody.put("stream", false);

        // Build messages array
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);

        // Add system prompt if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", config.getSystemPrompt());
        }

        String requestJson = objectMapper.writeValueAsString(requestBody);
        logger.debug("Ollama request: model={}, maxTokens={}, temperature={}", config.getModelName(),
                params.getMaxTokens(), params.getTemperature());

        // Send request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaHost + "/api/chat"))
                .header("Content-Type", "application/json").timeout(Duration.ofMillis(config.getTimeoutMs()))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama API error: " + response.statusCode() + " - " + response.body());
        }

        // Parse response
        JsonNode responseJson = objectMapper.readTree(response.body());
        String content = responseJson.path("message").path("content").asText();

        return ModelResponse.builder().withContent(content).withModelName(config.getModelName())
                .withProviderType(ModelProviderType.OLLAMA.name()).withTotalTokens(content.length()) // Approximate
                .build();
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Acquire semaphore for concurrent request limiting
                requestSemaphore.acquire();
                try {
                    // Use ollama4j SDK for streaming
                    if (ollamaAPI != null) {
                        return completeWithStreamingSDK(prompt, params, handler);
                    } else {
                        // Fallback to manual HTTP implementation
                        return completeWithStreamingHTTP(prompt, params, handler);
                    }
                } finally {
                    requestSemaphore.release();
                }
            } catch (Exception e) {
                logger.error("Error completing prompt with streaming", e);
                throw new RuntimeException("Failed to complete prompt with streaming", e);
            }
        }, executorService);
    }

    /**
     * Complete with streaming using the ollama4j SDK
     */
    private ModelResponse completeWithStreamingSDK(String prompt, ModelParameters params, ModelStreamHandler handler)
            throws OllamaBaseException, ToolInvocationException, IOException, InterruptedException {
        if (ollamaAPI == null) {
            throw new IllegalStateException("Ollama API not initialized");
        }

        // Create chat request using SDK
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(config.getModelName());

        // Add system prompt if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            builder.withMessage(OllamaChatMessageRole.SYSTEM, config.getSystemPrompt());
        }

        // Add user message
        OllamaChatRequest request = builder.withMessage(OllamaChatMessageRole.USER, prompt).build();

        // Execute streaming request
        StringBuilder fullContent = new StringBuilder();
        ollamaAPI.chatStreaming(request, token -> {
            String content = token.getMessage().getContent();
            fullContent.append(content);
            handler.onChunk(content);
        });

        ModelResponse finalResponse = ModelResponse.builder().withContent(fullContent.toString())
                .withModelName(config.getModelName()).withProviderType(ModelProviderType.OLLAMA.name())
                .withTotalTokens(fullContent.length()) // Approximate
                .build();

        handler.onComplete(finalResponse);

        // Return final response
        return finalResponse;
    }

    /**
     * Complete with streaming using manual HTTP implementation (fallback)
     */
    private ModelResponse completeWithStreamingHTTP(String prompt, ModelParameters params, ModelStreamHandler handler)
            throws Exception {
        // Build request payload (OpenAI-compatible format)
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", config.getModelName());
        requestBody.put("max_tokens", params.getMaxTokens());
        requestBody.put("temperature", params.getTemperature());
        requestBody.put("stream", true);

        // Build messages array
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);

        // Add system prompt if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", config.getSystemPrompt());
        }

        String requestJson = objectMapper.writeValueAsString(requestBody);
        logger.debug("Ollama streaming request: model={}, maxTokens={}, temperature={}", config.getModelName(),
                params.getMaxTokens(), params.getTemperature());

        // Send request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaHost + "/api/chat"))
                .header("Content-Type", "application/json").timeout(Duration.ofMillis(config.getTimeoutMs()))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama API error: " + response.statusCode() + " - " + response.body());
        }

        // Parse streaming response
        String[] lines = response.body().split("\n");
        StringBuilder fullContent = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6);
                if (data.equals("[DONE]")) {
                    break;
                }

                try {
                    JsonNode chunk = objectMapper.readTree(data);
                    String content = chunk.path("message").path("content").asText();
                    if (!content.isEmpty()) {
                        fullContent.append(content);
                        handler.onChunk(content);
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse streaming chunk: {}", data, e);
                }
            }
        }

        ModelResponse finalResponse = ModelResponse.builder().withContent(fullContent.toString())
                .withModelName(config.getModelName()).withProviderType(ModelProviderType.OLLAMA.name())
                .withTotalTokens(fullContent.length()) // Approximate
                .build();

        handler.onComplete(finalResponse);

        return finalResponse;
    }

    @Override
    public boolean isAvailable() {
        try {
            return testConnection().get();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ModelClientInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public ModelHealthMetrics getHealthStatus() {
        boolean available = isAvailable();
        return ModelHealthMetrics.builder("ollama-client")
                .withStatus(available ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY).withAvailable(available)
                .withAverageResponseTimeMs(available ? 100L : 0L).withSuccessRate(available ? 1.0 : 0.0)
                .withLastError(available ? null : "Connection failed").build();
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.OLLAMA;
    }

    @Override
    public String getModelName() {
        return config.getModelName();
    }

    @Override
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try using ollama4j SDK first
                if (ollamaAPI != null) {
                    try {
                        OllamaResult result = ollamaAPI.generate(config.getModelName(), "Hello", null);
                        return result != null && result.getResponse() != null;
                    } catch (Exception e) {
                        logger.debug("SDK connection test failed, trying HTTP fallback", e);
                    }
                }

                // Fallback to HTTP test
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaHost + "/api/generate"))
                        .header("Content-Type", "application/json").timeout(Duration.ofSeconds(5))
                        .POST(HttpRequest.BodyPublishers
                                .ofString("{\"model\":\"" + config.getModelName() + "\",\"prompt\":\"Hello\"}"))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            } catch (Exception e) {
                logger.debug("Connection test failed", e);
                return false;
            }
        }, executorService);
    }

    @Override
    public double estimateCost(String prompt, ModelParameters params) {
        return 0.0; // Local provider, no cost
    }

    @Override
    public int getMaxTokens() {
        return config.getMaxTokens();
    }

    @Override
    public double getCostPer1kTokens() {
        return 0.0; // Local provider, no cost
    }

    @Override
    public boolean supportsFunctionCalling() {
        // Investigate function calling support in ollama4j
        // Based on ollama4j documentation, function calling is supported for models that have it
        // This would need to be checked dynamically based on the model being used
        return true; // Assume support for now, can be refined based on model capabilities
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supportsMultimodal() {
        // Investigate multimodal support in ollama4j
        // Based on ollama4j documentation, multimodal support is available for models like LLaVA
        // This would need to be checked dynamically based on the model being used
        return true; // Assume support for now, can be refined based on model capabilities
    }

    @Override
    public @Nullable ModelRateLimitInfo getRateLimitInfo() {
        // Extract rate limit info from response headers
        // For Ollama, rate limiting is typically handled locally based on the semaphore
        // HTTP headers don't typically contain rate limit information for local services
        return null;
    }

    /**
     * Get available actions from the registry
     */
    private List<Action> getAvailableActions() {
        // Implement action discovery for Ollama
        if (actionRegistry != null) {
            try {
                Map<String, Action> actionsMap = actionRegistry.getAllActions();
                return new ArrayList<>(actionsMap.values());
            } catch (Exception e) {
                logger.warn("Error retrieving actions from registry", e);
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Shutdown the Ollama client and stop the server if it was started by this client
     */
    public void shutdown() {
        try {
            if (ollamaProcess != null && ollamaProcess.isAlive()) {
                logger.info("Shutting down Ollama server...");
                ollamaProcess.destroy();
                if (!ollamaProcess.waitFor(10, TimeUnit.SECONDS)) {
                    ollamaProcess.destroyForcibly();
                }
            }
        } catch (Exception e) {
            logger.error("Error shutting down Ollama server", e);
        } finally {
            executorService.shutdown();
        }
    }
}
