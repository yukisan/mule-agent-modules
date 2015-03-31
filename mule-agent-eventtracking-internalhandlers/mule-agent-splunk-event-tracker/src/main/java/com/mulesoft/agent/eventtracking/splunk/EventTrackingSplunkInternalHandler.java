package com.mulesoft.agent.eventtracking.splunk;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.splunk.Args;
import com.splunk.Index;
import com.splunk.Service;
import com.splunk.ServiceArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * <p>
 * The Splunk Internal handler will store all the Event Notifications produced from the Mule ESB flows in Splunk instance.
 * </p>
 */

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends BufferedHandler<AgentTrackingNotification>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingSplunkInternalHandler.class);
    private final static MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    /**
     * <p>
     * Username to connect to Splunk.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String user;

    /**
     * <p>
     * The password of the user to connect to Splunk.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String pass;

    /**
     * <p>
     * IP or hostname of the server where Splunk is running.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String host;

    /**
     * <p>
     * Splunk management port.
     * Default: 8089
     * </p>
     */
    @Configurable(value = "8089", type = Type.DYNAMIC)
    int port;

    /**
     * <p>
     * Scheme of connection to the Splunk management port.
     * Default: https
     * </p>
     */
    @Configurable(value = "https", type = Type.DYNAMIC)
    String scheme;

    /**
     * <p>
     * Splunk index name where all the events will be sent.
     * If the user has the rights, and the index doesn't exists, then the internal handler will create it.
     * Default: main
     * </p>
     */
    @Configurable(value = "main", type = Type.DYNAMIC)
    String splunkIndexName;

    /**
     * <p>
     * The source used on the events sent to Splunk.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    String splunkSource;

    /**
     * <p>
     * The sourcetype used on the events sent to Splunk.
     * Default: _json
     * </p>
     */
    @Configurable(value = "_json", type = Type.DYNAMIC)
    String splunkSourceType;

    /**
     * <p>
     * Date format pattern used to calculate the formattedDate variable used on the eventTemplate.
     * Default: yyyy-MM-dd'T'HH:mm:ssZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ssZ", type = Type.DYNAMIC)
    String dateFormatPattern;

    /**
     * <p>
     * A Mustache template (https://mustache.github.io/) that's used to serialize the AgentTrackingNotification.
     * The template system provides you with a 'notification' and 'formattedDate' variables.
     * <li>
     *     notification: is the AgentTrackingNotification object with his fields.
     * </li>
     * <li>
     *     formattedDate: is the timestamp of the AgentTrackingNotification formatted using the dateFormatPattern.
     * </li>
     * Default: {
     *      "timestamp": "{{formattedDate}}",
     *      "application": "{{notification.application}}",
     *      "action": "{{notification.action}}",
     *      "notificationType": "{{notification.notificationType}}",
     *      "resourceIdentifier": "{{notification.resourceIdentifier}}",
     *      "source": "{{notification.source}}",
     *      "muleMessage": "{{notification.muleMessage}}",
     *      "path": "{{notification.path}}",
     *      "muleMessageId": "{{notification.muleMessageId}}"
     *  }
     * </p>
     */
    @Configurable(value = "{" +
            "\"timestamp\": \"{{formattedDate}}\"," +
            "\"application\": \"{{notification.application}}\"," +
            "\"notificationType\": \"{{notification.notificationType}}\"," +
            "\"muleMessage\": \"{{notification.muleMessage}}\"," +
            "\"action\": \"{{notification.action}}\"," +
            "\"resourceIdentifier\": \"{{notification.resourceIdentifier}}\"," +
            "\"source\": \"{{notification.source}}\",\n" +
            "\"path\": \"{{notification.path}}\",\n" +
            "\"muleMessageId\": \"{{notification.muleMessageId}}\"\n" +
            "}", type = Type.DYNAMIC)
    String eventTemplate;

    private SimpleDateFormat dateFormat;
    private Mustache template;
    private Service service;
    private Index index;
    private boolean isConfigured;

    /**
     * http://dev.splunk.com/view/java-sdk/SP-CAAAECX
     * By default, the token is valid for one hour, but is refreshed every time you make a call to splunkd.
     */
    private final int TOKEN_EXPIRATION_MS = 55 * 60 * 1000; // I use 55 minutes, instead of 60 as a safe net.
    private long lastConnection;

    @PostConfigure
    public void postConfigurable ()
    {
        super.postConfigurable();
        LOGGER.trace("Configuring the mule.agent.tracking.handler.splunk internal handler...");
        isConfigured = false;

        if (isNullOrWhiteSpace(this.host)
                || isNullOrWhiteSpace(this.user)
                || isNullOrWhiteSpace(this.pass)
                || isNullOrWhiteSpace(this.scheme)
                || isNullOrWhiteSpace(this.splunkIndexName)
                || isNullOrWhiteSpace(this.splunkSource)
                || isNullOrWhiteSpace(this.splunkSourceType)
                || isNullOrWhiteSpace(this.eventTemplate)
                || isNullOrWhiteSpace(this.dateFormatPattern))
        {
            LOGGER.error("Please review the EventTrackingSplunkInternalHandler (mule.agent.tracking.handler.splunk) configuration; " +
                    "You must configure at least the following properties: user, pass and host.");
            isConfigured = false;
            return;
        }

        try
        {
            dateFormat = new SimpleDateFormat(this.dateFormatPattern);
            dateFormat.format(new Date());
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error using the dateFormatPattern you provided. " +
                    "Please review the configuration.", e);
            isConfigured = false;
            return;
        }

        try
        {
            LOGGER.info(String.format("Connecting to the Splunk server: %s:%s.", this.host, this.port));
            ServiceArgs loginArgs = new ServiceArgs();
            loginArgs.setUsername(this.user);
            loginArgs.setPassword(this.pass);
            loginArgs.setHost(this.host);
            loginArgs.setPort(this.port);
            loginArgs.setScheme(this.scheme);
            service = Service.connect(loginArgs);
            lastConnection = System.currentTimeMillis();
            LOGGER.info("Successfully connected to the Splunk server.");
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error connecting to the Splunk server. Please review your settings.", e);
            isConfigured = false;
            return;
        }

        try
        {
            LOGGER.info(String.format("Retrieving the Splunk index: %s", this.splunkIndexName));
            index = service.getIndexes().get(this.splunkIndexName);
            if (index == null)
            {
                LOGGER.warn(String.format("Creating the index: %s", this.splunkIndexName));
                index = service.getIndexes().create(this.splunkIndexName);
                if (index == null)
                {
                    throw new Exception(String.format("Couldn't create the Splunk index: %s",
                            this.splunkIndexName));
                }
                LOGGER.info(String.format("Splunk index: %s, created successfully.", this.splunkIndexName));
            }
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error obtaining the Splunk index.", e);
            isConfigured = false;
            return;
        }

        try
        {
            template = mustacheFactory.compile(new StringReader(this.eventTemplate), "eventTemplate");
            // Append the Splunk event delimiter.
            template.append("\r\n");
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("There was an error compiling the event template: %s.", this.eventTemplate), e);
            isConfigured = false;
            return;
        }

        isConfigured = true;
        LOGGER.info("Successfully configured the mule.agent.tracking.handler.splunk internal handler ");
    }

    @Override
    protected boolean canHandle (AgentTrackingNotification message)
    {
        return isConfigured;
    }

    @Override
    protected boolean flush (final Collection<AgentTrackingNotification> messages)
    {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        try
        {
            // Check if the authentication token, isn't expired
            if ((System.currentTimeMillis() - lastConnection) >= TOKEN_EXPIRATION_MS)
            {
                LOGGER.info("Refreshing the session token.");
                service.login();
            }
            /**
             * http://dev.splunk.com/view/java-sdk/SP-CAAAEJ2#add2index
             * Says to use the attachWith() method, but it didn't accept parameters
             * in order to specify the sourcetype, host, etc...
             * That's why we use the common attach() method.
             */
            Socket socket = null;
            OutputStream output = null;
            OutputStreamWriter writer = null;
            try
            {
                Args args = new Args();
                args.put("source", this.splunkSource);
                args.put("sourcetype", this.splunkSourceType);
                socket = index.attach(args);
                output = socket.getOutputStream();
                writer = new OutputStreamWriter(output, Charset.forName("UTF8"));
                for (AgentTrackingNotification notification : messages)
                {
                    HashMap<String, Object> templateParams = new HashMap<>(2);
                    templateParams.put("notification", notification);
                    // Since we don't use Java 8, we don't have Function object to post-process the template.
                    // so we add a formatted date variable available to the template.
                    templateParams.put("formattedDate", dateFormat.format(notification.getTimestamp()));
                    template.execute(writer, templateParams).flush();
                }
                writer.flush();
                output.flush();
                lastConnection = System.currentTimeMillis() / 1000L;
                LOGGER.trace(String.format("Flushed %s notifications.", messages.size()));
                return true;
            }
            catch (IOException e)
            {
                LOGGER.error("There was an error sending the notifications to the Splunk instance.", e);
                return false;
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (output != null)
                {
                    output.close();
                }
                if (socket != null)
                {
                    socket.close();
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("There was an error closing the communication to the Splunk instance.", e);
            return false;
        }
    }

    public static boolean isNullOrWhiteSpace (String a)
    {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }
}
