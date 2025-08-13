package org.openhab.core.ai.stub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP stub server for AI protocol testing.
 * 
 * This class provides a mock HTTP server for testing AI protocols
 * without requiring real HTTP connections.
 * 
 * 
 */
public class StubHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(StubHttpServer.class);

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private long requestCount = 0;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, Object> scenarioConfiguration = new ConcurrentHashMap<>();

    /**
     * Create a new HTTP stub server.
     * 
     * @param port Port to listen on
     */
    public StubHttpServer(int port) {
        this.port = port;
    }

    /**
     * Start the HTTP stub server.
     * 
     * @return true if started successfully
     */
    public boolean start() {
        if (running.get()) {
            logger.warn("HTTP stub server is already running on port {}", port);
            return true;
        }

        try {
            // Initialize server socket
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();

            // Start server thread
            executorService.submit(this::runServer);

            running.set(true);
            logger.info("HTTP stub server started on port {}", port);
            return true;
        } catch (Exception e) {
            logger.error("Failed to start HTTP stub server on port {}", port, e);
            return false;
        }
    }

    /**
     * Stop the HTTP stub server.
     * 
     * @return true if stopped successfully
     */
    public boolean stop() {
        if (!running.get()) {
            logger.warn("HTTP stub server is not running");
            return true;
        }

        try {
            shutdown.set(true);

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Shutdown executor service
            if (executorService != null) {
                executorService.shutdown();
            }

            running.set(false);
            logger.info("HTTP stub server stopped");
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop HTTP stub server", e);
            return false;
        }
    }

    /**
     * Reset the stub server state.
     */
    public void reset() {
        requestCount = 0;
        logger.debug("HTTP stub server reset");
    }

    /**
     * Get the request count.
     * 
     * @return Number of requests handled
     */
    public long getRequestCount() {
        return requestCount;
    }

    /**
     * Configure the server for a test scenario.
     * 
     * @param httpConfiguration HTTP configuration
     */
    public void configureScenario(Object httpConfiguration) {
        logger.debug("Configuring HTTP stub server with: {}", httpConfiguration);

        if (httpConfiguration instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) httpConfiguration;
            scenarioConfiguration.putAll(config);
        }
    }

    /**
     * Check if the server is running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get the server port.
     * 
     * @return Server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Run the server loop.
     */
    private void runServer() {
        try {
            while (!shutdown.get() && serverSocket != null && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                requestCount++;

                // Handle client connection in separate thread
                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            if (!shutdown.get()) {
                logger.error("Error in server loop", e);
            }
        }
    }

    /**
     * Handle client connection.
     * 
     * @param clientSocket the client socket
     */
    private void handleClient(Socket clientSocket) {
        try {
            // Simple HTTP response
            String response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: 13\r\n" + "\r\n"
                    + "Hello, World!";

            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.close();
        } catch (IOException e) {
            logger.error("Error handling client", e);
        }
    }
}
