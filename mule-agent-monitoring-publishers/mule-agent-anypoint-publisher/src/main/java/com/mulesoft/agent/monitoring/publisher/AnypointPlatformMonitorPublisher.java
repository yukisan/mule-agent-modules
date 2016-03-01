/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentInitializationException;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to CloudWatch.
 * </p>
 */
@Named("anypoint.agent.monitor.publisher")
@Singleton
public class AnypointPlatformMonitorPublisher extends BufferedHandler<List<Metric>> {

    public static final String URL_FORMAT = "https://%s:%s";

    AsyncHttpClient client;

    @Configurable
    private AnypointPlatformCertificate certificate;

    @Configurable
    private String host;

    @Configurable
    private int port;

    @Override
    protected boolean canHandle(@NotNull List<Metric> metrics) { return true; }

    @Override
    protected boolean flush(@NotNull Collection<List<Metric>> collection)
    {
        RequestBuilder post = new RequestBuilder("POST");
        post.setUrl(String.format(URL_FORMAT, host, port));
        client.executeRequest(post.build());
        return true;
    }

    @PostConfigure
    public void createClient() throws Exception {
        if ( certificate == null ){
            throw new AgentInitializationException("Certificate must be specified for the Anypoint Platform monitoring publisher", null);
        }

        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().setSSLContext(certificate.createSSLContext()).build();
        client = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(config));
    }

}
