/*
* (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
* law. All use of this software is subject to MuleSoft's Master Subscription Agreement
* (or other master license agreement) separately entered into in writing between you and
* MuleSoft. If such an agreement is not in place, you may not use the software.
*/

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Zabbix instance.
 * Utilizes version 2.0 of the Zabbix Sender protocol.
 * </p>
 */
@Named("mule.agent.zabbix.jmx.internal.handler")
@Singleton
public class ZabbixMonitorPublisher extends BufferedHandler<List<Metric>>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ZabbixMonitorPublisher.class);

    private static final String MESSAGE_START = "{\"request\":\"sender data\",\"data\":[{\"host\":\"";
    private static final String MESSAGE_MIDDLE_LEFT = "\",\"key\":\"";
    private static final String MESSAGE_MIDDLE_RIGHT = "\",\"value\":\"";
    private static final String MESSAGE_END = "\"}]}\n";

    /**
     * <p>
     * Host name defined in Zabbix for it to recognize the source of the metric.
     * </p>
     */
    @Configurable("com.mulesoft.agent")
    String host;

    /**
     * <p>
     * Address corresponding to the Zabbix server.
     * </p>
     */
    @Configurable("0.0.0.0")
    String zabbixServer;

    /**
     * <p>
     * Port corresponding to the Zabbix server.
     * </p>
     */
    @Configurable("10051")
    int zabbixPort;

    @Inject
    public ZabbixMonitorPublisher()
    {
        super();
    }

    public ZabbixMonitorPublisher(OnOffSwitch enabledSwitch)
    {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    public boolean canHandle(@NotNull List<Metric> metrics) {
        return true;
    }

    @Override
    public boolean flush(@NotNull Collection<List<Metric>> listOfMetrics)
    {
        Socket zabbixConnection = null;
        OutputStream out = null;
        BufferedReader in = null;
        try
        {
            for (List<Metric> metrics : listOfMetrics)
            {
                for (Metric metric : metrics)
                {
                    zabbixConnection = new Socket(zabbixServer, zabbixPort);

                    StringBuilder message = new StringBuilder();
                    message.append(MESSAGE_START);
                    message.append(host);
                    message.append(MESSAGE_MIDDLE_LEFT);
                    message.append(metric.getName().replaceAll("\\s", "").replace(":", ""));
                    message.append(MESSAGE_MIDDLE_RIGHT);
                    message.append(metric.getValue());
                    message.append(MESSAGE_END);

                    String s = message.toString();

                    byte[] chars = s.getBytes();
                    int length = chars.length;
                    out = zabbixConnection.getOutputStream();
                    out.write(new byte[]{
                            'Z', 'B', 'X', 'D',
                            '\1',
                            (byte) (length & 0xFF),
                            (byte) ((length >> 8) & 0x00FF),
                            (byte) ((length >> 16) & 0x0000FF),
                            (byte) ((length >> 24) & 0x000000FF),
                            '\0', '\0', '\0', '\0'});


                    out.write(chars);
                    out.flush();

                    in = new BufferedReader(new InputStreamReader(zabbixConnection.getInputStream()));
                    LOGGER.info("Message sent to Zabbix: " + message.toString());

                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to establish connection to Zabbix", e);
            return false;
        } finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
                if (zabbixConnection != null)
                {
                    zabbixConnection.close();
                }
            }
            catch (IOException e)
            {

            }

        }
        return true;
    }
}
