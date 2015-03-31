package com.mulesoft.agent.eventtracking.db;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * <p>
 * The DB Internal handler will store all
 * the Event Notifications produced from the Mule ESB flows in a configurable database.
 * </p>
 */

@Named("mule.agent.tracking.handler.database")
@Singleton
public class EventTrackingDBInternalHandler extends BufferedHandler<AgentTrackingNotification>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingDBInternalHandler.class);

    /**
     * <p>
     * JDBC driver to use to communicate with the database server.
     * </p>
     */
    @Configurable
    String driver;

    /**
     * <p>
     * JDBC URL to connect to the database server.
     * </p>
     */
    @Configurable
    String jdbcUrl;

    /**
     * <p>
     * Username in the database server.
     * </p>
     */
    @Configurable("")
    String user;

    /**
     * <p>
     * Password for the database user.
     * </p>
     */
    @Configurable("")
    String pass;

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_EVENTS'
     * </p>
     */
    @Configurable("MULE_EVENTS")
    String table;

    private String insertStatement;
    private boolean isConfigured;

    @Override
    public void postConfigurable ()
    {
        super.postConfigurable();
        LOGGER.trace("Configuring the DatabaseEventTrackingAgent");
        isConfigured = false;

        if (isNullOrWhiteSpace(driver)
                || isNullOrWhiteSpace(jdbcUrl))
        {
            LOGGER.error("Please review the DatabaseEventTrackingAgent (mule.agent.tracking.handler.database) configuration; " +
                    "You must configure the following properties: driver and jdbcUrl.");
            isConfigured = false;
            return;
        }

        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error(String.format("The DatabaseEventTrackingAgent (database.agent.eventtracking) couldn't load the database driver '%s'. " +
                    "Did you copy the JAR driver to the {MULE_HOME}/plugins/mule-agent-plugin/lib?", driver), e);
            isConfigured = false;
            return;
        }

        try
        {
            LOGGER.info("Testing database connection...");
            DriverManager.getConnection(this.jdbcUrl, this.user, this.pass).close();
            LOGGER.info("Database connection OK!.");
        }
        catch (SQLException e)
        {
            LOGGER.error(String.format("There was an error on the connection to the DataBase. Please review your agent configuration."), e);
            isConfigured = false;
            return;
        }

        insertStatement = String.format("INSERT INTO %s (action, application, mule_message, mule_message_id, notification_type, path, resource_identifier, timestamp, source) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", table);
        LOGGER.trace("Insert SQL Statement: " + insertStatement);
        isConfigured = true;
    }

    @Override
    protected boolean canHandle (AgentTrackingNotification message)
    {
        return isConfigured;
    }

    @Override
    protected boolean flush (Collection<AgentTrackingNotification> messages)
    {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(this.jdbcUrl, this.user, this.pass);
            PreparedStatement statement = null;
            try
            {
                statement = connection.prepareStatement(insertStatement);
                for (AgentTrackingNotification notification : messages)
                {
                    LOGGER.trace("Flushing Notification: " + notification);

                    statement.setString(1, notification.getAction());
                    statement.setString(2, notification.getApplication());
                    statement.setString(3, notification.getMuleMessage());
                    statement.setString(4, notification.getMuleMessageId());
                    statement.setString(5, notification.getNotificationType());
                    statement.setString(6, notification.getPath());
                    statement.setString(7, notification.getResourceIdentifier());
                    statement.setLong(8, notification.getTimestamp());
                    statement.setString(9, notification.getSource());
                    statement.addBatch();
                }

                statement.executeBatch();
            }
            finally
            {
                if (statement != null)
                {
                    statement.close();
                }
            }

            return true;
        }
        catch (SQLException e)
        {
            LOGGER.error("Couldn't insert the tracking notifications.", e);
            return false;
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    LOGGER.error("Error closing the database.", e);
                }
            }
        }
    }

    public static boolean isNullOrWhiteSpace (String a)
    {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }
}
