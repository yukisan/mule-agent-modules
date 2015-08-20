package com.mulesoft.agent.gw.http.splunk;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.module.client.model.HttpEvent;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Ignore
public class GatewayHttpEventsSplunkInternalHandlerTest
{

    @Test
    public void test ()
            throws IOException, AgentEnableOperationException
    {
        GatewayHttpEventsSplunkInternalHandler handler = new GatewayHttpEventsSplunkInternalHandler();
        handler.setEnabled(true);
        handler.user = System.getProperty("user");
        handler.pass = System.getProperty("pass");
        handler.host = System.getProperty("host");
        handler.port = Integer.parseInt(System.getProperty("port"));
        handler.scheme = System.getProperty("scheme");
        handler.sslSecurityProtocol = System.getProperty("sslSecurityProtocol");
        handler.splunkIndexName = System.getProperty("splunkIndexName");
        handler.splunkSource = System.getProperty("splunkSource");
        handler.splunkSourceType = System.getProperty("splunkSourceType");
        handler.dateFormatPattern = System.getProperty("dateFormatPattern");

        handler.postConfigurable();

        boolean success = true;
        for (HttpEvent notification : createNotifications())
        {
            success &= handler.handle(notification);
        }

        Assert.assertTrue(success);
    }

    private List<HttpEvent> createNotifications ()
    {
        List<HttpEvent> list = new ArrayList<HttpEvent>();
        for (int i = 0; i < 1000; i++)
        {
            list.add(new HttpEvent(i, i, "ORG_ID", "HOST_ID", "CLIENT", "TRANSACTION", "1",
                    "192.168.1.1", "GET", "/path", 200, "AGENT", 100, 100, "", "", ""));
        }
        return list;
    }
}
