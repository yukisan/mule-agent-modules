package com.mulesoft.agent.gw.http.log;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.module.client.model.HttpEvent;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class GatewayHttpEventsLogInternalHandlerTest
{
    @Test
    public void test ()
            throws AgentEnableOperationException, InitializationException
    {
        GatewayHttpEventsLogInternalHandler handler = new GatewayHttpEventsLogInternalHandler();
        handler.pattern = System.getProperty("pattern");
        handler.fileName = System.getProperty("fileName");
        handler.filePattern = System.getProperty("filePattern");
        handler.bufferSize = Integer.parseInt(System.getProperty("bufferSize"));
        handler.immediateFlush = Boolean.parseBoolean(System.getProperty("immediateFlush"));
        handler.daysTrigger = Integer.parseInt(System.getProperty("daysTrigger"));
        handler.mbTrigger = Integer.parseInt(System.getProperty("mbTrigger"));
        handler.enabled = Boolean.parseBoolean(System.getProperty("enabled"));
        handler.dateFormatPattern = System.getProperty("dateFormatPattern");
        handler.postConfigurable();
        handler.initialize();

        for (HttpEvent notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<HttpEvent> createNotifications ()
    {
        List<HttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++)
        {
            list.add(new HttpEvent(i, i, "ORG_ID", "HOST_ID", "CLIENT", "TRANSACTION", "1",
                    "192.168.1.1", "GET", "/path", 200, "AGENT", 100, 100, "", "", ""));
        }
        return list;
    }
}
