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
 * MQTT stub broker for AI protocol testing.
 * 
 * This class provides a mock MQTT broker for testing AI protocols
 * without requiring a real MQTT broker.
 * 
 * 
 */
public class StubMQTTBroker {

    private static final Logger logger = LoggerFactory.getLogger(StubMQTTBroker.class);

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private long messageCount = 0;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, Object> scenarioConfiguration = new ConcurrentHashMap<>();

    /**
     * Create a new MQTT stub broker.
     * 
     * @param port Port to listen on
     */
    public StubMQTTBroker(int port) {
        this.port = port;
    }

    /**
     * Start the MQTT stub broker.
     * 
     * @return true if started successfully
     */
    public boolean start() {
        if (running.get()) {
            logger.warn("MQTT stub broker is already running on port {}", port);
            return true;
        }

        try {
            // Initialize server socket
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();

            // Start broker thread
            executorService.submit(this::runBroker);

            running.set(true);
            logger.info("MQTT stub broker started on port {}", port);
            return true;
        } catch (Exception e) {
            logger.error("Failed to start MQTT stub broker on port {}", port, e);
            return false;
        }
    }

    /**
     * Stop the MQTT stub broker.
     * 
     * @return true if stopped successfully
     */
    public boolean stop() {
        if (!running.get()) {
            logger.warn("MQTT stub broker is not running");
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
            logger.info("MQTT stub broker stopped");
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop MQTT stub broker", e);
            return false;
        }
    }

    /**
     * Reset the stub broker state.
     */
    public void reset() {
        messageCount = 0;
        logger.debug("MQTT stub broker reset");
    }

    /**
     * Get the message count.
     * 
     * @return Number of messages handled
     */
    public long getMessageCount() {
        return messageCount;
    }

    /**
     * Configure the broker for a test scenario.
     * 
     * @param mqttConfiguration MQTT configuration
     */
    public void configureScenario(Object mqttConfiguration) {
        logger.debug("Configuring MQTT stub broker with: {}", mqttConfiguration);

        if (mqttConfiguration instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) mqttConfiguration;
            scenarioConfiguration.putAll(config);
        }
    }

    /**
     * Check if the broker is running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get the broker port.
     * 
     * @return Broker port
     */
    public int getPort() {
        return port;
    }

    /**
     * Run the broker loop.
     */
    private void runBroker() {
        try {
            while (!shutdown.get() && serverSocket != null && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                messageCount++;

                // Handle client connection in separate thread
                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            if (!shutdown.get()) {
                logger.error("Error in broker loop", e);
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
            // Simple MQTT-like response
            String response = "MQTT/1.0 200 OK\r\n" + "Content-Type: application/json\r\n" + "Content-Length: 25\r\n"
                    + "\r\n" + "{\"status\": \"connected\"}";

            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.close();
        } catch (IOException e) {
            logger.error("Error handling client", e);
        }
    }
}
