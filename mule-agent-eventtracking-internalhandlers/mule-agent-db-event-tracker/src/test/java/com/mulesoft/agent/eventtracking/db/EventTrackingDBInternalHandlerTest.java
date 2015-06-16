package com.mulesoft.agent.eventtracking.db;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class EventTrackingDBInternalHandlerTest
{

    @Test
    public void test ()
            throws SQLException, ClassNotFoundException, AgentEnableOperationException
    {
        EventTrackingDBInternalHandler handler = new EventTrackingDBInternalHandler();
        handler.driver = System.getProperty("driver");
        handler.jdbcUrl = System.getProperty("jdbcUrl");
        handler.user = System.getProperty("user");
        handler.pass = System.getProperty("pass");
        handler.eventsTable = System.getProperty("eventsTable");
        handler.annotationsTable = System.getProperty("annotationsTable");
        handler.businessTable = System.getProperty("businessTable");
        handler.postConfigurable();
        handler.enable(true);

        Connection conn = getConnection(handler);
        clearTable(conn, handler);
        List<AgentTrackingNotification> notifications = createNotifications();
        for (AgentTrackingNotification notification : notifications)
        {
            handler.handle(notification);
        }
        Assert.assertEquals(notifications.size(), countRecords(conn, handler.eventsTable));
        Assert.assertEquals(notifications.size() * 4, countRecords(conn, handler.businessTable));
        Assert.assertEquals(0, countRecords(conn, handler.annotationsTable));

        conn.close();
    }

    private long countRecords (Connection connection, String table)
            throws SQLException
    {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
        rs.next();
        long count = rs.getLong(1);
        rs.close();
        st.close();
        return count;
    }

    private void clearTable (Connection connection, EventTrackingDBInternalHandler agent)
            throws SQLException
    {
        Statement st = connection.createStatement();
        st.execute("DELETE FROM " + agent.businessTable);
        st.execute("DELETE FROM " + agent.annotationsTable);
        st.execute("DELETE FROM " + agent.eventsTable);
        st.close();
    }

    private Connection getConnection (EventTrackingDBInternalHandler agent)
            throws ClassNotFoundException, SQLException
    {
        Class.forName(agent.driver);
        return DriverManager.getConnection(agent.jdbcUrl, agent.user, agent.pass);
    }

    private List<AgentTrackingNotification> createNotifications ()
    {
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for (int i = 0; i < 10; i++)
        {
            Map<String, String> businessEvents = new HashMap<>();
            businessEvents.put("event-name", "new-customer");
            businessEvents.put("username", "jhondoe");
            businessEvents.put("ip", "200.13.18.94");
            businessEvents.put("channel", "cms");

            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .customEventProperties(businessEvents)
                    .build());
        }
        return list;
    }
}