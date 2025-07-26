# Health Check & Metrics Endpoints: openHAB MCP Bundle

## Health Check Endpoint
- **Path:** `/health`
- **Method:** GET
- **Description:** Returns the current health status of the MCP server and transport.
- **Example Response:**
  ```json
  {
    "status": "UP",
    "transport": {
      "type": "STDIO",
      "healthy": true,
      "uptime": 123456,
      "lastError": null
    },
    "uptime": 1234567,
    "version": "1.0.0"
  }
  ```
- **Config:** Enable/disable via `enableHealthChecks` in configuration or `MCP_ENABLE_HEALTHCHECKS` env var.

## Metrics Endpoint
- **Path:** `/metrics`
- **Method:** GET
- **Description:** Exposes Prometheus-compatible metrics for monitoring.
- **Example Response:**
  ```
  # HELP mcp_requests_total Total number of requests
  # TYPE mcp_requests_total counter
  mcp_requests_total 12345
  # HELP mcp_errors_total Total number of errors
  # TYPE mcp_errors_total counter
  mcp_errors_total 12
  # HELP mcp_uptime_seconds Uptime in seconds
  # TYPE mcp_uptime_seconds gauge
  mcp_uptime_seconds 123456
  ```
- **Config:** Enable/disable via `enableMetrics` in configuration or `MCP_ENABLE_METRICS` env var.

## Notes
- Endpoints are available only if enabled in configuration.
- Port is configurable (default: 8080).
- Use for integration with Prometheus, Grafana, or other monitoring tools. 