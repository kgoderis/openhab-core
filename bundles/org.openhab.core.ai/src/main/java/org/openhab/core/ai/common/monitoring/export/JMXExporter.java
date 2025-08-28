package org.openhab.core.ai.common.monitoring.export;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsExporter;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMX metrics exporter implementation.
 * 
 * <p>
 * This exporter exposes metrics data through JMX MBeans, making it accessible
 * through standard JMX monitoring tools and management consoles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsExporter.class, property = { "type=jmx" })
@NonNullByDefault
public class JMXExporter implements MetricsExporter {

    private static final Logger logger = LoggerFactory.getLogger(JMXExporter.class);
    private final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    @Override
    public String exportPrometheus(MetricsService metricsService) {
        logger.debug("JMXExporter does not support Prometheus export, delegating to PrometheusExporter");
        // This exporter specializes in JMX format, Prometheus should be handled by PrometheusExporter
        return "";
    }

    @Override
    public String exportJSON(MetricsService metricsService) {
        logger.debug("JMXExporter does not support JSON export, delegating to JSONExporter");
        // This exporter specializes in JMX format, JSON should be handled by JSONExporter
        return "{}";
    }

    @Override
    public void exportJMX(MetricsService metricsService) {
        logger.debug("Exporting metrics to JMX");

        try {
            // Get all snapshots and register them as MBeans
            List<GenericMetricsSnapshot> snapshots = metricsService.getAllSnapshots(GenericMetricsSnapshot.class);

            for (GenericMetricsSnapshot snapshot : snapshots) {
                String objectName = String.format("org.openhab.core.ai:type=Metrics,domain=%s,operation=%s",
                        escapeObjectName(snapshot.domain()), escapeObjectName(snapshot.operation()));

                try {
                    ObjectName name = new ObjectName(objectName);

                    // Unregister existing MBean if it exists
                    if (mbeanServer.isRegistered(name)) {
                        mbeanServer.unregisterMBean(name);
                    }

                    // Create and register new MBean
                    MetricsMBean mbean = new MetricsMBean(snapshot);
                    mbeanServer.registerMBean(mbean, name);

                    logger.debug("Registered JMX MBean: {}", objectName);

                } catch (Exception e) {
                    logger.warn("Failed to register JMX MBean for: {}", objectName, e);
                }
            }

            // Register domain aggregated MBeans
            List<String> domains = snapshots.stream().map(GenericMetricsSnapshot::domain).distinct().toList();

            for (String domain : domains) {
                String objectName = String.format("org.openhab.core.ai:type=DomainMetrics,domain=%s",
                        escapeObjectName(domain));

                try {
                    ObjectName name = new ObjectName(objectName);

                    // Unregister existing MBean if it exists
                    if (mbeanServer.isRegistered(name)) {
                        mbeanServer.unregisterMBean(name);
                    }

                    // Create domain aggregated snapshot and register as MBean
                    var domainSnapshot = metricsService.getDomainAggregatedSnapshot(domain);
                    DomainMetricsMBean mbean = new DomainMetricsMBean(domainSnapshot);
                    mbeanServer.registerMBean(mbean, name);

                    logger.debug("Registered JMX Domain MBean: {}", objectName);

                } catch (Exception e) {
                    logger.warn("Failed to register JMX Domain MBean for: {}", objectName, e);
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to export metrics to JMX", e);
        }
    }

    /**
     * Escape object name components for JMX.
     * 
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeObjectName(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", "\\,").replace("=", "\\=").replace(":", "\\:").replace("*", "\\*").replace("?", "\\?")
                .replace("\"", "\\\"");
    }

    /**
     * Dynamic MBean for individual metrics snapshots.
     */
    public static class MetricsMBean implements DynamicMBean {
        private final GenericMetricsSnapshot snapshot;

        public MetricsMBean(GenericMetricsSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Object getAttribute(String attribute) {
            return switch (attribute) {
                case "Domain" -> snapshot.domain();
                case "Operation" -> snapshot.operation();
                case "Total" -> snapshot.total();
                case "Success" -> snapshot.success();
                case "Failure" -> snapshot.failure();
                case "TotalDurationNanos" -> snapshot.totalDurationNanos();
                case "SuccessRate" -> snapshot.total() > 0 ? (snapshot.success() * 100.0) / snapshot.total() : 0.0;
                case "AverageDurationMs" ->
                    snapshot.total() > 0 ? (snapshot.totalDurationNanos() / 1_000_000.0) / snapshot.total() : 0.0;
                case "Timestamp" -> snapshot.timestamp().toEpochMilli();
                default -> null;
            };
        }

        @Override
        public void setAttribute(Attribute attribute) {
            // Read-only MBean
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attribute : attributes) {
                Object value = getAttribute(attribute);
                if (value != null) {
                    list.add(new Attribute(attribute, value));
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            // Read-only MBean
            return new AttributeList();
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) {
            // No operations supported
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attributes = {
                    new MBeanAttributeInfo("Domain", "java.lang.String", "Operation domain", true, false, false),
                    new MBeanAttributeInfo("Operation", "java.lang.String", "Operation name", true, false, false),
                    new MBeanAttributeInfo("Total", "long", "Total operations", true, false, false),
                    new MBeanAttributeInfo("Success", "long", "Successful operations", true, false, false),
                    new MBeanAttributeInfo("Failure", "long", "Failed operations", true, false, false),
                    new MBeanAttributeInfo("TotalDurationNanos", "long", "Total duration in nanoseconds", true, false,
                            false),
                    new MBeanAttributeInfo("SuccessRate", "double", "Success rate percentage", true, false, false),
                    new MBeanAttributeInfo("AverageDurationMs", "double", "Average duration in milliseconds", true,
                            false, false),
                    new MBeanAttributeInfo("Timestamp", "long", "Snapshot timestamp", true, false, false) };

            return new MBeanInfo("MetricsMBean", "OpenHAB AI Metrics", attributes, null, null, null);
        }
    }

    /**
     * Dynamic MBean for domain aggregated metrics.
     */
    public static class DomainMetricsMBean implements DynamicMBean {
        private final org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot snapshot;

        public DomainMetricsMBean(
                org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Object getAttribute(String attribute) {
            return switch (attribute) {
                case "Domain" -> snapshot.domain();
                case "TotalOperations" -> snapshot.totalOperations();
                case "SuccessfulOperations" -> snapshot.successfulOperations();
                case "FailedOperations" -> snapshot.failedOperations();
                case "TotalDurationNanos" -> snapshot.totalDurationNanos();
                case "AverageSuccessRate" -> snapshot.averageSuccessRate();
                case "SuccessRate" -> snapshot.getSuccessRate();
                case "AverageDurationMs" -> snapshot.getAverageDurationMs();
                case "OperationsPerSecond" -> snapshot.getOperationsPerSecond();
                case "Timestamp" -> snapshot.timestamp().toEpochMilli();
                default -> null;
            };
        }

        @Override
        public void setAttribute(Attribute attribute) {
            // Read-only MBean
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attribute : attributes) {
                Object value = getAttribute(attribute);
                if (value != null) {
                    list.add(new Attribute(attribute, value));
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            // Read-only MBean
            return new AttributeList();
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) {
            // No operations supported
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attributes = {
                    new MBeanAttributeInfo("Domain", "java.lang.String", "Domain name", true, false, false),
                    new MBeanAttributeInfo("TotalOperations", "long", "Total operations", true, false, false),
                    new MBeanAttributeInfo("SuccessfulOperations", "long", "Successful operations", true, false, false),
                    new MBeanAttributeInfo("FailedOperations", "long", "Failed operations", true, false, false),
                    new MBeanAttributeInfo("TotalDurationNanos", "long", "Total duration in nanoseconds", true, false,
                            false),
                    new MBeanAttributeInfo("AverageSuccessRate", "double", "Average success rate", true, false, false),
                    new MBeanAttributeInfo("SuccessRate", "double", "Success rate percentage", true, false, false),
                    new MBeanAttributeInfo("AverageDurationMs", "double", "Average duration in milliseconds", true,
                            false, false),
                    new MBeanAttributeInfo("OperationsPerSecond", "double", "Operations per second", true, false,
                            false),
                    new MBeanAttributeInfo("Timestamp", "long", "Snapshot timestamp", true, false, false) };

            return new MBeanInfo("DomainMetricsMBean", "OpenHAB AI Domain Metrics", attributes, null, null, null);
        }
    }
}
