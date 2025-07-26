# Production Deployment Guide: openHAB MCP Bundle

This guide describes best practices for deploying the openHAB MCP bundle in a production environment.

## 1. OSGi Deployment
- Place the MCP bundle JAR in your OSGi container's `addons` or `bundles` directory.
- Start the OSGi container (e.g., openHAB, Karaf, Equinox).
- Configure environment variables as needed (see below).

## 2. Docker Deployment
- Use the provided `Dockerfile` to build a container image:
  ```sh
  docker build -t openhab-mcp-bundle .
  ```
- Run the container:
  ```sh
  docker run -d --name mcp-server -e MCP_SERVER_ID=my-server -e MCP_TRANSPORT_TYPE=STDIO -p 8080:8080 openhab-mcp-bundle
  ```
- Mount configuration files or volumes as needed.

## 3. Environment Variables
- `MCP_SERVER_ID`: Unique server identifier
- `MCP_SERVER_NAME`: Human-readable name
- `MCP_SERVER_VERSION`: Version string
- `MCP_TRANSPORT_TYPE`: STDIO or SSE
- `MCP_AUTH_TOKEN`: (Optional) Authentication token
- `MCP_MAX_CONNECTIONS`: (Optional) Max concurrent connections
- `MCP_RATE_LIMIT`: (Optional) Requests per minute

## 4. JVM Tuning
- Set JVM options for memory and GC tuning:
  ```sh
  java -Xms512m -Xmx2g -XX:+UseG1GC -jar mcp-bundle.jar
  ```
- Adjust heap size and GC options based on workload.

## 5. Health & Metrics Endpoints
- If enabled, health checks are available at `/health` (HTTP, port 8080).
- Metrics are available at `/metrics` (Prometheus format).
- Configure endpoints via environment variables or configuration file.

## 6. Graceful Shutdown
- Send SIGTERM to the process or use OSGi shutdown command.
- The server will close connections and complete in-flight requests before exiting.
- Configure shutdown timeout as needed (default: 30s).

## 7. Security
- Enable authentication and request validation in production.
- Use strong, unique tokens for `MCP_AUTH_TOKEN`.
- Restrict network access to trusted clients.

## 8. Monitoring
- Integrate with Prometheus, Grafana, or other monitoring tools.
- Set up alerting for health check failures and high error rates.

---
For further details, see the main README and USAGE_EXAMPLES. 