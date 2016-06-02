
package com.mulesoft.agent.monitoring.publisher.ingest;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Monitoring Ingest API Client
 */
public class AnypointMonitoringIngestAPIClient
{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnypointMonitoringIngestAPIClient.class);

    private final String targetMetricsPath;

    private final AuthProxyClient authProxyClient;

    private AnypointMonitoringIngestAPIClient(String apiVersion, AuthProxyClient authProxyClient)
    {
        this.targetMetricsPath = String.format("/monitoring/ingest/api/v%s/targets", apiVersion);
        this.authProxyClient = authProxyClient;
    }

    public static AnypointMonitoringIngestAPIClient create(String apiVersion, AuthProxyClient authProxyClient)
    {
        return new AnypointMonitoringIngestAPIClient(apiVersion, authProxyClient);
    }

    public void postTargetMetrics(final IngestTargetMetricPostBody body) {
        LOGGER.info(String.format("Sending %s to %s...", body.toString(), this.targetMetricsPath));
        this.authProxyClient.post(this.targetMetricsPath, Entity.json(body));
    }

}
