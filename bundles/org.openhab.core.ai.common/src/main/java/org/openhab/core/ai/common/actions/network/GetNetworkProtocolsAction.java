package org.openhab.core.ai.common.actions.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get information about network protocols
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkProtocolsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkProtocolsAction.class);

    private static final String ACTION_ID = "get_network_protocols";
    private static final String ACTION_NAME = "Get Network Protocols";
    private static final String DESCRIPTION = "Retrieves information about network protocols and their characteristics";
    private static final String CATEGORY = "network";
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

        Map<String, Object> properties = new HashMap<>();
        properties.put("includeTransportProtocols",
                Map.of("type", "boolean", "description", "Include transport layer protocols", "default", true));
        properties.put("includeApplicationProtocols",
                Map.of("type", "boolean", "description", "Include application layer protocols", "default", true));
        properties.put("includeSecurityProtocols",
                Map.of("type", "boolean", "description", "Include security protocols", "default", true));
        properties.put("includeProtocolDetails",
                Map.of("type", "boolean", "description", "Include detailed protocol information", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("transportProtocols", Map.of("type", "array", "description", "Transport layer protocols"));
        properties.put("applicationProtocols", Map.of("type", "array", "description", "Application layer protocols"));
        properties.put("securityProtocols", Map.of("type", "array", "description", "Security protocols"));
        properties.put("protocolSummary", Map.of("type", "object", "description", "Protocol summary information"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the protocol information"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("protocol_information", true);
        capabilities.put("transport_protocols", true);
        capabilities.put("application_protocols", true);
        capabilities.put("security_protocols", true);
        return capabilities;
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeTransportProtocols", errors);
        validateBooleanParameter(parameters, "includeApplicationProtocols", errors);
        validateBooleanParameter(parameters, "includeSecurityProtocols", errors);
        validateBooleanParameter(parameters, "includeProtocolDetails", errors);

        if (errors.isEmpty()) {
            return AIActionValidationResult.valid(parameters);
        } else {
            return AIActionValidationResult.invalid(errors);
        }
    }

    private void validateBooleanParameter(Map<String, Object> parameters, String paramName, List<String> errors) {
        Object value = parameters.get(paramName);
        if (value != null && !(value instanceof Boolean)) {
            errors.add(paramName + " must be a boolean");
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing GetNetworkProtocolsAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeTransportProtocols = (Boolean) parameters.getOrDefault("includeTransportProtocols", true);
            boolean includeApplicationProtocols = (Boolean) parameters.getOrDefault("includeApplicationProtocols",
                    true);
            boolean includeSecurityProtocols = (Boolean) parameters.getOrDefault("includeSecurityProtocols", true);
            boolean includeProtocolDetails = (Boolean) parameters.getOrDefault("includeProtocolDetails", true);

            Map<String, Object> result = new HashMap<>();

            // Get transport protocols
            if (includeTransportProtocols) {
                result.put("transportProtocols", getTransportProtocols(includeProtocolDetails));
            }

            // Get application protocols
            if (includeApplicationProtocols) {
                result.put("applicationProtocols", getApplicationProtocols(includeProtocolDetails));
            }

            // Get security protocols
            if (includeSecurityProtocols) {
                result.put("securityProtocols", getSecurityProtocols(includeProtocolDetails));
            }

            // Generate protocol summary
            result.put("protocolSummary", generateProtocolSummary(result));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network protocols: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get network protocols: " + e.getMessage(), e);
        }
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
        logger.debug("GetNetworkProtocolsAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkProtocolsAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> getTransportProtocols(boolean includeDetails) {
        List<Map<String, Object>> protocols = new ArrayList<>();

        // TCP
        Map<String, Object> tcp = new HashMap<>();
        tcp.put("name", "TCP");
        tcp.put("fullName", "Transmission Control Protocol");
        tcp.put("layer", "Transport");
        tcp.put("portRange", "1-65535");
        tcp.put("reliability", "Reliable");
        tcp.put("connectionOriented", true);
        tcp.put("flowControl", true);
        tcp.put("errorChecking", true);
        if (includeDetails) {
            tcp.put("description",
                    "Connection-oriented protocol that provides reliable, ordered, and error-checked delivery of data");
            tcp.put("useCases", List.of("Web browsing (HTTP/HTTPS)", "Email (SMTP, POP3, IMAP)", "File transfer (FTP)",
                    "Remote access (SSH, Telnet)"));
            tcp.put("features", List.of("Three-way handshake", "Flow control", "Congestion control",
                    "Error detection and recovery"));
        }
        protocols.add(tcp);

        // UDP
        Map<String, Object> udp = new HashMap<>();
        udp.put("name", "UDP");
        udp.put("fullName", "User Datagram Protocol");
        udp.put("layer", "Transport");
        udp.put("portRange", "1-65535");
        udp.put("reliability", "Unreliable");
        udp.put("connectionOriented", false);
        udp.put("flowControl", false);
        udp.put("errorChecking", false);
        if (includeDetails) {
            udp.put("description",
                    "Connectionless protocol that provides fast, lightweight data transmission without reliability guarantees");
            udp.put("useCases", List.of("DNS queries", "DHCP", "Streaming media", "Online gaming", "VoIP"));
            udp.put("features", List.of("No connection establishment", "No flow control", "No congestion control",
                    "Minimal overhead"));
        }
        protocols.add(udp);

        return protocols;
    }

    private List<Map<String, Object>> getApplicationProtocols(boolean includeDetails) {
        List<Map<String, Object>> protocols = new ArrayList<>();

        // HTTP
        Map<String, Object> http = new HashMap<>();
        http.put("name", "HTTP");
        http.put("fullName", "Hypertext Transfer Protocol");
        http.put("layer", "Application");
        http.put("defaultPort", 80);
        http.put("transport", "TCP");
        http.put("security", "None");
        if (includeDetails) {
            http.put("description", "Protocol for transmitting hypermedia documents on the World Wide Web");
            http.put("methods", List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"));
            http.put("statusCodes", List.of("1xx Informational", "2xx Success", "3xx Redirection", "4xx Client Error",
                    "5xx Server Error"));
            http.put("headers", List.of("Content-Type", "Content-Length", "User-Agent", "Accept", "Authorization"));
        }
        protocols.add(http);

        // HTTPS
        Map<String, Object> https = new HashMap<>();
        https.put("name", "HTTPS");
        https.put("fullName", "HTTP Secure");
        https.put("layer", "Application");
        https.put("defaultPort", 443);
        https.put("transport", "TCP");
        https.put("security", "TLS/SSL");
        if (includeDetails) {
            https.put("description", "HTTP over TLS/SSL for secure communication");
            https.put("encryption", "End-to-end encryption");
            https.put("authentication", "Certificate-based");
            https.put("benefits", List.of("Data confidentiality", "Data integrity", "Server authentication"));
        }
        protocols.add(https);

        // FTP
        Map<String, Object> ftp = new HashMap<>();
        ftp.put("name", "FTP");
        ftp.put("fullName", "File Transfer Protocol");
        ftp.put("layer", "Application");
        ftp.put("defaultPort", 21);
        ftp.put("transport", "TCP");
        ftp.put("security", "None");
        if (includeDetails) {
            ftp.put("description", "Standard network protocol for transferring files between client and server");
            ftp.put("modes", List.of("Active mode", "Passive mode"));
            ftp.put("commands", List.of("USER", "PASS", "LIST", "RETR", "STOR", "QUIT"));
        }
        protocols.add(ftp);

        // SMTP
        Map<String, Object> smtp = new HashMap<>();
        smtp.put("name", "SMTP");
        smtp.put("fullName", "Simple Mail Transfer Protocol");
        smtp.put("layer", "Application");
        smtp.put("defaultPort", 25);
        smtp.put("transport", "TCP");
        smtp.put("security", "None");
        if (includeDetails) {
            smtp.put("description", "Protocol for sending email messages between servers");
            smtp.put("commands", List.of("HELO", "MAIL FROM", "RCPT TO", "DATA", "QUIT"));
            smtp.put("extensions", List.of("STARTTLS", "AUTH", "SIZE"));
        }
        protocols.add(smtp);

        // DNS
        Map<String, Object> dns = new HashMap<>();
        dns.put("name", "DNS");
        dns.put("fullName", "Domain Name System");
        dns.put("layer", "Application");
        dns.put("defaultPort", 53);
        dns.put("transport", "UDP/TCP");
        dns.put("security", "None");
        if (includeDetails) {
            dns.put("description", "Distributed naming system for computers, services, and resources");
            dns.put("recordTypes", List.of("A", "AAAA", "CNAME", "MX", "NS", "PTR", "TXT"));
            dns.put("queryTypes", List.of("Recursive", "Iterative"));
        }
        protocols.add(dns);

        return protocols;
    }

    private List<Map<String, Object>> getSecurityProtocols(boolean includeDetails) {
        List<Map<String, Object>> protocols = new ArrayList<>();

        // TLS
        Map<String, Object> tls = new HashMap<>();
        tls.put("name", "TLS");
        tls.put("fullName", "Transport Layer Security");
        tls.put("layer", "Transport");
        tls.put("versions", List.of("1.0", "1.1", "1.2", "1.3"));
        tls.put("security", "Strong");
        if (includeDetails) {
            tls.put("description",
                    "Cryptographic protocol designed to provide communications security over a computer network");
            tls.put("features", List.of("Encryption", "Authentication", "Integrity", "Forward secrecy"));
            tls.put("cipherSuites", List.of("AES", "ChaCha20", "RSA", "ECDSA", "ECDHE"));
        }
        protocols.add(tls);

        // SSL
        Map<String, Object> ssl = new HashMap<>();
        ssl.put("name", "SSL");
        ssl.put("fullName", "Secure Sockets Layer");
        ssl.put("layer", "Transport");
        ssl.put("versions", List.of("2.0", "3.0"));
        ssl.put("security", "Deprecated");
        if (includeDetails) {
            ssl.put("description", "Deprecated cryptographic protocol (replaced by TLS)");
            ssl.put("status", "Deprecated due to security vulnerabilities");
            ssl.put("replacement", "TLS");
        }
        protocols.add(ssl);

        // SSH
        Map<String, Object> ssh = new HashMap<>();
        ssh.put("name", "SSH");
        ssh.put("fullName", "Secure Shell");
        ssh.put("layer", "Application");
        ssh.put("defaultPort", 22);
        ssh.put("transport", "TCP");
        ssh.put("security", "Strong");
        if (includeDetails) {
            ssh.put("description", "Cryptographic network protocol for secure remote access and file transfer");
            ssh.put("authentication", List.of("Password", "Public key", "Keyboard-interactive"));
            ssh.put("features", List.of("Port forwarding", "X11 forwarding", "SFTP", "SCP"));
        }
        protocols.add(ssh);

        // IPsec
        Map<String, Object> ipsec = new HashMap<>();
        ipsec.put("name", "IPsec");
        ipsec.put("fullName", "Internet Protocol Security");
        ipsec.put("layer", "Network");
        ipsec.put("security", "Strong");
        if (includeDetails) {
            ipsec.put("description", "Suite of protocols for securing IP communications");
            ipsec.put("modes", List.of("Transport mode", "Tunnel mode"));
            ipsec.put("protocols", List.of("AH (Authentication Header)", "ESP (Encapsulating Security Payload)"));
        }
        protocols.add(ipsec);

        return protocols;
    }

    private Map<String, Object> generateProtocolSummary(Map<String, Object> protocols) {
        Map<String, Object> summary = new HashMap<>();

        int totalProtocols = 0;
        int transportProtocols = 0;
        int applicationProtocols = 0;
        int securityProtocols = 0;

        if (protocols.containsKey("transportProtocols")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transportList = (List<Map<String, Object>>) protocols.get("transportProtocols");
            transportProtocols = transportList.size();
            totalProtocols += transportProtocols;
        }

        if (protocols.containsKey("applicationProtocols")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> applicationList = (List<Map<String, Object>>) protocols
                    .get("applicationProtocols");
            applicationProtocols = applicationList.size();
            totalProtocols += applicationProtocols;
        }

        if (protocols.containsKey("securityProtocols")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> securityList = (List<Map<String, Object>>) protocols.get("securityProtocols");
            securityProtocols = securityList.size();
            totalProtocols += securityProtocols;
        }

        summary.put("totalProtocols", totalProtocols);
        summary.put("transportProtocols", transportProtocols);
        summary.put("applicationProtocols", applicationProtocols);
        summary.put("securityProtocols", securityProtocols);

        return summary;
    }
}
