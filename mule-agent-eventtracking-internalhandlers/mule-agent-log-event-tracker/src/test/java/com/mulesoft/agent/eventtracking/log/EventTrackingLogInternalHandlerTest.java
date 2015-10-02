package com.mulesoft.agent.eventtracking.log;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class EventTrackingLogInternalHandlerTest
{
    @Test
    public void test ()
            throws AgentEnableOperationException
    {
        EventTrackingLogInternalHandler handler = new EventTrackingLogInternalHandler();
        handler.fileName = System.getProperty("fileName");
        handler.filePattern = System.getProperty("filePattern");
        handler.bufferSize = Integer.parseInt(System.getProperty("bufferSize"));
        handler.immediateFlush = Boolean.parseBoolean(System.getProperty("immediateFlush"));
        handler.daysTrigger = Integer.parseInt(System.getProperty("daysTrigger"));
        handler.mbTrigger = Integer.parseInt(System.getProperty("mbTrigger"));
        handler.enabled = Boolean.parseBoolean(System.getProperty("enabled"));
        handler.dateFormatPattern = System.getProperty("dateFormatPattern");
        handler.postConfigurable();

        for (AgentTrackingNotification notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<AgentTrackingNotification> createNotifications ()
    {
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for (int i = 0; i < 100000; i++)
        {
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .build());
        }
        return list;
    }
}
