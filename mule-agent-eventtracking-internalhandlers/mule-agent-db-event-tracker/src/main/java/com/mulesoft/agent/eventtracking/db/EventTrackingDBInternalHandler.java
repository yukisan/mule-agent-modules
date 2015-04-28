package com.mulesoft.agent.eventtracking.db;

import com.mulesoft.agent.common.internalhandlers.AbstractDBInternalHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>
 * The DB Internal handler will store all
 * the Event Notifications produced from the Mule ESB flows in a configurable database.
 * </p>
 */
@Named("mule.agent.tracking.handler.database")
@Singleton
public class EventTrackingDBInternalHandler extends AbstractDBInternalHandler<AgentTrackingNotification>
{
    @Override
    protected String getInsertStatement (String table)
    {
        return String.format("INSERT INTO %s (action, application, mule_message, mule_message_id, notification_type, path, resource_identifier, timestamp, source) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", table);
    }

    @Override
    protected void fillStatement (PreparedStatement statement, AgentTrackingNotification notification) throws SQLException
    {
        statement.setString(1, notification.getAction());
        statement.setString(2, notification.getApplication());
        statement.setString(3, notification.getMuleMessage());
        statement.setString(4, notification.getMuleMessageId());
        statement.setString(5, notification.getNotificationType());
        statement.setString(6, notification.getPath());
        statement.setString(7, notification.getResourceIdentifier());
        statement.setLong(8, notification.getTimestamp());
        statement.setString(9, notification.getSource());
    }
}
