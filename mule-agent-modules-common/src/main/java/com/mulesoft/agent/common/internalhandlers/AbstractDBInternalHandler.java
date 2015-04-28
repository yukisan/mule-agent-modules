package com.mulesoft.agent.common.internalhandlers;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public abstract class AbstractDBInternalHandler<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractDBInternalHandler.class);

    private String insertStatement;
    private boolean isConfigured;

    /**
     * <p>
     * JDBC driver to use to communicate with the database server.
     * </p>
     */
    @Configurable
    public String driver;

    /**
     * <p>
     * JDBC URL to connect to the database server.
     * </p>
     */
    @Configurable
    public String jdbcUrl;

    /**
     * <p>
     * Username in the database server.
     * </p>
     */
    @Configurable()
    public String user;

    /**
     * <p>
     * Password for the database user.
     * </p>
     */
    @Configurable()
    public String pass;

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_EVENTS'
     * </p>
     */
    @Configurable("MULE_EVENTS")
    public String table;

    protected abstract String getInsertStatement (String table);

    protected abstract void fillStatement (PreparedStatement statement, T notification) throws SQLException;

    @Override
    protected boolean canHandle (T message)
    {
        return true;
    }

    @Override
    protected boolean flush (Collection<T> messages)
    {
        if (!this.isConfigured)
        {
            return false;
        }

        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(this.jdbcUrl, this.user, this.pass);
            PreparedStatement statement = null;
            try
            {
                statement = connection.prepareStatement(this.insertStatement);
                for (T message : messages)
                {
                    LOGGER.trace("Flushing Message: " + message);
                    fillStatement(statement, message);
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

    @Override
    public void postConfigurable ()
    {
        super.postConfigurable();
        LOGGER.trace("Configuring the AbstractDBInternalHandler...");
        this.isConfigured = false;

        if (StringUtils.isEmpty(this.driver)
                || StringUtils.isEmpty(this.jdbcUrl))
        {
            LOGGER.error("Please review the DatabaseEventTrackingAgent (mule.agent.tracking.handler.database) configuration; " +
                    "You must configure the following properties: driver and jdbcUrl.");
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
            return;
        }

        try
        {
            LOGGER.trace("Testing database connection...");
            DriverManager.getConnection(this.jdbcUrl, this.user, this.pass).close();
            LOGGER.trace("Database connection OK!.");
        }
        catch (SQLException e)
        {
            LOGGER.error(String.format("There was an error on the connection to the DataBase. Please review your agent configuration."), e);
            return;
        }

        this.insertStatement = getInsertStatement(this.table);
        this.isConfigured = true;
        LOGGER.trace("Insert SQL Statement: " + insertStatement);
        LOGGER.trace("Successfully configured the AbstractDBInternalHandler internal handler.");
    }
}
