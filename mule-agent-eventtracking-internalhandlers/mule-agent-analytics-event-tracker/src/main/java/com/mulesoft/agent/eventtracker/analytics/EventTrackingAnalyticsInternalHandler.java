/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracker.analytics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

@Singleton
@Named("mule.agent.tracking.handler.analytics")
public class EventTrackingAnalyticsInternalHandler extends BufferedHandler<AgentTrackingNotification> {

    private final String analyticsHost = "http://as-insight-ingest-int.qa.cloudhub.io/analytics/insight/ingest/api/v1/orgs/66310c16-bce5-43c4-b978-5945ed2f99c5/envs/543dc91ce4b045653c9178a3/workers/23/apps/5225/events/";

    private ObjectMapper objectMapper;

    @Configurable("true")
    protected boolean enabled;

    @Override
    protected boolean canHandle(AgentTrackingNotification agentTrackingNotification)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> notifications)
    {
        try
        {
            String serializedEvents = getMapper().writeValueAsString(notifications);

            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            Response response = asyncHttpClient.preparePut(analyticsHost).setHeader("Content-Type", "application/json").setBody(serializedEvents).execute().get();

            asyncHttpClient.close();

            return (response.getStatusCode() == HttpURLConnection.HTTP_OK);
        }
        catch (JsonProcessingException e)
        {
        }
        catch (IOException e)
        {
        }
        catch (InterruptedException e)
        {
        }
        catch (ExecutionException e)
        {
        }
        return false;
    }

    private ObjectMapper getMapper()
    {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();

            SimpleModule serializationModule = new SimpleModule("SerializationModule", new Version(1, 0, 0, null, null, null));

            serializationModule.addSerializer(new AnalyticsEventSerializer());
            objectMapper.registerModule(serializationModule);
        }

        return objectMapper;
    }
}
